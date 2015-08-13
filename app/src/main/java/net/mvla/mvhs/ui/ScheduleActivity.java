package net.mvla.mvhs.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.model.Events;

import net.mvla.mvhs.PrefUtils;
import net.mvla.mvhs.R;
import net.mvla.mvhs.model.Period;

import java.io.IOException;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class ScheduleActivity extends DrawerActivity {

    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        Observable.create(new Observable.OnSubscribe<Events>() {
            @Override
            public void call(Subscriber<? super Events> subscriber) {
                HttpTransport transport = new NetHttpTransport();
                JsonFactory factory = GsonFactory.getDefaultInstance();
                com.google.api.services.calendar.Calendar service =
                        new com.google.api.services.calendar.Calendar.Builder(transport, factory, null).build();
                try {
                    Events calendar = service.events().list("kci7ig724mqv7ps1mkn54cc9rs@group.calendar.google.com")
                            .setOrderBy("startTime").execute();
                    subscriber.onNext(calendar);
                    subscriber.onCompleted();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Events>() {
                    @Override
                    public void call(Events events) {
                        //Toast.makeText(ScheduleActivity.this,"Hi",Toast.LENGTH_SHORT).show();
                    }
                });


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
