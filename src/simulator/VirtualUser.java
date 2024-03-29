package simulator;

import java.awt.Color;
import java.awt.Graphics;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import mazeoblig.Box;
import mazeoblig.BoxMazeInterface;
import mazeoblig.Maze;
import mazeoblig.RMIServer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Stack;
import java.util.Arrays;
/**
 * Instansen av denne klassen tilbyr i praksis tre metoder til programmereren. Disse er:
 * <p>
 * a. Konstrukt�ren (som tar imot en Maze som parameter<br>
 * b. getFirstIterationLoop() som returnerer en rekke med posisjoner i Maze som finner veien<br>
 *    ut av Maze og reposisjonerer "spilleren" ved starten av Maze basert p� en tilfeldig <br>
 *    posisjonering av spilleren i Maze<br>
 * c. getIterationLoop() som returnerer en rekke med posisjoner i Maze som finner veien<br>
 *    ut av Maze (fra inngangen) og reposisjonerer "spileren" ved starten av Maze p� nytt<br>
 * <p>
 * Ideen er at programmereren skal kunne benytte disse ferdig definerte posisjonene til � simulere
 * hvordan en bruker forflytter seg i en labyrint.
 *     
 * @author asd
 *
 */
public class VirtualUser extends UnicastRemoteObject implements User {

	private static final long serialVersionUID = -528103055042584233L;
	private Box[][] maze;
	private int dim;

	 int xp;
	 int yp;
	 boolean found = false;

	private int id;
	private boolean trackback = true;
	
	private Stack <PositionInMaze> myWay = new Stack<PositionInMaze>();
	private PositionInMaze [] FirstIteration; 
	private PositionInMaze [] NextIteration;
	
	public Iterator<PositionInMaze> moves;
	private BoxMazeInterface boxmaze;
	private PositionInMaze pos;
	private HashMap<Integer,String> positions;
	private HashMap<Integer,User> users;
	
	private Color color = Color.green;

	/**
	 * Konstrukt�r
	 * @param bm
	 * @throws RemoteException 
	 */
	public VirtualUser(BoxMazeInterface bm, Color clr) throws RemoteException {
		color = setColor(clr);
		boxmaze = bm;
		users = new HashMap<Integer,User>();
		positions = new HashMap<Integer,String>();
		id = boxmaze.join(this);
		maze = boxmaze.getMaze();
		dim = maze[0].length;
		init();
	}
	
	@Override public int getId() { return id; }
	@Override public PositionInMaze getPos() { return pos; }
	
	@Override public String[] getAllPos() throws RemoteException {
		String[] usr = (String[])positions.values().toArray(new String[positions.values().size()]);
		return usr;
	}
	
	/**
	 * Set Color for this VirtualUser
	 * @param given
	 * @return
	 */
	public Color setColor (Color given) {
		if (given != null)
			return given;
		Random generator = new Random();
		Color[] array = {Color.black, Color.blue, Color.red, Color.pink, Color.yellow, Color.green, Color.orange}; 
        int rnd = generator.nextInt(array.length);
        return array[rnd];
    }
	
	public Color getColor () {
		return color != null ? color : Color.white;
	}
	
	/**
	 * Initsierer en tilfeldig posisjon i labyrint
	 * @throws RemoteException 
	 */
	private void init() throws RemoteException {
		/*
		 * Setter en tifeldig posisjon i maze (xp og yp)
		 */
		Random rand = new Random();
		xp = rand.nextInt(dim - 1) + 1;
		yp = rand.nextInt(dim - 1) + 1;

		System.out.println(xp + "::" + yp);
		// L�ser veien ut av labyrinten basert p� tilfeldig inngang ...
		makeFirstIteration();
		// og deretter l�ses labyrinten basert p� inngang fra starten 
		makeNextIteration();
		
		// Prepare moves
		turn();
	}
	
	/**
	 * Perform the next move
	 * @throws RemoteException
	 */
	public boolean move() throws RemoteException {
		if (moves.hasNext()) {
			pos = moves.next();
			announce();
			return true;
		}
		else turn();
		return true;
	}
	
