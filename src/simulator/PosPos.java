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
public interface PosPos extends Remote {
	public int getXpos() throws RemoteException;
	public int getYpos() throws RemoteException;
}