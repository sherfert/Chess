package brett;

import java.applet.Applet;
import java.applet.AudioClip;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.net.URL;
import java.util.Hashtable;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpinnerNumberModel;

import spieler.MyPCSpieler;
import spieler.Spieler;
import figuren.Figur.Farbe;

/**
 * Das Hauptfenster
 */
@SuppressWarnings("serial")
public class MainWindow extends JFrame {
	/**
	 * Ein Panel zum Einfuegen eines Abstands
	 */
	public class AbstandPanel extends JPanel {

		/**
		 * @param horizontal
		 *            horizontaler Abstand in Pixeln
		 * @param vertikal
		 *            vertikaler Abstand in Pixeln
		 */
		public AbstandPanel(int horizontal, int vertikal) {
			setPreferredSize(new Dimension(horizontal, vertikal));
		}
	}

	/**
	 * Ein Schwierigkeitsregler fuer PC-Spieler
	 */
	public class Schwierigkeitsregler extends JPanel {
		private JSlider slider;
		private JCheckBox agressiveBox;

		public Schwierigkeitsregler() {
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

			slider = new JSlider(JSlider.HORIZONTAL, 0, 6, 3);
			agressiveBox = new JCheckBox("agressiv");
			add(slider);
			add(agressiveBox);

			// Label Tabelle
			Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
			labelTable.put(0, new JLabel("Leicht"));
			labelTable.put(3, new JLabel("Normal"));
			labelTable.put(6, new JLabel("Schwer"));

			slider.setSnapToTicks(true);
			slider.setMajorTickSpacing(1);
			slider.setPaintTicks(true);
			slider.setPaintLabels(true);
			slider.setLabelTable(labelTable);
		}

		/**
		 * @param enabled
		 *            ob der Regler an sein soll
		 */
		@Override
		public void setEnabled(boolean enabled) {
			super.setEnabled(enabled);
			slider.setEnabled(enabled);
			agressiveBox.setEnabled(enabled);
		}

		/**
		 * @return einen neuen MyPCSpieler mit den Einstellungen des Reglers
		 */
		public MyPCSpieler getNewMyPCSpieler() {
			return new MyPCSpieler(brett, slider.getValue(), agressiveBox.isSelected());
		}
	}

	/**
	 * System-Property-Strings
	 */
	public static String dir = System.getProperty("java.class.path");
	public static String sep = System.getProperty("file.separator");

	public static String audioEnding = "wav";

	/**
	 * Ein evtl. gerade gespielter Sound
	 */
	private static AudioClip sound;

	/**
	 * Speichert, ob der Sound an ist.
	 */
	private static boolean soundOn = true;

	/**
	 * Die an der Seite dargestellte JTextArea
	 */
	private static JTextArea textArea;

	/**
	 * Das JPanel, in dem die Zeitanzeigen oder Fortschrittsbalken dargestellt
	 * werden
	 */
	private JPanel zeitPanel;

	/**
	 * Zwei Zeitanzeigen an der Seite bei einem Zeitspiel
	 */
	private JLabel[] zeitAnzeigen = new JLabel[2];

	/**
	 * Eine Fortschrittsanzeige fuer den Denkfortschritt bei PC-Spielern
	 */
	private static JProgressBar denkFortschritt;

	/**
	 * Das angezeigt BrettPanel
	 */
	private BrettPanel brettPanel;
	
	/**
	 * Das dazugehoerige Brett
	 */
	private Brett brett;

