package com.example.tiktok;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    Button btnLogin;
    EditText inputUsername;
    EditText inputIP;
    private AppNode appNode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnLogin = findViewById(R.id.btnLogin);
        inputUsername = findViewById(R.id.inputName);
        inputIP = findViewById(R.id.inputIP);
    }

    @Override
    protected void onStart(){
        super.onStart();

        btnLogin.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view){
                String channelName = inputUsername.getEditableText().toString();
                String localIp = inputIP.getEditableText().toString();

                if (channelName.equals("")) {
                    Toast.makeText(MainActivity.this, "No ChannelName inserted!", Toast.LENGTH_SHORT).show();
                }
                else if(localIp.equals("")){
                    Toast.makeText(MainActivity.this, "No IP inserted!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Connecting to Server: " + channelName, Toast.LENGTH_SHORT).show();

                    appNode = AppNode.getAppNode(channelName);
                    try {
                        appNode.connectPublisher(localIp, 4322);
                        appNode.connectConsumer(localIp, 4322);

                        Intent intent = new Intent(getApplicationContext(), SecondActivity.class);
                        startActivity(intent);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        });
    }
}