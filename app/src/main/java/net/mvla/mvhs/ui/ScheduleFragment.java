package net.mvla.mvhs.ui;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import net.mvla.mvhs.PrefUtils;
import net.mvla.mvhs.R;
import net.mvla.mvhs.model.BellSchedule;
import net.mvla.mvhs.model.BellSchedulePeriod;
import net.mvla.mvhs.model.Period;


public class ScheduleFragment extends Fragment {

    private TableLayout mTableLayout;
    private ProgressBar mProgressBar;

    public void setBellSchedule(BellSchedule schedule) {

        View heading = getActivity().getLayoutInflater().inflate(R.layout.table_row_heading_schedule, mTableLayout, false);
        mTableLayout.addView(heading);

        Period[] roomSubjectPeriods = new Period[8];
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        for (BellSchedulePeriod period : schedule.bellSchedulePeriods) {
            mTableLayout.addView(getActivity().getLayoutInflater().inflate(R.layout.table_row_divider, mTableLayout, false));
            TableRow tableRow = (TableRow) getActivity().getLayoutInflater().inflate(R.layout.table_row_schedule, mTableLayout, false);

            TextView periodText = (TextView) tableRow.findViewById(R.id.list_item_sched_period_name_text);
            TextView timeText = (TextView) tableRow.findViewById(R.id.list_item_sched_period_time_text);
            TextView room = (TextView) tableRow.findViewById(R.id.list_item_sched_room_text);
            TextView subject = (TextView) tableRow.findViewById(R.id.list_item_sched_subject_text);

            if (period.name.substring(0, 1).matches("^-?\\d+$")) {
                //is integer (is a number period)
                int i = Integer.parseInt(period.name.substring(0, 1));
                roomSubjectPeriods[i] = new Period();
                roomSubjectPeriods[i].room = preferences.getString(PrefUtils.PREF_SCHEDULE_PREFIX + i + PrefUtils.PREF_SCHEDULE_ROOM, "");
                roomSubjectPeriods[i].subject = preferences.getString(PrefUtils.PREF_SCHEDULE_PREFIX + i + PrefUtils.PREF_SCHEDULE_SBJCT, "");

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
            timeText.setText(String.format("%02d:%02d-%02d:%02d", period.startHour, period.startMinute, period.endHour, period.endMinute));

            mTableLayout.addView(tableRow);
        }


        mTableLayout.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.GONE);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_schedule, container, false);
        mTableLayout = (TableLayout) view.findViewById(R.id.fragment_schedule_table);
        mProgressBar = (ProgressBar) view.findViewById(R.id.fragment_schedule_loading_progress);

        mProgressBar.setVisibility(View.VISIBLE);
        mTableLayout.setVisibility(View.GONE);

        return view;
    }
}
