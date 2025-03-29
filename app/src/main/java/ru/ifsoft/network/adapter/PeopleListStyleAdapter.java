package ru.ifsoft.network.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import  ru.ifsoft.network.libs.circularImageView.*;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Locale;

import github.ankushsachdeva.emojicon.EmojiconTextView;
import ru.ifsoft.network.ProfileActivity;
import ru.ifsoft.network.R;
import ru.ifsoft.network.constants.Constants;
import ru.ifsoft.network.model.Chat;
import ru.ifsoft.network.model.Notify;
import ru.ifsoft.network.model.Profile;

public class PeopleListStyleAdapter extends RecyclerView.Adapter<PeopleListStyleAdapter.ViewHolder> implements Constants {

    private Context ctx;
    private List<Profile> items;
    private OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener {

        void onItemClick(View view, Profile item, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {

        this.mOnItemClickListener = mItemClickListener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView title, time;
        public CircularImageView image, online, verified, icon;
        public LinearLayout parent;
        public EmojiconTextView message;

        public ViewHolder(View view) {

            super(view);

            title = (TextView) view.findViewById(R.id.title);
            message = (EmojiconTextView) view.findViewById(R.id.message);
            time = (TextView) view.findViewById(R.id.time);
            image = (CircularImageView) view.findViewById(R.id.image);
            parent = (LinearLayout) view.findViewById(R.id.parent);

            online = (CircularImageView) view.findViewById(R.id.online);
            verified = (CircularImageView) view.findViewById(R.id.verified);
            icon = (CircularImageView) view.findViewById(R.id.icon);
        }
    }

    public PeopleListStyleAdapter(Context mContext, List<Profile> items) {

        this.ctx = mContext;
        this.items = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_people_list_row, parent, false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        final Profile item = items.get(position);

        holder.online.setVisibility(View.GONE);
        holder.verified.setVisibility(View.GONE);
        holder.message.setVisibility(View.GONE);
        holder.time.setVisibility(View.GONE);

        if (item.getNormalPhotoUrl() != null && item.getNormalPhotoUrl().length() > 0) {

            final ImageView img = holder.image;

            Picasso.with(ctx)
                    .load(item.getNormalPhotoUrl())
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

        } else {

            holder.image.setVisibility(View.VISIBLE);
            holder.image.setImageResource(R.drawable.profile_default_photo);
        }

        holder.title.setText(item.getFullname());

        if (item.isOnline()) {

            holder.online.setVisibility(View.VISIBLE);

        } else {

            holder.online.setVisibility(View.GONE);
        }

        if (item.isVerify()) {

            holder.verified.setVisibility(View.VISIBLE);

        } else {

            holder.verified.setVisibility(View.GONE);
        }

        switch (item.getReaction()) {

            case 1: {

                holder.icon.setImageResource(R.drawable.ic_reaction_1);

                break;
            }

            case 2: {

                holder.icon.setImageResource(R.drawable.ic_reaction_2);

                break;
            }

            case 3: {

                holder.icon.setImageResource(R.drawable.ic_reaction_3);

                break;
            }

            case 4: {

                holder.icon.setImageResource(R.drawable.ic_reaction_4);

                break;
            }

            case 5: {

                holder.icon.setImageResource(R.drawable.ic_reaction_5);

                break;
            }

            default: {

                holder.icon.setImageResource(R.drawable.ic_reaction_0);

                break;
            }
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

                Profile item = items.get(position);

                if (item.getId() != 0) {

                    Intent intent = new Intent(ctx, ProfileActivity.class);
                    intent.putExtra("profileId", item.getId());
                    ctx.startActivity(intent);
                }
            }
        });
    }

    public Profile getItem(int position) {

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