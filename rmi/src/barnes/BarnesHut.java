/* $Id$ */

package barnes;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import ibis.poolInfo.PoolInfo;

strictfp public class BarnesHut {

    public static final int TRIES = 4;

    private GlobalData bhGd;

    private boolean bhDistributed;

    private Registry registry;

    Procs g;

    BarnesHut() {

        bhGd = new GlobalData();
        bhDistributed = false;
    }

    void setArgs(String args[]) {

        int i;
        boolean foundArg;

        for (i = 0; i < args.length; i++) {

            switch (args[i].charAt(1)) {

            case 'D':
                System.out.println("Using Direct (N^2) Method");
                bhGd.gdComputeAccelerationsDirect = true;
                break;
            case 'M':
                bhGd.gdMaxBodiesPerNode =
                        Integer.valueOf(args[i].substring(2)).intValue();
                // System.out.println("Using " + bhGd.gdMaxBodiesPerNode + "
                // bodies per leaf node");
                break;
            case 'N':
                bhGd.gdTotNumBodies =
                        Integer.valueOf(args[i].substring(2)).intValue();
                // System.out.println("Using " + bhGd.gdTotNumBodies + "
                // bodies");
                break;
            default:
                foundArg = false;

                if (args[i].equals("-procs")) {
                    foundArg = true;
                    bhGd.gdNumProcs = Integer.valueOf(args[i + 1]).intValue();
                    i++;
                    // System.out.println("Using " + bhGd.gdNumProcs + "
                    // Processors ");
                }

                if (args[i].equals("-tstop")) {
                    foundArg = true;
                    bhGd.gdEndTime = Double.valueOf(args[i + 1]).doubleValue();
                    i++;
                    // System.out.println("Using " + bhGd.gdEndTime + " as
                    // endtime");
                }

                if (args[i].equals("-theta")) {
                    foundArg = true;
                    bhGd.gdTheta = Double.valueOf(args[i + 1]).doubleValue();
                    i++;
                    // System.out.println("Using " + bhGd.gdTheta + " as
                    // theta");
                }

                if (args[i].equals("-dtime")) {
                    foundArg = true;
                    bhGd.gdDt = Double.valueOf(args[i + 1]).doubleValue();
                    i++;
                    // System.out.println("Using " + bhGd.gdDt + " as time
                    // delta");
                }

                if (args[i].equals("-eps")) {
                    foundArg = true;
                    bhGd.gdSoft = Double.valueOf(args[i + 1]).doubleValue();
                    i++;
                    // System.out.println("Using " + bhGd.gdSoft + " as
                    // epsilon");
                }

                if (args[i].equals("-das") || args[i].equals("-distributed")) {
                    foundArg = true;
                    bhDistributed = true;
                }
                if (args[i].equals("-serialize")) {
                    foundArg = true;
                    bhGd.gdSerialize = true;
                }

                if (args[i].equals("-trim-arrays")) {
                    foundArg = true;
                    bhGd.gdTrimArrays = true;
                }

                if (args[i].equals("-threads")) {
                    foundArg = true;
                    bhGd.gdThreads = true;
                }

                if (args[i].equals("-gc-interval")) {
                    foundArg = true;
                    bhGd.gdGCInterval = Integer.valueOf(args[i + 1]).intValue();
                    i++;
                }

                if (!foundArg)
                    System.out.println("Unknown option: '" + args[i] + "'");
            }
        }
    }

    void doProcInfo(int hostno, int nhosts, String hostname0) {
        if (hostno == 0) {
            try {
                registry =
                        LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
                g = new ProcsImpl(nhosts);
                registry.bind("ProcsInfo", g);
                return;
            } catch (Exception e) {
                System.err.println("Caught exception! " + e.getMessage());
                e.printStackTrace(System.err);
            }
        } else {
            for (int i = 0; i < TRIES; i++) {
                try {
                    registry =
                            LocateRegistry.getRegistry(hostname0,
                                    Registry.REGISTRY_PORT);
                    g = (Procs) registry.lookup("ProcsInfo");
                    return;
                } catch (Exception e) {
                    System.err.println("warn: remote object not found: " + e);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e2) {
                        // IGNORE
                    }
                }
            }
        }
        System.err.println("failed to bind/lookup remote object");
        System.exit(1);
    }

    void runMultiThreaded() {

        int procs = bhGd.gdNumProcs;

        Thread Processor[] = new Thread[procs];

        doProcInfo(0, 1, "");

        System.out.println("Running multithreaded (" + bhGd.gdNumProcs
                + " threads)");

        // Start all ProcessorThreads.

        for (int i = 0; i < procs; i++) {
            Processor[i] =
                    new ProcessorThread(bhGd.GenerateClone(), procs, i, g);
            Processor[i].start();
        }

        // Wait for all thread 0 to finish

        try {

            for (int i = 0; i < procs; i++) {
                Processor[i].join();
            }

        } catch (InterruptedException e) {
            System.err.println("Caught exception! " + e.getMessage());
            e.printStackTrace(System.err);
        }

        System.out.println("All threads finished!");
    }

    void runDistributed() {

        ProcessorThread p;

        PoolInfo d = null;

        try {
            d = new PoolInfo(null, true);
        } catch (Exception e) {
            System.err.println("Oops: " + e);
            e.printStackTrace();
            System.exit(1);
        }

        if (d.rank() == 0)
            System.out.println("Running distributed (" + d.size() + " nodes)");

        bhGd.gdNumProcs = d.size();

        // System.out.println("Initializing!" );

        bhGd.Initialize();

        // System.out.println("Initialized!" );

        doProcInfo(d.rank(), d.size(), d.getInetAddress(0).getHostAddress());

        p = new ProcessorThread(bhGd, d, g);

        p.start();

        try {

            p.join();

        } catch (InterruptedException e) {
            System.err.println("Caught exception! " + e.getMessage());
            e.printStackTrace(System.err);
        }

        if (d.rank() == 0) {
            try {
                registry.unbind("ProcsInfo");
            } catch (Exception e) {
            }
            System.out.println("Finished!");
        }

        System.exit(0);
    }

    public void run() {
        if (bhDistributed) {
            runDistributed();
        } else {
            bhGd.Initialize();
            runMultiThreaded();
        }
    }

    public static void main(String argv[]) {
        BarnesHut bh = new BarnesHut();
        bh.setArgs(argv);
        bh.run();
    }
}
