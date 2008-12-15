package edu.pcs.musicfinder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import edu.pcs.musicfinder.automaton.Automaton;
import edu.pcs.musicfinder.automaton.Automaton.Status;

public class Test {
	
	private static final Logger logger = Logger.getLogger(Test.class);
	
	private static SongRepository repository;
	static {
		logger.info("loading repository...");
		loadRepository();
		logger.info("done!");
	}
	
	private static void loadRepository() {
		repository = new SongRepository();
		addSong("Aquarela", "Toquinho", "aquarela", 3);
		addSong("Chega De Saudade", "Vinícius De Moraes/Antonio Carlos Jobim", "chega_de_saudade", 4);
		addSong("Hey Jude", "The Beatles", "hey_jude", 8);
		addSong("Hino Do Corinthians", "Láuro D'Ávila", "hino_corinthians", 0);
		addSong("Hino Nacional Brasileiro", "Joaquim Osório Duque Estrada/Francisco Manuel Da Silva", "hino_nacional", 1);
		addSong("Tarde Em Itapoã", "Toquinho/Vinícius De Moraes", "itapoa", 4, 7);
		addSong("Marvin", "Titãs", "marvin", 0, 1, 5);
		addSong("Money For Nothing", "Dire Straits", "money_for_nothing", 3, 4);
		addSong("Ode To Joy", "Ludwig Van Beethoven", "ode_to_joy", 1);
		addSong("País Tropical", "Jorge Ben Jor", "pais_tropical", 2);
		addSongRemoveShort("Palco", "Gilberto Gil", "palco", 0.06, 5, 6, 10);
		addSong("Penny Lane", "The Beatles", "penny_lane", 1, 7);
		addSong("Sunday Bloody Sunday", "U2", "sunday_bloody_sunday", 6);
		addSong("Tico Tico No Fubá", "Zequinha De Abreu", "tico_tico_no_fuba", 6);
		addSong("Você É Linda", "Caetano Veloso", "voce_e_linda", 3);
		addSongRemoveShort("Wave", "Antonio Carlos Jobim", "wave", 0.06, 0);
		addSong("We Are The Champions", "Queen", "we_are_the_champions", 3);
		addSong("With Or Without You", "U2", "with_or_without_you", 3);
		addSong("Yesterday", "The Beatles", "yesterday", 1);
		addSong("Happy Birthday To You", "Unkown", "birthday", 0);
	}
	
	private static void addSong(String title, String artist, String tune, int ... channel) {
		addSongRemoveShort(title, artist, tune, 0.03, channel);
	}
	
