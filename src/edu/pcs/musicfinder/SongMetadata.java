package edu.pcs.musicfinder;

public class SongMetadata {
	
	private String artist;
	private String title;
	
	public SongMetadata() {
	
	}
	
	public SongMetadata(String title) {
		this.title = title;
	}
	
	public SongMetadata(String title, String artist) {
		this.title = title;
		this.artist = artist;
	}

	public String getArtist() {
		return artist;
	}

	public void setArtist(String artist) {
		this.artist = artist;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
	
}
