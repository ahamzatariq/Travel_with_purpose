package com.example.travelwithpurpose;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class CreateWriterAccount extends AppCompatActivity
{

    Button createWriterbtn;
    EditText regEmail;
    EditText regPass;
    EditText confPass;
    Button loginBtn;
    ProgressBar progressBar;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_writer_account);
        //getSupportActionBar().hide();

        createWriterbtn = findViewById(R.id.signupbtn);
        regEmail = findViewById(R.id.emailet);
        if (getIntent().getStringExtra("email")!=null)
            regEmail.setText(getIntent().getStringExtra("email"));
        regPass = findViewById(R.id.passwordet);
        confPass = findViewById(R.id.passwordconet);
        loginBtn = findViewById(R.id.loginBtn);
        progressBar = findViewById(R.id.progressBar);

        mAuth = FirebaseAuth.getInstance();

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CreateWriterAccount.this, WriterSignIn.class);
                startActivity(intent);
                finish();
            }
        });

        createWriterbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = regEmail.getText().toString();
                String pass = regPass.getText().toString();
                String conf = confPass.getText().toString();

                if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(pass) && !TextUtils.isEmpty(conf)) {
                    if (pass.equals(conf)) {

                        progressBar.setVisibility(View.VISIBLE);

                        mAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                if(task.isSuccessful()){
                                    Intent intent = new Intent(CreateWriterAccount.this, SetupActivity.class);
                                    startActivity(intent);
                                    finish();
                                    //sendToMain();
                                } else {
                                    String e = task.getException().getMessage();
                                    Toast.makeText(CreateWriterAccount.this, e, Toast.LENGTH_SHORT).show();
                                }

                                progressBar.setVisibility(View.INVISIBLE);
                            }
                        });
                    } else {
                        Toast.makeText(CreateWriterAccount.this, "Passwords don't match.",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser user = mAuth.getCurrentUser();
        if(user != null){
            sendToMain();
        }
    }

    private void sendToMain(){
        Intent intent = new Intent(CreateWriterAccount.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }
}