	/**
	 * Makes the VirtualUser change his moves.
	 */
	public void turn() {
		moves = new Iterator<PositionInMaze>() {
			private int position = 0;

			public boolean hasNext() {
				if (trackback)
					return (position < getIterationLoop().length);
				else
					return (position < getFirstIterationLoop().length);
			}

			public PositionInMaze next() {
				if (hasNext()) {
					if (trackback)
						return getIterationLoop()[position++];
					else
						return getFirstIterationLoop()[position++];
				} else {
					throw new NoSuchElementException();
				}
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
		trackback = !trackback;
	}
	
	/**
	 * Add the given User to our list of known users.
	 */
	@Override
	public void join(Integer i, User u) throws RemoteException {
		users.put(i, u);
//		System.out.println(id + " knows about "+ i);
	}
	
	/**
	 * Remove the given User from our list of known users.
	 */
	@Override
	public void drop(Integer i) throws RemoteException {
		users.remove(i);
	}
	
	/**
     * Announce leaving the maze to all known users.
     */
	@Override
	public void leave() throws RemoteException {
		Iterator<User> it = users.values().iterator();
		while (it.hasNext())
			it.next().drop(id);
		boxmaze.drop(id);
	}
	
    /**
     * Announce the updated position to all known users
     */
	@Override
	public void announce() throws RemoteException {
		User[] usr = (User[])users.values().toArray(new User[users.values().size()]);
		for (int i = 0; i < usr.length; i++ ) {
//			System.out.println(id + " told " + i);
			usr[i].tellPos(id, pos.getXpos() + "," + pos.getYpos());
		}
	}
	
	/**
	 * Receive positionsfrom other users
	 */
	@Override
	public void tellPos(int i, String p) throws RemoteException {
//		System.out.println(id + " heard " + p + " from " + i);
		positions.put(i, p);
	}
	
	/**
	 * L�ser maze ut fra en tilfeldig posisjon i maze
	 * @throws RemoteException 
	 */
	private void solveMaze() throws RemoteException {
		found = false;
		int X = xp; int Y = yp;
		// Siden posisjonen er tilfeldig valgt risikerer man at man kj�rer i en br�nn
		// Av denne grunn .... det er noe galt med kallet under
		try {
			System.out.println(xp + "--" + yp);
			System.out.flush();
			myWay.push(new PositionInMaze(xp, yp));
			Box a[] = maze[xp];
			Box aa = a[yp];
			Box b = maze[1][0];
			backtrack(aa, b);
		} catch (ArrayIndexOutOfBoundsException e) { 
			e.printStackTrace();
		}
	}

	/**
	 * Selve backtracking-algoritmen som brukes for � finne l�sningen
	 * @param b Box
	 * @param from Box
	 * @throws RemoteException 
	 */
	private void backtrack(Box b, Box from) throws RemoteException {
		// Aller f�rst - basistilfellet, slik at vi kan returnere
		// Under returen skrives det med R�dt
		if ((xp == dim - 2) && (yp == dim - 2)) {
			found = true;
			// Siden vi tegner den "riktige" veien under returen opp gjennom
			// Java's runtime-stack, s� legger vi utgang inn sist ...
			return;
		}
		// Henter boksene som det finnes veier til fra den boksen jeg st�r i
		Box [] adj = b.getAdjecent();
		// Og sjekker om jeg kan g� de veiene
		for (int i = 0; i < adj.length; i++) {
			// Hvis boksen har en utganger som ikke er lik den jeg kom fra ...
			if (!(adj[i].equals(from))) {
				adjustXYBeforeBacktrack(b, adj[i]);
				myWay.push(new PositionInMaze(xp, yp));
				backtrack(adj[i], b);
				// Hvis algoritmen har funnet veien ut av labyrinten, s� inneholder stacken (myWay) 
				// veien fra det tilfeldige startpunktet og ut av labyrinten
				if (!found) myWay.pop();
				adjustXYAfterBacktrack(b, adj[i]);
			}
			// Hvis veien er funnet, er det ingen grunn til � fortsette
			if (found) {
				break;
			}
		}
	}

	/**
	 * Oppdatere x og y i labyrinten f�r backtracking kalles
	 * @param from Box
	 * @param to Box
	 */
	private void adjustXYBeforeBacktrack(Box from, Box to) {
		if ((from.getUp() != null) && (to.equals(from.getUp()))) yp--;
		if ((from.getDown() != null) && (to.equals(from.getDown()))) yp++;
		if ((from.getLeft() != null) && (to.equals(from.getLeft()))) xp--;
		if ((from.getRight() != null) && (to.equals(from.getRight()))) xp++;
	}

	/**
	 * Oppdatere x og y i labyrinten etter at backtracking er kalt
	 * @param from Box
	 * @param to Box
	 */
	private void adjustXYAfterBacktrack(Box from, Box to) {
		if ((from.getUp() != null) && (to.equals(from.getUp()))) yp++;
		if ((from.getDown() != null) && (to.equals(from.getDown()))) yp--;
		if ((from.getLeft() != null) && (to.equals(from.getLeft()))) xp++;
		if ((from.getRight() != null) && (to.equals(from.getRight()))) xp--;
	}

	/**
	 * Returnerer hele veien, fra tilfeldig startpunkt og ut av Maze som en array
	 * @return [] PositionInMaze 
	 * @throws RemoteException 
	 */
	private PositionInMaze [] solve() throws RemoteException {
		solveMaze();
		PositionInMaze [] pos = new PositionInMaze[myWay.size()];
		for (int i = 0; i < myWay.size(); i++)
			pos[i] = myWay.get(i);
		return pos;
	}

	/**
	 * Returnerer posisjonene som gir en vei rundt maze, tilfeldig valgt - mot h�yre eller mot venstre
	 * @return [] PositionInMaze;
	 * @throws RemoteException 
	 */
	private PositionInMaze [] roundAbout() throws RemoteException {
		PositionInMaze [] pos = new PositionInMaze[dim * 2];
		int j = 0;
		pos[j++] = new PositionInMaze(dim - 2, dim - 1);
		// Vi skal enten g� veien rundt mot h�yre ( % 2 == 0)
		// eller mot venstre
		if (System.currentTimeMillis() % 2 == 0) { 
			for (int i = dim - 1; i >= 0; i--)
				pos[j++] = new PositionInMaze(dim - 1, i);
			for (int i = dim - 1; i >= 1; i--)
				pos[j++] = new PositionInMaze(i, 0);
		}
		else {
			for (int i = dim - 1; i >= 1; i--)
				pos[j++] = new PositionInMaze(i, dim - 1);
			for (int i = dim - 1; i >= 0; i--)
				pos[j++] = new PositionInMaze(0, i);
		}
		// Uansett, s� returneres resultatet
		return pos;
	}

	/**
	 * L�ser hele maze, fra startposisjonen
	 * @return
	 * @throws RemoteException 
	 */
	private PositionInMaze [] solveFull() throws RemoteException {
		solveMaze();
		PositionInMaze [] pos = new PositionInMaze[myWay.size()];
		for (int i = 0; i < myWay.size(); i++)
			pos[i] = myWay.get(i);
		return pos;
	}

	/**
	 * Genererer opp veien ut av labyrinten fra en tilfeldig posisjon, samt veien 
	 * rundt og frem til inngangen av labyrinten 
	 * @throws RemoteException 
	 */
	private void makeFirstIteration() throws RemoteException {
		PositionInMaze [] outOfMaze = solve();
		PositionInMaze [] backToStart = roundAbout();
		FirstIteration = VirtualUser.concat(outOfMaze, backToStart);
	}

	/**
	 * Genererer opp veien ut av labyrinten fra inngangsposisjonen i labyrinten, samt veien 
	 * rundt og frem til inngangen av labyrinten igjen
	 * @throws RemoteException 
	 */
	private void makeNextIteration() throws RemoteException {
		// Tvinger posisjonen til � v�re ved inngang av Maze
		xp = 1; yp = 1;
		myWay = new Stack<PositionInMaze>();
		PositionInMaze [] outOfMaze = solve();
		PositionInMaze [] backToStart = roundAbout();
		NextIteration = VirtualUser.concat(outOfMaze, backToStart);
	}

	/**
	 * Generisk metode som sl�r sammen to arrayer av samme type
	 * @param <T>
	 * @param first
	 * @param second
	 * @return
	 */
	private static <T> T[] concat(T[] first, T[] second) {
		T[] result = Arrays.copyOf(first, first.length + second.length);
		System.arraycopy(second, 0, result, first.length, second.length);
		return result;
	}

	/**
	 * Returnerer en PositionInMaze [] som inneholder x- og y-posisjonene som 
	 * en virituell spiller benytter for � finne veien ut av labyrinten ut fra
	 * inngangen i labyrinten.
	 * @return
	 */
	public PositionInMaze [] getIterationLoop() {
		return NextIteration;
	}

	/**
	 * Returnerer en PositionInMaze [] som inneholder x- og y-posisjonene som 
	 * en virituell spiller benytter for � finne veien ut av labyrinten ut fra
	 * en tilfedlig generert startposisjon i labyrinten.
	 * @return
	 */
	public PositionInMaze [] getFirstIterationLoop() {
		return FirstIteration;
	}
}
