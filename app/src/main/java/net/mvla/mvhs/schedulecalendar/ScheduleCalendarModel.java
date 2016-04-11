package net.mvla.mvhs.schedulecalendar;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Pair;

import net.mvla.mvhs.Utils;
import net.mvla.mvhs.schedulecalendar.bellschedule.BellSchedule;
import net.mvla.mvhs.schedulecalendar.bellschedule.BellSchedulePeriod;
import net.mvla.mvhs.schedulecalendar.sheet.Entry;
import net.mvla.mvhs.schedulecalendar.sheet.RootSheetElement;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import biweekly.Biweekly;
import biweekly.ICalendar;
import biweekly.component.VEvent;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import rx.Single;
import rx.schedulers.Schedulers;

public class ScheduleCalendarModel {

    private Context mContext;

    public ScheduleCalendarModel(Context context) {
        mContext = context;
    }

    @NonNull
    public Single<Pair<BellSchedule, List<VEvent>>> getBellScheduleAndEventsForSelectedDay(Calendar selectedDate) {
        return Single.zip(
                getEventsFromSelectedDay(selectedDate),
                getBellScheduleSheetEntries(),
                Pair::new
        )
                .subscribeOn(Schedulers.io())
                .flatMap(eventsAndSheet -> getBellScheduleAndEvents(eventsAndSheet, selectedDate));
    }

    private Single<List<VEvent>> getEventsFromSelectedDay(Calendar selectedDate) {
        Retrofit restAdapter = new Retrofit.Builder()
                .baseUrl("https://www.google.com/calendar/ical/")
                        //.baseUrl("http://www.mvla.net/")
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
        CalendarIcalService service = restAdapter.create(CalendarIcalService.class);
        return service.getCalendarFile()
                //.subscribeOn(Schedulers.io())
                .flatMap(this::getEventList)
                .flatMap(vEvents -> getEventsFromSelectedDayFromList(vEvents, selectedDate));
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
                //.subscribeOn(Schedulers.io())
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
            ICalendar calendar;
            try {
                calendar = Biweekly.parse(new ByteArrayInputStream(calBytes)).first();
            } catch (IOException e) {
                subscriber.onError(e);
                return;
            }

            subscriber.onSuccess(calendar.getEvents());
        });
    }

    @NonNull
    private Single<List<VEvent>> getEventsFromSelectedDayFromList(final List<VEvent> eventList, Calendar selectedDate) {
        return Single.create(subscriber -> {
            List<VEvent> eventsToday = new ArrayList<>();
            for (VEvent event : eventList) {
                Calendar time = new GregorianCalendar();
                time.setTime(event.getDateStart().getValue());
                if (Utils.sameDay(time, selectedDate)) {
                    eventsToday.add(event);
                }
            }
            subscriber.onSuccess(eventsToday);
        });
    }

    @NonNull
    private Single<Pair<BellSchedule, List<VEvent>>> getBellScheduleAndEvents(
            final Pair<List<VEvent>, List<Entry>> eventsAndSheet, Calendar selectedDate) {
        return Single.create(subscriber -> {
            List<VEvent> calendarEvents = eventsAndSheet.first;
            List<Entry> sheetEntries = eventsAndSheet.second;

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

                        SimpleDateFormat format = new SimpleDateFormat("M/dd/yyyy");
                        try {
                            Date date = format.parse(cellContent);
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(date);
                            if (Utils.sameDay(calendar, selectedDate)) {
                                schedule.name = cellContent;
                                findCol = cellCol;
                            }
                        } catch (ParseException e) {
                            //Nope.
                            for (Iterator<VEvent> iterator = calendarEvents.iterator(); iterator.hasNext(); ) {
                                VEvent vEvent = iterator.next();
                                String value = vEvent.getSummary().getValue();
                                if (cellContent.startsWith(value.split("\\|")[0])) {
                                    schedule.name = cellContent;
                                    findCol = cellCol;
                                    iterator.remove();
                                    break;
                                }
                            }
                        }
                    } else {
                        //If go through all, and none are it, choose standard schedule
                        switch (selectedDate.get(java.util.Calendar.DAY_OF_WEEK)) {
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

    private interface SheetService {
        @GET("feeds/cells/1BBGLmF4GgV7SjtZyfMANa6CVxr4-GY-_O1l1ZJX6Ooo/od6/public/basic?alt=json")
        Single<RootSheetElement> getRootElement();
    }

    private interface CalendarIcalService {
        @GET("mvla.net_3236303434383738363838%40resource.calendar.google.com/public/basic.ics")
        Single<ResponseBody> getCalendarFile();
    }
}
