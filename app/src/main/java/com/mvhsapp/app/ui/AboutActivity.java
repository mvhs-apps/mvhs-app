package com.mvhsapp.app.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.mvhsapp.app.BuildConfig;
import com.mvhsapp.app.R;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        TextView view = (TextView) findViewById(R.id.activity_about_appname_textview);
        view.setText(getString(R.string.app_name) + " v" + BuildConfig.VERSION_NAME);

        Button emailLead = (Button) findViewById(R.id.activity_about_email_lead);
        emailLead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_SENDTO,
                        Uri.fromParts(
                                "mailto", "plusCubed@gmail.com", null));
                startActivity(intent);
            }
        });

        Button emailCompSci = (Button) findViewById(R.id.activity_about_email_compsci);
        emailCompSci.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_SENDTO,
                        Uri.fromParts(
                                "mailto", "ly.nguyen@mvla.net", null));
                startActivity(intent);
            }
        });

        Button leadWebsite = (Button) findViewById(R.id.activity_about_lead_website);
        leadWebsite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://www.plusCubed.com"));
                startActivity(intent);
            }
        });

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(R.string.about);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
