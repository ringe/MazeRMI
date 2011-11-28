/**
 * 
 */
package simulator;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * @author runar
 *
 */
public interface User extends Remote {
	public int getId() throws RemoteException;
	public PosPos getPos() throws RemoteException;
	public Object[] getOthers() throws RemoteException;
	public void join(Integer i, User u) throws RemoteException;
	public void drop(Integer i) throws RemoteException;
	public void announce() throws RemoteException;
	public void tellPos(int id, PosPos pos) throws RemoteException;
	public void leave() throws RemoteException;
}
