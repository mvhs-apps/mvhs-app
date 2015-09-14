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
import net.mvla.mvhs.model.UserPeriodInfo;

import java.util.Calendar;


public class ScheduleFragment extends Fragment {

    public static final String STATE_BELL_SCHEDULE = "STATE_BELL_SCHEDULE";
    public static final String STATE_ERROR = "STATE_ERROR";
    private BellSchedule mSchedule;
    private String mError;

    private TextView mNameText;
    private TextView mSubtitleText;
    private TableLayout mTableLayout;
    private ProgressBar mProgressBar;

    public void setBellSchedule(BellSchedule bellSchedule) {
        mSchedule = bellSchedule;

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
            mSchedule = (BellSchedule) savedInstanceState.getSerializable(STATE_BELL_SCHEDULE);
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
        //now.set(Calendar.MONTH, Calendar.OCTOBER);
        //now.set(Calendar.DAY_OF_MONTH, 5);


        mNameText.setText("Today - " +
                DateUtils.formatDateTime(getActivity(),
                        now.getTimeInMillis(),
                        DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_MONTH));
        mSubtitleText.setText(mSchedule.name);
        mTableLayout.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.GONE);

        View heading = getActivity().getLayoutInflater()
                .inflate(R.layout.table_row_heading_schedule, mTableLayout, false);
        mTableLayout.addView(heading);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        for (BellSchedulePeriod period : mSchedule.bellSchedulePeriods) {
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
