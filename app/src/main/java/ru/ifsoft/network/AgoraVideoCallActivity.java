package ru.ifsoft.network;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.google.android.material.snackbar.Snackbar;
import  ru.ifsoft.network.libs.circularImageView.*;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.agora.rtc2.ChannelMediaOptions;
import io.agora.rtc2.IRtcEngineEventHandler;
import io.agora.rtc2.RtcEngine;
import io.agora.rtc2.RtcEngineConfig;
import io.agora.rtc2.video.VideoCanvas;
import io.agora.rtc2.video.VideoEncoderConfiguration;
import ru.ifsoft.network.app.App;
import ru.ifsoft.network.constants.Constants;
import ru.ifsoft.network.util.CustomRequest;
import ru.ifsoft.network.util.Helper;


public class AgoraVideoCallActivity extends AppCompatActivity implements Constants {
    private static final String TAG = AgoraVideoCallActivity.class.getSimpleName();

    private static final int ANSWER_WAIT_TIME = 15000;
    private static final int DIAL_TIME = 15000;
    private static final int CALL_TIME = 300000;

    private RtcEngine mRtcEngine;
    private boolean mCallEnd;
    private boolean mMuted;

    private FrameLayout mLocalContainer;
    private FrameLayout mRemoteContainer;
    private VideoCanvas mLocalVideo;
    private VideoCanvas mRemoteVideo;

    private ImageView mEndCallBtn, mAnswerCallBtn;
    private ImageView mMuteBtn;
    private ImageView mSwitchCameraBtn;

    private CircularImageView mProfilePhoto;
    private ProgressBar mProgressBar;
    private TextView mDescText, mFullnameText;

    private LinearLayout mControlPanel;

    //

    private long call_id = 0;

    private String token = "", channel = "";
    private long to_user_id = 0;
    private long from_user_id = 0;

    private String to_user_username = "", to_user_fullname = "", to_user_photo_url = "";

    private int call_status = VIDEO_CALL_ACTIVE;

    private Boolean gcm = false;

    private Boolean call_active = false;

    private int time = 0;

    private CountDownTimer callTimer, dialTimer, waitTimer;
    public Boolean isCallTimerRunning = false, isDialTimerRunning = false, isWaitTimerRunning = false;

    private ActivityResultLauncher<String[]> agoraPermissionLauncher;

