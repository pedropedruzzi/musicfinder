package edu.pcs.musicfinder;

public class SongMetadata {
	
	private String artist;
	private String album;
	private String title;
	private int year;
	
	public SongMetadata() {
	
	}
	
	public SongMetadata(String title) {
		this.title = title;
	}
	
	public SongMetadata(String artist, String title) {
		this.artist = artist;
		this.title = title;
	}
	
	public SongMetadata(String artist, String album, String title, int year) {
		this.artist = artist;
		this.album = album;
		this.title = title;
		this.year = year;
	}

	public String getArtist() {
		return artist;
	}

	public void setArtist(String artist) {
		this.artist = artist;
	}

	public String getAlbum() {
		return album;
	}

	public void setAlbum(String album) {
		this.album = album;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
	}
	
	
	
}
