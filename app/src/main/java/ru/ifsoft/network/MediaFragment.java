package ru.ifsoft.network;

import static android.content.Context.RECEIVER_NOT_EXPORTED;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.balysv.materialripple.MaterialRippleLayout;


import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import github.ankushsachdeva.emojicon.EmojiconEditText;
import ru.ifsoft.network.adapter.CommentsListAdapter;
import ru.ifsoft.network.adapter.MediaSliderAdapter;
import ru.ifsoft.network.app.App;
import ru.ifsoft.network.constants.Constants;
import ru.ifsoft.network.model.Comment;
import ru.ifsoft.network.model.Item;
import ru.ifsoft.network.model.MediaItem;
import ru.ifsoft.network.util.CustomRequest;
import ru.ifsoft.network.util.Helper;

public class MediaFragment extends Fragment implements Constants, SwipeRefreshLayout.OnRefreshListener{

    private static final String STATE_LIST = "State Adapter Data";

    private ProgressDialog pDialog;

    private SwipeRefreshLayout mItemsContainer;

    private ViewPager2 mViewPager;

    private TextView mTabImages, mTabVideos, mMessage;
    private ProgressBar mProgressBar;
    private ImageButton mCloseButton;

    private ImageView mTabLang;

    private RelativeLayout mTabLayout;

    //
    private BottomSheetBehavior mBehavior;
    private BottomSheetDialog mBottomSheetDialog;
    private View mBottomSheet;

    long replyToUserId = 0; // For comments
    //

    GoogleSignInClient mGoogleSignInClient;
    ActivityResultLauncher<Intent> googleSigninActivityResultLauncher;

    //
    private int itemId = 0, arrayLength = 0;

    private Boolean loadingMore = false, viewMore = true;

    private ArrayList<Item> itemsList;

    private MediaSliderAdapter mPagerStateAdapter;

    private int sectionId = 0;
    private int position = 0;

    private Boolean isMainScreen = true;

    public MediaFragment() {

        // Required empty public constructor
    }

