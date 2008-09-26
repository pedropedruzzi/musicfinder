package edu.pcs.musicfinder;

import java.io.File;
import java.io.IOException;
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

	private SoundTrack extractTrackFromFile(File input, int trackNumber) {
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

		return extractor.extract(trackNumber);
	}
	
	public static void main(String[] args) {
		Test t = new Test();
//		t.extractTrack("ode_to_joy", 1);
//		t.extractTrack("hey_jude", 1);
//		t.extractTrack("penny_lane", 4); piano
//		t.extractTrack("penny_lane", 6); baixo
//		t.extractTrack("penny_lane", 2);
		SoundTrack st = t.extractTrack("yesterday", 3);
		st = modify(st, 100, 16);
		t.recordNotes(st, "yesterday", 4);
	}
	
	private static SoundTrack modify(SoundTrack st, int start, int length) {
		final double durAbs = 1.1;
		final int durRel = 6;
		final double keyAbs = 0.9;
		final int keyRel = 2;
		List<Note> notes = st.getNotes().subList(start, start + length);
		for (Note n : notes) {
			System.out.println(n.getKey());
			n.setDuration((int)(n.getDuration()*durAbs + (Math.random()-0.5)*durRel));
			
			// key eh muito discreto!
			if (n.getKey() != SoundTrack.SILENCE)
				n.setKey((int)(n.getKey()*keyAbs + (Math.random()-0.5)*keyRel));
		}
		
		return new SoundTrack(st.getResolution(), notes);
	}

	public SoundTrack extractTrack(String tune, int trackNumber) {
		SoundTrack st = null;
		try {
			st = extractTrackFromFile(new File("resource/" + tune + ".mid"), trackNumber);
		} catch (Exception e) {
			logger.error("extracting track", e);
		}
		return st;
	}

	public void recordNotes(SoundTrack st, String tune, int trackNumber) {
		recordNotes(st, new File("out/" + tune + "_track_" + Integer.toString(trackNumber) + ".mid"));
	}

	private void recordNotes(SoundTrack st, File out) {
		long tick = 0;
		
		Sequence seq;
		try {
			seq = new Sequence(0, st.getResolution());
		} catch (InvalidMidiDataException e) {
			logger.error("creating midi sequence", e);
			return;
		}
		
		Track t = seq.createTrack();
		
		for (Note n : st.getNotes()) {
			if (n.getKey() != SoundTrack.SILENCE) {
				try {
					ShortMessage msg = new ShortMessage();
					msg.setMessage(MidiUtils.NOTE_ON<<4, n.getKey(), 30);
					t.add(new MidiEvent(msg, tick));
					
					tick += n.getDuration();
					
					msg = new ShortMessage();
					msg.setMessage(MidiUtils.NOTE_OFF<<4, n.getKey(), 30);
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
			MidiSystem.write(seq, 1, out);
		} catch (IOException e) {
			logger.error("error writting midi sequence", e);
		}
	}
}
