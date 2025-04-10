package ru.ifsoft.network;

import static android.content.Context.RECEIVER_NOT_EXPORTED;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;

import com.balysv.materialripple.MaterialRippleLayout;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import github.ankushsachdeva.emojicon.EditTextImeBackListener;
import github.ankushsachdeva.emojicon.EmojiconEditText;
import github.ankushsachdeva.emojicon.EmojiconGridView;
import github.ankushsachdeva.emojicon.EmojiconsPopup;
import github.ankushsachdeva.emojicon.emoji.Emojicon;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import ru.ifsoft.network.adapter.ChatListAdapter;
import ru.ifsoft.network.adapter.FeelingsListAdapter;
import ru.ifsoft.network.adapter.StickerListAdapter;
import ru.ifsoft.network.app.App;
import ru.ifsoft.network.constants.Constants;
import ru.ifsoft.network.model.ChatItem;
import ru.ifsoft.network.model.Feeling;
import ru.ifsoft.network.model.Sticker;
import ru.ifsoft.network.util.Api;
import ru.ifsoft.network.util.CountingRequestBody;
import ru.ifsoft.network.util.CustomRequest;
import ru.ifsoft.network.util.Helper;


public class ChatFragment extends Fragment implements Constants {

    private static final String STATE_LIST = "State Adapter Data";

    public final static int STATUS_START = 100;

    public final static String PARAM_TASK = "task";
    public final static String PARAM_STATUS = "status";

    public final static String BROADCAST_ACTION = "com.raccoonsquare.network.chat";
    public final static String BROADCAST_ACTION_SEEN = "com.raccoonsquare.network.seen";
    public final static String BROADCAST_ACTION_TYPING_START = "com.raccoonsquare.network.typing_start";
    public final static String BROADCAST_ACTION_TYPING_END = "com.raccoonsquare.network.typing_end";

    final String LOG_TAG = "myLogs";

    public static final int RESULT_OK = -1;

    private ProgressDialog pDialog;

    Menu MainMenu;

    View mListViewHeader;

    RelativeLayout mLoadingScreen, mErrorScreen;
    LinearLayout mContentScreen, mTypingContainer, mContainerImg, mChatListViewHeaderContainer;

    ImageView mSendMessage, mActionContainerImg, mEmojiBtn, mDeleteImg, mPreviewImg;
    EmojiconEditText mMessageText;

    ListView listView;

    private BottomSheetBehavior mBehavior;
    private BottomSheetDialog mBottomSheetDialog;
    private View mBottomSheet;

    private ArrayList<Sticker> stickersList;
    private StickerListAdapter stickersAdapter;

    BroadcastReceiver br, br_seen, br_typing_start, br_typing_end;

    private ArrayList<ChatItem> chatList;

    private ChatListAdapter chatAdapter;

    String withProfile = "", messageText = "", messageImg = "", stickerImg = "";
    int chatId = 0, msgId = 0, messagesCount = 0, position = 0;
    long profileId = 0, stickerId = 0, lStickerId = 0;

    String lMessage = "", lMessageImage = "", lStickerImg = "";

    Boolean blocked = false;

    Boolean stickers_container_visible = false, actions_container_visible = false, img_container_visible = false;

    long fromUserId = 0, toUserId = 0;

    private String selectedImagePath = "";

    int arrayLength = 0;
    Boolean loadingMore = false;
    Boolean viewMore = false;

    private Boolean loading = false;
    private Boolean restore = false;
    private Boolean preload = false;
    private Boolean visible = true;

    private Boolean inboxTyping = false, outboxTyping = false;

    private String with_user_username = "", with_user_fullname = "", with_user_photo_url = "";
    private int with_user_state = 0, with_user_verified = 0;

    EmojiconsPopup popup;

    private ActivityResultLauncher<String> cameraPermissionLauncher;
    private ActivityResultLauncher<String[]> storagePermissionLauncher;
    private ActivityResultLauncher<Intent> imgFromGalleryActivityResultLauncher;
    private ActivityResultLauncher<Intent> imgFromCameraActivityResultLauncher;

    public ChatFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setRetainInstance(true);

        setHasOptionsMenu(true);

        initpDialog();

        Intent i = getActivity().getIntent();
        position = i.getIntExtra("position", 0);
        chatId = i.getIntExtra("chatId", 0);
        profileId = i.getLongExtra("profileId", 0);
        withProfile = i.getStringExtra("withProfile");

        with_user_username = i.getStringExtra("with_user_username");
        with_user_fullname = i.getStringExtra("with_user_fullname");
        with_user_photo_url = i.getStringExtra("with_user_photo_url");

        with_user_state = i.getIntExtra("with_user_state", 0);
        with_user_verified = i.getIntExtra("with_user_verified", 0);

        blocked = i.getBooleanExtra("blocked", false);

        fromUserId = i.getLongExtra("fromUserId", 0);
        toUserId = i.getLongExtra("toUserId", 0);

