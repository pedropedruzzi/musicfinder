package edu.pcs.musicfinder;

import java.io.File;
import java.io.IOException;

import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.Track;

public class TrackRecorder {

	private Sequence sequence;

	public Sequence getSequence() { return sequence; }
	public void setSequence(Sequence sequence) { this.sequence = sequence; }
	
	public void record(int trackNumber, File file) throws IOException {
		Track[] tracks = sequence.getTracks();
		for (int i = 0; i < tracks.length; i++)
			if (i != trackNumber) sequence.deleteTrack(tracks[i]);
			
		MidiSystem.write(this.sequence, 1, file);
	}
	
}
