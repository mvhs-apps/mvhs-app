package net.mvla.mvhs.ui;

import android.animation.ValueAnimator;
import android.app.FragmentManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.text.format.DateUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Pair;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.squareup.okhttp.ResponseBody;

import net.mvla.mvhs.R;
import net.mvla.mvhs.Utils;
import net.mvla.mvhs.model.BellSchedule;
import net.mvla.mvhs.model.BellSchedulePeriod;
import net.mvla.mvhs.model.sheet.Entry;
import net.mvla.mvhs.model.sheet.RootSheetElement;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import biweekly.Biweekly;
import biweekly.ICalendar;
import biweekly.component.VEvent;
import retrofit.GsonConverterFactory;
import retrofit.Retrofit;
import retrofit.RxJavaCallAdapterFactory;
import retrofit.http.GET;
import rx.Single;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func2;
import rx.schedulers.Schedulers;

public class ScheduleActivity extends DrawerActivity {

    public static final String STATE_BELL_SCHEDULE = "STATE_BELL_SCHEDULE";
    public static final String STATE_ERROR = "STATE_ERROR";
    public static final String STATE_EVENTS = "STATE_EVENTS";
    private static final String STATE_CALENDAR = "state_calendar";
    private BellSchedule mSchedule;
    private List<VEvent> mEvents;
    private String mError;
    private Calendar mSelectedDate;

    private MaterialCalendarView mCalendarView;
    private LinearLayout mAppBar;
    private LinearLayout mTitleTextBar;
    private TextView mTitle;

    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(STATE_BELL_SCHEDULE, mSchedule);
        outState.putString(STATE_ERROR, mError);
        outState.putSerializable(STATE_CALENDAR, mSelectedDate);
        Gson gson = new GsonBuilder().create();
        String events = gson.toJson(mEvents, new TypeToken<List<VEvent>>() {
        }.getType());
        outState.putString(STATE_EVENTS, events);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        if (savedInstanceState != null) {
            mSchedule = (BellSchedule) savedInstanceState.getSerializable(STATE_BELL_SCHEDULE);
            mError = savedInstanceState.getString(STATE_ERROR);
            mSelectedDate = (Calendar) savedInstanceState.getSerializable(STATE_CALENDAR);
            Gson gson = new GsonBuilder().create();
            mEvents = gson.fromJson(savedInstanceState.getString(STATE_EVENTS),
                    new TypeToken<List<VEvent>>() {
                    }.getType());
        }

        mAppBar = (LinearLayout) findViewById(R.id.activity_schedule_appbar);
        mCalendarView = (MaterialCalendarView) findViewById(R.id.activity_schedule_calendar);
        mTitleTextBar = (LinearLayout) findViewById(R.id.activity_schedule_title_linear);
        mTitle = (TextView) findViewById(R.id.activity_schedule_title_text);

        mTitleTextBar.setOnClickListener(v -> {
            animateToggleCalendar();
        });

        mCalendarView.addDecorators(new DayViewDecorator() {
            @Override
            public boolean shouldDecorate(CalendarDay day) {
                return true;
            }

            @Override
            public void decorate(DayViewFacade view) {
                view.setSelectionDrawable(ContextCompat.getDrawable(ScheduleActivity.this, R.drawable.calendar_selector));
            }
        }, new DayViewDecorator() {
            @Override
            public boolean shouldDecorate(CalendarDay day) {
                Calendar cal = Calendar.getInstance();
                day.copyTo(cal);
                return Utils.sameDay(Calendar.getInstance(), cal);
            }

            @Override
            public void decorate(DayViewFacade view) {
                ForegroundColorSpan span = new ForegroundColorSpan(ContextCompat.getColor(ScheduleActivity.this, R.color.primary_text_default_material_dark));
                view.addSpan(span);
                view.setBackgroundDrawable(ContextCompat.getDrawable(ScheduleActivity.this, R.drawable.calendar_today_selected));
            }
        });

        mCalendarView.setOnDateChangedListener((widget, date) -> {
            if (date != null) {
                mSelectedDate = date.getCalendar();
                updateTitle();
                animateToggleCalendar();

                FragmentManager fm = getFragmentManager();
                ScheduleFragment f = (ScheduleFragment) fm.findFragmentById(R.id.activity_schedule_fragment);
                updateBellScheduleAndCalendarEvents(f);
            }
        });

