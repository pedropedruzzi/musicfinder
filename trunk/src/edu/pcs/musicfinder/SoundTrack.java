package edu.pcs.musicfinder;

import java.io.PrintStream;
import java.util.ArrayList;
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
	private SortedMap<Integer, Note> notes = new TreeMap<Integer, Note>();

	public SortedMap<Integer, Note> getNotes() { return notes; }
	
	public void addNote(Note note) {
		this.notes.put(note.getTick(), note);
	}
	
	public SoundTrack() {
	}
	
	private SoundTrack(SortedMap<Integer, Note> notes) {
		this.notes = notes;
	}
	
	public int getLength() {
		return this.notes.lastKey() - this.notes.firstKey();
	}
	
	public boolean isSame(SoundTrack track) {
		
		List<Note> mySequence = new ArrayList<Note>(this.notes.values());
		List<Note> trackSequence = new ArrayList<Note>(track.notes.values());
		
		for (int i = 0; i < mySequence.size(); i++) {
			
			Note myNote  =  mySequence.get(i);
			Note trackNote  =  trackSequence.get(i);
			
			if (!myNote.isSame(trackNote)) return false;
		}
		
		return true;
	}
	
	public boolean contains(SoundTrack track) {
		for (int tick : track.notes.keySet()) {
			SoundTrack window = new SoundTrack(this.notes.subMap(tick, tick + track.getLength()));
			if (window.isSame(track)) return true;
		}
		
		return false;
	}
	
	public SoundTrack subStrack(int startTick, int endTick) {
		return new SoundTrack(this.notes.subMap(startTick, endTick));
	}
	
	public void printTextChart(PrintStream stream) {
		
		Map<Integer, StringBuilder> lines = new TreeMap<Integer, StringBuilder>();
		
		int totalLength = this.getLength() + this.notes.get(this.notes.lastKey()).getDuration() / divider;
		
		for (Note note : this.notes.values()) {
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
		for (Note note : this.notes.values())
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
		for (Note note : this.notes.values()) {
			for (int i = 0; i < note.getDuration(); i++)
				stream.println(String.format("%s %s", note.getTick() + i, note.getKey()));
		}		
	}
}
