package net.mvla.mvhs.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Pair;
import android.widget.Toast;

import net.mvla.mvhs.R;
import net.mvla.mvhs.Utils;
import net.mvla.mvhs.model.BellSchedule;
import net.mvla.mvhs.model.BellSchedulePeriod;
import net.mvla.mvhs.model.sheet.Entry;
import net.mvla.mvhs.model.sheet.RootSheetElement;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import biweekly.Biweekly;
import biweekly.ICalendar;
import biweekly.component.VEvent;
import retrofit.RestAdapter;
import retrofit.client.Response;
import retrofit.http.GET;
import retrofit.mime.TypedByteArray;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func2;
import rx.schedulers.Schedulers;

public class ScheduleActivity extends DrawerActivity {

    private Calendar mNow;

    public static void saveBytesToFile(byte[] bytes, String path) {
        FileOutputStream fileOutputStream = null;

        try {
            fileOutputStream = new FileOutputStream(path);
            fileOutputStream.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        mNow = Calendar.getInstance();

        if (isDeviceOnline()) {
            //Fetch today's events (from calendar) and bell schedule sheet entries in parallel
            Observable.combineLatest(getEventsToday(), getBellScheduleSheetEntries(),
                    (Func2<List<VEvent>, List<Entry>, Pair<List<VEvent>, List<Entry>>>) Pair::new)
                    .flatMap(this::getBellSchedule)
                    .switchIfEmpty(getNoSchoolAlt())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<BellSchedule>() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onError(Throwable e) {
                            e.printStackTrace();

                            FragmentManager fm = getFragmentManager();
                            ScheduleFragment f = (ScheduleFragment) fm.findFragmentById(R.id.activity_schedule_fragment);
                            f.setErrorMessage("Error - cannot retrieve online bell schedule.");
                        }

                        @Override
                        public void onNext(BellSchedule bellSchedule) {
                            bellSchedule.sort();

                            FragmentManager fm = getFragmentManager();
                            ScheduleFragment f = (ScheduleFragment) fm.findFragmentById(R.id.activity_schedule_fragment);
                            f.setBellSchedule(bellSchedule);

                            //Toast.makeText(ScheduleActivity.this, bellSchedule.toString(), Toast.LENGTH_LONG).show();
                        }
                    });
        } else {
            Toast.makeText(ScheduleActivity.this, "Not online - cannot retrieve online bell schedule", Toast.LENGTH_LONG).show();
        }

        FragmentManager fm = getFragmentManager();
        Fragment f = fm.findFragmentById(R.id.activity_schedule_fragment);
        if (f == null) {
            f = new ScheduleFragment();
            fm.beginTransaction()
                    .replace(R.id.activity_schedule_fragment, f)
                    .commit();
        }

