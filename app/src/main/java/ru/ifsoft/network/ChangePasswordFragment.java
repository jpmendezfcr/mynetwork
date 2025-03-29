package ru.ifsoft.network;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import ru.ifsoft.network.app.App;
import ru.ifsoft.network.constants.Constants;
import ru.ifsoft.network.util.CustomRequest;
import ru.ifsoft.network.util.Helper;

public class ChangePasswordFragment extends Fragment implements Constants {

    private ProgressDialog pDialog;

    TextView mSubTitle, mCurrentLogin;

    EditText mCurrentPassword, mNewPassword, mNewLogin;

    String sCurrentPassword, sNewPassword, sNewLogin;

    private Boolean loading = false;

    public ChangePasswordFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setRetainInstance(true);

        setHasOptionsMenu(true);

        initpDialog();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_change_password, container, false);

        if (loading) {

            showpDialog();
        }

        mSubTitle = (TextView) rootView.findViewById(R.id.label_create_login_title);
        mCurrentLogin = (TextView) rootView.findViewById(R.id.label_create_login_subtitle);

        mCurrentPassword = (EditText) rootView.findViewById(R.id.currentPassword);
        mNewPassword = (EditText) rootView.findViewById(R.id.newPassword);
        mNewLogin = (EditText) rootView.findViewById(R.id.newLogin);

        mSubTitle.setVisibility(View.GONE);
        mCurrentLogin.setVisibility(View.GONE);
        mNewLogin.setVisibility(View.GONE);

        getActivity().setTitle(getString(R.string.settings_change_password));

        if (App.getInstance().getAccountFree() != 0) {

            getActivity().setTitle(getString(R.string.label_login_create));

            mSubTitle.setVisibility(View.VISIBLE);
            mCurrentLogin.setVisibility(View.VISIBLE);
            mNewLogin.setVisibility(View.VISIBLE);

            mCurrentPassword.setVisibility(View.GONE);

            mCurrentLogin.setText(getString(R.string.label_login_current) + ": " + App.getInstance().getUsername());
        }

        // Inflate the layout for this fragment
        return rootView;
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

        if (!pDialog.isShowing()) pDialog.show();
    }

    protected void hidepDialog() {

        if (pDialog.isShowing()) pDialog.dismiss();
    }

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
        // Inflate the menu; this adds items to the action bar if it is present.

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_set_password: {

//                mNewLogin.clearFocus();
//                mNewPassword.clearFocus();
//                mCurrentPassword.clearFocus();

                View view = getActivity().getCurrentFocus();

                if (view != null) {

                    InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }

                sCurrentPassword = mCurrentPassword.getText().toString();
                sNewPassword = mNewPassword.getText().toString();
                sNewLogin = mNewLogin.getText().toString();

                if (App.getInstance().getAccountFree() != 0) {

                    if (checkLogin(sNewLogin)) {

                        if (checkNewPassword(sNewPassword)) {

                            if (App.getInstance().isConnected()) {

                                accountSetPassword();

                            } else {

                                Toast.makeText(getActivity(), getText(R.string.msg_network_error), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                } else {

                    if (checkCurrentPassword(sCurrentPassword)) {

                        if (checkNewPassword(sNewPassword)) {

                            if (App.getInstance().isConnected()) {

                                accountSetPassword();

                            } else {

                                Toast.makeText(getActivity(), getText(R.string.msg_network_error), Toast.LENGTH_SHORT).show();
                            }

                        }
                    }
                }

                return true;
            }

            default: {

                break;
            }
        }

        return false;
    }

    public void accountSetPassword() {

        loading = true;

        showpDialog();

        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_ACCOUNT_SETPASSWORD, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {

                            if (response.has("error")) {

                                if (!response.getBoolean("error")) {

                                    if (App.getInstance().getAccountFree() != 0) {

                                        App.getInstance().setAccountFree(0);
                                        Toast.makeText(getActivity(), getText(R.string.label_login_create_success), Toast.LENGTH_SHORT).show();

                                    } else {

                                        Toast.makeText(getActivity(), getText(R.string.msg_password_changed), Toast.LENGTH_SHORT).show();
                                    }

                                    getActivity().finish();

                                } else {

                                    if (response.has("error_type")) {

                                        if (response.getInt("error_type") == 0) {

                                            // Error login

                                            Toast.makeText(getActivity(), getText(R.string.label_login_create_error), Toast.LENGTH_SHORT).show();

                                        } else {

                                            // Error Password

                                            Toast.makeText(getActivity(), getText(R.string.error_password), Toast.LENGTH_SHORT).show();
                                        }

                                    } else {

                                        Toast.makeText(getActivity(), getText(R.string.label_password_old_error), Toast.LENGTH_SHORT).show();
                                    }
                                }
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

                loading = false;

                hidepDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("accountId", Long.toString(App.getInstance().getId()));
                params.put("accessToken", App.getInstance().getAccessToken());
                params.put("currentPassword", sCurrentPassword);
                params.put("newPassword", sNewPassword);
                params.put("login", sNewLogin);

                return params;
            }
        };

        App.getInstance().addToRequestQueue(jsonReq);
    }

    public Boolean checkLogin(String login) {

        Helper helper = new Helper();

        if (login.length() == 0) {

            mNewLogin.setError(getString(R.string.error_field_empty));

            return false;
        }

        if (login.length() < 5) {

            mNewLogin.setError(getString(R.string.error_small_username));

            return false;
        }

        if (!helper.isValidLogin(login)) {

            mNewLogin.setError(getString(R.string.error_wrong_format));

            return false;
        }

        mNewLogin.setError(null);

        return true;
    }

    public Boolean checkCurrentPassword(String password) {

        Helper helper = new Helper();

        if (password.length() == 0) {

            mCurrentPassword.setError(getString(R.string.error_field_empty));

            return false;
        }

        if (password.length() < 6) {

            mCurrentPassword.setError(getString(R.string.error_small_password));

            return false;
        }

        if (!helper.isValidPassword(password)) {

            mCurrentPassword.setError(getString(R.string.error_wrong_format));

            return false;
        }

        mCurrentPassword.setError(null);

        return true;
    }

    public Boolean checkNewPassword(String password) {

        Helper helper = new Helper();

        if (password.length() == 0) {

            mNewPassword.setError(getString(R.string.error_field_empty));

            return false;
        }

        if (password.length() < 6) {

            mNewPassword.setError(getString(R.string.error_small_password));

            return false;
        }

        if (!helper.isValidPassword(password)) {

            mNewPassword.setError(getString(R.string.error_wrong_format));

            return false;
        }

        mNewPassword.setError(null);

        return true;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}