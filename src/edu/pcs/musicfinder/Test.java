package edu.pcs.musicfinder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

import org.apache.log4j.Logger;

public class Test {
	
	private static final Logger logger = Logger.getLogger(Test.class);

	private static List<RealNote> extractTrackFromFile(File input, int trackNumber) {
		Sequence seq = null;

		try {
			seq = MidiSystem.getSequence(input);
		} catch (Exception e) {
			logger.error("error getting midi sequence", e);
			return null;
		}

		logger.debug("MIDI File Details");
		logger.debug("division=" + seq.getDivisionType());
		logger.debug("res=" + seq.getResolution());
		logger.debug("len us=" + seq.getMicrosecondLength());
		logger.debug("len tick=" + seq.getTickLength());
		
		SoundTrackExtractor extractor = new SoundTrackExtractor(seq);

		return extractor.extractReal(trackNumber);
	}
	
	public static void main2(String[] args) throws FileNotFoundException {
		RedderTrackFileParser parser = new RedderTrackFileParser();
		
		List<RealNote> track = parser.parse("resource/pitchs.txt");
		
		//modify(track, 2, 0, 0, 0);
		
		recordNotes(track, "out/pedrox1.mid");
	}
	
	public static void dumpPitchs(List<RealNote> l) {
		for (RealNote n : l) {
			System.out.println(n.getPitch());
		}
	}
	
	public static void main3(String[] args) {
		//loadRepository();
		List<RealNote> track = extractTrack("yesterday", 3);
		track = removeSilence(track);
		
		track = copyPart(track, 21, 30); //copyPart(track, 100, 16);
		//modify(part);
		System.out.println("Antes:");
		dumpPitchs(track);
		
		modify(track);
		
		System.out.println("Depois:");
		dumpPitchs(track);
		
		//search(track, part);
		
		recordNotes(track, "out/yesterday4.mid");
	}
	
	public static List<RealNote> testMelody() {
		List<RealNote> track = extractTrack("yesterday", 3);
		track = removeSilence(track);
		track = copyPart(track, 21, 30); //copyPart(track, 100, 16);
		dumpPitchs(track);
		return track;
	}
	
	public static void main(String[] args) throws FileNotFoundException {
		//List<RealNote> x = MelodyFileUtils.fromFile("out/redder.melody");
		List<RealNote> x = extractTrackFromFile(new File("out/redder_track_1.mid"), 0);
		//x = copyPart(x, 1, 150);

		toExcelPlot(x);
		//recordNotes(x, "out/ode_to_joy.mid");
		//MelodyFileUtils.toFile(x, "out/redder.melody");
	}
	
	public static void toExcelPlot(List<RealNote> x) {
		double t = 0;
		boolean space = false;
		NumberFormat nf = NumberFormat.getNumberInstance();
		nf.setMaximumFractionDigits(10);
		for (RealNote rn : x) {
			double pitch = rn.getPitch();
			if (pitch == RealNote.SILENCE) {
				System.out.println();
				t += rn.getDuration();
				space = false;
			} else {
				if (space) System.out.println();
				System.out.println(nf.format(t) + "\t" + nf.format(pitch));
				t += rn.getDuration();
				System.out.println(nf.format(t) + "\t" + nf.format(pitch));
				space = true;
			}
		}
	}
	
	private static SongRepository loadRepository() {
		SongRepository repo = new SongRepository();
		repo.addSong(new Song(new SongMetadata("Ludwig van Beethoven", "Symphony No. 9 in D minor, Op. 125 'Choral'", "Ode To Joy", 1824), Collections.singleton(new MelodyLine(extractTrack("ode_to_joy", 1)))));
		repo.addSong(new Song(new SongMetadata("The Beatles", "Hey Jude", "Hey Jude", 1968), Collections.singleton(new MelodyLine(extractTrack("hey_jude", 1)))));
		repo.addSong(new Song(new SongMetadata("The Beatles", "Penny Lane"), Collections.singleton(new MelodyLine(extractTrack("penny_lane", 2)))));
		repo.addSong(new Song(new SongMetadata("The Beatles", "Help!", "Yesterday", 1965), Collections.singleton(new MelodyLine(extractTrack("yesterday", 3)))));
		return repo;
	}

	private static double search(List<RealNote> track, List<RealNote> part) {
		int partSize = part.size();
		int max = track.size() - partSize;
		
		double minDistance = Double.MAX_VALUE;
		int position = -1;
		
		for (int start = 0; start <= max; start++) {
			double d = pitchDistance(track.subList(start, start + partSize), part);
			logger.debug("start = " + start);
			logger.debug("d = " + d);
			if (d < minDistance) {
				minDistance = d;
				position = start;
			}
		}
		
		logger.debug("minDistance = " + minDistance);
		logger.debug("position = " + position);
		
		return minDistance;
	}
	
	private static double distance(List<RealNote> x, List<RealNote> y) {
		if (x.size() != y.size()) throw new IllegalArgumentException("different sizes");
		
		// y = a.x
		double xy = 0;
		double xx = 0;
		
		Iterator<RealNote> itX = x.iterator();
		Iterator<RealNote> itY = y.iterator();
		
		while (itX.hasNext()) {
			double dX = itX.next().getDuration();
			double dY = itY.next().getDuration();
			logger.debug("x = " + dX);
			logger.debug("y = " + dY);
			xy += dX * dY;
			xx += dX * dX;
		}
		
		double a = xy / xx;
		logger.debug("a = " + a);
		
		itX = x.iterator();
		itY = y.iterator();
		
		double distance = 0;
		
		while (itX.hasNext()) {
			double dX = itX.next().getDuration();
			double dY = itY.next().getDuration();
			distance += (dY - a * dX) * (dY - a * dX);
		}
		
		return distance;
	}
	
