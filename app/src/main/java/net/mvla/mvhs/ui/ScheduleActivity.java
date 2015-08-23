package net.mvla.mvhs.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
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

            Calendar calendar = Calendar.getInstance();
            String defaultSchedule = null;
            switch (calendar.get(Calendar.DAY_OF_WEEK)) {
                case Calendar.MONDAY:
                case Calendar.TUESDAY:
                case Calendar.FRIDAY:
                    defaultSchedule = "Sched. A";
                    break;
                case Calendar.WEDNESDAY:
                    defaultSchedule = "Sched. B";
                    break;
                case Calendar.THURSDAY:
                    defaultSchedule = "Sched. C";
                    break;
                //TODO: Special case for weekends
                default:
                    defaultSchedule = "Sched. A";
                    break;
            }

            CalendarService service = restAdapter.create(CalendarService.class);
            String apiKey = getString(R.string.web_api_key);
            service.listEvents(apiKey)
                    .flatMap(eventList -> {
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
                        return Observable.from(eventsToday);
                    })
                    .defaultIfEmpty(defaultSchedule)
                    .flatMap(eventName -> Observable.create(new Observable.OnSubscribe<BellSchedule>() {
                        @Override
                        public void call(Subscriber<? super BellSchedule> subscriber) {
                            if (mBellInitialized) return;

                            //TODO: Get Spreadsheet once.
                            BellSchedule schedule = new BellSchedule();

                            RestAdapter restAdapter = new RestAdapter.Builder()
                                    .setEndpoint("https://spreadsheets.google.com")
                                    .build();
                            SheetService service = restAdapter.create(SheetService.class);
                            service.getRootElement()
                                    .subscribe(rootSheetElement -> {
                                        List<Entry> entries = rootSheetElement.getFeed().getEntry();
                                        String findCol = null;
                                        for (Entry entry : entries) {
                                            String cellCoord = entry.getTitle().get$t();
                                            String cellContent = entry.getContent().get$t();
                                            if (findCol == null) {
                                                if (cellCoord.charAt(1) == '1' && cellContent.startsWith(eventName)) {
                                                    schedule.name = cellContent;
                                                    findCol = cellCoord.substring(0, 1);
                                                }
                                            } else {
                                                if (cellCoord.substring(0, 1).equals("A")) {
                                                    //Period name column
                                                    BellSchedulePeriod period = new BellSchedulePeriod();
                                                    period.name = cellContent;
                                                    schedule.bellSchedulePeriods.add(period);
                                                } else if (findCol.equals(cellCoord.substring(0, 1))) {
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

                                        if (!schedule.bellSchedulePeriods.isEmpty()) {
                                            mBellInitialized = true;
                                            subscriber.onNext(schedule);
                                            subscriber.onCompleted();
                                        } else {
                                            subscriber.onCompleted();
                                        }
                                    });
                        }
                    }))
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
        Observable<RootSheetElement> getRootElement();
    }

}
