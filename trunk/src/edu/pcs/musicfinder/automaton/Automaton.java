package edu.pcs.musicfinder.automaton;

import java.util.List;

import edu.pcs.musicfinder.RealNote;
import edu.pcs.musicfinder.SoundTrackExtractor;

public class Automaton {

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
	
	public void reset() {
		// TODO: Desfaz todas as modificações adaptativas
	}
	
	public boolean process(int[] input, int start) {
		int state = 0;
		
		
		return false;
	}
	
	
	
	

}
