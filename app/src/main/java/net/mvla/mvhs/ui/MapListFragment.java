package net.mvla.mvhs.ui;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.mvla.mvhs.R;
import net.mvla.mvhs.map.LocationNode;
import net.mvla.mvhs.map.MapData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * List
 */
public class MapListFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private LocationAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void updateDataAndSearch(String search) {
        if (mAdapter != null) {
            mAdapter.updateDataAndSearch(search);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRecyclerView = (RecyclerView) inflater.inflate(R.layout.fragment_list, container, false);
        mAdapter = new LocationAdapter();
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        return mRecyclerView;
    }

    private class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.ArtViewHolder> {

        private String mSearchQuery;
        private List<LocationNode> mLocations;

        public LocationAdapter() {
            updateDataAndSearch("");

            mSearchQuery = "";
        }

        @Override
        public ArtViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(getActivity()).inflate(R.layout.list_item_location, viewGroup, false);
            ArtViewHolder holder = new ArtViewHolder(view);
            holder.title = (TextView) view.findViewById(R.id.list_item_location_title_textview);
            holder.desc = (TextView) view.findViewById(R.id.list_item_location_desc_textview);
            return holder;
        }

        @Override
        public void onBindViewHolder(ArtViewHolder holder, int i) {
            LocationNode location = mLocations.get(i);
            holder.title.setText(location.getName());
            //holder.desc.setText(text);
        }

        public void updateDataAndSearch(String string) {
            mSearchQuery = string;

            if (!mSearchQuery.isEmpty()) {
                mLocations = new ArrayList<>();
                for (LocationNode location : new ArrayList<>(MapData.locationNodeMap.values())) {
                    if (location.matchFilter(string)) {
                        mLocations.add(location);
                    }
                }
            } else {
                mLocations = new ArrayList<>(MapData.locationNodeMap.values());
            }

            Collections.sort(mLocations, new Comparator<LocationNode>() {
                @Override
                public int compare(LocationNode lhs, LocationNode rhs) {
                    if (lhs.getName().startsWith(mSearchQuery) && !rhs.getName().startsWith(mSearchQuery)) {
                        return -1;
                    }
                    return lhs.getName().compareTo(rhs.getName());
                }
            });
            notifyDataSetChanged();
        }

        @Override
        public int getItemCount() {
            return mLocations.size();
        }

        class ArtViewHolder extends RecyclerView.ViewHolder {
            TextView title;
            TextView desc;

            public ArtViewHolder(View itemView) {
                super(itemView);
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MapActivity activity = (MapActivity) getActivity();
                        activity.onMapListItemClicked(mLocations.get(getLayoutPosition()));
                    }
                });
            }
        }
    }
}

