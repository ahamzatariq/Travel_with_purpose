package com.example.travelwithpurpose;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class CreateWriterAccount extends AppCompatActivity
{

    Button createWriterbtn;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_writer_account);
        getSupportActionBar().hide();

        createWriterbtn = findViewById(R.id.signupbtn);
        createWriterbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(getApplicationContext(), WriterProfile.class);
                startActivity(intent);
            }
        });
    }
}
