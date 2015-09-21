package net.mvla.mvhs.ui;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import net.mvla.mvhs.PrefUtils;
import net.mvla.mvhs.R;
import net.mvla.mvhs.model.BellSchedule;
import net.mvla.mvhs.model.BellSchedulePeriod;
import net.mvla.mvhs.model.UserPeriodInfo;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import biweekly.component.VEvent;
import biweekly.util.ICalDate;


public class ScheduleCalendarFragment extends Fragment {

    private TextView mEventsTitle;
    private TextView mBellScheduleTitle;
    private TableLayout mTableLayout;
    private LinearLayout mEventsLayout;
    private ProgressBar mProgressBar;
    private CardView mBellScheduleCard;
    private CardView mEventsCard;

    public void setErrorMessage(String error) {
        //TODO
        mProgressBar.setVisibility(View.GONE);
        mTableLayout.setVisibility(View.GONE);
        //mNameText.setText(mError);
        //mNameText.setPadding(0, 0, 0, Utils.convertDpToPx(getActivity(), 24));
        mBellScheduleTitle.setVisibility(View.GONE);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_schedule, container, false);

        // mNameText = (TextView) view.findViewById(R.id.list_item_schedule_name);
        mBellScheduleTitle = (TextView) view.findViewById(R.id.list_item_schedule_title);
        mTableLayout = (TableLayout) view.findViewById(R.id.list_item_schedule_table);
        mProgressBar = (ProgressBar) view.findViewById(R.id.list_item_schedule_progress);
        mBellScheduleCard = (CardView) view.findViewById(R.id.fragment_schedule_bell_schedule);
        mEventsCard = (CardView) view.findViewById(R.id.fragment_schedule_events);
        mEventsLayout = (LinearLayout) view.findViewById(R.id.fragment_schedule_events_linear);
        mEventsTitle = (TextView) view.findViewById(R.id.fragment_schedule_events_title);

        BellSchedule schedule = ((ScheduleCalendarActivity) getActivity()).getSchedule();
        List<VEvent> events = ((ScheduleCalendarActivity) getActivity()).getEvents();
        if (events != null && schedule != null) {
            setData(schedule, events);
        } else {
            setLoading();
        }

        return view;
    }

    public void setReady(boolean ready) {
        if (ready) {
            BellSchedule schedule = ((ScheduleCalendarActivity) getActivity()).getSchedule();
            List<VEvent> events = ((ScheduleCalendarActivity) getActivity()).getEvents();
            setData(schedule, events);
        } else {
            setLoading();
        }
    }

    private void setLoading() {
        if (mProgressBar != null) {
            mProgressBar.setVisibility(View.VISIBLE);
            mBellScheduleCard.setVisibility(View.GONE);
            mEventsCard.setVisibility(View.GONE);
        }
    }

    private void setData(@NonNull BellSchedule bellSchedule, @NonNull List<VEvent> events) {
        mBellScheduleCard.setVisibility(View.VISIBLE);
        mEventsCard.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.GONE);

        LayoutInflater layoutInflater = getActivity().getLayoutInflater();

        if (!bellSchedule.bellSchedulePeriods.isEmpty()) {
            inflateBellSchedule(bellSchedule, layoutInflater);
        } else {
            mBellScheduleTitle.setText(R.string.bell_schedule_no_school);
        }


        if (!events.isEmpty()) {
            mEventsTitle.setText(R.string.school_events);
            //CALENDAR EVENTS
            while (mEventsLayout.getChildCount() > 1) {
                mEventsLayout.removeViewAt(1);
            }
            for (VEvent event : events) {
                View tableRowSeparator = layoutInflater.inflate(R.layout.table_row_divider, mEventsLayout, false);
                mEventsLayout.addView(tableRowSeparator);
                View eventLayout = layoutInflater.inflate(R.layout.list_item_calendar_event, mEventsLayout, false);
                TextView name = (TextView) eventLayout.findViewById(R.id.list_item_calendar_event_name);
                TextView subtitle = (TextView) eventLayout.findViewById(R.id.list_item_calendar_event_subtitle);

                name.setText(event.getSummary().getValue());
                ICalDate startTime = event.getDateStart().getValue();
                ICalDate endTime = event.getDateEnd().getValue();
                String range = DateUtils.formatDateRange(getActivity(), startTime.getTime(), endTime.getTime(), DateUtils.FORMAT_SHOW_TIME);
                subtitle.setText(range);

                mEventsLayout.addView(eventLayout);
            }
        } else {
            mEventsTitle.setText(R.string.no_school_events);
        }
    }

    private void inflateBellSchedule(@NonNull BellSchedule bellSchedule, LayoutInflater layoutInflater) {
        mBellScheduleTitle.setText("Bell ");
        mBellScheduleTitle.append(bellSchedule.name.replace("Sched.", "Schedule"));

        //BELL SCHEDULE TABLE
        mTableLayout.removeAllViews();

        View divider = layoutInflater.inflate(R.layout.table_row_divider, mTableLayout, false);
        mTableLayout.addView(divider);
        View heading = layoutInflater.inflate(R.layout.table_row_heading_schedule, mTableLayout, false);
        mTableLayout.addView(heading);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        for (BellSchedulePeriod period : bellSchedule.bellSchedulePeriods) {
            View tableRowSeparator = layoutInflater.inflate(R.layout.table_row_divider, mTableLayout, false);
            mTableLayout.addView(tableRowSeparator);
            TableRow tableRow = (TableRow) layoutInflater
                    .inflate(R.layout.table_row_schedule, mTableLayout, false);

            Calendar start = Calendar.getInstance();
            Date selectedDate = ((ScheduleCalendarActivity) getActivity()).getSelectedDate().getTime();
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
                tableRow.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.primary));
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
            timeText.setText(String.format("%02d:%02d-%02d:%02d",
                    period.startHour, period.startMinute, period.endHour, period.endMinute));

            mTableLayout.addView(tableRow);
        }
    }
}
