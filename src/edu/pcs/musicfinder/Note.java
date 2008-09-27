package edu.pcs.musicfinder;

public class Note {

	public static final int SILENCE = -1;
	public static final int NO_KEY = -2;
	
	private int key;
	private int duration;
	private int tick;
	
	public int getKey() { return key; }
	public void setKey(int key) { this.key = key; }
	
	public int getDuration() { return duration; }
	public void setDuration(int duration) { this.duration = duration; }
	
	public int getTick() { return tick; }
	public void setTick(int tick) { this.tick = tick; }
	
	public Note() {
	}
	
	public Note(int key, int duration, int tick) {
		this.key = key;
		this.duration = duration;
		this.tick = tick;
	}
	
	public boolean isSame(Note note) {
		return this.equals(note);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Note)) return false;
		if (this == obj) return true;
		
		Note note = (Note) obj;
		
		return this.key == note.key && this.duration == note.duration;
	}
}
