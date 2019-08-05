package com.example.run18.withtoolbar;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class PlacesRecyclerViewAdapter extends RecyclerView.Adapter<PlacesRecyclerViewAdapter.CustomViewHolder> {
    private List<FeedPlace> feedsPlaces;
    private Context mContext;
    private OnPlaceClickListener onPlaceClickListener;

    public PlacesRecyclerViewAdapter(Context context, List<FeedPlace> feedsPlaces) {
        this.feedsPlaces = feedsPlaces;
        this.mContext = context;
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.place_item2, null);
        CustomViewHolder viewHolder = new CustomViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(CustomViewHolder customViewHolder, int i) {
        final FeedPlace feedPlace = feedsPlaces.get(i);

        customViewHolder.name.setText(Html.fromHtml(feedPlace.getName()));
        customViewHolder.placeAddress.setText(Html.fromHtml(feedPlace.getPlaceAddress()));
        customViewHolder.place_id.setText(Html.fromHtml(feedPlace.getPlaceId()));
        customViewHolder.count_check_in.setText(Html.fromHtml(feedPlace.getCountCheckIn()));
        if(!TextUtils.isEmpty(feedPlace.getPlacePicture())) {
            Picasso.with(mContext).load(feedPlace.getPlacePicture())
                    .error(R.drawable.placeholder)
                    .placeholder(R.drawable.placeholder)
                    .into(customViewHolder.placePicture);
        }


        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPlaceClickListener.onItemClick(feedPlace);
            }
        };
        customViewHolder.placePicture.setOnClickListener(listener);
        customViewHolder.name.setOnClickListener(listener);
    }

    @Override
    public int getItemCount() {
        return (null != feedsPlaces ? feedsPlaces.size() : 0);
    }

    class CustomViewHolder extends RecyclerView.ViewHolder {
        protected ImageView placePicture;
        protected TextView name;
        protected TextView placeAddress;
        protected TextView place_id;
        protected TextView count_check_in;

        public CustomViewHolder(View view) {
            super(view);
            this.placePicture = (ImageView) view.findViewById(R.id.placePicture);
            this.name = (TextView) view.findViewById(R.id.name);
            this.placeAddress = (TextView) view.findViewById(R.id.placeAddress);
            this.place_id = (TextView) view.findViewById(R.id.place_id);
            this.count_check_in = (TextView) view.findViewById(R.id.count_check_in);
        }
    }

    public OnPlaceClickListener getOnPlaceClickListener() {
        return onPlaceClickListener;
    }

    public void setOnPlaceClickListener(OnPlaceClickListener onItemClickListener) {
        this.onPlaceClickListener = onItemClickListener;
    }
}