	private static double pitchDistance(List<RealNote> x, List<RealNote> y) {
		if (x.size() != y.size()) throw new IllegalArgumentException("different sizes");
		
		// ln(y) = ln(x) + c
		double sum = 0;
		
		Iterator<RealNote> itX = x.iterator();
		Iterator<RealNote> itY = y.iterator();
		
		while (itX.hasNext()) {
			double dX = itX.next().getPitch();
			double dY = itY.next().getPitch();
			logger.debug("x = " + dX);
			logger.debug("y = " + dY);
			sum += Math.log(dY / dX);
		}
		
		double c = sum / x.size();
		logger.debug("c = " + c);
		logger.debug("c = " + 12 * c / Math.log(2) + " semitons");
		
		itX = x.iterator();
		itY = y.iterator();
		
		double distance = 0;
		
		while (itX.hasNext()) {
			double dX = itX.next().getPitch();
			double dY = itY.next().getPitch();
			distance += (Math.log(dX / dY) + c) * (Math.log(dX / dY) + c);
		}
		
		return distance;
	}
	
	public static void modify(List<RealNote> st) {
		final double durAbs = 1.0;
		final double durRel = 0.03;
		final double pitchFix = 3.5;
		final double pitchVar = 0.5;
		
		modify(st, durAbs, durRel, pitchFix, pitchVar);
	}
	
	public static void modify(List<RealNote> st, double durAbs, double durRel, double pitchFix, double pitchVar) {
		final Random rnd = new Random();
		
		for (RealNote n : st) {
			double dur = n.getDuration()*durAbs + rnd.nextGaussian()*durRel;
			n.setDuration(dur >= 0 ? dur : 0);
			
			if (n.getPitch() != RealNote.SILENCE) {
				double semitones = pitchFix + pitchVar * rnd.nextGaussian();
				n.setPitch(n.getPitch() * Math.pow(2, semitones / 12));
			}
		}
	}
	
	private static List<RealNote> copyPart(List<RealNote> st, int start, int length) {
		List<RealNote> parte = st.subList(start, start + length);
		List<RealNote> nova = new LinkedList<RealNote>();
		
		for (RealNote n : parte) nova.add(n.clone());
		
		return nova;
	}
	
	private static List<RealNote> removeSilence(List<RealNote> st) {
		List<RealNote> notes = new LinkedList<RealNote>();
		
		RealNote nova = null;
		for (RealNote n : st) {
			if (n.getPitch() == RealNote.SILENCE) {
				if (nova != null) {
					nova.setDuration(nova.getDuration() + n.getDuration());
				}
			} else {
				if (nova != null) {
					notes.add(nova);
				}
				nova = new RealNote(n.getPitch(), n.getDuration());
			}
		}
		if (nova != null) {
			notes.add(nova);
		}
		
		return notes;
	}

	public static List<RealNote> extractTrack(String tune, int trackNumber) {
		List<RealNote> st = null;
		try {
			st = extractTrackFromFile(new File("resource/" + tune + ".mid"), trackNumber);
		} catch (Exception e) {
			logger.error("extracting track", e);
		}
		return st;
	}

	public static void recordNotes(List<RealNote> st, String filename) {
		final int channel = 0;
		final int velocity = 90;
		final int patch = 72; // clarinet
		
		long tick = 0;
		final int res = 10;
		
		Sequence seq;
		try {
			seq = new Sequence(Sequence.SMPTE_30, res);
		} catch (InvalidMidiDataException e) {
			logger.error("creating midi sequence", e);
			return;
		}
		
		Track t = seq.createTrack();
		
		if (patch != 0) {
			ShortMessage programChange = new ShortMessage();
			try {
				programChange.setMessage(ShortMessage.PROGRAM_CHANGE, channel, patch, 0);
			} catch (InvalidMidiDataException e) {
				e.printStackTrace();
			}
			t.add(new MidiEvent(programChange, tick));
		}

		for (RealNote n : st) {
			if (n.getPitch() != RealNote.SILENCE && underLimits(n.getPitch())) {
				try {
					t.add(new MidiEvent(new PitchWheelMessage(channel, SoundTrackExtractor.pitchToKeyResidual(n.getPitch())), tick));
					logger.debug("pitch = " + n.getPitch());
					logger.debug("key = " + SoundTrackExtractor.pitchToKey(n.getPitch()));
					logger.debug("res = " + SoundTrackExtractor.pitchToKeyResidual(n.getPitch()));
					ShortMessage msg = new ShortMessage();
					msg.setMessage(ShortMessage.NOTE_ON, channel, SoundTrackExtractor.pitchToKey(n.getPitch()), velocity);
					t.add(new MidiEvent(msg, tick));
					
					tick += n.getDuration() * 300;
					
					msg = new ShortMessage();
					msg.setMessage(ShortMessage.NOTE_OFF, channel, SoundTrackExtractor.pitchToKey(n.getPitch()), velocity);
					t.add(new MidiEvent(msg, tick));
				} catch (InvalidMidiDataException e) {
					e.printStackTrace();
					return;
				}
			} else {
				tick += n.getDuration() * 300;
			}
		}
		
		try {
			MidiSystem.write(seq, 1, new File(filename));
		} catch (IOException e) {
			logger.error("error writting midi sequence", e);
		}
	}

	private static boolean underLimits(double pitch) {
		return pitch > 20 && pitch < 20000;
	}
}