	/**
	 * Das Hauptfenster
	 */
	private MainWindow() {
		super("Schach");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setResizable(false);
		Container contentPane = getContentPane();

		menuezeileErzeugen(this);

		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.X_AXIS));

		// Brett erzeugen
		contentPane.add(brettPanel = new BrettPanel());
		brett = brettPanel.getBrett();

		JPanel sidePanel = new JPanel();
		contentPane.add(sidePanel);
		sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.Y_AXIS));

		// TextArea erzeugen
		textArea = new JTextArea(1, 12);
		textArea.setEditable(false);
		JScrollPane scrollPane = new JScrollPane(textArea);
		scrollPane
				.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane
				.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

		sidePanel.add(scrollPane);

		zeitPanel = new JPanel();
		zeitPanel.setLayout(new BoxLayout(zeitPanel, BoxLayout.Y_AXIS));
		sidePanel.add(zeitPanel);

		// Aufbau abgeschlossen - Komponenten arrangieren lassen
		pack();

		// Das Fenster in der Mitte des Bildschirms platzieren und anzeigen
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation(d.width / 2 - getWidth() / 2, d.height / 2
				- getHeight() / 2);
		setVisible(true);

		// Brett anzeigen
		brettPanel.initializeGui();
	}

	/**
	 * Die Menüzeile des Hauptfensters erzeugen.
	 * 
	 * @param fenster
	 *            Das Fenster, in das die Menüzeile eingefügt werden soll.
	 */
	private void menuezeileErzeugen(JFrame fenster) {
		JMenuBar menuezeile = new JMenuBar();
		fenster.setJMenuBar(menuezeile);

		JMenu menue;
		JMenuItem eintrag;

		// Das Datei-Menü erzeugen
		menue = new JMenu("Spiel");

		eintrag = new JMenuItem("Neues Spiel");
		eintrag.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				neuesSpiel();
			}
		});
		menue.add(eintrag);

		eintrag = new JMenuItem("Beenden");
		eintrag.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				beenden();
			}
		});
		menue.add(eintrag);
		menuezeile.add(menue);

		menue = new JMenu("Optionen");
		final JCheckBoxMenuItem eintragCB = new JCheckBoxMenuItem("Sound");
		eintragCB.setState(true);
		eintragCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				soundOn = eintragCB.getState();
			}
		});
		menue.add(eintragCB);
		menuezeile.add(menue);

	}

	/**
	 * Startet ein neues Spiel
	 */
	private void neuesSpiel() {
		/*
		 * Es wird ein Spielauswahlfenster aufgebaut und angezeigt.
		 */

		// Hauptpanel des Dialogs
		JPanel newGamePanel = new JPanel();
		newGamePanel.setLayout(new BoxLayout(newGamePanel, BoxLayout.X_AXIS));

		// Panel zum Auswaehlen des Spieltyps
		JPanel gameTypePanel = new JPanel();
		gameTypePanel.setLayout(new BoxLayout(gameTypePanel, BoxLayout.Y_AXIS));
		newGamePanel.add(gameTypePanel);
		newGamePanel.add(new AbstandPanel(20, 0));

		final JRadioButton mmButton = new JRadioButton("Mensch vs. Mensch");
		mmButton.setActionCommand("mm");

		final JRadioButton pc1Button = new JRadioButton("Mensch vs. PC-Spieler");
		pc1Button.setActionCommand("pc1");
		pc1Button.setSelected(true);

		final JRadioButton pc2Button = new JRadioButton(
				"PC-Spieler vs. PC-Spieler");
		pc2Button.setActionCommand("pc2");

		ButtonGroup playTypeGroup = new ButtonGroup();
		playTypeGroup.add(mmButton);
		playTypeGroup.add(pc1Button);
		playTypeGroup.add(pc2Button);

		gameTypePanel.add(mmButton);
		gameTypePanel.add(pc1Button);
		gameTypePanel.add(pc2Button);

		// Optionenpanel
		JPanel optionPanel = new JPanel();
		optionPanel.setLayout(new BoxLayout(optionPanel, BoxLayout.Y_AXIS));
		newGamePanel.add(optionPanel);

		final JCheckBox zeitSpielCheckBox = new JCheckBox("Zeitspiel");

		optionPanel.add(zeitSpielCheckBox);

		// Zeitauswahlpanel
		JPanel zeitAuswahlPanel = new JPanel();
		zeitAuswahlPanel.setLayout(new BoxLayout(zeitAuswahlPanel,
				BoxLayout.X_AXIS));
		optionPanel.add(zeitAuswahlPanel);
		optionPanel.add(new AbstandPanel(0, 15));

		JLabel stundenLabel = new JLabel("Stunden ");
		JLabel minutenLabel = new JLabel("Minuten ");

		final JSpinner stundenAuswahl = new JSpinner(new SpinnerNumberModel(0,
				0, 24, 1));
		final JSpinner minutenAuswahl = new JSpinner(new SpinnerNumberModel(15,
				0, 60, 1));

		zeitSpielCheckBox.setEnabled(false);
		stundenAuswahl.setEnabled(false);
		minutenAuswahl.setEnabled(false);

		zeitAuswahlPanel.add(stundenLabel);
		zeitAuswahlPanel.add(stundenAuswahl);
		zeitAuswahlPanel.add(new AbstandPanel(10, 0));
		zeitAuswahlPanel.add(minutenLabel);
		zeitAuswahlPanel.add(minutenAuswahl);

		// Die JSpinner mit Klicken der ZeitSpielCheckBox (de)aktivieren.
		zeitSpielCheckBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				boolean enabled = (e.getStateChange() == ItemEvent.SELECTED);
				stundenAuswahl.setEnabled(enabled);
				minutenAuswahl.setEnabled(enabled);
			}
		});

		// Farbenauswahlpanel
		JPanel farbenAuswahlPanel = new JPanel();
		farbenAuswahlPanel.setLayout(new BoxLayout(farbenAuswahlPanel,
				BoxLayout.X_AXIS));
		optionPanel.add(farbenAuswahlPanel);

		final JRadioButton weissButton = new JRadioButton("Weiss");
		weissButton.setActionCommand("weiss");
		weissButton.setSelected(true);

		final JRadioButton schwarzButton = new JRadioButton("Schwarz");
		schwarzButton.setActionCommand("schwarz");

		ButtonGroup farbeGroup = new ButtonGroup();
		farbeGroup.add(weissButton);
		farbeGroup.add(schwarzButton);

		farbenAuswahlPanel.add(weissButton);
		farbenAuswahlPanel.add(schwarzButton);

		// 2 Schwierigkeitregler fuer bis zu 2 PC-Spieler
		final Schwierigkeitsregler pc1Schwierigkeit = new Schwierigkeitsregler();
		final Schwierigkeitsregler pc2Schwierigkeit = new Schwierigkeitsregler();
		pc2Schwierigkeit.setEnabled(false);

		optionPanel.add(pc1Schwierigkeit);
		optionPanel.add(pc2Schwierigkeit);

		// Je nach Spieltyp Komponenten aktivieren oder deaktivieren.
		ActionListener playTypeActionListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Object source = e.getSource();

				weissButton.setEnabled(source == pc1Button);
				schwarzButton.setEnabled(source == pc1Button);

				zeitSpielCheckBox.setEnabled(source == mmButton);
				stundenAuswahl.setEnabled(source == mmButton
						&& zeitSpielCheckBox.isSelected());
				minutenAuswahl.setEnabled(source == mmButton
						&& zeitSpielCheckBox.isSelected());

				pc1Schwierigkeit.setEnabled(source != mmButton);
				pc2Schwierigkeit.setEnabled(source == pc2Button);
			}
		};

		mmButton.addActionListener(playTypeActionListener);
		pc1Button.addActionListener(playTypeActionListener);
		pc2Button.addActionListener(playTypeActionListener);

		// Fragedialog anzeigen
		JOptionPane pane = new JOptionPane(newGamePanel,
				JOptionPane.PLAIN_MESSAGE);

		pane.createDialog("Neues Spiel").setVisible(true);

		// Wird das Fenster geschlossen, wird abgebrochen
		if (pane.getValue() == null) {
			return;
		}

		// Spieltyp ermitteln
		String playType = playTypeGroup.getSelection().getActionCommand();

		// Ggf. Noch alte Schachuhr/JProgressBar entfernen
		zeitPanel.removeAll();

		if (playType.equals("mm")) {
			// Mensch gegen Mensch
			boolean zeitSpiel = zeitSpielCheckBox.isSelected();
			if (zeitSpiel) {
				int stunden = (Integer) stundenAuswahl.getModel().getValue();
				int minuten = (Integer) minutenAuswahl.getModel().getValue();
				if (stunden == 0 && minuten == 0) {
					// Es muss mindestens eine Minute Bedenkzeit pro Spieler
					// geben
					minuten = 1;
				}

				// Zeitanzeigen initialisieren
				zeitAnzeigen[0] = new JLabel();
				zeitAnzeigen[1] = new JLabel();
				Font zeitFont = new Font("Arial", Font.BOLD, 30);
				zeitAnzeigen[0].setFont(zeitFont);
				zeitAnzeigen[1].setFont(zeitFont);
				zeitAnzeigen[0].setForeground(Color.RED);
				zeitAnzeigen[1].setForeground(Color.RED);

				zeitPanel.add(new JLabel("Weiss"));
				zeitPanel.add(zeitAnzeigen[0]);
				zeitPanel.add(new JLabel("Schwarz"));
				zeitPanel.add(zeitAnzeigen[1]);

				/*
				 * Das Brett mit zwei Spielern und SchachUhrThreads resetten.
				 * Die eine Uhr wird angewiesen, wenn der Thread gestartet wird
				 * zu laufen, die andere nicht. Beide bekommen ihr Label zur
				 * Aktualisierung der Zeit.
				 */
				brett.reset(
						new Spieler(brett),
						new Spieler(brett),
						new SchachUhrThread(brett, stunden * 60
								+ minuten, Farbe.WEISS, zeitAnzeigen[0], true),
						new SchachUhrThread(brett, stunden * 60
								+ minuten, Farbe.SCHWARZ, zeitAnzeigen[1],
								false));
			} else {
				brett.reset(new Spieler(brett), new Spieler(brett));
			}
		} else if (playType.equals("pc1")) {
			// Mensch gegen PC-Spieler
			denkFortschritt = new JProgressBar(0, 100);
			zeitPanel.add(denkFortschritt);

			// Farbe des Spielers abfragen und entsprechend das Brett resetten
			String spielerFarbe = farbeGroup.getSelection().getActionCommand();
			if (spielerFarbe.equals("weiss")) {
				brett.reset(new Spieler(brett),
						pc1Schwierigkeit.getNewMyPCSpieler());
			} else {
				brett.reset(
						pc1Schwierigkeit.getNewMyPCSpieler(), new Spieler(brett));
			}
		} else {
			// PC-Spieler gegen PC-Spieler
			denkFortschritt = new JProgressBar(0, 100);
			zeitPanel.add(denkFortschritt);

			brett.reset(pc1Schwierigkeit.getNewMyPCSpieler(),
					pc2Schwierigkeit.getNewMyPCSpieler());
		}

		// Fenster neu arrangieren
		pack();
	}

	/**
	 * 'Beenden'-Funktion: Beendet die Anwendung.
	 */
	private void beenden() {
		System.exit(0);
	}

	/**
	 * Spielt einen Sound ab.
	 * 
	 * @param name
	 *            der Name der abzuspielenden Sounddatei
	 */
	public static void playSound(String name) {
		if (!soundOn) {
			return;
		}

		// Ein evtl. noch spielender Sound wird gestoppt.
		if (sound != null) {
			sound.stop();
		}
		URL u = MainWindow.class.getResource("/sound/" + name + "." + audioEnding);
		sound = Applet.newAudioClip(u);
		if (sound != null) {
			sound.play();
		}
	}

	/**
	 * Schreibt den Text in die JTextArea
	 * 
	 * @param text
	 *            den zu schreibenden Text
	 */
	public static void print(String text) {
		textArea.append(text);
		textArea.setCaretPosition(textArea.getText().length());
	}

	/**
	 * Loescht den Text in der JTextArea
	 */
	public static void clearText() {
		textArea.setText("");
	}

	/**
	 * Setzt den Fortschrittsbalken (in einem neuen Thread).
	 * 
	 * @param percentage
	 *            die Prozentangabe
	 */
	public static void setProgress(final int percentage) {
		// TODO nicht immer neuer Thread, sofort machen iwie
		new Thread() {
			@Override
			public void run() {
				if(denkFortschritt.getValue() < percentage || percentage == 0) {
					denkFortschritt.setValue(percentage);
				}
			}
		}.start();

	}

	/**
	 * Programmeinstiegspunkt.
	 */
	public static void main(String[] args) {
		new MainWindow();
	}
}