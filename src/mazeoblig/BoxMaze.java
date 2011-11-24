package mazeoblig;

/************************************************************************
 * Denne koden skal ikke røres
 ***********************************************************************/

/**
 * <p>Title: BoxMaze</p>
 *
 * <p>Description: En 50 x 50 labyrint som er bygget opp av 50x50 bokser som er
 * satt ved siden av hverandre og under hverandre. </p>
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;

public class BoxMaze extends UnicastRemoteObject implements BoxMazeInterface
{
    private int maze[][];
    protected Box boxmaze[][];
    private int size = 50;
    /**
     * Konstruktør
     * Randomiserer opp en tilfeldig labyrint på 20 x 20 bokser hvor veggene
     * i mellom boksen er "fjernet slik at man får en labyint.
     */
    public BoxMaze() throws RemoteException {
        init(size);
    }

    public BoxMaze(int newSize) throws RemoteException {
        size = newSize;
        init(size);
    }
    /**
     * Genererer labyrinten. Koden er i all vesentlig grad hentet fra en enkel
     * algoritme som er publisert på http://en.wikipedia.org/wiki/Image:Maze.png
     *
     * Algoritmen er skrevet om til å håndtere boksene
     */
    private void init (int size) {
        int x, y, n, d;
        int dx[] = { 0, 0, -1, 1 };
        int dy[] = { -1, 1, 0, 0 };
        int todo[] = new int[size * size], todonum = 0;

        /* We want to create a maze on a grid. */
        maze = new int[size][size];


        /* We start with a grid full of walls. */
        for (x = 0; x < size; ++x)
            for (y = 0; y < size; ++y) {
                if (x == 0 || x == (size - 1) || y == 0 || y == (size - 1)) {
                    maze[x][y] = 32;
                } else {
                    maze[x][y] = 63;
                }
            }
        /* Select any square of the grid, to start with. */
        x = (int) (1 + Math.random () * (size - 2));
        y = (int) (1 + Math.random () * (size - 2));

        /* Mark this square as connected to the maze. */
        maze[x][y] &= ~48;

        /* Remember the surrounding squares, as we will */
        for (d = 0; d < 4; ++d)
            if ((maze[x + dx[d]][y + dy[d]] & 16) != 0) {
                /* want to connect them to the maze. */
                todo[todonum++] = ((x + dx[d]) << 16) | (y + dy[d]);
                maze[x + dx[d]][y + dy[d]] &= ~16;
            }

        /* We won't be finished until all is connected. */
        while (todonum > 0) {
            /* We select one of the squares next to the maze. */
            n = (int) (Math.random () * todonum);
            x = todo[n] >> 16;
            y = todo[n] & 65535;

            /* We will connect it, so remove it from the queue. */
            todo[n] = todo[--todonum];

            /* Select a direction, which leads to the maze. */
            do
                d = (int) (Math.random () * 4);
            while ((maze[x + dx[d]][y + dy[d]] & 32) != 0);

            /* Connect this square to the maze. */
            maze[x][y] &= ~((1 << d) | 32);
            maze[x + dx[d]][y + dy[d]] &= ~(1 << (d ^ 1));

            /* Remember the surrounding squares, which aren't */
            for (d = 0; d < 4; ++d)
                if ((maze[x + dx[d]][y + dy[d]] & 16) != 0) {

                    /* connected to the maze, and aren't yet queued to be. */
                    todo[todonum++] = ((x + dx[d]) << 16) | (y + dy[d]);
                    maze[x + dx[d]][y + dy[d]] &= ~16;
                }
            /* Repeat until finished. */
        }

        /* One may want to add an entrance and exit. */
        maze[1][1] &= ~1;
        maze[size - 2][size - 2] &= ~2;

        // Oppdatterer boksene, og antar at alle er forbundet med hverandre
        boxmaze = new Box[size][size];
        for (x = 0; x < boxmaze.length; x++) {
            for (y = 0; y < boxmaze[x].length; y++) {
                boxmaze[x][y] = new Box(maze[x][y]);
            }
        }
        for (x = 0; x < boxmaze.length; x++) {
            for (y = 0; y < boxmaze[x].length; y++) {
                boxmaze[x][y].setLeft( (x > 0                     ? boxmaze[x - 1][y] : null));
                boxmaze[x][y].setRight((x < boxmaze[x].length - 1 ? boxmaze[x + 1][y] : null));
                boxmaze[x][y].setUp(   (y > 0                     ? boxmaze[x][y - 1] : null));
                boxmaze[x][y].setDown( (y < boxmaze[y].length - 1 ? boxmaze[x][y + 1] : null));
                boxmaze[x][y].setValue(maze[x][y]);
             }
        }
        // Fjerner forbindelsene slik at vi kan representere en vegg
        for (x = 1; x < (size - 1); ++x)
            for (y = 1; y < (size - 1); ++y) {
                if ((boxmaze[x][y].getValue() & 1) != 0) {
                    boxmaze[x][y].setUp(null);
                    boxmaze[x][y - 1].setDown(null);
                }
                if ((boxmaze[x][y].getValue() & 2) != 0) {
                    boxmaze[x][y].setDown(null);
                    boxmaze[x][y + 1].setUp(null);
                }
                if ((boxmaze[x][y].getValue() & 4) != 0) {
                    boxmaze[x][y].setLeft(null);
                    boxmaze[x - 1][y].setRight(null);
                }
                if ((boxmaze[x][y].getValue() & 8) != 0) {
                    boxmaze[x][y].setRight(null);
                    boxmaze[x + 1][y].setLeft(null);
                }
            }
    }

    /**
     * Henter hele det aktuelle Maze
     * @return Box[][]
     * @throws RemoteException ved kommunikasjonsfeil
     */
    public Box [][] getMaze() throws RemoteException {
        return boxmaze;
    }


}
