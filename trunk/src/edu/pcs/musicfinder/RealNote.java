package edu.pcs.musicfinder;

public class RealNote {

	public static final double SILENCE = -1;
	
	private double pitch;
	private double duration;
	
	public double getPitch() { return pitch; }
	public void setPitch(double pitch) { this.pitch = pitch; }
	
	public double getDuration() { return duration; }
	public void setDuration(double duration) { this.duration = duration; }
	
	public RealNote() {
		
	}
	
	public RealNote(double pitch, double duration) {
		this.pitch = pitch;
		this.duration = duration;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof RealNote)) return false;
		if (this == obj) return true;
		
		RealNote note = (RealNote) obj;
		
		return this.pitch == note.pitch && this.duration == note.duration;
	}
	
	@Override
	protected RealNote clone() {
		return new RealNote(getPitch(), getDuration());
	}
}
