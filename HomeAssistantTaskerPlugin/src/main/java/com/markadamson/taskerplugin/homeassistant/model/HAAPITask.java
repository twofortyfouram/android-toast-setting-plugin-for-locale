package com.markadamson.taskerplugin.homeassistant.model;

import android.os.AsyncTask;

public abstract class HAAPITask<Params,Progress,Result> extends AsyncTask<Params,Progress,HAAPIResult<Result>> {
    private final HAAPI mAPI;

    protected HAAPITask(HAServer server) {
        this.mAPI = new HAAPI(server);
    }

    @Override @SafeVarargs
    protected final HAAPIResult<Result> doInBackground(Params... params) {
        try {
            return new HAAPIResult<>(doAPIInBackground(mAPI, params));
        } catch (HAAPIException e) {
            return new HAAPIResult<>(e);
        }
    }

    protected abstract Result doAPIInBackground(HAAPI api, Params... params) throws HAAPIException;
}
