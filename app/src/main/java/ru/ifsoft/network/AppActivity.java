package ru.ifsoft.network;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.ump.ConsentInformation;
import com.google.android.ump.UserMessagingPlatform;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import ru.ifsoft.network.app.App;
import ru.ifsoft.network.common.ActivityBase;
import ru.ifsoft.network.util.CustomRequest;

public class AppActivity extends ActivityBase {

    Button mSignupBtn, mExploreBtn, mGoogleBtn, mPhoneBtn;

    ProgressBar progressBar;

    private FusedLocationProviderClient mFusedLocationClient;
    protected Location mLastLocation;

    //

    TextView mLoginButton;
    TextView mTermsButton;

    LinearLayout mContentLayout;

    // Google

    GoogleSignInClient mGoogleSignInClient;
    ActivityResultLauncher<Intent> googleSigninActivityResultLauncher;

    //

    private BottomSheetBehavior mBehavior;
    private BottomSheetDialog mPhoneLoginBottomSheetDialog;
    private View mBottomSheet;

    //

    private Boolean settings_loaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Night mode

        if (App.getInstance().getNightMode() == 1) {

            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        } else {

            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        super.onCreate(savedInstanceState);

        //

        setContentView(R.layout.activity_app);

        // Prepare bottom sheet

        mBottomSheet = findViewById(R.id.bottom_sheet);
        mBehavior = BottomSheetBehavior.from(mBottomSheet);

        // Google

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(AppActivity.this, gso);

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

                                                Intent intent = new Intent(AppActivity.this, MainActivity.class);
                                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                startActivity(intent);

                                            } else {

                                                if (App.getInstance().getState() == ACCOUNT_STATE_BLOCKED) {

                                                    App.getInstance().logout();
                                                    Toast.makeText(AppActivity.this, getText(R.string.msg_account_blocked), Toast.LENGTH_SHORT).show();

                                                } else {

                                                    App.getInstance().updateGeoLocation();

                                                    Intent intent = new Intent(AppActivity.this, MainActivity.class);
                                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                    startActivity(intent);
                                                }
                                            }

                                        } else {

                                            Toast.makeText(AppActivity.this, getString(R.string.error_data_loading), Toast.LENGTH_SHORT).show();
                                        }

                                        hidepDialog();
                                    }
                                }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {

                                Log.e("Google", "signInWithCredential:failure");

                                Toast.makeText(AppActivity.this, getText(R.string.error_data_loading), Toast.LENGTH_LONG).show();

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
                                params.put("oauth_photo", account.getPhotoUrl().toString());

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

        //AppEventsLogger.activateApp(getApplication());

        // Get Firebase token


        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {

            @Override
            public void onComplete(@NonNull Task<String> task) {
                if (!task.isSuccessful()) {
                    Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                    return;
                }

                // Get new FCM registration token
                String token = task.getResult();

                App.getInstance().setGcmToken(token);

                Log.d("FCM Token", token);
            }
        });


        // Check GPS is enabled
        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER) && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

            mFusedLocationClient.getLastLocation().addOnCompleteListener(this, new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {

                    if (task.isSuccessful() && task.getResult() != null) {

                        mLastLocation = task.getResult();

                        // Set geo data to App class

                        App.getInstance().setLat(mLastLocation.getLatitude());
                        App.getInstance().setLng(mLastLocation.getLongitude());

                        // Save data

                        App.getInstance().saveData();

                        // Send location data to server

                        // App.getInstance().setLocation();

                    } else {

                        Log.d("GPS", "AppActivity getLastLocation:exception", task.getException());
                    }
                }
            });
        }

        mLoginButton = findViewById(R.id.button_login);
        mSignupBtn = findViewById(R.id.button_signup);
        mPhoneBtn = findViewById(R.id.button_phone);
        mGoogleBtn = findViewById(R.id.button_google);
        mExploreBtn = (Button) findViewById(R.id.exploreBtn);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        mExploreBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Intent i = new Intent(AppActivity.this, StreamActivity.class);
                startActivity(i);
            }
        });

        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent i = new Intent(AppActivity.this, LoginActivity.class);
                startActivity(i);
            }
        });

        mSignupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent i = new Intent(AppActivity.this, RegisterActivity.class);
                startActivity(i);
            }
        });

        // Google

        mGoogleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                googleSigninActivityResultLauncher.launch(signInIntent);
            }
        });

        // Phone

        mPhoneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mPhoneLoginBottomSheetDialog != null) {

                    mPhoneLoginBottomSheetDialog.dismiss();
                }

                showPhoneLoginBottomSheet(BottomSheetBehavior.STATE_EXPANDED);
            }
        });

        //

        mContentLayout = findViewById(R.id.layout_content);

        //

        mTermsButton = (TextView) findViewById(R.id.button_terms);

        mTermsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(AppActivity.this, WebViewActivity.class);
                i.putExtra("url", METHOD_APP_TERMS);
                i.putExtra("title", getText(R.string.settings_terms));
                startActivity(i);
            }
        });

