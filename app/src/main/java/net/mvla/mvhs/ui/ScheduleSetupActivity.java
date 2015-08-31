package net.mvla.mvhs.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import net.mvla.mvhs.PrefUtils;
import net.mvla.mvhs.R;
import net.mvla.mvhs.Utils;
import net.mvla.mvhs.model.StudentPeriodInfo;


/**
 * Setting up classes/rooms
 */
public class ScheduleSetupActivity extends AppCompatActivity {

    public static final String EXTRA_OPTIONAL = "ScheduleSetupActivity.OPTIONAL";

    private Toolbar mActionBarToolbar;
    private boolean mOptional;

    Toolbar getActionBarToolbar() {
        if (mActionBarToolbar == null) {
            mActionBarToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
            if (mActionBarToolbar != null) {
                setSupportActionBar(mActionBarToolbar);
            }
        }
        return mActionBarToolbar;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sched_setup);

        mOptional = getIntent().getBooleanExtra(EXTRA_OPTIONAL, false);

        getActionBarToolbar();

        FragmentManager fm = getFragmentManager();
        Fragment f = fm.findFragmentById(R.id.activity_setup_fragment);
        if (f == null) {
            f = new SetupFragment();
            fm.beginTransaction()
                    .replace(R.id.activity_setup_fragment, f)
                    .commit();
        }

        if (mOptional) {
            mActionBarToolbar.setNavigationIcon(R.drawable.ic_close_black_24dp);
            //TODO: Convert to DP
            mActionBarToolbar.setContentInsetsRelative(Utils.convertDpToPx(this, 72), 0);
        }

        setTitle(getString(R.string.schedule_setup));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (mOptional) {
            super.onBackPressed();
        } else {
            Snackbar.make(findViewById(android.R.id.content), getString(R.string.enter_schedule_warning), Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_setup, menu);

        return super.onCreateOptionsMenu(menu);
    }

    public static class SetupFragment extends Fragment {
        private RecyclerView mRecyclerView;
        private SetupAdapter mAdapter;
        private StudentPeriodInfo[] mPeriods;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setHasOptionsMenu(true);
            mPeriods = new StudentPeriodInfo[8];
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            for (int i = 0; i < mPeriods.length; i++) {
                mPeriods[i] = new StudentPeriodInfo();
                mPeriods[i].room = preferences.getString(PrefUtils.PREF_SCHEDULE_PREFIX + i + PrefUtils.PREF_SCHEDULE_ROOM, "");
                mPeriods[i].subject = preferences.getString(PrefUtils.PREF_SCHEDULE_PREFIX + i + PrefUtils.PREF_SCHEDULE_SBJCT, "");
            }

        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.menu_setup_done:
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    for (int i = 0; i < mPeriods.length; i++) {
                        StudentPeriodInfo period = mPeriods[i];
                        preferences.edit().putString(PrefUtils.PREF_SCHEDULE_PREFIX + i + PrefUtils.PREF_SCHEDULE_ROOM,
                                String.valueOf(period.room)).apply();
                        preferences.edit().putString(PrefUtils.PREF_SCHEDULE_PREFIX + i + PrefUtils.PREF_SCHEDULE_SBJCT,
                                String.valueOf(period.subject)).apply();
                    }
                    getActivity().finish();
                    break;
            }
            return super.onOptionsItemSelected(item);
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            mRecyclerView = (RecyclerView) inflater.inflate(R.layout.fragment_list, container, false);
            mAdapter = new SetupAdapter();
            mRecyclerView.setAdapter(mAdapter);
            mRecyclerView.setHasFixedSize(true);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            return mRecyclerView;
        }


        private class SetupAdapter extends RecyclerView.Adapter<SetupAdapter.SetupViewHolder> {

            @Override
            public SetupViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(getActivity()).inflate(R.layout.list_item_sched_setup, viewGroup, false);
                SetupViewHolder holder = new SetupViewHolder(view);
                holder.period = (TextView) view.findViewById(R.id.list_item_setup_period_text);
                holder.room = (EditText) view.findViewById(R.id.list_item_setup_room_edit);
                holder.subject = (EditText) view.findViewById(R.id.list_item_setup_subject_edit);
                return holder;
            }

            @Override
            public void onBindViewHolder(SetupViewHolder holder, final int i) {
                holder.period.setText("Period " + i + ": ");
                holder.room.setText(mPeriods[i].room);
                holder.subject.setText(mPeriods[i].subject);


                if (holder.room.getTag() != null) {
                    holder.room.removeTextChangedListener((TextWatcher) holder.room.getTag());
                }
                TextWatcher watcher = new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        mPeriods[i].room = s.toString();
                    }
                };
                holder.room.addTextChangedListener(watcher);
                holder.room.setTag(watcher);

                if (holder.subject.getTag() != null) {
                    holder.subject.removeTextChangedListener((TextWatcher) holder.subject.getTag());
                }
                TextWatcher watcherSub = new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        mPeriods[i].subject = s.toString();
                    }
                };
                holder.subject.addTextChangedListener(watcherSub);
                holder.subject.setTag(watcher);
            }

            @Override
            public int getItemCount() {
                return 8;
            }

            class SetupViewHolder extends RecyclerView.ViewHolder {
                TextView period;
                EditText room;
                EditText subject;

                public SetupViewHolder(View itemView) {
                    super(itemView);
                }
            }
        }
    }
}
