package onetothefour;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.net.URL;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.UnsupportedAudioFileException;

public class Main {
	/*
	 * https://www.youtube.com/watch?v=nUKya2DvYSo Java Tutorials: Ep. 21 - Java
	 * Sound API - Clips
	 */

	public static Mixer mixer;
	public static Clip clip;

	public static void main (String[] args) {
		Mixer.Info[] mixInfos = AudioSystem.getMixerInfo();
		
		mixer = AudioSystem.getMixer(mixInfos[0]);
		
		DataLine.Info dataInfo = new DataLine.Info(Clip.class, null);
		try { clip = (Clip)mixer.getLine(dataInfo); }
		catch (LineUnavailableException lue) { lue.printStackTrace(); }
		
		try {
			URL soundURL = Main.class.getResource("/onetothefour/Amen-Break.wav");
			AudioInputStream ais = AudioSystem.getAudioInputStream(soundURL);
			clip.open(ais);
		}
		catch (LineUnavailableException lue) { lue.printStackTrace(); }
		catch (IOException io) { io.printStackTrace(); } 
		catch (UnsupportedAudioFileException e) { e.printStackTrace(); }
	
		Clip duplicate = clip;
		
		int lengthMS = (int)(duplicate.getMicrosecondLength()*0.001);
		
		int quarterLength = lengthMS / 4;
		int[] splitBeginningPts = new int [4];
		
		for (int x = 0; x < 4; x++) {
			splitBeginningPts[x] = quarterLength * x;
		}
		
		String startURL = "C:/Users/mattc/Documents/Code 2019/Java/OneToTheFour/src/";
		
		//Clipper.copyAudio(startURL + "onetothefour/Amen-Break.wav", startURL + "Amen-Break-1-2.wav", 0, 3);
		Clipper.copyAudioMs(startURL + "onetothefour/Amen-Break.wav", startURL + "Amen-Break-1.wav", splitBeginningPts[0], quarterLength);
		Clipper.copyAudioMs(startURL + "onetothefour/Amen-Break.wav", startURL + "Amen-Break-2.wav", splitBeginningPts[1], quarterLength);
		Clipper.copyAudioMs(startURL + "onetothefour/Amen-Break.wav", startURL + "Amen-Break-3.wav", splitBeginningPts[2], quarterLength);
		Clipper.copyAudioMs(startURL + "onetothefour/Amen-Break.wav", startURL + "Amen-Break-4.wav", splitBeginningPts[3], quarterLength);
		
		String ab1 = startURL + "Amen-Break-1.wav";
        String ab2 = startURL + "Amen-Break-2.wav";
		String ab3 = startURL + "Amen-Break-3.wav";
        String ab4 = startURL + "Amen-Break-4.wav";


        try {
            AudioInputStream clip1 = AudioSystem.getAudioInputStream(new File(ab4));
            AudioInputStream clip2 = AudioSystem.getAudioInputStream(new File(ab3));

            AudioInputStream appendedFiles = 
                            new AudioInputStream(
                                new SequenceInputStream(clip1, clip2),     
                                clip1.getFormat(), 
                                clip1.getFrameLength() + clip2.getFrameLength());

            AudioSystem.write(appendedFiles, 
                            AudioFileFormat.Type.WAVE, 
                            new File(startURL + "Amen-Break-4-3.wav"));
        } catch (Exception e) {
            e.printStackTrace();
        }
		
	}
}
