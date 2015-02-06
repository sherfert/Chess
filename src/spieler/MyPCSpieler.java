package spieler;

import java.util.ArrayList;

import brett.Brett;
import brett.Brett.Situation;
import brett.Brett.Zug;
import brett.MainWindow;

/**
 * Mein PC-Spieler
 */
public class MyPCSpieler extends PCSpieler {
	/**
	 * Die Schwierigkeit (Zuege die nach dem eigenen noch betrachtet werden)
	 */
	private int schwierigkeit;

	/**
	 * Ob der PC-Spieler agressiv spielen soll oder nicht.
	 */
	private boolean agressiv;

	/**
	 * Die tiefe Wertigkeit des aktuell auszufuehrenden Zuges
	 */
	private int alpha;

	/**
	 * Die Liste von besten Zuegen bei den aktuellen Berechnungen.
	 */
	private ArrayList<Zug> besteZuege;

	/**
	 * Die Anzahl an moeglichen Zuegen.
	 */
	private int anzahlMoeglicherZuege;

	/**
	 * Die Anzahl an betrachteten Zuegen
	 */
	private int anzahlBetrachteterZuege;

	/**
	 * Ein neuer PC-Spieler.
	 * 
	 * @param brett
	 *            Das Brett auf dem der Spieler spielt.
	 * @param schwierigkeit
	 *            Eine nicht-negative Zahl fuer die Schwierigkeit. Bereits 5 ist
	 *            relativ rechenintensiv.
	 * @param agressiv
	 *            Ob der PC-Spieler agressiv spielen soll oder nicht.
	 */
	public MyPCSpieler(Brett brett, int schwierigkeit, boolean agressiv) {
		super(brett);
		this.agressiv = agressiv;
		this.schwierigkeit = schwierigkeit;
		if (this.schwierigkeit < 0) {
			this.schwierigkeit = 0;
		}
	}

	/**
	 * Fuehrt den besten Zug aus. Wenn es mehrere gleich gute gibt, wird
	 * zufaellig einer ausgewaehlt.
	 */
	@Override
	public void run() {
		long zeit = System.currentTimeMillis();
		// besteZuege fuellen lassen
		fillBesteZuege();
		// auf das reale Brett umrechnen
		ArrayList<Zug> besteZuege = new ArrayList<Zug>(this.besteZuege.size());
		for (int i = 0; i < this.besteZuege.size(); i++) {
			Zug alterZug = this.besteZuege.get(i);
			besteZuege
					.add(brett.new Zug(alterZug.getVon(), alterZug.getNach()));
		}

		zeit = 1000 - (System.currentTimeMillis() - zeit);
		/*
		 * Wenn die Reaktion weniger als eine Sekunde gedauert hat, wird diese
		 * Zeit durch Schlafen aufgefuellt.
		 */
		if (zeit > 0) {
			try {
				Thread.sleep(zeit);
			} catch (InterruptedException e) {
				interrupt();
			}
		}

		int startIndex;
		int endIndex;
		Zug besterZug;

		if (agressiv) {
			/*
			 * Bei agressiver Spielweise werden innerhalb der besten Zuege die
			 * mit der besten flachen Wertigkeit ausgewaehlt. Die besten Zuege
			 * sind bereits nach flacher Wertigkeit sortiert. Als Grenze wird
			 * der endIndex innerhalb der besten Zuege errechnet. startIndex ist
			 * 0.
			 */
			startIndex = 0;
			endIndex = besteZuege.size();
			int ersteWertigkeit = besteZuege.get(0).wertigkeit();
			for (int i = 1; i < besteZuege.size(); i++) {
				if (besteZuege.get(i).wertigkeit() < ersteWertigkeit) {
					endIndex = i;
					break;
				}
			}
		} else {
			/*
			 * Bei nicht-agressiver Spielweise werden genau andersherum die mit
			 * der schlechtesten flachen Wertigkeit ausgewaehlt. endIndex ist
			 * also die Groesse der Liste, startIndex muss neu berechnet werden.
			 */
			endIndex = besteZuege.size();
			startIndex = 0;
			int letzteWertigkeit = besteZuege.get(besteZuege.size() - 1)
					.wertigkeit();
			for (int i = besteZuege.size() - 2; i >= 0; i--) {
				if (besteZuege.get(i).wertigkeit() > letzteWertigkeit) {
					startIndex = i + 1;
					break;
				}
			}

			if (alpha > 900) {
				/*
				 * Wenn Matt gesetzt werden kann oder Patt "herausgeholt" werden
				 * kann, soll dies auch trotz nicht-agressiver Spielweise sofort
				 * getan werden. Dafuer wird bei allen Zuegen nochmal geguckt,
				 * ob ein Zug direkt zum Matt/Patt fuehrt.
				 */
				for (int i = 0; i < besteZuege.size(); i++) {
					Zug zug = besteZuege.get(i);
					zug.fuehreAus(false);
					boolean ende = brett.getSpieler(this, false)
							.generiereMoeglicheZuege().isEmpty();
					zug.rueckgaengig();

					if (ende) {
						startIndex = endIndex = i;
						break;
					}
				}
			}
		}

		// Nun wird ein zufaelliger bestimmt.
		int randPos = (int) ((Math.random() * (endIndex - startIndex)) + startIndex);
		besterZug = besteZuege.get(randPos);

		// Wenn das Spiel zu Ende ist, sollte nichts mehr veraendert werden.
		if (isInterrupted()) {
			return;
		}

		besterZug.fuehreAus(true);
	}

