package net.mvla.mvhs.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.format.DateUtils;
import android.widget.Toast;

import net.mvla.mvhs.R;
import net.mvla.mvhs.model.BellSchedule;
import net.mvla.mvhs.model.BellSchedulePeriod;
import net.mvla.mvhs.model.event.EventList;
import net.mvla.mvhs.model.event.Item;
import net.mvla.mvhs.model.sheet.Entry;
import net.mvla.mvhs.model.sheet.RootSheetElement;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import retrofit.RestAdapter;
import retrofit.http.GET;
import retrofit.http.Query;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class ScheduleActivity extends DrawerActivity {

    private boolean mBellInitialized;

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

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint("https://www.googleapis.com/calendar/v3/calendars/kci7ig724mqv7ps1mkn54cc9rs%40group.calendar.google.com")
                .build();

        if (isDeviceOnline()) {
            CalendarService service = restAdapter.create(CalendarService.class);
            String apiKey = getString(R.string.web_api_key);
            service.listEvents(apiKey)
                    .flatMap(eventList -> Observable.<List<String>>create(subscriber -> {
                        List<Item> items = eventList.getItems();
                        List<String> eventsToday = new ArrayList<>();
                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
                        for (Item item : items) {
                            //Toast.makeText(ScheduleActivity.this, item.getSummary() + "\n" + item.getStart().getDateTime(), Toast.LENGTH_SHORT).show();
                            try {
                                Date start = format.parse(item.getStart().getDateTime());
                                if (DateUtils.isToday(start.getTime())) {
                                    eventsToday.add(item.getSummary());
                                }
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                        subscriber.onNext(eventsToday);
                        subscriber.onCompleted();
                    }))
                    .observeOn(Schedulers.io())
                    .flatMap(this::getBellSchedule)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<BellSchedule>() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onError(Throwable e) {
                            e.printStackTrace();
                            Toast.makeText(ScheduleActivity.this, "Error - cannot retrieve online bell schedule", Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onNext(BellSchedule bellSchedule) {
                            bellSchedule.sort();

                            FragmentManager fm = getFragmentManager();
                            ScheduleFragment f = (ScheduleFragment) fm.findFragmentById(R.id.activity_schedule_fragment);
                            f.setBellSchedule(bellSchedule);

                            Toast.makeText(ScheduleActivity.this, bellSchedule.toString(), Toast.LENGTH_LONG).show();
                        }
                    });
        } else {
            Toast.makeText(ScheduleActivity.this, "Cannot retrieve online bell schedule", Toast.LENGTH_LONG).show();
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

    @NonNull
    private Observable<BellSchedule> getBellSchedule(final List<String> eventsToday) {
        return Observable.create(new Observable.OnSubscribe<BellSchedule>() {
            @Override
            public void call(Subscriber<? super BellSchedule> subscriber) {
                if (mBellInitialized) return;

                BellSchedule schedule = new BellSchedule();

                RestAdapter restAdapter1 = new RestAdapter.Builder()
                        .setEndpoint("https://spreadsheets.google.com")
                        .build();
                SheetService service1 = restAdapter1.create(SheetService.class);
                RootSheetElement rootSheetElement = service1.getRootElement();
                List<Entry> entries = rootSheetElement.getFeed().getEntry();

                String findCol = null;
                for (Entry entry : entries) {
                    String cellCoord = entry.getTitle().get$t();
                    String cellRow = cellCoord.substring(1, 2);
                    String cellCol = cellCoord.substring(0, 1);
                    String cellContent = entry.getContent().get$t();
                    if (findCol == null) {
                        if (cellRow.equals("1")) {
                            //Iterating through schedule names - decide column
                            for (String eventName : eventsToday) {
                                if (cellContent.startsWith(eventName)) {
                                    schedule.name = cellContent;
                                    findCol = cellCol;
                                }
                            }
                        } else {
                            //If go through all, and none are it, choose standard schedule
                            Calendar calendar = Calendar.getInstance();
                            switch (calendar.get(Calendar.DAY_OF_WEEK)) {
                                case Calendar.MONDAY:
                                case Calendar.TUESDAY:
                                case Calendar.FRIDAY:
                                    schedule.name = "Sched. A";
                                    findCol = "B";
                                    break;
                                case Calendar.WEDNESDAY:
                                    schedule.name = "Sched. B";
                                    findCol = "C";
                                    break;
                                case Calendar.THURSDAY:
                                    schedule.name = "Sched. C";
                                    findCol = "D";
                                    break;
                                //TODO: Special case for weekends
                                default:
                                    schedule.name = "Sched. A";
                                    findCol = "B";
                                    break;
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

                mBellInitialized = true;
                subscriber.onNext(schedule);
                subscriber.onCompleted();
            }
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

    public interface CalendarService {
        @GET("/events?orderBy=startTime&singleEvents=true&fields=items")
        Observable<EventList> listEvents(@Query("key") String key);
    }

    public interface SheetService {
        @GET("/feeds/cells/1BBGLmF4GgV7SjtZyfMANa6CVxr4-GY-_O1l1ZJX6Ooo/od6/public/basic?alt=json")
        RootSheetElement getRootElement();
    }

}
