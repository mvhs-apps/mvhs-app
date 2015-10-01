package net.mvla.mvhs.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v14.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import net.mvla.mvhs.PrefUtils;
import net.mvla.mvhs.R;

public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_with_toolbar);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(toolbar);

        FragmentManager fm = getFragmentManager();
        Fragment f = fm.findFragmentById(R.id
                .activity_with_toolbar_content_framelayout);
        if (f == null) {
            fm.beginTransaction()
                    .replace(R.id.activity_with_toolbar_content_framelayout,
                            new SettingsFragment())
                    .commit();
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(R.string.settings);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static class SettingsFragment extends PreferenceFragment {

        @Override
        public void onCreatePreferences(Bundle bundle, String s) {
            addPreferencesFromResource(R.xml.preferences);

            Preference openSchdSetup = findPreference(PrefUtils.PREF_OPEN_SCHEDULE_SETUP);
            openSchdSetup.setOnPreferenceClickListener(preference -> {
                Intent intent = new Intent(getActivity(), ScheduleSetupActivity.class);
                intent.putExtra(ScheduleSetupActivity.EXTRA_OPTIONAL, true);
                startActivity(intent);
                return true;
            });

            ListPreference modes = (ListPreference) findPreference(PrefUtils.PREF_MODE);
            modes.setEntryValues(new String[]{"0", "1", "2"});
        }
    }


}