    public MediaFragment newInstance(Boolean isMainScreen) {

        MediaFragment myFragment = new MediaFragment();

        Bundle args = new Bundle();
        args.putBoolean("isMainScreen", isMainScreen);
        myFragment.setArguments(args);

        return myFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        //

        initpDialog();

        //

        Intent i = getActivity().getIntent();
        position = i.getIntExtra("position", 0);
        sectionId = i.getIntExtra("sectionId", 0);

        if (getArguments() != null) {

            isMainScreen = getArguments().getBoolean("isMainScreen", true);
        }

        if (savedInstanceState != null) {

            itemId = savedInstanceState.getInt("itemId");

        } else {

            itemId = 0;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_media, container, false);

        mItemsContainer = (SwipeRefreshLayout) rootView.findViewById(R.id.container_items);
        mItemsContainer.setProgressViewOffset(false, 200, 250);
        mItemsContainer.setOnRefreshListener(this);

        //

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(getActivity(), gso);

        googleSigninActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {

            @Override
            public void onActivityResult(ActivityResult result) {

                if (result.getResultCode() == Activity.RESULT_OK) {

                    // There are no request codes
                    Intent data = result.getData();

                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

                    try {

                        GoogleSignInAccount account = task.getResult(ApiException.class);

                        // Signed in successfully, show authenticated UI.

                        showpDialog();

                        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_ACCOUNT_OAUTH, null,
                                new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {

                                        if (App.getInstance().authorize(response)) {

                                            if (App.getInstance().getState() == ACCOUNT_STATE_ENABLED) {

                                                Intent intent = new Intent(getActivity(), MainActivity.class);
                                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                startActivity(intent);

                                            } else {

                                                if (App.getInstance().getState() == ACCOUNT_STATE_BLOCKED) {

                                                    App.getInstance().logout();
                                                    Toast.makeText(getActivity(), getText(R.string.msg_account_blocked), Toast.LENGTH_SHORT).show();

                                                } else {

                                                    App.getInstance().updateGeoLocation();

                                                    Intent intent = new Intent(getActivity(), MainActivity.class);
                                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                    startActivity(intent);
                                                }
                                            }

                                        } else {

                                            Toast.makeText(getActivity(), getString(R.string.error_data_loading), Toast.LENGTH_SHORT).show();
                                        }

                                        hidepDialog();
                                    }
                                }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {

                                Log.e("Google", "signInWithCredential:failure");

                                Toast.makeText(getActivity(), getText(R.string.error_data_loading), Toast.LENGTH_LONG).show();

                                hidepDialog();
                            }
                        }) {

                            @Override
                            protected Map<String, String> getParams() {
                                Map<String, String> params = new HashMap<String, String>();

                                params.put("client_id", CLIENT_ID);

                                params.put("account_id", Long.toString(App.getInstance().getId()));
                                params.put("access_token", App.getInstance().getAccessToken());

                                params.put("app_type", Integer.toString(APP_TYPE_ANDROID));
                                params.put("fcm_regId", App.getInstance().getGcmToken());

                                params.put("action", "");

                                params.put("uid", account.getId());
                                params.put("oauth_type", Integer.toString(OAUTH_TYPE_GOOGLE));
                                params.put("oauth_name", account.getDisplayName());
                                params.put("oauth_email", account.getEmail());

                                return params;
                            }
                        };

                        App.getInstance().addToRequestQueue(jsonReq);

                    } catch (ApiException e) {

                        // The ApiException status code indicates the detailed failure reason.
                        // Please refer to the GoogleSignInStatusCodes class reference for more information.
                        Log.e("Google", "Google sign in failed", e);
                    }
                }
            }
        });

        //

        // Prepare bottom sheet

        mBottomSheet = rootView.findViewById(R.id.bottom_sheet);
        mBehavior = BottomSheetBehavior.from(mBottomSheet);

        //

        mViewPager = rootView.findViewById(R.id.view_pager);
        //mViewPager.setOffscreenPageLimit(3);

