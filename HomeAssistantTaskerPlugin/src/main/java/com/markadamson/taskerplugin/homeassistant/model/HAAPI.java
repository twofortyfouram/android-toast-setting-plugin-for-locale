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
import java.net.MalformedURLException;
import java.net.ProtocolException;
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

    public boolean testServer() {
        boolean result = false;

        URL url = null;
        try {
            url = new URL(mServer.getBaseURL() + "/api/");
            HttpURLConnection httpConn = (HttpURLConnection)url.openConnection();
            httpConn.setRequestMethod("GET");
            httpConn.setRequestProperty("Authorization", "Bearer " + mServer.getAccessToken());

            InputStream inputStream = httpConn.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line = bufferedReader.readLine();

            JSONObject apiResult = new JSONObject(line);
            result = apiResult.has("message") && "API running.".equals(apiResult.getString("message"));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return result;
    }

    public List<String> getServices() {
        List<String> result = new ArrayList<>();

        try {
            URL url = new URL(mServer.getBaseURL() + "/api/services");
            HttpURLConnection httpConn = (HttpURLConnection)url.openConnection();
            httpConn.setRequestMethod("GET");
            httpConn.setRequestProperty("Authorization", "Bearer " + mServer.getAccessToken());

            InputStream inputStream = httpConn.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line = bufferedReader.readLine();

            JSONArray apiResults = new JSONArray(line);
            for (int d = 0; d < apiResults.length(); d++) {
                JSONObject jsonDomain = apiResults.getJSONObject(d);
                String strDomain = jsonDomain.getString("domain");

                JSONObject jsonServices = jsonDomain.getJSONObject("services");
                Iterator<String> keys = jsonServices.keys();
                while (keys.hasNext())
                    result.add(strDomain + "." + keys.next());
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Collections.sort(result);

        return result;
    }

    public void callService(String domain, String service, String data) {
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
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