//        mLanguageBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//                List<String> language_names = new ArrayList<String>();
//
//                Resources r = getResources();
//                Configuration c = r.getConfiguration();
//
//                for (int i = 0; i < App.getInstance().getLanguages().size(); i++) {
//
//                    language_names.add(App.getInstance().getLanguages().get(i).get("lang_name"));
//                }
//
//                AlertDialog.Builder b = new AlertDialog.Builder(AppActivity.this);
//                b.setTitle(getText(R.string.title_select_language));
//
//                b.setItems(language_names.toArray(new CharSequence[language_names.size()]), new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//
//                        App.getInstance().setLanguage(App.getInstance().getLanguages().get(which).get("lang_id"));
//
//                        App.getInstance().saveData();
//                        App.getInstance().readData();
//
//                        // Set App Language
//
//                        App.getInstance().setLocale(App.getInstance().getLanguage());
//
//                        setLanguageBtnTitle();
//                    }
//                });
//
//                b.setNegativeButton(getText(R.string.action_cancel), null);
//
//                AlertDialog d = b.create();
//                d.show();
//            }
//        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                window.setStatusBarColor(getColor(R.color.app_bg));

            } else {

                window.setStatusBarColor(getApplicationContext().getResources().getColor(R.color.app_bg));
            }
        }
    }

    @Override
    protected void  onStart() {

        super.onStart();

        if (App.getInstance().isConnected() && App.getInstance().getId() != 0) {

            showLoadingScreen();

            CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_ACCOUNT_AUTHORIZE, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {

                            if (App.getInstance().authorize(response)) {

                                if (App.getInstance().getState() == ACCOUNT_STATE_ENABLED) {

                                    App.getInstance().updateGeoLocation();

                                    Intent intent = new Intent(AppActivity.this, MainActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);

                                } else {

                                    getSettings();
                                    App.getInstance().logout();
                                }

                            } else {

                                getSettings();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                    getSettings();
                }
            }) {

                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("clientId", CLIENT_ID);
                    params.put("accountId", Long.toString(App.getInstance().getId()));
                    params.put("accessToken", App.getInstance().getAccessToken());
                    params.put("appType", Integer.toString(APP_TYPE_ANDROID));
                    params.put("fcm_regId", App.getInstance().getGcmToken());

                    return params;
                }
            };

            App.getInstance().addToRequestQueue(jsonReq);

        } else {

            getSettings();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    public void getSettings() {

        if (settings_loaded) {

            showContentScreen();

        } else {

            showLoadingScreen();

            CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_ACCOUNT_GET_SETTINGS, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {

                            try {

                                if (!response.getBoolean("error")) {

                                    // Read Phone Login settings

                                    App.getInstance().getPhoneLoginSettings().read_from_json(response);
                                }

                            } catch (JSONException e) {

                                e.printStackTrace();

                            } finally {

                                settings_loaded = true;

                                showContentScreen();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                    showContentScreen();

                    Log.e("loadSettings()", error.toString());
                }
            }) {

                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("clientId", CLIENT_ID);
                    params.put("accountId", Long.toString(App.getInstance().getId()));
                    params.put("accessToken", App.getInstance().getAccessToken());

                    return params;
                }
            };

            App.getInstance().addToRequestQueue(jsonReq);
        }
    }

    public void setLanguageBtnTitle() {

//        Configuration config = new Configuration(getResources().getConfiguration());
//        config.setLocale(new Locale(App.getInstance().getLanguage()));
//
//        mExploreBtn.setText(createConfigurationContext(config).getText(R.string.action_explore).toString());
//        mLanguageBtn.setText(createConfigurationContext(config).getText(R.string.settings_language_label).toString() + ": " + App.getInstance().getLanguageNameByCode(App.getInstance().getLanguage()));
//
//        mLoginButton.setText(createConfigurationContext(config).getText(R.string.action_login).toString());
//        mSignupBtn.setText(createConfigurationContext(config).getText(R.string.action_signup).toString());
    }

    public void showContentScreen() {

        setLanguageBtnTitle();

        progressBar.setVisibility(View.GONE);

        if (EXPLORE_FEATURE) {

            mExploreBtn.setVisibility(View.VISIBLE);

        } else {

            mExploreBtn.setVisibility(View.GONE);
        }

        if (!GOOGLE_AUTHORIZATION) {

            mGoogleBtn.setVisibility(View.GONE);

        } else {

            mGoogleBtn.setVisibility(View.VISIBLE);
        }

        //

        mContentLayout.setVisibility(View.VISIBLE);
    }

    public void showLoadingScreen() {

        mContentLayout.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void showPhoneLoginBottomSheet(int state) {

        mBehavior.setState(state);

        FirebaseAuth mAuth;

        final String[] mVerificationId = new String[1];

        final boolean[] mVerificationInProgress = {false};

        String phoneCountry = "";
        final String[] phoneNumber = {""};
        String smsCode = "";

        //

        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null) {

            mAuth.signOut();
        }

        //

        final View view = getLayoutInflater().inflate(R.layout.bottom_sheet_phone_login, null);

        // Close

        ImageButton mCloseButton = view.findViewById(R.id.close_button);
        mCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mPhoneLoginBottomSheetDialog != null) {

                    mPhoneLoginBottomSheetDialog.dismiss();
                }
            }
        });

        //

        LinearLayout mProgressRow = view.findViewById(R.id.progressBarLayout);
        mProgressRow.setVisibility(View.GONE);

        LinearLayout mPhoneRow = view.findViewById(R.id.phoneInputLayout);
        //mPhoneRow.setVisibility(View.GONE);

        LinearLayout mCodeRow = view.findViewById(R.id.codeInputLayout);
        mCodeRow.setVisibility(View.GONE);

        LinearLayout mMainPanel = (LinearLayout) view.findViewById(R.id.main_panel);

        //

        LinearLayout mCountryCodeContainer = view.findViewById(R.id.country_code_text_container);
        TextView mCountryCode = view.findViewById(R.id.country_code_text);

        if (!App.getInstance().getPhoneLoginSettings().c_getList().isEmpty()) {

            mCountryCode.setText(App.getInstance().getPhoneLoginSettings().c_getList().get(0));
        }

        mCountryCodeContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // setup the alert builder
                AlertDialog.Builder builder = new AlertDialog.Builder(AppActivity.this);
                builder.setTitle(getString(R.string.auth_2fa_choose_code));

                // add a list
                String[] codes = App.getInstance().getPhoneLoginSettings().c_getList().toArray(new String[App.getInstance().getPhoneLoginSettings().c_getList().size()]);

                builder.setItems(codes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        mCountryCode.setText(codes[which]);
                    }
                });

                // create and show the alert dialog
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        //

        EditText mPhoneNumber = view.findViewById(R.id.phoneInputRow);

        EditText mCodeRow1 = view.findViewById(R.id.codeInputRow1);
        EditText mCodeRow2 = view.findViewById(R.id.codeInputRow2);
        EditText mCodeRow3 = view.findViewById(R.id.codeInputRow3);
        EditText mCodeRow4 = view.findViewById(R.id.codeInputRow4);
        EditText mCodeRow5 = view.findViewById(R.id.codeInputRow5);
        EditText mCodeRow6 = view.findViewById(R.id.codeInputRow6);

        //

        Button mActionButton = (Button) view.findViewById(R.id.action_2fa);
        mActionButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                if (mVerificationInProgress[0]) {

                    String code = mCodeRow1.getText().toString().trim() +
                            mCodeRow2.getText().toString().trim() +
                            mCodeRow3.getText().toString().trim() +
                            mCodeRow4.getText().toString().trim() +
                            mCodeRow5.getText().toString().trim() +
                            mCodeRow6.getText().toString().trim();

                    if (code.length() == 6) {

                        mProgressRow.setVisibility(View.VISIBLE);

                        mCodeRow.setVisibility(View.GONE);
                        mPhoneRow.setVisibility(View.GONE);
                        mActionButton.setVisibility(View.GONE);

                        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId[0], code);
                        signInWithPhoneAuthCredential(mAuth, credential, mVerificationInProgress, mPhoneRow, mCodeRow, mProgressRow, mActionButton);

                    } else {

                        Toast.makeText(AppActivity.this, getString(R.string.auth_2fa_sms_code_error), Toast.LENGTH_SHORT).show();
                    }

                } else {

                    String pn = mPhoneNumber.getText().toString().trim();

                    if (Pattern.compile("\\d{6,13}").matcher(pn).matches()) {

                        mPhoneRow.setVisibility(View.GONE);
                        mCodeRow.setVisibility(View.GONE);
                        mActionButton.setVisibility(View.GONE);

                        mProgressRow.setVisibility(View.VISIBLE);

                        //

                        PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = null;

                        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                            @Override
                            public void onVerificationCompleted(PhoneAuthCredential credential) {

                                // This callback will be invoked in two situations:
                                // 1 - Instant verification. In some cases the phone number can be instantly
                                //     verified without needing to send or enter a verification code.
                                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                                //     detect the incoming verification SMS and perform verification without
                                //     user action.

                                Log.d(TAG, "onVerificationCompleted:" + credential);

                                mVerificationInProgress[0] = false;

                                signInWithPhoneAuthCredential(mAuth, credential, mVerificationInProgress, mPhoneRow, mCodeRow, mProgressRow, mActionButton);
                            }

                            @Override
                            public void onVerificationFailed(FirebaseException e) {

                                // This callback is invoked in an invalid request for verification is made,
                                // for instance if the the phone number format is not valid.

                                Log.w(TAG, "onVerificationFailed", e);

                                //

                                mProgressRow.setVisibility(View.GONE);

                                mPhoneRow.setVisibility(View.VISIBLE);
                                mCodeRow.setVisibility(View.GONE);

                                mActionButton.setVisibility(View.VISIBLE);
                                mActionButton.setText(getText(R.string.action_send_code));

                                //

                                mVerificationInProgress[0] = false;

                                if (e instanceof FirebaseAuthInvalidCredentialsException) {

                                    // Invalid request

                                    //mInputRow.setError(getString(R.string.otp_verification_phone_number_error_msg_2));
                                    Toast.makeText(AppActivity.this, getString(R.string.otp_verification_phone_number_error_msg), Toast.LENGTH_SHORT).show();

                                } else if (e instanceof FirebaseTooManyRequestsException) {

                                    // The SMS quota for the project has been exceeded

                                    Toast.makeText(AppActivity.this, getString(R.string.otp_verification_many_requests_error_msg), Toast.LENGTH_SHORT).show();
                                    //Snackbar.make(getView(), getString(R.string.otp_verification_many_requests_error_msg), Snackbar.LENGTH_SHORT).show();

                                } else {

                                    Toast.makeText(AppActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken token) {

                                // The SMS verification code has been sent to the provided phone number, we
                                // now need to ask the user to enter the code and then construct a credential
                                // by combining the code with a verification ID.

                                Log.d(TAG, "onCodeSent:" + verificationId);

                                mPhoneNumber.setText("");
                                mCodeRow1.setText("");
                                mCodeRow2.setText("");
                                mCodeRow3.setText("");
                                mCodeRow4.setText("");
                                mCodeRow5.setText("");
                                mCodeRow6.setText("");

                                mCodeRow.setVisibility(View.VISIBLE);

                                mProgressRow.setVisibility(View.GONE);
                                mPhoneRow.setVisibility(View.GONE);

                                mActionButton.setVisibility(View.VISIBLE);
                                mActionButton.setText(getText(R.string.action_check_code));

                                mVerificationInProgress[0] = true;

                                // Save verification ID and resending token so we can use them later

                                mVerificationId[0] = verificationId;

                                Toast.makeText(AppActivity.this, getString(R.string.otp_verification_code_sent_msg), Toast.LENGTH_LONG).show();
                            }
                        };

                        //

                        phoneNumber[0] = "+" +  mCountryCode.getText().toString().replaceAll("[^0-9]", "")  + pn;

                        PhoneAuthOptions options =
                                PhoneAuthOptions.newBuilder(mAuth)
                                        .setPhoneNumber(phoneNumber[0])       // Phone number to verify
                                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                                        .setActivity(AppActivity.this)                 // Activity (for callback binding)
                                        .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                                        .build();

                        PhoneAuthProvider.verifyPhoneNumber(options);

                        mVerificationInProgress[0] = true;

                    } else {

                        Toast.makeText(getApplicationContext(), getString(R.string.auth_2fa_phone_number_error), Toast.LENGTH_SHORT).show();
                    }
                }




                //mPhoneLoginBottomSheetDialog.dismiss();
                //showAuthorizationBottomSheet(BottomSheetBehavior.STATE_HIDDEN);

//                Intent i = new Intent(MainActivity.this, RegisterActivity.class);
//                startActivity(i);
            }
        });

        //

        mCodeRow1.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (s.length() != 0) {

                    mCodeRow1.clearFocus();
                    mCodeRow2.requestFocus();
                    mCodeRow2.setCursorVisible(true);
                }
            }
        });

        mCodeRow2.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (s.length() != 0) {

                    mCodeRow2.clearFocus();
                    mCodeRow3.requestFocus();
                    mCodeRow3.setCursorVisible(true);
                }
            }
        });

        mCodeRow3.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (s.length() != 0) {

                    mCodeRow3.clearFocus();
                    mCodeRow4.requestFocus();
                    mCodeRow4.setCursorVisible(true);
                }
            }
        });

        mCodeRow4.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (s.length() != 0) {

                    mCodeRow4.clearFocus();
                    mCodeRow5.requestFocus();
                    mCodeRow5.setCursorVisible(true);
                }
            }
        });

        mCodeRow5.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (s.length() != 0) {

                    mCodeRow5.clearFocus();
                    mCodeRow6.requestFocus();
                    mCodeRow6.setCursorVisible(true);
                }
            }
        });

        //

        mPhoneLoginBottomSheetDialog = new BottomSheetDialog(this);
        mPhoneLoginBottomSheetDialog.setContentView(view);
        ((View) view.getParent()).setBackgroundColor(Color.TRANSPARENT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            mPhoneLoginBottomSheetDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        mPhoneLoginBottomSheetDialog.show();

        doKeepDialog(mPhoneLoginBottomSheetDialog);

        mPhoneLoginBottomSheetDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface dialog) {

                mPhoneLoginBottomSheetDialog = null;
            }
        });
    }

    private void signInWithPhoneAuthCredential(FirebaseAuth mAuth, PhoneAuthCredential credential, boolean[] mVerificationInProgress, LinearLayout mPhoneRow, LinearLayout mCodeRow, LinearLayout mProgressRow, Button mActionButton) {

        mProgressRow.setVisibility(View.VISIBLE);

        mPhoneRow.setVisibility(View.GONE);
        mCodeRow.setVisibility(View.GONE);
        mActionButton.setVisibility(View.GONE);

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(AppActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {

                            // Sign in success, update UI with the signed-in user's information

                            FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();

                            mUser.getIdToken(true)
                                    .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                                        public void onComplete(@NonNull Task<GetTokenResult> task) {

                                            if (task.isSuccessful()) {

                                                String token = task.getResult().getToken();

                                                Log.e("Firebase token", token);

                                                finishPhoneLoginVerification(token, mVerificationInProgress, mPhoneRow, mCodeRow, mProgressRow, mActionButton);

                                                //Toast.makeText(MainActivity.this, "finishVerification", Toast.LENGTH_LONG).show();

                                            } else {

                                                // Handle error -> task.getException();
                                            }
                                        }
                                    });

                        } else {

                            mProgressRow.setVisibility(View.GONE);

                            mPhoneRow.setVisibility(View.VISIBLE);
                            mCodeRow.setVisibility(View.GONE);

                            mActionButton.setVisibility(View.VISIBLE);
                            mActionButton.setText(getText(R.string.action_send_code));

                            mVerificationInProgress[0] = false;

                            Toast.makeText(AppActivity.this, getString(R.string.otp_verification_code_error_msg), Toast.LENGTH_LONG).show();

                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {

                                // The verification code entered was invalid

                                //Toast.makeText(getActivity(), getString(R.string.otp_verification_code_error_msg), Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });
    }

    public void finishPhoneLoginVerification(String token, boolean[] mVerificationInProgress, LinearLayout mPhoneRow, LinearLayout mCodeRow, LinearLayout mProgressRow, Button mActionButton) {

        mProgressRow.setVisibility(View.VISIBLE);

        mPhoneRow.setVisibility(View.GONE);
        mCodeRow.setVisibility(View.GONE);
        mActionButton.setVisibility(View.GONE);

        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_ACCOUNT_PHONE_LOGIN, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {

                            if (response.has("error")) {

                                if (!response.getBoolean("error")) {

                                    if (response.has("account_id")) {

                                        App.getInstance().setId(response.getLong("account_id"));
                                    }

                                    if (response.has("access_token")) {

                                        App.getInstance().setAccessToken(response.getString("access_token"));
                                    }

                                    App.getInstance().saveData();

                                    Intent intent = new Intent(AppActivity.this, AppActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);

                                } else {

//                                    if (mProgressRow != null) {
//
//                                        mProgressRow.setVisibility(View.GONE);
//
//                                        mPhoneRow.setVisibility(View.VISIBLE);
//                                        mCodeRow.setVisibility(View.GONE);
//
//                                        mActionButton.setVisibility(View.VISIBLE);
//                                        mActionButton.setText(getText(R.string.action_send_code));
//                                    }

                                    Toast.makeText(AppActivity.this, getString(R.string.otp_verification_phone_number_taken_error_msg), Toast.LENGTH_LONG).show();

                                    mPhoneLoginBottomSheetDialog.dismiss();
                                    showPhoneLoginBottomSheet(BottomSheetBehavior.STATE_HIDDEN);
                                }
                            }

                        } catch (JSONException e) {

                            e.printStackTrace();

                        } finally {

                            Log.e("Response", response.toString());
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Log.e("OTP finishVerification", error.toString());

                mPhoneLoginBottomSheetDialog.dismiss();
                showPhoneLoginBottomSheet(BottomSheetBehavior.STATE_HIDDEN);

                Toast.makeText(AppActivity.this, getString(R.string.otp_verification_phone_number_taken_error_msg), Toast.LENGTH_LONG).show();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("client_id", CLIENT_ID);
                params.put("app_type", Integer.toString(APP_TYPE_ANDROID));
                params.put("fcm_regId", App.getInstance().getGcmToken());
                params.put("lang", "en");
                params.put("token", token);

                return params;
            }
        };

        App.getInstance().addToRequestQueue(jsonReq);
    }

    private static void doKeepDialog(Dialog dialog){

        WindowManager.LayoutParams lp = new  WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        dialog.getWindow().setAttributes(lp);
    }
}
