package simulator;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class PositionInMaze extends UnicastRemoteObject implements PosPos {

	private static final long serialVersionUID = 1539230392527715735L;
	private int xpos, ypos;
	
	public PositionInMaze(int xp, int yp) throws RemoteException {
		xpos = xp;
		ypos = yp;
	}

	public int getXpos() throws RemoteException {
		return xpos;
	}

	public int getYpos() throws RemoteException {
		return ypos;
	}
	
	public String toString() {
		return "xpos: " + xpos + "\typos: " + ypos;
	}
}
