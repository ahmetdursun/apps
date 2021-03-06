/* $Id$ */

package barnes;

import ibis.poolInfo.PoolInfo;

public class ProcessorThread extends Thread {

    GlobalData g;

    PoolInfo d;

    int myProc, numProcs;

    OrbTree orbTree;

    Procs p;

    private void GenerateBodiesPlummer() {
        int i;

        for (i = 0; i < g.gdTotNumBodies; i++) {
            g.gdBodies[i] = new Body();
            g.gdBodies[i].bWeight = 100;
        }
        g.gdPlummer.Generate(g.gdBodies, g.gdTotNumBodies);
    }

    private void PrintBody(int i) {
        if (g.logger.isDebugEnabled()) {
            g.logger.debug("Body " + i + ": [ " + g.gdBodies[i].bPos.x + ", "
                + g.gdBodies[i].bPos.y + ", " + g.gdBodies[i].bPos.z + " ]");
            g.logger.debug("     " + i + ": [ " + g.gdBodies[i].bVel.x + ", "
                + g.gdBodies[i].bVel.y + ", " + g.gdBodies[i].bVel.z + " ]");
            g.logger.debug("     " + i + ": [ " + g.gdBodies[i].bAcc.x + ", "
                + g.gdBodies[i].bAcc.y + ", " + g.gdBodies[i].bAcc.z + " ]");
            g.logger.debug("     " + i + ": [ " + g.gdBodies[i].bMass + " ]");
        //        g.logger.debug("     " + i + ": " + g.gdBodies[i].bNumber);
        }
    }

    private void PrintBodies() {

        int i;

        for (i = 0; i < g.gdNumBodies; i++)
            PrintBody(i);
    }

    void MainSetup() {

        g.InitializeBodies();

        g.logger.info("initializing Orb Tree");

        orbTree = new OrbTree(g);

        g.logger.info("initialized Orb Tree");

        try {

            g.gdNumBodies = 0;

            if (myProc == 0) {

                // generate bodies

                GenerateBodiesPlummer();

                g.logger.info("broadcasting bodies");

                g.Proc.broadcastBodies();

            } else {

                g.logger.info("receiving bodies\n");

                g.Proc.receiveBodies();
            }

        } catch (Exception e) {
            System.out
                .println(myProc + ": Exception caught, terminating! " + e);
            e.printStackTrace();
            System.exit(-1);
        }

        if (g.logger.isDebugEnabled()) {
            g.logger.info("totNumBodies: " + g.gdTotNumBodies + ", numBodies "
                + g.gdNumBodies);
        }

    }