	private static void addSongRemoveShort(String title, String artist, String tune, double minDuration, int ... channel) {
		Collection<MelodyLine> lines = null;
		if (channel.length == 1) {
			List<RealNote> track = openTune(tune).extractReal(channel[0]);
			if (minDuration != 0) track = MelodyUtils.removeNotesShorterThan(track, minDuration);
			lines = Collections.singleton(new MelodyLine(track));
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
	
	public static void extractMelodyTracks(String file) throws FileNotFoundException {
		System.out.println("MIDI File: " + file);
		SoundTrackExtractor ste = SoundTrackExtractor.open(file);
		
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
	
	public static void main(String[] args) throws FileNotFoundException {
		//liveTest();
		//processInput();
		//extractMelodyTracks("resource/birthday.mid");
		processAll();
	}
	
	public static void liveTest() throws FileNotFoundException {
		final String filename = "resource/log.txt";
		final File input = new File(filename);
		long last = 0;
		while (true) {
			while (input.length() == 0 || input.lastModified() == last) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			last = input.lastModified();
			
			List<RealNote> track = MidiOxLogReader.open(filename);
			if (track.size() == 0) continue;
			
			SoundTrackExporter.export(track, "out/last_processed.mid");
			MelodyFileUtils.toFile(track, "out/last_processed.melody");
			
			process(track, System.out, null);
		}
	}
	
	public static void processInput() throws FileNotFoundException {
		List<RealNote> track = MelodyFileUtils.fromFile2("resource/input.txt");
		process(track, System.out, "resource/input");
	}
	
	public static void dumpRepositoryData() throws FileNotFoundException {
		final String base = "out/repo/";
		
		for (Song song : repository.getSongs()) {
			int i = 0;
			for (MelodyLine ml : song.getLines()) {
				SoundTrackExporter.export(MelodyUtils.trim(ml), base + song.getMetadata().getTitle() + (i++) + ".mid");
			}
		}
	}
	
	public static void teste1() throws FileNotFoundException {
		List<RealNote> input = MelodyFileUtils.fromFile2("resource/recordings/chega_de_saudade1.txt");
		Automaton automaton = Automaton.fromMelody(input);
		List<RealNote> track = MelodyUtils.removeNotesShorterThan(openTune("chega_de_saudade").extractReal(4), 0.03);
		
		int[] line = Automaton.quantizeFromMelody(track);
		boolean ret = automaton.process(line, 0);
		
		System.out.println(ret);
		System.out.println(automaton.getStatus());
	}
	
	public static void processAll() throws FileNotFoundException {
		//process(MelodyFileUtils.fromFile("out/teste.melody"));
		//process(MelodyFileUtils.fromFile2("resource/recordings/yesterday1.melody2"));
		File dir = new File("resource/recordings");
		for (File f : dir.listFiles()) {
			if (f.isFile()) {
				String path = f.getAbsolutePath();
				if (path.endsWith(".txt")) {
					System.out.println(path);
					PrintStream out = new PrintStream(path + ".out");
					List<RealNote> track = MelodyFileUtils.fromFile2(path);
					SoundTrackExporter.export(track, path + ".mid");
					process(track, out, path);
					out.close();
				}
			}
		}
	}
	
	private static class Match implements Comparable<Match> {
		int errors;
		int start;
		Song song;
		List<Status> status;
		List<RealNote> line;

		public int compareTo(Match o) {
			return errors - o.errors;
		}

		public Match(int errors, int start, List<RealNote> line, Song song, List<Status> status) {
			this.errors = errors;
			this.start = start;
			this.line = line;
			this.song = song;
			this.status = status;
		}
	}

	private static void process(List<RealNote> track, PrintStream out, String basename) {
		logger.info("Processing new input");

		int[] keys = Automaton.quantizeFromMelody(track, out);
		if (basename != null) {
			SoundTrackExporter.export(track, basename + ".mid");
			SoundTrackExporter.export(create(keys, track), basename + ".quant.mid");
		}
		
		// generate automaton
		Automaton automaton = new Automaton(keys);
		
		List<Match> matches = new ArrayList<Match>();
		
		for (Song song : repository.getSongs()) {
			int minErrors = Integer.MAX_VALUE;
			int matchStart = -1;
			List<Status> matchStatus = null;
			List<RealNote> matchLine = null;
			
			for (MelodyLine ml : song.getLines()) {
				int[] line = Automaton.quantizeFromMelody(ml);
				for (int start=0; start<line.length; start++) {
					boolean ret = automaton.process(line, start);
					
					List<Status> status = automaton.getStatus();
					int err = countErrors(status);
					if (!ret) err += left(status, keys.length) + 100;
					if (err < minErrors) {
						minErrors = err;
						matchStart = start;
						matchStatus = status;
						matchLine = ml;
					}
				}
			}

			if (minErrors != Integer.MAX_VALUE) {
				matches.add(new Match(minErrors, matchStart, matchLine, song, matchStatus));
			}
		}
		
		Collections.sort(matches);
		
		for (Match m : matches) {
			out.println("MATCH!");
			out.println(m.song.getMetadata().getTitle());
			out.println("start = " + m.start);
			out.println("err = " + m.errors);
			out.println(m.status);
			
			// corrigir e comparar duraçoes
			List<RealNote> fixed = fix(track, m.status);
			List<RealNote> match = findMatch(m.line, m.start, fixed.size());
			
			if (basename != null)
				SoundTrackExporter.export(match, basename + "_match_" + m.song.getMetadata().getTitle() + ".mid");
			
			double d = MelodyUtils.distance(fixed, match);
			
			out.println("distance = " + d);
			printChart2(fixed, match, out);
			out.println();
		}
	}

	private static List<RealNote> create(int[] keys, List<RealNote> track) {
		List<RealNote> ret = new ArrayList<RealNote>(track.size());
		int i = 0;
		for (RealNote rn : track) {
			if (rn.getPitch() == RealNote.SILENCE) ret.add(rn);
			else ret.add(new RealNote(MelodyUtils.keyToPitch(keys[i++]), rn.getDuration()));	
		}
		return ret;
	}

	private static void printChart2(List<RealNote> fixed, List<RealNote> match, PrintStream out) {
		NumberFormat nf = NumberFormat.getNumberInstance();
		nf.setMaximumFractionDigits(10);
		for (int i=0; i<fixed.size(); i++) {
			out.println(nf.format(fixed.get(i).getDuration()) + "\t" + nf.format(match.get(i).getDuration()));
		}
	}

	private static List<RealNote> findMatch(List<RealNote> matchLine, int start, int length) {
		return MelodyUtils.removeSilence(matchLine).subList(start, start + length);
	}

	private static List<RealNote> fix(List<RealNote> track, List<Status> status) {
		track = MelodyUtils.removeSilence(track);
		List<RealNote> ret = new ArrayList<RealNote>(status.size());
		Iterator<RealNote> itT = track.iterator();
		Iterator<Status> itS = status.iterator();
		
		while (itS.hasNext()) {
			switch (itS.next()) {
			case OK:
			case EXCHANGE: // TODO: para altura precisa corrigir altura!
				ret.add(itT.next());
				break;
			case ADDITION:
				RealNote rem = ret.remove(ret.size() - 1);
				ret.add(new RealNote(rem.getPitch(), rem.getDuration() + itT.next().getDuration()));
				break;
			case OMISSION:
				ret.add(new RealNote(440, 0));
				break;
			}
		}
		
		// a ultima tem duracao duvidosa!
		ret.remove(ret.size() - 1);
		
		return ret;
	}

	private static int countErrors(List<Status> status) {
		int errCount = 0;
		for (Status s : status)
			if (s != Status.OK) errCount++;
		return errCount;
	}

	private static int left(List<Status> status, int length) {
		int consumiu = 0;
		for (Status s : status) {
			switch (s) {
			case OK:
			case EXCHANGE:
			case ADDITION:
				consumiu++;
				break;
			}
		}
		return length - consumiu;
	}
	
}
