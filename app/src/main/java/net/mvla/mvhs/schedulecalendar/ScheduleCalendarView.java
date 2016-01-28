package net.mvla.mvhs.schedulecalendar;

import android.content.Context;
import android.support.annotation.NonNull;

import net.mvla.mvhs.MvpView;
import net.mvla.mvhs.schedulecalendar.bellschedule.BellSchedule;

import java.util.Calendar;
import java.util.List;

import biweekly.component.VEvent;

public interface ScheduleCalendarView extends MvpView {

    void showErrorMessage(String error);

    void setLoading();

    void setData(@NonNull BellSchedule bellSchedule, @NonNull List<VEvent> events, Calendar selectedCalDate);

    void setSelectedDate(Calendar date);

    void showChangelog();

    Context getContext();

    void openCalendarView();

    Context getApplicationContext();
}
