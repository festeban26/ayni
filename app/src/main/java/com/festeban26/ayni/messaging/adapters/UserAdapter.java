package com.festeban26.ayni.messaging.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.festeban26.ayni.activities.MessageActivity;
import com.festeban26.ayni.messaging.model.User;
import com.festeban26.ayni.R;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private Context mContext;
    private List<User> mUsers;

    public UserAdapter(Context context, List<User> users) {
        mContext = context;
        mUsers = users;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_user, viewGroup, false);
        return new UserAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        final User user = mUsers.get(position);
        viewHolder.getUsername().setText(user.getUsername());
        if (user.getImageUrl().equals("default")) {
            Glide.with(mContext)
                    .load(R.mipmap.ic_launcher)
                    .apply(RequestOptions.circleCropTransform())
                    .into(viewHolder.getProfileImage());
        } else {
            Glide.with(mContext)
                    .load(user.getImageUrl())
                    .apply(RequestOptions.circleCropTransform())
                    .into(viewHolder.getProfileImage());
        }

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, MessageActivity.class);
                intent.putExtra("USER_ID", user.getId());
                mContext.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView mUsername;
        private ImageView mProfileImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            mUsername = itemView.findViewById(R.id.TextView_UserItem_Username);
            mProfileImage = itemView.findViewById(R.id.ImageView_UserItem_ProfileImage);
        }

        public TextView getUsername() {
            return mUsername;
        }

        public ImageView getProfileImage() {
            return mProfileImage;
        }


    }
}
