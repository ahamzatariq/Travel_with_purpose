package com.example.travelwithpurpose;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import fr.ganfra.materialspinner.MaterialSpinner;
import id.zelory.compressor.Compressor;

import static io.opencensus.tags.TagKey.MAX_LENGTH;

public class NewPostActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    Toolbar toolbar;
    EditText newPostDesc;
    ImageView newPostImage;
    Button newPostBtn;
    ProgressBar progressBar;
    MaterialSpinner spinner;

    Uri postImageUri = null;
    StorageReference storageReference;
    FirebaseFirestore firebaseFirestore;
    FirebaseAuth mAuth;

    String currentUserID;
    Bitmap compressedImageFile;

    String postImage, postDesc, blogID, imageUri, category;
    String[] spinnerItems = {"General", "Entertainment", "Lifestyle", "Technology", "Fashion", "Health", "Sports", "Academia"};
    String spinnerText = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        newPostDesc = findViewById(R.id.newPostDesc);
        newPostImage = findViewById(R.id.newPostImage);
        newPostBtn = findViewById(R.id.newPostBtn);
        toolbar = findViewById(R.id.main_toolbar);
        progressBar = findViewById(R.id.progressBar);
        spinner = findViewById(R.id.spinner);

        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        currentUserID = mAuth.getCurrentUser().getUid();

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Add New Post");
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, spinnerItems);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        if(getIntent() != null){
            postImage = getIntent().getStringExtra("image");
            postDesc = getIntent().getStringExtra("desc");
            blogID = getIntent().getStringExtra("blogID");
            imageUri = getIntent().getStringExtra("imageUri");
            category = getIntent().getStringExtra("category");

            newPostDesc.setText(postDesc);
            Glide.with(getApplicationContext())
                    .load(postImage)
                    .into(newPostImage);

            if(imageUri != null) {
                postImageUri = Uri.parse(imageUri);
            }
        }

        if(getIntent().getStringExtra("edit") != null){

            Toast.makeText(this, "inside Edit GetIntent", Toast.LENGTH_SHORT).show();

            getSupportActionBar().setTitle("Edit Post");
            newPostBtn.setText("Save Changes");

        }

        if(getIntent().getStringExtra("view") != null) {

            Toast.makeText(this, "inside View GetIntent", Toast.LENGTH_SHORT).show();

            getSupportActionBar().setTitle("Blog Post Details");
            newPostDesc.setEnabled(false);
            newPostDesc.setTextColor(getResources().getColor(R.color.colorAccent));
            newPostDesc.setTextSize(20);
            newPostImage.setEnabled(false);
            newPostBtn.setVisibility(View.INVISIBLE);
//            spinner.setEnabled(false);
            spinner.setVisibility(View.INVISIBLE);


        }

