package com.example.travelwithpurpose;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {

    Toolbar toolbar;
    CircleImageView setupImage;
    EditText nameEdit;
    Button setupBtn;
    ProgressBar progressBar;
    Uri imageUri = null;

    private String username;
    private String userID;

    private Boolean isChanged = false;

    private StorageReference storageReference;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        setupBtn = findViewById(R.id.saveBtn);
        nameEdit = findViewById(R.id.nameEdit);
        setupImage = findViewById(R.id.imageView);
        toolbar = findViewById(R.id.toolbar);
        progressBar = findViewById(R.id.progressSetup);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Account Settings");

        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        userID = mAuth.getCurrentUser().getUid();

        progressBar.setVisibility(View.VISIBLE);
        setupBtn.setEnabled(false);

        firebaseFirestore.collection("Users").document(userID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    if(task.getResult().exists()){
                        String name = task.getResult().getString("name");
                        String image = task.getResult().getString("image");

                        imageUri = Uri.parse(image);

                        nameEdit.setText(name);

                        RequestOptions placeholderRequest = new RequestOptions();
                        placeholderRequest.placeholder(R.drawable.default_profile);
                        Glide.with(SetupActivity.this).setDefaultRequestOptions(placeholderRequest)
                                .load(image).into(setupImage);

                        Toast.makeText(SetupActivity.this, "Data exists", Toast.LENGTH_SHORT).show();
                    } else {
                        //String error = task.getException().getMessage();
                        Toast.makeText(SetupActivity.this, "Data does not exist", Toast.LENGTH_SHORT).show();
                    }

                    progressBar.setVisibility(View.INVISIBLE);
                    setupBtn.setEnabled(true);

                } else {
                    String error = task.getException().getMessage();
                    Toast.makeText(SetupActivity.this, error, Toast.LENGTH_SHORT).show();
                }
            }
        });

        setupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                username = nameEdit.getText().toString();

                if(!TextUtils.isEmpty(username) && imageUri != null){

                progressBar.setVisibility(View.VISIBLE);

                if(isChanged){

                        userID = mAuth.getCurrentUser().getUid();

                        final StorageReference imagePath = storageReference.child("profile_images").child(userID + ".jpg");

                        imagePath.putFile(imageUri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                            @Override
                            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                if(!task.isSuccessful()){
                                    throw task.getException();
                                }
                                return imagePath.getDownloadUrl();
                            }
                        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                if (task.isSuccessful()) {
                                    storeFirestore(task);

                                } else {
                                    Toast.makeText(SetupActivity.this, "Image upload failed: " +
                                            task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                                    progressBar.setVisibility(View.INVISIBLE);
                                }
                            }
                        });
                    } else {
                    storeFirestore(null);
                }
                }
            }
        });

        setupImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    if(ContextCompat.checkSelfPermission(SetupActivity.this,
                            Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                        Toast.makeText(SetupActivity.this, "Permission denied", Toast.LENGTH_SHORT).show();
                        ActivityCompat.requestPermissions(SetupActivity.this,
                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                    } else {
                        imagePicker();
                    }
                } else {
                    imagePicker();
                }
            }
        });
    }

    private void storeFirestore(Task<Uri> task) {

        Uri downloadUri;

        if(task != null){
            downloadUri = task.getResult();
        } else {
            downloadUri = imageUri;
        }

        Toast.makeText(SetupActivity.this, "Image uploaded", Toast.LENGTH_SHORT).show();

        Map<String, String> userMap = new HashMap<>();
        userMap.put("name", username);
        userMap.put("image", downloadUri.toString());

        firebaseFirestore.collection("Users").document(userID).set(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(SetupActivity.this, "Firestore updated", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(SetupActivity.this, HomeActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    String error = task.getException().getMessage();
                    Toast.makeText(SetupActivity.this, "Firestore error: " +
                            error, Toast.LENGTH_SHORT).show();
                }

                progressBar.setVisibility(View.INVISIBLE);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                imageUri = result.getUri();
                setupImage.setImageURI(imageUri);
                isChanged = true;
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    private void imagePicker(){
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .start(SetupActivity.this);
    }

}
