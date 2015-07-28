package com.mvhsapp.app.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.mvhsapp.app.R;


public class MainMenuActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        getSupportActionBar().hide();
        // If your minSdkVersion is 11 or higher, instead use:
        //getActionBar().hide();
        // if(isSetup())
        openSetUp();

    }

    private boolean isSetup() {
        boolean yes = true;
        String value = null;
        int i = 0;
        int[] ids = {
                R.id.Pd0, R.id.Pd1, R.id.Pd2, R.id.Pd3, R.id.Pd4, R.id.Pd5, R.id.Pd6, R.id.Pd7};
        SharedPreferences setUps = this.getSharedPreferences("com.example.jialewan.mvhsappjiale", Context.MODE_PRIVATE);
        for (int each : ids) {
            EditText Pd = (EditText) findViewById(each);
            Pd.getText();
            Log.v("LoginActivity", String.valueOf(Pd.getText()));
            String key = "Pd" + i;
            value = setUps.getString(key, "");
            Pd.setText(value);
            i++;
        }
        if (value == null) {
            return yes;
        } else {
            return false;
        }

    }

    public void openSetUp() {
        Intent myIntent = new Intent(this, LoginActivity.class);
        // myIntent.putExtra("key", value); //Optional parameters
        myIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        this.startActivity(myIntent);
        overridePendingTransition(R.anim.abc_slide_in_top, R.anim.abc_slide_out_bottom);
    }

    public void openAeries(View view) {
        String url = "https://parentportal.mvla.net/";
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(i);
        overridePendingTransition(R.anim.abc_slide_in_top, R.anim.abc_slide_out_bottom);
    }

    public void openCalendar(View view) {
        Intent myIntent = new Intent(this, CalendarMainActivity.class);
        // myIntent.putExtra("key", value); //Optional parameters
        myIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        this.startActivity(myIntent);
        overridePendingTransition(R.anim.abc_slide_in_top, R.anim.abc_slide_out_bottom);
    }

    public void openLoginActivity(View view) {
        openSetUp();
    }

    public void openMVHS(View view) {
        String url = "http://www.mvla.net/MVHS/";
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(i);
        overridePendingTransition(R.anim.abc_slide_in_top, R.anim.abc_slide_out_bottom);
    }
}
