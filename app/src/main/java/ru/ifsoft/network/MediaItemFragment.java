package ru.ifsoft.network;

import static android.content.Context.RECEIVER_NOT_EXPORTED;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;


import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultAllocator;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import  ru.ifsoft.network.libs.circularImageView.*;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ru.ifsoft.network.HashtagsActivity;
import ru.ifsoft.network.ProfileActivity;
import ru.ifsoft.network.R;
import ru.ifsoft.network.app.App;
import ru.ifsoft.network.constants.Constants;
import ru.ifsoft.network.model.Item;
import ru.ifsoft.network.util.CustomRequest;
import ru.ifsoft.network.util.OnSwipeTouchListener;
import ru.ifsoft.network.util.TagClick;
import ru.ifsoft.network.util.TagSelectingTextview;

public class MediaItemFragment extends Fragment implements Constants, TagClick {

    private static final String STATE_LIST = "State Adapter Data";

    TagSelectingTextview mTagSelectingTextview;

    public static int hashTagHyperLinkDisabled = 0;

    ImageLoader imageLoader = App.getInstance().getImageLoader();

    private LinearLayout mSideMenu, mImagesLayout, mDetailsLayout, mCommentLayout, mShareLayout, mItemActionsLayout, mBottomLayout;

    private RelativeLayout mProfileLayout, mLikeLayout;

    private ImageView mPreviewImage, mLikeImage, mPlayImage, mImagesImage;

    private CircularImageView mProfileImage, mVerifiedImage;

    private PlayerView mPlayerView;

    private TextView mFullname, mUsername, mDescription, mDuration, mImagesCount, mCommentsCount, mLikesCount;

    private ProgressBar mProgressBar, mImagesProgressBar;

    private ViewPager2 mMenuPager;

    private Item item;

    public ExoPlayer exoplayer;

    private Context mContext;

    private Boolean isVisibleToUser, isMainPage = false, isUpdateViews = false;

    private int section_id = 0;

    public MediaItemFragment(Item item, ViewPager2 mMenuPager, Boolean isMainPage, int section) {

        this.item = item;
        this.mMenuPager = mMenuPager;
        this.isMainPage = isMainPage;
        this.section_id = section;

        if (imageLoader == null) {

            imageLoader = App.getInstance().getImageLoader();
        }
    }

