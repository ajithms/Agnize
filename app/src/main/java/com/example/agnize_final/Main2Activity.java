package com.example.agnize_final;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.method.ScrollingMovementMethod;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionCloudTextRecognizerOptions;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class Main2Activity extends AppCompatActivity {
    ImageView img;
    Button add,detect,save,copy;
    TextView txtview,prog;
    Uri uri;
    Bitmap photo = null;
    String txt;
    public static final int MULTIPLE_PERMISSIONS = 123;
    String[] permissions = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        img = (ImageView)findViewById(R.id.img);
        add = (Button)findViewById(R.id.add);
        detect = (Button)findViewById(R.id.detect);
        save = (Button)findViewById(R.id.save);
        copy = (Button)findViewById(R.id.copy);
        txtview = (TextView)findViewById(R.id.txtview);
        prog = (TextView)findViewById(R.id.prog);
        txtview.setMovementMethod(new ScrollingMovementMethod());
        checkPermissions();

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Image load n crop
                add.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        CropImage.startPickImageActivity(Main2Activity.this);
                    }
                });
            }
        });

        //Save file
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermissions();
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(Main2Activity.this, R.style.AppThemeAlert);
                alertDialog.setMessage("Enter File Name");

                final EditText input = new EditText(Main2Activity.this);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
                input.setLayoutParams(lp);
                alertDialog.setView(input);
                alertDialog.setPositiveButton("save",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                String file_name = input.getText().toString();
                                if (!txtview.getText().toString().isEmpty()) {
                                    String root = Environment.getExternalStorageDirectory().toString();
                                    File file = new File(root+"/Agnize_Text_Recognition");
                                    if (!file.exists()) {
                                        file.mkdir();
                                    }
                                    try {
                                        if(!file_name.isEmpty()) {
                                            File gpxfile = new File(file, file_name + ".txt");
                                            FileWriter writer = new FileWriter(gpxfile);
                                            writer.append(txtview.getText().toString());
                                            writer.flush();
                                            writer.close();
                                            Toast.makeText(Main2Activity.this, "File saved:"+file+"/"+file_name+".txt", Toast.LENGTH_LONG).show();
                                        }
                                        } catch (Exception e) {
                                    }
                                }


                            }
                        });

                alertDialog.show();

            }
        });

        //Copy to clipboard
        copy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                android.content.ClipboardManager clipboardManager = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                android.content.ClipData clipData = android.content.ClipData.newPlainText("Text Label", txtview.getText().toString());
                clipboardManager.setPrimaryClip(clipData);
                Toast.makeText(getApplicationContext(), "Text copied!", Toast.LENGTH_SHORT).show();
            }
        });
        //Detect text
        detect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                img.setVisibility(View.GONE);
                prog.setVisibility(View.VISIBLE);
                FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(photo);
                FirebaseVisionCloudTextRecognizerOptions options = new FirebaseVisionCloudTextRecognizerOptions.Builder().setLanguageHints(Arrays.asList("en", "kn")).build();
                FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance()
                        .getCloudTextRecognizer(options);

                detector.processImage(image)
                        .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                            @Override
                            public void onSuccess(FirebaseVisionText result) {
                                // Task completed successfully
                                txt = "";
                                for (FirebaseVisionText.TextBlock block : result.getTextBlocks()) {
                                    txt += block.getText();
                                    txtview.setText(txt);
                                }
                                prog.setVisibility(View.GONE);
                                txtview.setVisibility(View.VISIBLE);
                                save.setVisibility(View.VISIBLE);
                                copy.setVisibility(View.VISIBLE);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                prog.setVisibility(View.GONE);
                                txtview.setVisibility(View.GONE);
                                Toast.makeText(getApplicationContext(), "Connection Failure!!", Toast.LENGTH_LONG).show();
                            }
                        });
            }
        });
    }

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE
                && resultCode == Activity.RESULT_OK) {
            Uri imageUri = CropImage.getPickImageResultUri(this, data);
            if (CropImage.isReadExternalStoragePermissionsRequired(this, imageUri)) {
                uri = imageUri;
                requestPermissions(new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},0);
            } else {
                startCrop(imageUri);
            }
        }

        if( requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri temp = result.getUri();
                //img.setImageURI(temp);
                txtview.setVisibility(View.GONE);
                save.setVisibility(View.INVISIBLE);
                copy.setVisibility(View.INVISIBLE);
                img.setVisibility(View.VISIBLE);
                try {
                    photo = MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.parse(String.valueOf(temp)));
                } catch (IOException e) {
                    e.printStackTrace();
                }
              //  det = new Detect(photo);
                img.setImageBitmap(photo);
               detect.setVisibility(View.VISIBLE);
            }

        }
    }

    private void startCrop(Uri imageuri) {
        CropImage.activity(imageuri)
                .setGuidelines(CropImageView.Guidelines.ON)
                .setMultiTouchEnabled(true)
                .start(this);
    }

    //Request permission
    private boolean checkPermissions() {
        int result;
        List<String> listPermissionsNeeded = new ArrayList<>();

        for (String p : permissions) {
            result = ContextCompat.checkSelfPermission(getApplicationContext(), p);
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);
            }
        }

        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MULTIPLE_PERMISSIONS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    String perStr = "";
                    for (String per : permissions) {
                        perStr += "\n" + per;
                    }   // permissions list of don't granted permission
                }
                return;
            }
        }
    }

}
