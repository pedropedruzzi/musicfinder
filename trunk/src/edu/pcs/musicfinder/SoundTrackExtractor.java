package edu.pcs.musicfinder;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Sequence;
import javax.sound.midi.Track;


public class SoundTrackExtractor {

	private static final int NOTE_ON = 9;
	private static final int NOTE_OFF = 8;

	private static final int SILENCE = -1;
	private static final int NO_KEY = -2;
	private static final boolean DO_NOT_IGNORE = true;
	
	private Sequence sequence;
	private int pKey = NO_KEY;
	private long start = 0;
	
	private int ignored = 0;
	private int added = 0;
	private int silents = 0;
	private int invalid = 0;
	
	private long curTick = 0;

	SoundTrack soundTrack = new SoundTrack();
	
	public Sequence getSequence() { return sequence; }
	
	public SoundTrackExtractor(Sequence sequence) {
		this.sequence = sequence;
	}

	public SoundTrack extract(int trackNumber) {
		
		Track track = this.sequence.getTracks()[trackNumber];

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
				case NOTE_ON:
					noteOn(tick, status & 0xf, msgData[s], msgData[s+1]);
					break;
				case NOTE_OFF:
					noteOff(tick, status & 0xf, msgData[s], msgData[s+1]);
					break;
				default: invalid++;
			}
		}
		
		System.out.println("ignored: " + this.ignored);
		System.out.println("invalid: " + this.invalid);
		System.out.println("silents: " + this.silents);
		System.out.println("added: " + this.added);
		
		return soundTrack;
	}
	
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
					ignored++;
				}
			}
		} else {
			pKey = key;
			start = tick;
			if (velocity == 0) {
				noteOff(tick, channel, key, velocity);
			}
		}
	}
	
	private void noteOff(long tick, int channel, int key, int velocity) {
		if (key == pKey) {
			postNote(key, start, tick);
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
