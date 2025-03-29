package ru.ifsoft.network.adapter;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.drawable.Drawable;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;

import ru.ifsoft.network.R;
import ru.ifsoft.network.constants.Constants;
import ru.ifsoft.network.model.GalleryItem;
import ru.ifsoft.network.model.Image;



public class GalleryListAdapter extends RecyclerView.Adapter<GalleryListAdapter.MyViewHolder> {

    private List<GalleryItem> images;
    private Context mContext;

    public class MyViewHolder extends RecyclerView.ViewHolder {

        public ImageView thumbnail, playIcon;
        public ProgressBar progressBar;

        public MyViewHolder(View view) {

            super(view);

            thumbnail = (ImageView) view.findViewById(R.id.thumbnail);
            playIcon = (ImageView) view.findViewById(R.id.playImg);
            progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        }
    }


    public GalleryListAdapter(Context context, List<GalleryItem> images) {

        mContext = context;
        this.images = images;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.gallery_thumbnail, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        GalleryItem image = images.get(position);

        holder.thumbnail.setVisibility(View.VISIBLE);
        holder.progressBar.setVisibility(View.VISIBLE);
        holder.playIcon.setVisibility(View.GONE);

        final ImageView playImg = holder.playIcon;
        final ProgressBar progressBar = holder.progressBar;

        if (image.getItemType() == Constants.GALLERY_ITEM_TYPE_VIDEO) {

            Picasso.with(mContext)
                    .load(image.getPreviewVideoImgUrl())
                    .into(holder.thumbnail, new Callback() {

                        @Override
                        public void onSuccess() {

                            progressBar.setVisibility(View.GONE);
                            playImg.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onError() {

                            progressBar.setVisibility(View.GONE);
                            playImg.setVisibility(View.VISIBLE);
                        }
                    });

        } else {

            Picasso.with(mContext)
                    .load(image.getImgUrl())
                    .into(holder.thumbnail, new Callback() {

                        @Override
                        public void onSuccess() {

                            progressBar.setVisibility(View.GONE);
                        }

                        @Override
                        public void onError() {

                            progressBar.setVisibility(View.GONE);
                        }
                    });
        }
    }

    @Override
    public int getItemCount() {

        return images.size();
    }

    public interface ClickListener {

        void onClick(View view, int position);

        void onLongClick(View view, int position);
    }

    public static class RecyclerTouchListener implements RecyclerView.OnItemTouchListener {

        private GestureDetector gestureDetector;
        private ClickListener clickListener;

        public RecyclerTouchListener(Context context, final RecyclerView recyclerView, final ClickListener clickListener) {

            this.clickListener = clickListener;

            gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {

                @Override
                public boolean onSingleTapUp(MotionEvent e) {

                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {

                    View child = recyclerView.findChildViewUnder(e.getX(), e.getY());

                    if (child != null && clickListener != null) {

                        clickListener.onLongClick(child, recyclerView.getChildPosition(child));
                    }
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {

            View child = rv.findChildViewUnder(e.getX(), e.getY());

            if (child != null && clickListener != null && gestureDetector.onTouchEvent(e)) {

                clickListener.onClick(child, rv.getChildPosition(child));
            }
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {

        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    }
}