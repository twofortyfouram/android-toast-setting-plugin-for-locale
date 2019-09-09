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
        HttpURLConnection httpConn = null;
        try {
            URL url = new URL(mServer.getBaseURL() + "/api/");
            httpConn = (HttpURLConnection) url.openConnection();
            httpConn.setRequestMethod("GET");
            httpConn.setRequestProperty("Authorization", "Bearer " + mServer.getAccessToken());

            InputStream inputStream = httpConn.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line = bufferedReader.readLine();

            JSONObject apiResult = new JSONObject(line);
            return apiResult.has("message") && "API running.".equals(apiResult.getString("message"));
        } catch (IOException e) {
            if (httpConn != null)
                try {
                    throw new HAAPIException("Network Error: ".concat(httpConn.getResponseMessage()), e);
                } catch (IOException e1) {
                    throw new HAAPIException("Network Error", e);
                }
            else
                throw new HAAPIException("IO Error", e);
        } catch (JSONException e) {
            throw new HAAPIException("JSON Error", e);
        }
    }

    public List<String> getServices() throws HAAPIException {
        HttpURLConnection httpConn = null;
        try {
            URL url = new URL(mServer.getBaseURL() + "/api/services");
            httpConn = (HttpURLConnection) url.openConnection();
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
            if (httpConn != null)
                try {
                    throw new HAAPIException("Network Error: ".concat(httpConn.getResponseMessage()), e);
                } catch (IOException e1) {
                    throw new HAAPIException("Network Error", e);
                }
            else
                throw new HAAPIException("IO Error", e);
        } catch (JSONException e) {
            throw new HAAPIException("JSON Error", e);
        }
    }

    public void callService(String domain, String service, String data) throws HAAPIException {
        HttpURLConnection httpConn = null;
        try {
            URL url = new URL(mServer.getBaseURL() + "/api/services/" + domain + "/" + service);
            httpConn = (HttpURLConnection)url.openConnection();
            httpConn.setRequestMethod("POST");
            httpConn.setRequestProperty("Authorization", "Bearer " + mServer.getAccessToken());
            httpConn.setRequestProperty("Content-Type", "application/json");
            httpConn.setDoOutput(true);

            if (data != null &! data.isEmpty()) {
                OutputStream os = httpConn.getOutputStream();
                os.write(data.getBytes("UTF-8"));
                os.close();
            }

            if (httpConn.getResponseCode() != 200) {
                InputStream errorStream = httpConn.getErrorStream();
                InputStreamReader errorStreamReader = new InputStreamReader(errorStream);
                BufferedReader bufferedReader = new BufferedReader(errorStreamReader);
                JSONObject jsonResult = new JSONObject(bufferedReader.readLine());
                throw new HAAPIException(jsonResult.getString("message"));
            }

        } catch (IOException e) {
            if (httpConn != null)
                try {
                    throw new HAAPIException("Network Error: ".concat(httpConn.getResponseMessage()), e);
                } catch (IOException e1) {
                    throw new HAAPIException("Network Error", e);
                }
            else
                throw new HAAPIException("IO Error", e);
        } catch (JSONException e) {
            throw new HAAPIException("JSON Error", e);
        }
    }

    public List<String> getEntities() throws HAAPIException {
        HttpURLConnection httpConn = null;
        try {
            URL url = new URL(mServer.getBaseURL() + "/api/states");
            httpConn = (HttpURLConnection)url.openConnection();
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
            if (httpConn != null)
                try {
                    throw new HAAPIException("Network Error: ".concat(httpConn.getResponseMessage()), e);
                } catch (IOException e1) {
                    throw new HAAPIException("Network Error", e);
                }
            else
                throw new HAAPIException("IO Error", e);
        } catch (JSONException e) {
            throw new HAAPIException("JSON Error", e);
        }
    }

    public HAEntity getEntity(String entityId) throws HAAPIException {
        HttpURLConnection httpConn = null;
        try {
            URL url = new URL(mServer.getBaseURL() + "/api/states/" + entityId);
            httpConn = (HttpURLConnection)url.openConnection();
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
            if (httpConn != null)
                try {
                    throw new HAAPIException("Network Error: ".concat(httpConn.getResponseMessage()), e);
                } catch (IOException e1) {
                    throw new HAAPIException("Network Error", e);
                }
            else
                throw new HAAPIException("IO Error", e);
        } catch (JSONException e) {
            throw new HAAPIException("JSON Error", e);
        }
    }

    public String renderTemplate(String template) throws HAAPIException {
        HttpURLConnection httpConn = null;
        try {
            URL url = new URL(mServer.getBaseURL() + "/api/template");
            httpConn = (HttpURLConnection)url.openConnection();
            httpConn.setRequestMethod("POST");
            httpConn.setRequestProperty("Authorization", "Bearer " + mServer.getAccessToken());
            httpConn.setRequestProperty("Content-Type", "application/json");
            httpConn.setDoOutput(true);

            JSONObject jsonBody = new JSONObject();
            jsonBody.put("template", template);
            byte[] outputBytes = jsonBody.toString().getBytes("UTF-8");
            OutputStream os = httpConn.getOutputStream();
            os.write(outputBytes);
            os.close();

            StringBuilder sb = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(httpConn.getInputStream()));
            int c;
            while ((c = reader.read()) != -1)
                sb.append((char) c);
            return sb.toString();
        } catch (IOException e) {
            if (httpConn != null)
                try {
                    throw new HAAPIException("Network Error: ".concat(httpConn.getResponseMessage()), e);
                } catch (IOException e1) {
                    throw new HAAPIException("Network Error", e);
                }
            else
                throw new HAAPIException("IO Error", e);
        } catch (JSONException e) {
            throw new HAAPIException("JSON Error", e);
        }
    }
}
