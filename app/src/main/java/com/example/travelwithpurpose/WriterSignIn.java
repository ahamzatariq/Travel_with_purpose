package com.example.travelwithpurpose;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class WriterSignIn extends AppCompatActivity {

    TextView signUpTv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_writer_sign_in);
        getSupportActionBar().hide();

        initialise();
    }

    private void initialise()
    {
        signUpTv = findViewById(R.id.create_account_tv);
        signUpTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(getApplicationContext(), CreateWriterAccount.class);
                startActivity(intent);
            }
        });
    }
}
