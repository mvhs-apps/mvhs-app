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
        Button teacher = (Button) findViewById(R.id.activity_welcome_teacher);
        student.setOnClickListener(v -> setChoice(0));
        teacher.setOnClickListener(v -> setChoice(1));
        guest.setOnClickListener(v -> setChoice(2));
    }

    @Override
    public void onBackPressed() {
        //Override back
    }

    private void setChoice(int mode) {
        PrefUtils.setMode(this, mode);
        if (mode != 2) {
            Intent intent = new Intent(this, ScheduleSetupActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        }
        PrefUtils.markWelcomeDone(this);
        finish();
    }
}
