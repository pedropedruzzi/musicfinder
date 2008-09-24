package edu.poli.musicfinder.test;

import junit.framework.Assert;

import org.junit.Test;

import edu.pcs.musicfinder.Note;
import edu.pcs.musicfinder.SoundTrack;

public class SoundTrackTest {

	@Test
	public void testIsSame() {
		SoundTrack track1 = new SoundTrack();
		track1.addNote(new Note(20, 2, 0));
		track1.addNote(new Note(30, 2, 1));
		track1.addNote(new Note(10, 1, 2));
		track1.addNote(new Note(10, 3, 3)); // <-- Início do trecho igual
		track1.addNote(new Note(20, 2, 4));
		track1.addNote(new Note(30, 2, 5));
		track1.addNote(new Note(10, 1, 6));
		track1.addNote(new Note(10, 2, 7));
		track1.addNote(new Note(30, 1, 8)); // <-- Fim do trecho igual
		track1.addNote(new Note(30, 2, 9));
		track1.addNote(new Note(10, 1, 10));
		track1.addNote(new Note(10, 3, 11));
		track1.addNote(new Note(20, 2, 12));
		track1.addNote(new Note(30, 2, 13));
		
		SoundTrack track2 = new SoundTrack();
		track2.addNote(new Note(10, 3, 0));
		track2.addNote(new Note(20, 2, 1));
		track2.addNote(new Note(30, 2, 2));
		track2.addNote(new Note(10, 1, 3));
		track2.addNote(new Note(10, 2, 4));
		track2.addNote(new Note(30, 1, 5));
		
		Assert.assertTrue("Os trecho nao sao os mesmos", track1.contains(track2));			
	}	
}
