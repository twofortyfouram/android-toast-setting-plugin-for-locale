package com.markadamson.taskerplugin.homeassistant.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class HAServerStore {
    private final Context mContext;

    public HAServerStore(final Context context) {
        mContext = context;
    }

    private static String PREFS_KEY = "com.markadamson.taskerplugin.homeassistant.model.HAServerStore.PREFS_KEY";

    public Map<UUID, HAServer> getServers() {
        Map<UUID,HAServer> result = new HashMap<>();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        if (prefs.contains(PREFS_KEY)) {
            try {
                JSONObject jsonServers = new JSONObject(prefs.getString(PREFS_KEY, ""));
                Iterator<String> keys = jsonServers.keys();

                while (keys.hasNext()) {
                    String uuid = keys.next();
                    result.put(UUID.fromString(uuid), HAServer.fromJSON((JSONObject) jsonServers.get(uuid)));
                }
            } catch (JSONException e) {
                e.printStackTrace();
                prefs.edit().remove(PREFS_KEY).apply();
            }
        }

        return result;
    }

    public UUID addServer(HAServer server) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

        JSONObject jsonServers;
        if (prefs.contains(PREFS_KEY)) {
            try {
                jsonServers = new JSONObject(prefs.getString(PREFS_KEY, ""));
            } catch (JSONException e) {
                jsonServers = new JSONObject();
            }
        } else
            jsonServers = new JSONObject();

        UUID result = UUID.randomUUID();

        try {
            jsonServers.put(result.toString(), server.toJSON());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        prefs.edit().putString(PREFS_KEY, jsonServers.toString()).apply();

        return result;
    }

    public void deleteServer(UUID serverID) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

        if (prefs.contains(PREFS_KEY)) {
            JSONObject jsonServers;
            try {
                jsonServers = new JSONObject(prefs.getString(PREFS_KEY, ""));
            } catch (JSONException e) {
                e.printStackTrace();
                prefs.edit().remove(PREFS_KEY).apply();
                return;
            }

            if (jsonServers.has(serverID.toString())) {
                jsonServers.remove(serverID.toString());
                prefs.edit().putString(PREFS_KEY, jsonServers.toString()).apply();
            }
        }
    }

    public void updateServer(UUID serverID, HAServer server) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

        if (!prefs.contains(PREFS_KEY))
            throw new RuntimeException("No such server in storage!");

        JSONObject jsonServers;

        try {
            jsonServers = new JSONObject(prefs.getString(PREFS_KEY, ""));
        } catch (JSONException e) {
            e.printStackTrace();
            prefs.edit().remove(PREFS_KEY).apply();
            throw new RuntimeException("No such server in storage!", e);
        }

        if (!jsonServers.has(serverID.toString()))
            throw new RuntimeException("No such server in storage!");

        jsonServers.remove(serverID.toString());
        try {
            jsonServers.put(serverID.toString(), server.toJSON());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        prefs.edit().putString(PREFS_KEY, jsonServers.toString()).apply();
    }
}
