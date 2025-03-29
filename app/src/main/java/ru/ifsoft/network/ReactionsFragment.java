package ru.ifsoft.network;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.balysv.materialripple.MaterialRippleLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import ru.ifsoft.network.adapter.PeopleListAdapter;
import ru.ifsoft.network.adapter.PeopleListStyleAdapter;
import ru.ifsoft.network.app.App;
import ru.ifsoft.network.constants.Constants;
import ru.ifsoft.network.model.Profile;
import ru.ifsoft.network.util.CustomRequest;
import ru.ifsoft.network.util.Helper;
import ru.ifsoft.network.view.LineItemDecoration;


public class ReactionsFragment extends Fragment implements Constants, SwipeRefreshLayout.OnRefreshListener {

    private static final String STATE_LIST = "State Adapter Data";

    RecyclerView mRecyclerView;
    TextView mMessage;
    ImageView mSplash;

    SwipeRefreshLayout mItemsContainer;

    MaterialRippleLayout mReactionButtonAll, mReactionButton0, mReactionButton1, mReactionButton2, mReactionButton3, mReactionButton4, mReactionButton5;
    TextView mReaction0Text, mReaction1Text, mReaction2Text, mReaction3Text, mReaction4Text, mReaction5Text;
    LinearLayout mHeader;


    private ArrayList<Profile> itemsList;
    private PeopleListStyleAdapter itemsAdapter;

    long itemId = 0, reactionId = 0;
    int reaction = 100;
    private int arrayLength = 0;
    private Boolean loadingMore = false;
    private Boolean viewMore = false;
    private Boolean restore = false;

    int pastVisiblesItems = 0, visibleItemCount = 0, totalItemCount = 0;

    public ReactionsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        Intent i = getActivity().getIntent();

        itemId = i.getLongExtra("itemId", 0);

        if (savedInstanceState != null) {

            itemsList = savedInstanceState.getParcelableArrayList(STATE_LIST);
            itemsAdapter = new PeopleListStyleAdapter(getActivity(), itemsList);

            restore = savedInstanceState.getBoolean("restore");
            itemId = savedInstanceState.getLong("itemId");
            reactionId = savedInstanceState.getLong("reactionId");
            reaction = savedInstanceState.getInt("reaction");

        } else {

            itemsList = new ArrayList<Profile>();
            itemsAdapter = new PeopleListStyleAdapter(getActivity(), itemsList);

            restore = false;
            reactionId = 0;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_reactions, container, false);

        mItemsContainer = (SwipeRefreshLayout) rootView.findViewById(R.id.container_items);
        mItemsContainer.setOnRefreshListener(this);

        mMessage = (TextView) rootView.findViewById(R.id.message);
        mSplash = (ImageView) rootView.findViewById(R.id.splash);

        //

        mHeader = (LinearLayout) rootView.findViewById(R.id.header);

        mReaction0Text = (TextView) rootView.findViewById(R.id.reaction_0_count);
        mReaction1Text = (TextView) rootView.findViewById(R.id.reaction_1_count);
        mReaction2Text = (TextView) rootView.findViewById(R.id.reaction_2_count);
        mReaction3Text = (TextView) rootView.findViewById(R.id.reaction_3_count);
        mReaction4Text = (TextView) rootView.findViewById(R.id.reaction_4_count);
        mReaction5Text = (TextView) rootView.findViewById(R.id.reaction_5_count);

        mReactionButtonAll = (MaterialRippleLayout) rootView.findViewById(R.id.reaction_all_button);
        mReactionButton0 = (MaterialRippleLayout) rootView.findViewById(R.id.reaction_0_button);
        mReactionButton1 = (MaterialRippleLayout) rootView.findViewById(R.id.reaction_1_button);
        mReactionButton2 = (MaterialRippleLayout) rootView.findViewById(R.id.reaction_2_button);
        mReactionButton3 = (MaterialRippleLayout) rootView.findViewById(R.id.reaction_3_button);
        mReactionButton4 = (MaterialRippleLayout) rootView.findViewById(R.id.reaction_4_button);
        mReactionButton5 = (MaterialRippleLayout) rootView.findViewById(R.id.reaction_5_button);