    /**
     * Event handler registered into RTC engine for RTC callbacks.
     * Note that UI operations needs to be in UI thread because RTC
     * engine deals with the events in a separate thread.
     */
    private final IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() {
        /**
         * Occurs when the local user joins a specified channel.
         * The channel name assignment is based on channelName specified in the joinChannel method.
         * If the uid is not specified when joinChannel is called, the server automatically assigns a uid.
         *
         * @param channel Channel name.
         * @param uid User ID.
         * @param elapsed Time elapsed (ms) from the user calling joinChannel until this callback is triggered.
         */
        @Override
        public void onJoinChannelSuccess(String channel, final int uid, int elapsed) {

            runOnUiThread(new Runnable() {

                @Override
                public void run() {

                    //mLogView.logI("Join channel success, uid: " + (uid & 0xFFFFFFFFL));
                    Log.e("joinChannel", "onJoinChannelSuccess");
                }
            });
        }

        @Override
        public void onUserJoined(final int uid, int elapsed) {

            runOnUiThread(new Runnable() {

                @Override
                public void run() {

                    //mLogView.logI("First remote video decoded, uid: " + (uid & 0xFFFFFFFFL));
                    call_active = true;

                    setupRemoteVideo(uid);

                    mProfilePhoto.setVisibility(View.GONE);
                    mDescText.setVisibility(View.GONE);
                    mFullnameText.setVisibility(View.GONE);

                    mLocalContainer.setVisibility(View.VISIBLE);
                    mMuteBtn.setVisibility(View.VISIBLE);
                    mSwitchCameraBtn.setVisibility(View.VISIBLE);

                    isWaitTimerRunning = false;

                    if (waitTimer != null) {

                        waitTimer.cancel();
                    }

                    isDialTimerRunning = false;

                    if (dialTimer != null) {

                        dialTimer.cancel();
                    }

                    //

                    callTimer = new CountDownTimer(CALL_TIME, 1000) {
                        @Override
                        public void onTick(long l) {

                            time++;
                        }

                        @Override
                        public void onFinish() {

                            isCallTimerRunning = false;
                            setVideoCallStatus(VIDEO_CALL_ENDED, time);
                            endCall();
                        }
                    };

                    isCallTimerRunning = true;
                    callTimer.start();

                    Log.e("joinChannel", "onUserJoined");
                }
            });
        }

        /**
         * Occurs when a remote user (Communication)/host (Live Broadcast) leaves the channel.
         *
         * There are two reasons for users to become offline:
         *
         *     Leave the channel: When the user/host leaves the channel, the user/host sends a
         *     goodbye message. When this message is received, the SDK determines that the
         *     user/host leaves the channel.
         *
         *     Drop offline: When no data packet of the user or host is received for a certain
         *     period of time (20 seconds for the communication profile, and more for the live
         *     broadcast profile), the SDK assumes that the user/host drops offline. A poor
         *     network connection may lead to false detections, so we recommend using the
         *     Agora RTM SDK for reliable offline detection.
         *
         * @param uid ID of the user or host who leaves the channel or goes offline.
         * @param reason Reason why the user goes offline:
         *
         *     USER_OFFLINE_QUIT(0): The user left the current channel.
         *     USER_OFFLINE_DROPPED(1): The SDK timed out and the user dropped offline because no data packet was received within a certain period of time. If a user quits the call and the message is not passed to the SDK (due to an unreliable channel), the SDK assumes the user dropped offline.
         *     USER_OFFLINE_BECOME_AUDIENCE(2): (Live broadcast only.) The client role switched from the host to the audience.
         */
        @Override
        public void onUserOffline(final int uid, int reason) {

            runOnUiThread(new Runnable() {

                @Override
                public void run() {

                    //mLogView.logI("User offline, uid: " + (uid & 0xFFFFFFFFL));
                    call_active = false;

                    onRemoteUserLeft(uid);

                    isCallTimerRunning = false;

                    if (callTimer != null) {

                        callTimer.cancel();
                    }

                    if (time != 0) {

                        setVideoCallStatus(VIDEO_CALL_CANCELED, time);
                    }

                    endCall();

                    Log.e("joinChannel", "onUserOffline");
                }
            });
        }
    };

    private void setupRemoteVideo(int uid) {

        ViewGroup parent = mRemoteContainer;

//        if (parent.indexOfChild(mLocalVideo.view) > -1) {
//
//            parent = mLocalContainer;
//        }

        // Only one remote video view is available for this
        // tutorial. Here we check if there exists a surface
        // view tagged as this uid.

        if (mRemoteVideo != null) {

            return;
        }

        /*
          Creates the video renderer view.
          CreateRendererView returns the SurfaceView type. The operation and layout of the view
          are managed by the app, and the Agora SDK renders the view provided by the app.
          The video display view must be created using this method instead of directly
          calling SurfaceView.
         */
        SurfaceView view = RtcEngine.CreateRendererView(getBaseContext());
        view.setZOrderMediaOverlay(parent == mLocalContainer);
        parent.addView(view);
        mRemoteVideo = new VideoCanvas(view, VideoCanvas.RENDER_MODE_HIDDEN, uid);
        // Initializes the video view of a remote user.
        mRtcEngine.setupRemoteVideo(mRemoteVideo);
    }

