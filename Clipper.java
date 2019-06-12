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

	/*
	 * Size of 12 String array that stores String filepaths for clipped WAVs.
	 * 
	 * clippingsURLStr[0 - 7] correspond to e1 - e8 WAV files. clippingsURLStr[8-11]
	 * correspond to q1 - q4 WAV files.
	 */
	private String[] clippingsURLStr;

	private int lengthMS;

	public static Mixer mixer;
	public static Clip clip;

	private AudioInputStream[] eighthsOfClip;
	private AudioInputStream[] quartersOfClip;

	public Clipper(String clipperName, String sourceURLStr, String destURLStr) {
		this.clipperName = clipperName;
		this.sourceURLStr = sourceURLStr;
		this.destURLStr = destURLStr;

		try {
			sourceURL = new URL("file:///" + sourceURLStr);
			destURL = new URL("file:///" + destURLStr);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		lengthMS = getLengthMS(sourceURL);

		eighthsOfClip = initEighthsAIS();
		quartersOfClip = initQuartersAIS();

		// Initializes split clips of the file, and receives their respective filepath.
		// clippingsURLStr = initSplitFiles();
	}

	/*
	 * From the input file, a folder with WAV clippings is generated.
	 * 
	 * Outputs a String array with the URL Strings of the resulting files.
	 */
	public String[] createSplitFiles() {
		String[] clippingsFilesURLStr = new String[12];

		String clipperFolderFilePath = destURLStr + clipperName + "-Clips/";
		String stitchesFolderFilePath = destURLStr + clipperName + "-Stitches/";

		boolean success = (new File(clipperFolderFilePath)).mkdirs();
		success = (new File(stitchesFolderFilePath)).mkdirs();
		if (!success) {
			System.out.println("Possible problems with your generated folders");
		}

		int fileLenEighth = lengthMS / 8;

		int[] startPts = new int[8];

		for (int x = 0; x < 8; x++) {
			startPts[x] = fileLenEighth * x;
		}

		String newPathTemp = "";
		// Splits file into 8 parts.
		for (int x = 0; x < 8; x++) {
			newPathTemp = clipperFolderFilePath + clipperName + "-e" + (x + 1) + ".wav";

			clippingsFilesURLStr[x] = newPathTemp;
			copyAudioMs(sourceURLStr, newPathTemp, startPts[x], fileLenEighth);
		}

		// Splits file into 4 parts.
		for (int x = 0; x < 8; x = x + 2) {
			newPathTemp = clipperFolderFilePath + clipperName + "-q" + ((x / 2) + 1) + ".wav";

			clippingsFilesURLStr[8 + (x / 2)] = newPathTemp;
			copyAudioMs(sourceURLStr, newPathTemp, startPts[x], fileLenEighth * 2);
		}

		return clippingsFilesURLStr;
	}

	
	/*
	 * This method initializes the AudioInputStreams of eight pieces of the file, so 
	 * that they may be sequenced later.
	 */
	public AudioInputStream[] initEighthsAIS() {
		AudioInputStream[] eighthsClips = new AudioInputStream[8];

		int eClipLen = (lengthMS / 8);

		for (int x = 0; x < eighthsClips.length; x++) {
			eighthsClips[x] = Clipper.shortenedStream(sourceURLStr, eClipLen * x, eClipLen);
		}

		return eighthsClips;
	}
	
	/*
	 * This method initializes the AISs of four pieces of the file, for sequencing.
	 */
	public AudioInputStream[] initQuartersAIS() {
		AudioInputStream[] quartersClips = new AudioInputStream[8];

		int qClipLen = (lengthMS / 4);

		for (int x = 0; x < quartersClips.length; x++) {
			quartersClips[x] = Clipper.shortenedStream(sourceURLStr, qClipLen * x, qClipLen);
		}

		return quartersClips;
	}

	/*
	 * Outputs the ms of the WAV file.
	 */
	public static int getLengthMS(URL sourceURL) {
		Mixer.Info[] mixInfos = AudioSystem.getMixerInfo();

		mixer = AudioSystem.getMixer(mixInfos[0]);

		DataLine.Info dataInfo = new DataLine.Info(Clip.class, null);
		try {
			clip = (Clip) mixer.getLine(dataInfo);
		} catch (LineUnavailableException lue) {
			lue.printStackTrace();
		}

		try {
			AudioInputStream ais = AudioSystem.getAudioInputStream(sourceURL);
			clip.open(ais);
		} catch (IOException io) {
			io.printStackTrace();
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		}
		int lengthMS = (int) (clip.getMicrosecondLength() * 0.001);

		return lengthMS;
	}


	/*
	 * Method creates an audio file based on a clip of another, trims by
	 * microseconds to minimize trimming error.
	 * 
	 * Code from: https://tinyurl.com/y6h9qloa
	 */
	public static void copyAudioMs(String sourceFileName, String destinationFileName, int startMsecs, int msecsToCopy) {
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
			int bytesPerMsecond = (int) (format.getFrameSize() * format.getFrameRate() * 0.001);

			// Skips to that point of the file. ADJUST HERE
			inputStream.skip(startMsecs * bytesPerMsecond);

			// Finds the end point to end reading bytes. ADJUST HERE
			long framesOfAudioToCopy = (int) (msecsToCopy * format.getFrameRate() * 0.001);

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
	 * This method returns an AudioInputStream that holds a selection of a given WAV file.
	 */
	public static AudioInputStream shortenedStream(String sourceFileName, int startMsecs, int msecsToCopy) {
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
			int bytesPerMsecond = (int) (format.getFrameSize() * format.getFrameRate() * 0.001);

			// Skips to that point of the file. ADJUST HERE
			inputStream.skip(startMsecs * bytesPerMsecond);

			// Finds the end point to end reading bytes. ADJUST HERE
			long framesOfAudioToCopy = (int) (msecsToCopy * format.getFrameRate() * 0.001);

			// Initializes the shortened stream of the clip.
			shortenedStream = new AudioInputStream(inputStream, format, framesOfAudioToCopy);

		} catch (Exception e) {
			System.out.println(e);
		}
		return shortenedStream;
	}
	
	/*
	 * Stitch takes a sequence, which is an array of strings, and plays it back.
	 * If the parameter boolean recordStitch is true, the sequence will output a 
	 * WAV file of the sequence.
	 */
	public AudioInputStream stitch(String[] sequence, boolean recordStitch) {
		AudioInputStream appendedFiles = null;
		
		if (sequence.length < 2) {
			System.out.println("Error: at least two sequences are needed to stitch.");
			return appendedFiles;
		}
		// Sequences with a length greater than 12 are not allowed.
		if (sequence.length > 12) {
			System.out.println("Error: sequence too long, could not stitch.");
			return appendedFiles;
		}

		// Initializes the new stitche's string URL name.
		String newStitchURLStr = destURLStr + clipperName + "-Stitches/";
		for (int x = 0; x < sequence.length; x++) {
			newStitchURLStr += sequence[x];
		}
		newStitchURLStr += ".wav";

		// Initial stitch of the first and second clip.
		try {
			AudioInputStream clip1 = getClip(sequence[0]);
			AudioInputStream clip2 = getClip(sequence[1]);

			appendedFiles = new AudioInputStream(new SequenceInputStream(clip1, clip2),
					clip1.getFormat(), clip1.getFrameLength() + clip2.getFrameLength());
			
			// Appends additional sequenced clips, if necessary. 
			for (int n = 2; n < sequence.length; n++) {
				AudioInputStream newClip = getClip(sequence[n]);
				
				appendedFiles = new AudioInputStream(new SequenceInputStream(appendedFiles, newClip),
						appendedFiles.getFormat(), appendedFiles.getFrameLength() + newClip.getFrameLength());
				System.out.println(n);
			}
			// If recordStitch is true, a file is generated for that sequence.
			if (recordStitch) {
				AudioSystem.write(appendedFiles, AudioFileFormat.Type.WAVE, new File(newStitchURLStr));
				System.out.println("Sequence written to file:");
				System.out.println(newStitchURLStr);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return appendedFiles;
	}

	public AudioInputStream getClip(String clipID) {
		AudioInputStream clip = null;
		switch (clipID) {
		case "e1":
			clip = eighthsOfClip[0];
			break;
		case "e2":
			clip = eighthsOfClip[1];
			break;
		case "e3":
			clip = eighthsOfClip[2];
			break;
		case "e4":
			clip = eighthsOfClip[3];
			break;
		case "e5":
			clip = eighthsOfClip[4];
			break;
		case "e6":
			clip = eighthsOfClip[5];
			break;
		case "e7":
			clip = eighthsOfClip[6];
			break;
		case "e8":
			clip = eighthsOfClip[7];
			break;
		case "q1":
			clip = quartersOfClip[0];
			break;
		case "q2":
			clip = quartersOfClip[1];
			break;
		case "q3":
			clip = quartersOfClip[2];
			break;
		case "q4":
			clip = quartersOfClip[3];
			break;
		default:
			System.out.println("Error: invalid input clip ID " + clipID);
			break;
		}
		return clip;
	}
}
