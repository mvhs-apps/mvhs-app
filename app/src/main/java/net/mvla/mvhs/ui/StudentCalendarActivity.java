package net.mvla.mvhs.ui;

import android.content.ContentUris;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.widget.Button;

import net.mvla.mvhs.R;

import java.util.Date;


public class StudentCalendarActivity extends DrawerActivity {

    //TODO: Integrate with Google Cal and schedule system

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_calendar);

        Button add = (Button) findViewById(R.id.activity_calendar_add);
        Button today = (Button) findViewById(R.id.activity_calendar_view);

        add.setOnClickListener(v -> addEvent());

        today.setOnClickListener(v -> openCalendar());

        overridePendingTransition(0, 0);
    }

    public void addEvent() {
        Intent calIntent = new Intent(Intent.ACTION_INSERT);
        calIntent.setType("vnd.android.cursor.item/event");
        calIntent.putExtra(CalendarContract.Events.TITLE, "");
        calIntent.putExtra(CalendarContract.Events.EVENT_LOCATION, "");
        calIntent.putExtra(CalendarContract.Events.DESCRIPTION, "");
        startActivity(calIntent);
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
    protected int getSelfNavDrawerItem() {
        return NAVDRAWER_ITEM_CALENDAR;
    }
}
