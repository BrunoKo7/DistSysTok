package Main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class ChannelName{

    private String channelName;
	private ArrayList<String> hashTagsPublished;
	private HashMap<String, VideoFile> userVideoFilesMap;

	public ChannelName(String name){
        this.channelName = name;
        this.userVideoFilesMap = new HashMap<>();
    }
    
    public String getChannelName() {
        return this.channelName;
    }

    public VideoFile getVideo(String nameOfVideo){
        return this.userVideoFilesMap.get(nameOfVideo);
    }

    public ArrayList<String> getHashTags() {
        return this.hashTagsPublished;
    }

    public void addHashTag(String nHashTag){
        if (!this.hashTagsPublished.contains(nHashTag)){
            this.hashTagsPublished.add(nHashTag);
        }
    }
    
    public void removeHashTag(String rHashTag){
        this.hashTagsPublished.remove(rHashTag);
    }

    public void addVideo(String nameOfVideo, VideoFile videoFile){
        this.userVideoFilesMap.put(nameOfVideo, videoFile);
    }

    //TODO 
    //to be CHECKED
    public void removeVideo(String nameOfVideo){
        if(this.userVideoFilesMap.containsKey(nameOfVideo)){
        	for(String hashTag : this.userVideoFilesMap.get(nameOfVideo).getAssociatedHashTags()) {
        		removeHashTag(hashTag);
        	}
            this.userVideoFilesMap.remove(nameOfVideo);
            updateHashTagList(); // If two videos have the same hashtag, after deleting one video the hashtag should be still in the list
        }
    }

  //to be CHECKED
    private void updateHashTagList() {
		for(VideoFile videoFile : this.userVideoFilesMap.values()) {
			for(String hashTag : videoFile.getAssociatedHashTags()) {
				if(!this.hashTagsPublished.contains(hashTag)) {
					this.hashTagsPublished.add(hashTag);
				}
			}
		}
	}
	
	public ArrayList<String> getVideoWithSpecificHashTag(String topic){
		ArrayList<String> videoFileNames = new ArrayList<>();
		
		for (String videoName : this.userVideoFilesMap.keySet()) {
			if (this.userVideoFilesMap.get(videoName).getAssociatedHashTags().contains(topic)) {
				videoFileNames.add(videoName);
			}
		}
		return videoFileNames;
	}
	
	public Set<String> getAllVideoNames(){
		return this.userVideoFilesMap.keySet();
	}
}