	/**
	 * Fuellt die Liste mit besten Zuegen
	 */
	private void fillBesteZuege() {
		/*
		 * Fuer die initialen Alpha- und Beta-Grenze wurde nicht
		 * Integer.MIN/MAX_VALUE genutzt, da es sonst bei der Negation dieser
		 * Werte zu Fehlern kommt. Alpha dient gleichzeitig als maximaler Wert
		 * der Zuege. Beta wird bei jedem Thread lokal gesetzt.
		 */
		alpha = -100000;

		besteZuege = new ArrayList<Zug>();
		// Wenn das Spiel zu Ende ist, sollte nichts mehr geprueft werden.
		if (isInterrupted()) {
			return;
		}
		ArrayList<Zug> moeglicheZuege = brett.getSpieler(this, true)
				.generiereMoeglicheZuege();

		if (schwierigkeit == 0) {
			/*
			 * Es wird nur der aktuelle Zug betrachtet, daher werden alle Zuege
			 * in der Wertigkeit verglichen und die besten zurueckgegeben. Es
			 * werden nicht mehrere Threads erstellt.
			 */
			for (Zug zug : moeglicheZuege) {
				if (zug.wertigkeit() >= alpha) {
					if (zug.wertigkeit() > alpha) {
						besteZuege.clear();
						alpha = zug.wertigkeit();
					}
					besteZuege.add(zug);
				}
			}
			// fertig!
			MainWindow.setProgress(100);
			return;
		}
		
		// Es muss mindestens noch der Folgezug betrachtet werden.
		anzahlMoeglicherZuege = moeglicheZuege.size();
		anzahlBetrachteterZuege = 0;
		MainWindow.setProgress(0);

		int cores = Runtime.getRuntime().availableProcessors();
		MyPCSpielerThread[] threads = new MyPCSpielerThread[cores];

		threads[0] = new MyPCSpielerThread(this, 0, anzahlMoeglicherZuege
				/ cores, moeglicheZuege, true);

		for (int i = 1; i < cores; i++) {
			threads[i] = new MyPCSpielerThread(brett.clone().getSpieler(
					figuren.get(0).getFarbe()), anzahlMoeglicherZuege * i
					/ cores, anzahlMoeglicherZuege * (i + 1) / cores,
					moeglicheZuege, false);
		}

		for (MyPCSpielerThread thread : threads) {
			thread.start();
		}

		for (MyPCSpielerThread thread : threads) {
			try {
				thread.join();
			} catch (InterruptedException e) {
				interrupt();
			}
		}
		// fertig!
		MainWindow.setProgress(100);
	}

	/**
	 * Erhoeht den Fortschritt um den Prozentwert eines Zuges.
	 */
	private synchronized void increaseFortschritt() {
		anzahlBetrachteterZuege++;
		MainWindow.setProgress(anzahlBetrachteterZuege * 100
				/ anzahlMoeglicherZuege);
	}

