import java.io.InputStream;
import java.io.OutputStream;

import ibis.io.ArrayInputStream;
import ibis.io.ArrayOutputStream;
import ibis.io.BufferedArrayInputStream;
import ibis.io.BufferedArrayOutputStream;
import ibis.io.MantaInputStream;
import ibis.io.MantaOutputStream;

public class Main {

	public static final boolean DEBUG = false;
	public static final int LEN   = 1024*1024;
	public static final int COUNT = 100;
	public static final int TESTS = 10;
		
	public static double round(double val) { 		
		return (Math.ceil(val*100.0)/100.0);
	} 

	public static void main(String args[]) {
		
		try {
			long start, end;
			int bytes;
		
			double best_ktp = 0.0;
			long best_time = 1000000;

			// System.out.println("Main starting");
			
			StoreBuffer buf = new StoreBuffer();
			StoreArrayOutputStream out = new StoreArrayOutputStream(buf);
			StoreArrayInputStream in = new StoreArrayInputStream(buf);

			MantaOutputStream mout = new MantaOutputStream(out);
			MantaInputStream min = new MantaInputStream(in);
				
			// Create array
			byte [] temp = new byte[LEN];
			
			// System.out.println("Writing byte[" + LEN + "]");

			mout.writeObject(temp);
			mout.flush();
			mout.reset();

//			System.out.println("Wrote " + out.getAndReset() + " bytes");
			
			System.out.print("Read +new -cnv byte[" + (LEN) + "]\t");
			min.readObject();
			in.reset();
			buf.clear();

//			System.out.println("Rewriting byte[" + (LEN) + "]");

			mout.writeObject(temp);
			mout.flush();
			mout.reset();

			bytes = out.getAndReset();

//			System.out.println("Wrote " + bytes + " bytes");
			
//			System.out.println("Starting test");

			for (int j=0;j<TESTS;j++) { 

				start = System.currentTimeMillis();
				
				for (int i=0;i<COUNT;i++) {
					min.readObject();
					in.reset();
				}
				
				end = System.currentTimeMillis();
				
				long time = end-start;
				double kb = COUNT*LEN;
				double ktp = ((1000.0*kb)/(1024*1024))/time;
				
//				System.out.println();
//				System.out.println("Read took " + time + " ms");
//				System.out.println("Bytes read " + kb + " throughput = " + ktp + " MBytes/s");

				if (time < best_time) { 
					best_time = time;
					best_ktp = ktp;
				}
			} 

			System.out.println("" + round(best_ktp));
			temp = null;
			in.reset();
			buf.clear();
			best_time= 1000000;
			/*********************************/

				
			// Create array
			int [] temp1 = new int[LEN/4];
			
			// System.out.println("Writing int[" + (LEN/4) + "]");

			mout.writeObject(temp1);
			mout.flush();
			mout.reset();

//			System.out.println("Wrote " + out.getAndReset() + " bytes");
			
			System.out.print("Read +new -cnv int[" + (LEN/4) + "]\t");
			min.readObject();
			in.reset();
			buf.clear();

//			System.out.println("Rewriting int[" + (LEN/4) + "]");

			mout.writeObject(temp1);
			mout.flush();
			mout.reset();

			bytes = out.getAndReset();

//			System.out.println("Wrote " + bytes + " bytes");
			
//			System.out.println("Starting test");

			for (int j=0;j<TESTS;j++) { 

				start = System.currentTimeMillis();
				
				for (int i=0;i<COUNT;i++) {
					min.readObject();
					in.reset();
				}
				
				end = System.currentTimeMillis();
				
				long time = end-start;
				double kb = COUNT*LEN;
				double ktp = ((1000.0*kb)/(1024*1024))/time;
				
//				System.out.println();
//				System.out.println("Read took " + time + " ms");
//				System.out.println("Bytes read " + kb + " throughput = " + ktp + " MBytes/s");

				if (time < best_time) { 
					best_time = time;
					best_ktp = ktp;
				}
			} 

			System.out.println("" + round(best_ktp));
			temp1 = null;
			in.reset();
			buf.clear();
			best_time= 1000000;
			/*********************************/

			// Create array
			byte [] temp2 = new byte[LEN];
			
<<<<<<< Main.java
			// System.out.println("Writing long[" + (LEN/8) + "]");
=======
			System.out.println("Writing byte[" + (LEN) + "]");
>>>>>>> 1.3

			mout.writeObject(temp2);
			mout.flush();
			mout.reset();

//			System.out.println("Wrote " + out.getAndReset() + " bytes");
			
			System.out.print("Read +new -cnv long[" + (LEN/8) + "]\t");
			min.readObject();
			in.reset();
			buf.clear();

//			System.out.println("Rewriting long[" + (LEN/8) + "]");

			mout.writeObject(temp2);
			mout.flush();
			mout.reset();

			bytes = out.getAndReset();

//			System.out.println("Wrote " + bytes + " bytes");
			
//			System.out.println("Starting test");

			for (int j=0;j<TESTS;j++) { 

				start = System.currentTimeMillis();
				
				for (int i=0;i<COUNT;i++) {
					min.readObject();
					in.reset();
				}
				
				end = System.currentTimeMillis();
				
				long time = end-start;
				double kb = COUNT*LEN;
				double ktp = ((1000.0*kb)/(1024*1024))/time;
				
//				System.out.println();
//				System.out.println("Read took " + time + " ms");
//				System.out.println("Bytes read " + kb + " throughput = " + ktp + " MBytes/s");

				if (time < best_time) { 
					best_time = time;
					best_ktp = ktp;
				}
			} 

			System.out.println("" + round(best_ktp));
			temp2 = null;
			in.reset();
			buf.clear();
			best_time= 1000000;
			/*********************************/

			// Create array
			double [] temp3 = new double[LEN/8];
			
			// System.out.println("Writing double[" + (LEN/8) + "]");

			mout.writeObject(temp3);
			mout.flush();
			mout.reset();

//			System.out.println("Wrote " + out.getAndReset() + " bytes");
			
			System.out.print("Read +new -cnv double[" + (LEN/8) + "]\t");
			min.readObject();
			in.reset();
			buf.clear();

			//		System.out.println("Rewriting double[" + (LEN/8) + "]");

			mout.writeObject(temp3);
			mout.flush();
			mout.reset();

			bytes = out.getAndReset();

//			System.out.println("Wrote " + bytes + " bytes");
			
//			System.out.println("Starting test");

			for (int j=0;j<TESTS;j++) { 

				start = System.currentTimeMillis();
				
				for (int i=0;i<COUNT;i++) {
					min.readObject();
					in.reset();
				}
				
				end = System.currentTimeMillis();
				
				long time = end-start;
				double kb = COUNT*LEN;
				double ktp = ((1000.0*kb)/(1024*1024))/time;
				
//				System.out.println();
//				System.out.println("Read took " + time + " ms");
//				System.out.println("Bytes read " + kb + " throughput = " + ktp + " MBytes/s");

				if (time < best_time) { 
					best_time = time;
					best_ktp = ktp;
				}
			} 

			System.out.println("" + round(best_ktp));
			temp3 = null;


		} catch (Exception e) {
			System.out.println("Main got exception " + e);
			e.printStackTrace();
		}
	}
}


