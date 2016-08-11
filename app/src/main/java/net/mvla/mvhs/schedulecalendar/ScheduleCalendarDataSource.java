package net.mvla.mvhs.schedulecalendar;

import android.support.annotation.NonNull;

import net.mvla.mvhs.schedulecalendar.bellschedule.BellSchedule;
import net.mvla.mvhs.schedulecalendar.bellschedule.BellSchedulePeriod;
import net.mvla.mvhs.schedulecalendar.sheet.Entry;
import net.mvla.mvhs.schedulecalendar.sheet.RootSheetElement;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
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

public class ScheduleCalendarDataSource {

    @NonNull
    Single<ScheduleCalendarRepository.CalendarEvents> getCalendarEvents() {
        Retrofit restAdapter = new Retrofit.Builder()
                .baseUrl("https://www.google.com/calendar/ical/")
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
        CalendarIcalService service = restAdapter.create(CalendarIcalService.class);
        return service.getCalendarFile()
                .flatMap(this::getEventListFromResponse)
                .map(vEvents -> {
                    ScheduleCalendarRepository.CalendarEvents events = new ScheduleCalendarRepository.CalendarEvents();
                    events.events = vEvents;
                    events.timestamp = System.currentTimeMillis();
                    return events;
                });
    }

    Single<List<BellSchedule>> getBellSchedules() {
        return getBellScheduleSheet()
                .flatMap(this::parseSheetBellSchedules);
    }

    @NonNull
    private Single<List<Entry>> getBellScheduleSheet() {
        Retrofit restAdapter = new Retrofit.Builder()
                .baseUrl("https://spreadsheets.google.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
        SheetService service = restAdapter.create(SheetService.class);

        return service.getRootElement()
                .flatMap(rootSheetElement -> Single.create(singleSubscriber -> {
                    singleSubscriber.onSuccess(rootSheetElement.getFeed().getEntry());
                }));
    }

    @NonNull
    private Single<List<ScheduleCalendarRepository.Event>> getEventListFromResponse(ResponseBody calendarResponse) {
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

            List<ScheduleCalendarRepository.Event> events = new ArrayList<>();
            for (VEvent vEvent : calendar.getEvents()) {
                ScheduleCalendarRepository.Event event = new ScheduleCalendarRepository.Event();
                event.startTime = vEvent.getDateStart().getValue().getTime();
                event.endTime = vEvent.getDateEnd().getValue().getTime();
                event.name = vEvent.getSummary().getValue();
                events.add(event);
            }

            Collections.sort(events, (e1, e2) -> Double.compare(e1.startTime, e2.startTime));
            subscriber.onSuccess(events);
        });
    }

    @NonNull
    private Single<List<BellSchedule>> parseSheetBellSchedules(List<Entry> sheetEntries) {
        return Single.create(subscriber -> {
            List<BellSchedule> schedules = new ArrayList<>();

            String periodName = null;
            for (Entry cell : sheetEntries) {
                String cellCoord = cell.getTitle().get$t();
                String cellRow = cellCoord.substring(1);
                String cellCol = cellCoord.substring(0, 1);
                String cellContent = cell.getContent().get$t();
                if (cellRow.equals("1")) {
                    BellSchedule schedule = new BellSchedule();
                    schedule.name = cellContent;
                    schedules.add(schedule);
                } else {
                    if (cellCol.equals("A")) {
                        periodName = cellContent;
                    } else {
                        BellSchedule schedule = schedules.get(cellCol.charAt(0) - 66);
                        BellSchedulePeriod period = new BellSchedulePeriod();
                        String[] time = cellContent.split("[\\s:\\-]");
                        if (time.length == 4) {
                            period.startHour = Integer.parseInt(time[0]);
                            period.startMinute = Integer.parseInt(time[1]);
                            period.endHour = Integer.parseInt(time[2]);
                            period.endMinute = Integer.parseInt(time[3]);
                        }

                        if (period.startHour != 0) {
                            period.name = periodName;
                            schedule.addPeriod(period);
                        }
                    }
                }
            }

            for (BellSchedule schedule : schedules) {
                schedule.sort();
            }

            subscriber.onSuccess(schedules);
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
