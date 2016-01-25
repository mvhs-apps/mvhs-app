package net.mvla.mvhs.schedulecalendar;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.preference.PreferenceManager;
import android.util.Pair;

import com.crashlytics.android.Crashlytics;

import net.mvla.mvhs.BuildConfig;
import net.mvla.mvhs.MvpPresenter;
import net.mvla.mvhs.PrefUtils;
import net.mvla.mvhs.schedulecalendar.bellschedule.BellSchedule;
import net.mvla.mvhs.ui.WelcomeActivity;

import java.util.Calendar;
import java.util.List;

import biweekly.component.VEvent;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;

public class ScheduleCalendarPresenter extends MvpPresenter<ScheduleCalendarView> {
    private BellSchedule mSchedule;
    private List<VEvent> mEvents;
    private Calendar mSelectedDate;
    private String mError;

    private ScheduleCalendarModel mModel;

    public ScheduleCalendarPresenter() {
        mSelectedDate = Calendar.getInstance();
    }

    @Override
    public void attachView(ScheduleCalendarView view) {
        super.attachView(view);
        mModel = new ScheduleCalendarModel(view.getContext());
    }

    public void onCreate() {
        if (isViewAttached()) {
            //noinspection ConstantConditions
            if (!PrefUtils.isWelcomeDone(getView().getContext())) {
                //noinspection ConstantConditions
                getView().getContext().startActivity(new Intent(getView().getContext(), WelcomeActivity.class));
            }

            if (mEvents != null && mSchedule != null) {
                //noinspection ConstantConditions
                getView().setData(mSchedule, mEvents, mSelectedDate);
            } else if (mError != null) {
                //noinspection ConstantConditions
                getView().showErrorMessage(mError);
            } else {
                //noinspection ConstantConditions
                getView().setLoading();
                updateBellScheduleAndCalendarEvents();
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
        if (isDeviceOnline()) {
            if (isViewAttached()) {
                //noinspection ConstantConditions
                getView().setLoading();
            }

            //Fetch today's events (from calendar) and bell schedule sheet entries in parallel
            mModel.getBellScheduleAndEventsForSelectedDay(mSelectedDate)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<Pair<BellSchedule, List<VEvent>>>() {
                        @Override
                        public void onCompleted() {
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
                        public void onNext(Pair<BellSchedule, List<VEvent>> bellScheduleEventsPair) {
                            mSchedule = bellScheduleEventsPair.first;
                            mEvents = bellScheduleEventsPair.second;
                            if (isViewAttached()) {
                                //noinspection ConstantConditions
                                getView().setData(mSchedule, mEvents, mSelectedDate);
                            }
                        }
                    });
        } else {
            if (isViewAttached()) {
                //noinspection ConstantConditions
                getView().showErrorMessage("Not online - cannot retrieve online bell schedule");
            }
        }
    }

    private boolean isDeviceOnline() {
        if (isViewAttached()) {
            //noinspection ConstantConditions
            ConnectivityManager connMgr =
                    (ConnectivityManager) getView().getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            return (networkInfo != null && networkInfo.isConnected());
        } else {
            return false;
        }
    }
}
