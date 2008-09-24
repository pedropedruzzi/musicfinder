package edu.pcs.musicfinder;

// Midi.java
// Based on BasicGUI.java, this shows how to load midi files.
// David Kosbie, 15-111/AB, Spring 2007

/*
 Standard disclaimer:  As usual with sample code designed (often in
 class!) to demonstrate specific issues, the style may not be perfect
 (it is especially lacking comments and some top-down design),
 and there may even be a bug or two lurking in the code!

 This sample demonstrates the use of a very simple MidiPlayer class.
 This class only has a few static methods, allowing you to play or
 stop a midi file (and you can play it once or looping).  For more
 advanced use, you can get the Sequencer object.  See the Java API
 for details.

 //////////////////////////////////////////////
 // MidiPlayer API (all statics)
 //////////////////////////////////////////////

 class MidiPlayer {
 public static void play(String filename);
 public static void play(String filename, boolean loop);
 public static Sequencer getSequencer();  // for advanced use!
 public static void stop();
 }

 */

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.File;

import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequencer;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class Midi extends IntroCsApplication {

	private static final long serialVersionUID = 1L;

	public static void main(String[] args) {
		new Midi();
	}

	JLabel nowPlaying;

	public void init() {
		add(nowPlaying = newCenteredLabel("No midi file playing yet!"));
		addButtons("Play Next", "Play Next (Looping)", "Stop");
	}

	public void playNextMidiFile(boolean loop) {
		String midiFilename = getNextMidiFilename();
		if (midiFilename == null)
			nowPlaying.setText("No midi files found!!!");
		else {
			nowPlaying
					.setText("Now playing: " + midiFilename.toString() + "  ");
			MidiPlayer.play(midiFilename, loop);
		}
	}

	public void stopPlayingMidi() {
		MidiPlayer.stop();
		nowPlaying.setText("Midi player stopped!");
	}

	public void onButtonPressed(String label) {
		if (label.equals("Play Next"))
			playNextMidiFile(false);
		else if (label.equals("Play Next (Looping)"))
			playNextMidiFile(true);
		else if (label.equals("Stop"))
			stopPlayingMidi();
	}

	// find next ".mid" file in this directory
	// or return null if none are found
	public String getNextMidiFilename() {
		java.io.File directory = new java.io.File("resource");
		String[] files = directory.list();
		for (int i = 0; i < files.length; i++) {
			String filename = "resource/" + files[(i + nextMidiIndex) % files.length];
			if (filename.endsWith(".mid")) {
				nextMidiIndex += (i + 1);
				return filename;
			}
		}
		return null;
	}

	private int nextMidiIndex = 0;

	public static JLabel newCenteredLabel(String text) {
		JLabel label = new JLabel(text);
		label.setAlignmentX(Component.CENTER_ALIGNMENT);
		return label;
	}
}

// //////////////////////////////////////////////////////////
// //////////////////////////////////////////////////////////
// You are not (yet) responsible for the code below here!
//
// MidiPlayer
//
// Include this class if you want to play midi files,
// And include these imports:
//
// import java.io.*;
// import javax.sound.midi.*;
//
// //////////////////////////////////////////////////////////
// //////////////////////////////////////////////////////////

class MidiPlayer {
	public static void play(String filename) {
		play(filename, false);
	}

	public static void play(String filename, boolean loop) {
		try {
			stop();
			sequencer = MidiSystem.getSequencer();
			File midiFile = new File(filename);
			sequencer.setSequence(MidiSystem.getSequence(midiFile));
			if (loop)
				sequencer.setLoopCount(Sequencer.LOOP_CONTINUOUSLY);
			sequencer.open();
			sequencer.start();
		} catch (Exception e) {
			System.err.println("MidiPlayer: " + e);
			sequencer = null;
		}
	}

	public static Sequencer getSequencer() {
		return sequencer;
	}

	public static void stop() {
		try {
			if ((sequencer == null) || (!sequencer.isRunning()))
				return;
			sequencer.stop();
			sequencer.close();
		} catch (Exception e) {
			System.err.println("MidiPlayer: " + e);
		}
		sequencer = null;
	}