    private void onRemoteUserLeft(int uid) {

        if (mRemoteVideo != null && mRemoteVideo.uid == uid) {

            removeFromParent(mRemoteVideo);

            // Destroys remote view
            mRemoteVideo = null;
        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_agora_video_call);

        //

        String c_id = "0";

        Intent i = getIntent();

        Bundle extras = i.getExtras();

        if (extras != null) {

            if (extras.containsKey("call_id")) {

                call_id = i.getLongExtra("call_id", 0);

                Log.e("call_id c_id: ", Long.toString(call_id));
            }

            if (extras.containsKey("gcm")) {

                gcm = i.getBooleanExtra("gcm", false);
            }
        }

        to_user_id = i.getLongExtra("to_user_id", 0);
        to_user_username = i.getStringExtra("to_user_username");
        to_user_fullname = i.getStringExtra("to_user_fullname");
        to_user_photo_url = i.getStringExtra("to_user_photo_url");

        Log.e("gcm: ", Boolean.toString(gcm));
        Log.e("call_id: ", Long.toString(call_id));

        //call_id = 27;

        if (call_id == 0) {

            from_user_id = App.getInstance().getId();
        }

        //

        agoraPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), (Map<String, Boolean> isGranted) -> {

            boolean granted = true;

            for (Map.Entry<String, Boolean> x : isGranted.entrySet())

                if (!x.getValue()) granted = false;

            if (granted) {

                agoraVideoCall();

            } else {

                // Permission is denied

                Log.e("Permissions", "denied");

                View parentLayout = findViewById(android.R.id.content);
                Snackbar.make(parentLayout, getString(R.string.label_no_camera_or_video_permission) , Snackbar.LENGTH_LONG).setAction(getString(R.string.action_settings), new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        Intent appSettingsIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + App.getInstance().getPackageName()));
                        startActivity(appSettingsIntent);

                        Toast.makeText(AgoraVideoCallActivity.this, getString(R.string.label_grant_camera_and_audio_permission), Toast.LENGTH_SHORT).show();
                    }

                }).show();
            }
        });

        //

        Helper helper = new Helper(AgoraVideoCallActivity.this);

        if (!helper.checkAgoraPermission()) {

            requestAgoraPermission();

        } else {

            agoraVideoCall();
        }
    }

    private void agoraVideoCall() {

        initUI();

        initVideoCall();
    }

    @Override
    public void onNewIntent(Intent intent){

        super.onNewIntent(intent); // Propagate.

        setIntent(intent); // Passing the new intent to setIntent() means this new intent will be the one returned whenever getIntent() is called.

        Log.e("call_id: ", "onNewIntent");
    }

    @Override
    public void onBackPressed() {

        super.onBackPressed();

        endCall();
    }

    private void initVideoCall() {

        mProgressBar.setVisibility(View.VISIBLE);

        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_AGORA_VIDEO_CALL, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {

                            if (!response.getBoolean("error")) {

                                if (response.has("token")) {

                                    token = response.getString("token");
                                }

                                if (response.has("channel")) {

                                    channel = response.getString("channel");
                                }

                                if (response.has("call_id")) {

                                    call_id = response.getLong("call_id");
                                }

                                if (response.has("call_status")) {

                                    call_status = response.getInt("call_status");
                                }

                                if (response.has("from_user_id")) {

                                    from_user_id = response.getLong("from_user_id");
                                }

                                if (response.has("to_user_id")) {

                                    to_user_id = response.getLong("to_user_id");
                                }

                                if (to_user_id != App.getInstance().getId()) {

                                    to_user_photo_url = response.getString("to_user_photo_url");
                                    to_user_fullname = response.getString("to_user_fullname");

                                } else {

                                    to_user_photo_url = response.getString("from_user_photo_url");
                                    to_user_fullname = response.getString("from_user_fullname");
                                }
                            }

                        } catch (JSONException e) {

                            loadingComplete();

                            e.printStackTrace();

                        } finally {

                            Log.e("agoraCall", response.toString());

                            loadingComplete();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Log.e("ERROR", "agoraCall");

                loadingComplete();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("account_id", Long.toString(App.getInstance().getId()));
                params.put("access_token", App.getInstance().getAccessToken());
                params.put("call_id", Long.toString(call_id));
                params.put("from_user_id", Long.toString(from_user_id));
                params.put("to_user_id", Long.toString(to_user_id));
                params.put("channel", channel);
                params.put("language", "en");

                return params;
            }
        };

        RetryPolicy policy = new DefaultRetryPolicy((int) TimeUnit.SECONDS.toMillis(VOLLEY_REQUEST_SECONDS), DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        jsonReq.setRetryPolicy(policy);

        App.getInstance().addToRequestQueue(jsonReq);
    }

    private void loadingComplete() {

        mProgressBar.setVisibility(View.GONE);

        if (token.length() != 0 && call_id != 0) {

            // Show photo

            if (to_user_photo_url.length() != 0) {

                ImageLoader imageLoader = App.getInstance().getImageLoader();

                if (imageLoader == null) {

                    imageLoader = App.getInstance().getImageLoader();
                }

                imageLoader.get(to_user_photo_url, ImageLoader.getImageListener(mProfilePhoto, R.drawable.profile_default_photo, R.drawable.profile_default_photo));
            }

            mProfilePhoto.setVisibility(View.VISIBLE);
            mDescText.setVisibility(View.VISIBLE);
            mControlPanel.setVisibility(View.VISIBLE);

            mFullnameText.setVisibility(View.VISIBLE);
            mFullnameText.setText(to_user_fullname);

            // Call status

            if (call_status == VIDEO_CALL_ACTIVE) {

                if (App.getInstance().getId() == from_user_id) {

                    dialTimer = new CountDownTimer(DIAL_TIME, 1000) {
                        @Override
                        public void onTick(long l) {


                        }

                        @Override
                        public void onFinish() {

                            isDialTimerRunning = false;
                            setVideoCallStatus(VIDEO_CALL_CANCELED, 0);
                            endCall();
                        }
                    };

                    isDialTimerRunning = true;
                    dialTimer.start();

                    //

                    mDescText.setText(R.string.label_outgoing_call);

                    initEngineAndJoinChannel();
                    joinChannel();

                } else {

                    waitTimer = new CountDownTimer(ANSWER_WAIT_TIME, 1000) {
                        @Override
                        public void onTick(long l) {


                        }

                        @Override
                        public void onFinish() {

                            isWaitTimerRunning = false;
                            setVideoCallStatus(VIDEO_CALL_DECLINED, 0);
                            endCall();
                        }
                    };

                    isWaitTimerRunning = true;
                    waitTimer.start();

                    //

                    mAnswerCallBtn.setVisibility(View.VISIBLE);
                    mDescText.setText(R.string.label_incoming_call);
                }

            } else {

                if (App.getInstance().getId() == from_user_id) {

                    mDescText.setText(R.string.label_outgoing_call);

                } else {

                    mDescText.setText(R.string.label_missed_call);
                }
            }
        }
    }

    private void setVideoCallStatus(int status, int time) {

        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_AGORA_VIDEO_CALL_STATUS, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {

                            if (!response.getBoolean("error")) {

                            }

                        } catch (JSONException e) {

                            e.printStackTrace();

                        } finally {

                            Log.e("agoraCallStatus", response.toString());
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Log.e("ERROR", "agoraCallStatus");
            }
        }) {

            @Override
            protected Map<String, String> getParams() {

                Map<String, String> params = new HashMap<String, String>();

                params.put("account_id", Long.toString(App.getInstance().getId()));
                params.put("access_token", App.getInstance().getAccessToken());
                params.put("call_id", Long.toString(call_id));
                params.put("status", Integer.toString(status));
                params.put("time", Integer.toString(time));
                params.put("language", "en");

                return params;
            }
        };

        RetryPolicy policy = new DefaultRetryPolicy((int) TimeUnit.SECONDS.toMillis(VOLLEY_REQUEST_SECONDS), DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        jsonReq.setRetryPolicy(policy);

        App.getInstance().addToRequestQueue(jsonReq);
    }

    private void initUI() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getColorWrapper(this, R.color.agora_remote_background));
        }

        mLocalContainer = findViewById(R.id.local_video_view_container);
        mRemoteContainer = findViewById(R.id.remote_video_view_container);

        mControlPanel = findViewById(R.id.control_panel);

        mEndCallBtn = findViewById(R.id.btn_end_call);
        mAnswerCallBtn = findViewById(R.id.btn_answer_call);
        mMuteBtn = findViewById(R.id.btn_mute);
        mSwitchCameraBtn = findViewById(R.id.btn_switch_camera);

        mProfilePhoto = findViewById(R.id.profile_photo);
        mProgressBar = findViewById(R.id.progress_bar);
        mDescText = findViewById(R.id.desc_text);
        mFullnameText = findViewById(R.id.fullname_text);

        mProfilePhoto.setVisibility(View.GONE);
        mDescText.setVisibility(View.GONE);
        mFullnameText.setVisibility(View.GONE);

        mLocalContainer.setVisibility(View.GONE);
        mControlPanel.setVisibility(View.GONE);

        mAnswerCallBtn.setVisibility(View.GONE);
        mMuteBtn.setVisibility(View.GONE);
        mSwitchCameraBtn.setVisibility(View.GONE);
    }

    private int getColorWrapper(Context context, int id) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            return context.getColor(id);

        } else {

            //noinspection deprecation
            return context.getResources().getColor(id);
        }
    }

    private void initEngineAndJoinChannel() {

        // This is our usual steps for joining
        // a channel and starting a call.

        //initializeEngine();
        setupVideoConfig();
        setupLocalVideo();

    }

    private void initializeEngine() {

        try {

            mRtcEngine = RtcEngine.create(getBaseContext(), App.getInstance().getAgoraSettings().getAppId(), mRtcEventHandler);

        } catch (Exception e) {

            Log.e(TAG, Log.getStackTraceString(e));
            throw new RuntimeException("NEED TO check rtc sdk init fatal error\n" + Log.getStackTraceString(e));
        }
    }

    private void setupVideoConfig() {
        // In simple use cases, we only need to enable video capturing
        // and rendering once at the initialization step.
        // Note: audio recording and playing is enabled by default.

        try {

            RtcEngineConfig config = new RtcEngineConfig();
            config.mContext = getBaseContext();
            config.mAppId = App.getInstance().getAgoraSettings().getAppId();
            config.mEventHandler = mRtcEventHandler;
            mRtcEngine = RtcEngine.create(config);
            // By default, the video module is disabled, call enableVideo to enable it.
            mRtcEngine.enableVideo();

            // Please go to this page for detailed explanation
            // https://docs.agora.io/en/Video/API%20Reference/java/classio_1_1agora_1_1rtc_1_1_rtc_engine.html#af5f4de754e2c1f493096641c5c5c1d8f
            mRtcEngine.setVideoEncoderConfiguration(new VideoEncoderConfiguration(
                    VideoEncoderConfiguration.VD_640x360,
                    VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_15,
                    VideoEncoderConfiguration.STANDARD_BITRATE,
                    VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT));

        } catch (Exception e) {

            //showMessage(e.toString());
        }
        //mRtcEngine.enableVideo();
    }

    private void setupLocalVideo() {
        // This is used to set a local preview.
        // The steps setting local and remote view are very similar.
        // But note that if the local user do not have a uid or do
        // not care what the uid is, he can set his uid as ZERO.
        // Our server will assign one and return the uid via the event
        // handler callback function (onJoinChannelSuccess) after
        // joining the channel successfully.
        SurfaceView view = new SurfaceView(getBaseContext());
        view.setZOrderMediaOverlay(true);
        mLocalContainer.addView(view);
        // Initializes the local video view.
        // RENDER_MODE_HIDDEN: Uniformly scale the video until it fills the visible boundaries. One dimension of the video may have clipped contents.
//        mLocalVideo = new VideoCanvas(view, VideoCanvas.RENDER_MODE_HIDDEN, 0);
//        mRtcEngine.setupLocalVideo(mLocalVideo);
        mRtcEngine.setupLocalVideo(new VideoCanvas(view, VideoCanvas.RENDER_MODE_HIDDEN, 0));
    }

    private void joinChannel() {
        // 1. Users can only see each other after they join the
        // same channel successfully using the same app id.
        // 2. One token is only valid for the channel name that
        // you use to generate this token.
//        String token = getString(R.string.agora_access_token);
//
//        if (TextUtils.isEmpty(token) || TextUtils.equals(token, "#YOUR ACCESS TOKEN#")) {
//
//            token = null; // default, no token
//        }
//
//        mRtcEngine.joinChannel(token, "demoChannel", "Extra Optional Data", 0);

//        String token = null; // default, no token

//        RtcTokenBuilder2 tokenBuilder = new RtcTokenBuilder2();
//        // Calculate the time expiry timestamp
//        int timestamp = (int)(System.currentTimeMillis() / 1000 + 3600);
//
//        String result = tokenBuilder.buildTokenWithUid(App.getInstance().getAgoraSettings().getAppId(), App.getInstance().getAgoraSettings().getAppCertificate(),
//                "demo123", 0, RtcTokenBuilder2.Role.ROLE_PUBLISHER, timestamp, timestamp);
//
//        Log.e("agr_token", result);
//
//        mRtcEngine.joinChannel(result, "demo123", "Extra Optional Data", 0);

//        token = "007eJxTYBDTyInuvLGw9EtQja+ExzEr1cMTH7LmrQ2adY6r7v0cl6cKDGbJJolmJqmGhmnGRiapKSYWaQZm5oYGZiZpBomGBqlJBokvUxoCGRlsdvOzMjJAIIjPwlCSWlzCwAAAC9MecQ==";
//        channel = "test";

//        token = "0066c4a64e11f324ed48f0671064f0a10ebIAB4v2bek+H2vUvvD1UHVyhJjjnQ76g2PwAJRMQG0FdJ8jgk4l0AAAAAIgBXTQEAebTqZAQAAQAJcelkAwAJcelkAgAJcelkBAAJcelk";
//        channel = "c11655f9";

        //","channel":"f76d39f4"

        final int CLIENT_ROLE_BROADCASTER = 1;

        ChannelMediaOptions options = new ChannelMediaOptions();
        options.clientRoleType = CLIENT_ROLE_BROADCASTER;

        mRtcEngine.startPreview();
        mRtcEngine.joinChannel(token, channel, 0, options);

        Log.e("agr_join", "joinChannel");
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();

//        if (!mCallEnd) {
//
//            leaveChannel();
//        }
        /*
          Destroys the RtcEngine instance and releases all resources used by the Agora SDK.

          This method is useful for apps that occasionally make voice or video calls,
          to free up resources for other operations when not making calls.
         */
        RtcEngine.destroy();

    }

    private void leaveChannel() {

        if (call_active) {

            mRtcEngine.leaveChannel();
        }
    }

    public void onLocalAudioMuteClicked(View view) {

        mMuted = !mMuted;
        // Stops/Resumes sending the local audio stream.
        mRtcEngine.muteLocalAudioStream(mMuted);

        int res = mMuted ? R.drawable.btn_mute : R.drawable.btn_unmute;
        mMuteBtn.setImageResource(res);
    }

    public void onSwitchCameraClicked(View view) {

        // Switches between front and rear cameras.

        mRtcEngine.switchCamera();
    }

    public void onEndCallClicked(View view) {

        // Call status

        if (call_status == VIDEO_CALL_ACTIVE) {

            if (App.getInstance().getId() == from_user_id) {

                endCall();

            } else {

                if (call_active) {

                    endCall();

                } else {

                    endCall();
                }
            }

        } else {

            endCall();
        }
    }

    public void onAnswerCallClicked(View view) {

        mAnswerCallBtn.setVisibility(View.GONE);

        initEngineAndJoinChannel();
        joinChannel();
    }

    private void endCall() {

        removeFromParent(mLocalVideo);
        mLocalVideo = null;
        removeFromParent(mRemoteVideo);
        mRemoteVideo = null;
        leaveChannel();

        if (dialTimer == null && callTimer == null) {

            setVideoCallStatus(VIDEO_CALL_DECLINED, time);
        }

        if (dialTimer != null) {

            isDialTimerRunning = false;
            dialTimer.cancel();

            setVideoCallStatus(VIDEO_CALL_CANCELED, time);
        }

        if (callTimer != null) {

            isCallTimerRunning = false;
            callTimer.cancel();

            setVideoCallStatus(VIDEO_CALL_ENDED, time);
        }

        if (call_status == VIDEO_CALL_ACTIVE) {

            Toast.makeText(this, getString(R.string.label_ended_call), Toast.LENGTH_SHORT).show();
        }

        if (gcm) {

            //setVideoCallStatus(AGORA_CALL_STATUS_CANCELED);

            ActivityCompat.finishAffinity(AgoraVideoCallActivity.this);

            Intent i = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(i);

        } else {

            finish();
        }

        Log.e("call_id: ", "onBackPressed");
    }

    private void showButtons(boolean show) {

        int visibility = show ? View.VISIBLE : View.GONE;
        mMuteBtn.setVisibility(visibility);
        mSwitchCameraBtn.setVisibility(visibility);
    }

    private ViewGroup removeFromParent(VideoCanvas canvas) {

        if (canvas != null) {

            ViewParent parent = canvas.view.getParent();

            if (parent != null) {

                ViewGroup group = (ViewGroup) parent;
                group.removeView(canvas.view);

                return group;
            }
        }
        return null;
    }

    private void switchView(VideoCanvas canvas) {

        ViewGroup parent = removeFromParent(canvas);

        if (parent == mLocalContainer) {

            if (canvas.view instanceof SurfaceView) {

                ((SurfaceView) canvas.view).setZOrderMediaOverlay(false);
            }

            mRemoteContainer.addView(canvas.view);

        } else if (parent == mRemoteContainer) {

            if (canvas.view instanceof SurfaceView) {

                ((SurfaceView) canvas.view).setZOrderMediaOverlay(true);
            }

            mLocalContainer.addView(canvas.view);
        }
    }

    public void onLocalContainerClick(View view) {

        switchView(mLocalVideo);
        switchView(mRemoteVideo);
    }

    private void requestAgoraPermission() {

        agoraPermissionLauncher.launch(new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO});
    }
}
