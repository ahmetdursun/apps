// File: $Id$

/** Helper methods. */

class Helpers {
    /**
     * Given an array <code>a</code> and a size <code>sz</code>, create a new array of size <code>sz</code>
     * that contains the first <code>sz</code> elements of <code>a</code>.
     * @param a the array to clone
     * @param sz the number of elements to clone
     * @return the cloned array
     */
    static int[] cloneIntArray( int a[], int sz )
    {
        int res[] = new int[sz];

	System.arraycopy( a, 0, res, 0, sz );
	return res;
    }

    /**
     * Given a number of choices in a clause, returns the information
     * content of this choice.
     * @param n The number of choices.
     * @return The information contents of this choice.
     */
    static public float information( int n )
    {
	if( n == 0 ){
	    n = 1;
	}
        return 1.0f/(n*n*n);
    }

    /**
     * Prints the specified array of assignments to the error stream 
     * suppressing deduced assignments when possible.
     * @param assignment The array of assignments.
     * @param antecedent The array of antecedents, or null.
     */
    public static void dumpAssignments( String label, int assignment[], int antecedent[] )
    {
	System.err.print( label + ": " );
	for( int j=0; j<assignment.length; j++ ){
	    int v = assignment[j];
	    
	    if( v != -1 && (antecedent==null || antecedent[j] == -1) ){
		System.err.print( " v" + j + "=" + v );
	    }
	}
	System.err.println();
    }

}
