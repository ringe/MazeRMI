package simulator;

import java.awt.Color;
import java.awt.Graphics;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import mazeoblig.Box;
import mazeoblig.BoxMazeInterface;
import mazeoblig.Maze;
import mazeoblig.RMIServer;

import java.util.Random;
import java.util.Stack;
import java.util.Arrays;
/**
 * Instansen av denne klassen tilbyr i praksis tre metoder til programmereren. Disse er:
 * <p>
 * a. Konstruktøren (som tar imot en Maze som parameter<br>
 * b. getFirstIterationLoop() som returnerer en rekke med posisjoner i Maze som finner veien<br>
 *    ut av Maze og reposisjonerer "spilleren" ved starten av Maze basert på en tilfeldig <br>
 *    posisjonering av spilleren i Maze<br>
 * c. getIterationLoop() som returnerer en rekke med posisjoner i Maze som finner veien<br>
 *    ut av Maze (fra inngangen) og reposisjonerer "spileren" ved starten av Maze på nytt<br>
 * <p>
 * Ideen er at programmereren skal kunne benytte disse ferdig definerte posisjonene til å simulere
 * hvordan en bruker forflytter seg i en labyrint.
 *     
 * @author asd
 *
 */
public class VirtualUser {

	private Box[][] maze;
	private int dim;

	static int xp;
	static int yp;
	static boolean found = false;

	
	private Stack <PositionInMaze> myWay = new Stack();
	private PositionInMaze [] FirstIteration; 
	private PositionInMaze [] NextIteration; 

	/**
	 * Konstruktør
	 * @param maze
	 */
	public VirtualUser(Box[][] maze) {
		this.maze = maze;
		dim = maze[0].length;
		init();
	}
	/**
	 * Initsierer en tilfeldig posisjon i labyrint
	 */
	private void init() {
		/*
		 * Setter en tifeldig posisjon i maze (xp og yp)
		 */
		Random rand = new Random();
		xp = rand.nextInt(dim - 1) + 1;
		yp = rand.nextInt(dim - 1) + 1;

		// Løser veien ut av labyrinten basert på tilfeldig inngang ...
		makeFirstIteration();
		// og deretter løses labyrinten basert på inngang fra starten 
		makeNextIteration();
	}
	
	/**
	 * Løser maze ut fra en tilfeldig posisjon i maze
	 */
	private void solveMaze() {
		found = false;
		// Siden posisjonen er tilfeldig valgt risikerer man at man kjører i en brønn
		// Av denne grunn .... det er noe galt med kallet under
		myWay.push(new PositionInMaze(xp, yp));
		backtrack(maze[xp][yp], maze[1][0]);
	}

	/**
	 * Selve backtracking-algoritmen som brukes for å finne løsningen
	 * @param b Box
	 * @param from Box
	 */
	private void backtrack(Box b, Box from) {
		// Aller først - basistilfellet, slik at vi kan returnere
		// Under returen skrives det med Rødt
		if ((xp == dim - 2) && (yp == dim - 2)) {
			found = true;
			// Siden vi tegner den "riktige" veien under returen opp gjennom
			// Java's runtime-stack, så legger vi utgang inn sist ...
			return;
		}
		// Henter boksene som det finnes veier til fra den boksen jeg står i
		Box [] adj = b.getAdjecent();
		// Og sjekker om jeg kan gå de veiene
		for (int i = 0; i < adj.length; i++) {
			// Hvis boksen har en utganger som ikke er lik den jeg kom fra ...
			if (!(adj[i].equals(from))) {
				adjustXYBeforeBacktrack(b, adj[i]);
				myWay.push(new PositionInMaze(xp, yp));
				backtrack(adj[i], b);
				// Hvis algoritmen har funnet veien ut av labyrinten, så inneholder stacken (myWay) 
				// veien fra det tilfeldige startpunktet og ut av labyrinten
				if (!found) myWay.pop();
				adjustXYAfterBacktrack(b, adj[i]);
			}
			// Hvis veien er funnet, er det ingen grunn til å fortsette
			if (found) {
				break;
			}
		}
	}

	/**
	 * Oppdatere x og y i labyrinten før backtracking kalles
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
	 */
	private PositionInMaze [] solve() {
		solveMaze();
		PositionInMaze [] pos = new PositionInMaze[myWay.size()];
		for (int i = 0; i < myWay.size(); i++)
			pos[i] = myWay.get(i);
		return pos;
	}

	/**
	 * Returnerer posisjonene som gir en vei rundt maze, tilfeldig valgt - mot høyre eller mot venstre
	 * @return [] PositionInMaze;
	 */
	private PositionInMaze [] roundAbout() {
		PositionInMaze [] pos = new PositionInMaze[dim * 2];
		int j = 0;
		pos[j++] = new PositionInMaze(dim - 2, dim - 1);
		// Vi skal enten gå veien rundt mot høyre ( % 2 == 0)
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
		// Uansett, så returneres resultatet
		return pos;
	}

	/**
	 * Løser hele maze, fra startposisjonen
	 * @return
	 */
	private PositionInMaze [] solveFull() {
		solveMaze();
		PositionInMaze [] pos = new PositionInMaze[myWay.size()];
		for (int i = 0; i < myWay.size(); i++)
			pos[i] = myWay.get(i);
		return pos;
	}

	/**
	 * Genererer opp veien ut av labyrinten fra en tilfeldig posisjon, samt veien 
	 * rundt og frem til inngangen av labyrinten 
	 */
	private void makeFirstIteration() {
		PositionInMaze [] outOfMaze = solve();
		PositionInMaze [] backToStart = roundAbout();
		FirstIteration = VirtualUser.concat(outOfMaze, backToStart);
	}

	/**
	 * Genererer opp veien ut av labyrinten fra inngangsposisjonen i labyrinten, samt veien 
	 * rundt og frem til inngangen av labyrinten igjen
	 */
	private void makeNextIteration() {
		// Tvinger posisjonen til å være ved inngang av Maze
		xp = 1; yp = 1;
		myWay = new Stack();
		PositionInMaze [] outOfMaze = solve();
		PositionInMaze [] backToStart = roundAbout();
		NextIteration = VirtualUser.concat(outOfMaze, backToStart);
	}

	/**
	 * Generisk metode som slår sammen to arrayer av samme type
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
	 * en virituell spiller benytter for å finne veien ut av labyrinten ut fra
	 * inngangen i labyrinten.
	 * @return
	 */
	public PositionInMaze [] getIterationLoop() {
		return NextIteration;
	}

	/**
	 * Returnerer en PositionInMaze [] som inneholder x- og y-posisjonene som 
	 * en virituell spiller benytter for å finne veien ut av labyrinten ut fra
	 * en tilfedlig generert startposisjon i labyrinten.
	 * @return
	 */
	public PositionInMaze [] getFirstIterationLoop() {
		return FirstIteration;
	}
}
