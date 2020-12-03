package com.litkaps.stickman.mediaviewer;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.litkaps.stickman.R;

public class ImageViewActivity extends AppCompatActivity {

    private static final String TAG = "ImageViewActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_image);


        Intent callingIntent = this.getIntent();
        if(callingIntent != null) {
            Uri imageUri = callingIntent.getData();

            ImageView imgView = findViewById(R.id.imageView);
            imgView.setImageURI(imageUri);
        }
    }
}
