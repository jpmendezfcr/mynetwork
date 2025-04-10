package ru.ifsoft.network.adapter;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import  ru.ifsoft.network.libs.circularImageView.*;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;

import github.ankushsachdeva.emojicon.EmojiconTextView;
import ru.ifsoft.network.ProfileActivity;
import ru.ifsoft.network.model.Chat;
import ru.ifsoft.network.R;


public class DialogsListAdapter extends RecyclerView.Adapter<DialogsListAdapter.ViewHolder> {

    private Context ctx;
    private List<Chat> items;
    private OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener {

        void onItemClick(View view, Chat item, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {

        this.mOnItemClickListener = mItemClickListener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView title, subtitle, count, time;
        public CircularImageView image, online, verified;
        public LinearLayout parent;
        public EmojiconTextView message;

        public ViewHolder(View view) {

            super(view);

            title = (TextView) view.findViewById(R.id.title);
            subtitle = (TextView) view.findViewById(R.id.subtitle);
            message = (EmojiconTextView) view.findViewById(R.id.message);
            count = (TextView) view.findViewById(R.id.count);
            time = (TextView) view.findViewById(R.id.time);
            image = (CircularImageView) view.findViewById(R.id.image);
            parent = (LinearLayout) view.findViewById(R.id.parent);

            online = (CircularImageView) view.findViewById(R.id.online);
            verified = (CircularImageView) view.findViewById(R.id.verified);
        }
    }

    public DialogsListAdapter(Context mContext, List<Chat> items) {

        this.ctx = mContext;
        this.items = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_dialog_list_row, parent, false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        final Chat item = items.get(position);

        holder.online.setVisibility(View.GONE);

        if (item.getWithUserVerify() != 0) {

            holder.verified.setVisibility(View.VISIBLE);

        } else {

            holder.verified.setVisibility(View.GONE);
        }

        if (item.getWithUserPhotoUrl().length() > 0) {

            final ImageView img = holder.image;

            try {

                Picasso.with(ctx)
                        .load(item.getWithUserPhotoUrl())
                        .into(holder.image, new Callback() {

                            @Override
                            public void onSuccess() {

                                img.setVisibility(View.VISIBLE);
                            }

                            @Override
                            public void onError() {

                                img.setImageResource(R.drawable.profile_default_photo);
                                img.setVisibility(View.VISIBLE);
                            }
                        });

            } catch (Exception e) {

                Log.e("DialogsListAdapter", e.toString());
            }

        } else {

            holder.image.setImageResource(R.drawable.profile_default_photo);
        }

        holder.title.setText(item.getWithUserFullname());
        holder.subtitle.setVisibility(View.GONE);

        if (item.getLastMessage().length() != 0) {

            holder.message.setText(item.getLastMessage().replaceAll("<br>", " "));

        } else {

            holder.message.setText(ctx.getString(R.string.label_last_message_image));
        }

        if (item.getLastMessageAgo().length() != 0) {

            holder.time.setText(item.getLastMessageAgo());

        } else {

            holder.time.setText("");
        }

        if (item.getNewMessagesCount() == 0) {

            holder.count.setVisibility(View.GONE);
            holder.count.setText(Integer.toString(item.getNewMessagesCount()));

        } else {

            holder.count.setVisibility(View.VISIBLE);
            holder.count.setText(Integer.toString(item.getNewMessagesCount()));
        }

        holder.parent.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (mOnItemClickListener != null) {

                    mOnItemClickListener.onItemClick(v, items.get(position), position);
                }
            }
        });

        holder.image.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Chat chat = items.get(position);

                Intent intent = new Intent(ctx, ProfileActivity.class);
                intent.putExtra("profileId", chat.getWithUserId());
                ctx.startActivity(intent);
            }
        });
    }

    public Chat getItem(int position) {

        return items.get(position);
    }

    @Override
    public int getItemCount() {

        return items.size();
    }

    public interface OnClickListener {

        void onItemClick(View view, Chat item, int pos);
    }
}