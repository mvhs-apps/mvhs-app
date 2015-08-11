package net.mvla.mvhs.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.mvla.mvhs.PrefUtils;
import net.mvla.mvhs.R;
import net.mvla.mvhs.model.Period;

public class ScheduleActivity extends DrawerActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        FragmentManager fm = getFragmentManager();
        Fragment f = fm.findFragmentById(R.id.activity_schedule_fragment);
        if (f == null) {
            f = new ScheduleFragment();
            fm.beginTransaction()
                    .replace(R.id.activity_schedule_fragment, f)
                    .commit();
        }

        overridePendingTransition(0, 0);
    }

    @Override
    int getSelfNavDrawerItem() {
        return NAVDRAWER_ITEM_TODAYS_SCHED;
    }

    public static class ScheduleFragment extends Fragment {

        private Period[] mPeriods;
        private RecyclerView mRecyclerView;

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            mRecyclerView = (RecyclerView) inflater.inflate(R.layout.fragment_list, container, false);
            mRecyclerView.setAdapter(new ScheduleAdapter());
            mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            mRecyclerView.setHasFixedSize(true);

            mPeriods = new Period[8];
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            for (int i = 0; i < mPeriods.length; i++) {
                mPeriods[i] = new Period();
                String room = preferences.getString(PrefUtils.PREF_SCHEDULE_PREFIX + i + PrefUtils.PREF_SCHEDULE_ROOM, "");
                mPeriods[i].room = room.isEmpty() ? ScheduleSetupActivity.ROOM_EMPTY_DATA : Integer.parseInt(room);
                mPeriods[i].subject = preferences.getString(PrefUtils.PREF_SCHEDULE_PREFIX + i + PrefUtils.PREF_SCHEDULE_SBJCT, "");
            }

            return mRecyclerView;
        }

        private class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ScheduleViewHolder> {

            @Override
            public ScheduleViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(getActivity()).inflate(R.layout.list_item_sched, viewGroup, false);
                ScheduleViewHolder holder = new ScheduleViewHolder(view);

                holder.period = (TextView) view.findViewById(R.id.list_item_sched_period_text);
                holder.room = (TextView) view.findViewById(R.id.list_item_sched_room_text);
                holder.subject = (TextView) view.findViewById(R.id.list_item_sched_subject_text);

                return holder;
            }

            @Override
            public void onBindViewHolder(ScheduleViewHolder holder, final int i) {
                holder.period.setText("Period " + i + ": ");

                Period period = mPeriods[i];
                if (period.room != ScheduleSetupActivity.ROOM_EMPTY_DATA) {
                    holder.room.setText("" + period.room);
                } else {
                    holder.room.setText(R.string.na);
                }

                if (!period.subject.isEmpty()) {
                    holder.subject.setText(period.subject);
                } else {
                    holder.subject.setText(R.string.na);
                }
            }

            @Override
            public int getItemCount() {
                return 8;
            }

            class ScheduleViewHolder extends RecyclerView.ViewHolder {
                TextView period;
                TextView room;
                TextView subject;

                public ScheduleViewHolder(View itemView) {
                    super(itemView);
                }
            }
        }
    }
}
