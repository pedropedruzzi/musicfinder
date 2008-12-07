package edu.pcs.musicfinder;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class Test {
	
	private static SongRepository repository;
	static {
		repository = new SongRepository();
		addSong("Aquarela", "Toquinho", "aquarela", 3);
		addSong("Chega de Saudade", "João Gilberto", "chega_de_saudade", 4);
		addSong("Hey Jude", "The Beatles", "hey_jude", 8);
		addSong("Hino Nacional Brasileiro", "?", "hino_nacional", 1);
		addSong("Tarde em Itapoã", "Chico Buarque de Holanda", "itapoa", 4, 7);
		addSong("Marvin", "Titãs", "marvin", 0, 1, 5);
		addSong("Money for Nothing", "Dire Straits", "money_for_nothing", 3, 4);
		addSong("Ode To Joy", "Ludwig van Beethoven", "ode_to_joy", 1);
		addSong("País Tropical", "?", "pais_tropical", 2);
		addSongRemoveShort("Palco", "Gilberto Gil", "palco", 0.06, 5, 6, 10);
		addSong("Penny Lane", "The Beatles", "penny_lane", 1, 7);
		addSong("Sunday Bloody Sunday", "U2", "sunday_bloody_sunday", 6);
		addSong("Tico Tico no Fubá", "?", "tico_tico_no_fuba", 6);
		addSong("Você é Linda", "Caetano Veloso", "voce_e_linda", 3);
		addSongRemoveShort("Wave", "Antonio Carlos Jobim", "wave", 0.06, 0);
		addSong("With or Without You", "U2", "with_or_without_you", 3);
		addSong("Yesterday", "The Beatles", "yesterday", 1);
	}
	
	private static void addSong(String title, String artist, String tune, int ... channel) {
		addSongRemoveShort(title, artist, tune, 0, channel);
	}
	
	private static void addSongRemoveShort(String title, String artist, String tune, double minDuration, int ... channel) {
		Collection<MelodyLine> lines = null;
		if (channel.length == 1) {
			lines = Collections.singleton(new MelodyLine(openTune(tune).extractReal(channel[0])));
		} else {
			lines = new ArrayList<MelodyLine>(channel.length);
			SoundTrackExtractor ste = openTune(tune);
			for (int ch : channel) {
				List<RealNote> track = ste.extractReal(ch);
				if (minDuration != 0) track = MelodyUtils.removeNotesShorterThan(track, minDuration);
				lines.add(new MelodyLine(track));
			}
		}
		repository.addSong(new Song(new SongMetadata(title, artist), lines));
	}
	public static SoundTrackExtractor openTune(String tune) {
		return SoundTrackExtractor.open("resource/repertorio/" + tune + "/" + tune + ".mid");
	}
	
	public static List<RealNote> testMelody() {
		SoundTrackExtractor ste = openTune("yesterday");
		List<RealNote> track = ste.extractReal(3);
		track = MelodyUtils.removeSilence(track);
		track = MelodyUtils.copyPart(track, 21, 30); //copyPart(track, 100, 16);
		MelodyUtils.dumpPitchs(track);
		return track;
	}
	
	public static void main2(String[] args) throws FileNotFoundException {
		RedderTrackFileParser parser = new RedderTrackFileParser();
		
		List<RealNote> track = parser.parse("resource/pitchs.txt");
		
		//MelodyUtils.modify(track, 2, 0, 0, 0);
		
		SoundTrackExporter.export(track, "out/pedrox1.mid");
	}
	
	public static void main3(String[] args) {
		//loadRepository();
		SoundTrackExtractor ste = openTune("yesterday");
		List<RealNote> track = ste.extractReal(3);
		track = MelodyUtils.removeSilence(track);
		
		track = MelodyUtils.copyPart(track, 21, 30); //copyPart(track, 100, 16);
		//MelodyUtils.modify(part);
		System.out.println("Antes:");
		MelodyUtils.dumpPitchs(track);
		
		MelodyUtils.modify(track);
		
		System.out.println("Depois:");
		MelodyUtils.dumpPitchs(track);
		
		//MelodyUtils.search(track, part);
		
		SoundTrackExporter.export(track, "out/yesterday4.mid");
	}
	
	public static void main(String[] args) throws FileNotFoundException {
		String file = "/Users/Isabela/pedro/repertorio/yesterday.mid";
		System.out.println("MIDI File: " + file);
		SoundTrackExtractor ste = SoundTrackExtractor.open(file);
		
		ste.testeTempo();
		
		for (int ch = 0; ch < 16; ch++) {
			double r = ste.ratioPoliphonic(ch);
			if (r < 0.2) {
				List<RealNote> track = ste.extractReal(ch);
				if (track.size() != 0) {
					System.out.println("CH #" + ch + " " + r);
					SoundTrackExporter.export(MelodyUtils.trim(track), file + "_" + ch + ".mid");
					//SoundTrackExporter.export(trim(removeNotesShorterThan(track, 0.06)), file + "_" + ch + ".mid");
				}
			}
		}
		
		//List<RealNote> x = MelodyFileUtils.fromFile("out/redder.melody");
		//List<RealNote> x = ste.extractReal(1, 0);
		//x = MelodyUtils.removeNotesShorterThan(x, 0.02);
		//x = MelodyUtils.removeSilence(x);
		
		//x = MelodyUtils.copyPart(x, 1, 150);

		//MelodyUtils.toExcelPlot(x);
		//SoundTrackExporter.export(MelodyUtils.trim(x), "/Users/Isabela/pedro/repertorio/1111.mid");
		//MelodyFileUtils.toFile(x, "out/redder.melody");
	}
	
}
