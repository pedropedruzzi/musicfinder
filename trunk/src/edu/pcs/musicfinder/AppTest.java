package edu.pcs.musicfinder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;

import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;

public class AppTest {
	
	private static final String outputPath = "C:/Users/Redder/Desktop/";
	private static final String sourceFile = "resource/ode_to_joy.mid";
	private static final String outputBaseFile = "Ode to Joy";
	private static final int cuttingThreshold = 20000;
	
	public static void main(String[] args) {

		try {
			
			AppTest app = new AppTest();
			
			Sequence sequence = MidiSystem.getSequence(new File(sourceFile));
			app.createGraphics(sequence, 1);
//			app.createGraphics(sequence, 2);
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void createGraphics(Sequence sequence, int track) throws FileNotFoundException {
		SoundTrackExtractor extractor = new SoundTrackExtractor(sequence);
		SoundTrack soundTrack = extractor.extract(track);
		soundTrack.printTextChart(new PrintStream(outputPath + outputBaseFile + " - track" + track + ".txt"));
		soundTrack.printData(new PrintStream(outputPath + outputBaseFile + " - track" + track + " - data.txt"));
		
		SoundTrack cutted = soundTrack.subStrack(0, cuttingThreshold);
//		cutted.printTextChart(new PrintStream(outputPath + outputBaseFile + " - cutted - track" + track + ".txt"));
		
		System.out.println("Contains: " + soundTrack.contains(cutted));
	}
}
