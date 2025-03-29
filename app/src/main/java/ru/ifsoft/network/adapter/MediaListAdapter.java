package ru.ifsoft.network.adapter;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.balysv.materialripple.MaterialRippleLayout;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

import ru.ifsoft.network.R;
import ru.ifsoft.network.app.App;
import ru.ifsoft.network.constants.Constants;
import ru.ifsoft.network.model.MediaItem;


public class MediaListAdapter extends RecyclerView.Adapter<MediaListAdapter.MyViewHolder> implements Constants {

    private List<MediaItem> items;
    private Context ctx;

    private OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener {

        void onItemClick(View view, MediaItem obj, int position, int action);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {

        this.mOnItemClickListener = mItemClickListener;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        public ImageView thumbnail, mPlayIcon;
        public ProgressBar mProgressBar;
        public MaterialRippleLayout mParent;
        public LinearLayout mDelete;

        public MyViewHolder(View view) {

            super(view);

            mParent = (MaterialRippleLayout) view.findViewById(R.id.parent);
            thumbnail = (ImageView) view.findViewById(R.id.thumbnail);
            mPlayIcon = (ImageView) view.findViewById(R.id.play_icon);
            mDelete = (LinearLayout) view.findViewById(R.id.delete);
            mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        }
    }


    public MediaListAdapter(Context context, List<MediaItem> items) {

        this.ctx = context;
        this.items = items;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_media_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {

        final MediaItem item = items.get(position);

        holder.thumbnail.setVisibility(View.VISIBLE);
        holder.mProgressBar.setVisibility(View.VISIBLE);
        holder.mDelete.setVisibility(View.GONE);
        holder.mPlayIcon.setVisibility(View.GONE);

        if (item.getImageUrl().length() > 0) {

            final ProgressBar progressBar = holder.mProgressBar;
            final ImageView imageView = holder.thumbnail;
            final LinearLayout deleteButton = holder.mDelete;
            final ImageView playButton = holder.mPlayIcon;

            Picasso.with(ctx)
                    .load(item.getImageUrl())
                    .into(holder.thumbnail, new Callback() {

                        @Override
                        public void onSuccess() {

                            progressBar.setVisibility(View.GONE);
                            deleteButton.setVisibility(View.VISIBLE);

                            if (item.getType() != 0) {

                                playButton.setVisibility(View.VISIBLE);
                            }
                        }

                        @Override
                        public void onError() {

                            progressBar.setVisibility(View.GONE);
                            imageView.setImageResource(R.drawable.img_loading);
                            deleteButton.setVisibility(View.VISIBLE);

                            if (item.getType() != 0) {

                                playButton.setVisibility(View.VISIBLE);
                            }
                        }
                    });

        } else {

            holder.mProgressBar.setVisibility(View.GONE);
            holder.mDelete.setVisibility(View.VISIBLE);

            if (item.getType() != 0) {

                holder.mPlayIcon.setVisibility(View.VISIBLE);

                if (item.getSelectedImageFileName().length() > 0) {

                    holder.thumbnail.setImageURI(Uri.fromFile(new File(App.getInstance().getDirectory(), VIDEO_THUMBNAIL_FILE)));

                } else {

                    holder.thumbnail.setImageResource(R.drawable.ic_video_preview);
                }

            } else {

                if (item.getSelectedImageFileName().length() > 0) {

                    holder.thumbnail.setImageURI(Uri.fromFile(new File(item.getSelectedImageFileName())));
                }
            }
        }

        holder.mDelete.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (mOnItemClickListener != null) {

                    mOnItemClickListener.onItemClick(v, item, position, 1);
                }
            }
        });

        holder.mParent.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                if (mOnItemClickListener != null) {

                    mOnItemClickListener.onItemClick(view, item, position, 0);
                }
            }
        });
    }

    @Override
    public int getItemCount() {

        return items.size();
    }
}