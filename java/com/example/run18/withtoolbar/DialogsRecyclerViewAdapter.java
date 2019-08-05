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

public class DialogsRecyclerViewAdapter extends RecyclerView.Adapter<DialogsRecyclerViewAdapter.CustomViewHolder> {
    private List<FeedDialog> feedsDialogs;
    private Context mContext;
    private OnDialogClickListener onDialogClickListener;

    public DialogsRecyclerViewAdapter(Context context, List<FeedDialog> feedsDialogs) {
        this.feedsDialogs = feedsDialogs;
        this.mContext = context;
    }

    class CustomViewHolder extends RecyclerView.ViewHolder {
        protected TextView userId;
        protected TextView userName;
        protected ImageView userPicture;
        protected TextView lastMessage;
        protected TextView lastMessageTimestamp;

        public CustomViewHolder(View view) {
            super(view);
            this.userId = (TextView) view.findViewById(R.id.user_id);
            this.userName = (TextView) view.findViewById(R.id.user_name);
            this.userPicture = (ImageView) view.findViewById(R.id.user_picture);
            this.lastMessage = (TextView) view.findViewById(R.id.last_message);
            this.lastMessageTimestamp = (TextView) view.findViewById(R.id.last_message_timestamp);
        }
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.dialog_item, null);
        CustomViewHolder viewHolder = new CustomViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(CustomViewHolder customViewHolder, int i) {
        final FeedDialog feedDialog = feedsDialogs.get(i);

        customViewHolder.userId.setText(Html.fromHtml(feedDialog.getUserId()));
        customViewHolder.userName.setText(Html.fromHtml(feedDialog.getUserName()));
        if(!TextUtils.isEmpty(feedDialog.getUserPicture())) {
            Picasso.with(mContext).load(feedDialog.getUserPicture())
                    .error(R.drawable.placeholder)
                    .placeholder(R.drawable.placeholder)
                    .into(customViewHolder.userPicture);
        }
        customViewHolder.lastMessage.setText(Html.fromHtml(feedDialog.getLastMessage()));
        customViewHolder.lastMessageTimestamp.setText(Html.fromHtml(feedDialog.getLastMessageTimestamp()));


        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDialogClickListener.onItemClick(feedDialog);
            }
        };
        customViewHolder.userPicture.setOnClickListener(listener);
        customViewHolder.userName.setOnClickListener(listener);
        customViewHolder.lastMessage.setOnClickListener(listener);
    }

    @Override
    public int getItemCount() {
        return (null != feedsDialogs ? feedsDialogs.size() : 0);
    }

    public OnDialogClickListener getOnDialogClickListener() {
        return onDialogClickListener;
    }

    public void setOnDialogClickListener(OnDialogClickListener onItemClickListener) {
        this.onDialogClickListener = onItemClickListener;
    }
}