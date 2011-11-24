package mazeoblig;
/************************************************************************
 * Denne koden skal ikke røres
 ***********************************************************************/
import java.util.Vector;
/**
 * <p>Title: Box</p>
 *
 * <p>Description: Representerer et kvadrat/boks i en labyrint
 *    som består av en rekke bokser som står ved siden av hverandre.</p>
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: </p>
 *
 * @author Asbjørn D.
 * @version 1.0
 */
import java.io.Serializable;

public class Box implements Serializable {
    private Box Up = null;
    private Box Down = null;
    private Box Left = null;
    private Box Right = null;
    private int Value = 0;

    /**
     * Konstruktør - lager en selvstendig boks og angir en verdi som
     * temporært brukes til å bestemme hvilke "vegger" som skal være
     * i boksen.
     * @param value int Temporær verdi
     */
    public Box(int value) {
        Value = value;
    }

    /**
     * Konstruktør, på lik linje med Box(int value), men som til forskjell
     * også angir hvilke andre bokser som grenser til denne boksen
     * @param up Box
     * @param down Box
     * @param left Box
     * @param right Box
     * @param value int
     */
    public Box ( Box up, Box down, Box left, Box right, int value) {
        Up = up;
        Down = down;
        Left = left;
        Right = right;
        Value = value;
    }

    /**
     * Henter boksen over denne boks.
     * Hvis det ikke finnes en boks over, returneres null.
     * @return Box
     */
    public Box getUp() { return Up; }
    /**
     * Henter boksen under denne boks.
     * Hvis det ikke finnes en boks under, returneres null.
     * @return Box
     */
    public Box getDown() { return Down; }
    /**
     * Henter boksen til venstre for denne boks.
     * Hvis det ikke finnes en boks til venstre, returneres null.
     * @return Box
     */
    public Box getLeft() { return Left; }
    /**
     * Henter boksen til høyre for denne boks.
     * Hvis det ikke finnes en boks til høyre, returneres null.
     * @return Box
     */
    public Box getRight() { return Right; }
    /**
     * Henter ut den temporære verdien som brukes for å bestemme sammensetningen
     * av vegger.
     * @return int
     */
    public int getValue() { return Value; }

    /**
     * Setter boksen over denne boks
     * @param value Box
     */
    public void setUp(Box value) { Up = value; }
    /**
     * Setter boksen under denne boks.
     * @param value Box
     */
    public void setDown(Box value) { Down = value; }
    /**
     * Setter boksen til venstre for denne boks.
     * @param value Box
     */
    public void setLeft(Box value) { Left = value; }
    /**
     * Setter boksen til høyre for denne boks
     * @param value Box
     */
    public void setRight(Box value) { Right = value; }
    /**
     * Setter den temporære verdien som brukes for å bestemme sammensetningen
     * av vegger.
     * @param value int
     */
    public void setValue(int value) { Value = value; }

    /**
     * Returnerer boksene som ligger inntil denne boksen og hvor det ikke er en
     * vegg mellom boksene
     * @return Box[]
     */
    public Box  [] getAdjecent() {
        Vector adj = new Vector();
        if (Down != null) adj.add(Down);
        if (Right != null) adj.add(Right);
        if (Up != null) adj.add(Up);
        if (Left != null) adj.add(Left);

        Box [] retval = new Box[adj.size()];

        for (int i = 0; i < adj.size(); i++) retval[i] = (Box)adj.elementAt(i);
        return retval;
    }

    public boolean equals(Box b) {
        if (this.hashCode() == b.hashCode())
            return true;
        else
            return false;
    }
}