        overridePendingTransition(0, 0);
    }

    private Observable<List<VEvent>> getEventsToday() {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint("http://www.mvla.net")
                .build();
        CalendarIcalService service = restAdapter.create(CalendarIcalService.class);
        return service.getCalendarFile()
                .observeOn(Schedulers.io())
                .flatMap(this::getEventList)
                .flatMap(this::getEventsTodayFromList);
    }

    @NonNull
    private Observable<List<Entry>> getBellScheduleSheetEntries() {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint("https://spreadsheets.google.com")
                .build();
        SheetService service = restAdapter.create(SheetService.class);

        return service.getRootElement()
                .observeOn(Schedulers.io())
                .flatMap(rootSheetElement -> Observable.create(new Observable.OnSubscribe<List<Entry>>() {
                    @Override
                    public void call(Subscriber<? super List<Entry>> subscriber) {
                        subscriber.onNext(rootSheetElement.getFeed().getEntry());
                        subscriber.onCompleted();
                    }
                }));
    }

    @NonNull
    private Observable<BellSchedule> getNoSchoolAlt() {
        return Observable.create(subscriber -> {
            FragmentManager fm = getFragmentManager();
            ScheduleFragment f = (ScheduleFragment) fm.findFragmentById(R.id.activity_schedule_fragment);
            f.setErrorMessage("No school today!");

            subscriber.onCompleted();
        });
    }

    @NonNull
    private Observable<List<VEvent>> getEventList(Response calendarResponse) {
        return Observable.create(subscriber -> {
            byte[] calBytes = ((TypedByteArray) calendarResponse.getBody()).getBytes();
            File file = new File(getCacheDir(), "calendar.ics");
            saveBytesToFile(calBytes, file.getPath());
            ICalendar calendar;
            try {
                calendar = Biweekly.parse(file).first();
            } catch (IOException e) {
                subscriber.onError(e);
                return;
            }

            subscriber.onNext(calendar.getEvents());
            subscriber.onCompleted();
        });
    }

    @NonNull
    private Observable<List<VEvent>> getEventsTodayFromList(final List<VEvent> eventList) {
        return Observable.create(subscriber -> {
            List<VEvent> eventsToday = new ArrayList<>();
            for (VEvent event : eventList) {
                Calendar time = new GregorianCalendar();
                time.setTime(event.getDateStart().getValue());
                if (Utils.sameDay(time, mNow)) {
                    eventsToday.add(event);
                }
            }
            subscriber.onNext(eventsToday);
            subscriber.onCompleted();
        });
    }

    @NonNull
    private Observable<BellSchedule> getBellSchedule(final Pair<List<VEvent>, List<Entry>> eventsAndBellSched) {
        return Observable.create(subscriber -> {

            BellSchedule schedule = new BellSchedule();

            String findCol = null;
            for (Entry entry : eventsAndBellSched.second) {
                String cellCoord = entry.getTitle().get$t();
                String cellRow = cellCoord.substring(1, 2);
                String cellCol = cellCoord.substring(0, 1);
                String cellContent = entry.getContent().get$t();
                if (findCol == null) {
                    if (cellRow.equals("1")) {
                        //Iterating through schedule names - decide column
                        for (VEvent vEvent : eventsAndBellSched.first) {
                            if (cellContent.startsWith(vEvent.getSummary().getValue().split("\\|")[0])) {
                                schedule.name = cellContent;
                                findCol = cellCol;
                            }
                        }
                    } else {
                        //If go through all, and none are it, choose standard schedule
                        switch (mNow.get(java.util.Calendar.DAY_OF_WEEK)) {
                            case java.util.Calendar.MONDAY:
                            case java.util.Calendar.TUESDAY:
                            case java.util.Calendar.FRIDAY:
                                schedule.name = "Sched. A";
                                findCol = "B";
                                break;
                            case java.util.Calendar.WEDNESDAY:
                                schedule.name = "Sched. B";
                                findCol = "C";
                                break;
                            case java.util.Calendar.THURSDAY:
                                schedule.name = "Sched. C";
                                findCol = "D";
                                break;
                            default:
                                //Weekend
                                subscriber.onCompleted();
                                return;
                        }
                        //cellCol is now at "A" - we're in second row
                        addScheduleName(schedule, cellContent);
                    }
                } else {
                    if (cellCol.equals("A")) {
                        addScheduleName(schedule, cellContent);
                    } else if (findCol.equals(cellCol)) {
                        //Got the schedule
                        BellSchedulePeriod period = schedule.bellSchedulePeriods.get(schedule.bellSchedulePeriods.size() - 1);
                        String[] time = cellContent.split("[\\s:\\-]");
                        period.startHour = Integer.parseInt(time[0]);
                        period.startMinute = Integer.parseInt(time[1]);
                        period.endHour = Integer.parseInt(time[2]);
                        period.endMinute = Integer.parseInt(time[3]);
                    }
                }
            }

            for (Iterator<BellSchedulePeriod> iterator = schedule.bellSchedulePeriods.iterator(); iterator.hasNext(); ) {
                BellSchedulePeriod period = iterator.next();
                if (period.startHour == 0) {
                    iterator.remove();
                }
            }

            subscriber.onNext(schedule);
            subscriber.onCompleted();
        });
    }

    private void addScheduleName(BellSchedule schedule, String cellContent) {
        //Period name column
        BellSchedulePeriod period = new BellSchedulePeriod();
        period.name = cellContent;
        schedule.bellSchedulePeriods.add(period);
    }

    @Override
    int getSelfNavDrawerItem() {
        return NAVDRAWER_ITEM_TODAYS_SCHED;
    }



    public interface SheetService {
        @GET("/feeds/cells/1BBGLmF4GgV7SjtZyfMANa6CVxr4-GY-_O1l1ZJX6Ooo/od6/public/basic?alt=json")
        Observable<RootSheetElement> getRootElement();
    }

    public interface CalendarIcalService {
        @GET("/rss.cfm?a=Events&s=MVHS&format=ical")
        Observable<Response> getCalendarFile();
    }

}
