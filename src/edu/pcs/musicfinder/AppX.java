package edu.pcs.musicfinder;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.jfree.ui.RefineryUtilities;

public class AppX {
	
	public static AudioInputStream createAudioInputStream() {
		try {
			return AudioSystem.getAudioInputStream(new URL("file:///C:/Users/Redder/Desktop/Coop8/TCC/tcc-results/ode-tojoy-assobio.wav"));
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;	
	}
	
	public static void main(String[] args) {
		
		try {
			AudioInputStream stream = createAudioInputStream();
			stream.mark(stream.available());
			
			byte[] buf = new byte[stream.available()];
			stream.read(buf, 0, buf.length);
			stream.reset();
			
			int div = 500;
			
			
			double data[] = new double[buf.length / div + 1];
			
			int j = 0;
			for (int i = 0; i < buf.length; i++) {
				if (i % div == 0)
					data[j++] = buf[i];
			}
			
			final ChartFrame demo = new ChartFrame("Time Series Demo 8", data);
			demo.pack();
			RefineryUtilities.centerFrameOnScreen(demo);
			demo.setVisible(true);
			
			Player player = new Player();
			player.play(stream);
		
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}
