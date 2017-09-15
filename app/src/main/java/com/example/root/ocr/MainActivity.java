package com.example.root.ocr;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;


public class MainActivity extends AppCompatActivity {
    Button button, button1;
    TextView textView;
    ImageView imageView;
    Intent intent;
    int RESULT_LOAD_IMAGE = 1,CROP_PIC_REQUEST_CODE=2;
    Bitmap bitmap;
    String detectedText;
    Uri selectedImage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
        button = (Button) findViewById(R.id.button);
        textView = (TextView) findViewById(R.id.textView);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textView.setText(" ");
                intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, RESULT_LOAD_IMAGE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();
            imageView = (ImageView) findViewById(R.id.imageView);
            imageView.setImageBitmap(BitmapFactory.decodeFile(picturePath));
            bitmap = BitmapFactory.decodeFile(picturePath);
            textView = (TextView) findViewById(R.id.textView);
            textView.setMovementMethod(new ScrollingMovementMethod());
            button1 = (Button) findViewById(R.id.button2);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        Intent cropIntent = new Intent("com.android.camera.action.CROP");

                        cropIntent.setDataAndType(selectedImage, "image/*");
                        cropIntent.putExtra("crop", "true");
                        cropIntent.putExtra("aspectX", 0);
                        cropIntent.putExtra("aspectY", 0);
                        cropIntent.putExtra("outputX", 200);
                        cropIntent.putExtra("outputY", 500);
                        cropIntent.putExtra("return-data", true);
                        startActivityForResult(cropIntent, CROP_PIC_REQUEST_CODE);
                    }
                    // respond to users whose devices do not support the crop action
                    catch (ActivityNotFoundException anfe) {
                        // display an error message
                        String errorMessage = "Whoops - your device doesn't support the crop action!";
                        Toast toast = Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT);
                        toast.show();
                    }
                }
            });

            button1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();
                    if (!textRecognizer.isOperational()) {
                        new AlertDialog.Builder(getApplicationContext())
                                .setMessage("Text recognizer could not be set up on your device :(").show();
                        return;
                    }
                    Frame frame = new Frame.Builder().setBitmap(bitmap).build();
                    SparseArray<TextBlock> text = textRecognizer.detect(frame);
                    for (int i = 0; i < text.size(); i++) {
                        TextBlock textBlock = text.valueAt(i);
                        if (textBlock != null && textBlock.getValue() != null) {
                            detectedText += textBlock.getValue();
                        }
                    }
                    textView.setText(detectedText);
                    detectedText = null;
                    String a = textView.getText().toString();
                    String desiredString = a.substring(4, a.length());
                    textView.setText(desiredString);
                    textRecognizer.release();
                }
            });

            super.onActivityResult(requestCode, resultCode, data);

        }if (requestCode == CROP_PIC_REQUEST_CODE && resultCode == RESULT_OK && data!=null) {
            try{
                    Bundle extras = data.getExtras();
                    if (extras != null) {
                        bitmap = extras.getParcelable("data");
                        imageView.setImageBitmap(bitmap);
                        button1.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();
                                if (!textRecognizer.isOperational()) {
                                    new AlertDialog.Builder(getApplicationContext())
                                            .setMessage("Text recognizer could not be set up on your device :(").show();
                                    return;
                                }
                                Frame frame = new Frame.Builder().setBitmap(bitmap).build();
                                SparseArray<TextBlock> text = textRecognizer.detect(frame);
                                for (int i = 0; i < text.size(); i++) {
                                    TextBlock textBlock = text.valueAt(i);
                                    if (textBlock != null && textBlock.getValue() != null) {
                                        detectedText += textBlock.getValue();
                                    }
                                }
                                if(detectedText!=null)
                                { textView.setText(detectedText);
                                detectedText = null;
                                String a = textView.getText().toString();
                                String desiredString = a.substring(4, a.length());
                                textView.setText(desiredString);
                                textRecognizer.release();}
                            }
                        });

                    }
            }catch (Exception e){
                Toast.makeText(getApplicationContext(), (CharSequence) e,Toast.LENGTH_LONG).show();

            }
        }

    }


}