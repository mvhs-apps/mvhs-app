package com.mvhsapp.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.mvhsapp.app.PrefUtils;
import com.mvhsapp.app.R;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        Button student = (Button) findViewById(R.id.activity_welcome_student);
        Button guest = (Button) findViewById(R.id.activity_welcome_guest);
        student.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setChoice(false);
            }
        });

        guest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setChoice(true);
            }
        });
    }

    @Override
    public void onBackPressed() {
        //Override back
    }

    private void setChoice(boolean guest) {
        PrefUtils.setGuestMode(this, guest);
        if (!guest) {
            startActivity(new Intent(this, ScheduleSetupActivity.class));
            overridePendingTransition(0, 0);
        }
        PrefUtils.markWelcomeDone(this);
        finish();
    }
}
