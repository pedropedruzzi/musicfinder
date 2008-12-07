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

public class SoundTrackExporter {
	
	private static final Logger logger = Logger.getLogger(SoundTrackExtractor.class);
	
	public static void export(List<RealNote> st, String filename) {
		final int channel = 0;
		final int velocity = 127;
		final int patch = 0; // piano
		
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
		
		if (patch != 0) {
			ShortMessage programChange = new ShortMessage();
			try {
				programChange.setMessage(ShortMessage.PROGRAM_CHANGE, channel, patch, 0);
			} catch (InvalidMidiDataException e) {
				e.printStackTrace();
			}
			t.add(new MidiEvent(programChange, tick));
		}

		for (RealNote n : st) {
			if (n.getPitch() != RealNote.SILENCE && underLimits(n.getPitch())) {
				try {
					t.add(new MidiEvent(new PitchWheelMessage(channel, SoundTrackExtractor.pitchToKeyResidual(n.getPitch())), tick));
					//logger.debug("pitch = " + n.getPitch());
					//logger.debug("key = " + SoundTrackExtractor.pitchToKey(n.getPitch()));
					//logger.debug("res = " + SoundTrackExtractor.pitchToKeyResidual(n.getPitch()));
					ShortMessage msg = new ShortMessage();
					msg.setMessage(ShortMessage.NOTE_ON, channel, SoundTrackExtractor.pitchToKey(n.getPitch()), velocity);
					t.add(new MidiEvent(msg, tick));
					
					tick += n.getDuration() * 300;
					
					msg = new ShortMessage();
					msg.setMessage(ShortMessage.NOTE_OFF, channel, SoundTrackExtractor.pitchToKey(n.getPitch()), velocity);
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
			MidiSystem.write(seq, 1, new File(filename));
		} catch (IOException e) {
			logger.error("error writting midi sequence", e);
		}
	}

	private static boolean underLimits(double pitch) {
		return pitch > 20 && pitch < 20000;
	}
	
}