    void MainLoop() {

        BodyTree tree;
        long tStart, tTree, tCOFM, tAccels, tNewPos, tUpdateCOFM, tEssentialTree, tLoadBalance, totalMillis, interactions, totEssentialTree, totTree, totAccels, totNewPos, totUpdateCOFM, totCOFM, totLoadBalance, tGC, totGC;
        int level;

        if (g.GD_PRINT_BODIES) PrintBodies();

        totalMillis = 0;
        totEssentialTree = 0;
        totTree = 0;
        totAccels = 0;
        totNewPos = 0;
        totUpdateCOFM = 0;
        totCOFM = 0;
        totLoadBalance = 0;
        totGC = 0;

        try {

            for (g.gdIteration = 0; g.gdIteration < g.gdIterations; g.gdIteration++) {

                if (g.Proc.myProc == 0)
                    System.out.println("Computing iteration: " + g.gdIteration);

                g.logger.info("Computing iteration: " + g.gdIteration);

                tStart = System.currentTimeMillis();

                //	g.logger.debug( "iteratie " + g.gdIteration + ", voor update, bodies: " + g.gdNumBodies );

                orbTree.Update();

                //	g.logger.debug( "iteratie " + g.gdIteration + ", na update, bodies: " + g.gdNumBodies );

                // Balance the load

                if (g.gdIteration == 0) {
                    level = g.gdLogProcs;
                } else {
                    level = orbTree.determineLoadBalanceLevel();
                }

                g.logger.info("load balancing, level = " + level);

                orbTree.LoadBalance(level);

                //        g.logger.debug("finished loadbalance (level: " + level + ")" );

                tLoadBalance = System.currentTimeMillis();

                // Load balancing finished, build the local tree...

                //        g.logger.debug("Building tree");

                tree = new BodyTree(g, orbTree.getGlobalMin(), orbTree
                    .getGlobalMax());

                tTree = System.currentTimeMillis();

                tree.ComputeCenterOfMass();

                //	g.logger.debug("CenterOfMass computed!");

                tCOFM = System.currentTimeMillis();
                /*
                 g.logger.debug( "bodies: " + tree.dumpTree( 0, 10 ));
                 */
                orbTree.ExchangeEssentialTree(tree);

                //      	g.logger.debug("Essential trees updated");

                tEssentialTree = System.currentTimeMillis();

                tree.ComputeCenterOfMass();

                //	g.logger.debug("CenterOfMass updated!");
                /*
                 g.logger.debug( "bodies: " + tree.dumpTree( 0, 10 ));
                 */
                tUpdateCOFM = System.currentTimeMillis();

                if (g.gdComputeAccelerationsDirect) interactions = tree
                    .ComputeAccelerationsDirect();
                else interactions = tree.ComputeAccelerationsBarnes();

                tAccels = System.currentTimeMillis();

                tree.ComputeNewPositions();

                tNewPos = System.currentTimeMillis();

                if ((g.gdGCInterval > 0)
                    && ((g.gdIteration % g.gdGCInterval) == 0)) {
                    tree = null;
                    System.gc();
                }

                tGC = System.currentTimeMillis();

                g.logger.info("Iteration " + g.gdIteration + ":");
                g.logger.info("Load balancing: " + (tLoadBalance - tStart)
                    + " ms (level " + level + ")");
                g.logger.info("Tree construction: " + (tTree - tLoadBalance)
                    + " ms");
                g.logger.info("Center of mass computation: " + (tCOFM - tTree)
                    + " ms");
                g.logger.info("Essential tree transmission: "
                    + (tEssentialTree - tCOFM) + " ms ");
                g.logger.info("Center of mass update: "
                    + (tUpdateCOFM - tEssentialTree) + " ms");
                g.logger.info("Acceleration computation: "
                    + (tAccels - tUpdateCOFM) + " ms (" + interactions
                    + " interactions) ");
                g.logger.info("New position computation: " + (tNewPos - tAccels)
                    + " ms");
                g.logger.info("Explicit Garbage Collection: " + (tGC - tNewPos)
                    + " ms");

                g.logger.info("Total: " + (tGC - tStart) + " ms\n");

                if (g.gdIteration > 2) {
                    totalMillis += (tGC - tStart);
                    totLoadBalance += (tLoadBalance - tStart);
                    totTree += (tTree - tLoadBalance);
                    totCOFM += (tCOFM - tTree);
                    totEssentialTree += (tEssentialTree - tCOFM);
                    totUpdateCOFM += (tUpdateCOFM - tEssentialTree);
                    totAccels += (tAccels - tUpdateCOFM);
                    totNewPos += (tNewPos - tAccels);
                    totGC += (tGC - tNewPos);
                }
            }
        } catch (NullPointerException n) {

            System.out
                .println("Nullpointer exception caught (tree inconsistent),"
                    + " during iteration " + g.gdIteration + ": "
                    + n.getMessage());
            n.printStackTrace();
        }

        g.logger.info("stats; Bodies: " + g.gdTotNumBodies + ", Bodies per Leaf: "
            + g.gdMaxBodiesPerNode + ", Processors: " + g.gdNumProcs + "");

        g.logger.info("stats; Total time: " + totalMillis + " ms, average: "
            + (totalMillis / (g.gdIterations - 3)) + " ms per iteration");

        g.logger.info("stats; time spent in load balancing:              "
            + totLoadBalance + " ms ("
            + ((totLoadBalance * 1.0) / (totalMillis * 1.0) * 100.0)
            + " percent)");

        g.logger.info("stats; time spent in tree construction:           "
            + totTree + " ms ("
            + ((totTree * 1.0) / (totalMillis * 1.0) * 100.0) + " percent)");

        g.logger.info("stats; time spent in center of mass computation:  "
            + totCOFM + " ms ("
            + ((totCOFM * 1.0) / (totalMillis * 1.0) * 100.0) + " percent)");

        g.logger.info("stats; time spent in essential tree exchange:     "
            + totEssentialTree + " ms ("
            + ((totEssentialTree * 1.0) / (totalMillis * 1.0) * 100.0)
            + " percent)");

        g.logger.info("stats; time spent in center of mass update:  "
            + totUpdateCOFM + " ms ("
            + ((totUpdateCOFM * 1.0) / (totalMillis * 1.0) * 100.0)
            + " percent)");

        g.logger.info("stats; time spent in force computation:           "
            + totAccels + " ms ("
            + ((totAccels * 1.0) / (totalMillis * 1.0) * 100.0) + " percent)");

        g.logger.info("stats; time spent in position update:             "
            + totNewPos + " ms ("
            + ((totNewPos * 1.0) / (totalMillis * 1.0) * 100.0) + " percent)");

        g.logger.info("stats; time spent in explicit garbage collection: " + totGC
            + " ms (" + ((totGC * 1.0) / (totalMillis * 1.0) * 100.0)
            + " percent)");

        if (g.GD_PRINT_BODIES) PrintBodies();

        if (g.gdMyProc == 0) {
            System.err.println("Barnes, " + g.gdTotNumBodies + " bodies took "
                + totalMillis + " ms");
        }
    }

