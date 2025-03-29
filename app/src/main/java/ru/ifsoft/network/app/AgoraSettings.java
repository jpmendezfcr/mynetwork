package ru.ifsoft.network.app;

import android.app.Application;
import android.util.Log;

import org.json.JSONObject;

import ru.ifsoft.network.constants.Constants;

public class AgoraSettings extends Application implements Constants {

	public static final String TAG = AgoraSettings.class.getSimpleName();
    private String agora_app_id = "";
    private String agora_app_certificate = "";

    private int agora_app_enabled = 1;

	@Override
	public void onCreate() {

		super.onCreate();
	}

    public void read_from_json(JSONObject jsonData) {

        try {

            if (jsonData.has("agora_app_enabled")) {

                this.setAppEnabled(jsonData.getInt("agora_app_enabled"));
            }

            if (jsonData.has("agora_app_id")) {

                this.setAppId(jsonData.getString("agora_app_id"));
            }

            if (jsonData.has("agora_app_certificate")) {

                this.setAppCertificate(jsonData.getString("agora_app_certificate"));
            }

        } catch (Throwable t) {

            Log.e("AgoraSettings", "Could not parse malformed JSON: \"" + jsonData.toString() + "\"");

        } finally {

            Log.e("AgoraSettings", "");
            Log.e("Agora app id: ", this.getAppId());
            Log.e("Agora app certificate: ", this.getAppCertificate());
        }
    }

    //

    public void setAppEnabled(int value) {

        this.agora_app_enabled = value;

//        this.agora_app_enabled = 0;
    }

    public int getAppEnabled() {

        return this.agora_app_enabled;
    }

    public void setAppId(String appId) {

        this.agora_app_id = appId;
    }

    public String getAppId() {

        return this.agora_app_id;
    }

    public void setAppCertificate(String agora_app_certificate) {

        this.agora_app_certificate = agora_app_certificate;
    }

    public String getAppCertificate() {

        return this.agora_app_certificate;
    }
}