package com.mvhsapp.app.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.mvhsapp.app.R;


public class LoginActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getSupportActionBar().hide();
        // If your minSdkVersion is 11 or higher, instead use:
        //getActionBar().hide();
        int i = 0;
        int[] ids = {
                R.id.Pd0, R.id.Pd1, R.id.Pd2, R.id.Pd3, R.id.Pd4, R.id.Pd5, R.id.Pd6, R.id.Pd7};
        SharedPreferences setUps = this.getSharedPreferences("com.example.jialewan.mvhsappjiale", Context.MODE_PRIVATE);
        for (int each : ids) {
            EditText Pd = (EditText) findViewById(each);
            Pd.getText();
            Log.v("LoginActivity", String.valueOf(Pd.getText()));
            String key = "Pd" + i;
            String value = setUps.getString(key, "");
            Pd.setText(value);
            i++;
        }

        i = 0;
        int[] ids2 = {
                R.id.Pd0Rm, R.id.Pd1Rm, R.id.Pd2Rm, R.id.Pd3Rm, R.id.Pd4Rm, R.id.Pd5Rm, R.id.Pd6Rm, R.id.Pd7Rm};
        for (int each : ids2) {
            EditText Pd = (EditText) findViewById(each);
            Pd.getText();
            Log.v("LoginActivity", String.valueOf(Pd.getText()));
            String key = "PdRm" + i;
            String value = setUps.getString(key, "");
            Pd.setText(value);
            i++;
        }


    }

    public void savePreference(View view) {
        int i = 0;
        int[] ids = {
                R.id.Pd0, R.id.Pd1, R.id.Pd2, R.id.Pd3, R.id.Pd4, R.id.Pd5, R.id.Pd6, R.id.Pd7};
        SharedPreferences setUps = this.getSharedPreferences("com.example.jialewan.mvhsappjiale", Context.MODE_PRIVATE);
        SharedPreferences.Editor e = setUps.edit();
        for (int each : ids) {
            EditText Pd = (EditText) findViewById(each);
            Pd.getText();
            Log.v("LoginActivity", String.valueOf(Pd.getText()));
            String key = "Pd" + i;
            e.putString(key, Pd.getText().toString());
            i++;
        }

        i = 0;
        int[] ids2 = {
                R.id.Pd0Rm, R.id.Pd1Rm, R.id.Pd2Rm, R.id.Pd3Rm, R.id.Pd4Rm, R.id.Pd5Rm, R.id.Pd6Rm, R.id.Pd7Rm};
        for (int each : ids2) {
            EditText Pd = (EditText) findViewById(each);
            Pd.getText();
            Log.v("LoginActivity", String.valueOf(Pd.getText()));
            String key = "PdRm" + i;
            e.putString(key, Pd.getText().toString());
            i++;
        }
        e.commit();
        this.finish();
    }
}
