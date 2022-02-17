package com.example.tiktok;

import android.util.Log;

import java.io.*;
import java.util.*;

public class ActionsForPublisher extends Thread {

    private ObjectOutputStream objOS;
    private VideoFile videoFile;
    private ChannelName channelName;
    private String requestType;
    private String topic;
    private String consumer;

    ActionsForPublisher(ObjectOutputStream objOS, VideoFile videofile, ChannelName channelName, String requestType,
                        String topic, String consumer) {
        this.objOS = objOS;
        this.videoFile = videofile;
        this.channelName = channelName;
        this.requestType = requestType;
        this.topic = topic;
        this.consumer = consumer;
    }

    public void run() {

        try {
            Message message;
            if (requestType.equals("NV")) {
                message = new Message('P', this.channelName.getChannelName(), "NV"); // for when a publisher publishes a
                // NEW video

                this.objOS.writeObject(message);
                this.objOS.flush();
                Log.d("DEBUG:",
                        ">Publisher->" + this.channelName.getChannelName() + ": Message sent from publisher: NV");

                this.objOS.writeObject(this.videoFile.getAssociatedHashTags());
                this.objOS.flush();

                ArrayList<VideoFileChunk> vfcl = videoFile.getChunks();
                Log.d("DEBUG:",">Publisher->" + this.channelName.getChannelName() + ": Messages to send:");

                for (VideoFileChunk vfc : vfcl) {
                    Log.d("DEBUG:",">Publisher " + vfc.getorder());
                    this.objOS.writeObject(vfc);
                    this.objOS.flush();
                }
            } else {

                ArrayList<String> videos;

                if (this.channelName.getChannelName().equals(topic)) {
                    videos = new ArrayList();
                    Set<String> temp_videos = this.channelName.getAllVideoNames();
                    videos.addAll(temp_videos);
                } else {
                    videos = this.channelName.getVideoWithSpecificHashTag(topic);
                }

                Log.d("DEBUG:",">Publisher has videos:");
                for (String str : videos) {
                    Log.d("DEBUG:","VideoName: " + str);
                }

                Message nmessage = new Message('P', this.channelName.getChannelName(), "SRVFC");
                this.objOS.writeObject(nmessage);
                this.objOS.flush();

                this.objOS.writeUTF(this.consumer);
                this.objOS.flush();

                this.objOS.writeInt(videos.size());
                this.objOS.flush();
                Log.d("DEBUG:",">Publisher sends number of videos: " + videos.size());

                for (String videoName : videos) {
                    ArrayList<VideoFileChunk> chunks = this.channelName.getVideo(videoName).getChunks();

                    for (VideoFileChunk vfc : chunks) {
                        Log.d("DEBUG:",">Publisher sends video with orderID " + vfc.getorder());
                        this.objOS.writeObject(vfc);
                        this.objOS.flush();
                    }
                }
            }

        } catch (IOException io) {
            io.printStackTrace();
        }
    }
}