        chatList = new ArrayList<ChatItem>();
        chatAdapter = new ChatListAdapter(getActivity(), chatList);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_chat, container, false);

        //

        imgFromGalleryActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {

            @Override
            public void onActivityResult(ActivityResult result) {

                if (result.getResultCode() == Activity.RESULT_OK) {

                    // The document selected by the user won't be returned in the intent.
                    // Instead, a URI to that document will be contained in the return intent
                    // provided to this method as a parameter.  Pull that uri using "resultData.getData()"

                    if (result.getData() != null) {

                        Helper helper = new Helper(App.getInstance().getApplicationContext());
                        helper.createImage(result.getData().getData(), IMAGE_FILE);

                        selectedImagePath = new File(App.getInstance().getDirectory(), IMAGE_FILE).getPath();

                        mPreviewImg.setImageURI(null);
                        mPreviewImg.setImageURI(Uri.fromFile(new File(App.getInstance().getDirectory(), IMAGE_FILE)));

                        showImageContainer();
                    }
                }
            }
        });

        imgFromCameraActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {

            @Override
            public void onActivityResult(ActivityResult result) {

                if (result.getResultCode() == Activity.RESULT_OK) {

                    selectedImagePath = new File(App.getInstance().getDirectory(), IMAGE_FILE).getPath();

                    mPreviewImg.setImageURI(null);
                    mPreviewImg.setImageURI(Uri.fromFile(new File(App.getInstance().getDirectory(), IMAGE_FILE)));

                    showImageContainer();
                }
            }
        });

        cameraPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {

            if (isGranted) {

                // Permission is granted
                Log.e("Permissions", "Permission is granted");

                showMoreDialog();

            } else {

                // Permission is denied

                Log.e("Permissions", "denied");

                Snackbar.make(getView(), getString(R.string.label_no_camera_permission) , Snackbar.LENGTH_LONG).setAction(getString(R.string.action_settings), new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        Intent appSettingsIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + App.getInstance().getPackageName()));
                        startActivity(appSettingsIntent);

                        Toast.makeText(getActivity(), getString(R.string.label_grant_camera_permission), Toast.LENGTH_SHORT).show();
                    }

                }).show();
            }
        });

        storagePermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), (Map<String, Boolean> isGranted) -> {

            boolean granted = false;
            String storage_permission = Manifest.permission.READ_EXTERNAL_STORAGE;

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {

                storage_permission = Manifest.permission.READ_MEDIA_IMAGES;
            }

            for (Map.Entry<String, Boolean> x : isGranted.entrySet()) {

                if (x.getKey().equals(storage_permission)) {

                    if (x.getValue()) {

                        granted = true;
                    }
                }
            }

            if (granted) {

                Log.e("Permissions", "granted");

                showMoreDialog();

            } else {

                Log.e("Permissions", "denied");

                Snackbar.make(getView(), getString(R.string.label_no_storage_permission) , Snackbar.LENGTH_LONG).setAction(getString(R.string.action_settings), new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        Intent appSettingsIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + App.getInstance().getPackageName()));
                        startActivity(appSettingsIntent);

                        Toast.makeText(getActivity(), getString(R.string.label_grant_storage_permission), Toast.LENGTH_SHORT).show();
                    }

                }).show();
            }

        });

        //

        popup = new EmojiconsPopup(rootView, getActivity());

        popup.setSizeForSoftKeyboard();

        //Set on emojicon click listener
        popup.setOnEmojiconClickedListener(new EmojiconGridView.OnEmojiconClickedListener() {

            @Override
            public void onEmojiconClicked(Emojicon emojicon) {

                mMessageText.append(emojicon.getEmoji());
            }
        });

        //Set on backspace click listener
        popup.setOnEmojiconBackspaceClickedListener(new EmojiconsPopup.OnEmojiconBackspaceClickedListener() {

            @Override
            public void onEmojiconBackspaceClicked(View v) {

                KeyEvent event = new KeyEvent(0, 0, 0, KeyEvent.KEYCODE_DEL, 0, 0, 0, 0, KeyEvent.KEYCODE_ENDCALL);
                mMessageText.dispatchKeyEvent(event);
            }
        });

        //If the emoji popup is dismissed, change mEmojiBtn to emoji icon
        popup.setOnDismissListener(new PopupWindow.OnDismissListener() {

            @Override
            public void onDismiss() {

                setIconEmojiKeyboard();
            }
        });

        //If the text keyboard closes, also dismiss the emoji popup
        popup.setOnSoftKeyboardOpenCloseListener(new EmojiconsPopup.OnSoftKeyboardOpenCloseListener() {

            @Override
            public void onKeyboardOpen(int keyBoardHeight) {

            }

            @Override
            public void onKeyboardClose() {

                if(popup.isShowing())

                    popup.dismiss();
            }
        });

        //On emoji clicked, add it to edittext
        popup.setOnEmojiconClickedListener(new EmojiconGridView.OnEmojiconClickedListener() {

            @Override
            public void onEmojiconClicked(Emojicon emojicon) {

                mMessageText.append(emojicon.getEmoji());
            }
        });

        //On backspace clicked, emulate the KEYCODE_DEL key event
        popup.setOnEmojiconBackspaceClickedListener(new EmojiconsPopup.OnEmojiconBackspaceClickedListener() {

            @Override
            public void onEmojiconBackspaceClicked(View v) {

                KeyEvent event = new KeyEvent(0, 0, 0, KeyEvent.KEYCODE_DEL, 0, 0, 0, 0, KeyEvent.KEYCODE_ENDCALL);
                mMessageText.dispatchKeyEvent(event);
            }
        });




        if (savedInstanceState != null) {

            restore = savedInstanceState.getBoolean("restore");
            loading = savedInstanceState.getBoolean("loading");
            preload = savedInstanceState.getBoolean("preload");

            stickers_container_visible = savedInstanceState.getBoolean("stickers_container_visible");
            actions_container_visible = savedInstanceState.getBoolean("actions_container_visible");
            img_container_visible = savedInstanceState.getBoolean("img_container_visible");

            stickersList = savedInstanceState.getParcelableArrayList(STATE_LIST);
            stickersAdapter = new StickerListAdapter(getActivity(), stickersList);

        } else {

            stickersList = new ArrayList<Sticker>();
            stickersAdapter = new StickerListAdapter(getActivity(), stickersList);

            App.getInstance().setCurrentChatId(chatId);

            restore = false;
            loading = false;
            preload = false;

            stickers_container_visible = false;
            actions_container_visible = false;
            img_container_visible = false;
        }

        br_typing_start = new BroadcastReceiver() {

            public void onReceive(Context context, Intent intent) {

                int task = intent.getIntExtra(PARAM_TASK, 0);
                int status = intent.getIntExtra(PARAM_STATUS, 0);

                typing_start();
            }
        };

        IntentFilter intFilt4 = new IntentFilter(BROADCAST_ACTION_TYPING_START);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {

            getActivity().registerReceiver(br_typing_start, intFilt4, RECEIVER_NOT_EXPORTED);

        } else {

            getActivity().registerReceiver(br_typing_start, intFilt4);
        }

        br_typing_end = new BroadcastReceiver() {

            public void onReceive(Context context, Intent intent) {

                int task = intent.getIntExtra(PARAM_TASK, 0);
                int status = intent.getIntExtra(PARAM_STATUS, 0);

                typing_end();
            }
        };

        IntentFilter intFilt3 = new IntentFilter(BROADCAST_ACTION_TYPING_END);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {

            getActivity().registerReceiver(br_typing_end, intFilt3, RECEIVER_NOT_EXPORTED);

        } else {

            getActivity().registerReceiver(br_typing_end, intFilt3);
        }

        br_seen = new BroadcastReceiver() {

            public void onReceive(Context context, Intent intent) {

                int task = intent.getIntExtra(PARAM_TASK, 0);
                int status = intent.getIntExtra(PARAM_STATUS, 0);

                seen();
            }
        };

        IntentFilter intFilt2 = new IntentFilter(BROADCAST_ACTION_SEEN);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {

            getActivity().registerReceiver(br_seen, intFilt2, RECEIVER_NOT_EXPORTED);

        } else {

            getActivity().registerReceiver(br_seen, intFilt2);
        }

        br = new BroadcastReceiver() {

            public void onReceive(Context context, Intent intent) {

                int task = intent.getIntExtra(PARAM_TASK, 0);
                int status = intent.getIntExtra(PARAM_STATUS, 0);

                int msgId = intent.getIntExtra("msgId", 0);
                long msgFromUserId = intent.getLongExtra("msgFromUserId", 0);
                int msgFromUserState = intent.getIntExtra("msgFromUserState", 0);
                int msgFromUserVerify = intent.getIntExtra("msgFromUserVerify", 0);
                String msgFromUserUsername = intent.getStringExtra("msgFromUserUsername");
                String msgFromUserFullname = intent.getStringExtra("msgFromUserFullname");
                String msgFromUserPhotoUrl = intent.getStringExtra("msgFromUserPhotoUrl");
                String msgMessage = intent.getStringExtra("msgMessage");
                String msgImgUrl = intent.getStringExtra("msgImgUrl");
                int msgCreateAt = intent.getIntExtra("msgCreateAt", 0);
                String msgDate = intent.getStringExtra("msgDate");
                String msgTimeAgo = intent.getStringExtra("msgTimeAgo");

                ChatItem c = new ChatItem();
                c.setId(msgId);
                c.setFromUserId(msgFromUserId);

                if (msgFromUserId == App.getInstance().getId()) {

                    c.setFromUserState(App.getInstance().getState());
                    c.setFromUserVerify(App.getInstance().getVerify());
                    c.setFromUserUsername(App.getInstance().getUsername());
                    c.setFromUserFullname(App.getInstance().getFullname());
                    c.setFromUserPhotoUrl(App.getInstance().getPhotoUrl());

                } else {

                    c.setFromUserState(with_user_state);
                    c.setFromUserVerify(with_user_verified);
                    c.setFromUserUsername(with_user_username);
                    c.setFromUserFullname(with_user_fullname);
                    c.setFromUserPhotoUrl(with_user_photo_url);
                }

                c.setMessage(msgMessage);
                c.setImgUrl(msgImgUrl);
                c.setCreateAt(msgCreateAt);
                c.setDate(msgDate);
                c.setTimeAgo(msgTimeAgo);

                Log.e(LOG_TAG, "onReceive: task = " + task + ", status = " + status + " " + c.getMessage() + " " + Integer.toString(c.getId()));



                final ChatItem lastItem = (ChatItem) listView.getAdapter().getItem(listView.getAdapter().getCount() - 1);

                messagesCount = messagesCount + 1;

                chatList.add(c);

                if (!visible) {

                    try {

                        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                        Ringtone r = RingtoneManager.getRingtone(getActivity(), notification);
                        r.play();

                    } catch (Exception e) {

                        e.printStackTrace();
                    }
                }

                chatAdapter.notifyDataSetChanged();

                scrollListViewToBottom();

                if (inboxTyping) typing_end();

                seen();

                sendNotify(GCM_NOTIFY_SEEN);
            }
        };

        IntentFilter intFilt = new IntentFilter(BROADCAST_ACTION);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {

            getActivity().registerReceiver(br, intFilt, RECEIVER_NOT_EXPORTED);

        } else {

            getActivity().registerReceiver(br, intFilt);
        }

        if (loading) {

            showpDialog();
        }

        mLoadingScreen = (RelativeLayout) rootView.findViewById(R.id.loadingScreen);
        mErrorScreen = (RelativeLayout) rootView.findViewById(R.id.errorScreen);

        mContentScreen = (LinearLayout) rootView.findViewById(R.id.contentScreen);

        mSendMessage = (ImageView) rootView.findViewById(R.id.sendMessage);
        mMessageText = (EmojiconEditText) rootView.findViewById(R.id.messageText);

        mSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                newMessage();
            }
        });

        listView = (ListView) rootView.findViewById(R.id.listView);

        listView.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);

        mListViewHeader = getActivity().getLayoutInflater().inflate(R.layout.chat_listview_header, null);
        mChatListViewHeaderContainer = (LinearLayout) mListViewHeader.findViewById(R.id.chatListViewHeaderContainer);

        listView.addHeaderView(mListViewHeader);

        mListViewHeader.setVisibility(View.GONE);

        listView.setAdapter(chatAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (position == 0 && mListViewHeader.getVisibility() == View.VISIBLE) {

                    getPreviousMessages();
                }
            }
        });

        mActionContainerImg = (ImageView) rootView.findViewById(R.id.actionContainerImg);

        mTypingContainer = (LinearLayout) rootView.findViewById(R.id.container_typing);

        mTypingContainer.setVisibility(View.GONE);

        mEmojiBtn = (ImageView) rootView.findViewById(R.id.emojiBtn);
        mDeleteImg = (ImageView) rootView.findViewById(R.id.deleteImg);
        mPreviewImg = (ImageView) rootView.findViewById(R.id.previewImg);

        mBottomSheet = rootView.findViewById(R.id.bottom_sheet);
        mBehavior = BottomSheetBehavior.from(mBottomSheet);

        mContainerImg = (LinearLayout) rootView.findViewById(R.id.container_img);
        mContainerImg.setVisibility(View.GONE);

        mDeleteImg.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

                selectedImagePath = "";

                hideImageContainer();
            }
        });

        mActionContainerImg.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                showMoreDialog();
            }
        });

        if (selectedImagePath != null && selectedImagePath.length() > 0) {

            mPreviewImg.setImageURI(FileProvider.getUriForFile(App.getInstance().getApplicationContext(), App.getInstance().getPackageName() + ".provider", new File(selectedImagePath)));

            showImageContainer();
        }

        if (!EMOJI_KEYBOARD) {

            mEmojiBtn.setVisibility(View.GONE);
        }

        mEmojiBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (img_container_visible) {

                    mActionContainerImg.setVisibility(View.GONE);
                }

                //If popup is not showing => emoji keyboard is not visible, we need to show it
                if (!popup.isShowing()) {

                    //If keyboard is visible, simply show the emoji popup
                    if (popup.isKeyBoardOpen()){

                        popup.showAtBottom();
                        setIconSoftKeyboard();

                    } else {

                        //else, open the text keyboard first and immediately after that show the emoji popup
                        mMessageText.setFocusableInTouchMode(true);
                        mMessageText.requestFocus();
                        popup.showAtBottomPending();

                        final InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputMethodManager.showSoftInput(mMessageText, InputMethodManager.SHOW_IMPLICIT);
                        setIconSoftKeyboard();
                    }

                } else {

                    //If popup is showing, simply dismiss it to show the undelying text keyboard
                    popup.dismiss();
                }
            }
        });

        EditTextImeBackListener er = new EditTextImeBackListener() {

            @Override
            public void onImeBack(EmojiconEditText ctrl, String text) {

                hideEmojiKeyboard();
            }
        };

        mMessageText.setOnEditTextImeBackListener(er);

        mMessageText.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {

                String txt = mMessageText.getText().toString();

                if (txt.length() == 0 && outboxTyping) {

                    outboxTyping = false;

                    sendNotify(GCM_NOTIFY_TYPING_END);

                } else {

                    if (!outboxTyping && txt.length() > 0) {

                        outboxTyping = true;

                        sendNotify(GCM_NOTIFY_TYPING_START);
                    }
                }

                Log.e("", "afterTextChanged");
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                //Log.e("", "beforeTextChanged");
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {

                //Log.e("", "onTextChanged");
            }
        });

        if (inboxTyping) {

            mTypingContainer.setVisibility(View.VISIBLE);

        } else {

            mTypingContainer.setVisibility(View.GONE);
        }

        if (!restore) {

            if (App.getInstance().isConnected()) {

                showLoadingScreen();
                getChat();

            } else {

                showErrorScreen();
            }

        } else {

            if (App.getInstance().isConnected()) {

                if (!preload) {

                    showContentScreen();

                } else {

                    showLoadingScreen();
                }

            } else {

                showErrorScreen();
            }
        }

        // Inflate the layout for this fragment
        return rootView;
    }

    public void typing_start() {

        inboxTyping = true;

        mTypingContainer.setVisibility(View.VISIBLE);
    }

    public void typing_end() {

        mTypingContainer.setVisibility(View.GONE);

        inboxTyping = false;
    }

    public void seen() {

        if (chatAdapter.getCount() > 0) {

            for (int i = 0; i < chatAdapter.getCount(); i++) {

                ChatItem item = chatList.get(i);

                if (item.getFromUserId() == App.getInstance().getId()) {

                    chatList.get(i).setSeenAt(1);
                }
            }
        }

        chatAdapter.notifyDataSetChanged();
    }

    public void sendNotify(final int notifyId) {

        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_CHAT_NOTIFY, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        if (!isAdded() || getActivity() == null) {

                            Log.e("ERROR", "ChatFragment Not Added to Activity");

                            return;
                        }

                        try {

                            if (!response.getBoolean("error")) {

                            }

                        } catch (JSONException e) {

                            e.printStackTrace();

                        } finally {

                            Log.d("send fcm", response.toString());
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                if (!isAdded() || getActivity() == null) {

                    Log.e("ERROR", "ChatFragment Not Added to Activity");

                    return;
                }

                Log.e("send fcm error", error.toString());
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("accountId", Long.toString(App.getInstance().getId()));
                params.put("accessToken", App.getInstance().getAccessToken());
                params.put("chatId", Integer.toString(chatId));
                params.put("notifyId", Integer.toString(notifyId));
                params.put("chatFromUserId", Long.toString(fromUserId));
                params.put("chatToUserId", Long.toString(toUserId));

                return params;
            }
        };

        App.getInstance().addToRequestQueue(jsonReq);
    }

    public void hideEmojiKeyboard() {

        popup.dismiss();
    }

    public void setIconEmojiKeyboard() {

        mEmojiBtn.setBackgroundResource(R.drawable.ic_emoji);
    }

    public void setIconSoftKeyboard() {

        mEmojiBtn.setBackgroundResource(R.drawable.ic_keyboard);
    }

    public void onDestroyView() {

        super.onDestroyView();

        getActivity().unregisterReceiver(br);

        getActivity().unregisterReceiver(br_seen);

        getActivity().unregisterReceiver(br_typing_start);

        getActivity().unregisterReceiver(br_typing_end);

        hidepDialog();
    }

    @Override
    public void onResume() {

        super.onResume();

        visible = true;
    }

    @Override
    public void onPause() {

        super.onPause();

        visible = false;
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
    public void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);

        outState.putBoolean("restore", true);
        outState.putBoolean("loading", loading);
        outState.putBoolean("preload", preload);

        outState.putBoolean("stickers_container_visible", stickers_container_visible);
        outState.putBoolean("actions_container_visible", actions_container_visible);
        outState.putBoolean("img_container_visible", img_container_visible);

        outState.putParcelableArrayList(STATE_LIST, stickersList);
    }

    private void scrollListViewToBottom() {

        listView.smoothScrollToPosition(chatAdapter.getCount());

        listView.post(new Runnable() {
            @Override
            public void run() {
                // Select the last row so it will scroll into view...
                listView.setSelection(chatAdapter.getCount() - 1);
            }
        });
    }

    public void updateChat() {

        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_CHAT_UPDATE, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        if (!isAdded() || getActivity() == null) {

                            Log.e("ERROR", "ChatFragment Not Added to Activity");

                            return;
                        }

                        try {

                            if (!response.getBoolean("error")) {

                            }

                        } catch (JSONException e) {

                            e.printStackTrace();

                        } finally {

                            Log.e("TAG", response.toString());
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                if (!isAdded() || getActivity() == null) {

                    Log.e("ERROR", "ChatFragment Not Added to Activity");

                    return;
                }

                preload = false;
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("accountId", Long.toString(App.getInstance().getId()));
                params.put("accessToken", App.getInstance().getAccessToken());

                params.put("chatId", Integer.toString(chatId));

                params.put("chatFromUserId", Long.toString(fromUserId));
                params.put("chatToUserId", Long.toString(toUserId));

                return params;
            }
        };

        App.getInstance().addToRequestQueue(jsonReq);
    }

    public void getChat() {

        preload = true;

        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_CHAT_GET, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        if (!isAdded() || getActivity() == null) {

                            Log.e("ERROR", "ChatFragment Not Added to Activity");

                            return;
                        }

                        try {

                            if (!response.getBoolean("error")) {

                                msgId = response.getInt("msgId");
                                chatId = response.getInt("chatId");
                                messagesCount = response.getInt("messagesCount");

                                App.getInstance().setCurrentChatId(chatId);

                                fromUserId = response.getLong("chatFromUserId");
                                toUserId = response.getLong("chatToUserId");

                                if (messagesCount > 20) {

                                    mListViewHeader.setVisibility(View.VISIBLE);
                                }

                                if (response.has("newMessagesCount")) {

                                    App.getInstance().setMessagesCount(response.getInt("newMessagesCount"));
                                }

                                if (response.has("messages")) {

                                    JSONArray messagesArray = response.getJSONArray("messages");

                                    arrayLength = messagesArray.length();

                                    if (arrayLength > 0) {

                                        for (int i = messagesArray.length() - 1; i > -1; i--) {

                                            JSONObject msgObj = (JSONObject) messagesArray.get(i);

                                            ChatItem item = new ChatItem(msgObj);

                                            chatList.add(item);
                                        }
                                    }
                                }
                            }

                        } catch (JSONException e) {

                            e.printStackTrace();

                        } finally {

                            showContentScreen();

                            chatAdapter.notifyDataSetChanged();

                            scrollListViewToBottom();

                            updateChat();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                if (!isAdded() || getActivity() == null) {

                    Log.e("ERROR", "ChatFragment Not Added to Activity");

                    return;
                }

                preload = false;
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("accountId", Long.toString(App.getInstance().getId()));
                params.put("accessToken", App.getInstance().getAccessToken());

                params.put("profileId", Long.toString(profileId));

                params.put("chatId", Integer.toString(chatId));
                params.put("msgId", Integer.toString(msgId));

                params.put("chatFromUserId", Long.toString(fromUserId));
                params.put("chatToUserId", Long.toString(toUserId));

                return params;
            }
        };

        App.getInstance().addToRequestQueue(jsonReq);
    }

    public void getPreviousMessages() {

        loading = true;

        showpDialog();

        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_CHAT_GET_PREVIOUS, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        if (!isAdded() || getActivity() == null) {

                            Log.e("ERROR", "ChatFragment Not Added to Activity");

                            return;
                        }

                        try {

                            if (!response.getBoolean("error")) {

                                msgId = response.getInt("msgId");
                                chatId = response.getInt("chatId");

                                if (response.has("messages")) {

                                    JSONArray messagesArray = response.getJSONArray("messages");

                                    arrayLength = messagesArray.length();

                                    if (arrayLength > 0) {

                                        for (int i = 0; i < messagesArray.length(); i++) {

                                            JSONObject msgObj = (JSONObject) messagesArray.get(i);

                                            ChatItem item = new ChatItem(msgObj);

                                            chatList.add(0, item);
                                        }
                                    }
                                }
                            }

                        } catch (JSONException e) {

                            e.printStackTrace();

                        } finally {

                            loading = false;

                            hidepDialog();

                            chatAdapter.notifyDataSetChanged();

                            if (messagesCount <= listView.getAdapter().getCount() - 1) {

                                mListViewHeader.setVisibility(View.GONE);

                            } else {

                                mListViewHeader.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                if (!isAdded() || getActivity() == null) {

                    Log.e("ERROR", "ChatFragment Not Added to Activity");

                    return;
                }

                loading = false;

                hidepDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("accountId", Long.toString(App.getInstance().getId()));
                params.put("accessToken", App.getInstance().getAccessToken());

                params.put("profileId", Long.toString(profileId));

                params.put("chatId", Integer.toString(chatId));
                params.put("msgId", Integer.toString(msgId));

                params.put("chatFromUserId", Long.toString(fromUserId));
                params.put("chatToUserId", Long.toString(toUserId));

                return params;
            }
        };

        App.getInstance().addToRequestQueue(jsonReq);
    }

    public void newMessage() {

        if (App.getInstance().isConnected()) {

            messageText = mMessageText.getText().toString();
            messageText = messageText.trim();

            if (selectedImagePath.length() != 0) {

                loading = true;

                pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                pDialog.setProgressNumberFormat(null);
                pDialog.setProgress(0);
                pDialog.setMax(100);

                showpDialog();

                File f = new File(selectedImagePath);

                uploadFile(METHOD_MSG_UPLOAD_IMG, f);

            } else {

                if (messageText.length() > 0) {

                    loading = true;

//                    showpDialog();

                    send();

                } else {

                    Toast toast= Toast.makeText(getActivity(), getText(R.string.msg_enter_msg), Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
            }

        } else {

            Toast toast= Toast.makeText(getActivity(), getText(R.string.msg_network_error), Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        }
    }

    public void send() {

        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_MSG_NEW, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        if (!isAdded() || getActivity() == null) {

                            Log.e("ERROR", "ChatFragment Not Added to Activity");

                            return;
                        }

                        try {

                            if (!response.getBoolean("error")) {

                                chatId = response.getInt("chatId");

                                App.getInstance().setCurrentChatId(chatId);

                                if (response.has("chatFromUserId")) {

                                    fromUserId = response.getLong("chatFromUserId");
                                }

                                if (response.has("chatToUserId")) {

                                    toUserId = response.getLong("chatToUserId");
                                }

                                if (response.has("message")) {

                                    JSONObject msgObj = (JSONObject) response.getJSONObject("message");

                                    ChatItem item = new ChatItem(msgObj);

                                    item.setListId(response.getInt("listId"));
                                }

                            } else {

                                Toast.makeText(getActivity(), getString(R.string.msg_send_msg_error), Toast.LENGTH_SHORT).show();
                            }

                        } catch (JSONException e) {

                            e.printStackTrace();

                        } finally {

                            loading = false;

                            hidepDialog();

                            messageText = "";
                            messageImg = "";
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                if (!isAdded() || getActivity() == null) {

                    Log.e("ERROR", "ChatFragment Not Added to Activity");

                    return;
                }

                messageText = "";
                messageImg = "";

                loading = false;

                hidepDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("accountId", Long.toString(App.getInstance().getId()));
                params.put("accessToken", App.getInstance().getAccessToken());

                params.put("profileId", Long.toString(profileId));

                params.put("chatId", Integer.toString(chatId));
                params.put("messageText", lMessage);
                params.put("messageImg", lMessageImage);

                params.put("listId", Integer.toString(listView.getAdapter().getCount()));

                params.put("chatFromUserId", Long.toString(fromUserId));
                params.put("chatToUserId", Long.toString(toUserId));

                params.put("stickerImgUrl", lStickerImg);
                params.put("stickerId", Long.toString(lStickerId));

                return params;
            }
        };

        lMessage = messageText;
        lMessageImage = messageImg;
        lStickerImg = stickerImg;
        lStickerId = stickerId;

        if (stickerId != 0) {

            messageImg = stickerImg;

            lMessage = "";
            lMessageImage = "";

            messageText = "";
        }

        ChatItem cItem = new ChatItem();

        cItem.setListId(listView.getAdapter().getCount());
        cItem.setId(0);
        cItem.setFromUserId(App.getInstance().getId());
        cItem.setFromUserState(ACCOUNT_STATE_ENABLED);
        cItem.setFromUserUsername(App.getInstance().getUsername());
        cItem.setFromUserFullname(App.getInstance().getFullname());
        cItem.setFromUserPhotoUrl(App.getInstance().getPhotoUrl());
        cItem.setMessage(messageText);
        cItem.setStickerId(stickerId);
        cItem.setStickerImgUrl(stickerImg);
        cItem.setImgUrl(messageImg);
        cItem.setTimeAgo(getActivity().getString(R.string.label_just_now));

        chatList.add(cItem);

        chatAdapter.notifyDataSetChanged();

        scrollListViewToBottom();

        int socketTimeout = 0;//0 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        jsonReq.setRetryPolicy(policy);

        App.getInstance().addToRequestQueue(jsonReq);

        outboxTyping = false;

        mContainerImg.setVisibility(View.GONE);
        selectedImagePath = "";
        messageImg = "";
        mMessageText.setText("");
        messagesCount++;

        stickerImg = "";
        stickerId = 0;

        hideImageContainer();
    }

    public void deleteChat() {

        loading = true;

        showpDialog();

        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_CHAT_REMOVE, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        if (!isAdded() || getActivity() == null) {

                            Log.e("ERROR", "ChatFragment Not Added to Activity");

                            return;
                        }

                        try {

                            if (!response.getBoolean("error")) {

                                Intent i = new Intent();
                                i.putExtra("action", "Delete");
                                i.putExtra("position", position);
                                i.putExtra("chatId", chatId);
                                getActivity().setResult(RESULT_OK, i);

                                getActivity().finish();

//                                Toast.makeText(getActivity(), getString(R.string.msg_send_msg_error), Toast.LENGTH_SHORT).show();
                            }

                        } catch (JSONException e) {

                            e.printStackTrace();

                        } finally {

                            loading = false;

                            hidepDialog();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                if (!isAdded() || getActivity() == null) {

                    Log.e("ERROR", "ChatFragment Not Added to Activity");

                    return;
                }

                loading = false;

                hidepDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("accountId", Long.toString(App.getInstance().getId()));
                params.put("accessToken", App.getInstance().getAccessToken());

                params.put("profileId", Long.toString(profileId));
                params.put("chatId", Integer.toString(chatId));

                return params;
            }
        };

        App.getInstance().addToRequestQueue(jsonReq);
    }

    public void showLoadingScreen() {

        mContentScreen.setVisibility(View.GONE);
        mErrorScreen.setVisibility(View.GONE);

        mLoadingScreen.setVisibility(View.VISIBLE);
    }

    public void showErrorScreen() {

        mContentScreen.setVisibility(View.GONE);
        mLoadingScreen.setVisibility(View.GONE);

        mErrorScreen.setVisibility(View.VISIBLE);
    }

    public void showContentScreen() {

        mLoadingScreen.setVisibility(View.GONE);
        mErrorScreen.setVisibility(View.GONE);

        mContentScreen.setVisibility(View.VISIBLE);

        preload = false;

        getActivity().invalidateOptionsMenu();
    }

    private void showMenuItems(Menu menu, boolean visible) {

        for (int i = 0; i < menu.size(); i++) {

            menu.getItem(i).setVisible(visible);
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {

        super.onPrepareOptionsMenu(menu);

        if (App.getInstance().isConnected()) {

            if (!preload) {

                getActivity().setTitle(withProfile);

                showMenuItems(menu, true);

            } else {

                showMenuItems(menu, false);
            }

        } else {

            showMenuItems(menu, false);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        super.onCreateOptionsMenu(menu, inflater);

        menu.clear();

        inflater.inflate(R.menu.menu_chat, menu);

        MainMenu = menu;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_chat_delete: {

                deleteChat();

                return true;
            }

            default: {

                return super.onOptionsItemSelected(item);
            }
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {

        super.onDetach();

        updateChat();

        if (outboxTyping) {

            sendNotify(GCM_NOTIFY_TYPING_END);
        }
    }


    public Boolean uploadFile(String serverURL, File file) {

        final CountingRequestBody.Listener progressListener = new CountingRequestBody.Listener() {
            @Override
            public void onRequestProgress(long bytesRead, long contentLength) {

                if (bytesRead >= contentLength) {

                    if (isAdded() && getActivity() != null && pDialog != null) {

                        requireActivity().runOnUiThread(new Runnable() {

                            public void run() {
                                //            progressBar.setVisibility(View.GONE);
                            }
                        });
                    }

                } else {

                    if (contentLength > 0) {

                        final int progress = (int) (((double) bytesRead / contentLength) * 100);

                        if (isAdded() && getActivity() != null && pDialog != null) {

                            requireActivity().runOnUiThread(new Runnable() {

                                public void run() {

                                    pDialog.setProgress(progress);
//                                    progressBar.setVisibility(View.VISIBLE);
//                                    progressBar.setProgress(progress);
                                }
                            });
                        }

                        if (progress >= 100) {

                            if (isAdded() && getActivity() != null && pDialog != null) {

                                pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                            }
                        }

                        Log.e("uploadProgress called", progress+" ");
                    }
                }
            }
        };

        final okhttp3.OkHttpClient client = new okhttp3.OkHttpClient().newBuilder().addNetworkInterceptor(new Interceptor() {

                    @NonNull
                    @Override
                    public okhttp3.Response intercept(@NonNull Chain chain) throws IOException {

                        okhttp3.Request originalRequest = chain.request();

                        if (originalRequest.body() == null) {

                            return chain.proceed(originalRequest);
                        }

                        okhttp3.Request progressRequest = originalRequest.newBuilder()
                                .method(originalRequest.method(),
                                        new CountingRequestBody(originalRequest.body(), progressListener))
                                .build();

                        return chain.proceed(progressRequest);

                    }
                })
                .build();

        Uri uris = Uri.fromFile(file);
        String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uris.toString());
        String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension.toLowerCase());

        //client.setProtocols(Arrays.asList(Protocol.HTTP_1_1));

        try {

            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("uploaded_file", file.getName(), okhttp3.RequestBody.create(file, MediaType.parse(mime)))
                    .addFormDataPart("accountId", Long.toString(App.getInstance().getId()))
                    .addFormDataPart("accessToken", App.getInstance().getAccessToken())
                    .build();

            okhttp3.Request request = new okhttp3.Request.Builder()
                    .url(serverURL)
                    .addHeader("Accept", "application/json;")
                    .post(requestBody)
                    .build();

            client.newCall(request).enqueue(new Callback() {

                @Override
                public void onFailure(Call call, IOException e) {

                    loading = false;

                    hidepDialog();

                    Log.e("failure", request.toString());
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull okhttp3.Response response) throws IOException {

                    String jsonData = response.body().string();

                    Log.e("response", jsonData);

                    try {

                        JSONObject result = new JSONObject(jsonData);

                        if (!result.getBoolean("error")) {

                            messageImg = result.getString("imgUrl");
                        }

                        Log.d("My App", response.toString());

                    } catch (Throwable t) {

                        Log.e("My App", "Could not parse malformed JSON: \"" + t.getMessage() + "\"");

                    } finally {

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                send();
                            }
                        });
                    }

                }
            });

            return true;

        } catch (Exception ex) {
            // Handle the error

            loading = false;

            hidepDialog();
        }

        return false;
    }

    private void showMoreDialog() {

        if (mBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {

            mBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }

        final View view = getLayoutInflater().inflate(R.layout.chat_sheet_list, null);

        MaterialRippleLayout mStickersButton = (MaterialRippleLayout) view.findViewById(R.id.stickers_button);
        MaterialRippleLayout mGalleryButton = (MaterialRippleLayout) view.findViewById(R.id.gallery_button);
        MaterialRippleLayout mCameraButton = (MaterialRippleLayout) view.findViewById(R.id.camera_button);

        mStickersButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                mBottomSheetDialog.dismiss();

                choiceStickerDialog();
            }
        });

        mGalleryButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                mBottomSheetDialog.dismiss();

                Helper helper = new Helper(App.getInstance().getApplicationContext());

                if (!helper.checkStoragePermission()) {

                    requestStoragePermission();

                } else {

                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("image/jpeg");

                    imgFromGalleryActivityResultLauncher.launch(intent);
                }
            }
        });

        mCameraButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                mBottomSheetDialog.dismiss();

                Helper helper = new Helper(App.getInstance().getApplicationContext());

                if (!helper.checkStoragePermission()) {

                    requestStoragePermission();

                } else {

                    if (helper.checkPermission(Manifest.permission.CAMERA)) {

                        try {

                            Uri selectedImage = FileProvider.getUriForFile(App.getInstance().getApplicationContext(), App.getInstance().getPackageName() + ".provider", new File(App.getInstance().getDirectory(), IMAGE_FILE));

                            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            cameraIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, selectedImage);
                            cameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            cameraIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                            imgFromCameraActivityResultLauncher.launch(cameraIntent);

                        } catch (Exception e) {

                            Toast.makeText(getActivity(), "Error occured. Please try again later.", Toast.LENGTH_SHORT).show();
                        }

                    } else {

                        requestCameraPermission();
                    }
                }
            }
        });

        mBottomSheetDialog = new BottomSheetDialog(getActivity());

        mBottomSheetDialog.setContentView(view);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            mBottomSheetDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        mBottomSheetDialog.show();

        doKeepDialog(mBottomSheetDialog);

        mBottomSheetDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface dialog) {

                mBottomSheetDialog = null;
            }
        });
    }

    private void choiceStickerDialog() {

        final FeelingsListAdapter feelingsAdapter;

        feelingsAdapter = new FeelingsListAdapter(getActivity(), App.getInstance().getFeelingsList());

        final Dialog dialog = new Dialog(getActivity());
        dialog.setContentView(R.layout.dialog_stickers);
        dialog.setCancelable(true);

        final ProgressBar mProgressBar = (ProgressBar) dialog.findViewById(R.id.progress_bar);
        mProgressBar.setVisibility(View.GONE);

        TextView mDlgTitle = (TextView) dialog.findViewById(R.id.title_label);
        mDlgTitle.setText(R.string.label_chat_stickers);

        AppCompatButton mDlgCancelButton = (AppCompatButton) dialog.findViewById(R.id.cancel_button);
        mDlgCancelButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                dialog.dismiss();
            }
        });

        NestedScrollView mDlgNestedView = (NestedScrollView) dialog.findViewById(R.id.nested_view);
        final RecyclerView mDlgRecyclerView = (RecyclerView) dialog.findViewById(R.id.recycler_view);

        final LinearLayoutManager mLayoutManager = new GridLayoutManager(getActivity(), Helper.getStickersGridSpanCount(getActivity()));
        mDlgRecyclerView.setLayoutManager(mLayoutManager);
        mDlgRecyclerView.setHasFixedSize(true);
        mDlgRecyclerView.setItemAnimator(new DefaultItemAnimator());

        mDlgRecyclerView.setAdapter(stickersAdapter);

        mDlgRecyclerView.setNestedScrollingEnabled(true);

        feelingsAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {

            @Override
            public void onChanged() {

                super.onChanged();

                if (stickersList.size() != 0) {

                    mDlgRecyclerView.setVisibility(View.VISIBLE);
                    mProgressBar.setVisibility(View.GONE);
                }
            }
        });

        stickersAdapter.setOnItemClickListener(new StickerListAdapter.OnItemClickListener() {

            @Override
            public void onItemClick(View view, Sticker obj, int position) {

                stickerId = obj.getId();
                stickerImg = obj.getImgUrl();

                send();

                dialog.dismiss();
            }
        });

        if (stickersList.size() == 0) {

            mDlgRecyclerView.setVisibility(View.GONE);
            mProgressBar.setVisibility(View.VISIBLE);

            CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_GET_STICKERS, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {

                            try {

                                if (!isAdded() || getActivity() == null) {

                                    Log.e("ERROR", "ChatFragment Not Added to Activity");

                                    return;
                                }

                                if (!loadingMore) {

                                    stickersList.clear();
                                }

                                arrayLength = 0;

                                if (!response.getBoolean("error")) {

//                                stickerId = response.getInt("itemId");

                                    if (response.has("items")) {

                                        JSONArray stickersArray = response.getJSONArray("items");

                                        arrayLength = stickersArray.length();

                                        if (arrayLength > 0) {

                                            for (int i = 0; i < stickersArray.length(); i++) {

                                                JSONObject stickerObj = (JSONObject) stickersArray.get(i);

                                                Sticker u = new Sticker(stickerObj);

                                                stickersList.add(u);
                                            }
                                        }
                                    }
                                }

                            } catch (JSONException e) {

                                e.printStackTrace();

                            } finally {

                                Log.d("SUCCESS", "ChatFragment Success Load Stickers");

                                stickersAdapter.notifyDataSetChanged();

                                if (stickersAdapter.getItemCount() != 0) {

                                    mDlgRecyclerView.setVisibility(View.VISIBLE);
                                    mProgressBar.setVisibility(View.GONE);
                                }
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                    if (!isAdded() || getActivity() == null) {

                        Log.e("ERROR", "ChatFragment Not Added to Activity");

                        return;
                    }

                    Log.e("ERROR", "ChatFragment Not Load Stickers");
                }
            }) {

                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("accountId", Long.toString(App.getInstance().getId()));
                    params.put("accessToken", App.getInstance().getAccessToken());
                    params.put("itemId", Integer.toString(0));

                    return params;
                }
            };

            jsonReq.setRetryPolicy(new RetryPolicy() {

                @Override
                public int getCurrentTimeout() {

                    return 50000;
                }

                @Override
                public int getCurrentRetryCount() {

                    return 50000;
                }

                @Override
                public void retry(VolleyError error) throws VolleyError {

                }
            });

            App.getInstance().addToRequestQueue(jsonReq);
        }

        dialog.show();

        doKeepDialog(dialog);
    }

    // Prevent dialog dismiss when orientation changes
    private static void doKeepDialog(Dialog dialog){

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        dialog.getWindow().setAttributes(lp);
    }

    public void showImageContainer() {

        img_container_visible = true;

        mContainerImg.setVisibility(View.VISIBLE);

        mActionContainerImg.setVisibility(View.GONE);
    }

    public void hideImageContainer() {

        img_container_visible = false;

        mContainerImg.setVisibility(View.GONE);

        mActionContainerImg.setVisibility(View.VISIBLE);

        mActionContainerImg.setBackgroundResource(R.drawable.ic_plus);
    }

    private void requestStoragePermission() {

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {

            storagePermissionLauncher.launch(new String[]{Manifest.permission.READ_MEDIA_IMAGES});

        } else {

            storagePermissionLauncher.launch(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE});
        }
    }

    private void requestCameraPermission() {

        cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
    }
}