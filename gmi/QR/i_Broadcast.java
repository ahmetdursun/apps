import ibis.group.GroupInterface;

interface i_Broadcast extends GroupInterface { 
	public void broadcast_it(double [] data);
}