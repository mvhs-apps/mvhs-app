package net.mvla.mvhs.schedulecalendar;

import android.content.Context;
import android.util.Pair;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;

import net.mvla.mvhs.Utils;
import net.mvla.mvhs.schedulecalendar.bellschedule.BellSchedule;
import net.mvla.mvhs.schedulecalendar.cache.DiskMemoryCache;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import okio.Buffer;
import rx.Observable;
import rx.Single;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class ScheduleCalendarRepository {
    public static final int CACHE_MAX_BYTES = 4_048_576;
    public static final int CACHE_MAX_SIZE = 5000;
    private static ScheduleCalendarRepository instance;
    private final JsonAdapter<CalendarEvents> calendarEventsAdapter;
    private final JsonAdapter<List<BellSchedule>> bellScheduleListAdapter;

    private ScheduleCalendarModel model;
    private DiskMemoryCache<List<BellSchedule>> bellSchedulesCache;
    private DiskMemoryCache<CalendarEvents> calendarEventCache;

    private Observable<List<Event>> eventListObservable;

    private ScheduleCalendarRepository(Context context) {
        model = new ScheduleCalendarModel();

        Moshi moshi = new Moshi.Builder().build();
        calendarEventsAdapter = moshi.adapter(CalendarEvents.class);
        bellScheduleListAdapter = moshi.adapter(Types.newParameterizedType(List.class, BellSchedule.class));

        bellSchedulesCache = new DiskMemoryCache<>(context, CACHE_MAX_BYTES, CACHE_MAX_SIZE, "bellSchedule", new DiskMemoryCache.Mapper<List<BellSchedule>>() {
            @Override
            public List<BellSchedule> fromStream(InputStream inputStream) throws IOException {
                return bellScheduleListAdapter.fromJson(new Buffer().readFrom(inputStream));
            }

            @Override
            public void toStream(OutputStream outputStream, List<BellSchedule> bellSchedules) throws IOException {
                Buffer sink = new Buffer();
                bellScheduleListAdapter.toJson(sink, bellSchedules);
                sink.writeTo(outputStream);
                sink.clear();
            }
        });

        calendarEventCache = new DiskMemoryCache<>(context, CACHE_MAX_BYTES, CACHE_MAX_SIZE, "calendarEvents", new DiskMemoryCache.Mapper<CalendarEvents>() {
            @Override
            public CalendarEvents fromStream(InputStream inputStream) throws IOException {
                return calendarEventsAdapter.fromJson(new Buffer().readFrom(inputStream));
            }

            @Override
            public void toStream(OutputStream outputStream, CalendarEvents item) throws IOException {
                Buffer sink = new Buffer();
                calendarEventsAdapter.toJson(sink, item);
                sink.writeTo(outputStream);
                sink.clear();
            }
        });
    }

    public static ScheduleCalendarRepository getInstance(Context context) {
        if (instance == null) {
            instance = new ScheduleCalendarRepository(context.getApplicationContext());
        }
        return instance;
    }

    public Observable<List<Event>> getEventListOnDate(Calendar selectedDate) {
        if (eventListObservable != null) {
            return eventListObservable;
        }

        Observable<List<Event>> cache = calendarEventCache.get(String.valueOf(selectedDate.getTimeInMillis()),
                calendarEvents -> calendarEvents != null)
                .flatMap(calendarEvents -> Observable.just(calendarEvents.events));

        Single<List<Event>> queryPutCache = model.getCalendarEvents().flatMap(calendarEvents -> {
            List<Event> events = calendarEvents.events;

            List<Event> eventsOnSelectedDay = new ArrayList<>();
            List<Event> eventsOnDay = new ArrayList<>();
            Calendar currDay = null;
            long lastDay = Long.MAX_VALUE;
            for (Event event : events) {
                Calendar eventTime = new GregorianCalendar();
                eventTime.setTimeInMillis(event.startTime);
                if (currDay == null) {
                    currDay = new GregorianCalendar();
                    currDay.clear();
                    currDay.set(eventTime.get(Calendar.YEAR), eventTime.get(Calendar.MONTH), eventTime.get(Calendar.DATE));
                }
                if (Utils.sameDay(eventTime, currDay)) {
                    eventsOnDay.add(event);
                } else {
                    CalendarEvents finishedEvents = new CalendarEvents();
                    finishedEvents.events = new ArrayList<>(eventsOnDay);
                    if (lastDay != Long.MAX_VALUE) {
                        for (long i = (long) (lastDay + 8.64e+7); i < currDay.getTimeInMillis(); i += 8.64e+7) {
                            CalendarEvents item = new CalendarEvents();
                            item.events = new ArrayList<>();
                            calendarEventCache.put(String.valueOf(i), item);
                        }
                    }
                    calendarEventCache.put(String.valueOf(currDay.getTimeInMillis()), finishedEvents);
                    if (Utils.sameDay(currDay, selectedDate)) {
                        eventsOnSelectedDay = new ArrayList<>(finishedEvents.events);
                    }

                    lastDay = currDay.getTimeInMillis();
                    currDay = null;
                    eventsOnDay.clear();
                }
            }

            return Single.just(eventsOnSelectedDay);
        });

        eventListObservable = Observable.concat(cache, queryPutCache.toObservable())
                .subscribeOn(Schedulers.io())
                .doOnCompleted(() -> eventListObservable = null);

        return eventListObservable;
    }

    public Observable<BellSchedule> getBellSchedule(Calendar selectedDate) {

        Observable<List<BellSchedule>> cache = bellSchedulesCache.get("0",
                bellSchedules -> bellSchedules != null
        );

        Single<List<BellSchedule>> bellSchedulesQuery = model.getBellSchedules()
                .doOnSuccess(bellSchedules -> {
                    bellSchedulesCache.put("0", bellSchedules);
                });


        return Observable.combineLatest(
                getEventListOnDate(selectedDate),
                Observable.concat(cache, bellSchedulesQuery.toObservable()),
                Pair::new
        ).subscribeOn(Schedulers.io())
                .flatMap(new Func1<Pair<List<Event>, List<BellSchedule>>, Observable<BellSchedule>>() {
                    @Override
                    public Observable<BellSchedule> call(Pair<List<Event>, List<BellSchedule>> eventsBellSchedule) {
                        List<Event> calendarEvents = eventsBellSchedule.first;
                        List<BellSchedule> bellSchedules = eventsBellSchedule.second;

                        String defaultScheduleName = null;
                        switch (selectedDate.get(java.util.Calendar.DAY_OF_WEEK)) {
                            case java.util.Calendar.MONDAY:
                            case java.util.Calendar.FRIDAY:
                                defaultScheduleName = "Sched. A";
                                break;
                            case java.util.Calendar.TUESDAY:
                                defaultScheduleName = "Tutorial Schedule";
                                break;
                            case java.util.Calendar.WEDNESDAY:
                                defaultScheduleName = "Sched. B";
                                break;
                            case java.util.Calendar.THURSDAY:
                                defaultScheduleName = "Sched. C";
                                break;
                            default:
                                //Weekend
                                break;
                        }

                        BellSchedule defaultSchedule = null;
                        BellSchedule chosen = null;
                        SimpleDateFormat format = new SimpleDateFormat("M/dd/yyyy");
                        for (BellSchedule schedule : bellSchedules) {
                            String[] scheduleNameParts = schedule.name.split("-");
                            try {
                                Date date = format.parse(scheduleNameParts[0]);
                                Calendar calendar = Calendar.getInstance();
                                calendar.setTime(date);

                                Calendar endTime = null;
                                if (scheduleNameParts.length == 2 && !scheduleNameParts[1].isEmpty()) {
                                    endTime = Calendar.getInstance();
                                    endTime.setTime(format.parse(scheduleNameParts[1]));
                                }

                                boolean inRange = endTime != null
                                        && calendar.getTimeInMillis() <= selectedDate.getTimeInMillis()
                                        && endTime.getTimeInMillis() >= selectedDate.getTimeInMillis();

                                if (Utils.sameDay(calendar, selectedDate) || inRange) {
                                    chosen = schedule;
                                    break;
                                }
                            } catch (ParseException e) {
                                //Nope.
                                for (Event event : calendarEvents) {
                                    String value = event.name;
                                    if (schedule.name.startsWith(value.split("\\|")[0])) {
                                        chosen = schedule;
                                        break;
                                    }
                                }
                                if (chosen != null) {
                                    break;
                                }

                                if (defaultScheduleName != null && schedule.name.startsWith(defaultScheduleName)) {
                                    defaultSchedule = schedule;
                                }
                            }
                        }

                        if (chosen == null) {
                            chosen = defaultSchedule;
                        }

                        return Observable.just(chosen);

                    }
                });
    }


    public static class CalendarEvents {
        public long timestamp;
        public List<Event> events;
    }

    public static class Event {
        public String name;
        public long startTime;
        public long endTime;
    }
}
