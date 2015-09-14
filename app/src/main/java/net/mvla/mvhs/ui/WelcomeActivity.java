package net.mvla.mvhs.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;

import net.mvla.mvhs.PrefUtils;
import net.mvla.mvhs.R;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        Button student = (Button) findViewById(R.id.activity_welcome_student);
        Button guest = (Button) findViewById(R.id.activity_welcome_guest);
        student.setOnClickListener(v -> setChoice(false));

        guest.setOnClickListener(v -> setChoice(true));
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
