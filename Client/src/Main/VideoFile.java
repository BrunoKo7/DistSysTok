package Main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.mp4.MP4Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.mp4parser.Box;
import org.mp4parser.IsoFile;
import org.mp4parser.boxes.iso14496.part12.MovieBox;
import org.xml.sax.SAXException;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;

public class VideoFile {

	private String videoName;
	private String dateCreated;
	private String length;
	private String frameRate;
	private String frameWidth;
	private String frameHeight;
	private ArrayList<String> associatedHashtags;
	private byte[] videoFileChunkInByte;
	private ArrayList<VideoFileChunk> chunks;
	private boolean readCorrectly;

	// Testing reasons.
	public static void main(String[] args) {
		VideoFile nvideoFile = new VideoFile("SampleVideo.mp4");
	}

	public VideoFile(String path) {
		this.associatedHashtags = new ArrayList<>();
		this.chunks = new ArrayList<>();

		File file = new File("src/Main/Videos/" + path);
		if (file.exists()) {
			this.readCorrectly = true;
			readFrameRate(file);
			readHashTags(file);
			readMetadataFromVideoFileWithTika(file);

			readFileToByteArray(file);

			// Testing reasons
			// transformByteArrayToVideo(file);

			splitByteArrayIntoChunksOfConstantBlockSize(524288); // 512 kB = 524.288 B = 2^19 B
			printObject();
		} else {
			this.readCorrectly = false;
		}

	}

	public static void saveVideoFileFromArrayList(ArrayList<VideoFileChunk> chunks, String path) {
		File file = new File("src/Main/Videos/" + path);

		try (FileOutputStream stream = new FileOutputStream(file)) {
			for (VideoFileChunk chunk : chunks) {
				stream.write(chunk.getChunk());
			}

			System.out.println("Successfully parsed back " + path);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void splitByteArrayIntoChunksOfConstantBlockSize(int blockSize) {
		int blockCount = (this.videoFileChunkInByte.length + blockSize - 1) / blockSize;
		byte[] range = null;

		for (int i = 1; i < blockCount; i++) {
			int idx = (i - 1) * blockSize;
			range = Arrays.copyOfRange(this.videoFileChunkInByte, idx, idx + blockSize);
			chunks.add(new VideoFileChunk(this.videoName, (i - 1), '1', range));
		}

		int end = this.videoFileChunkInByte.length;
		range = Arrays.copyOfRange(this.videoFileChunkInByte, (blockCount - 1) * blockSize, end);
		chunks.add(new VideoFileChunk(this.videoName, (blockCount - 1), '0', range));
	}

	private void transformByteArrayToVideo(File file) {
		try (FileOutputStream stream = new FileOutputStream(
				new File(file.getAbsolutePath().replace(".mp4", "ParsedBack.mp4")))) {
			stream.write(this.videoFileChunkInByte);
			System.out.println("Successfully parsed back");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void readFileToByteArray(File file) {
		try {
			this.videoFileChunkInByte = Files.readAllBytes(file.toPath());
			System.out.println("File to byte successfully");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void printObject() {
		System.out.println("videoName: " + this.videoName);
		System.out.println("length: " + this.length);
		System.out.println("frameHeight: " + this.frameHeight);
		System.out.println("frameWidth: " + this.frameWidth);
		System.out.println("frameRate: " + this.frameRate);
		System.out.println("dateCreated: " + this.dateCreated);

		System.out.print("associatedHashtags: ");
		for (String elem : associatedHashtags) {
			System.out.print(elem + " ");
		}
		System.out.println();

		System.out.println("Video File in chunks:");
		for (VideoFileChunk v : this.chunks) {
			System.out.println(v.getvideoName() + " " + v.getorder() + " " + v.getMC() + " " + v.getChunk().length);
		}
	}

	private void readFrameRate(File file) {
		try {
			com.drew.metadata.Metadata metadata = ImageMetadataReader.readMetadata(file);

			for (Directory d : metadata.getDirectories()) {
				// Framerate has static tag type 214
				// https://github.com/drewnoakes/metadata-extractor/blob/5773aa0e2877d5bafb091c6cbccb550ad2a58479/Source/com/drew/metadata/mp4/media/Mp4VideoDirectory.java
				if (d.getObject(214) != null) {
					this.frameRate = d.getObject(214) + "";
					return;
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (ImageProcessingException e) {
			// e.printStackTrace();
			this.frameRate = "";
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void readHashTags(File file) {
		FileChannel fc;
		try {
			fc = new FileInputStream(file).getChannel();
			IsoFile isoFile = new IsoFile(fc);
			MovieBox moov = isoFile.getMovieBox();

			for (Box b : moov.getBoxes()) {
				if (b.toString().contains("WM/Category=[string]")) {
					String[] arr = b.toString().split("WM\\/Category=\\[string\\]");
					for (int i = 0; i < arr.length; i++) {
						if (arr[i].charAt(0) == '#') {
							associatedHashtags.add(arr[i].split(";")[0]);
						}
					}
				}
			}
			fc.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void readMetadataFromVideoFileWithTika(File file) {
		BodyContentHandler handler = new BodyContentHandler();
		Metadata metadata = new Metadata();
		FileInputStream inputstream;
		try {
			inputstream = new FileInputStream(file);
			ParseContext pcontext = new ParseContext();

			// Html parser
			MP4Parser MP4Parser = new MP4Parser();
			MP4Parser.parse(inputstream, handler, metadata, pcontext);
			String[] metadataNames = metadata.names();

			this.videoName = metadata.get("title");
			this.length = metadata.get("xmpDM:duration");
			this.frameHeight = metadata.get("tiff:ImageLength");
			this.frameWidth = metadata.get("tiff:ImageWidth");
			this.dateCreated = metadata.get("meta:creation-date");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (TikaException e) {
			e.printStackTrace();
		}
	}

	public ArrayList<VideoFileChunk> getChunks() {
		return this.chunks;
	}

	public ArrayList<String> getAssociatedHashTags() {
		return this.associatedHashtags;
	}

	public String getVideoName() {
		return this.videoName;
	}

	public boolean readCorrectly() {
		return this.readCorrectly;
	}
}