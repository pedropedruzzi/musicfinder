package edu.pcs.musicfinder;

import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class Player {

	public void play(AudioInputStream stream) {
		
		try {
			AudioFormat format = stream.getFormat();

			SourceDataLine.Info info = new DataLine.Info(SourceDataLine.class, stream.getFormat(), ((int) stream.getFrameLength() * format.getFrameSize()));
			SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
			line.open(stream.getFormat());
			line.start();

			System.out.println(line.getBufferSize());
			
			int numRead = 0;
			byte[] buf = new byte[line.getBufferSize()];
			while ((numRead = stream.read(buf, 0, buf.length)) >= 0) {
				int offset = 0;
				while (offset < numRead) {
					offset += line.write(buf, offset, numRead - offset);
				}
			}
			line.drain();
			line.stop();
			
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
