package com.example.tiktok;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class SecondActivity extends AppCompatActivity {

    Button btnFollow;
    Button btnPublish;
    Button btnSrch;
    Button btnChoosePub;
    Button showReceivedVideos;
    Button goTV;
    EditText topicEditText;
    TextView appBanner;
    VideoView vv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        btnFollow = findViewById(R.id.buttonFollow);
        btnPublish = findViewById(R.id.buttonPublish);
        btnSrch = findViewById(R.id.buttonSearch);
        topicEditText = findViewById(R.id.inputTopic);
        btnChoosePub = findViewById(R.id.btnChoosePub);
        goTV = findViewById(R.id.goBtn);
        vv = findViewById(R.id.videoView);
        showReceivedVideos = findViewById(R.id.buttonShowReceivedVideo);
        appBanner = findViewById(R.id.textViewBanner);
        appBanner.setText(AppNode.getAppNode().getPublisher().getChannelName().getChannelName());
    }

    @Override
    protected void onStart() {
        super.onStart();

        Intent intent = getIntent();

        Log.d("DEBUG", AppNode.getAppNode().getPublisher().getChannelName().getChannelName());

        btnFollow.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                topicEditText.setVisibility(View.VISIBLE);
                goTV.setVisibility(View.VISIBLE);

                goTV.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        String topicToSearch = topicEditText.getEditableText().toString();
                        AppNode.getAppNode().getConsumer().follow(topicToSearch);
                        Toast.makeText(SecondActivity.this, topicToSearch, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        btnSrch.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                topicEditText.setVisibility(View.VISIBLE);
                goTV.setVisibility(View.VISIBLE);

                goTV.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        String topicToSearch = topicEditText.getEditableText().toString();
                        AppNode.getAppNode().getConsumer().requestATopic(topicToSearch);
                    }
                });
            }
        });

        btnChoosePub.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                showPublisherList();
            }
        });

        btnPublish.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), PublishVideoActivity.class);
                startActivity(intent);
            }
        });

        showReceivedVideos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showReceivedVideosList();
            }
        });

        vv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vv.start();
            }
        });
    }

    public void showPublisherList() {
        AlertDialog.Builder publisherMenu = new AlertDialog.Builder(this);
        String[] following = (AppNode.getAppNode().getConsumer().getPublishersFollowing()).toArray(new String[0]);

        publisherMenu.setTitle("Choose Publisher!");

        publisherMenu.setItems(following,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AppNode.getAppNode().getConsumer().requestATopic(following[which]);
                    }
                });
        publisherMenu.show();
    }

    public void showReceivedVideosList() {
        AlertDialog.Builder receivedVideosMenu = new AlertDialog.Builder(this);
        ArrayList<String> receivedVideoNames = new ArrayList();

        for(ArrayList<VideoFileChunk> arrList : AppNode.getAppNode().getConsumer().getReceivedVideos()) {
            receivedVideoNames.add(arrList.get(0).getvideoName());
        }

        String[] receivedVideos = receivedVideoNames.toArray(new String[0]);
        receivedVideosMenu.setTitle("Choose Video to play");

        receivedVideosMenu.setItems(receivedVideos,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            showVideoinVideoView(AppNode.getAppNode().getConsumer().getReceivedVideos().get(which));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
        receivedVideosMenu.show();
    }

    public void showVideoinVideoView(ArrayList<VideoFileChunk> chunks) throws IOException {
        File file = File.createTempFile("tempFile", ".mp4");
        file.deleteOnExit();
        try (FileOutputStream stream = new FileOutputStream(file)) {
            for (VideoFileChunk chunk : chunks) {
                stream.write(chunk.getChunk());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        vv.setVisibility(View.VISIBLE);
        vv.setVideoURI(Uri.fromFile(file));
        vv.requestFocus();
        vv.start();

        Log.d("DEBUG:", ">Consumer: Message displayed on Consumer");
    }


}