	private static Sequencer sequencer = null;
}

// //////////////////////////////////////////////////////////
// //////////////////////////////////////////////////////////
// You are not (yet) responsible for the code below here!
// //////////////////////////////////////////////////////////
// //////////////////////////////////////////////////////////

class IntroCsComponent extends JComponent {

	public IntroCsComponent() {
		this.addEventListeners();
		this.setPreferredSize(new Dimension(400, 400));
	}

	// override these methods to respond to events
	public void onTimer() {
	}

	public void onMousePressed(int x, int y) {
	}

	public void onMouseDragged(int x, int y) {
	}

	public void onMouseReleased(int x, int y) {
	}

	public void onKeyPressed(KeyEvent e, int keyCode, char keyChar) {
	}

	public void onResized() {
	}

	public void beep() {
		Toolkit.getDefaultToolkit().beep();
	}

	public void startTimer(final int timerDelay) {
		// use invokeLater to allow window to become visible before starting
		// to be initialized in subclasses before calling init() method
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new javax.swing.Timer(timerDelay, new ActionListener() {
					public void actionPerformed(ActionEvent evt) {
						onTimer();
						repaint();
					}
				}).start();
			}
		});
	}

	protected void addEventListeners() {
		// add actions in response to mouse events
		addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				onMousePressed(e.getX(), e.getY());
				repaint();
			}

			public void mouseReleased(MouseEvent e) {
				onMouseReleased(e.getX(), e.getY());
				repaint();
			}
		});

		// and for mouse motion events
		addMouseMotionListener(new MouseMotionAdapter() {
			public void mouseDragged(MouseEvent e) {
				onMouseDragged(e.getX(), e.getY());
				repaint();
			}
		});

		// and for keyboard events
		addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				onKeyPressed(e, e.getKeyCode(), e.getKeyChar());
				repaint();
			}
		});

		// and for component events
		addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				onResized();
				repaint();
			}
		});
	}
}

class IntroCsApplication extends JFrame {

	public void init() {
		// override this method to add your buttons, objects, etc
	}

	public void onTimer() {
	}

	// This method lives outside the anonymous inner class Runnable
	// so that getClass() returns this class (in fact, the overridden subclass)
	protected String makeTitle() {
		return this.getClass().getName();
	}

	public IntroCsApplication() {
		super();
		Container cp = this.getContentPane();
		cp.setBackground(Color.white);
		cp.setLayout(new BoxLayout(cp, BoxLayout.Y_AXIS));
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		// use invokeLater to allow instance variables (like buttonLabels[])
		// to be initialized in subclasses before calling init() method
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				init(); // override this method!
				pack();
				if (focusComponent != null)
					// send key events to this component!
					focusComponent.requestFocusInWindow();
				setTitle(makeTitle());
				setVisible(true);
			}
		});
	}

	private IntroCsComponent focusComponent = null;

	public Component add(Component c) {
		Component result = super.add(c);
		if (c instanceof IntroCsComponent) {
			// the first one added gets the keyboard by default
			if (focusComponent == null) {
				focusComponent = (IntroCsComponent) c;
				// send key events to this component!
				focusComponent.requestFocusInWindow();
			}
		}
		return result;
	}

	public void onButtonPressed(String label) {
		System.out.printf("onButtonPressed(%s)\n", label);
	}

	public void addButtons(String... buttonLabels) {
		JPanel buttons = new JPanel();
		buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
		for (int i = 0; i < buttonLabels.length; i++) {
			JButton button = new JButton(buttonLabels[i]);
			button.setFocusable(false);
			button.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					onButtonPressed(((JButton) e.getSource()).getText());
					repaint();
				}
			});
			buttons.add(button);
		}
		this.getContentPane().add(buttons);
	}

	public void beep() {
		Toolkit.getDefaultToolkit().beep();
	}

	public void startTimer(final int timerDelay) {
		// use invokeLater to allow window to become visible before starting
		// to be initialized in subclasses before calling init() method
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new javax.swing.Timer(timerDelay, new ActionListener() {
					public void actionPerformed(ActionEvent evt) {
						onTimer();
					}
				}).start();
			}
		});
	}
}