package edu.pcs.musicfinder.automaton;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.pcs.musicfinder.RealNote;
import edu.pcs.musicfinder.SoundTrackExtractor;

public class Automaton {
	
	private State first;
	
	public static void main(String[] args) {
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
	
	
	

	public static double[] obtemAlturas(List<RealNote> cadeia) {
		double[] keys = new double[cadeia.size()];
		for (int i = 0; i < keys.length; i++) {
			keys[i] = SoundTrackExtractor.pitchToKeyDouble(cadeia.get(i).getPitch());
		}
		
		return keys;
	}

	public static int[] prepara(double[] keys) {
		double sum1 = 0;
		double sum2 = 0;

		// tudo em [0; 1[
		for (int i = 0; i < keys.length; i++) {
			sum1 += keys[i] - Math.floor(keys[i]);
			sum2 += keys[i] + 0.5 - Math.floor(keys[i] + 0.5);
		}
		
		double m1 = sum1 / keys.length;
		double m2 = sum2 / keys.length;
		
		double e1 = 0;
		double e2 = 0;
		for (int i = 0; i < keys.length; i++) {
			e1 += Math.abs(keys[i] - Math.floor(keys[i]) - m1);
			e2 += Math.abs(keys[i] + 0.5 - Math.floor(keys[i] + 0.5) - m2);
		}
		
		// verifica qual tem menor erro, faz a correção e calcula a média novamente
		
		double offset;
		if (e1 < e2) {
			offset = 0.5 - m1;
		} else {
			offset = -m2;
		}

		double sum = 0;
		for (int i = 0; i < keys.length; i++) {
			double tmp = keys[i] + offset;
			sum += tmp - Math.floor(tmp);
		}

		double m = sum / keys.length;
		double e = 0;

		for (int i = 0; i < keys.length; i++) {
			double tmp = keys[i] + offset;
			e += Math.abs(tmp - Math.floor(tmp) - m);
		}
		
		// depois de achar a média final
		offset += 0.5 - m;
		
		double erro = 0;
		int[] qKeys = new int[keys.length];
		for (int i = 0; i < keys.length; i++) {
			double tmp = keys[i] + offset;
			qKeys[i] = (int) Math.floor(tmp);
			erro += Math.abs(tmp - qKeys[i] - offset);
		}
		
		
		return qKeys;
	}
	

}
