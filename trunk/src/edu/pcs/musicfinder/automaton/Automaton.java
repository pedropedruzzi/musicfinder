package edu.pcs.musicfinder.automaton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import edu.pcs.musicfinder.MelodyFileUtils;
import edu.pcs.musicfinder.RealNote;
import edu.pcs.musicfinder.SoundTrackExtractor;
import edu.pcs.musicfinder.Test;

public class Automaton {
	
	private final int length;
	private final int firstNote;
	private State first;
	private List<Status> status;
	
	private static enum Status { OK, EXCHANGE, ADDITION, OMISSION }
	
	public static void testAutomaton() {
		Automaton a = new Automaton(new int[] { 3, 4, 5, 6, 7, 8 });
		a.slidingWindow(new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13});
	}
	
	public void slidingWindow(int[] input) {
		for (int i = 0; i < input.length; i++)
			System.out.println("start = " + i + ": " + process(input, i));
	}
	
	public Automaton(int[] string) {
		length = string.length;
		firstNote = string[0];
		first = new FinalState();
		for (int i = length - 1; i >= 0; i--) {
			first = new SimpleAdaptiveState(string[i], first);
		}
	}
	
	public List<Status> getStatus() {
		return status;
	}
	
	public boolean process(int[] input, int start) {
		status = new ArrayList<Status>(length);
		int offset = firstNote - input[start];
		return first.process(input, start, offset);
	}
	
	private static abstract class State {
		public final boolean process(int[] input, int start, int offset) {
			if (start >= input.length) return isFinal();
			
			State s = nextState(input[start] + offset);
			if (s == null) return isFinal();
			else return s.process(input, start + 1, offset);
		}

		public abstract State nextState(int note);
		public abstract boolean isFinal();
	}
	
	private static class FinalState extends State {
		public State nextState(int note) {
			return null;
		}
		
		public boolean isFinal() {
			return true;
		}
	}
	
	private class SimpleState extends State {
		
		private int note;
		private State next;
		
		public SimpleState(int note, State next) {
			this.note = note;
			this.next = next;
		}

		public State nextState(int note) {
			if (note == this.note) {
				status.add(Status.OK);
				return next;
			} else {
				return null;
			}
		}
		
		public boolean isFinal() {
			return false;
		}
		
	}
	
	private class SimpleAdaptiveState extends State {
		
		private int note;
		private State next;
		
		public SimpleAdaptiveState(int note, State next) {
			this.note = note;
			this.next = next;
		}

		public State nextState(int note) {
			// System.out.println("next! veio " + note + ", quero " + this.note);
			if (note == this.note) {
				status.add(Status.OK);
				return next;
			} else {
				if (next.isFinal()) {
					status.add(Status.EXCHANGE);
					return next;
				} else {
					SimpleAdaptiveState s = (SimpleAdaptiveState) next;
					int note2 = s.note;
					if (note2 == note) {
						if (s.next.isFinal()) {
							status.add(Status.OMISSION);
							return s.next;
						} else {
							SimpleAdaptiveState s2 = (SimpleAdaptiveState) s.next;
							int note3 = s2.note;
							MapState ret = new MapState();
							ret.addTransition(note3, s2.next, Status.ADDITION);
							if (this.note != note3)
								ret.addTransition(this.note, this.next, Status.OMISSION);
							if (note != note3)
								ret.addTransition(note, s2, Status.EXCHANGE);
							return ret;
						}
					} else {
						if (this.note == note2) {
							status.add(Status.EXCHANGE);
							return new SimpleState(note2, s.next);
						} else {
							MapState ret = new MapState();
							ret.addTransition(this.note, this.next, Status.OMISSION);
							ret.addTransition(note2, s.next, Status.EXCHANGE);
							return ret;
						}
					}
				}
			}
		}
		
		public boolean isFinal() {
			return false;
		}
		
	}
	
	private class MapState extends State {
		
		private Map<Integer, StateStatus> map = new HashMap<Integer, StateStatus>();
		private class StateStatus {
			State state;
			Status status;
			public StateStatus(State state, Status status) {
				this.state = state;
				this.status = status;
			}
		}
		
		public State nextState(int note) {
			StateStatus ss = map.get(note);
			if (ss == null) return null;
			status.add(ss.status);
			status.add(Status.OK);
			return ss.state;
		}
		
		public void addTransition(int note, State state, Status status) {
			map.put(note, new StateStatus(state, status));
		}
		
		public boolean isFinal() {
			return false;
		}
		
	}
	
	
	

	public static double[] toKeys(List<RealNote> cadeia) {
		double[] keys = new double[cadeia.size()];
		for (int i = 0; i < keys.length; i++) {
			keys[i] = SoundTrackExtractor.pitchToKeyDouble(cadeia.get(i).getPitch());
		}
		
		return keys;
	}
	
	public static double offsetIterative(double[] keys, int steps) {
		double k = 0;
		
		for (int i = 0; i < steps; i++) {
			System.out.println("k = " + k + ": E = " + quantizationError(keys, k));
			k = offset2(keys, k);
		}
		
		return k;
	}

	public static double offset2(double[] keys, double k) {
		double sum1 = 0;
		double sum2 = 0;

		// tudo em [0; 1[
		for (int i = 0; i < keys.length; i++) {
			sum1 += keys[i] + k - Math.floor(keys[i] + k);
			sum2 += keys[i] + k + 0.5 - Math.floor(keys[i] + k + 0.5);
		}
		
		double m1 = sum1 / keys.length;
		double m2 = sum2 / keys.length;
		
//		double k1 = 1 - m1 + k;
//		double k2 = 1.5 - m2 + k;
		
		double k1 = k - m1;
		double k2 = 0.5 + k - m2;
		
		double e1 = quantizationError(keys, k1);
		double e2 = quantizationError(keys, k2);
		
		if (e1 < e2) return k1;
		else return k2;
	}

	public static List<Double> possibleKs(double[] keys) {
		double[] discontinuities = new double[keys.length];
		List<Double> list = new ArrayList<Double>(keys.length * 2 + 1);
		
		for (int i = 0; i < keys.length; i++) {
			discontinuities[i] = 0.5 + Math.floor(keys[i] + 0.5) - keys[i];
			list.add(discontinuities[i]);
		}
		
		Arrays.sort(discontinuities);
		Collections.sort(list);
		
		double last = 0;
		for (int i = 0; i < discontinuities.length; i++) {
			double k = (last + discontinuities[i]) / 2;
			
			double sum = 0;
			for (int j = 0; j < keys.length; j++) {
				double tmp = keys[j] - Math.floor(keys[j]) + k + 0.5;
				sum += Math.floor(keys[j]) + Math.floor(tmp) - keys[j];
			}
			
			k = sum / keys.length;
			if (k > last && k < discontinuities[i]) list.add(k);
			
			last = discontinuities[i];
		}
		
		if (last < 1) {
			// last interval
			double k = (last + 1) / 2;
			
			double sum = 0;
			for (int j = 0; j < keys.length; j++) {
				double tmp = keys[j] - Math.floor(keys[j]) + k + 0.5;
				sum += Math.floor(keys[j]) + Math.floor(tmp) - keys[j];
			}
			
			k = sum / keys.length;
			if (k > last && k < 1) list.add(k);
		}
		
		return list;
	}
	
	private static double bestK(double[] keys, List<Double> list) {
		double best = Double.NaN;
		double minError = Double.MAX_VALUE;
		for (double k : list) {
			double E = quantizationError(keys, k);
			//System.out.println("k = " + k + ": E = " + E);
			if (E < minError) {
				minError = E;
				best = k;
			}
		}
		return best;
	}
	
	private static double offset(double[] keys) {
		return bestK(keys, possibleKs(keys));
	}
	
	private static double normalizeOffset(double k) {
		return k - Math.floor(k + 0.5);
	}
	
	private static int[] quantize(double[] keys) {
		int[] string = new int[keys.length];
		
		double k = normalizeOffset(offset(keys));
		
		for (int i = 0; i < keys.length; i++)
			string[i] = (int) Math.round(keys[i] + k);
		
		return string;
	}
	
	private static double quantizationError(double[] keys, double k) {
		double sum = 0;
		
		for (double x : keys) {
			double e = x + k - Math.round(x + k);
			sum += e*e;
		}
		
		return sum;
	}
	
	public static void teste() {
		List<RealNote> melody = Test.testMelody();
		
		System.out.println("Cadeia original (MIDI)");
		for (RealNote n : melody) {
			System.out.println(SoundTrackExtractor.pitchToKey(n.getPitch()));
		}
		
		System.out.println("Cadeia original (em Hertz)");
		for (RealNote n : melody) {
			System.out.println(n.getPitch());
		}
		
		Test.modify(melody, 1, 0, 1.5, 0.3);
		
		MelodyFileUtils.toFile(melody, "out/trilha1.melody");
		Test.recordNotes(melody, "out/trilha1.mid");
		
		System.out.println("Cadeia distorcida (em Hertz)");
		for (RealNote n : melody) {
			System.out.println(n.getPitch());
		}

		double[] keys = toKeys(melody);
		int[] cadeia = quantize(keys);
		
		System.out.println("Cadeia distorcida após quantização:");
		for (int nota : cadeia)
			System.out.println(nota);
		
		// Automaton automaton = new Automaton(cadeia);
		
	}
	
	public static void main(String[] args) {
		System.out.println("Entre com uma cadeia terminada por 0 para gerar o autômato:");
		Scanner s = new Scanner(System.in);
		
		int[] ref = fromInput(s);
		Automaton a = new Automaton(ref);
		
		int op;
		while ((op = menu(s)) != 0) {
			if (op == 1) {
				System.out.println("Entre com a cadeia de entrada terminando com 0:");
				int[] input = fromInput(s);
				System.out.println(a.process(input, 0));
				System.out.println(a.getStatus());
			} else if (op == 2) {
				System.out.println("pitchFix:");
				double pitchFix = s.nextDouble();
				System.out.println("pitchVar:");
				double pitchVar = s.nextDouble();
				List<RealNote> track = fromKeys(ref);

				Test.modify(track, 1, 0, pitchFix, pitchVar);
				double[] keys = toKeys(track);
				System.out.println("Cadeia gerada:");
				System.out.println(Arrays.toString(keys));
				int[] input = quantize(keys);
				System.out.println("Após quantização:");
				System.out.println(Arrays.toString(input));
				System.out.println(a.process(input, 0));
				System.out.println(a.getStatus());
			}
		}
	}
	
	private static int menu(Scanner s) {
		System.out.println("Digite:");
		System.out.println("\t1, para digitar uma cadeia de entrada.");
		System.out.println("\t2, para gerar uma cadeia de entrada.");
		System.out.println("\t0, para sair.");
		return s.nextInt();
	}
	
	private static int[] fromInput(Scanner s) {
		List<Integer> lista = new ArrayList<Integer>();
		while (true) {
			int val = s.nextInt();
			if (val == 0) break;
			lista.add(val);
		}
		int[] input = new int[lista.size()];
		for (int i = 0; i < input.length; i++) input[i] = lista.get(i);
		return input;
	}
	
	private static List<RealNote> fromKeys(int[] keys) {
		List<RealNote> track = new ArrayList<RealNote>(keys.length);
		for (int key : keys) {
			track.add(new RealNote(SoundTrackExtractor.keyToPitch(key), 1));
		}
		return track;
	}
	

}
