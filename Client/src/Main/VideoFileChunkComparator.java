package Main;

import java.util.Comparator;

import org.apache.commons.lang3.builder.CompareToBuilder;

public class VideoFileChunkComparator implements Comparator<VideoFileChunk> {

	@Override
	public int compare(VideoFileChunk arg0, VideoFileChunk arg1) {
		return new CompareToBuilder()
				.append(arg0.getvideoName(), arg1.getvideoName())
				.append(arg0.getorder(), arg1.getorder())
				.toComparison();
	}
}