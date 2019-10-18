package com.example.travelwithpurpose;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener
{
    ImageButton readerBtn,writerBtn;
    TextView readerTv,writerTv;

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
        readerBtn = findViewById(R.id.readerbtn);
        writerBtn = findViewById(R.id.writerbtn);
        readerTv = findViewById(R.id.readertext);
        writerTv = findViewById(R.id.writertext);
    }

    @Override
    public void onClick(View view)
    {
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

    private void goToWriter()
    {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
    }

    private void goToReader()
    {
        Intent intent = new Intent(this, Reader.class);
        startActivity(intent);

    }
}
