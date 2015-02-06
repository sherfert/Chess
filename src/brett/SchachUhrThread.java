package brett;

import java.util.Calendar;
import java.util.TimeZone;

import javax.swing.JLabel;

import figuren.Figur.Farbe;

/**
 * Implementierung der Schachuhr.
 * 
 * Erstellen mit Konstruktor.
 * 
 * Starten mir start()
 * 
 * Anhalten mit stopTimer()
 * 
 * Weiterzaehlen mit continueTimer()
 * 
 * Komplett anhalten mit interrupt()
 */
public class SchachUhrThread extends Thread {

	/**
	 * Die Farbe des Spielers der Schachuhr.
	 */
	private Farbe farbe;

	/**
	 * Die zu aktualisierende Anzeige
	 */
	private JLabel anzeige;

	/**
	 * Die Anzahl von Millisekunden, auf die diese Schachuhr gestellt werden
	 * soll.
	 */
	private long millisekunden;

	/**
	 * Jedes mal, wenn der Timer gestartet oder fortgesetzt wird, wird dieser
	 * Variable auf die aktuelle Anzahl von Millisekunden gestellt.
	 */
	private long startMillisekunden;

	/**
	 * Die Systemzeit, zu der der Timer gestartet oder fortgesetzt wurde
	 */
	private long startTime;

	/**
	 * Speichert, ob die Uhr gerade laeuft.
	 */
	private boolean running = false;

	/**
	 * Speichert, on die Uhr laufend (oder stehend) starten soll.
	 */
	private boolean startetLaufend;

	/**
	 * Ein Calendar zur Berechnung der Zeiteinheiten.
	 */
	private Calendar cal;
	
	/**
	 * Das Brett des SchachUhrThreads
	 */
	private Brett brett;

	/**
	 * @param brett
	 * 			  Das Brett des SchachUhrThreads
	 * @param minuten
	 *            Die Minuten, auf die diese Schachuhr gestellt werden soll.
	 * @param farbe
	 *            Die Farbe des Spielers der Schachuhr.
	 * @param anzeige
	 *            Ein JLabel, in dem die (Rest)Zeit angezeigt werden soll.
	 * @param startetLaufend
	 *            Ob die Schachuhr direkt mit Starten des Threads mitgestartet
	 *            werden soll
	 */
	public SchachUhrThread(Brett brett, int minuten, Farbe farbe, JLabel anzeige,
			boolean startetLaufend) {
		super();
		this.brett = brett;
		this.millisekunden = 60000 * minuten;
		this.farbe = farbe;
		this.anzeige = anzeige;
		this.startetLaufend = startetLaufend;

		cal = Calendar.getInstance();
		// Britische Zeit setzen, da System.currentTimeMillis davon ausgeht
		cal.setTimeZone(TimeZone.getTimeZone("GMT"));
		refreshTime();
	}

	/**
	 * 
	 */
	@Override
	public void run() {
		continueTimer();
		if (!startetLaufend) {
			running = false;
		}

		while (!isInterrupted() && millisekunden > 0) {
			if (running) {
				// Zeit runterzaehlen
				millisekunden = startMillisekunden
						- (System.currentTimeMillis() - startTime);

				// Pruefung ob Zeit abgelaufen
				if (millisekunden <= 0) {
					// Es soll ja nicht 23:59:59 angezeigt werden, ne?
					millisekunden = 0;
					refreshTime();
					timeOver();
					break;
				}
				// JLabel aktualisieren
				refreshTime();
			}

			// Jeweils 500 ms schlafen
			try {
				sleep(500);
			} catch (InterruptedException e) {
				interrupt();
			}
		}
	}

	/**
	 * Aktualisiert die Zeitanzeige
	 */
	private void refreshTime() {
		cal.setTimeInMillis(millisekunden);

		/*
		 * siehe
		 * http://download.oracle.com/javase/6/docs/api/java/util/Formatter
		 * .html#syntax
		 */
		anzeige.setText(String.format("%tT", cal));
	}

	/**
	 * Stoppt den Timer temporaer.
	 */
	public void stopTimer() {
		running = false;
	}

	/**
	 * Setzt den Timer fort.
	 */
	public void continueTimer() {
		startTime = System.currentTimeMillis();
		startMillisekunden = millisekunden;
		running = true;
	}

	/**
	 * Wird aufgerufen, wenn die Zeit zu Ende ist.
	 */
	private void timeOver() {
		System.out.println("Zeit von " + farbe.toString() + " ist vorbei.");
		brett.endGame(farbe.andereFarbe(), "abgelaufener Zeit");
	}
}