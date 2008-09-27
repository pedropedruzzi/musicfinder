package edu.pcs.musicfinder;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

import org.apache.log4j.Logger;

public class Test {
	
	private static final Logger logger = Logger.getLogger(Test.class);

	private List<RealNote> extractTrackFromFile(File input, int trackNumber) {
		Sequence seq = null;

		try {
			seq = MidiSystem.getSequence(input);
		} catch (Exception e) {
			logger.error("error getting midi sequence", e);
			return null;
		}

		logger.debug("MIDI File Details");
		logger.debug("division=" + seq.getDivisionType());
		logger.debug("res=" + seq.getResolution());
		logger.debug("len us=" + seq.getMicrosecondLength());
		logger.debug("len tick=" + seq.getTickLength());
		
		SoundTrackExtractor extractor = new SoundTrackExtractor(seq);

		return extractor.extractReal(trackNumber);
	}
	
	public static void main(String[] args) {
		Test t = new Test();
//		t.extractTrack("ode_to_joy", 1);
//		t.extractTrack("hey_jude", 1);
//		t.extractTrack("penny_lane", 4); piano
//		t.extractTrack("penny_lane", 6); baixo
//		t.extractTrack("penny_lane", 2);
		List<RealNote> st = t.extractTrack("yesterday", 3);
		st = modify(st, 100, 16);
		//st = removeSilence(st);
		t.recordNotes(st, "yesterday", 4);
	}
	
	private static List<RealNote> modify(List<RealNote> st, int start, int length) {
		final double durAbs = 1.1;
		final double durRel = 0.1;
		final double keyAbs = Math.pow(2, 1/6);
		final double keyRel = Math.pow(2, 1/12) - 1;
		List<RealNote> notes = st.subList(start, start + length);
		
		for (RealNote n : notes) {
			System.out.println(n.getPitch());
			n.setDuration(n.getDuration()*durAbs + (Math.random()-0.5)*durRel);
			
			if (n.getPitch() != RealNote.SILENCE)
				n.setPitch(n.getPitch()*keyAbs*((Math.random()-0.5)*keyRel + 1));
		}
		
		return notes;
	}
	
	private static List<RealNote> removeSilence(List<RealNote> st) {
		List<RealNote> notes = new LinkedList<RealNote>();
		
		RealNote nova = null;
		for (RealNote n : st) {
			if (n.getPitch() == RealNote.SILENCE) {
				if (nova != null) {
					nova.setDuration(nova.getDuration() + n.getDuration());
				}
			} else {
				if (nova != null) {
					notes.add(nova);
				}
				nova = new RealNote(n.getPitch(), n.getDuration());
			}
		}
		if (nova != null) {
			notes.add(nova);
		}
		
		return notes;
	}

	public List<RealNote> extractTrack(String tune, int trackNumber) {
		List<RealNote> st = null;
		try {
			st = extractTrackFromFile(new File("resource/" + tune + ".mid"), trackNumber);
		} catch (Exception e) {
			logger.error("extracting track", e);
		}
		return st;
	}

	public void recordNotes(List<RealNote> st, String tune, int trackNumber) {
		recordNotes(st, new File("out/" + tune + "_track_" + Integer.toString(trackNumber) + ".mid"));
	}

	private void recordNotes(List<RealNote> st, File out) {
		long tick = 0;
		final int res = 10;
		
		Sequence seq;
		try {
			seq = new Sequence(Sequence.SMPTE_30, res);
		} catch (InvalidMidiDataException e) {
			logger.error("creating midi sequence", e);
			return;
		}
		
		Track t = seq.createTrack();
		
		for (RealNote n : st) {
			if (n.getPitch() != RealNote.SILENCE) {
				try {
					ShortMessage msg = new ShortMessage();
					msg.setMessage(MidiUtils.NOTE_ON<<4, SoundTrackExtractor.pitchToKey(n.getPitch()), 30);
					t.add(new MidiEvent(msg, tick));
					
					tick += n.getDuration() * 300;
					
					msg = new ShortMessage();
					msg.setMessage(MidiUtils.NOTE_OFF<<4, SoundTrackExtractor.pitchToKey(n.getPitch()), 30);
					t.add(new MidiEvent(msg, tick));
				} catch (InvalidMidiDataException e) {
					e.printStackTrace();
					return;
				}
			} else {
				tick += n.getDuration() * 300;
			}
		}
		
		try {
			MidiSystem.write(seq, 1, out);
		} catch (IOException e) {
			logger.error("error writting midi sequence", e);
		}
	}
}
