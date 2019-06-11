package com.markadamson.taskerplugin.homeassistant.model;

import org.json.JSONException;
import org.json.JSONObject;

public class HAServer {
    private String name, baseURL, accessToken;

    public HAServer(String name, String baseURL, String accessToken) {
        this.name = name;
        this.baseURL = baseURL;
        this.accessToken = accessToken;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBaseURL() {
        return baseURL;
    }

    public void setBaseURL(String baseURL) {
        this.baseURL = baseURL;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    @Override
    public String toString() {
        return getName();
    }

    private static String NAME = "Name";
    private static String BASE_URL = "BaseURL";
    private static String ACCESS_TOKEN = "AccessToken";

    public static HAServer fromJSON(JSONObject json) throws JSONException {
        return new HAServer(
                json.getString(NAME), json.getString(BASE_URL), json.getString(ACCESS_TOKEN)
        );
    }

    public JSONObject toJSON() {
        JSONObject result = new JSONObject();
        try {
            result.put(NAME, name);
            result.put(BASE_URL, baseURL);
            result.put(ACCESS_TOKEN, accessToken);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }
}
