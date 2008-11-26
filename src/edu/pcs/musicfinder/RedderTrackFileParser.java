package edu.pcs.musicfinder;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

public class RedderTrackFileParser {
	
	public List<RealNote> parse(String filename) throws FileNotFoundException {
		List<RealNote> track = new LinkedList<RealNote>();

		Scanner s = new Scanner(new File(filename));
		s.useLocale(Locale.US);
		int i = 1;
		
		double lastKey = -1;
		double lastTime = 0;
		double time = 0, key;
		
		while (s.hasNextDouble()) {
			time = s.nextDouble();
			key = s.nextDouble();
			
			if (key != lastKey) {
				if (lastKey != -1) {
					double pitch;
					if (lastKey == 90) {
						pitch = RealNote.SILENCE;
					} else {
						pitch = lastKey;
						System.out.println(i++ + "\t" + lastKey + "\t" + (time - lastTime));
					}
					track.add(new RealNote(pitch, time - lastTime));
				}
				
				lastTime = time;
				lastKey = key;
			}
		}

		if (lastKey != -1) {
			double pitch;
			if (lastKey == 90) {
				pitch = RealNote.SILENCE;
			} else {
				pitch = lastKey;
				System.out.println(i++ + "\t" + lastKey + "\t" + (time - lastTime));
			}
			track.add(new RealNote(pitch, time - lastTime));
		}
		
		return track;
	}
	
}
