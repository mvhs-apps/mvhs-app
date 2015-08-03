package com.mvhsapp.app.ui;

import android.content.ContentUris;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.view.View;
import android.widget.Button;

import com.mvhsapp.app.R;

import java.util.Date;


public class CalendarActivity extends DrawerActivity {

    //TODO: Integrate with Google Cal and schedule system

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        Button add = (Button) findViewById(R.id.activity_calendar_add);
        Button today = (Button) findViewById(R.id.activity_calendar_view);
        Button upcoming = (Button) findViewById(R.id.activity_calendar_upcoming);

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addHomework();
            }
        });

        today.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                todayHW();
            }
        });

        upcoming.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCalendar();
            }
        });

        overridePendingTransition(0, 0);
    }

    public void addHomework() {
        Intent calIntent = new Intent(Intent.ACTION_INSERT);
        calIntent.setType("vnd.android.cursor.item/event");
        calIntent.putExtra(CalendarContract.Events.TITLE, "");
        calIntent.putExtra(CalendarContract.Events.EVENT_LOCATION, "");
        calIntent.putExtra(CalendarContract.Events.DESCRIPTION, "");
        startActivity(calIntent);
    }

    public void todayHW() {
        Uri.Builder builder = CalendarContract.CONTENT_URI.buildUpon();
        builder.appendPath("time");
        ContentUris.appendId(builder, new Date().getTime());
        Intent intent = new Intent(Intent.ACTION_VIEW).setData(builder.build());
        intent.putExtra("VIEW", "DAY");
        startActivity(intent);
    }

    public void openCalendar() {
        // A date-time specified in milliseconds since the epoch.
        Uri.Builder builder = CalendarContract.CONTENT_URI.buildUpon();
        builder.appendPath("time");
        ContentUris.appendId(builder, new Date().getTime());
        Intent intent = new Intent(Intent.ACTION_VIEW).setData(builder.build());
        startActivity(intent);
    }

    @Override
    int getSelfNavDrawerItem() {
        return NAVDRAWER_ITEM_CALENDAR;
    }
}
