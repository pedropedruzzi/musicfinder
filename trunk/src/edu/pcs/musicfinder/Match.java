package edu.pcs.musicfinder;


public class Match implements Comparable<Match> {

	private Song song;
	private int score;
	
	public Match(Song song, int score) {
		this.song = song;
		this.score = score;
	}
	
	public Song getSong() {
		return song;
	}
	
	public int getScore() {
		return score;
	}

	@Override
	public int compareTo(Match o) {
		// não está consistente com equals!
		return score - o.score;
	}
	
}