	/**
	 * Ueberprueft, ob ein ausgewaehlter Zug zu den besten gehoert, bzw.
	 * vielleicht auch der Beste ist. Die Liste der besten Zuege wird dann
	 * synchronisiert angepasst.
	 * 
	 * @param wert
	 *            Der tiefe Wert des Zuges
	 * @param zug
	 *            Der Zug
	 */
	private synchronized void checkAndSetBesteZuege(int wert, Zug zug) {
		if (wert >= alpha) {
			if (wert > alpha) {
				alpha = wert;
				besteZuege.clear();
			}
			besteZuege.add(zug);
		}
	}

	/**
	 * Ein Thread, der Berechnungen aus höchster Ebene ausfuehrt. Gedacht fuer
	 * Aufteilung der Rechnung auf mehrere Threads.
	 */
	private class MyPCSpielerThread extends Thread {
		/**
		 * Eine Spieler-Referenz auf den eigenen Klon auf dem Brett, das dem
		 * Thread fuer Berechnungen zugrundeliegt
		 */
		private Spieler self;

		/**
		 * Das Brett, das dem Thread fuer Berechnungen zugrundeliegt
		 */
		private Brett brett;

		/**
		 * Die (auf dem hier relevanten Brett) moeglichen Zuege
		 */
		private ArrayList<Zug> moeglicheZuege;

		/**
		 * Konstruktor, bei dem die moeglichen Zuege uebernommen werden koennen.
		 * Sollen sie nicht uebernommen werden, (weil auf einem anderen Brett
		 * gerchnet wird), so werden sie aus den uebergebenen neu generiert.
		 * 
		 * @param self
		 *            Eine Spieler-Referenz auf den eigenen Klon auf dem Brett,
		 *            das dem Thread fuer Berechnungen zugrundeliegt
		 * @param von
		 *            Anfangs-Index
		 * @param nach
		 *            Der erste nicht mehr (!!) zu betrachtende Index
		 * @param moeglicheZuege
		 *            Liste von moeglichen Zuegen, die, wenn sie uebernommen
		 *            werden sollen, auf dem Brett von self erstellt worden sein
		 *            muessen
		 * @param uebernehmen
		 *            ob die Liste von moeglichen Zuegen uebernommen (true) oder
		 *            neu generiert werden (false) soll.
		 */
		public MyPCSpielerThread(Spieler self, int von, int nach,
				ArrayList<Zug> moeglicheZuege, boolean uebernehmen) {
			this.self = self;
			this.brett = self.brett;
			this.moeglicheZuege = new ArrayList<Zug>();

			if (uebernehmen) {
				// Die noetigen Zuege werden uebernommen
				for (int i = von; i < nach; i++) {
					this.moeglicheZuege.add(moeglicheZuege.get(i));
				}
			} else {
				// Die noetigen Zuege werden auf dem neuen Brett neu generiert.
				for (int i = von; i < nach; i++) {
					Zug alterZug = moeglicheZuege.get(i);
					this.moeglicheZuege.add(brett.new Zug(alterZug.getVon(),
							alterZug.getNach()));
				}
			}
		}

		/**
		 * Tut seinen Teil fuer die Berechnung der besten Zuege.
		 */
		@Override
		public void run() {
			int beta = 100000;

			for (int i = 0; i < moeglicheZuege.size(); i++) {
				if (i > 0) {
					// aktuellen Fortschritt aktualisieren.
					increaseFortschritt();
				}

				// Zug ausfuehren
				Zug zug = moeglicheZuege.get(i).fuehreAus(false);
				int wert = zug.wertigkeit();

				/*
				 * Mit rekursivem Aufruf werden die besten Zuege des Gegners
				 * geholt.
				 */
				Integer gegnerZug = besterZug(schwierigkeit - 1, -beta + wert,
						-alpha + wert, false);

				// Mache Zug rueckgaengig
				zug.rueckgaengig();

				/*
				 * Die Wertigkeit des gegnerischen Zuges wird mit der Wertigkeit
				 * des betrachteten Zuges verrechnet und gespeichert. Wenn
				 * gegnerZug null ist, wurde dort wegen einem beta-Cut
				 * aufgehoert und hier kann der naechste Zug betrachtet werden.
				 */
				if (gegnerZug != null) {
					wert -= gegnerZug;
				} else {
					continue;
				}
				// Auf der hoechsten Ebene kann es keine Beta-Cuts geben!

				/*
				 * Gucke, ob es sich um den besten Zug handelt. Dieser Wert wird
				 * dann ggf. angepasst.
				 */
				checkAndSetBesteZuege(wert, zug);
			}
			// fertig!
		}

