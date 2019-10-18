package com.example.chisu.myapplication;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

public class CapturedActivity extends AppCompatActivity {

    public static ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_captured);

//        imageView = findViewById(R.id.captured);
//        imageView.setImageBitmap(ARCamera.bm);
    }
}
