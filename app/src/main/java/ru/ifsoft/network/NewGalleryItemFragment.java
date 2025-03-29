package ru.ifsoft.network;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Location;
import android.location.LocationManager;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.provider.Settings;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Size;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.balysv.materialripple.MaterialRippleLayout;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import  ru.ifsoft.network.libs.circularImageView.*;
import com.otaliastudios.transcoder.Transcoder;
import com.otaliastudios.transcoder.TranscoderListener;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import github.ankushsachdeva.emojicon.EditTextImeBackListener;
import github.ankushsachdeva.emojicon.EmojiconEditText;
import github.ankushsachdeva.emojicon.EmojiconGridView;
import github.ankushsachdeva.emojicon.EmojiconsPopup;
import github.ankushsachdeva.emojicon.emoji.Emojicon;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Interceptor;
import okhttp3.MultipartBody;
import ru.ifsoft.network.adapter.FeelingsListAdapter;
import ru.ifsoft.network.adapter.MediaListAdapter;
import ru.ifsoft.network.app.App;
import ru.ifsoft.network.constants.Constants;
import ru.ifsoft.network.model.Feeling;
import ru.ifsoft.network.model.GalleryItem;
import ru.ifsoft.network.model.Image;
import ru.ifsoft.network.model.Item;
import ru.ifsoft.network.model.MediaItem;
import ru.ifsoft.network.util.Api;
import ru.ifsoft.network.util.CountingRequestBody;
import ru.ifsoft.network.util.CustomRequest;
import ru.ifsoft.network.util.Helper;

public class NewGalleryItemFragment extends Fragment implements Constants {

    private static final int BUFFER_SIZE = 1024 * 2;

    private static final int VIDEO_FILES_LIMIT = 1;
    private static final int IMAGE_FILES_LIMIT = 7;

    public static final int REQUEST_TAKE_GALLERY_VIDEO = 1001;

    private static final String STATE_LIST = "State Adapter Data";

    public static final int RESULT_OK = -1;

    private static final int ITEM_FEELINGS = 1;

    private FusedLocationProviderClient mFusedLocationClient;
    protected Location mLastLocation;

    private MaterialRippleLayout mOpenBottomSheet;

    private BottomSheetBehavior mBehavior;
    private BottomSheetDialog mBottomSheetDialog;
    private View mBottomSheet;

    private CircularImageView mPhoto;
    private TextView mFullname;

    private LinearLayout mMediaContainer, mDeleteMedia;
    private ImageView mThumbnail, mPlayIcon;
    private ProgressBar mProgressBar;

    private ProgressDialog pDialog;

    EmojiconEditText mPostEdit;
    ImageView mEmojiBtn;

    private GalleryItem item;

    private int itemType = 0;

    private String selectedImagePath = "";

    private ActivityResultLauncher<String> cameraPermissionLauncher;
    private ActivityResultLauncher<String[]> storagePermissionLauncher;
    private ActivityResultLauncher<Intent> imgFromGalleryActivityResultLauncher;
    private ActivityResultLauncher<Intent> videoFromGalleryActivityResultLauncher;
    private ActivityResultLauncher<Intent> imgFromCameraActivityResultLauncher;

    private ActivityResultLauncher<Intent> videoFromCameraActivityResultLauncher;

    private ActivityResultLauncher<String> recordAudioPermissionLauncher;
    private ActivityResultLauncher<String[]> videoCapturePermissionLauncher;

    private Boolean loading = false;
    private Boolean uploading = false;
    private Boolean compressing = false;

    EmojiconsPopup popup;

    public NewGalleryItemFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setRetainInstance(true);

        setHasOptionsMenu(true);

        initpDialog();

        Intent i = getActivity().getIntent();

        if (i.getExtras() != null) {

            item = (GalleryItem) i.getExtras().getParcelable("item");

            if (item == null) {

                item = new GalleryItem();
            }

        } else {

            item = new GalleryItem();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_new_gallery_item, container, false);

        if (savedInstanceState != null) {

            item = savedInstanceState.getParcelable("item");
        }

        //

        recordAudioPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {

            if (isGranted) {

                // Permission is granted
                Log.e("Permissions", "Permission is granted");

                captureVideo();

            } else {

                // Permission is denied

                Log.e("Permissions", "denied");

                Snackbar.make(getView(), getString(R.string.label_no_record_audio_permission) , Snackbar.LENGTH_LONG).setAction(getString(R.string.action_settings), new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        Intent appSettingsIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + App.getInstance().getPackageName()));
                        startActivity(appSettingsIntent);

                        Toast.makeText(getActivity(), getString(R.string.label_grant_record_audio_permission), Toast.LENGTH_SHORT).show();
                    }

                }).show();
            }
        });

        //

        videoFromCameraActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {

            @Override
            public void onActivityResult(ActivityResult result) {

                if (result.getResultCode() == 100) {

                    File file =  new File(App.getInstance().getDirectory(), VIDEO_SRC_FILE);

                    if (file.exists()) {

                        Log.e("MyApp file size: ", Long.toString(file.length()));

                        selectedImagePath = file.getPath();

                        Helper helper = new Helper(getActivity().getApplicationContext());
                        helper.createThumbnail(ThumbnailUtils.createVideoThumbnail(file.getPath(), MediaStore.Images.Thumbnails.MINI_KIND), VIDEO_THUMBNAIL_FILE);

                        mThumbnail.setImageURI(Uri.fromFile(new File(App.getInstance().getDirectory(), VIDEO_THUMBNAIL_FILE)));

                        itemType = GALLERY_ITEM_TYPE_VIDEO;
                    }

                    updateMediaContainer();
                }
            }
        });

        //

        videoCapturePermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), (Map<String, Boolean> isGranted) -> {

            boolean granted = true;

            for (Map.Entry<String, Boolean> x : isGranted.entrySet())

                if (!x.getValue()) granted = false;

            if (granted) {

                captureVideo();

            } else {

                // Permission is denied

                Log.e("Permissions", "denied");

                Snackbar.make(getView(), getString(R.string.label_no_camera_or_video_permission) , Snackbar.LENGTH_LONG).setAction(getString(R.string.action_settings), new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        Intent appSettingsIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + App.getInstance().getPackageName()));
                        startActivity(appSettingsIntent);

                        Toast.makeText(getActivity(), getString(R.string.label_grant_camera_and_audio_permission), Toast.LENGTH_SHORT).show();
                    }

                }).show();
            }
        });

        //

        videoFromGalleryActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {

            @Override
            public void onActivityResult(ActivityResult result) {

                if (result.getResultCode() == Activity.RESULT_OK) {

                    // The document selected by the user won't be returned in the intent.
                    // Instead, a URI to that document will be contained in the return intent
                    // provided to this method as a parameter.  Pull that uri using "resultData.getData()"

                    if (result.getData() != null) {

                        selectedImagePath = Helper.getRealPath(result.getData().getData());

                        File videoFile = new File(selectedImagePath);

                        if (videoFile.length() > VIDEO_FILE_MAX_SIZE_FROM_GALLERY) {

                            selectedImagePath = "";

                            Toast.makeText(getActivity(), getString(R.string.msg_video_too_large), Toast.LENGTH_SHORT).show();

                        } else {

                            Helper helper = new Helper(App.getInstance().getApplicationContext());
                            helper.createThumbnail(ThumbnailUtils.createVideoThumbnail(selectedImagePath, MediaStore.Images.Thumbnails.MINI_KIND), VIDEO_THUMBNAIL_FILE);

                            itemType = GALLERY_ITEM_TYPE_VIDEO;

                            updateMediaContainer();
                        }
                    }
                }
            }
        });

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

                        mThumbnail.setImageURI(Uri.fromFile(new File(App.getInstance().getDirectory(), IMAGE_FILE)));

                        itemType = GALLERY_ITEM_TYPE_IMAGE;

                        updateMediaContainer();
                    }
                }
            }
        });

        imgFromCameraActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {

            @Override
            public void onActivityResult(ActivityResult result) {

                if (result.getResultCode() == Activity.RESULT_OK) {

                    selectedImagePath = new File(App.getInstance().getDirectory(), IMAGE_FILE).getPath();

                    mThumbnail.setImageURI(Uri.fromFile(new File(App.getInstance().getDirectory(), IMAGE_FILE)));

                    itemType = GALLERY_ITEM_TYPE_IMAGE;

                    updateMediaContainer();
                }
            }
        });

        cameraPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {

            if (isGranted) {

                // Permission is granted
                Log.e("Permissions", "Permission is granted");

                choiceImageAction();

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

                showBottomSheet();

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

        popup.setOnEmojiconClickedListener(new EmojiconGridView.OnEmojiconClickedListener() {

            @Override
            public void onEmojiconClicked(Emojicon emojicon) {

                mPostEdit.append(emojicon.getEmoji());
            }
        });

        popup.setOnEmojiconBackspaceClickedListener(new EmojiconsPopup.OnEmojiconBackspaceClickedListener() {

            @Override
            public void onEmojiconBackspaceClicked(View v) {

                KeyEvent event = new KeyEvent(0, 0, 0, KeyEvent.KEYCODE_DEL, 0, 0, 0, 0, KeyEvent.KEYCODE_ENDCALL);
                mPostEdit.dispatchKeyEvent(event);
            }
        });

        popup.setOnDismissListener(new PopupWindow.OnDismissListener() {

            @Override
            public void onDismiss() {

                setIconEmojiKeyboard();
            }
        });

        popup.setOnSoftKeyboardOpenCloseListener(new EmojiconsPopup.OnSoftKeyboardOpenCloseListener() {

            @Override
            public void onKeyboardOpen(int keyBoardHeight) {

            }

            @Override
            public void onKeyboardClose() {

                if (popup.isShowing())

                    popup.dismiss();
            }
        });

        popup.setOnEmojiconClickedListener(new EmojiconGridView.OnEmojiconClickedListener() {

            @Override
            public void onEmojiconClicked(Emojicon emojicon) {

                mPostEdit.append(emojicon.getEmoji());
            }
        });

        popup.setOnEmojiconBackspaceClickedListener(new EmojiconsPopup.OnEmojiconBackspaceClickedListener() {

            @Override
            public void onEmojiconBackspaceClicked(View v) {

                KeyEvent event = new KeyEvent(0, 0, 0, KeyEvent.KEYCODE_DEL, 0, 0, 0, 0, KeyEvent.KEYCODE_ENDCALL);
                mPostEdit.dispatchKeyEvent(event);
            }
        });

        if (loading) {

            showpDialog();
        }

        //

        mMediaContainer = (LinearLayout) rootView.findViewById(R.id.media_container);
        mDeleteMedia = (LinearLayout) rootView.findViewById(R.id.delete);

        mPlayIcon = (ImageView) rootView.findViewById(R.id.play_icon);
        mThumbnail = (ImageView) rootView.findViewById(R.id.thumbnail);
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);

        mDeleteMedia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                selectedImagePath = "";

                updateMediaContainer();
            }
        });

        //

        mOpenBottomSheet = (MaterialRippleLayout) rootView.findViewById(R.id.open_bottom_sheet_button);

        mOpenBottomSheet.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                showBottomSheet();
            }
        });

        // Prepare bottom sheet

        mBottomSheet = rootView.findViewById(R.id.bottom_sheet);
        mBehavior = BottomSheetBehavior.from(mBottomSheet);

        //

        mPhoto = (CircularImageView) rootView.findViewById(R.id.photo_image);
        mFullname = (TextView) rootView.findViewById(R.id.fullname_label);

        //


        mEmojiBtn = (ImageView) rootView.findViewById(R.id.emojiBtn);
        mEmojiBtn.setVisibility(View.GONE);

        mPostEdit = (EmojiconEditText) rootView.findViewById(R.id.postEdit);
        mPostEdit.setText(item.getComment());

        mPostEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {

                if (isAdded()) {

                    if (hasFocus) {

                        //got focus

                        if (EMOJI_KEYBOARD) {

                            mEmojiBtn.setVisibility(View.VISIBLE);
                        }

                    } else {

                        mEmojiBtn.setVisibility(View.GONE);
                    }
                }
            }
        });

        setEditTextMaxLength(POST_CHARACTERS_LIMIT);

        mPostEdit.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                int cnt = s.length();

                if (cnt == 0) {

                    updateTitle();

                } else {

                    getActivity().setTitle(Integer.toString(POST_CHARACTERS_LIMIT - cnt));
                }
            }

        });

        mEmojiBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (!popup.isShowing()) {

                    if (popup.isKeyBoardOpen()){

                        popup.showAtBottom();
                        setIconSoftKeyboard();

                    } else {

                        mPostEdit.setFocusableInTouchMode(true);
                        mPostEdit.requestFocus();
                        popup.showAtBottomPending();

                        final InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputMethodManager.showSoftInput(mPostEdit, InputMethodManager.SHOW_IMPLICIT);
                        setIconSoftKeyboard();
                    }

                } else {

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

        mPostEdit.setOnEditTextImeBackListener(er);

        updateMediaContainer();
        updateTitle();
        updateProfileInfo();

        // Inflate the layout for this fragment
        return rootView;
    }

    private void updateMediaContainer() {

        mMediaContainer.setVisibility(View.GONE);
        mPlayIcon.setVisibility(View.GONE);
        mOpenBottomSheet.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.GONE);

        if (selectedImagePath != null && selectedImagePath.length() > 0) {

            mOpenBottomSheet.setVisibility(View.GONE);

            mMediaContainer.setVisibility(View.VISIBLE);

            if (itemType == GALLERY_ITEM_TYPE_IMAGE) {

                mThumbnail.setImageURI(Uri.fromFile(new File(selectedImagePath)));
                mPlayIcon.setVisibility(View.GONE);

            } else {

                File f = new File(App.getInstance().getDirectory(), VIDEO_THUMBNAIL_FILE);

                if (f.exists()) {

                    mThumbnail.setImageURI(Uri.fromFile(new File(App.getInstance().getDirectory(), VIDEO_THUMBNAIL_FILE)));

                } else {

                    mThumbnail.setImageResource(R.drawable.ic_video_preview);
                }

                mPlayIcon.setVisibility(View.VISIBLE);
            }
        }
    }

    private void updateTitle() {

        if (isAdded()) {

            if (item.getId() != 0) {

                getActivity().setTitle(getText(R.string.title_edit_item));

            } else {

                getActivity().setTitle(getText(R.string.title_new_item));
            }
        }
    }

    private void updateProfileInfo() {

        if (isAdded()) {

            if (App.getInstance().getPhotoUrl() != null && App.getInstance().getPhotoUrl().length() > 0) {

                App.getInstance().getImageLoader().get(App.getInstance().getPhotoUrl(), ImageLoader.getImageListener(mPhoto, R.drawable.profile_default_photo, R.drawable.profile_default_photo));

            } else {

                mPhoto.setImageResource(R.drawable.profile_default_photo);
            }

            mFullname.setText(App.getInstance().getFullname());
        }
    }

    public void setEditTextMaxLength(int length) {

        InputFilter[] FilterArray = new InputFilter[1];
        FilterArray[0] = new InputFilter.LengthFilter(length);
        mPostEdit.setFilters(FilterArray);
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

        hidepDialog();
    }

    protected void initpDialog() {

        pDialog = new ProgressDialog(getActivity());
        pDialog.setMessage(getString(R.string.msg_loading));
        pDialog.setCancelable(false);
    }

    protected void showpDialog() {

        if (pDialog == null) {

            initpDialog();
        }

        if (uploading) {

            pDialog.setMessage(getString(R.string.msg_uploading));
            pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

        } else {

            if (compressing) {

                pDialog.setMessage(getString(R.string.msg_compressing_video));
                pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

            } else {

                pDialog.setMessage(getString(R.string.msg_loading));
                pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            }
        }

        pDialog.setProgressNumberFormat(null);
        pDialog.setProgress(0);
        pDialog.setMax(100);

        if (!pDialog.isShowing()) pDialog.show();
    }

    protected void hidepDialog() {

        if (pDialog.isShowing()) pDialog.dismiss();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);

        outState.putParcelable("item", item);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem mItem) {

        switch (mItem.getItemId()) {

            case R.id.action_post: {

                hideEmojiKeyboard();

                this.item.setComment(mPostEdit.getText().toString().trim());

                if (selectedImagePath != null && selectedImagePath.length() != 0) {

                    if (itemType == GALLERY_ITEM_TYPE_IMAGE) {

                        File f = new File(selectedImagePath);

                        uploadFile(METHOD_GALLERY_UPLOAD_IMAGE, f);

                    } else {

                        File f = new File(selectedImagePath);
                        File f_thumb = new File(App.getInstance().getDirectory(), VIDEO_THUMBNAIL_FILE);

                        if (f.length() > VIDEO_FILE_MAX_SIZE) {

                            compressingVideoFile(f, new File(App.getInstance().getDirectory(), VIDEO_DEST_FILE));

                        } else {

                            uploadVideoFile(METHOD_GALLERY_UPLOAD_VIDEO, f, f_thumb);
                        }
                    }

                } else {

                    Toast toast= Toast.makeText(getActivity(), getText(R.string.msg_enter_item), Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }

                return true;
            }

            default: {

                break;
            }
        }

        return false;
    }

    public void choiceImageAction() {

        AlertDialog.Builder builderSingle = new AlertDialog.Builder(getActivity());

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1);

        arrayAdapter.add(getText(R.string.action_gallery).toString());
        arrayAdapter.add(getText(R.string.action_camera).toString());

        builderSingle.setTitle(getText(R.string.dlg_choice_image_title));


        builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                switch (which) {

                    case 0: {

                        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                        intent.addCategory(Intent.CATEGORY_OPENABLE);
                        intent.setType("image/jpeg");

                        imgFromGalleryActivityResultLauncher.launch(intent);

                        break;
                    }

                    default: {

                        Helper helper = new Helper(getActivity());

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

                        break;
                    }
                }
            }
        });

        builderSingle.setNegativeButton(getText(R.string.action_cancel), new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
            }
        });

        AlertDialog d = builderSingle.create();
        d.show();
    }

    private void newPost() {

        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_GALLERY_ITEM_NEW, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {

                            if (!response.getBoolean("error")) {


                            }

                        } catch (JSONException e) {

                            e.printStackTrace();

                        } finally {

                            sendPostSuccess();

                            Log.e("Response", response.toString());
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                sendPostSuccess();

//                     Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("accountId", Long.toString(App.getInstance().getId()));
                params.put("accessToken", App.getInstance().getAccessToken());
                params.put("comment", item.getComment());
                params.put("originImgUrl", item.getOriginImgUrl());
                params.put("previewImgUrl", item.getPreviewImgUrl());
                params.put("imgUrl", item.getImgUrl());
                params.put("postArea", item.getArea());
                params.put("postCountry", item.getCountry());
                params.put("postCity", item.getCity());
                params.put("postLat", Double.toString(item.getLat()));
                params.put("postLng", Double.toString(item.getLng()));

                params.put("previewVideoImgUrl", item.getPreviewVideoImgUrl());
                params.put("videoUrl", item.getVideoUrl());

                return params;
            }
        };

        int socketTimeout = 0;//0 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        jsonReq.setRetryPolicy(policy);

        App.getInstance().addToRequestQueue(jsonReq);
    }

    public void sendPostSuccess() {

        loading = false;

        hidepDialog();

        if (isAdded()) {

            Intent i = new Intent();
            getActivity().setResult(RESULT_OK, i);

            Toast.makeText(getActivity(), getText(R.string.msg_item_posted), Toast.LENGTH_SHORT).show();

            getActivity().finish();
        }
    }

    @Override
    public void onAttach(Context context) {

        super.onAttach(context);
    }

    @Override
    public void onDetach() {

        super.onDetach();
    }


    private void showBottomSheet() {

        if (mBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {

            mBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }

        if (App.getInstance().getCountry().length() == 0 && App.getInstance().getCity().length() == 0) {

            if (App.getInstance().getLat() != 0.000000 && App.getInstance().getLng() != 0.000000) {

                App.getInstance().getAddress(App.getInstance().getLat(), App.getInstance().getLng());
            }
        }

        final View view = getLayoutInflater().inflate(R.layout.item_gallery_editor_sheet_list, null);

        MaterialRippleLayout mAddImageButton = (MaterialRippleLayout) view.findViewById(R.id.add_image_button);
        MaterialRippleLayout mAddVideoButton = (MaterialRippleLayout) view.findViewById(R.id.add_video_button);
        MaterialRippleLayout mCaptureVideoButton = (MaterialRippleLayout) view.findViewById(R.id.capture_video_button);

        mAddImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mBottomSheetDialog != null) {

                    mBottomSheetDialog.dismiss();
                }

                Helper helper = new Helper(App.getInstance().getApplicationContext());

                if (!helper.checkStoragePermission()) {

                    requestStoragePermission();

                } else {

                    choiceImageAction();
                }
            }
        });

        mAddVideoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mBottomSheetDialog != null) {

                    mBottomSheetDialog.dismiss();
                }

                Helper helper = new Helper(App.getInstance().getApplicationContext());

                if (!helper.checkStoragePermission()) {

                    requestStoragePermission();

                } else {

                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("video/mp4");

                    videoFromGalleryActivityResultLauncher.launch(intent);
                }
            }
        });

        mCaptureVideoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mBottomSheetDialog != null) {

                    mBottomSheetDialog.dismiss();
                }

                if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {

                    captureVideo();

                } else {

                    requestVideoCapturePermission();
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

    public void captureVideo() {

        Intent intent = new Intent(getActivity(), CaptureActivity.class);
        //intent.putExtra("profileId", obj.getId());
        videoFromCameraActivityResultLauncher.launch(intent);
    }

    // Prevent dialog dismiss when orientation changes
    private static void doKeepDialog(Dialog dialog) {

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        dialog.getWindow().setAttributes(lp);
    }

    public Boolean uploadFile(String serverURL, File file) {

        loading = true;
        uploading = true;

        pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pDialog.setProgressNumberFormat(null);
        pDialog.setProgress(0);
        pDialog.setMax(100);

        showpDialog();

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

        //client.setProtocols(Arrays.asList(Protocol.HTTP_1_1));

        String fileExtension = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(file).toString());
        String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension.toLowerCase());

        try {

            okhttp3.RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("uploaded_file", file.getName(), okhttp3.RequestBody.create(file, okhttp3.MediaType.parse(mime)))
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
                public void onFailure(@NonNull Call call, @NonNull IOException e) {

                    uploading = false;
                    loading = false;
                    compressing = false;

                    hidepDialog();

                    Log.e("failure", request.toString() + "|" + e.toString());
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull okhttp3.Response response) throws IOException {

                    String jsonData = response.body().string();

                    try {

                        JSONObject result = new JSONObject(jsonData);

                        if (!result.getBoolean("error")) {

                            item.setImgUrl(result.getString("normalPhotoUrl"));
                            item.setOriginImgUrl(result.getString("originPhotoUrl"));
                            item.setPreviewImgUrl(result.getString("previewPhotoUrl"));

                        }

                        Log.d("My App", response.toString());

                    } catch (Throwable t) {

                        Log.e("My App", "Could not parse malformed JSON: \"" + t.getMessage() + "\"");

                    } finally {

                        Log.e("response", jsonData);

                        newPost();
                    }
                }

            });

            return true;

        } catch (Exception ex) {
            // Handle the error

            uploading = false;
            loading = false;
            compressing = false;

            hidepDialog();
        }

        return false;
    }

    private void compressingVideoFile(File src, File dest) {

        loading = true;
        compressing = true;

        pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pDialog.setProgressNumberFormat(null);
        pDialog.setProgress(0);
        pDialog.setMax(100);

        showpDialog();

        Transcoder.into(dest.getPath())
                .addDataSource(src.getPath()) // or...
                .setListener(new TranscoderListener() {
                    public void onTranscodeProgress(double progress) {

                        double prgs = progress * 100;

                        pDialog.setProgress((int) prgs);

                        Log.e("Dimon", "onTranscodeProgress:" + Integer.valueOf((int) prgs).toString());
                    }
                    public void onTranscodeCompleted(int successCode) {

                        uploading = false;
                        loading = false;
                        compressing = false;

                        pDialog.hide();

                        Log.e("compressing", "onTranscodeCompleted");
                        Log.e("compressing src", Long.toString(src.length()));
                        Log.e("compressing dest", Long.toString(dest.length()));

                        File f = new File(App.getInstance().getDirectory(), VIDEO_DEST_FILE);
                        File f_thumb = new File(App.getInstance().getDirectory(), VIDEO_THUMBNAIL_FILE);

                        uploadVideoFile(METHOD_GALLERY_UPLOAD_VIDEO, f, f_thumb);
                    }
                    public void onTranscodeCanceled() {

                        uploading = false;
                        loading = false;
                        compressing = false;

                        Log.e("compressing", "onTranscodeCanceled");
                        pDialog.hide();
                    }
                    public void onTranscodeFailed(@NonNull Throwable exception) {

                        uploading = false;
                        loading = false;
                        compressing = false;

                        pDialog.hide();
                        Log.e("compressing", exception.toString());
                    }
                }).transcode();
    }

    public Boolean uploadVideoFile(String serverURL, File videoFile,  File videoThumb) {

        loading = true;
        uploading = true;

        pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pDialog.setProgressNumberFormat(null);
        pDialog.setProgress(0);
        pDialog.setMax(100);

        showpDialog();

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
                .connectTimeout(OKHTTP3_REQUEST_SECONDS, TimeUnit.SECONDS)
                .readTimeout(OKHTTP3_REQUEST_SECONDS, TimeUnit.SECONDS)
                .writeTimeout(OKHTTP3_REQUEST_SECONDS, TimeUnit.SECONDS)
                .build();

        //client.setProtocols(Arrays.asList(Protocol.HTTP_1_1));

        String vidFileExtension = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(videoFile).toString());
        String thumbFileExtension = MimeTypeMap.getFileExtensionFromUrl( Uri.fromFile(videoThumb).toString());
        String vidMime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(vidFileExtension.toLowerCase());
        String thumbMime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(thumbFileExtension.toLowerCase());

        try {

            okhttp3.RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("uploaded_file", videoThumb.getName(), okhttp3.RequestBody.create(videoThumb, okhttp3.MediaType.parse(thumbMime)))
                    .addFormDataPart("uploaded_video_file", videoFile.getName(), okhttp3.RequestBody.create(videoFile, okhttp3.MediaType.parse(vidMime)))
                    .addFormDataPart("accountId", Long.toString(App.getInstance().getId()))
                    .addFormDataPart("accessToken", App.getInstance().getAccessToken())
                    .build();

            okhttp3.Request request = new okhttp3.Request.Builder()
                    .url(serverURL)
                    .addHeader("Accept", "application/json;")
                    .post(requestBody)
                    .build();

            client.newCall(request).enqueue(new okhttp3.Callback() {

                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {

                    uploading = false;
                    loading = false;
                    compressing = false;

                    hidepDialog();

                    Log.e("failure", request.toString() + "|" + e.toString());
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull okhttp3.Response response) throws IOException {

                    String jsonData = response.body().string();

                    Log.e("response", jsonData);

                    try {

                        JSONObject result = new JSONObject(jsonData);

                        if (!result.getBoolean("error")) {

                            item.setVideoUrl(result.getString("videoFileUrl"));
                            item.setPreviewVideoImgUrl(result.getString("imgFileUrl"));
                        }

                        Log.d("My App", response.toString());

                    } catch (Throwable t) {

                        Log.e("My App", "Could not parse malformed JSON: \"" + t.getMessage() + "\"");

                    } finally {

                        Log.e("response", jsonData);

                        newPost();
                    }
                }

            });

            return true;

        } catch (Exception ex) {
            // Handle the error

            uploading = false;
            loading = false;
            compressing = false;

            hidepDialog();
        }

        return false;
    }

    private void requestStoragePermission() {

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {

            storagePermissionLauncher.launch(new String[]{android.Manifest.permission.READ_MEDIA_IMAGES, android.Manifest.permission.READ_MEDIA_VIDEO, android.Manifest.permission.ACCESS_MEDIA_LOCATION});

        } else {

            storagePermissionLauncher.launch(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE});
        }
    }

    private void requestCameraPermission() {

        cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
    }

    private void requestVideoCapturePermission() {

        videoCapturePermissionLauncher.launch(new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA});
    }
}