        mReactionButton0.setVisibility(View.GONE);
        mReactionButton1.setVisibility(View.GONE);
        mReactionButton2.setVisibility(View.GONE);
        mReactionButton3.setVisibility(View.GONE);
        mReactionButton4.setVisibility(View.GONE);
        mReactionButton5.setVisibility(View.GONE);

        mReactionButtonAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                reaction = 100;
                reactionId = 0;

                getItems();
            }
        });

        mReactionButton0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                reaction = 0;
                reactionId = 0;

                getItems();
            }
        });

        mReactionButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                reaction = 1;
                reactionId = 0;

                getItems();
            }
        });

        mReactionButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                reaction = 2;
                reactionId = 0;

                getItems();
            }
        });

        mReactionButton3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                reaction = 3;
                reactionId = 0;

                getItems();
            }
        });

        mReactionButton4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                reaction = 4;
                reactionId = 0;

                getItems();
            }
        });

        mReactionButton5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                reaction = 5;
                reactionId = 0;

                getItems();
            }
        });

        //

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);

        final LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        //mRecyclerView.addItemDecoration(new LineItemDecoration(getActivity(), LinearLayout.VERTICAL));
        //mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), 0));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());


        mRecyclerView.setAdapter(itemsAdapter);

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

                if (dy > 0) {

                    visibleItemCount = mLayoutManager.getChildCount();
                    totalItemCount = mLayoutManager.getItemCount();
                    pastVisiblesItems = mLayoutManager.findFirstVisibleItemPosition();

                    if (!loadingMore) {

                        if ((visibleItemCount + pastVisiblesItems) >= totalItemCount && (viewMore) && !(mItemsContainer.isRefreshing())) {

                            loadingMore = true;
                            Log.e("...", "Last Item Wow !");

                            getItems();
                        }
                    }
                }
            }
        });

        mRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), mRecyclerView, new RecyclerItemClickListener.OnItemClickListener() {

            @Override
            public void onItemClick(View view, int position) {

                Profile u = (Profile) itemsList.get(position);

                Intent intent = new Intent(getActivity(), ProfileActivity.class);
                intent.putExtra("profileId", u.getId());
                startActivity(intent);
            }

            @Override
            public void onItemLongClick(View view, int position) {
                // ...
            }
        }));

        if (itemsAdapter.getItemCount() == 0) {

            showMessage(getText(R.string.label_empty_list).toString());

        } else {

            hideMessage();
        }

        if (!restore) {

            showMessage(getText(R.string.msg_loading_2).toString());

            getItems();
        }


        // Inflate the layout for this fragment
        return rootView;
    }

    @Override
    public void onRefresh() {

        if (App.getInstance().isConnected()) {

            reactionId = 0;

            getItems();

        } else {

            mItemsContainer.setRefreshing(false);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);

        outState.putBoolean("restore", true);
        outState.putLong("itemId", itemId);
        outState.putInt("reaction", reaction);
        outState.putLong("reactionId", reactionId);
        outState.putParcelableArrayList(STATE_LIST, itemsList);
    }

    public void getItems() {

        mItemsContainer.setRefreshing(true);

        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_REACTIONS_GET, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        if (!isAdded() || getActivity() == null) {

                            Log.e("ERROR", "ReactionsFragment Not Added to Activity");

                            return;
                        }

                        if (!loadingMore) {

                            itemsList.clear();
                        }

                        try {

                            arrayLength = 0;

                            if (!response.getBoolean("error")) {

                                reactionId = response.getInt("reactionId");

                                if (response.has("reactions")) {

                                    JSONObject reactionsObj = (JSONObject) response.getJSONObject("reactions");

                                    if (reactionsObj.has("type_0")) {

                                        if (reactionsObj.getInt("type_0") != 0) {

                                            mReactionButton0.setVisibility(View.VISIBLE);
                                            mReaction0Text.setText(Integer.toString(reactionsObj.getInt("type_0")));

                                        } else {

                                            mReactionButton0.setVisibility(View.GONE);
                                        }
                                    }

                                    if (reactionsObj.has("type_1")) {

                                        if (reactionsObj.getInt("type_1") != 0) {

                                            mReactionButton1.setVisibility(View.VISIBLE);
                                            mReaction1Text.setText(Integer.toString(reactionsObj.getInt("type_1")));

                                        } else {

                                            mReactionButton1.setVisibility(View.GONE);
                                        }
                                    }

                                    if (reactionsObj.has("type_2")) {

                                        if (reactionsObj.getInt("type_2") != 0) {

                                            mReactionButton2.setVisibility(View.VISIBLE);
                                            mReaction2Text.setText(Integer.toString(reactionsObj.getInt("type_2")));

                                        } else {

                                            mReactionButton2.setVisibility(View.GONE);
                                        }
                                    }

                                    if (reactionsObj.has("type_3")) {

                                        if (reactionsObj.getInt("type_3") != 0) {

                                            mReactionButton3.setVisibility(View.VISIBLE);
                                            mReaction3Text.setText(Integer.toString(reactionsObj.getInt("type_3")));

                                        } else {

                                            mReactionButton3.setVisibility(View.GONE);
                                        }
                                    }

                                    if (reactionsObj.has("type_4")) {

                                        if (reactionsObj.getInt("type_4") != 0) {

                                            mReactionButton4.setVisibility(View.VISIBLE);
                                            mReaction4Text.setText(Integer.toString(reactionsObj.getInt("type_4")));

                                        } else {

                                            mReactionButton4.setVisibility(View.GONE);
                                        }
                                    }

                                    if (reactionsObj.has("type_5")) {

                                        if (reactionsObj.getInt("type_5") != 0) {

                                            mReactionButton5.setVisibility(View.VISIBLE);
                                            mReaction5Text.setText(Integer.toString(reactionsObj.getInt("type_5")));

                                        } else {

                                            mReactionButton5.setVisibility(View.GONE);
                                        }
                                    }
                                }

                                if (response.has("items")) {

                                    JSONArray usersArray = response.getJSONArray("items");

                                    arrayLength = usersArray.length();

                                    if (arrayLength > 0) {

                                        for (int i = 0; i < usersArray.length(); i++) {

                                            JSONObject userObj = (JSONObject) usersArray.get(i);

                                            Profile profile = new Profile(userObj);

                                            itemsList.add(profile);
                                        }
                                    }
                                }
                            }

                        } catch (JSONException e) {

                            e.printStackTrace();

                        } finally {

                            loadingComplete();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                if (!isAdded() || getActivity() == null) {

                    Log.e("ERROR", "ReactionsFragment Not Added to Activity");

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

                params.put("reaction_id", Long.toString(reactionId));
                params.put("item_id", Long.toString(itemId));
                params.put("reaction", Integer.toString(reaction));
                params.put("language", "en");

                return params;
            }
        };

        App.getInstance().addToRequestQueue(jsonReq);
    }

    public void loadingComplete() {

        if (arrayLength == LIST_ITEMS) {

            viewMore = true;

        } else {

            viewMore = false;
        }

        itemsAdapter.notifyDataSetChanged();

        if (itemsAdapter.getItemCount() == 0) {

            showMessage(getText(R.string.label_empty_list).toString());

        } else {

            hideMessage();
        }

        loadingMore = false;
        mItemsContainer.setRefreshing(false);
    }

    public void showMessage(String message) {

        mMessage.setText(message);
        mMessage.setVisibility(View.VISIBLE);

        mSplash.setVisibility(View.VISIBLE);
    }

    public void hideMessage() {

        mMessage.setVisibility(View.GONE);

        mSplash.setVisibility(View.GONE);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    static class RecyclerItemClickListener implements RecyclerView.OnItemTouchListener {

        public interface OnItemClickListener {

            void onItemClick(View view, int position);

            void onItemLongClick(View view, int position);
        }

        private OnItemClickListener mListener;

        private GestureDetector mGestureDetector;

        public RecyclerItemClickListener(Context context, final RecyclerView recyclerView, OnItemClickListener listener) {

            mListener = listener;

            mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {

                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {

                    View childView = recyclerView.findChildViewUnder(e.getX(), e.getY());

                    if (childView != null && mListener != null) {

                        mListener.onItemLongClick(childView, recyclerView.getChildAdapterPosition(childView));
                    }
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView view, MotionEvent e) {

            View childView = view.findChildViewUnder(e.getX(), e.getY());

            if (childView != null && mListener != null && mGestureDetector.onTouchEvent(e)) {

                mListener.onItemClick(childView, view.getChildAdapterPosition(childView));
            }

            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView view, MotionEvent motionEvent) {

        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    }
}