package spieler;

import brett.Brett;
import figuren.Dame;
import figuren.Figur;

/**
 * Ein KI-gesteurter PC-Spieler
 * 
 * Subklassen muessen run() implementieren und dort jeweils ihren Zug
 * vollziehen lassen. Sie sollten in run() auf isInterrupted pruefen, und
 * dann die Rechnungen abbrechen.
 */
public abstract class PCSpieler extends Spieler implements Runnable {

	/**
	 * Der rechnende Thread
	 */
	private Thread rechner;
	
	/**
	 * Ein neuer PC-Spieler.
	 * 
	 * @param brett
	 * 			  Das Brett auf dem der Spieler spielt.
	 */
	protected PCSpieler(Brett brett) {
		super(brett);
	}

	/**
	 * Weist den PC-Spieler darauf hin, dass er dran ist. Ein neuer Thread,
	 * der den Zug vollziehen soll, wird gestartet.
	 */
	public void notifyTurn() {
		rechner = new Thread(this);
		rechner.start();
	}

	/**
	 * @return false
	 */
	public boolean istMensch() {
		return false;
	}
	
	/**
	 * PC-Gegner waehlen grundsaetzlich die Dame.
	 */
	@Override
	public Class<? extends Figur> getNewBauerClass() {
		return Dame.class;
	}
	
	/**
	 * Unterbricht den Thread
	 */
	public void interrupt() {
		if(rechner != null) {
			rechner.interrupt();
		}
	}
	
	/**
	 * @return ob der Thread unterbrochen wurde.
	 */
	public boolean isInterrupted() {
		if(rechner != null) {
			return rechner.isInterrupted();
		} else {
			return false;
		}
	}
}