//        mViewPager.setPageTransformer(true, new ViewPager.PageTransformer() {
//
//            @Override
//            public void transformPage(View page, float position) {
//
//                float translationX;
//                float scale;
//                float alpha;
//
//                if (position >= 1 || position <= -1) {
//                    // Fix for https://code.google.com/p/android/issues/detail?id=58918
//                    translationX = 0;
//                    scale = 1;
//                    alpha = 1;
//                } else if (position >= 0) {
//                    translationX = -page.getWidth() * position;
//                    scale = -0.2f * position + 1;
//                    alpha = Math.max(1 - position, 0);
//                } else {
//                    translationX = 0.5f * page.getWidth() * position;
//                    scale = 1.0f;
//                    alpha = Math.max(0.1f * position + 1, 0);
//                }
//
////                ViewHelper.setTranslationX(page, translationX);
////                ViewHelper.setScaleX(page, scale);
////                ViewHelper.setScaleY(page, scale);
////                ViewHelper.setAlpha(page, alpha);
//            }
//        });

        //

        //

        mTabLayout = rootView.findViewById(R.id.tab_layout);

        mTabImages = rootView.findViewById(R.id.tab_images);
        mTabImages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (App.getInstance().getId() == 0) {

                    showAuthorizationBottomSheet(BottomSheetBehavior.STATE_EXPANDED);

                } else {

                    mTabImages.setBackground(getActivity().getDrawable(R.drawable.top_menu_item_active));
                    mTabVideos.setBackground(getActivity().getDrawable(R.drawable.top_menu_item));

                    mTabImages.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.top_menu_active_text_size));
                    mTabVideos.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.top_menu_text_size));

                    itemId = 0;
                    loadingMore = false;
                    sectionId = 1;
                    getItems();
                }
            }
        });

        mTabVideos = rootView.findViewById(R.id.tab_videos);
        mTabVideos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mTabImages.setBackground(getActivity().getDrawable(R.drawable.top_menu_item));
                mTabVideos.setBackground(getActivity().getDrawable(R.drawable.top_menu_item_active));

                mTabImages.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.top_menu_text_size));
                mTabVideos.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.top_menu_active_text_size));

                itemId = 0;
                loadingMore = false;
                sectionId = 0;
                getItems();
            }
        });

        mMessage = rootView.findViewById(R.id.message_label);
        hideMessage();

        mProgressBar = rootView.findViewById(R.id.progress_bar);
        mProgressBar.setVisibility(View.GONE);

        mCloseButton = rootView.findViewById(R.id.close_button);
        mCloseButton.setVisibility(View.GONE);

        mCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                getActivity().finish();
            }
        });

        //

        init();

        return rootView;
    }

    public void init() {

        if (!isMainScreen) {

            mTabLayout.setVisibility(View.GONE);
            mCloseButton.setVisibility(View.VISIBLE);
            mItemsContainer.setEnabled(false);
        }

        mCloseButton.setVisibility(View.VISIBLE);

        itemsList = new ArrayList<Item>();

        mPagerStateAdapter = new MediaSliderAdapter(getChildFragmentManager(), mViewPager, getLifecycle());

        mViewPager.setAdapter(mPagerStateAdapter);
        //mViewPager.setOffscreenPageLimit(3);
        //mViewPager.setSaveFromParentEnabled(false);

        mViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }

            @Override
            public void onPageSelected(int position) {

                super.onPageSelected(position);

                if (position == 0) {

                    if (isMainScreen) {

                        mItemsContainer.setEnabled(true);
                    }

                } else {

                    mItemsContainer.setEnabled(false);
                }

                if (position == 0 && (mPagerStateAdapter != null && mPagerStateAdapter.getCount() > 0)) {

                    MediaItemFragment fragment = (MediaItemFragment) mPagerStateAdapter.getItem(mViewPager.getCurrentItem());
                    fragment.updateView();

                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            fragment.setPlayer(true);
                        }

                    }, 200);
                }

                Log.e("Dimon3", "callVideoApi() onPageSelected " + Integer.toString(position));

                if (!loadingMore && viewMore && position == itemsList.size() - 3) {

                    loadingMore = true;

                    getItems();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
            }
        });

