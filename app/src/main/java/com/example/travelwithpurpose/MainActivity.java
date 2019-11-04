package com.example.travelwithpurpose;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity implements View.OnClickListener
{
    ImageButton readerBtn,writerBtn;
    TextView readerTv,writerTv;

    FirebaseAuth mAuth;

    ProgressBar progressBar;

    Boolean guest = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initialise();
        //getSupportActionBar().hide();
        readerBtn.setOnClickListener(this);
        readerTv.setOnClickListener(this);

        writerBtn.setOnClickListener(this);
        writerTv.setOnClickListener(this);
    }

    private void initialise()
    {
        progressBar = findViewById(R.id.login_progress);
        readerBtn = findViewById(R.id.readerbtn);
        writerBtn = findViewById(R.id.writerbtn);
        readerTv = findViewById(R.id.readertext);
        writerTv = findViewById(R.id.writertext);

        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.readerbtn:
                goToReader();
                break;

            case R.id.readertext:
                goToReader();
                break;

            case R.id.writerbtn:
                goToWriter();
                break;

            case R.id.writertext:
                goToWriter();
                break;
    }
}

    private void goToWriter() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
    }

    private void goToReader() {
        guest = true;
        progressBar.setVisibility(View.VISIBLE);
        mAuth.signInAnonymously().addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Toast.makeText(MainActivity.this, "Guest Signed-in", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                    intent.putExtra("guest", guest);
                    startActivity(intent);
                } else {
                    Toast.makeText(MainActivity.this, "Guest sign-in failed", Toast.LENGTH_SHORT).show();
                }

                progressBar.setVisibility(View.INVISIBLE);
            }
        });
    }
}
