package net.mvla.mvhs.schedulecalendar;

import android.content.Context;
import android.support.annotation.NonNull;

import net.mvla.mvhs.MvpView;
import net.mvla.mvhs.schedulecalendar.bellschedule.BellSchedule;

import java.util.Calendar;
import java.util.List;

public interface ScheduleCalendarView extends MvpView {

    void showCalendarError(String error);

    void showBellScheduleError(String error);

    void setLoading();

    void setBellSchedule(@NonNull BellSchedule bellSchedule, Calendar selectedCalDate);

    void hideBellScheduleProgress();

    void setSelectedDate(Calendar date);

    void showChangelog();

    Context getContext();

    void setEvents(@NonNull List<ScheduleCalendarRepository.Event> events);

    void hideCalendarProgress();

    void openCalendarView();

    Context getApplicationContext();
}