//        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
//
//            @Override
//            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//
//            }
//
//            @Override
//            public void onPageSelected(int position) {
//
//                if (position == 0) {
//
//                    if (isMainScreen) {
//
//                        mItemsContainer.setEnabled(true);
//                    }
//
//                } else {
//
//                    mItemsContainer.setEnabled(false);
//                }
//
//                if (position == 0 && (mPagerStateAdapter != null && mPagerStateAdapter.getCount() > 0)) {
//
//                    ItemFragment fragment = (ItemFragment) mPagerStateAdapter.getItem(mViewPager.getCurrentItem());
//                    fragment.updateView();
//
//                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//
//                            fragment.setPlayer(true);
//                        }
//
//                    }, 200);
//                }
//
//                Log.e("Dimon3", "callVideoApi() onPageSelected " + Integer.toString(position));
//
//                if (!loadingMore && viewMore && position == itemsList.size() - 3) {
//
//                    loadingMore = true;
//
//                    getItems();
//                }
//            }
//
//            @Override
//            public void onPageScrollStateChanged(int state) {
//
//
//            }
//        });

        if (isMainScreen) {

            Log.e("isMainScreen", "isMainScreen");

            getItems();

        } else {

//            itemsList = (ArrayList<Item>)App.getInstance().getHomeItemsList().clone();
//
//            for (int i = 0; i < itemsList.size(); i++) {
//
//                mPagerStateAdapter.addFragment(new MediaItemFragment(itemsList.get(i), mViewPager, isMainScreen));
//            }

            loadingComplete();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);

        outState.putInt("itemId", itemId);
    }

    @Override
    public void onRefresh() {

        if (App.getInstance().isConnected()) {

            itemId = 0;
            getItems();

        } else {

            mItemsContainer.setRefreshing(false);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ITEM_EDIT && resultCode == getActivity().RESULT_OK) {

            int position = data.getIntExtra("position", 0);

            if (data.getExtras() != null) {

                Item item = (Item) data.getExtras().getParcelable("item");

                itemsList.set(position, item);
            }

            mPagerStateAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        super.onCreateOptionsMenu(menu, inflater);
    }

    public void getItems() {

        if (loadingMore) {

            mItemsContainer.setRefreshing(true);

        } else {

            mProgressBar.setVisibility(View.VISIBLE);
        }


        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_MEDIA_GET, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        if (!isAdded() || getActivity() == null) {

                            Log.e("ERROR", "MediaFragment Not Added to Activity");

                            return;
                        }

                        if (!loadingMore) {

                            Log.e("newPost", "asd");

                            itemsList.clear();

                            mPagerStateAdapter = new MediaSliderAdapter(getChildFragmentManager(), mViewPager, getLifecycle());
                            mViewPager.setAdapter(mPagerStateAdapter);
                        }

                        try {

                            arrayLength = 0;

                            if (!response.getBoolean("error")) {

                                itemId = response.getInt("itemId");

                                if (response.has("items")) {

                                    JSONArray itemsArray = response.getJSONArray("items");

                                    arrayLength = itemsArray.length();

                                    if (arrayLength > 0) {

                                        for (int i = 0; i < itemsArray.length(); i++) {

                                            JSONObject itemObj = (JSONObject) itemsArray.get(i);

                                            Item item = new Item(itemObj);

                                            Log.e("newPost cnt", Integer.toString(arrayLength));

                                            if (!App.getInstance().getDatabaseManager().isExist(item.getId())) {

                                                itemsList.add(item);
                                                mPagerStateAdapter.addFragment(new MediaItemFragment(item, mViewPager, isMainScreen, sectionId));
                                            }
                                        }
                                    }
                                }
                            }

                        } catch (JSONException e) {

                            e.printStackTrace();

                        } finally {

                            Log.e("NewPost", response.toString());

                            loadingComplete();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                if (!isAdded() || getActivity() == null) {

                    Log.e("ERROR", "MediaFragment Not Added to Activity");

                    return;
                }

                loadingComplete();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("account_id", Long.toString(App.getInstance().getId()));
                params.put("access_token", App.getInstance().getAccessToken());

                params.put("item_id", Integer.toString(itemId));
                params.put("section_id", Integer.toString(sectionId));
                params.put("language", "en");

                return params;
            }
        };

        App.getInstance().addToRequestQueue(jsonReq);
    }

    public void loadingComplete() {

        mProgressBar.setVisibility(View.GONE);

        if (arrayLength == LIST_ITEMS) {

            viewMore = true;

        } else {

            viewMore = false;
        }

        if (itemsList.size() == 0) {

            mPagerStateAdapter = new MediaSliderAdapter(getChildFragmentManager(), mViewPager, getLifecycle());
            mViewPager.setAdapter(mPagerStateAdapter);
        }

        mPagerStateAdapter.notifyDataSetChanged();

        if (mPagerStateAdapter.getCount() == 0) {

            if (MediaFragment.this.isVisible()) {

                showMessage(getText(R.string.label_empty_list).toString());
            }

        } else {

            hideMessage();

            if (!loadingMore) {

                if (isMainScreen) {

                    mViewPager.setCurrentItem(0, false);

                } else {

                    mViewPager.setCurrentItem(position, false);
                }
            }
        }

        loadingMore = false;
        mItemsContainer.setRefreshing(false);
    }

    public void showMessage(String message) {

        mMessage.setText(message);
        mMessage.setVisibility(View.VISIBLE);

        //mSplash.setVisibility(View.VISIBLE);
    }

    public void hideMessage() {

        mMessage.setVisibility(View.GONE);
        //mSplash.setVisibility(View.GONE);
    }

    @Override
    public void setMenuVisibility(final boolean visible) {
        super.setMenuVisibility(visible);
        //is_visible_to_user = visible;

        if (visible && mPagerStateAdapter != null && mPagerStateAdapter.getCount() > 0) {

            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {

                @Override
                public void run() {

                    MediaItemFragment fragment = (MediaItemFragment) mPagerStateAdapter.getItem(mViewPager.getCurrentItem());
                    fragment.mainMenuVisibility(visible);
                }

            },200);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public void showAuthorizationBottomSheet(int state) {


    }

    public void showItemInfo(Item item) {

        Intent intent = new Intent(getActivity(), ViewItemActivity.class);
        intent.putExtra("itemId", item.getId());
        startActivity(intent);
    }

    public void showItemActionBottomSheet(int state) {


    }

    public void showItemShareBottomSheet(int state) {


    }

    private void showItemCommentsDialog(int state, final Item item, final int item_position) {


    }

    private static void doKeepDialog(Dialog dialog){

        WindowManager.LayoutParams lp = new  WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        dialog.getWindow().setAttributes(lp);
    }

    protected void initpDialog() {

        pDialog = new ProgressDialog(getActivity());
        pDialog.setMessage(getString(R.string.msg_loading));
        pDialog.setCancelable(false);
    }

    protected void showpDialog() {

        if (!pDialog.isShowing()) pDialog.show();
    }

    protected void hidepDialog() {

        if (pDialog.isShowing()) pDialog.dismiss();
    }

    @Override
    public void onResume() {

        super.onResume();

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {

            getActivity().registerReceiver(mFragmentReceiver, new IntentFilter(TAG_ITEM_ACTION_BOTTOM_SHEET), RECEIVER_NOT_EXPORTED);

        } else {

            getActivity().registerReceiver(mFragmentReceiver, new IntentFilter(TAG_ITEM_ACTION_BOTTOM_SHEET));
        }
    }

    @Override
    public void onPause() {

        super.onPause();

        getActivity().unregisterReceiver(mFragmentReceiver);
    }

    @Override
    public void onStart() {

        super.onStart();

        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public void onStop() {

        super.onStop();

        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private BroadcastReceiver mFragmentReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            // Extract data included in the Intent
            String message = intent.getStringExtra("message");

            int currentPosition = mViewPager.getCurrentItem();
            Item p = itemsList.get(currentPosition);

            switch (message) {

                case "auth": {

                    showAuthorizationBottomSheet(BottomSheetBehavior.STATE_EXPANDED);

                    break;
                }

                case "details": {

                    showItemInfo(p);

                    break;
                }

                case "share": {

                    showItemShareBottomSheet(BottomSheetBehavior.STATE_EXPANDED);

                    break;
                }

                case "images": {

                    ArrayList<MediaItem> images = new ArrayList<>();
                    images.add(new MediaItem("", "", p.getImgUrl(), "", 0));

                    Intent i = new Intent(context, MediaViewerActivity.class);
                    i.putExtra("position", 0);
                    i.putExtra("itemId", p.getId());
                    i.putExtra("count", p.getImagesCount());
                    i.putParcelableArrayListExtra("images", images);
                    startActivity(i);

                    break;
                }

                case "comments": {

                    if (App.getInstance().getId() == 0) {

                        showAuthorizationBottomSheet(BottomSheetBehavior.STATE_EXPANDED);

                    } else {

                        showItemCommentsDialog(BottomSheetBehavior.STATE_EXPANDED, p, currentPosition);
                    }

                    break;
                }

                case "actions": {

                    showItemActionBottomSheet(BottomSheetBehavior.STATE_EXPANDED);

                    break;
                }

                default: {

                    break;
                }
            }
        }
    };
}