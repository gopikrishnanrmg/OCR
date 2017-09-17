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
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;


public class MainActivity extends AppCompatActivity {
    Button button, button1,button2;
    ImageView imageView;
    Intent intent,intent1,intent2;
    int RESULT_LOAD_IMAGE = 1,CROP_PIC_REQUEST_CODE=2,CAMERA_REQUEST=3;
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
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},1);
        }
        button = (Button) findViewById(R.id.button);
        button2 = (Button) findViewById(R.id.button3);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, RESULT_LOAD_IMAGE);
            }
        });

        button2.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View v) {
                intent2 = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent2,CAMERA_REQUEST);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        imageView = (ImageView) findViewById(R.id.imageView);
        button1 = (Button) findViewById(R.id.button2);
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();
            imageView.setImageBitmap(BitmapFactory.decodeFile(picturePath));
            bitmap = BitmapFactory.decodeFile(picturePath);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        Intent cropIntent = new Intent("com.android.camera.action.CROP");

                        cropIntent.setDataAndType(selectedImage, "image/*");
                        cropIntent.putExtra("crop", "true");
                        cropIntent.putExtra("aspectX", 2);
                        cropIntent.putExtra("aspectY", 3);
                        cropIntent.putExtra("outputX", 200);
                        cropIntent.putExtra("outputY", 300);
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
            super.onActivityResult(requestCode, resultCode, data);

        }

        if (requestCode == CROP_PIC_REQUEST_CODE && resultCode == RESULT_OK && data!=null) {
            try{
                    Bundle extras = data.getExtras();
                    if (extras != null) {
                        bitmap = extras.getParcelable("data");
                        imageView.setImageBitmap(bitmap);


                    }
            }catch (Exception e){
                Toast.makeText(getApplicationContext(), (CharSequence) e,Toast.LENGTH_LONG).show();

            }
        }
        if(requestCode == CAMERA_REQUEST && resultCode == RESULT_OK && data.getExtras().get("data")!=null)
        {     try
               {
                bitmap = (Bitmap) data.getExtras().get("data");
                imageView.setImageBitmap(bitmap);
                }catch (Exception e){
                       Toast.makeText(getApplicationContext(), (CharSequence) e,Toast.LENGTH_LONG).show();

            }
        }
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkit();
            }
        });
    }

    public void checkit(){
          detectedText=null;
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
        Toast.makeText(getApplicationContext(), "done",Toast.LENGTH_LONG).show();

    if(detectedText!=null) {
        intent1 = new Intent(getApplicationContext(), Result.class);
        intent1.putExtra("key", detectedText);
        textRecognizer.release();
        startActivity(intent1);
    }
    }

}