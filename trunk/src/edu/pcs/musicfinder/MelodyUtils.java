package edu.pcs.musicfinder;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;

public class MelodyUtils {
	
	private static final Logger logger = Logger.getLogger(MelodyUtils.class);
	
	public static void dumpPitchs(List<RealNote> l) {
		for (RealNote n : l) {
			System.out.println(n.getPitch());
		}
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

	public static double search(List<RealNote> track, List<RealNote> part) {
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
	
	public static double distance(List<RealNote> x, List<RealNote> y) {
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
	
	public static double pitchDistance(List<RealNote> x, List<RealNote> y) {
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
	
	public static List<RealNote> copyPart(List<RealNote> st, int start, int length) {
		List<RealNote> parte = st.subList(start, start + length);
		List<RealNote> nova = new LinkedList<RealNote>();
		
		for (RealNote n : parte) nova.add(n.clone());
		
		return nova;
	}
	
	public static List<RealNote> trim(List<RealNote> st) {
		List<RealNote> notes = new ArrayList<RealNote>(st.size());
		
		Iterator<RealNote> it = st.iterator();
		
		while (it.hasNext()) {
			RealNote rn = it.next();
			if (rn.getPitch() != RealNote.SILENCE) {
				notes.add(rn);
				break;
			}
		}
		
		RealNote silence = null;
		while (it.hasNext()) {
			RealNote rn = it.next();
			if (rn.getPitch() == RealNote.SILENCE) {
				if (silence != null) {
					silence.setDuration(silence.getDuration() + rn.getDuration());
				} else {
					silence = rn;
				}
			} else {
				if (silence != null) {
					notes.add(silence);
					silence = null;
				}
				notes.add(rn);
			}
		}
		
		return notes;
	}
	
	public static List<RealNote> removeSilence(List<RealNote> st) {
		List<RealNote> notes = new ArrayList<RealNote>(st.size());
		
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

	public static List<RealNote> removeNotesShorterThan(List<RealNote> st, double seconds) {
		List<RealNote> notes = new ArrayList<RealNote>(st.size());
		
		RealNote nova = null;
		for (RealNote n : st) {
			double dur = n.getDuration();
			if (n.getPitch() != RealNote.SILENCE && dur < seconds) {
				if (nova != null) {
					nova.setDuration(nova.getDuration() + dur);
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
	
}
