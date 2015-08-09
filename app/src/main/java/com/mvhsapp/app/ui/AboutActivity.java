package com.mvhsapp.app.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mvhsapp.app.BuildConfig;
import com.mvhsapp.app.R;
import com.mvhsapp.app.Utils;

import java.util.ArrayList;
import java.util.List;

public class AboutActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;


    private List<Developer> mDevelopers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);


        TextView view = (TextView) findViewById(R.id.activity_about_appname_textview);
        view.setText(getString(R.string.app_name) + " v" + BuildConfig.VERSION_NAME);

        mDevelopers = new ArrayList<>();

        for (int i = 1; ; i++) {
            int id = getDevStringResource("" + i);
            if (id == 0) {
                break;
            }
            Developer dev = new Developer();
            dev.name = getString(id);
            dev.subtitle = getString(getDevStringResource(i + "_subtitle"));
            dev.desc = getString(getDevStringResource(i + "_desc"));

            int website = getDevStringResource(i + "_website");
            if (website != 0) {
                dev.website = getString(website);
            }

            int email = getDevStringResource(i + "_email");
            if (email != 0) {
                dev.email = getString(email);
            }

            mDevelopers.add(dev);
        }

        mRecyclerView = (RecyclerView) findViewById(R.id.activity_about_recycler);
        mRecyclerView.setAdapter(new AboutRecyclerAdapter());
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(R.string.about);
    }

    private int getDevStringResource(String i) {
        return getResources().getIdentifier("about_dev" + i, "string", getPackageName());
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

    private class Developer {
        String name;
        String subtitle;
        String desc;

        String website;
        String email;
    }

    private class AboutRecyclerAdapter extends RecyclerView.Adapter<AboutRecyclerAdapter.ViewHolder> {

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.list_item_about, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            final Developer developer = mDevelopers.get(position);
            holder.name.setText(developer.name);
            holder.subtitle.setText(developer.subtitle);
            holder.desc.setText(developer.desc);


            if (developer.email == null && developer.website == null) {
                holder.actionButtonBar.setVisibility(View.GONE);
                holder.desc.setPadding(0, 0, 0, Utils.convertDpToPx(AboutActivity.this, 24));
            } else {
                holder.actionButtonBar.setVisibility(View.VISIBLE);
                holder.desc.setPadding(0, 0, 0, Utils.convertDpToPx(AboutActivity.this, 16));

                if (developer.website != null) {
                    holder.website.setVisibility(View.VISIBLE);
                    holder.website.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(Intent.ACTION_VIEW,
                                    Uri.parse(developer.website));
                            startActivity(intent);
                        }
                    });
                } else {
                    holder.website.setVisibility(View.GONE);
                }

                if (developer.email != null) {
                    holder.email.setVisibility(View.VISIBLE);
                    holder.email.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(Intent.ACTION_SENDTO,
                                    Uri.fromParts(
                                            "mailto", developer.email, null));
                            startActivity(intent);
                        }
                    });
                } else {
                    holder.email.setVisibility(View.GONE);
                }
            }

        }

        @Override
        public int getItemCount() {
            return mDevelopers.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            private TextView name;
            private TextView subtitle;
            private TextView desc;
            private Button website;
            private Button email;
            private LinearLayout actionButtonBar;

            public ViewHolder(View itemView) {
                super(itemView);
                name = (TextView) itemView.findViewById(R.id.list_item_about_name);
                subtitle = (TextView) itemView.findViewById(R.id.list_item_about_subtitle);
                desc = (TextView) itemView.findViewById(R.id.list_item_about_desc);
                website = (Button) itemView.findViewById(R.id.list_item_about_website_button);
                email = (Button) itemView.findViewById(R.id.list_item_about_email_button);
                actionButtonBar = (LinearLayout) itemView.findViewById(R.id.list_item_about_actions_bar);
            }
        }
    }
}
