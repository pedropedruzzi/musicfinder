package edu.pcs.musicfinder;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Scanner;

import javax.sound.midi.ShortMessage;

import edu.pcs.musicfinder.SoundTrackExtractor.SoundTrackBuilder;

public class MidiOxLogReader {
	
	public static List<RealNote> open(String filename) throws FileNotFoundException {
		SoundTrackBuilder builder = new SoundTrackBuilder();

		Scanner s = new Scanner(new File(filename));
		s.nextLine(); //header lines
		s.nextLine();
		s.nextLine();
		while (s.hasNext()) {
			String tok = s.next();
			if (tok.charAt(0) != '0') break;
			int timestamp = Integer.parseInt(tok, 16);
			s.next(); //in
			s.next(); //port
			int status = Integer.parseInt(s.next(), 16);
			int data1 = Integer.parseInt(s.next(), 16);
			int data2 = Integer.parseInt(s.next(), 16);
			s.nextLine(); //ignores the rest
			
			if (status == ShortMessage.NOTE_ON)
				builder.noteOn(timestamp, data1, data2);
			else if (status == ShortMessage.NOTE_OFF)
				builder.noteOff(timestamp, data1, data2);
		}
		
		s.close();
		
		return MelodyUtils.trim(MelodyUtils.toReal(builder.getTrack(), 1e-3));
	}
	
	public static void main(String[] args) throws FileNotFoundException {
		List<RealNote> track = MidiOxLogReader.open("resource/log.txt");
		SoundTrackExporter.export(track, "out/from_log.mid");
		MelodyFileUtils.toFile(track, "out/from_log.melody");
		System.out.println("Done!");
	}

}
