/* $Id$ */

package tsp;

public class DistanceTable implements java.io.Serializable {

    private int[][] table;

    public DistanceTable(int size) {
        table = new int[size][size];
    }

    public static DistanceTable readTable(String filename) {
        DistanceTable dt;
        Input in;
        int size;

        in = new Input(filename);
        size = in.readInt();

        //		size = 10;
        dt = new DistanceTable(size);

        System.out.println("table size = " + size);

        in.readln();
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                dt.table[i][j] = in.readInt();
            }
            in.readln();
        }

        return dt;
    }

    public int getSize() {
        return table.length;
    }

    public int distance(int from, int to) {
        return table[from][to];
    }
}
