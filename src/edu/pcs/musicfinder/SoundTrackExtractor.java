package edu.pcs.musicfinder;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

import org.apache.log4j.Logger;

public class SoundTrackExtractor {
	
	private static final Logger logger = Logger.getLogger(SoundTrackExtractor.class);
	private static final boolean DO_NOT_IGNORE = true;

	private final Sequence sequence;
	private final EventReader reader;
	
	private final float tick;
	
	public Sequence getSequence() { return sequence; }
	
	public int getNumberOfTracks() {
		return sequence.getTracks().length;
	}
	
	private SoundTrackExtractor(Sequence sequence) {
		this.sequence = sequence;
		this.reader = new EventReader(sequence.getTracks());
		
		float div = sequence.getDivisionType();
		
		if (div == 0) {
			logger.warn("tempo-based resolution. assuming 100 beats per minute.");
			float bpm = 100;
			tick = 60/(bpm*sequence.getResolution());
		} else {
			tick = 1/(div*sequence.getResolution());
		}
		
		logger.debug("tick duration in milli seconds = " + tick*1000);
	}

	public List<RealNote> extractReal(int channel) {
		List<Note> st = extract(channel);
		List<RealNote> notes = new ArrayList<RealNote>(st.size());
		for (Note n : st) {
			double pitch;
			
			if (n.getKey() == Note.SILENCE)
				pitch = RealNote.SILENCE;
			else
				pitch = keyToPitch(n.getKey());
			
			notes.add(new RealNote(pitch, n.getDuration() * tick));
		}
		
		return notes;
	}

	public List<Note> extract(int channel) {
		SoundTrackBuilder builder = new SoundTrackBuilder();

		byte status = 0;
		reader.reset();
		for (MidiEvent event : reader) {
			MidiMessage message = event.getMessage();
			long tick = event.getTick();
			
			byte[] msgData = message.getMessage();

			int s = 0;
			if (msgData[0] < 0) {
				status = msgData[0];
				s = 1;
			} else {
				// running status!
				System.out.println("running status! " + message.getClass().getSimpleName());
				throw new RuntimeException();
			}
			
			int type = (status>>4) & 0xf;
			int ch = status & 0xf;
			
			switch (type) {
				case MidiUtils.NOTE_ON:
					if (channel == ch)
						builder.noteOn(tick, msgData[s], msgData[s+1]);
					break;
				case MidiUtils.NOTE_OFF:
					if (channel == ch)
						builder.noteOff(tick, msgData[s], msgData[s+1]);
					break;
				default: builder.invalid++;
				logger.debug("Unkown message type! status=" + Integer.toHexString(status) + " type=" + type);
			}
		}
		
		logger.debug("ignored: " + builder.ignored);
		logger.debug("invalid: " + builder.invalid);
		logger.debug("silents: " + builder.silents);
		logger.debug("added: " + builder.added);
		
		return builder.track;
	}
	
	public void testeTempo() {
		for (Track track : sequence.getTracks()) {
			for (int i=0; i<track.size(); i++) {
				MidiEvent event = track.get(i);
				MidiMessage message = event.getMessage();
				if (message instanceof MetaMessage) {
					MetaMessage mm = (MetaMessage) message;
					if (mm.getType() == MidiUtils.TEMPO) {
						byte[] data = mm.getData();
						int tempo = (data[0] << 16) | (data[1] << 8) | data[2];
						System.out.println("tempo = " + tempo);
					} else {
						//System.out.println("META type = " + mm.getType());
					}
				}
			}
		}
	}
	
	public void testeTempo2() {
		reader.reset();
		for (MidiEvent event : reader) {
			MidiMessage message = event.getMessage();
			if (message instanceof MetaMessage) {
				MetaMessage mm = (MetaMessage) message;
				if (mm.getType() == MidiUtils.TEMPO) {
					byte[] data = mm.getData();
					int tempo = (data[0] << 16) | (data[1] << 8) | data[2];
					System.out.println("tempo = " + tempo);
				} else {
					//System.out.println("META type = " + mm.getType());
				}
			}
		}
	}

