package edu.pcs.musicfinder;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

public class SongRepository {
	
	private Collection<Song> songs = new HashSet<Song>();

	public Collection<Song> getSongs() {
		return Collections.unmodifiableCollection(songs);
	}
	
	public void addSong(Song song) {
		songs.add(song);
	}
	
}