//        if(getIntent().getStringExtra("image") != null){
//
//            Toast.makeText(this, "inside Edit GetIntent", Toast.LENGTH_SHORT).show();
//
//            postImage = getIntent().getStringExtra("image");
//            postDesc = getIntent().getStringExtra("desc");
//            blogID = getIntent().getStringExtra("blogID");
//            imageUri = getIntent().getStringExtra("imageUri");
//
//            newPostDesc.setText(postDesc);
//            Glide.with(getApplicationContext())
//                    .load(postImage)
//                    .into(newPostImage);
//
//            postImageUri = Uri.parse(imageUri);
//
//            getSupportActionBar().setTitle("Edit Post");
//            newPostBtn.setText("Save Changes");
//        }

        newPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imagePicker();
            }
        });

        newPostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String desc = newPostDesc.getText().toString();

                if(!TextUtils.isEmpty(desc) && postImageUri != null && !spinnerText.equalsIgnoreCase("Category")){
                    Toast.makeText(NewPostActivity.this, "Inside button", Toast.LENGTH_SHORT).show();

                    progressBar.setVisibility(View.VISIBLE);

                    final String randomName = UUID.randomUUID().toString();

                    final StorageReference filePath = storageReference.child("post_images")
                            .child(randomName + ".jpg");

                    filePath.putFile(postImageUri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                            if(!task.isSuccessful()){
                                throw task.getException();
                            }
                            return filePath.getDownloadUrl();
                        }
                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull final Task<Uri> task) {

                            final String downloadUri = task.getResult().toString();

                            if (task.isSuccessful()) {
                                File newImageFile = new File(postImageUri.getPath());

                                try {
                                    compressedImageFile = new Compressor(NewPostActivity.this)
                                            .setMaxHeight(200)
                                            .setMaxWidth(200)
                                            .setQuality(5)
                                            .compressToBitmap(newImageFile);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                compressedImageFile.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                                byte[] thumbData = baos.toByteArray();

                                final StorageReference ref = storageReference.child("post_images/thumbs")
                                        .child(randomName + ".jpg");

                                UploadTask uploadTask = ref.putBytes(thumbData);

//                                UploadTask uploadTask = storageReference.child("post_images/thumbs")
//                                        .child(randomName + ".jpg").putBytes(thumbData);

                                Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                                    @Override
                                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                        if (!task.isSuccessful()) {
                                            throw task.getException();
                                        }

                                        // Continue with the task to get the download URL
                                        return ref.getDownloadUrl();
                                    }
                                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Uri> task) {
                                        if (task.isSuccessful()) {
                                            String downloadThumbUri = task.getResult().toString();

                                            Map<String, Object> postMap = new HashMap<>();
                                            postMap.put("imageURI", postImageUri.toString());
                                            postMap.put("image_url", downloadUri);
                                            postMap.put("thumb", downloadThumbUri);
                                            postMap.put("desc", desc);
                                            postMap.put("user_id", currentUserID);
                                            postMap.put("timestamp", FieldValue.serverTimestamp());
                                            postMap.put("category", spinnerText);

                                            if(getIntent().getStringExtra("image") != null){

                                                firebaseFirestore.collection("Posts").document(blogID).set(postMap)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if(task.isSuccessful()){
                                                            Toast.makeText(NewPostActivity.this, "Post Edited", Toast.LENGTH_SHORT).show();
                                                            Intent intent = new Intent(NewPostActivity.this, HomeActivity.class);
                                                            startActivity(intent);
                                                            finish();
                                                        } else {
                                                            Toast.makeText(NewPostActivity.this, "Error saving post", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });

                                            } else {

                                                firebaseFirestore.collection("Posts").add(postMap)
                                                        .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<DocumentReference> task) {
                                                                if(task.isSuccessful()){
                                                                    Toast.makeText(NewPostActivity.this, "Post Created",
                                                                            Toast.LENGTH_SHORT).show();
                                                                    Intent intent = new Intent(NewPostActivity.this, HomeActivity.class);
                                                                    startActivity(intent);
                                                                    finish();
                                                                } else {
                                                                    String error = task.getException().getMessage();
                                                                    Toast.makeText(NewPostActivity.this, error, Toast.LENGTH_SHORT).show();
                                                                }

                                                                progressBar.setVisibility(View.INVISIBLE);
                                                            }
                                                        });

                                            }

                                        } else {
                                            String error = task.getException().getMessage();
                                            Toast.makeText(NewPostActivity.this, error, Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });

//                                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                                    @Override
//                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//
//                                    }
//                                }).addOnFailureListener(new OnFailureListener() {
//                                    @Override
//                                    public void onFailure(@NonNull Exception e) {
//
//                                    }
//                                });
                            } else {
                                Toast.makeText(NewPostActivity.this, "Post upload failed: " +
                                        task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                                progressBar.setVisibility(View.INVISIBLE);
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        spinnerText = parent.getItemAtPosition(position).toString();
        //Toast.makeText(parent.getContext(), text, Toast.LENGTH_SHORT).show();
        Log.d("Spinner value", spinnerText);
    }
    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private void imagePicker(){
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .start(NewPostActivity.this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                postImageUri = result.getUri();
                newPostImage.setImageURI(postImageUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}