	public double ratioPoliphonic(int channel) {
		// how many notes sounding
		int notes = 0;
		long begin1 = 0;
		long sum1 = 0;
		long beginN = 0;
		long sumN = 0;
		
		reader.reset();
		for (MidiEvent event : reader) {
			MidiMessage message = event.getMessage();
			long tick = event.getTick();
			
			byte[] msgData = message.getMessage();

			if (msgData[0] >= 0) {
				throw new RuntimeException("running status!");
			}
			
			if (message instanceof MetaMessage) {
				MetaMessage mm = (MetaMessage) message;
				if (mm.getType() == MidiUtils.TEMPO) {
					byte[] data = mm.getData();
					int tempo = (data[0] << 16) | (data[1] << 8) | data[2];
					System.out.println("tempo = " + tempo);
				}
			} else if (message instanceof ShortMessage) {
				ShortMessage sm = (ShortMessage) message;
				if (channel == sm.getChannel()) {
					if (sm.getCommand() == ShortMessage.NOTE_ON && sm.getData2() != 0) {
						if (notes == 0) begin1 = tick;
						else if (notes == 1) beginN = tick;
						notes++;
					} else if ((sm.getCommand() == ShortMessage.NOTE_ON && sm.getData2() == 0) || sm.getCommand() == ShortMessage.NOTE_OFF) {
						notes--;
						if (notes == 0) sum1 += tick - begin1;
						else if (notes == 1) sumN += tick - beginN;
					}
				}
				
			}
		}
		
		logger.debug("sum1 = " + sum1);
		logger.debug("sumN = " + sumN);
		
		if (sum1 == 0) return Double.POSITIVE_INFINITY;
		else return sumN / (double)sum1;
	}
	
	private static class SoundTrackBuilder {
		private int pKey = Note.NO_KEY;
		private long start = 0;
		
		private int ignored = 0;
		private int added = 0;
		private int silents = 0;
		private int invalid = 0;

		private long curTick = 0;

		private List<Note> track = new LinkedList<Note>();

		public void noteOn(long tick, int key, int velocity) {
			if (velocity == 0) {
				noteOff(tick, key, velocity);
			} else {
				if (pKey != Note.NO_KEY) {
					//if (key == pKey) return;
					if (DO_NOT_IGNORE) {
						noteOff(tick, pKey, velocity); // channel e velocity estao invalidos!
						noteOn(tick, key, velocity);
					} else {
						logger.debug("Ignoring Note On event. tick=" + tick + ". key=" + key);
						ignored++;
					}
				} else {
					pKey = key;
					start = tick;
					logger.debug(">>>>>>>>>>Begin note event!! key=" + pKey + ". velocity=" + velocity);
				}
			}
		}

		public void noteOff(long tick, int key, int velocity) {
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

				track.add(silence);
				silents++;
			}

			Note note = new Note();
			note.setTick((int) curTick);
			note.setKey(key);
			note.setDuration((int)(end - start));

			track.add(note);

			curTick = end;
		}
	}
	
	public static double keyToPitch(double key) {
		return Math.pow(2, (key - 69) / 12d) * 440;
	}
	
	public static int pitchToKey(double pitch) {
		return (int) Math.round(pitchToKeyDouble(pitch));
	}
	
	public static double pitchToKeyDouble(double pitch) {
		return 69 + 12 * Math.log(pitch / 440) / Math.log(2);
	}
	
	public static double pitchToKeyResidual(double pitch) {
		double key = pitchToKeyDouble(pitch);
		return key - Math.round(key);
	}
	
	public static SoundTrackExtractor open(String midifile) {
		Sequence seq = null;

		try {
			seq = MidiSystem.getSequence(new File(midifile));
		} catch (Exception e) {
			logger.error("error getting midi sequence", e);
			return null;
		}

		logger.debug("MIDI File Details");
		logger.debug("division=" + seq.getDivisionType());
		logger.debug("res=" + seq.getResolution());
		logger.debug("len us=" + seq.getMicrosecondLength());
		logger.debug("len tick=" + seq.getTickLength());

		return new SoundTrackExtractor(seq);
	}
	
}
