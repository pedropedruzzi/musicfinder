package edu.pcs.musicfinder.automaton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.pcs.musicfinder.MelodyFileUtils;
import edu.pcs.musicfinder.RealNote;
import edu.pcs.musicfinder.SoundTrackExtractor;
import edu.pcs.musicfinder.Test;

public class Automaton {
	
	private State first;
	
	public static void testAutomaton() {
		Automaton a = new Automaton(new int[] { 3, 4, 5, 6, 7, 8 });
		a.slidingWindow(new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13});
	}
	
	public void slidingWindow(int[] input) {
		for (int i = 0; i < input.length; i++)
			System.out.println("start = " + i + ": " + process(input, i));
	}
	
	public Automaton(int[] string) {
		first = new FinalState();
		for (int i = string.length - 1; i >= 0; i--) {
			first = new SimpleAdaptiveState(string[i], first);
		}
	}
	
	public void reset() {
		// TODO: Desfaz todas as modificações adaptativas
		// Por enquanto elas já não são persistentes!
		// Deve entar resetar as informações de erros salvas
	}
	
	public boolean process(int[] input, int start) {
		return first.process(input, start);
	}
	
	private static abstract class State {
		public final boolean process(int[] input, int start) {
			if (start >= input.length) return isFinal();
			
			State s = nextState(input[start]);
			if (s == null) return isFinal();
			else return s.process(input, start + 1);
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
	
	private static class SimpleState extends State {
		
		private int note;
		private State next;
		
		public SimpleState(int note, State next) {
			this.note = note;
			this.next = next;
		}

		public State nextState(int note) {
			if (note == this.note) return next;
			else return null;
		}
		
		public boolean isFinal() {
			return false;
		}
		
	}
	
	private static class SimpleAdaptiveState extends State {
		
		private int note;
		private State next;
		
		public SimpleAdaptiveState(int note, State next) {
			this.note = note;
			this.next = next;
		}

		public State nextState(int note) {
			System.out.println("next! veio " + note + ", quero " + this.note);
			if (note == this.note) return next;
			else {
				if (next.isFinal()) return new SimpleState(this.note, next);
				else {
					SimpleAdaptiveState s = (SimpleAdaptiveState) next;
					int note2 = s.note;
					if (note2 == note) {
						if (s.next.isFinal()) return new SimpleState(note, s.next);
						else {
							SimpleAdaptiveState s2 = (SimpleAdaptiveState) s.next;
							int nextNextNext = s2.note;
							MapState ret = new MapState();
							ret.addTransition(note, s2);
							ret.addTransition(nextNextNext, s2.next);
							return ret;
						}
					} else {
						MapState ret = new MapState();
						ret.addTransition(note, next);
						ret.addTransition(note2, s.next);
						return ret;
					}
				}
			}
		}
		
		public boolean isFinal() {
			return false;
		}
		
	}
	
	private static class MapState extends State {
		
		private Map<Integer, State> map = new HashMap<Integer, State>();
		
		public State nextState(int note) {
			return map.get(note);
		}
		
		public void addTransition(int note, State s) {
			map.put(note, s);
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
	
	public static void main(String[] args) {
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
		
		Automaton automaton = new Automaton(cadeia);
		
	}
	

}
