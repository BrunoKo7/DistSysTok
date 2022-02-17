package com.example.tiktok;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;

public class PublishVideoActivity extends AppCompatActivity {

    private MaterialButton chooseVideoButton;
    private static VideoView videoView;
    private MaterialButton addHashtagsButton;
    private MaterialButton updateVideonameButton;
    private MaterialButton publishVideoFinalButton;

    public static final String VIDEO_DIRECTORY = "/tiktok";
    private int GALLERY = 1, CAMERA = 2;

    private VideoFile videoFileToUpload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publish_video);

        chooseVideoButton = findViewById(R.id.chooseVideoButton);
        videoView = findViewById(R.id.vv);
        addHashtagsButton = findViewById(R.id.addHashtags);
        updateVideonameButton = findViewById(R.id.updateVideoName);
        publishVideoFinalButton = findViewById(R.id.publishVideoFinalButton);
    }

    @Override
    protected void onStart() {
        super.onStart();

        chooseVideoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChooseVideoDialog();
            }
        });

        addHashtagsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialogToUpdateHashtagsOrVideoname("Add Hashtags");
            }
        });

        updateVideonameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialogToUpdateHashtagsOrVideoname("Update Videoname");
            }
        });

        videoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                videoView.start();
            }
        });

        publishVideoFinalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppNode.getAppNode().getPublisher().push(getVideoFile());
                Toast.makeText(PublishVideoActivity.this, "Video published on Server.", Toast.LENGTH_SHORT).show();
            }
        });


    }

    public VideoFile getVideoFile(){
        return this.videoFileToUpload;
    }

    public void showDialogToUpdateHashtagsOrVideoname(String header) {
        String defaultText = "";

        if (header.equals("Add Hashtags")) {
            for (String hashTag : videoFileToUpload.getAssociatedHashTags()) {
                defaultText += hashTag + ";";
            }
        } else if (header.equals("Update Videoname")) {
            defaultText = videoFileToUpload.getVideoName();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(header);

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(defaultText);
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (header.equals("Add Hashtags")) {
                    String[] hashtagsArray = input.getText().toString().split(";");
                    ArrayList<String> hashtagsArrayList = new ArrayList<String>();
                    for (String hashTag : hashtagsArray) {
                        hashtagsArrayList.add(hashTag);
                    }
                    videoFileToUpload.setAssociatedHashtags(hashtagsArrayList);
                    Toast.makeText(PublishVideoActivity.this, "Hashtags added.", Toast.LENGTH_SHORT).show();
                } else if (header.equals("Update Videoname")) {
                    videoFileToUpload.updateVideoName(input.getText());
                    Toast.makeText(PublishVideoActivity.this, "Videoname updated.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void showChooseVideoDialog() {
        AlertDialog.Builder pictureDialog = new AlertDialog.Builder(this);
        pictureDialog.setTitle("Select type to choose Video!");
        String[] pictureDialogItems = {
                "Select video from gallery",
                "Record video from camera",
                "Cancel"};
        pictureDialog.setItems(pictureDialogItems,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                chooseVideoFromGallery();
                                break;
                            case 1:
                                takeVideoFromCamera();
                                break;
                            case 2:
                                dialog.dismiss();
                                break;
                        }
                    }
                });
        pictureDialog.show();
    }

    public void chooseVideoFromGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        galleryIntent.setType("video/*");
        startActivityForResult(galleryIntent, GALLERY);
    }

    private void takeVideoFromCamera() {
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        startActivityForResult(intent, CAMERA);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        Log.d("DEBUG:", "" + resultCode);
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == this.RESULT_CANCELED) {
            Log.d("DEBUG:", "cancelled");
            return;
        } else if (requestCode == GALLERY) {
            Log.d("DEBUG:", "gale");
            if (data != null) {
                Uri contentURI = data.getData();

                String selectedVideoPath = getPath(contentURI);
                Log.d("DEBUG:", selectedVideoPath);
                saveVideoToInternalStorage(selectedVideoPath);

                this.videoFileToUpload = new VideoFile(selectedVideoPath);
                this.updateVideonameButton.setEnabled(true);
                this.addHashtagsButton.setEnabled(true);
                this.publishVideoFinalButton.setEnabled(true);

                videoView.setVideoURI(contentURI);
                videoView.requestFocus();
                videoView.start();

            }

        } else if (requestCode == CAMERA) {
            Uri contentURI = data.getData();
            String recordedVideoPath = getPath(contentURI);
            Log.d("DEBUG:", recordedVideoPath);
            saveVideoToInternalStorage(recordedVideoPath);

            this.videoFileToUpload = new VideoFile(recordedVideoPath);
            this.updateVideonameButton.setEnabled(true);
            this.addHashtagsButton.setEnabled(true);
            this.publishVideoFinalButton.setEnabled(true);

            videoView.setVideoURI(contentURI);
            videoView.requestFocus();
            videoView.start();
        }
    }

    private void saveVideoToInternalStorage(String filePath) {

        File newfile;

        try {

            File currentFile = new File(filePath);
            File wallpaperDirectory = new File(Environment.getExternalStorageDirectory() + VIDEO_DIRECTORY);
            newfile = new File(wallpaperDirectory, Calendar.getInstance().getTimeInMillis() + ".mp4");

            if (!wallpaperDirectory.exists()) {
                wallpaperDirectory.mkdirs();
            }

            if (currentFile.exists()) {

                InputStream in = new FileInputStream(currentFile);
                OutputStream out = new FileOutputStream(newfile);

                // Copy the bits from instream to outstream
                byte[] buf = new byte[1024];
                int len;

                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                in.close();
                out.close();
                Log.d("DEBUG:", "Video file saved successfully.");
            } else {
                Log.d("DEBUG:", "Video saving failed. Source file missing.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public String getPath(Uri uri) {
        String[] projection = {MediaStore.Video.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            // HERE YOU WILL GET A NULLPOINTER IF CURSOR IS NULL
            // THIS CAN BE, IF YOU USED OI FILE MANAGER FOR PICKING THE MEDIA
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } else
            return null;
    }
}