    // used for multithread runs
    ProcessorThread(GlobalData g, int numProcs, int myProc, Procs p) {

        this.g = g;
        this.p = p;
        this.myProc = myProc;
        this.numProcs = numProcs;

        g.gdMyProc = myProc;
        g.logger.debug("Logfile for processor " + myProc + " of " + numProcs);

        try {
            g.Proc = new ProcessorImpl(g, numProcs, myProc, p);
        } catch (Exception e) {
            System.err.println("Exception: " + e);
        }
    }

    ProcessorThread(GlobalData g, PoolInfo d, Procs p) {

        this.g = g;
        this.p = p;
        this.d = d;
        this.myProc = d.rank();
        this.numProcs = d.size();

        g.gdMyProc = myProc;
        g.logger.debug("Logfile for processor " + myProc + " of " + numProcs);

        //    g.logger.debug("Creating Processor Implementation");

        if (ProcessorImpl.VERBOSE) {
            System.out.println("creating ProcImpl");
        }

        try {
            g.Proc = new ProcessorImpl(g, d, p);
        } catch (Exception e) {
            g.logger.debug("exceptie gevangen!!!!!!!: " + e);
            e.printStackTrace();
            System.exit(-1);
        }
        if (ProcessorImpl.VERBOSE) {
            System.out.println("created ProcImpl");
        }

        //    g.logger.debug("Created Processor Implementation");
    }

    public void run() {

        try {

            g.logger.debug("ProcessorThread: running");

            g.Proc.register();

            g.logger.debug("ProcessorThread: calling MainSetup");

            MainSetup();

            g.logger.debug("ProcessorThread: starting MainLoop");

            MainLoop();

            g.logger.debug("ProcessorThread: finished mainloop");

            g.Proc.cleanup();

        } catch (Exception e) {

            System.out.println("caught exception: " + e);
            e.printStackTrace();
            System.exit(-1);
        }
    }
}
