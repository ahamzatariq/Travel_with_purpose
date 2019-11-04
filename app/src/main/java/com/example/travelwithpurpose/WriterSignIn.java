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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class WriterSignIn extends AppCompatActivity {

    TextView signUpTv;
    EditText loginEmailText;
    EditText loginPassText;
    Button loginBtn;
    ProgressBar loginProgress;

    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_writer_sign_in);
        //getSupportActionBar().hide();

        initialise();

        signUpTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(getApplicationContext(), CreateWriterAccount.class);
                startActivity(intent);
            }
        });

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = loginEmailText.getText().toString();
                String pass = loginPassText.getText().toString();

                if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(pass)){
                    loginProgress.setVisibility(View.VISIBLE);
                    mAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if(task.isSuccessful()){
                                sendtoMain();
                            } else {
                                String e = task.getException().getMessage();
                                Toast.makeText(WriterSignIn.this, e, Toast.LENGTH_SHORT).show();
                            }

                            loginProgress.setVisibility(View.INVISIBLE);
                        }
                    });
                }
            }
        });
    }

    private void initialise() {
        signUpTv = findViewById(R.id.create_account_tv);
        loginEmailText = findViewById(R.id.emailet);
        loginPassText = findViewById(R.id.passwordet);
        loginBtn = findViewById(R.id.signupbtn);
        loginProgress = findViewById(R.id.login_progress);

        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            sendtoMain();
        }
    }

    private void sendtoMain(){
        Intent mainIntent = new Intent(WriterSignIn.this, HomeActivity.class);
        startActivity(mainIntent);
        finish();
    }

}
