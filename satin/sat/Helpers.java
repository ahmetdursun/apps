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
     * @param n the number of choices
     * @return the information contents of this choice.
     */
    static public int information( int n )
    {
	if( n == 0 ){
	    n = 1;
	}
        return (1<<16)/(n*n*n);
    }
}
