import ibis.rmi.*;

interface i_GlobalData extends Remote {

public i_SOR [] table(i_SOR me, int node) throws RemoteException;
public double reduceDiff(double value) throws RemoteException;

}