package com.mvhsapp.app.ui;

import android.content.ContentUris;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.mvhsapp.app.R;

import java.util.Date;


public class CalendarMainActivity extends ActionBarActivity implements View.OnClickListener {
    private Button button;
    private ListView listView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("Initialization", "We ARE HERE");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar_main);
        Log.i("Initialization", "About to set up List Yasha");

        getSupportActionBar().hide();
        // If your minSdkVersion is 11 or higher, instead use:
        //getActionBar().hide();


    }

    public void onClick(View v) {

    }


    public void addHomework(View view) {
        Intent calIntent = new Intent(Intent.ACTION_INSERT);
        calIntent.setType("vnd.android.cursor.item/event");
        calIntent.putExtra(CalendarContract.Events.TITLE, "");
        calIntent.putExtra(CalendarContract.Events.EVENT_LOCATION, "");
        calIntent.putExtra(CalendarContract.Events.DESCRIPTION, "");
        startActivity(calIntent);
    }

    public void todayHW(View view) {
        Uri.Builder builder = CalendarContract.CONTENT_URI.buildUpon();
        builder.appendPath("time");
        ContentUris.appendId(builder, new Date().getTime());
        Intent intent = new Intent(Intent.ACTION_VIEW).setData(builder.build());
        intent.putExtra("VIEW", "DAY");
        startActivity(intent);
    }

    public void openCalendar(View view) {
// A date-time specified in milliseconds since the epoch.
        Uri.Builder builder = CalendarContract.CONTENT_URI.buildUpon();
        builder.appendPath("time");
        ContentUris.appendId(builder, new Date().getTime());
        Intent intent = new Intent(Intent.ACTION_VIEW).setData(builder.build());
        startActivity(intent);
    }

}
