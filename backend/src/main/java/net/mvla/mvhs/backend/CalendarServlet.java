package net.mvla.mvhs.backend;

import com.firebase.client.Firebase;
import com.squareup.okhttp.ResponseBody;

import net.mvla.mvhs.backend.model.BellSchedule;
import net.mvla.mvhs.backend.model.BellSchedulePeriod;
import net.mvla.mvhs.backend.model.sheet.Entry;
import net.mvla.mvhs.backend.model.sheet.RootSheetElement;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import biweekly.Biweekly;
import biweekly.ICalendar;
import biweekly.component.VEvent;
import retrofit.GsonConverterFactory;
import retrofit.Retrofit;
import retrofit.RxJavaCallAdapterFactory;
import retrofit.http.GET;
import rx.Single;

public class CalendarServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Firebase firebase = new Firebase("https://mvhs-app.firebaseio.com/");

        Firebase scheduleRef = firebase.child("schedule");

        List<Entry> sheetEntries = getBellScheduleSheetEntries().toObservable().toBlocking().first();
        //List<Map<>> periods = new ArrayList<>();

        List<BellSchedule> bellSchedules = new ArrayList<>();
        BellSchedule schedule = new BellSchedule();

        String findCol = null;
        for (Entry cell : sheetEntries) {
            String cellCoord = cell.getTitle().get$t();
            String cellRow = cellCoord.substring(1, 2);
            String cellCol = cellCoord.substring(0, 1);
            String cellContent = cell.getContent().get$t();
            if (cellCol.equals("A")) {
                //schedule.addPeriod(cellContent);
                // scheduleRef.child("periods").child(cellRow).setValue();
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

        for (Iterator<BellSchedulePeriod> iterator = schedule.bellSchedulePeriods.iterator(); iterator.hasNext(); ) {
            BellSchedulePeriod period = iterator.next();
            if (period.startHour == 0) {
                iterator.remove();
            }
        }

        schedule.sort();

    }

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

    private Single<List<VEvent>> getEvents() {
        Retrofit restAdapter = new Retrofit.Builder()
                .baseUrl("https://www.google.com/calendar/ical/")
                //.baseUrl("http://www.mvla.net/")
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
        CalendarIcalService service = restAdapter.create(CalendarIcalService.class);
        return service.getCalendarFile()
                .flatMap(this::getEventList);
    }


    private interface CalendarIcalService {
        @GET("mvla.net_3236303434383738363838%40resource.calendar.google.com/public/basic.ics")
        Single<ResponseBody> getCalendarFile();
    }

    private interface SheetService {
        @GET("feeds/cells/1BBGLmF4GgV7SjtZyfMANa6CVxr4-GY-_O1l1ZJX6Ooo/od6/public/basic?alt=json")
        Single<RootSheetElement> getRootElement();
    }
}
