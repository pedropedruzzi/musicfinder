package edu.pcs.musicfinder;

import java.util.Collection;
import java.util.Collections;

public class Song {
	
	private SongMetadata metadata;
	private Collection<MelodyLine> lines;
	
	public Song() {
	
	}
	
	public Song(SongMetadata metadata, Collection<MelodyLine> lines) {
		this.metadata = metadata;
		this.lines = lines;
	}
	
	public SongMetadata getMetadata() {
		return metadata;
	}
	
	public void setMetadata(SongMetadata metadata) {
		this.metadata = metadata;
	}
	
	public Collection<MelodyLine> getLines() {
		return Collections.unmodifiableCollection(lines);
	}
	
	public void addLine(MelodyLine line) {
		lines.add(line);
	}
	
}
