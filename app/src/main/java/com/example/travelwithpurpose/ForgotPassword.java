package com.example.travelwithpurpose;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPassword extends AppCompatActivity
{

    EditText Email;
    Button PasswordResetButton;
    String EmailStr;
    FirebaseAuth auth;
    private String TAG;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        auth = FirebaseAuth.getInstance();
        Email = findViewById(R.id.emailedittext);
        PasswordResetButton = findViewById(R.id.passwordresetbtn);
        PasswordResetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                EmailStr = Email.getText().toString();
                auth.sendPasswordResetEmail(EmailStr)
                        .addOnCompleteListener(new OnCompleteListener<Void>()
                        {
                            @Override
                            public void onComplete(@NonNull Task<Void> task)
                            {
                                if (task.isSuccessful())
                                {
                                    Log.d(TAG, "Email sent.");
                                    Toast.makeText(
                                            getApplicationContext(),
                                            "Email sent, please check your inbox\nRedirecting you to sign up page . . .",
                                            Toast.LENGTH_LONG)
                                            .show();

                                    Intent intent = new Intent(getApplicationContext(), WriterSignIn.class);
                                    intent.putExtra("email",EmailStr);
                                    startActivity(intent);

                                }
                                else
                                {
                                    Toast.makeText(
                                            getApplicationContext(),
                                            "Email doesn't exist\nCreate a new writer account",
                                            Toast.LENGTH_LONG)
                                            .show();

                                    Intent intent = new Intent(getApplicationContext(), CreateWriterAccount.class);
                                    intent.putExtra("email",EmailStr);
                                    startActivity(intent);

                                }
                            }
                        });


            }
        });
    }
}
