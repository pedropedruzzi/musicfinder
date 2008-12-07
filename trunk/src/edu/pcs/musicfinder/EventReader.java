package edu.pcs.musicfinder;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.Track;

/**
 * Iterates over MidiEvent`s from all the given Tracks, in timestamp order
 *
 */
public class EventReader implements Iterator<MidiEvent>, Iterable<MidiEvent> {
	
	private static final int NO_TRACK = -1;
	private final Track[] tracks;
	private final int[] index;
	private final MidiEvent[] last;
	private int tracksLeft;

	public EventReader(Track[] tracks) {
		this.tracks = tracks;
		this.index = new int[tracks.length];
		this.last = new MidiEvent[tracks.length];
		this.tracksLeft = tracks.length;
	}
	
	public boolean hasNext() {
		for (int i = 0; i < tracks.length; i++) {
			if (index[i] < tracks[i].size()) return true;
		}
		return false;
	}
	
	public MidiEvent next() {
		long first = Integer.MAX_VALUE;
		int track = NO_TRACK;
		
		for (int i = 0; i < tracks.length; i++) {
			if (index[i] < tracks[i].size()) {
				if (last[i] == null) {
					last[i] = tracks[i].get(index[i]);
				}
				long tick = last[i].getTick();
				if (tick < first) {
					first = tick;
					track = i;
				}
			}
		}
		
		if (track == NO_TRACK) throw new NoSuchElementException();
		
		MidiEvent ret = tracks[track].get(index[track]);
		index[track]++;
		last[track] = null;
		if (index[track] == tracks[track].size()) tracksLeft--;
		
		return ret;
	}
	
	public void remove() {
		throw new UnsupportedOperationException();
	}
	
	public void reset() {
		tracksLeft = tracks.length;
		Arrays.fill(last, null);
		Arrays.fill(index, 0);
	}

	public Iterator<MidiEvent> iterator() {
		return this;
	}
	
}
