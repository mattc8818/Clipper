package onetothefour;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.net.MalformedURLException;
import java.net.URL;

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
	private URL sourceURL;
	private URL destURL;
	
	private String sourceURLStr;
	private String destURLStr; 
	private String clipperName;
	
	/* Size of 12 String array that stores String filepaths for clipped WAVs.
	 * 
	 * clippingsURLStr[0 - 7] correspond to e1 - e8 WAV files.
	 * clippingsURLStr[8-11] correspond to q1 - q4 WAV files.
	 */
	private String[] clippingsURLStr;
	
	private int lengthMS; 
	
	public static Mixer mixer;
	public static Clip clip;
	
	public Clipper(String clipperName, String sourceURLStr, String destURLStr) {
		this.clipperName = clipperName;
		this.sourceURLStr = sourceURLStr;
		this.destURLStr = destURLStr;
		
		try {
			sourceURL = new URL("file:///"+sourceURLStr);
			destURL = new URL("file:///"+destURLStr);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		lengthMS = getLengthMS(sourceURL);

		// Initializes split clips of the file, and recieves their respective filepath.
		clippingsURLStr = initSplitFiles();
	}
	
	/*
	 * From the input file, a folder with WAV clippings is generated.
	 * 
	 *  Outputs a String array with the URL Strings of the resulting files.
	 */
	public String[] initSplitFiles() {
		String[] clippingsFilesURLStr = new String[12];
		
		String clipperFolderFilePath = destURLStr+clipperName+"Clips/";
		
		boolean success = (new File(clipperFolderFilePath)).mkdirs();
		if (!success) {
			System.out.println("Error creating folder for split points");
		}

		
		int fileLenEighth = lengthMS / 8;
		
		int[] startPts = new int[8];
		
		for (int x = 0; x < 8; x++) {
			startPts[x] = fileLenEighth * x;
		}
		
		String newPathTemp = "";
		// Splits file into 8 parts.
		for (int x = 0; x < 8; x++) {
			newPathTemp = clipperFolderFilePath+clipperName+"-e"+(x+1)+".wav";
			
			clippingsFilesURLStr[x] = newPathTemp;
			copyAudioMs(sourceURLStr, newPathTemp, startPts[x], fileLenEighth);
		}
		
		
		// Splits file into 4 parts.
		for (int x = 0; x < 8; x= x + 2) {
			newPathTemp = clipperFolderFilePath+clipperName+"-q"+((x/2) + 1)+".wav";
			
			clippingsFilesURLStr[8 + (x/2)] = newPathTemp;
			copyAudioMs(sourceURLStr, newPathTemp, startPts[x], fileLenEighth*2);
		}
		
		return clippingsFilesURLStr;
	}
	
	public int getLengthMS(URL sourceURL) {
		Mixer.Info[] mixInfos = AudioSystem.getMixerInfo();
		
		mixer = AudioSystem.getMixer(mixInfos[0]);
		
		DataLine.Info dataInfo = new DataLine.Info(Clip.class, null);
		try { clip = (Clip)mixer.getLine(dataInfo); }
		catch (LineUnavailableException lue) { lue.printStackTrace(); }
		
		try {
			AudioInputStream ais = AudioSystem.getAudioInputStream(sourceURL);
			clip.open(ais);
		}
		catch (IOException io) { io.printStackTrace(); } 
		catch (UnsupportedAudioFileException e) { e.printStackTrace(); } 
		catch (LineUnavailableException e) { e.printStackTrace(); }
		
		int lengthMS = (int)(clip.getMicrosecondLength()*0.001);

		return lengthMS;
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
