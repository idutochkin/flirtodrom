package com.example.run18.withtoolbar;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import java.util.List;

public class UsersRecyclerViewAdapter extends RecyclerView.Adapter<UsersRecyclerViewAdapter.CustomViewHolder> {
    private List<FeedUser> feedsUsers;
    private Context mContext;
    private OnUserClickListener onUserClickListener;
    private OnLikeListener onLikeListener;

    public UsersRecyclerViewAdapter(Context context, List<FeedUser> feedsUsers) {
        this.feedsUsers = feedsUsers;
        this.mContext = context;
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_item, null);
        CustomViewHolder viewHolder = new CustomViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final CustomViewHolder customViewHolder, final int i) {
        final FeedUser feedUser = feedsUsers.get(i);

        customViewHolder.user_id.setText(Html.fromHtml(feedUser.getUserId()));
        customViewHolder.name.setText(Html.fromHtml(feedUser.getName()));
        customViewHolder.age.setText(Html.fromHtml(feedUser.getAge().toString()));
        customViewHolder.like_status.setText(Html.fromHtml(feedUser.getLikeStatus()));
        customViewHolder.like_status_too.setText(Html.fromHtml(feedUser.getLikeStatusToo()));
        if(feedUser.getLikeStatus().equals("1")) {
            customViewHolder.user_like_button.setBackgroundResource(R.drawable.ic_favorite_black_24dp);
            customViewHolder.like_status.setText("1");
        } else {
            customViewHolder.user_like_button.setBackgroundResource(R.drawable.ic_favorite_border_black_24dp);
            customViewHolder.like_status.setText("0");
        }
        if(!TextUtils.isEmpty(feedUser.getPicture())) {
            Picasso.with(mContext).load(feedUser.getPicture())
                    .error(R.drawable.placeholder)
                    .placeholder(R.drawable.placeholder)
                    .into(customViewHolder.picture);
        }

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onUserClickListener.onItemClick(feedUser);
            }
        };
        View.OnClickListener like = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onLikeListener.onItemClick(feedUser, customViewHolder);
            }
        };
        customViewHolder.picture.setOnClickListener(listener);
        customViewHolder.name.setOnClickListener(listener);
        customViewHolder.user_like_button.setOnClickListener(like);
    }

    @Override
    public int getItemCount() {
        return (null != feedsUsers ? feedsUsers.size() : 0);
    }


    class CustomViewHolder extends RecyclerView.ViewHolder {
        protected TextView user_id;
        protected TextView name;
        protected TextView age;
        protected ImageView picture;
        protected TextView like_status;
        protected TextView like_status_too;
        ImageView user_like_button;

        public CustomViewHolder(View view) {
            super(view);
            this.user_id = (TextView) view.findViewById(R.id.user_id);
            this.name = (TextView) view.findViewById(R.id.name);
            this.age = (TextView) view.findViewById(R.id.age);
            this.picture = (ImageView) view.findViewById(R.id.picture);
            this.like_status = (TextView) view.findViewById(R.id.like_status);
            this.like_status_too = (TextView) view.findViewById(R.id.like_status_too);
            user_like_button = (ImageView)view.findViewById(R.id.user_like_button);
        }
    }

    public OnUserClickListener getOnUserClickListener() {
        return onUserClickListener;
    }

    public void setOnUserClickListener(OnUserClickListener onUserClickListener) {
        this.onUserClickListener = onUserClickListener;
    }

    public OnLikeListener getOnLikeListener() {
        return onLikeListener;
    }

    public void setOnLikeListener(OnLikeListener onLikeListener) {
        this.onLikeListener = onLikeListener;
    }
}