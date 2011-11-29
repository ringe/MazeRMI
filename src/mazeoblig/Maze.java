package mazeoblig;

import java.awt.*;
import java.applet.*;

import simulator.*;

/**
 *
 * <p>Title: Maze</p>
 *
 * <p>Description: En enkel applet som viser den randomiserte labyrinten</p>
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
import java.rmi.RemoteException;
import java.rmi.NotBoundException;

/**
 * Tegner opp maze i en applet, basert p� definisjon som man finner p� RMIServer
 * RMIServer p� sin side  henter st�rrelsen fra definisjonen i Maze
 * @author asd
 *
 */
public class Maze extends Applet {

	private static final long serialVersionUID = 1L;
	private BoxMazeInterface bm;
	private Box[][] maze;
	public static int DIM = 60;
	private int dim = DIM;

	static int xp;
	static int yp;
	static boolean found = false;

	private String server_hostname;
	private int server_portnumber;
	private VirtualUser self = null;

	/**
	 * Henter labyrinten fra RMIServer
	 */
	public void init() {
		/*
		 ** Kobler opp mot RMIServer, under forutsetning av at disse
		 ** kj�rer p� samme maskin. Hvis ikke m� oppkoblingen
		 ** skrives om slik at dette passer med virkeligheten.
		 */
		if (server_hostname == null)
			server_hostname = RMIServer.getHostName();
		if (server_portnumber == 0)
			server_portnumber = RMIServer.getRMIPort();
		try {
			java.rmi.registry.Registry r = java.rmi.registry.LocateRegistry.
			getRegistry(server_hostname, server_portnumber);

			/*
			 ** Henter inn referansen til Labyrinten (ROR)
			 */
			bm = (BoxMazeInterface) r.lookup(RMIServer.MazeName);
			maze = bm.getMaze();
			
			/*
			 * Simulerer et antall spillere
			 */
			LotsOfPlayers pl = new LotsOfPlayers(100);
			pl.setDaemon(true);
			pl.start();
			
			/*
			 * Starter periodisk repaint.
			 */
			Painter pa = new Painter();
			pa.setDaemon(true);
			pa.start();
		}
		catch (RemoteException e) {
			e.printStackTrace();
			System.err.println("Remote Exception: " + e.getMessage());
			System.exit(0);
		}
		catch (NotBoundException f) {
			/*
			 ** En exception her er en indikasjon p� at man ved oppslag (lookup())
			 ** ikke finner det objektet som man s�ker.
			 ** �rsaken til at dette skjer kan v�re mange, men v�r oppmerksom p�
			 ** at hvis hostname ikke er OK (RMIServer gir da feilmelding under
			 ** oppstart) kan v�re en �rsak.
			 */
			System.err.println("Not Bound Exception: " + f.getMessage());
			System.exit(0);
		}
	}
	
	// Thread to start a number of players
	class LotsOfPlayers extends Thread {
		private int n;
		LotsOfPlayers(int c) { n = c; }
		public void run() {
			for ( int i = n; i != 0; i--) {
				Worker w = new Worker();
				w.setDaemon(true);
				w.start();
				try {
					sleep(150);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private class Worker extends Thread {
		public void run(){
			try {
				// Create a new user for this maze.
				VirtualUser vu = new VirtualUser(bm, (self == null ? null : Color.white));
				
				if (self == null)
					self = vu;
				
				// Move until all moves done.
				while (true) {
					if (!vu.move())
						break;
					sleep(150);
				}
			}
			catch (InterruptedException ex) {
				ex.printStackTrace();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
	    }
	}
	
	private class Painter extends Thread {
		public void run() {
			try {
				while(true) {
					Thread.sleep(150);
			    	repaint();
				}
			} catch (InterruptedException ie) {
				ie.printStackTrace();
			}
		}
	}
	
	//Get a parameter value
	public String getParameter(String key, String def) {
		return getParameter(key) != null ? getParameter(key) : def;
	}
	//Get Applet information
	public String getAppletInfo() {
		return "Applet Information";
	}

	//Get parameter info
	public String[][] getParameterInfo() {
		java.lang.String[][] pinfo = { {"Size", "int", ""},
		};
		return pinfo;
	}

	/**
	 * Viser labyrinten / tegner den i applet
	 * @param g Graphics
	 */
	public void paint (Graphics g) {
		int x, y;

		// Tegner baser p� box-definisjonene ....

		for (x = 1; x < (dim - 1); ++x)
			for (y = 1; y < (dim - 1); ++y) {
				if (maze[x][y].getUp() == null)
					g.drawLine(x * 10, y * 10, x * 10 + 10, y * 10);
				if (maze[x][y].getDown() == null)
					g.drawLine(x * 10, y * 10 + 10, x * 10 + 10, y * 10 + 10);
				if (maze[x][y].getLeft() == null)
					g.drawLine(x * 10, y * 10, x * 10, y * 10 + 10);
				if (maze[x][y].getRight() == null)
					g.drawLine(x * 10 + 10, y * 10, x * 10 + 10, y * 10 + 10);
			}
		
		
		if (self != null) {
			try {
				String[] pos = self.getAllPos();
				for (int i = 0; i < pos.length; i++)
					drawThem(g, pos[i]);
				drawSelf(g);
			} catch (RemoteException e) {}
		}
	}
	
	private void drawSelf(Graphics g) throws RemoteException {
		PosPos myPos = self.getPos();
		g.setColor(Color.yellow);
		g.fillOval((myPos.getXpos() * 10) + 2, (myPos.getYpos() * 10) + 2, 7, 7);
		g.setColor(Color.black);
	}
	
	private void drawThem(Graphics g, String where) {
		String[] coord = where.split(",");
		int x = new Integer(coord[0]);
		int y = new Integer(coord[1]);
		g.setColor(Color.red);
		g.fillOval((x * 10) + 2, (y * 10) + 2, 7, 7);
		g.setColor(Color.black);
	}
}

