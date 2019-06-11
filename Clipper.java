package onetothefour;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.UnsupportedAudioFileException;

public class Clipper {
	private int frameLength;
	private long usLength;

	/*
	 * clipPoints[0] = beginning of the clip clipPoints[1] = 1/4 of the length of
	 * the clip clipPoints[2] = 1/2 of the length of the clip clipPoints[3] = 3/4 of
	 * the length of the clip clipPoints[4] = full length of the clip
	 */
	private int[] clipPoints = new int[5];
	private Clip fullClip;
	private Clip[] dividedClip = new Clip[4];

	private AudioInputStream[] shortenedStream = new AudioInputStream[4];
	
	private String fileLocation;

	public Clipper(String fileLocation, long usLength) {
		this.fileLocation = fileLocation;
		this.usLength = usLength;
		
		initializeAIS(fileLocation, usLength);
	}
	
	public void initializeAIS(String sourceFileName, long usLength) {
		AudioInputStream inputStream = null;
		
		long qtrLenInSecs = (usLength / 4) * 1000000;
		
		try {
			// Reads an input file and it's file type.
			File file = new File(sourceFileName);
			AudioFileFormat fileFormat = AudioSystem.getAudioFileFormat(file);
			AudioFormat format = fileFormat.getFormat();

			// Initializes the input stream.
			inputStream = AudioSystem.getAudioInputStream(file);

			// Finds the skip point to begin reading bytes.
			int bytesPerSecond = format.getFrameSize() * (int) format.getFrameRate();
			// Finds the total number of copied frames.
			long framesOfAudioToCopy = qtrLenInSecs * (int) format.getFrameRate();
			
			for (int i = 0; i < 4; i++) {
				// Sets the inputStream to the next reading point.
				inputStream.skip(qtrLenInSecs * bytesPerSecond * i);
				// Initializes the shortened stream of the clip.
				shortenedStream[0] = new AudioInputStream(inputStream, format, framesOfAudioToCopy);
			}
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	
	public void playClip1() {
		Mixer.Info[] mixInfos = AudioSystem.getMixerInfo();
		Mixer mixer = AudioSystem.getMixer(mixInfos[0]);
		
		DataLine.Info dataInfo = new DataLine.Info(Clip.class, null);
		try { dividedClip[0] = (Clip)mixer.getLine(dataInfo); }
		catch (LineUnavailableException lue) { lue.printStackTrace(); }
		
		try { dividedClip[0].open(shortenedStream[0]); }
		catch (LineUnavailableException lue) { lue.printStackTrace(); } 
		catch (IOException e) { e.printStackTrace(); }
		
		dividedClip[0].start();
		
		do {
			try { Thread.sleep(dividedClip[0].getMicrosecondLength()); }
			catch (InterruptedException ie) { ie.printStackTrace(); }
		}
		while (dividedClip[0].isActive());

	}

	public void playClip2() {
		Mixer.Info[] mixInfos = AudioSystem.getMixerInfo();
		Mixer mixer = AudioSystem.getMixer(mixInfos[0]);
		
		DataLine.Info dataInfo = new DataLine.Info(Clip.class, null);
		try { dividedClip[1] = (Clip)mixer.getLine(dataInfo); }
		catch (LineUnavailableException lue) { lue.printStackTrace(); }
		
		try { dividedClip[1].open(shortenedStream[1]); }
		catch (LineUnavailableException lue) { lue.printStackTrace(); } 
		catch (IOException e) { e.printStackTrace(); }
		
		dividedClip[1].start();
		
		do {
			try { Thread.sleep(dividedClip[1].getMicrosecondLength()); }
			catch (InterruptedException ie) { ie.printStackTrace(); }
		}
		while (dividedClip[1].isActive());
	}
	
	public void playClip3() {
		Mixer.Info[] mixInfos = AudioSystem.getMixerInfo();
		Mixer mixer = AudioSystem.getMixer(mixInfos[0]);
		
		DataLine.Info dataInfo = new DataLine.Info(Clip.class, null);
		try { dividedClip[2] = (Clip)mixer.getLine(dataInfo); }
		catch (LineUnavailableException lue) { lue.printStackTrace(); }
		
		try { dividedClip[2].open(shortenedStream[2]); }
		catch (LineUnavailableException lue) { lue.printStackTrace(); } 
		catch (IOException e) { e.printStackTrace(); }
		
		dividedClip[2].start();
		
		do {
			try { Thread.sleep(dividedClip[2].getMicrosecondLength()); }
			catch (InterruptedException ie) { ie.printStackTrace(); }
		}
		while (dividedClip[2].isActive());
	}

	public void playClip4() {
		Mixer.Info[] mixInfos = AudioSystem.getMixerInfo();
		Mixer mixer = AudioSystem.getMixer(mixInfos[0]);
		
		DataLine.Info dataInfo = new DataLine.Info(Clip.class, null);
		try { dividedClip[3] = (Clip)mixer.getLine(dataInfo); }
		catch (LineUnavailableException lue) { lue.printStackTrace(); }
		
		try { dividedClip[3].open(shortenedStream[3]); }
		catch (LineUnavailableException lue) { lue.printStackTrace(); } 
		catch (IOException e) { e.printStackTrace(); }
		
		dividedClip[3].start();
		
		do {
			try { Thread.sleep(dividedClip[3].getMicrosecondLength()); }
			catch (InterruptedException ie) { ie.printStackTrace(); }
		}
		while (dividedClip[3].isActive());
	}
	
	public int getFrameLength() {
		return frameLength;
	}

	public int[] getClipPoints() {
		return clipPoints;
	}

	public Clip getFullClip() {
		return fullClip;
	}

	public Clip[] getDividedClip() {
		return dividedClip;
	}

	public void setFullClip(Clip newClip) {
		fullClip = newClip;
		frameLength = fullClip.getFrameLength();

		setClipPoints(frameLength);
	}

	/*
	 * setClipPoints() is private because it should not be altered outside of this
	 * class.
	 */
	private void setClipPoints(int frameLength) {
		clipPoints[0] = 0;
		for (int i = 1; i < 5; i++) {
			clipPoints[i] = clipPoints[i - 1] + (frameLength / 4);
		}
	}


	/*
	 * Method creates an audio file based on a clip of another.
	 * 
	 * Code from: https://tinyurl.com/y6h9qloa
	 */
	public static void copyAudio(String sourceFileName, String destinationFileName, int startSecond,
			int secondsToCopy) {
		AudioInputStream inputStream = null;
		AudioInputStream shortenedStream = null;
		try {
			// Reads an input file and it's file type.
			File file = new File(sourceFileName);
			AudioFileFormat fileFormat = AudioSystem.getAudioFileFormat(file);
			AudioFormat format = fileFormat.getFormat();

			// Initializes the input stream.
			inputStream = AudioSystem.getAudioInputStream(file);

			// Finds the bytes per second, used to begin reading bytes.
			int bytesPerSecond = format.getFrameSize() * (int) format.getFrameRate();

			// Skips to that point of the file.
			inputStream.skip(startSecond * bytesPerSecond);

			// Finds the end point to end reading bytes.
			long framesOfAudioToCopy = secondsToCopy * (int) format.getFrameRate();
			System.out.println("frames to copy: " + framesOfAudioToCopy);

			// Initializes the shortened stream of the clip.
			shortenedStream = new AudioInputStream(inputStream, format, framesOfAudioToCopy);

			File destinationFile = new File(destinationFileName);
			AudioSystem.write(shortenedStream, fileFormat.getType(), destinationFile);
		} catch (Exception e) {
			System.out.println(e);
		} finally {
			if (inputStream != null)
				try {
					inputStream.close();
				} catch (Exception e) {
					System.out.println(e);
				}
			if (shortenedStream != null)
				try {
					shortenedStream.close();
				} catch (Exception e) {
					System.out.println(e);
				}
		}
	}
	
	/*
	 * Method creates an audio file based on a clip of another, trims by microseconds to minimize
	 * trimming error.
	 * 
	 * Code from: https://tinyurl.com/y6h9qloa
	 */
	public static void copyAudioMs(String sourceFileName, String destinationFileName, int startMsecs,
			int msecsToCopy) {
		AudioInputStream inputStream = null;
		AudioInputStream shortenedStream = null;
		try {
			// Reads an input file and it's file type.
			File file = new File(sourceFileName);
			AudioFileFormat fileFormat = AudioSystem.getAudioFileFormat(file);
			AudioFormat format = fileFormat.getFormat();

			// Initializes the input stream.
			inputStream = AudioSystem.getAudioInputStream(file);

			// Finds the skip point to begin reading bytes.
			int bytesPerMsecond = (int)(format.getFrameSize() * format.getFrameRate() * 0.001);

			// Skips to that point of the file. ADJUST HERE
			inputStream.skip(startMsecs * bytesPerMsecond);

			// Finds the end point to end reading bytes. ADJUST HERE 
			long framesOfAudioToCopy = (int)(msecsToCopy * format.getFrameRate() * 0.001);

			// Initializes the shortened stream of the clip.
			shortenedStream = new AudioInputStream(inputStream, format, framesOfAudioToCopy);

			File destinationFile = new File(destinationFileName);
			AudioSystem.write(shortenedStream, fileFormat.getType(), destinationFile);
		} catch (Exception e) {
			System.out.println(e);
		} finally {
			if (inputStream != null)
				try {
					inputStream.close();
				} catch (Exception e) {
					System.out.println(e);
				}
			if (shortenedStream != null)
				try {
					shortenedStream.close();
				} catch (Exception e) {
					System.out.println(e);
				}
		}
	}
}
