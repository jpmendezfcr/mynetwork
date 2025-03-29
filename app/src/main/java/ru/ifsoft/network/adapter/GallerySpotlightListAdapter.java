package ru.ifsoft.network.adapter;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.balysv.materialripple.MaterialRippleLayout;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;

import ru.ifsoft.network.R;
import ru.ifsoft.network.model.GalleryItem;
import ru.ifsoft.network.model.Image;


public class GallerySpotlightListAdapter extends RecyclerView.Adapter<GallerySpotlightListAdapter.MyViewHolder> {

    private List<GalleryItem> items;
    private Context mContext;

    private OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener {

        void onItemClick(View view, GalleryItem obj, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {

        this.mOnItemClickListener = mItemClickListener;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        public ImageView thumbnail, mPlayIcon;
        public ProgressBar mProgressBar;
        public MaterialRippleLayout mParent;

        public MyViewHolder(View view) {

            super(view);

            mParent = (MaterialRippleLayout) view.findViewById(R.id.parent);
            thumbnail = (ImageView) view.findViewById(R.id.thumbnail);
            mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);
            mPlayIcon = (ImageView) view.findViewById(R.id.play_icon);
        }
    }


    public GallerySpotlightListAdapter(Context context, List<GalleryItem> items) {

        mContext = context;
        this.items = items;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.gallery_spotlight_thumbnail, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {

        final GalleryItem item = items.get(position);

        holder.mPlayIcon.setVisibility(View.GONE);
        holder.thumbnail.setVisibility(View.VISIBLE);
        holder.mProgressBar.setVisibility(View.VISIBLE);

        if (item.getVideoUrl().length() != 0) {

            if (item.getPreviewVideoImgUrl().length() > 0) {

                final ProgressBar progressBar = holder.mProgressBar;
                final ImageView imageView = holder.thumbnail;
                final ImageView playIcon = holder.mPlayIcon;

                Picasso.with(mContext)
                        .load(item.getPreviewVideoImgUrl())
                        .into(holder.thumbnail, new Callback() {

                            @Override
                            public void onSuccess() {

                                progressBar.setVisibility(View.GONE);
                                playIcon.setVisibility(View.VISIBLE);
                            }

                            @Override
                            public void onError() {

                                progressBar.setVisibility(View.GONE);
                                playIcon.setVisibility(View.VISIBLE);
                                imageView.setImageResource(R.drawable.profile_default_photo);
                            }
                        });

            } else {

                holder.mProgressBar.setVisibility(View.GONE);
                holder.mPlayIcon.setVisibility(View.VISIBLE);
                holder.thumbnail.setImageResource(R.drawable.profile_default_photo);
            }

        } else {

            if (item.getImgUrl() != null && item.getImgUrl().length() > 0) {

                final ProgressBar progressBar = holder.mProgressBar;
                final ImageView imageView = holder.thumbnail;

                Picasso.with(mContext)
                        .load(item.getImgUrl())
                        .into(holder.thumbnail, new Callback() {

                            @Override
                            public void onSuccess() {

                                progressBar.setVisibility(View.GONE);
                            }

                            @Override
                            public void onError() {

                                progressBar.setVisibility(View.GONE);
                                imageView.setImageResource(R.drawable.profile_default_photo);
                            }
                        });

            } else {

                holder.mProgressBar.setVisibility(View.GONE);
                holder.thumbnail.setImageResource(R.drawable.profile_default_photo);
            }
        }

        holder.mParent.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                if (mOnItemClickListener != null) {

                    mOnItemClickListener.onItemClick(view, item, position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {

        return items.size();
    }
}