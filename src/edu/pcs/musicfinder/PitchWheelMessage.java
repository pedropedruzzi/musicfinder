package edu.pcs.musicfinder;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.ShortMessage;

public class PitchWheelMessage extends ShortMessage {

	public PitchWheelMessage(int channel, double semitones) {
		int val = (int) (semitones * 0x1000) + 0x2000;
		if (val >> 14 != 0) throw new IllegalArgumentException();
		
		int data1 = val & 0x7f;
		int data2 = val >> 7;
		
		try {
			this.setMessage(ShortMessage.PITCH_BEND, channel, data1, data2);
		} catch (InvalidMidiDataException e) {
			e.printStackTrace();
		}
	}

}
