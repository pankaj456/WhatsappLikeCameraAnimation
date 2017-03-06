package com.snapown.android.camera;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import com.snapown.android.R;

public class PreviewActivity extends AppCompatActivity
{
private ImageView imageView;
    private ImageBean imageBean;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);
        imageView = (ImageView)findViewById(R.id.image);




    }
}
