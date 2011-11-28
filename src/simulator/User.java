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
	public PositionInMaze getPos() throws RemoteException;
	public String[] getAllPos() throws RemoteException;
	public void join(Integer i, User u) throws RemoteException;
	public void drop(Integer i) throws RemoteException;
	public void announce() throws RemoteException;
	public void tellPos(int id, String pos) throws RemoteException;
	public void leave() throws RemoteException;
}
