package ru.ifsoft.network.app;

import android.app.Application;
import android.util.Log;


import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import ru.ifsoft.network.constants.Constants;

public class PhoneLoginSettings extends Application implements Constants {

	public static final String TAG = PhoneLoginSettings.class.getSimpleName();

    private int pl_enabled = 1;

    private ArrayList<String> c_list;

	@Override
	public void onCreate() {

		super.onCreate();
	}

    public void read_from_json(JSONObject jsonData) {

        c_list = new ArrayList<String>();

        try {

            if (jsonData.has("pl_enabled")) {

                this.setEnabled(jsonData.getInt("pl_enabled"));
            }

            if (jsonData.has("c_list")) {

                JSONArray itemsArray = jsonData.getJSONArray("c_list");

                if (itemsArray.length() > 0) {

                    c_list.clear();

                    for (int i = 0; i < itemsArray.length(); i++) {

                        JSONObject itemObj = (JSONObject) itemsArray.get(i);

                        c_list.add(itemObj.getString("c_name") + " (+" + itemObj.getString("p_code") + ")");

                        //Log.e("c_list", itemObj.getString("c_name") + " (+" + itemObj.getString("p_code") + ")");
                    }
                }
            }

        } catch (Throwable t) {

            Log.e("PhoneLoginSettings", "Could not parse malformed JSON: \"" + jsonData.toString() + "\"");

        } finally {

            Log.e("PhoneLoginSettings", "");
        }
    }

    //

    public void setEnabled(int value) {

        this.pl_enabled = value;
    }

    public int getEnabled() {

        return this.pl_enabled;
    }

    public ArrayList<String> c_getList() {

        if (this.c_list == null) {

            this.c_list = new ArrayList<String>();
        }

        return this.c_list;
    }
}