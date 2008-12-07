package edu.pcs.musicfinder;

import java.io.FileNotFoundException;
import java.io.PrintStream;

public class AppTest {
	
	private static final String outputPath = "C:/Users/Redder/Desktop/";
	private static final String sourceFile = "resource/ode_to_joy.mid";
	private static final String outputBaseFile = "Ode to Joy";
	private static final int cuttingThreshold = 20000;
	private final SoundTrackExtractor extractor;
	
	public AppTest(SoundTrackExtractor extractor) {
		this.extractor = extractor;
	}

	public static void main(String[] args) {

		try {
			SoundTrackExtractor ste = SoundTrackExtractor.open(sourceFile);
			AppTest app = new AppTest(ste);
			app.createGraphics(1);
//			app.createGraphics(2);
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void createGraphics(int track) throws FileNotFoundException {
		SoundTrack soundTrack = null;//extractor.extract(track);
		soundTrack.printTextChart(new PrintStream(outputPath + outputBaseFile + " - track" + track + ".txt"));
		soundTrack.printData(new PrintStream(outputPath + outputBaseFile + " - track" + track + " - data.txt"));
		
		SoundTrack cutted = soundTrack.subStrack(0, cuttingThreshold);
//		cutted.printTextChart(new PrintStream(outputPath + outputBaseFile + " - cutted - track" + track + ".txt"));
		
		System.out.println("Contains: " + soundTrack.contains(cutted));
	}
}
