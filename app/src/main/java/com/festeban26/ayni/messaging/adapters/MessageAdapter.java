package com.festeban26.ayni.messaging.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.festeban26.ayni.AppAuth;
import com.festeban26.ayni.R;
import com.festeban26.ayni.messaging.model.Chat;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    public static final int MESSAGE_TYPE_LEFT = 0;
    public static final int MESSAGE_TYPE_RIGHT = 1;

    private Context mContext;
    private List<Chat> mChats;
    private String mImageUrl;

    private String mMyId;

    public MessageAdapter(Context context, List<Chat> chats, String imageUrl) {
        mContext = context;
        mChats = chats;
        mImageUrl = imageUrl;
        mMyId = AppAuth.getInstance().getCurrentFacebookUser(context).getId();
    }

    @NonNull
    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        if(viewType == MESSAGE_TYPE_RIGHT){
            View view = LayoutInflater.from(mContext).inflate(R.layout.item_chat_right, viewGroup, false);
            return new MessageAdapter.ViewHolder(view);
        }
        else{
            View view = LayoutInflater.from(mContext).inflate(R.layout.item_chat_left, viewGroup, false);
            return new MessageAdapter.ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MessageAdapter.ViewHolder viewHolder, int position) {
        Chat chat = mChats.get(position);

        viewHolder.mMessageText.setText(chat.getMessage());
        if (mImageUrl.equals("default")) {
            Glide.with(mContext)
                    .load(R.mipmap.ic_launcher)
                    .apply(RequestOptions.circleCropTransform())
                    .into(viewHolder.getProfileImage());
        } else {
            Glide.with(mContext)
                    .load(mImageUrl)
                    .apply(RequestOptions.circleCropTransform())
                    .into(viewHolder.getProfileImage());
        }

    }

    @Override
    public int getItemCount() {
        return mChats.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView mMessageText;
        private ImageView mProfileImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            mMessageText = itemView.findViewById(R.id.TextView_ChatItem_MessageText);
            // TODO
            mProfileImage = itemView.findViewById(R.id.ImageView_ChatItem_ProfileImage);
        }

        public TextView getUsername() {
            return mMessageText;
        }

        public ImageView getProfileImage() {
            return mProfileImage;
        }


    }

    @Override
    public int getItemViewType(int position) {
        if (mChats.get(position).getSender().equals(mMyId))
            return MESSAGE_TYPE_RIGHT;
        else
            return MESSAGE_TYPE_LEFT;
    }
}
