package edu.pcs.musicfinder;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

import org.apache.log4j.Logger;

public class Test {
	
	private static final int NOTE_ON = 9;
	private static final int NOTE_OFF = 8;

	private static final int SILENCE = -1;
	private static final int NO_KEY = -2;
	private static final boolean DO_NOT_IGNORE = true;
	
	private Logger logger = Logger.getLogger(Test.class);

	private int openTestFile(File input, int trackNumber) {
		Sequence seq = null;
		int resolution;

		try {
			seq = MidiSystem.getSequence(input);
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}

		logger.debug("MIDI File Details");
		logger.debug("division=" + seq.getDivisionType());
		resolution = seq.getResolution();
		logger.debug("res=" + resolution);
		logger.debug("len us=" + seq.getMicrosecondLength());
		logger.debug("len tick=" + seq.getTickLength());
		
		Track[] tracks = seq.getTracks();

		logger.debug("tracks=" + tracks.length);
		
		Track track = tracks[trackNumber];

		logger.debug("Track " + trackNumber);
		logger.debug("# events=" + track.size());
		logger.debug("# ticks=" + track.ticks());

		pKey = NO_KEY;
		start = 0;


		byte status = 0;

		// Varre os eventos do track
		for (int e=0; e<track.size(); e++) {
			MidiEvent event = track.get(e);
			MidiMessage m = event.getMessage();
			long tick = event.getTick();

			byte[] msg = m.getMessage();

			int s = 0;
			if (msg[0] < 0) {
				status = msg[0];
				s = 1;
			}

			int type = (status>>4) & 0xf;

			logger.debug(Integer.toString(status,2));

			switch (type) {
			case NOTE_ON:
				noteOn(tick, status & 0xf, msg[s], msg[s+1]);
				break;
			case NOTE_OFF:
				noteOff(tick, status & 0xf, msg[s], msg[s+1]);
				break;
			default:
				logger.debug("Unkown message type! status=" + status + " type=" + type);
			}
		}

		logger.info("Notes added: " + added);
		logger.info("Notes ignored: " + ignored);
		logger.info("Notes silents: " + silents);
		logger.info("notes.size(): " + notes.size());

		return resolution;
	}

	private int pKey = NO_KEY;
	private long start = 0;

	private int ignored = 0;
	private int added = 0;
	private int silents = 0;
	
	private void noteOn(long tick, int channel, int key, int velocity) {
		if (pKey != NO_KEY) {
			if (key == pKey) {
				// second note on
				noteOff(tick, channel, key, velocity);
			} else {
				if (DO_NOT_IGNORE) {
					noteOff(tick, channel, pKey, velocity); // channel e velocity estão invalidos!
					noteOn(tick, channel, key, velocity);
				} else {
					logger.debug("Ignoring Note On event. tick=" + tick + ". key=" + key);
					ignored++;
				}
			}
		} else {
			pKey = key;
			start = tick;
			logger.debug(">>>>>>>>>>Begin note event!! key=" + pKey + ". velocity=" + velocity);
			if (velocity == 0) {
				noteOff(tick, channel, key, velocity);
			}
		}
	}
	
	private void noteOff(long tick, int channel, int key, int velocity) {
		if (key == pKey) {
			postNote(key, start, tick);
			logger.debug("<<<<<<<<<<End note event!! duration=" + (tick - start));
			added++;
			pKey = NO_KEY;
		}
	}

	private long curTick = 0;
	private List<Note> notes = new LinkedList<Note>();

	private void postNote(int key, long start, long end) {
		if (start > end) throw new RuntimeException();
		if (curTick > start) throw new RuntimeException();
		
		
		if (curTick < start) {
			Note silence = new Note();
			silence.setKey(SILENCE);
			silence.setDuration((int) (start - curTick));
			
			notes.add(silence);
			silents++;
		}
		
		Note note = new Note();
		note.setTick((int) curTick);
		note.setKey(key);
		note.setDuration((int)(end - start));
		
		notes.add(note);
		
		curTick = end;
	}
	
	public static void main(String[] args) {
		extractTrack("ode_to_joy", 1);
//		extractTrack("hey_jude", 1);
//		extractTrack("penny_lane", 4); piano
//		extractTrack("penny_lane", 6); baixo
//		extractTrack("penny_lane", 2);
//		extractTrack("yesterday", 3);
	}
	
	public static void extractTrack(String tune, int trackNumber) {
		Test test = new Test();

		int res = 120;
		try {
			res = test.openTestFile(new File("resource/" + tune + ".mid"), trackNumber);
		} catch (Exception e) {

		}
		test.recordNotes(res, new File("out/" + tune + "_track_" + Integer.toString(trackNumber) + ".mid"));
	}

	private void recordNotes(int res, File out) {
		long tick = 0;
		
		Sequence seq;
		try {
			seq = new Sequence(0, res);
		} catch (InvalidMidiDataException e1) {
			e1.printStackTrace();
			return;
		}
		
		Track t = seq.createTrack();
		
		for (Note n : notes) {
			if (n.getKey() != SILENCE) {
				try {
					ShortMessage msg = new ShortMessage();
					msg.setMessage(NOTE_ON<<4, n.getKey(), 30);
					t.add(new MidiEvent(msg, tick));
					
					tick += n.getDuration();
					
					msg = new ShortMessage();
					msg.setMessage(NOTE_OFF<<4, n.getKey(), 30);
					t.add(new MidiEvent(msg, tick));
				} catch (InvalidMidiDataException e) {
					e.printStackTrace();
					return;
				}
			} else {
				tick += n.getDuration();
			}
		}
		
//		try {
//			track.printTextChart(new PrintStream("out/x.txt"));
//		} catch (FileNotFoundException e1) {
//			e1.printStackTrace();
//		}
		
		try {
			MidiSystem.write(seq, 1, out);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
