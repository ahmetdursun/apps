/* Sun RMI One Dimensional Fast Fourier Transformation (FFT)

   author: Ronald Blankendaal
   email : rcblanke@cs.vu.nl
   date  : 10-5-1999
*/

import ibis.rmi.registry.*;
import ibis.rmi.*;
import java.net.*;
import java.io.*;
import ibis.util.PoolInfo;

class fft {

    int M, N, rootN, rowsperproc, cpus, host;
    Master masterObject;
    String masterName;
    Registry local;
    double[] u, u2;
    int[][] distribution;

    DataOutputStream output = null;

    fft(String[] argv) {
	PoolInfo d = new PoolInfo();

	host = d.rank();
	cpus = d.size();
	masterName = d.hostName(0);
	if (argv.length == 0) {
	    M = 16;
	} else if (argv.length != 1) {
	    System.out.println("Parameters: M");
	    System.exit(1);
	} else {
	    try {
		M = Integer.parseInt(argv[0]);
	    } catch (NumberFormatException e) {
		System.out.println(e.getMessage());
		System.exit(1);
	    }
	}

	N = (1 << M);
	rootN = (1 << (M / 2));
	rowsperproc = rootN / cpus;

	u = new double[2 * rootN];
	u2= new double[2 * (N + rootN)];
	initU(u);
	initU2(u2);
	distribution = new int[2][cpus * cpus];
	initdis();

	try {
	    local = RMI_init.getRegistry(masterName);
	} catch (IOException e) {
	    System.out.println("Fail to connect to registry: exception " + e);
	    System.exit(49);
	}

	if (host == 0) {
	    System.out.println("Sun RMI One Dimensional Fast Fourier Transformation");
	    System.out.println("M:" + M + " N:" + N + " rootN:" + rootN +
			       "  Running on " + cpus + " cpu(s), each having " +
			       rowsperproc + " rows to process");
	    try {
	        masterObject = new Master(local, cpus, M, rowsperproc);
	    } catch (Exception e) {
	        System.out.println("Exception: " + e);
	        fatal("Couldn't create master");
	    }
	}

	try {
	    new Slave(masterName, host, cpus, N, M, rootN,
		      rowsperproc, u, u2, distribution, d);
	} catch (Exception e) {
	    System.out.println("Exception: " + e);
	    fatal("Couldn't create slave");
	}

        if (host == 0) {
            masterObject.unbind(local);
        }

	System.exit(0);
    }


    void fatal(String message) {
	System.err.println(message);
	System.exit(1);
    }


    void initdis() {
	int cpu = 0;
	for (int i = 0; i < cpus * cpus; i++) {
	    distribution[0][(cpu) % (cpus * cpus)] = i / cpus;
	    distribution[1][(cpu) % (cpus * cpus)] = i % cpus;
	    cpu += (cpus + 1);
	}
    }


    void initU(double[] u) {
	for (int q = 0; (1 << q) < N; q++) {
	    int n1 = 1 << q;
	    int base = n1 - 1;
	    for (int j = 0; j < n1; j++) {
		if (base + j > rootN - 1)
		    return;
		u[(base + j)*2] = Math.cos(2.0 * Math.PI * j / (2 * n1));
		u[(base + j)*2 + 1] = -Math.sin(2.0 * Math.PI * j / (2 * n1));
	    }
	}
    }


    void initU2(double[] u2) {
	for (int j = 0; j < rootN; j++) {
	    int k = j * rootN;
	    for (int i = 0; i < rootN; i++) {
		u2[(k + i)*2] = Math.cos(2.0 * Math.PI * i * j / N);
		u2[(k + i)*2 + 1] = -Math.sin(2.0 * Math.PI * i * j / N);
	    }
	}
    }


    void start() {
    }


    public static void main(String argv[]) {
	new fft(argv).start();
    }
}