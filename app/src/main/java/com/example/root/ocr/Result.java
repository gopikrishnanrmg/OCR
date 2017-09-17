package com.example.root.ocr;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.TextView;

public class Result extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        TextView textView = (TextView) findViewById(R.id.textView2);
        textView.setMovementMethod(new ScrollingMovementMethod());
        String s = getIntent().getStringExtra("key");
        String desiredString = s.substring(4, s.length());
        textView.setText(desiredString);

    }

}
