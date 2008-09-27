package edu.pcs.musicfinder;

import java.util.LinkedList;
import java.util.List;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Sequence;
import javax.sound.midi.Track;

import org.apache.log4j.Logger;

public class SoundTrackExtractor {
	
	private static final boolean DO_NOT_IGNORE = true;
	
	private Logger logger = Logger.getLogger(Test.class);

	private Sequence sequence;
	private int pKey = Note.NO_KEY;
	private long start = 0;
	
	private int ignored = 0;
	private int added = 0;
	private int silents = 0;
	private int invalid = 0;
	
	private long curTick = 0;
	
	private float tick;

	private SoundTrack soundTrack;
	
	public Sequence getSequence() { return sequence; }
	
	public SoundTrackExtractor(Sequence sequence) {
		this.sequence = sequence;
		this.soundTrack = new SoundTrack(sequence.getResolution());
		
		float div = sequence.getDivisionType();
		
		if (div == 0) {
			logger.warn("tempo-based resolution. assuming 120 beats per minute.");
			float bpm = 120;
			tick = 60/(bpm*sequence.getResolution());
		} else {
			tick = 1/(div*sequence.getResolution());
		}
		
		logger.debug("tick duration in milli seconds = " + tick*1000);
	}

	public SoundTrack extract(int trackNumber) {
		
		Track track = this.sequence.getTracks()[trackNumber];

		logger.debug("Track " + trackNumber);
		logger.debug("# events=" + track.size());
		logger.debug("# ticks=" + track.ticks());

		byte status = 0;
		for (int i = 0; i < track.size(); i++) {
			
			MidiEvent event = track.get(i);
			MidiMessage message = event.getMessage();
			long tick = event.getTick();
			
			byte[] msgData = message.getMessage();

			int s = 0;
			if (msgData[0] < 0) {
				status = msgData[0];
				s = 1;
			}
			
			int type = (status>>4) & 0xf;
			
			switch (type) {
				case MidiUtils.NOTE_ON:
					noteOn(tick, status & 0xf, msgData[s], msgData[s+1]);
					break;
				case MidiUtils.NOTE_OFF:
					noteOff(tick, status & 0xf, msgData[s], msgData[s+1]);
					break;
				default: invalid++;
				logger.debug("Unkown message type! status=" + Integer.toHexString(status) + " type=" + type);
			}
		}
		
		System.out.println("ignored: " + this.ignored);
		System.out.println("invalid: " + this.invalid);
		System.out.println("silents: " + this.silents);
		System.out.println("added: " + this.added);
		
		return soundTrack;
	}

	public List<RealNote> extractReal(int trackNumber) {
		List<RealNote> notes = new LinkedList<RealNote>();
		
		SoundTrack st = extract(trackNumber);
		for (Note n : st.getNotes()) {
			double pitch;
			
			if (n.getKey() == Note.SILENCE)
				pitch = RealNote.SILENCE;
			else
				pitch = keyToPitch(n.getKey());
			
			notes.add(new RealNote(pitch, n.getDuration() * tick));
		}
		
		return notes;
	}
	
	public static double keyToPitch(int key) {
		return Math.pow(2, (key - 69) / 12d) * 440;
	}
	
	public static int pitchToKey(double pitch) {
		return (int) (69 + 12 * Math.log(pitch / 440) / Math.log(2));
	}

	private void noteOn(long tick, int channel, int key, int velocity) {
		if (pKey != Note.NO_KEY) {
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
			pKey = Note.NO_KEY;
		}
	}

	
	private void postNote(int key, long start, long end) {
		if (start > end) throw new RuntimeException();
		if (curTick > start) throw new RuntimeException();
		
		
		if (curTick < start) {
			Note silence = new Note();
			silence.setKey(Note.SILENCE);
			silence.setDuration((int) (start - curTick));
			
			soundTrack.addNote(silence);
			silents++;
		}
		
		Note note = new Note();
		note.setTick((int) curTick);
		note.setKey(key);
		note.setDuration((int)(end - start));
		
		soundTrack.addNote(note);
		
		curTick = end;
	}
}
