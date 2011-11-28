package mazeoblig;

import java.rmi.*;
import java.util.List;

import simulator.PosPos;
import simulator.User;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public interface BoxMazeInterface extends Remote {
    public Box [][] getMaze() throws RemoteException;
	public int join(User u) throws RemoteException;
	public void drop(Integer i) throws RemoteException;
}
