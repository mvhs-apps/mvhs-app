package net.mvla.mvhs.ui;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import net.mvla.mvhs.PrefUtils;
import net.mvla.mvhs.R;
import net.mvla.mvhs.Utils;
import net.mvla.mvhs.model.BellSchedule;
import net.mvla.mvhs.model.BellSchedulePeriod;
import net.mvla.mvhs.model.Schedule;
import net.mvla.mvhs.model.UserPeriodInfo;

import java.util.Calendar;


public class ScheduleFragment extends Fragment {

    public static final String STATE_BELL_SCHEDULE = "STATE_BELL_SCHEDULE";
    public static final String STATE_ERROR = "STATE_ERROR";
    private Schedule mSchedule;
    private String mError;

    private TextView mNameText;
    private TextView mSubtitleText;
    private TableLayout mTableLayout;
    private ProgressBar mProgressBar;

    public void setBellSchedule(BellSchedule bellSchedule) {
        mSchedule = new Schedule();
        mSchedule.bellSchedule = bellSchedule;

        initSchedule();
    }

    public void setErrorMessage(String error) {
        mError = error;
        mProgressBar.setVisibility(View.GONE);
        mTableLayout.setVisibility(View.GONE);
        mNameText.setText(mError);
        mNameText.setPadding(0, 0, 0, Utils.convertDpToPx(getActivity(), 24));
        mSubtitleText.setVisibility(View.GONE);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable(STATE_BELL_SCHEDULE, mSchedule);
        outState.putString(STATE_ERROR, mError);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_schedule, container, false);

        if (savedInstanceState != null) {
            mSchedule = (Schedule) savedInstanceState.getSerializable(STATE_BELL_SCHEDULE);
            mError = savedInstanceState.getString(STATE_ERROR);
        }

        mNameText = (TextView) view.findViewById(R.id.list_item_schedule_name);
        mSubtitleText = (TextView) view.findViewById(R.id.list_item_schedule_subtitle);
        mTableLayout = (TableLayout) view.findViewById(R.id.list_item_schedule_table);
        mProgressBar = (ProgressBar) view.findViewById(R.id.list_item_schedule_progress);

        if (mError != null) {
            setErrorMessage(mError);
        } else if (mSchedule == null) {
            mProgressBar.setVisibility(View.VISIBLE);
            mTableLayout.setVisibility(View.GONE);

            ((ScheduleActivity) getActivity()).retrieveBellSchedule();
        } else {
            initSchedule();
        }

        return view;
    }

    private void initSchedule() {
        Calendar now = Calendar.getInstance();
        //now.set(Calendar.MONTH, Calendar.SEPTEMBER);
        //now.set(Calendar.DAY_OF_MONTH, 23);


        mNameText.setText("Today - " +
                DateUtils.formatDateTime(getActivity(),
                        now.getTimeInMillis(),
                        DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_MONTH));
        mSubtitleText.setText(mSchedule.bellSchedule.name);
        mTableLayout.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.GONE);

        View heading = getActivity().getLayoutInflater()
                .inflate(R.layout.table_row_heading_schedule, mTableLayout, false);
        mTableLayout.addView(heading);

        UserPeriodInfo[] roomSubjectPeriods = new UserPeriodInfo[8];
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        for (BellSchedulePeriod period : mSchedule.bellSchedule.bellSchedulePeriods) {
            mTableLayout.addView(getActivity().getLayoutInflater()
                    .inflate(R.layout.table_row_divider, mTableLayout, false));
            TableRow tableRow = (TableRow) getActivity().getLayoutInflater()
                    .inflate(R.layout.table_row_schedule, mTableLayout, false);

            Calendar start = Calendar.getInstance();
            start.set(Calendar.HOUR_OF_DAY, period.startHour);
            start.set(Calendar.MINUTE, period.startMinute);
            Calendar end = Calendar.getInstance();
            end.set(Calendar.HOUR_OF_DAY, period.endHour);
            end.set(Calendar.MINUTE, period.endMinute);

            if (now.getTime().after(start.getTime()) && now.getTime().before(end.getTime())) {
                tableRow.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.primary));
            }

            TextView periodText = (TextView) tableRow.findViewById(R.id.table_row_schedule_period_name_text);
            TextView timeText = (TextView) tableRow.findViewById(R.id.table_row_schedule_period_time_text);
            TextView room = (TextView) tableRow.findViewById(R.id.table_row_schedule_room_text);
            TextView subject = (TextView) tableRow.findViewById(R.id.table_row_schedule_subject_text);

            if (period.name.substring(0, 1).matches("^-?\\d+$")) {
                //is integer (is a number period)
                int i = Integer.parseInt(period.name.substring(0, 1));
                roomSubjectPeriods[i] = new UserPeriodInfo();
                roomSubjectPeriods[i].room = preferences.getString(PrefUtils.PREF_SCHEDULE_PREFIX
                        + i + PrefUtils.PREF_SCHEDULE_ROOM, "");
                roomSubjectPeriods[i].subject = preferences.getString(PrefUtils.PREF_SCHEDULE_PREFIX
                        + i + PrefUtils.PREF_SCHEDULE_SBJCT, "");

                if (!roomSubjectPeriods[i].room.isEmpty()) {
                    room.setText(roomSubjectPeriods[i].room);
                } else {
                    room.setText(R.string.na);
                }

                if (!roomSubjectPeriods[i].subject.isEmpty()) {
                    subject.setText(roomSubjectPeriods[i].subject);
                } else {
                    subject.setText(R.string.na);
                }
            }

            periodText.setText(period.name);
            timeText.setText(String.format("%02d:%02d-%02d:%02d",
                    period.startHour, period.startMinute, period.endHour, period.endMinute));

            mTableLayout.addView(tableRow);
        }
    }
}
