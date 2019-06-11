package com.markadamson.taskerplugin.homeassistant.model;

import android.os.AsyncTask;

public abstract class HAAPITask<Params,Progress,Result> extends AsyncTask<Params,Progress,Result> {
    protected final HAAPI mAPI;

    protected HAAPITask(HAServer server) {
        this.mAPI = new HAAPI(server);
    }
}
