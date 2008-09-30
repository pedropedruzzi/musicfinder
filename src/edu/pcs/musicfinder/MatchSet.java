package edu.pcs.musicfinder;

import java.util.SortedSet;
import java.util.TreeSet;

public class MatchSet {
	
	private SortedSet<Match> matches;
	private final int capacity;
	

	public MatchSet() {
		this(5);
	}
	
	public MatchSet(int capacity) {
		this.matches = new TreeSet<Match>();
		this.capacity = capacity;
	}
	
	public SortedSet<Match> getMatches() {
		return matches;
	}
	
	
	
	public void process(Song song, int score) {
		if (matches.size() < capacity) {
			System.out.println("entrou: " + score);
			matches.add(new Match(song, score));
		}
		else {
			Match first = matches.first();
			if (first.getScore() < score) {
				matches.remove(first);
				matches.add(new Match(song, score));
				System.out.println("saiu: " + first.getScore());
				System.out.println("entrou: " + score);
			} else {
				System.out.println("não entrou: " + score);
			}
		}
	}
	
	
	
	public static void main(String[] args) {
		MatchSet ms = new MatchSet();
		ms.process(new Song(), 1);
		ms.process(new Song(), 2);
		ms.process(new Song(), 3);
		ms.process(new Song(), 4);
		ms.process(new Song(), 5);
		ms.process(new Song(), 6);
		ms.process(new Song(), 7);
		ms.process(new Song(), 8);
		ms.process(new Song(), 9);
		ms.process(new Song(), 8);
		ms.process(new Song(), 4);
	}
	
	
	
}
