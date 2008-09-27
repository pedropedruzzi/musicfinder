package edu.pcs.musicfinder;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.jfree.data.time.FixedMillisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.ui.RefineryUtilities;


public class SoundTrack {
	
	public static int divider = 200;
	
	private List<Note> notes = new LinkedList<Note>();
	private SortedMap<Integer, Note> noteMap = new TreeMap<Integer, Note>();
	private int resolution = 120;
	
	public SoundTrack() {
		
	}
	
	private SoundTrack(SortedMap<Integer, Note> notes) {
		this.noteMap = notes;
	}
	
	public SoundTrack(int resolution) {
		this.resolution = resolution;
	}

	public SoundTrack(int resolution, List<Note> notes) {
		this.resolution = resolution;
		this.notes = notes;
	}

	public List<Note> getNotes() { return notes; }
	public SortedMap<Integer, Note> getNoteMap() { return noteMap; }
	public int getResolution() { return resolution; }
	
	public void addNote(Note note) {
		this.notes.add(note);
		this.noteMap.put(note.getTick(), note);
	}

	public int getLength() {
		return this.noteMap.lastKey() - this.noteMap.firstKey();
	}
	
	public boolean isSame(SoundTrack track) {
		
		List<Note> mySequence = new ArrayList<Note>(this.noteMap.values());
		List<Note> trackSequence = new ArrayList<Note>(track.noteMap.values());
		
		for (int i = 0; i < mySequence.size(); i++) {
			
			Note myNote  =  mySequence.get(i);
			Note trackNote  =  trackSequence.get(i);
			
			if (!myNote.isSame(trackNote)) return false;
		}
		
		return true;
	}
	
	public boolean contains(SoundTrack track) {
		for (int tick : track.noteMap.keySet()) {
			SoundTrack window = new SoundTrack(this.noteMap.subMap(tick, tick + track.getLength()));
			if (window.isSame(track)) return true;
		}
		
		return false;
	}
	
	public SoundTrack subStrack(int startTick, int endTick) {
		return new SoundTrack(this.noteMap.subMap(startTick, endTick));
	}
	
	public void printTextChart(PrintStream stream) {
		
		Map<Integer, StringBuilder> lines = new TreeMap<Integer, StringBuilder>();
		
		int totalLength = this.getLength() + this.noteMap.get(this.noteMap.lastKey()).getDuration() / divider;
		
		for (Note note : this.noteMap.values()) {
			if (!lines.containsKey(note.getKey()))
				lines.put(note.getKey(), new StringBuilder(StringUtils.repeat("-", totalLength)));
			
			StringBuilder builder = lines.get(note.getKey());
			builder.insert(note.getTick() / divider, StringUtils.repeat("*", note.getDuration() / divider));
		}
		
		for (Entry<Integer, StringBuilder> entry : lines.entrySet()) {
			StringBuilder builder = entry.getValue(); 
			builder.setLength(totalLength / divider);
			stream.println(entry.getKey() + " " + builder.toString());
		}
	}
	
	public void showChart() {
		
		TimeSeries series = new TimeSeries("xxxxx", FixedMillisecond.class);
		for (Note note : this.noteMap.values())
			for (int i = 0;  i < note.getDuration(); i++)
				series.add(new FixedMillisecond(note.getTick() + i), note.getKey());
				    	
        final TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(series);
		
        final ChartFrame demo = new ChartFrame("AAAAA", dataset);
        demo.pack();
        RefineryUtilities.centerFrameOnScreen(demo);
        demo.setVisible(true);	
	}
	
	public void printData(PrintStream stream) {
		for (Note note : this.noteMap.values()) {
			for (int i = 0; i < note.getDuration(); i++)
				stream.println(String.format("%s %s", note.getTick() + i, note.getKey()));
		}		
	}
}
