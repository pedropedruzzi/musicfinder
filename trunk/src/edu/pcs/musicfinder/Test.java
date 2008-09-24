package edu.pcs.musicfinder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;

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
	
	private long curTick = 0;
	
	private Logger logger = Logger.getLogger(Test.class);
	private SoundTrack track = new SoundTrack();

	private void openTestFile() {
		Sequence seq = null;

		try {
			seq = MidiSystem.getSequence(new File("resource/ode_to_joy.mid"));
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		logger.debug("MIDI File Details");
		logger.debug("division=" + seq.getDivisionType());
		logger.debug("res=" + seq.getResolution());
		logger.debug("len us=" + seq.getMicrosecondLength());
		logger.debug("len tick=" + seq.getTickLength());
		
		Track[] tracks = seq.getTracks();

		logger.debug("Tracks:");
		
		int n=0;
		for (Track track : tracks) {
			logger.debug("Track " + n++);
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
				
//				logger.debug(Integer.toString(status,2));
				
				switch (type) {
				case NOTE_ON:
					noteOn(tick, status & 0xf, msg[s], msg[s+1]);
					break;
				case NOTE_OFF:
					noteOff(tick, status & 0xf, msg[s], msg[s+1]);
					break;
				default:
//					logger.debug("Unkown message type! status=" + status + " type=" + type);
				}
			}
		}
		
		logger.info("Notes added: " + added);
		logger.info("Notes ignored: " + ignored);
		logger.info("Notes silents: " + silents);
		logger.info("notes.size(): " + track.getNotes().size());
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
//					logger.debug("Ignoring Note On event. tick=" + tick + ". key=" + key);
					ignored++;
				}
			}
		} else {
			pKey = key;
			start = tick;
//			logger.debug(">>>>>>>>>>Begin note event!! key=" + pKey + ". velocity=" + velocity);
			if (velocity == 0) {
				noteOff(tick, channel, key, velocity);
			}
		}
	}
	
	private void noteOff(long tick, int channel, int key, int velocity) {
		if (key == pKey) {
			postNote(key, start, tick);
//			logger.debug("<<<<<<<<<<End note event!! duration=" + (tick - start));
			added++;
			pKey = NO_KEY;
		}
	}

	
	private void postNote(int key, long start, long end) {
		if (start > end) throw new RuntimeException();
		if (curTick > start) throw new RuntimeException();
		
		
		if (curTick < start) {
			Note silence = new Note();
			silence.setKey(SILENCE);
			silence.setDuration((int) (start - curTick));
			
			track.addNote(silence);
			silents++;
		}
		
		Note note = new Note();
		note.setTick((int) curTick);
		note.setKey(key);
		note.setDuration((int)(end - start));
		
		track.addNote(note);
		
		curTick = end;
	}
	
	public static void main(String[] args) {
		Test t = new Test();
		
		t.openTestFile();
		t.recordNotes();
	}

	private void recordNotes() {
		long tick = 0;
		
		Sequence seq;
		try {
			seq = new Sequence(0, 120);
		} catch (InvalidMidiDataException e1) {
			e1.printStackTrace();
			return;
		}
		
		Track t = seq.createTrack();
		
		for (Note n : track.getNotes().values()) {
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
		
		try {
			track.printTextChart(new PrintStream("C:\\Users\\Redder\\Desktop\\ode_to_joy.txt"));
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		
		try {
			MidiSystem.write(seq, 1, new File("C:\\Users\\Redder\\Desktop\\out.mid"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
