public final class Knapsack extends ibis.satin.SatinObject implements KnapsackInterface, java.io.Serializable  {

	static final int THRESHOLD = 20;
	static final int MAXVAL = 2000;
	static final int MAXWEIGHT = 1000;

	public Return spawn_try_it(int i, int tw, int av, int limw, int maxv, int[] values, int[] weights, byte[] s, byte[] opts) {
		return try_it(i, tw, av, limw, maxv, values, weights, s, opts);
	}

	Return try_it (int i, int tw, int av, int limw, int maxv, int[] values, int[] weights, byte[] s, byte[] opts) {
		int k,av1,maxv2;
		byte[] optdup = null;
		av1 = av - values[i];
		maxv2 = -1;
		Return ret1=null, ret2=null, result=null;
		int spawn1 = 0;
		int spawn2 = 0;
		int N = s.length-1;

		if ( tw + weights[i] <= limw ) {
			s[i] = 1;
			if ( i < N ) {
				maxv2 = maxv;
				optdup = (byte[]) opts.clone();

				if(i < THRESHOLD) {
					byte[] sdup = (byte[]) s.clone(); // only for spawn...
					ret1 = spawn_try_it(i+1, tw+weights[i], av, limw, maxv2, values, weights, sdup, optdup);
				} else {
					ret1 = try_it(i+1, tw+weights[i], av, limw, maxv2, values, weights, s, optdup);
				}
				spawn1 = 1;
			} else if ( av > maxv ) {
				maxv = av;
				System.arraycopy(s, 0, opts, 0, N+1);
			}
			s[i] = 0;
		}
		if ( av1 > maxv ) {
			if ( i < N ) {
				if(i < THRESHOLD) {
					byte[] sdup = (byte[]) s.clone(); // only for spawn...
					ret2 = spawn_try_it(i+1, tw, av1, limw, maxv, values, weights, sdup, opts);
				} else {
					ret2 = try_it(i+1, tw, av1, limw, maxv, values, weights, s, opts);
				}
				spawn2 = 1;
			} else {
				maxv = av1;
				System.arraycopy(s, 0, opts, 0, N+1);
			}
		}

		if(i < THRESHOLD) {
			sync();
		}

		if(spawn1 == 1) {
			maxv2 = ret1.maxv;
			optdup = ret1.opts;
			result = ret1;
		}
		if(spawn2 == 1) {
			maxv = ret2.maxv;
			opts = ret2.opts;
			result = ret2;
		}

		if(result == null) {
			result = new Return();
		}

		if (maxv2 > maxv) {
			result.maxv = maxv2;
			result.opts = optdup;
		} else {
			result.maxv = maxv;
			if(spawn2 == 1) {
				result.opts = opts;
			} else {
				result.opts = (byte[]) opts.clone(); // make a copy of param!
			}
		}

		return result;
	}


	private static void putmin(double[] prio, int[] a, int[] b, int pos) {
		int minpos = pos;
		double min = Integer.MAX_VALUE;

		for(int i=pos; i<prio.length; i++) {
			if(prio[i] == 0) prio[i] = Integer.MAX_VALUE;
			if(prio[i] < min) {
				minpos = i;
				min = prio[i];
			}
		}

		/* reverse elts in prio, a and b */
		double d;
		d = prio[pos];
		prio[pos] = prio[minpos];
		prio[minpos] = d;

		int tmp;
		tmp = a[pos];
		a[pos] = a[minpos];
		a[minpos] = tmp;

		tmp = b[pos];
		b[pos] = b[minpos];
		b[minpos] = tmp;
	}


	private static void sort(double[] prio, int[] a, int[] b) {
		for (int i=0; i<prio.length-1; i++) {
			putmin(prio, a, b, i); // zet min elt in a(vanaf i) op plaats i.
		}
	}


	public static void main(String[] args) {
		int N, i, limw, totv = 0;
		byte[] s, opts;
		long start, end;
		double time;
		int[] values;
		int[] weights;
		OrcaRandom r = new OrcaRandom();
		Knapsack k = new Knapsack();
		Return ret;

		if(args.length != 1) {
			System.out.println("Usage: knapsack <size>");
			System.exit(99);
		}

		N = Integer.parseInt(args[0]);

		s = new byte[N+1];
		opts = new byte[N+1];
		values = new int[N+1];
		weights = new int[N+1];
		double[] prios = new double[N+1];

		for (i=0; i<=N; i++) {
			weights[i] = r.nextInt() % MAXWEIGHT;
			values[i] = r.nextInt() % MAXVAL;
			prios[i] = (double) weights[i] / values[i];
			totv += values[i];
		}

		/*
		System.out.println("Table is now:");
		for (int o=0;o<N+1; o++) {
			System.out.println("index " + o + " weight " + weights[o] + " value " + values[o] + " prio " + prios[o]);
		}
		*/

		/* I think we want to sort on value/kg */
		sort(prios, values, weights);

		/*
		System.out.println("Table is now:");
		for (int o=0;o<N+1; o++) {
			System.out.println("index " + o + " weight " + weights[o] + " value " + values[o] + " prio " + prios[o]);
		}
		*/
		limw = N * MAXWEIGHT/2;

		System.out.println("knapsack started, N = " + N);
		start = System.currentTimeMillis();
		ret = k.spawn_try_it(1, 0, totv, limw, 0, values, weights, s, opts);
		k.sync();
		end = System.currentTimeMillis();
		time = (double) end - start;
		time /= 1000.0;

		System.out.println("application knapsack (" + N + ") took " + time + " seconds, result = max weight " +
				   limw + " has the value " + ret.maxv);

		for (i=1; i<=N; i++) {
			if (ret.opts[i]==1) {
				System.out.println("( " + weights[i] + " " + values[i] + " )");
			}
		}
	}
}
