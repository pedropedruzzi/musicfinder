package edu.pcs.musicfinder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

public class MelodyFileUtils {
	
	public static List<RealNote> fromFile(String filename) throws FileNotFoundException {
		List<RealNote> track = new LinkedList<RealNote>();

		Scanner s = new Scanner(new File(filename));
		s.useLocale(Locale.US);
		
		while (s.hasNextDouble()) {
			double pitch = s.nextDouble();
			double duration = s.nextDouble();
			
			track.add(new RealNote(pitch, duration));
		}
		
		s.close();
		
		return track;
	}
	
	public static void toFile(List<RealNote> track, String filename) {
		FileWriter fw = null;
		
		try {
			fw = new FileWriter(filename);

			for (RealNote rn : track) {
				fw.append(Double.toString(rn.getPitch()) + "\t" + Double.toString(rn.getDuration()) + "\n");
			}
		} catch (IOException e) {
			System.err.println("error writing to file");
		} finally {
			if (fw != null) try { fw.close(); } catch (IOException e) { }
		}

	}
	
}
