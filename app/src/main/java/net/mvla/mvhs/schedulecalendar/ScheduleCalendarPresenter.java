package net.mvla.mvhs.schedulecalendar;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.preference.PreferenceManager;

import com.crashlytics.android.Crashlytics;

import net.mvla.mvhs.BuildConfig;
import net.mvla.mvhs.MvpPresenter;
import net.mvla.mvhs.PrefUtils;
import net.mvla.mvhs.schedulecalendar.bellschedule.BellSchedule;
import net.mvla.mvhs.ui.WelcomeActivity;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;

public class ScheduleCalendarPresenter extends MvpPresenter<ScheduleCalendarView> {
    private BellSchedule mSchedule;
    private List<ScheduleCalendarRepository.Event> mEvents;
    private Calendar mSelectedDate;
    private String mError;

    public ScheduleCalendarPresenter() {
        Calendar eventTime = Calendar.getInstance();
        mSelectedDate = new GregorianCalendar();
        mSelectedDate.clear();
        mSelectedDate.set(eventTime.get(Calendar.YEAR), eventTime.get(Calendar.MONTH), eventTime.get(Calendar.DATE));
    }

    @Override
    public void attachView(ScheduleCalendarView view) {
        super.attachView(view);
    }

    public void onCreate() {
        if (isViewAttached()) {
            //noinspection ConstantConditions
            if (!PrefUtils.isWelcomeDone(getView().getContext())) {
                //noinspection ConstantConditions
                getView().getContext().startActivity(new Intent(getView().getContext(), WelcomeActivity.class));
            }

            if (mEvents == null && mSchedule == null) {
                //noinspection ConstantConditions
                getView().setLoading();
                updateBellScheduleAndCalendarEvents();
            } else if (mError != null) {
                //noinspection ConstantConditions
                getView().showErrorMessage(mError);
            } else {
                if (mEvents != null) {
                    getView().setEvents(mEvents);
                }
                if (mSchedule != null) {
                    getView().setBellSchedule(mSchedule, mSelectedDate);
                }
            }

            getView().setSelectedDate(mSelectedDate);

            if (!PrefUtils.isCalWelcomeDone(getView().getContext())) {
                getView().openCalendarView();
                PrefUtils.markCalWelcomeDone(getView().getContext());
            }
        }

        checkChangelog();
    }

    private void checkChangelog() {
        //noinspection ConstantConditions
        if (isViewAttached() && PrefUtils.isWelcomeDone(getView().getContext())) {
            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getView().getContext());
            final int currentVersion = BuildConfig.VERSION_CODE;
            if (currentVersion > prefs.getInt("changelog_version", -1)) {
                getView().showChangelog();
            }

            prefs.edit().putInt("changelog_version", currentVersion).apply();
        }
    }

    public void onDateChanged(Calendar date) {
        mSelectedDate = date;
        updateBellScheduleAndCalendarEvents();
        if (isViewAttached()) {
            getView().setSelectedDate(date);
        }
    }

    private void updateBellScheduleAndCalendarEvents() {
        if (!isDeviceOnline()) {
            if (isViewAttached()) {
                //noinspection ConstantConditions
                getView().showErrorMessage("Not online - cannot retrieve online bell schedule");
            }
            return;
        }

        if (isViewAttached()) {
            //noinspection ConstantConditions
            getView().setLoading();
        }

        //Fetch today's events (from calendar) and bell schedule sheet entries in parallel
        ScheduleCalendarRepository.getInstance(getView().getContext())
                .getEventListOnDate(mSelectedDate)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<ScheduleCalendarRepository.Event>>() {
                    @Override
                    public void onCompleted() {
                        if (isViewAttached()) {
                            getView().hideCalendarProgress();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        Crashlytics.logException(e);

                        if (isViewAttached()) {
                            mError = "Error - cannot retrieve online bell schedule.\n" + e.getMessage();
                            //noinspection ConstantConditions
                            getView().showErrorMessage(mError);
                        }
                    }

                    @Override
                    public void onNext(List<ScheduleCalendarRepository.Event> events) {
                        mEvents = events;
                        if (isViewAttached()) {
                            //noinspection ConstantConditions
                            getView().setEvents(mEvents);
                        }
                    }
                });

        ScheduleCalendarRepository.getInstance(getView().getContext())
                .getBellSchedule(mSelectedDate)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<BellSchedule>() {
                    @Override
                    public void onCompleted() {
                        if (isViewAttached()) {
                            getView().hideBellScheduleProgress();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        Crashlytics.logException(e);

                        if (isViewAttached()) {
                            mError = "Error - cannot retrieve online bell schedule.\n" + e.getMessage();
                            //noinspection ConstantConditions
                            getView().showErrorMessage(mError);
                        }
                    }

                    @Override
                    public void onNext(BellSchedule bellSchedule) {
                        mSchedule = bellSchedule;
                        if (isViewAttached()) {
                            //noinspection ConstantConditions
                            getView().setBellSchedule(bellSchedule, mSelectedDate);
                        }
                    }
                });
    }

    private boolean isDeviceOnline() {
        if (isViewAttached()) {
            //noinspection ConstantConditions
            ConnectivityManager connMgr =
                    (ConnectivityManager) getView().getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            return (networkInfo != null && networkInfo.isConnected());
        } else {
            return false;
        }
    }
}