        //mSelectedDate.set(Calendar.MONTH, Calendar.OCTOBER);
        //mSelectedDate.set(Calendar.DAY_OF_MONTH, 5);
        if (mSelectedDate == null) {
            mSelectedDate = Calendar.getInstance();
        }
        mCalendarView.setSelectedDate(mSelectedDate);

        updateTitle();

        FragmentManager fm = getFragmentManager();
        ScheduleFragment f = (ScheduleFragment) fm.findFragmentById(R.id.activity_schedule_fragment);
        if (f == null) {
            f = new ScheduleFragment();
            fm.beginTransaction()
                    .replace(R.id.activity_schedule_fragment, f)
                    .commit();
        }

        if (savedInstanceState == null) {
            updateBellScheduleAndCalendarEvents(f);
        } else {
            f.setData(mSchedule, mEvents);
        }

        overridePendingTransition(0, 0);
    }

    private void animateToggleCalendar() {
        ValueAnimator animator;
        if (mAppBar.getHeight() == mTitleTextBar.getHeight()) {
            animator = ValueAnimator.ofInt(mTitleTextBar.getHeight(),
                    Utils.convertDpToPx(this, 48 * 8) + mTitleTextBar.getHeight());

        } else {
            animator = ValueAnimator.ofInt(mAppBar.getHeight(), mTitleTextBar.getHeight());
        }

        animator.addUpdateListener(animation -> {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mAppBar.getLayoutParams();
            params.height = ((int) animation.getAnimatedValue());
            mAppBar.setLayoutParams(params);
        });

        animator.start();
    }

    public void updateTitle() {
        mTitle.setText(DateUtils.formatDateTime(
                this, mSelectedDate.getTimeInMillis(),
                DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_MONTH));
    }

    public void updateBellScheduleAndCalendarEvents(ScheduleFragment f) {
        if (isDeviceOnline()) {
            f.setLoading();

            //Fetch today's events (from calendar) and bell schedule sheet entries in parallel
            Single.zip(
                    getEventsFromSelectedDay(),
                    getBellScheduleSheetEntries(),
                    (Func2<List<VEvent>, List<Entry>, Pair<List<VEvent>, List<Entry>>>) Pair::new
            )
                    .flatMap(this::getBellScheduleAndEvents)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<Pair<BellSchedule, List<VEvent>>>() {
                        @Override
                        public void onCompleted() {
                        }

                        @Override
                        public void onError(Throwable e) {
                            e.printStackTrace();

                            FragmentManager fm = getFragmentManager();
                            ScheduleFragment f = (ScheduleFragment)
                                    fm.findFragmentById(R.id.activity_schedule_fragment);
                            f.setErrorMessage("Error - cannot retrieve online bell schedule.");
                        }

                        @Override
                        public void onNext(Pair<BellSchedule, List<VEvent>> bellScheduleEventsPair) {
                            mEvents = bellScheduleEventsPair.second;
                            mSchedule = bellScheduleEventsPair.first;

                            f.setData(mSchedule, mEvents);
                        }
                    });
        } else {
            Toast.makeText(ScheduleActivity.this,
                    "Not online - cannot retrieve online bell schedule", Toast.LENGTH_LONG).show();
        }
    }

    private Single<List<VEvent>> getEventsFromSelectedDay() {
        Retrofit restAdapter = new Retrofit.Builder()
                .baseUrl("https://www.google.com/calendar/ical/")
                        //.baseUrl("http://www.mvla.net/")
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
        CalendarIcalService service = restAdapter.create(CalendarIcalService.class);
        return service.getCalendarFile()
                .observeOn(Schedulers.io())
                .flatMap(this::getEventList)
                .flatMap(this::getEventsFromSelectedDayFromList);
    }

    @NonNull
    private Single<List<Entry>> getBellScheduleSheetEntries() {
        Retrofit restAdapter = new Retrofit.Builder()
                .baseUrl("https://spreadsheets.google.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
        SheetService service = restAdapter.create(SheetService.class);

        return service.getRootElement()
                .observeOn(Schedulers.io())
                .flatMap(rootSheetElement -> Single.create(singleSubscriber -> {
                    singleSubscriber.onSuccess(rootSheetElement.getFeed().getEntry());
                }));
    }

    @NonNull
    private Single<List<VEvent>> getEventList(ResponseBody calendarResponse) {
        return Single.create(subscriber -> {
            byte[] calBytes = new byte[0];
            try {
                calBytes = calendarResponse.bytes();
            } catch (IOException e) {
                subscriber.onError(e);
            }
            File file = new File(getCacheDir(), "basic.ics");
            Utils.saveBytesToFile(calBytes, file.getPath());
            ICalendar calendar;
            try {
                calendar = Biweekly.parse(file).first();
            } catch (IOException e) {
                subscriber.onError(e);
                return;
            }

            subscriber.onSuccess(calendar.getEvents());
        });
    }

    @NonNull
    private Single<List<VEvent>> getEventsFromSelectedDayFromList(final List<VEvent> eventList) {
        return Single.create(subscriber -> {
            List<VEvent> eventsToday = new ArrayList<>();
            for (VEvent event : eventList) {
                Calendar time = new GregorianCalendar();
                time.setTime(event.getDateStart().getValue());
                if (Utils.sameDay(time, mSelectedDate)) {
                    eventsToday.add(event);
                }
            }
            subscriber.onSuccess(eventsToday);
        });
    }

    @NonNull
    private Single<Pair<BellSchedule, List<VEvent>>> getBellScheduleAndEvents(
            final Pair<List<VEvent>, List<Entry>> eventsAndBellSched) {
        return Single.create(subscriber -> {
            List<VEvent> calendarEvents = eventsAndBellSched.first;
            List<Entry> sheetEntries = eventsAndBellSched.second;

            BellSchedule schedule = new BellSchedule();

            String findCol = null;
            for (Entry cell : sheetEntries) {
                String cellCoord = cell.getTitle().get$t();
                String cellRow = cellCoord.substring(1, 2);
                String cellCol = cellCoord.substring(0, 1);
                String cellContent = cell.getContent().get$t();
                if (findCol == null) {
                    if (cellRow.equals("1")) {
                        //Iterating through schedule names - decide column
                        for (Iterator<VEvent> iterator = calendarEvents.iterator(); iterator.hasNext(); ) {
                            VEvent vEvent = iterator.next();
                            String value = vEvent.getSummary().getValue();
                            if (cellContent.startsWith(value.split("\\|")[0])) {
                                schedule.name = cellContent;
                                findCol = cellCol;
                                iterator.remove();
                            }
                        }
                    } else {
                        //If go through all, and none are it, choose standard schedule
                        switch (mSelectedDate.get(java.util.Calendar.DAY_OF_WEEK)) {
                            case java.util.Calendar.MONDAY:
                            case java.util.Calendar.TUESDAY:
                            case java.util.Calendar.FRIDAY:
                                schedule.name = "Sched. A: Regular";
                                findCol = "B";
                                break;
                            case java.util.Calendar.WEDNESDAY:
                                schedule.name = "Sched. B: Wed. Block";
                                findCol = "C";
                                break;
                            case java.util.Calendar.THURSDAY:
                                schedule.name = "Sched. C: Thurs. Block";
                                findCol = "D";
                                break;
                            default:
                                //Weekend
                                break;
                        }
                        if (schedule.name == null) {
                            break;
                        }
                        //cellCol is now at "A" - we're in second row
                        schedule.addPeriod(cellContent);
                    }
                } else {
                    if (cellCol.equals("A")) {
                        schedule.addPeriod(cellContent);
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

            schedule.sort();

            subscriber.onSuccess(new Pair<>(schedule, calendarEvents));
        });
    }

    @Override
    int getSelfNavDrawerItem() {
        return NAVDRAWER_ITEM_TODAYS_SCHED;
    }

    @Override
    protected String getToolbarTitle(String navDrawerString) {
        return "";
    }

    public interface SheetService {
        @GET("feeds/cells/1BBGLmF4GgV7SjtZyfMANa6CVxr4-GY-_O1l1ZJX6Ooo/od6/public/basic?alt=json")
        Single<RootSheetElement> getRootElement();
    }

    public interface CalendarIcalService {
        //@GET("rss.cfm?a=Events&s=MVHS&format=ical")
        @GET("mvla.net_3236303434383738363838%40resource.calendar.google.com/public/basic.ics")
        Single<ResponseBody> getCalendarFile();
    }

}