    public MediaItemFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_media_item, container, false);

        mContext = rootView.getContext();

        //

        mPlayerView = rootView.findViewById(R.id.player_view);

        mPreviewImage = rootView.findViewById(R.id.preview_image);
        mPreviewImage.setVisibility(View.GONE);

        mPlayImage = rootView.findViewById(R.id.play_image);
        mPlayImage.setVisibility(View.GONE);
        mPlayImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mPlayImage.setVisibility(View.GONE);

                exoplayer.setPlayWhenReady(true);
            }
        });

        mProgressBar = rootView.findViewById(R.id.progress_bar);
        mImagesProgressBar = rootView.findViewById(R.id.images_progress_bar);
        mImagesProgressBar.setVisibility(View.GONE);

        //

        mBottomLayout = rootView.findViewById(R.id.bottom_layout);

        // Side menu

        mSideMenu = rootView.findViewById(R.id.side_menu);

        mProfileLayout = rootView.findViewById(R.id.profile_layout);
        mLikeLayout = rootView.findViewById(R.id.like_layout);
        mCommentLayout = rootView.findViewById(R.id.comment_layout);
        mCommentLayout.setVisibility(View.GONE);
        mShareLayout = rootView.findViewById(R.id.share_layout);
        mShareLayout.setVisibility(View.GONE);
        mItemActionsLayout = rootView.findViewById(R.id.item_actions_layout);
        mItemActionsLayout.setVisibility(View.GONE);
        mImagesLayout = rootView.findViewById(R.id.images_layout);
        mImagesLayout.setVisibility(View.GONE);
        mDetailsLayout = rootView.findViewById(R.id.details_layout);

        mProfileImage = rootView.findViewById(R.id.profile_image);
        mVerifiedImage = rootView.findViewById(R.id.verified_image);

        mLikeImage = rootView.findViewById(R.id.like_image);
        mImagesImage = rootView.findViewById(R.id.images_image);

        mLikesCount = rootView.findViewById(R.id.like_count);
        mCommentsCount = rootView.findViewById(R.id.comment_count);
        mImagesCount = rootView.findViewById(R.id.images_count);

        mDuration = rootView.findViewById(R.id.duration_text);
        mDuration.setVisibility(View.GONE);

        //

        mTagSelectingTextview = new TagSelectingTextview();

        //

        mCommentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(TAG_ITEM_ACTION_BOTTOM_SHEET);
                intent.putExtra("message", "comments");
                getContext().sendBroadcast(intent);

                if (exoplayer != null) {

                    mPlayImage.setVisibility(View.VISIBLE);

                    exoplayer.pause();
                }
            }
        });

        mImagesLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(TAG_ITEM_ACTION_BOTTOM_SHEET);
                intent.putExtra("message", "images");
                intent.setPackage(App.getInstance().getPackageName());
                getContext().sendBroadcast(intent);
            }
        });

        mDetailsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(TAG_ITEM_ACTION_BOTTOM_SHEET);
                intent.putExtra("message", "details");
                intent.setPackage(App.getInstance().getPackageName());
                getContext().sendBroadcast(intent);
            }
        });

        mShareLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(TAG_ITEM_ACTION_BOTTOM_SHEET);
                intent.putExtra("message", "share");
                intent.setPackage(App.getInstance().getPackageName());
                getContext().sendBroadcast(intent);
            }
        });

        mItemActionsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(TAG_ITEM_ACTION_BOTTOM_SHEET);
                intent.putExtra("message", "actions");
                intent.setPackage(App.getInstance().getPackageName());
                getContext().sendBroadcast(intent);
            }
        });

        mProfileLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getActivity(), ProfileActivity.class);
                intent.putExtra("profileId", item.getFromUserId());
                startActivity(intent);
            }
        });

        mLikeLayout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                if (App.getInstance().getId() == 0) {

                    Intent intent = new Intent(TAG_ITEM_ACTION_BOTTOM_SHEET);
                    intent.putExtra("message", "auth");
                    intent.setPackage(App.getInstance().getPackageName());
                    getContext().sendBroadcast(intent);

                } else {

                    if (item.isMyLike()) {

                        item.setMyLike(false);
                        item.setLikesCount(item.getLikesCount() - 1);

                    } else {

                        item.setMyLike(true);
                        item.setLikesCount(item.getLikesCount() + 1);
                    }

                    makeLike(item.getId(), 0);
                    updateView();
                }
            }
        });

        //

        mFullname = rootView.findViewById(R.id.fullname_text);
        mUsername = rootView.findViewById(R.id.username_text);
        mDescription = rootView.findViewById(R.id.desc_text);

        if (section_id == 0) {

            initPlayer();
        }

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {

                updateView();
            }

        },200);

        return rootView;
    }

    public void updateView() {

        if (isAdded()) {

//            if (item.getFromUserId() == App.getInstance().getId()) {
//
//                mItemActionsLayout.setVisibility(View.VISIBLE);
//            }

            if (section_id == 0) {

                mPreviewImage.setVisibility(View.GONE);
                mPlayerView.setVisibility(View.VISIBLE);

            } else {

                mPreviewImage.setVisibility(View.VISIBLE);
                mPlayerView.setVisibility(View.GONE);
                mProgressBar.setVisibility(View.GONE);

                mImagesProgressBar.setVisibility(View.VISIBLE);

                Picasso.with(getActivity())
                        .load(item.getImgUrl())
                        .into(mPreviewImage, new Callback() {

                            @Override
                            public void onSuccess() {

                                mImagesProgressBar.setVisibility(View.GONE);
                            }

                            @Override
                            public void onError() {

                                mImagesProgressBar.setVisibility(View.GONE);
                                mPreviewImage.setImageResource(R.drawable.img_loading);
                            }
                        });

                if (item.getImagesCount() != 0) {

                    mImagesImage.setImageResource(R.drawable.ic_images);
                    mImagesCount.setText(" +" + Integer.toString(item.getImagesCount()));

                } else {

                    mImagesImage.setImageResource(R.drawable.ic_resize);
                    mImagesCount.setText(getString(R.string.label_zoom));
                }

                mImagesLayout.setVisibility(View.VISIBLE);
            }

            mVerifiedImage.setVisibility(View.GONE);

            if (item.getFromUserVerify() == 1) {

                mVerifiedImage.setVisibility(View.VISIBLE);
            }

            if (item.getFromUserPhotoUrl() != null && item.getFromUserPhotoUrl().length() != 0) {

                imageLoader.get(item.getFromUserPhotoUrl(), ImageLoader.getImageListener(mProfileImage, R.drawable.profile_default_photo, R.drawable.profile_default_photo));
            }

            mFullname.setText(item.getFromUserFullname());
            mUsername.setText("@" + item.getFromUserUsername());

            if (item.getPost().length() != 0) {

                //mDescription.setText(item.getPost());
                mDescription.setMovementMethod(LinkMovementMethod.getInstance());

                String textHtml = item.getPost();

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {

                    mDescription.setText(mTagSelectingTextview.addClickablePart(Html.fromHtml(textHtml, Html.FROM_HTML_MODE_LEGACY).toString(), this, hashTagHyperLinkDisabled, getResources().getString(R.color.colorHashtag)), TextView.BufferType.SPANNABLE);

                } else {

                    mDescription.setText(mTagSelectingTextview.addClickablePart(Html.fromHtml(textHtml).toString(), this, hashTagHyperLinkDisabled, getResources().getString(R.color.colorHashtag)), TextView.BufferType.SPANNABLE);
                }

                mDescription.setVisibility(View.VISIBLE);

            } else {

                mDescription.setVisibility(View.GONE);
            }

            mCommentsCount.setText(Integer.toString(item.getCommentsCount()));
            mLikesCount.setText(Long.toString(item.getLikesCount()));

            if (item.isMyLike()) {

                mLikeImage.setColorFilter(getActivity().getResources().getColor(R.color.active_like), android.graphics.PorterDuff.Mode.SRC_IN);

            } else {

                mLikeImage.setColorFilter(getActivity().getResources().getColor(R.color.white), android.graphics.PorterDuff.Mode.SRC_IN);
            }

            if (isMainPage) {

                int actionBarHeight = 50;

                if (section_id != 0) {

                    actionBarHeight = 20;
                }

                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.MATCH_PARENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT
                );

                params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                params.setMargins(0, 0, 0, actionBarHeight);
                mBottomLayout.setLayoutParams(params);

                mBottomLayout.invalidate();
            }
        }
    }

    @Override
    public void clickedTag(CharSequence tag) {

        Intent i = new Intent(getActivity(), HashtagsActivity.class);
        i.putExtra("hashtag", tag);
        getActivity().startActivity(i);
    }

    private void initPlayer() {

        if (exoplayer == null && item != null) {

//            ExecutorService executorService = Executors.newSingleThreadExecutor();
//            executorService.execute(new Runnable() {
//                @Override
//                public void run() {
//
//
//
//                }
//            });

            LoadControl loadControl = new DefaultLoadControl.Builder()
                    .setAllocator(new DefaultAllocator(true, 16))
                    .setBufferDurationsMs(1 * 1024, 1 * 1024, 500, 1024)
                    .setTargetBufferBytes(-1)
                    .setPrioritizeTimeOverSizeThresholds(true)
                    .build();

            DefaultTrackSelector trackSelector = new DefaultTrackSelector(mContext);

            try {

                //exoplayer = new ExoPlayer.Builder(mContext).build();

                exoplayer = new ExoPlayer.Builder(mContext)
                        .setLooper(Looper.getMainLooper())
                        .setTrackSelector(trackSelector)
                        .setLoadControl(loadControl)
                        .build();

                DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(mContext, mContext.getString(R.string.app_name));
                MediaSource videoSource = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(MediaItem.fromUri(item.getVideoUrl()));
                //exoplayer.setThrowsWhenUsingWrongThread(false);

                //MediaItem mediaItem = MediaItem.fromUri(Uri.parse(item.getVideoUrl()));
                //exoplayer.addMediaItem(mediaItem);
                exoplayer.addMediaSource(videoSource);
                exoplayer.prepare();
                exoplayer.setRepeatMode(Player.REPEAT_MODE_ALL);

                exoplayer.addListener(new Player.Listener() {

                    @Override
                    public void onPlaybackStateChanged(@Player.State int state) {

                        if (state == Player.STATE_BUFFERING) {

                            Log.e("ItemFragment", "STATE_BUFFERING");

                            mProgressBar.setVisibility(View.VISIBLE);

                        } else if (state == Player.STATE_READY) {

                            Log.e("ItemFragment", "STATE_READY");

                            mPreviewImage.setVisibility(View.GONE);
                            mProgressBar.setVisibility(View.GONE);
                        }

                        Log.e("ItemFragment", Integer.toString(state));
                    }
                });

//                AudioAttributes audioAttributes = new AudioAttributes.Builder()
//                        .setUsage(C.USAGE_MEDIA)
//                        .setContentType(C.CONTENT_TYPE_MOVIE)
//                        .build();
//
//                exoplayer.setAudioAttributes(audioAttributes, true);

            }  catch (Exception e) {

                Log.d("ItemFragment","Exception audio focus : "+e);
            }

            getActivity().runOnUiThread(new Runnable() {

                @Override
                public void run() {

                    mPlayerView.setVisibility(View.VISIBLE);

                    if (exoplayer != null) {

                        mPlayerView.setPlayer(exoplayer);
                    }
                }
            });
        }
    }

    public void setPlayer(boolean isVisibleToUser) {

        if (exoplayer != null) {

            if (isVisibleToUser) {

                if (!isUpdateViews) {

                    isUpdateViews = true;
                }

                mPlayImage.setVisibility(View.GONE);

                exoplayer.setPlayWhenReady(true);

            } else {

                mPlayImage.setVisibility(View.VISIBLE);

                exoplayer.setPlayWhenReady(false);
                mPlayerView.setAlpha(1);
            }

            // Controller

            mPlayerView.setUseController(true);

            mPlayerView.setControllerShowTimeoutMs(0);
            mPlayerView.setControllerHideOnTouch(false);
            mPlayerView.showController();

            //

            mPlayerView.setOnTouchListener(new OnSwipeTouchListener(mContext) {

                public void onSwipeLeft() {

                    //openProfile(item, true);
                }

                @Override
                public void onLongClick() {

                    if (isVisibleToUser) {

                        //showVideoOption(item);
                    }
                }

                @Override
                public void onSingleClick() {

                    if (!exoplayer.getPlayWhenReady()) {

                        exoplayer.setPlayWhenReady(true);
                        //mPlayerView.setAlpha(0);
                        mPlayImage.setVisibility(View.GONE);

                    } else {

                        mPlayImage.setVisibility(View.VISIBLE);

                        exoplayer.setPlayWhenReady(false);
                        mPlayerView.setAlpha(1);
                    }
                }

                @Override
                public void onDoubleClick(MotionEvent e) {

                    if (!exoplayer.getPlayWhenReady()) {

                        exoplayer.setPlayWhenReady(true);
                        mPlayImage.setVisibility(View.GONE);
                    }

                    //likeVideo(item);
                }
            });
        }
    }

    public void releasePlayer() {

        if (exoplayer != null) {

            exoplayer.release();
            exoplayer = null;
        }
    }

    public void mainMenuVisibility(boolean isvisible) {

        if (exoplayer != null && isvisible) {

            exoplayer.setPlayWhenReady(true);

        } else if (exoplayer != null && !isvisible) {

            exoplayer.setPlayWhenReady(false);
            mPlayerView.setAlpha(1);
        }
    }

    @Override
    public void setMenuVisibility(final boolean visible) {

        isVisibleToUser = visible;

        Log.e("ItemFragment", "setMenuVisibility");

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {

            @Override
            public void run() {

                if (exoplayer != null && visible) {

                    Log.e("ItemFragment", "Looper.getMainLooper() run()");

                    setPlayer(isVisibleToUser);
                }
            }

        },200);

        if (visible) {

//            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
//
//                @Override
//                public void run() {
//
//                    if (view != null && getActivity()!= null) {
//
//                        getActivity().runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//
//                                if (item!=null && item.getId()!= null)
//                                {
//                                    setLikeData();
//
//                                }
//
//                            }
//                        });
//                    }
//                }
//            },200);
        }
    }

    @Override
    public void onResume() {

        super.onResume();

        Log.e("Dimon", " onResume Comments cnt");

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {

            getActivity().registerReceiver(mItemFragmentReceiver, new IntentFilter(TAG_UPDATE_VIDEO_ITEM), RECEIVER_NOT_EXPORTED);

        } else {

            getActivity().registerReceiver(mItemFragmentReceiver, new IntentFilter(TAG_UPDATE_VIDEO_ITEM));
        }
    }

    @Override
    public void onPause() {

        super.onPause();

        getActivity().unregisterReceiver(mItemFragmentReceiver);

        if (exoplayer != null) {

            mPlayImage.setVisibility(View.VISIBLE);

            exoplayer.setPlayWhenReady(false);
            mPlayerView.setAlpha(1);
        }
    }


    @Override
    public void onStop() {

        super.onStop();

        if (exoplayer != null) {

            mPlayImage.setVisibility(View.VISIBLE);

            exoplayer.setPlayWhenReady(false);
            mPlayerView.setAlpha(1);
        }
    }

    private BroadcastReceiver mItemFragmentReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            // Extract data included in the Intent
            String message = intent.getStringExtra("message");

            long itemId = intent.getLongExtra("itemId", 0);
            int commentsCnt = intent.getIntExtra("comments_cnt", 0);

            if (itemId == item.getId()) {

                if (isAdded()) {

                    item.setCommentsCount(commentsCnt);
                    mCommentsCount.setText(Integer.toString(item.getCommentsCount()));
                }
            }

        }
    };

    @Override
    public void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onDestroy() {

        releasePlayer();
        super.onDestroy();
    }

    @Override
    public void onAttach(Activity activity) {

        super.onAttach(activity);
    }

    @Override
    public void onDetach() {

        super.onDetach();
    }

    private void makeLike(final long itemId, final int reaction) {

        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_REACTIONS_MAKE, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {

                            if (!response.getBoolean("error")) {

//                                p.setLikesCount(response.getInt("likesCount"));
//                                p.setMyLike(response.getBoolean("myLike"));
                            }

                        } catch (JSONException e) {

                            e.printStackTrace();

                        } finally {

                            Log.e("Item.Reaction", response.toString());

                            // Interstitial ad

//                            if (App.getInstance().getInterstitialAdSettings().getInterstitialAdAfterNewLike() != 0 && App.getInstance().getAdmob() == ADMOB_DISABLED) {
//
//                                App.getInstance().getInterstitialAdSettings().setCurrentInterstitialAdAfterNewLike(App.getInstance().getInterstitialAdSettings().getCurrentInterstitialAdAfterNewLike() + 1);
//
//                                if (App.getInstance().getInterstitialAdSettings().getCurrentInterstitialAdAfterNewLike() >= App.getInstance().getInterstitialAdSettings().getInterstitialAdAfterNewLike()) {
//
//                                    App.getInstance().getInterstitialAdSettings().setCurrentInterstitialAdAfterNewLike(0);
//
//                                    App.getInstance().showInterstitialAd(null);
//                                }
//
//                                App.getInstance().saveData();
//                            }

                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Log.e("Item.Reaction", error.toString());
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("accountId", Long.toString(App.getInstance().getId()));
                params.put("accessToken", App.getInstance().getAccessToken());
                params.put("reaction", Integer.toString(reaction));
                params.put("itemId", Long.toString(itemId));

                return params;
            }
        };

        App.getInstance().addToRequestQueue(jsonReq);
    }
}