/*
 MPI-Java version :
    Sang Lim (slim@npac.syr.edu)
    Northeast Parallel Architectures Center at Syracuse University
    12/2/98
*/
/* Ported to MPJ:
   Markus Bornemann
   Vrije Universiteit Amsterdam Department of Computer Science
   25/5/2005
*/
import ibis.mpj.*;

class scattervO {
  static public void test() throws MPJException {
    
    final int MAXLEN = 10;

    int myself,tasks;
    myself = MPJ.COMM_WORLD.rank();
    tasks = MPJ.COMM_WORLD.size();

    int root,i=0,j,k,stride=15;
    test out[] = new test[tasks*stride];
    test in[]  = new test[MAXLEN];
    int dis[] = new int[tasks];
    int scount[] = new int[tasks];

    
    for(i = 0; i < MAXLEN;i++){
       in[i] = new test();
       in[i].a = 0;
    }
    for(i=0;i<tasks;i++){
       dis[i] = i*stride;
       scount[i] = 5;
    }

    scount[0] = 10;
    
    for (i = 0;i < tasks*stride;i++){
       out[i] = new test();
       out[i].a = i;
    }
    
    MPJ.COMM_WORLD.scatterv(out, 0, scount, dis   , MPJ.OBJECT,
                            in , 0, scount[myself], MPJ.OBJECT, 0);

    String [] messbuf = new String [1] ;

    if(myself==0){
       System.out.println("Original array on root...");
       for(i=0; i<tasks*stride; i++)
          System.out.print(out[i].a+" ");
       System.out.println();
       System.out.println();

       System.out.println("Result on proc 0...");
       System.out.println("Stride = 15 "+"Count = "+scount[0]);
       for(i=0; i<MAXLEN; i++)
          System.out.print(in[i].a+" ");
       System.out.println();
       System.out.println();

       // Reproduces output of original test case, but deterministically

       int nmess = tasks < 3 ? tasks : 3 ;
       for(int t = 1 ; t < nmess ; t++) {
           MPJ.COMM_WORLD.recv(messbuf, 0, 1, MPJ.OBJECT, t, 0) ;

           System.out.print(messbuf [0]) ;
       }
    }
    
    if(myself==1){
        StringBuffer mess = new StringBuffer() ;

        mess.append("Result on proc 1...\n");
        mess.append("Stride = 15 "+"Count = "+scount[1] + "\n");
        for(i=0; i<MAXLEN; i++)
            mess.append(in[i].a+" ");
        mess.append("\n");
        mess.append("\n");

        messbuf [0] = mess.toString() ;
        MPJ.COMM_WORLD.send(messbuf, 0, 1, MPJ.OBJECT, 0, 0) ;
    }

    if(myself==2){
        StringBuffer mess = new StringBuffer() ;

        mess.append("Result on proc 2...\n");
        mess.append("Stride = 15 "+"Count = "+scount[2] + "\n");
        for(i=0; i<MAXLEN; i++)
            mess.append(in[i].a+" ");
        mess.append("\n");

        messbuf [0] = mess.toString() ;
        MPJ.COMM_WORLD.send(messbuf, 0, 1, MPJ.OBJECT, 0, 0) ;
    }

    if(myself == 0)  System.out.println("ScattervO TEST COMPLETE\n");
  
  }
  
  
  
  
  static public void main(String[] args) throws MPJException {
    MPJ.init(args);
    
    test();
    
    MPJ.finish();
  }
}

// Things to do
//
//   Make output deterministic by gathering and printing from root.

