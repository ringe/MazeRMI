package mazeoblig;

import java.rmi.*;
import java.util.List;

import simulator.PosPos;

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
    public PosPos[] announce(int id, PosPos p) throws RemoteException;
    public int join(PosPos p) throws RemoteException;
}
