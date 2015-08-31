package net.mvla.mvhs.ui;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import net.mvla.mvhs.model.StudentPeriodInfo;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class ScheduleFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private ScheduleRecyclerAdapter mAdapter;

    public void setBellSchedule(BellSchedule schedule) {
        Schedule aSchedule = new Schedule();
        aSchedule.bellSchedule = schedule;
        mAdapter.setSchedule(aSchedule);
    }

    public void setErrorMessage(String error) {
        mAdapter.setErrorMessage(error);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, container, false);
        view.setPadding(0, 0, 0, view.getPaddingBottom());

        mRecyclerView = (RecyclerView) view;
        mRecyclerView.setHasFixedSize(true);
        mAdapter = new ScheduleRecyclerAdapter();
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        return view;
    }


    private class ScheduleRecyclerAdapter extends RecyclerView.Adapter<ScheduleRecyclerAdapter.ViewHolder> {

        private List<Schedule> mScheduleList;
        private String mError;

        public ScheduleRecyclerAdapter() {
            mScheduleList = new ArrayList<>();
            mScheduleList.add(new Schedule());
        }

        void setSchedule(Schedule schedule) {
            mScheduleList.get(0).init(schedule);
            notifyItemChanged(0);
        }

        void setErrorMessage(String error) {
            mError = error;
            notifyItemChanged(0);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = getActivity().getLayoutInflater().inflate(R.layout.list_item_schedule, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            if (mError != null) {
                holder.progressBar.setVisibility(View.GONE);
                holder.table.setVisibility(View.GONE);
                holder.name.setText(mError);
                holder.name.setPadding(0, 0, 0, Utils.convertDpToPx(getActivity(), 24));
                holder.subtitle.setVisibility(View.GONE);
                return;
            }

            Schedule schedule = mScheduleList.get(position);
            if (!schedule.initialized) {
                holder.progressBar.setVisibility(View.VISIBLE);
                holder.table.setVisibility(View.GONE);
            } else {
                Calendar now = Calendar.getInstance();
                holder.name.setText("Today - " +
                        android.text.format.DateUtils.formatDateTime(getActivity(),
                                now.getTimeInMillis(),
                                android.text.format.DateUtils.FORMAT_SHOW_WEEKDAY | android.text.format.DateUtils.FORMAT_SHOW_DATE | android.text.format.DateUtils.FORMAT_SHOW_YEAR));
                holder.subtitle.setText(schedule.bellSchedule.name);
                holder.table.setVisibility(View.VISIBLE);
                holder.progressBar.setVisibility(View.GONE);

                View heading = getActivity().getLayoutInflater().inflate(R.layout.table_row_heading_schedule, holder.table, false);
                holder.table.addView(heading);

                StudentPeriodInfo[] roomSubjectPeriods = new StudentPeriodInfo[8];
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                for (BellSchedulePeriod period : schedule.bellSchedule.bellSchedulePeriods) {
                    holder.table.addView(getActivity().getLayoutInflater().inflate(R.layout.table_row_divider, holder.table, false));
                    TableRow tableRow = (TableRow) getActivity().getLayoutInflater().inflate(R.layout.table_row_schedule, holder.table, false);

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
                        roomSubjectPeriods[i] = new StudentPeriodInfo();
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

                    holder.table.addView(tableRow);
                }
            }
        }

        @Override
        public int getItemCount() {
            return 1;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            private TextView name;
            private TextView subtitle;
            private TableLayout table;
            private ProgressBar progressBar;


            public ViewHolder(View itemView) {
                super(itemView);
                name = (TextView) itemView.findViewById(R.id.list_item_schedule_name);
                subtitle = (TextView) itemView.findViewById(R.id.list_item_schedule_subtitle);
                table = (TableLayout) itemView.findViewById(R.id.list_item_schedule_table);
                progressBar = (ProgressBar) itemView.findViewById(R.id.list_item_schedule_progress);
            }
        }
    }
}
