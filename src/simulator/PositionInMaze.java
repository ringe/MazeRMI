package simulator;

public class PositionInMaze {
	private int xpos, ypos;
	
	public PositionInMaze(int xp, int yp) {
		xpos = xp;
		ypos = yp;
	}

	public int getXpos() {
		return xpos;
	}

	public int getYpos() {
		return ypos;
	}
	
	public String toString() {
		return "xpos: " + xpos + "\typos: " + ypos;
	}
}
