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

public class MessageRecyclerViewAdapter extends RecyclerView.Adapter<MessageRecyclerViewAdapter.CustomViewHolder> {
    private List<FeedMessage> feedsMessages;
    private Context mContext;
    private OnMessageClickListener onMessageClickListener;

    public MessageRecyclerViewAdapter(Context context, List<FeedMessage> feedsMessages) {
        this.feedsMessages = feedsMessages;
        this.mContext = context;
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        final FeedMessage feedMessage = feedsMessages.get(i);

        if(feedMessage.getType().equals("in")) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.message_in, null);
            CustomViewHolder viewHolder = new CustomViewHolder(view);
            return viewHolder;
        } else {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.message_out, null);
            CustomViewHolder viewHolder = new CustomViewHolder(view);
            return viewHolder;
        }
    }

    @Override
    public void onBindViewHolder(CustomViewHolder customViewHolder, int i) {
        final FeedMessage feedMessage = feedsMessages.get(i);

        customViewHolder.message.setText(Html.fromHtml(feedMessage.getMessage()));
        customViewHolder.timestamp.setText(Html.fromHtml(feedMessage.getDateCreate()));
        if(!TextUtils.isEmpty(feedMessage.getPicture()) && feedMessage.getType().equals("in")) {
            Picasso.with(mContext).load(feedMessage.getPicture())
                    .error(R.drawable.placeholder)
                    .placeholder(R.drawable.placeholder)
                    .into(customViewHolder.picture);
        }

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onMessageClickListener.onItemClick(feedMessage);
            }
        };
        if(feedMessage.getType().equals("in")) {
            customViewHolder.picture.setOnClickListener(listener);
        }
    }

    @Override
    public int getItemCount() {
        return (null != feedsMessages ? feedsMessages.size() : 0);
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    class CustomViewHolder extends RecyclerView.ViewHolder {
        protected TextView message;
        protected TextView timestamp;
        protected ImageView picture;

        public CustomViewHolder(View view) {
            super(view);
            this.message = (TextView) view.findViewById(R.id.message);
            this.timestamp = (TextView) view.findViewById(R.id.timestamp);
            this.picture = (ImageView) view.findViewById(R.id.picture);
        }
    }

    public OnMessageClickListener getOnMessageClickListener() {
        return onMessageClickListener;
    }

    public void setOnMessageClickListener(OnMessageClickListener onMessageClickListener) {
        this.onMessageClickListener = onMessageClickListener;
    }
}