		/**
		 * Gibt die Wertigkeit des besten Zuges.
		 * 
		 * @param tiefe
		 *            Wie viele Schritte in die Berechnung einfliessen sollen
		 * @param alpha
		 *            Alpha-Grenze, bei initialem Aufruf theoretisch -unendlich
		 * @param beta
		 *            Beta-Grenze, bei initialem Aufruf theoretisch +unendlich
		 * @param my
		 *            Ob gerade die eigenen (true) oder die gegnerischen (false)
		 *            besten Zuege berechnet werden sollen.
		 * @return die Wertigkeit des besten Zuges oder null bei einem beta-cut
		 */
		
		private Integer besterZug(int tiefe, int alpha, int beta, boolean my) {
			Integer result = -100000;
			// Wenn das Spiel zu Ende ist, sollte nichts mehr geprueft werden.
			if (isInterrupted()) {
				return result;
			}
			Spieler spieler = brett.getSpieler(self, my);
			ArrayList<Zug> moeglicheZuege = spieler.generiereMoeglicheZuege();

			// Hole Brettsituation
			Situation situation = brett.brettSituation(spieler, moeglicheZuege);

			switch (situation) {
			case MATT:
				// Matt, also seeehr schlecht.
				return -10000;
			case PATT:
			case R50Z:
				int wertigkeitsDifferenz = spieler.figurenWertigkeit()
						- brett.getSpieler(self, !my).figurenWertigkeit();
				if (wertigkeitsDifferenz > 0) {
					/*
					 * Ich habe eine bessere Stellung, Remis waere also eher
					 * schlecht
					 */
					return -1000;
				} else if (wertigkeitsDifferenz < 0) {
					// Ich habe eine schlechtere Stellung, Remis waere also eher
					// gut
					return 1000;
				} else {
					// Gleich gute Stellung, Remis waere also "egal"
					return 0;
				}
			case NORMAL:
				break;
			}

			if (tiefe == 0) {
				/*
				 * Es wird nur der aktuelle Zug betrachtet, daher werden alle
				 * Zuege in der Wertigkeit verglichen und die besten
				 * zurueckgegeben.
				 */
				for (Zug zug : moeglicheZuege) {
					if (zug.wertigkeit() > result) {
						result = zug.wertigkeit();
					}
				}
				return result;
			}
			// Es muss mindestens noch der Folgezug betrachtet werden.

			for (int i = 0; i < moeglicheZuege.size(); i++) {
				// Zug ausfuehren
				Zug zug = moeglicheZuege.get(i).fuehreAus(false);
				int wert = zug.wertigkeit();

				/*
				 * Mit rekursivem Aufruf wird die Wertigkeit des besten Zuges
				 * des Gegners geholt.
				 */
				Integer gegnerZug = besterZug(tiefe - 1, -beta + wert, -alpha
						+ wert, !my);

				// Mache Zug rueckgaengig
				zug.rueckgaengig();

				/*
				 * Die Wertigkeit des gegnerischen Zuges wird mit der Wertigkeit
				 * des betrachteten Zuges verrechnet und gespeichert. Wenn
				 * gegnerZug null ist, wurde dort wegen einem beta-Cut
				 * aufgehoert und hier kann der naechste Zug betrachtet werden.
				 */
				if (gegnerZug != null) {
					wert -= gegnerZug;
				} else {
					continue;
				}

				/*
				 * Vergleiche den Wert mit beta. Ist er groesser, so war der
				 * vorhergehende Zug bereits keine gute Wahl, so dass hier
				 * jegliche weitere Betrachtung abgebrochen werden kann.
				 */
				if (wert > beta) {
					return null;
				}

				/*
				 * Gucke, ob es sich um den intern besten Zug handelt, und ob er
				 * vllt. sogar alpha ueberschreitet. Die Werte werden dann ggf.
				 * angepasst.
				 */
				if (wert > result) {
					result = wert;
					if (wert > alpha) {
						alpha = wert;
					}
				}
			}
			return result;
		}
	}
}