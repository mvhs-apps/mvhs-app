package net.mvla.mvhs.schedulecalendar;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.widget.CardView;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import net.mvla.mvhs.MvpPresenterHolder;
import net.mvla.mvhs.PrefUtils;
import net.mvla.mvhs.R;
import net.mvla.mvhs.Utils;
import net.mvla.mvhs.schedulecalendar.bellschedule.BellSchedule;
import net.mvla.mvhs.schedulecalendar.bellschedule.BellSchedulePeriod;
import net.mvla.mvhs.schedulecalendar.bellschedule.UserPeriodInfo;
import net.mvla.mvhs.ui.DrawerActivity;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ScheduleCalendarActivity extends DrawerActivity implements ScheduleCalendarView {

    @BindView(R.id.activity_schedule_calendar)
    MaterialCalendarView mCalendarView;
    @BindView(R.id.activity_schedule_appbar)
    LinearLayout mAppBar;
    @BindView(R.id.activity_schedule_title_linear)
    LinearLayout mTitleTextBar;
    @BindView(R.id.activity_schedule_title_text)
    TextView mTitle;
    @BindView(R.id.activity_schedule_calendar_dropdown_image)
    ImageView mCalendarDropdownImage;

    @BindView(R.id.fragment_schedule_events_title)
    TextView mEventsTitle;
    @BindView(R.id.list_item_schedule_title)
    TextView mBellScheduleTitle;
    @BindView(R.id.list_item_schedule_table)
    TableLayout mTableLayout;
    @BindView(R.id.schedule_events_linear)
    LinearLayout mEventsLayout;
    @BindView(R.id.calendar_events_progress)
    ProgressBar mCalendarProgressBar;
    @BindView(R.id.bell_schedule_progress)
    ProgressBar mBellScheduleProgressBar;
    @BindView(R.id.fragment_schedule_bell_schedule)
    CardView mBellScheduleCard;
    @BindView(R.id.schedule_events)
    CardView mEventsCard;
    @BindView(R.id.schedule_disclaimer)
    TextView mDisclaimer;

    private ScheduleCalendarPresenter mPresenter;

    @Override
    public void showCalendarError(String error) {
        mCalendarProgressBar.setVisibility(View.GONE);
        mEventsTitle.setText(error);
    }

    @Override
    public void showBellScheduleError(String error) {
        mBellScheduleProgressBar.setVisibility(View.GONE);
        mBellScheduleTitle.setText(error);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPresenter.detachView();
    }

    public void setLoading() {
        mCalendarProgressBar.setVisibility(View.VISIBLE);
        mBellScheduleProgressBar.setVisibility(View.VISIBLE);
        mEventsTitle.setText(R.string.loading);
        mBellScheduleTitle.setText(R.string.loading);
        while (mEventsLayout.getChildCount() > 1) {
            mEventsLayout.removeViewAt(1);
        }
        mTableLayout.removeAllViews();
    }

    @Override
    public void setBellSchedule(@NonNull BellSchedule bellSchedule, Calendar selectedCalDate) {
        LayoutInflater layoutInflater = getLayoutInflater();

        mTableLayout.removeAllViews();
        if (!bellSchedule.bellSchedulePeriods.isEmpty()) {
            inflateBellSchedule(bellSchedule, layoutInflater, selectedCalDate);
        } else {
            mBellScheduleTitle.setText(R.string.bell_schedule_no_school);
        }
    }

    @Override
    public void hideBellScheduleProgress() {
        mBellScheduleProgressBar.setVisibility(View.GONE);
    }

    @Override
    public void setSelectedDate(Calendar date) {
        mCalendarView.setDateSelected(date, true);
        mTitle.setText(DateUtils.formatDateTime(
                this, date.getTimeInMillis(), DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_MONTH));
    }

    @Override
    public void setEvents(@NonNull List<ScheduleCalendarRepository.Event> events) {
        LayoutInflater layoutInflater = getLayoutInflater();

        while (mEventsLayout.getChildCount() > 1) {
            mEventsLayout.removeViewAt(1);
        }
        if (!events.isEmpty()) {
            mEventsTitle.setText(R.string.school_events);
            //CALENDAR EVENTS
            for (ScheduleCalendarRepository.Event event : events) {
                View tableRowSeparator = layoutInflater.inflate(R.layout.table_row_divider, mEventsLayout, false);
                mEventsLayout.addView(tableRowSeparator);
                View eventLayout = layoutInflater.inflate(R.layout.list_item_calendar_event, mEventsLayout, false);
                TextView name = (TextView) eventLayout.findViewById(R.id.list_item_calendar_event_name);
                TextView subtitle = (TextView) eventLayout.findViewById(R.id.list_item_calendar_event_subtitle);

                name.setText(event.name);
                String range = DateUtils.formatDateRange(this, event.startTime, event.endTime, DateUtils.FORMAT_SHOW_TIME);
                subtitle.setText(range);

                mEventsLayout.addView(eventLayout);
            }
        } else {
            mEventsTitle.setText(R.string.no_school_events);
        }
    }

    @Override
    public void hideCalendarProgress() {
        mCalendarProgressBar.setVisibility(View.GONE);
    }

    private void inflateBellSchedule(@NonNull BellSchedule bellSchedule, LayoutInflater layoutInflater, Calendar selectedCalDate) {
        mBellScheduleTitle.setText(bellSchedule.name.replace("Sched.", "Bell Schedule"));

        //BELL SCHEDULE TABLE

        View divider = layoutInflater.inflate(R.layout.table_row_divider, mTableLayout, false);
        mTableLayout.addView(divider);
        View heading = layoutInflater.inflate(R.layout.table_row_heading_schedule, mTableLayout, false);
        mTableLayout.addView(heading);

        SharedPreferences preferences = android.preference.PreferenceManager.getDefaultSharedPreferences(this);
        for (BellSchedulePeriod period : bellSchedule.bellSchedulePeriods) {
            inflatePeriod(layoutInflater, preferences, period, selectedCalDate);
        }
    }

    @SuppressLint("SetTextI18n")
    private void inflatePeriod(LayoutInflater layoutInflater, SharedPreferences preferences, BellSchedulePeriod period, Calendar selectedCalDate) {
        View tableRowSeparator = layoutInflater.inflate(R.layout.table_row_divider, mTableLayout, false);
        mTableLayout.addView(tableRowSeparator);
        TableRow tableRow = (TableRow) layoutInflater.inflate(R.layout.table_row_schedule, mTableLayout, false);

        Calendar start = Calendar.getInstance();
        Date selectedDate = selectedCalDate.getTime();
        start.setTime(selectedDate);
        start.set(Calendar.HOUR_OF_DAY, period.startHour);
        start.set(Calendar.MINUTE, period.startMinute);
        Calendar end = Calendar.getInstance();
        end.setTime(selectedDate);
        end.set(Calendar.HOUR_OF_DAY, period.endHour);
        end.set(Calendar.MINUTE, period.endMinute);

        Calendar now = Calendar.getInstance();
        if (now.getTime().after(start.getTime()) && now.getTime().before(end.getTime())) {
            //In this period right now
            tableRow.setBackgroundColor(ContextCompat.getColor(this, R.color.primary));
        }

        TextView periodText = (TextView) tableRow.findViewById(R.id.table_row_schedule_period_name_text);
        TextView timeText = (TextView) tableRow.findViewById(R.id.table_row_schedule_period_time_text);
        TextView room = (TextView) tableRow.findViewById(R.id.table_row_schedule_room_text);
        TextView subject = (TextView) tableRow.findViewById(R.id.table_row_schedule_subject_text);

        if (period.name.substring(0, 1).matches("^-?\\d+$")) {
            //is integer (is a number period)
            int firstChar = Integer.parseInt(period.name.substring(0, 1));

            UserPeriodInfo info = new UserPeriodInfo();
            info.room = preferences.getString(PrefUtils.PREF_SCHEDULE_PREFIX
                    + firstChar + PrefUtils.PREF_SCHEDULE_ROOM, "");
            info.subject = preferences.getString(PrefUtils.PREF_SCHEDULE_PREFIX
                    + firstChar + PrefUtils.PREF_SCHEDULE_SBJCT, "");


            boolean secondCharA = period.name.length() > 1 && period.name.substring(1, 2).equals("A");
            boolean secondCharB = period.name.length() > 1 && period.name.substring(1, 2).equals("B");
            boolean rallyB = preferences.getBoolean(PrefUtils.PREF_SCHEDULE_RALLY_B, false);
            if (firstChar == 2 && (secondCharA || secondCharB) && ((rallyB && secondCharB) || (!rallyB && secondCharA))) {
                //Rally schedule and this period is their rally
                room.setText(R.string.gym);
                subject.setText(String.format(getString(R.string.rally_blank), rallyB ? "B" : "A"));
            } else {
                room.setText(!info.room.isEmpty() ? info.room : "");
                subject.setText(!info.subject.isEmpty() ? info.subject : "");
            }
        }

        periodText.setText(period.name);

        timeText.setText(Utils.formatTime(start.getTime()) + "-" + Utils.formatTime(end.getTime()));

        mTableLayout.addView(tableRow);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        MvpPresenterHolder.getInstance().putPresenter(ScheduleCalendarPresenter.class, mPresenter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_MVHSApp_Light_WithNavDrawer);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_calendar);

        ButterKnife.bind(this);

        mDisclaimer.setMovementMethod(LinkMovementMethod.getInstance());

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
                view.setSelectionDrawable(ContextCompat
                        .getDrawable(ScheduleCalendarActivity.this, R.drawable.calendar_selector));
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
                ForegroundColorSpan span = new ForegroundColorSpan(ContextCompat
                        .getColor(ScheduleCalendarActivity.this, R.color.primary_text_default_material_dark));
                view.addSpan(span);
                view.setBackgroundDrawable(ContextCompat
                        .getDrawable(ScheduleCalendarActivity.this, R.drawable.calendar_today_selected));
            }
        });

        mCalendarView.setOnDateChangedListener((widget, date, selected) -> {
            mPresenter.onDateChanged(date.getCalendar());
            ScheduleCalendarActivity.this.animateToggleCalendar();
        });
        mCalendarView.setDynamicHeightEnabled(true);

        mPresenter = MvpPresenterHolder.getInstance().getPresenter(ScheduleCalendarPresenter.class);
        if (mPresenter == null) {
            mPresenter = new ScheduleCalendarPresenter();
        }
        mPresenter.attachView(this);
        mPresenter.onCreate();

        overridePendingTransition(0, 0);
    }

    public void openCalendarView() {
        ViewGroup.LayoutParams layoutParams = mAppBar.getLayoutParams();
        layoutParams.height = Utils.convertDpToPx(this, 48 * 8)
                + getResources().getDimensionPixelSize(R.dimen.abc_action_bar_default_height_material);
        mAppBar.setLayoutParams(layoutParams);
        mCalendarDropdownImage.setRotation(180);
    }

    private void animateToggleCalendar() {
        AnimatorSet set = new AnimatorSet();

        ValueAnimator animator;
        boolean needExpand = mAppBar.getHeight() == mTitleTextBar.getHeight();
        if (needExpand) {
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

        animator.setInterpolator(new FastOutSlowInInterpolator());

        ObjectAnimator animatorSpin;
        if (needExpand) {
            animatorSpin = ObjectAnimator.ofFloat(mCalendarDropdownImage, ImageView.ROTATION, 180);
        } else {
            animatorSpin = ObjectAnimator.ofFloat(mCalendarDropdownImage, ImageView.ROTATION, 0f);
        }

        animatorSpin.setInterpolator(new FastOutSlowInInterpolator());

        set.setDuration(300);
        set.play(animator).with(animatorSpin);
        set.start();
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    protected int getSelfNavDrawerItem() {
        return NAVDRAWER_ITEM_TODAYS_SCHED;
    }

    @Override
    protected String getToolbarTitle(String navDrawerString) {
        return "";
    }

}
