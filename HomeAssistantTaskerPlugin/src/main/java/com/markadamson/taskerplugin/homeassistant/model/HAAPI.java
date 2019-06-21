package com.markadamson.taskerplugin.homeassistant.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class HAAPI {
    private final HAServer mServer;

    public HAAPI(HAServer mServer) {
        this.mServer = mServer;
    }

    public boolean testServer() throws HAAPIException {
        try {
            URL url = new URL(mServer.getBaseURL() + "/api/");
            HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
            httpConn.setRequestMethod("GET");
            httpConn.setRequestProperty("Authorization", "Bearer " + mServer.getAccessToken());

            InputStream inputStream = httpConn.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line = bufferedReader.readLine();

            JSONObject apiResult = new JSONObject(line);
            return apiResult.has("message") && "API running.".equals(apiResult.getString("message"));
        } catch (IOException e) {
            throw new HAAPIException("Network Error", e);
        } catch (JSONException e) {
            throw new HAAPIException("JSON Error", e);
        }
    }

    public List<String> getServices() throws HAAPIException {
        try {
            URL url = new URL(mServer.getBaseURL() + "/api/services");
            HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
            httpConn.setRequestMethod("GET");
            httpConn.setRequestProperty("Authorization", "Bearer " + mServer.getAccessToken());

            InputStream inputStream = httpConn.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line = bufferedReader.readLine();

            List<String> result = new ArrayList<>();
            JSONArray apiResults = new JSONArray(line);
            for (int d = 0; d < apiResults.length(); d++) {
                JSONObject jsonDomain = apiResults.getJSONObject(d);
                String strDomain = jsonDomain.getString("domain");

                JSONObject jsonServices = jsonDomain.getJSONObject("services");
                Iterator<String> keys = jsonServices.keys();
                while (keys.hasNext())
                    result.add(strDomain + "." + keys.next());
            }

            Collections.sort(result);
            return result;
        } catch (IOException e) {
            throw new HAAPIException("Network Error", e);
        } catch (JSONException e) {
            throw new HAAPIException("JSON Error", e);
        }
    }

    public void callService(String domain, String service, String data) throws HAAPIException {
        try {
            URL url = new URL(mServer.getBaseURL() + "/api/services/" + domain + "/" + service);
            HttpURLConnection httpConn = (HttpURLConnection)url.openConnection();
            httpConn.setRequestMethod("POST");
            httpConn.setRequestProperty("Authorization", "Bearer " + mServer.getAccessToken());
            httpConn.setRequestProperty("Content-Type", "application/json");
            httpConn.setDoOutput(true);

            if (data != null &! data.isEmpty()) {
                JSONObject jsonBody = new JSONObject(data);
                byte[] outputBytes = jsonBody.toString().getBytes("UTF-8");
                OutputStream os = httpConn.getOutputStream();
                os.write(outputBytes);
                os.close();
            }

            InputStream inputStream = httpConn.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            bufferedReader.readLine();
        } catch (IOException e) {
            throw new HAAPIException("Network Error", e);
        } catch (JSONException e) {
            throw new HAAPIException("JSON Error", e);
        }
    }

    public List<String> getEntities() throws HAAPIException {
        try {
            URL url = new URL(mServer.getBaseURL() + "/api/states");
            HttpURLConnection httpConn = (HttpURLConnection)url.openConnection();
            httpConn.setRequestMethod("GET");
            httpConn.setRequestProperty("Authorization", "Bearer " + mServer.getAccessToken());

            InputStream inputStream = httpConn.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line = bufferedReader.readLine();

            List<String> result = new ArrayList<>();
            JSONArray apiResults = new JSONArray(line);
            for (int e = 0; e < apiResults.length(); e++)
                result.add(apiResults.getJSONObject(e).getString("entity_id"));

            Collections.sort(result);
            return result;
        } catch (IOException e) {
            throw new HAAPIException("Network Error", e);
        } catch (JSONException e) {
            throw new HAAPIException("JSON Error", e);
        }
    }

    public HAEntity getEntity(String entityId) throws HAAPIException {
        try {
            URL url = new URL(mServer.getBaseURL() + "/api/states/" + entityId);
            HttpURLConnection httpConn = (HttpURLConnection)url.openConnection();
            httpConn.setRequestMethod("GET");
            httpConn.setRequestProperty("Authorization", "Bearer " + mServer.getAccessToken());

            InputStream inputStream = httpConn.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line = bufferedReader.readLine();

            JSONObject json = new JSONObject(line);

            return new HAEntity(
                    json.getString("state"),
                    json.getString("attributes"));
        } catch (IOException e) {
            throw new HAAPIException("Network Error", e);
        } catch (JSONException e) {
            throw new HAAPIException("JSON Error", e);
        }
    }
}
