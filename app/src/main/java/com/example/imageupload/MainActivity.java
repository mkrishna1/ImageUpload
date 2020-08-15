package com.example.imageupload;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    ImageView uploadPicIV;
    EditText uploadPicET;

    final int IMAGE_REQUST = 71;
    Uri imageLocationPath;

    StorageReference objectStorageReference;
    FirebaseFirestore objectFirebaseFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        uploadPicET = findViewById(R.id.imageNameET);
        uploadPicIV = findViewById(R.id.imageID);

        objectStorageReference = FirebaseStorage.getInstance().getReference("ImageFolder");
        objectFirebaseFirestore = FirebaseFirestore.getInstance();
    }

    public void selectImage(View view) {
        try {
            Intent objectIntent = new Intent();
            objectIntent.setType("image/*");

            objectIntent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(objectIntent, IMAGE_REQUST);
        }
        catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (requestCode == IMAGE_REQUST && resultCode == RESULT_OK && data != null && data.getData() != null) {
                imageLocationPath =data.getData();
                Bitmap objectBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageLocationPath);

                uploadPicIV.setImageBitmap(objectBitmap);
            }
        }
        catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public void uploadImage(View view) {
        try {
            if (!uploadPicET.getText().toString().isEmpty() && imageLocationPath != null) {
                String nameOfImage = uploadPicET.getText().toString() + "." + getExtension(imageLocationPath);
                final StorageReference imageRef = objectStorageReference.child(nameOfImage);

                UploadTask objectUploadTask = imageRef.putFile(imageLocationPath);
                objectUploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }
                        return imageRef.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Map<String, String> objectMap = new HashMap<>();
                            objectMap.put("url", task.getResult().toString());

                            objectFirebaseFirestore.collection("Links").document(uploadPicET.getText().toString())
                                    .set(objectMap)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(MainActivity.this, "Image is uploaded", Toast.LENGTH_LONG).show();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(MainActivity.this, "Image not upload" + e.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                        else {
                            Toast.makeText(MainActivity.this, task.getException().toString(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
            else {
                Toast.makeText(this, "Please provide name for image", Toast.LENGTH_LONG).show();
            }
        }
        catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private String getExtension(Uri uri) {
        try {
            ContentResolver objectContentResolver = getContentResolver();
            MimeTypeMap objectMimeTypeMap = MimeTypeMap.getSingleton();

            return objectMimeTypeMap.getExtensionFromMimeType(objectContentResolver.getType(uri));
        }
        catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
        return null;
    }

    public void moveToShowImageAct(View view) {
        try {
            startActivity(new Intent(this, ShowImageActivity.class));
        }
        catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}