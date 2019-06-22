package com.markadamson.taskerplugin.homeassistant.service;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;
import android.widget.Toast;

import com.markadamson.taskerplugin.homeassistant.Constants;
import com.markadamson.taskerplugin.homeassistant.TaskerPlugin;
import com.markadamson.taskerplugin.homeassistant.bundle.GetStatePluginBundleValues;
import com.markadamson.taskerplugin.homeassistant.bundle.PluginBundleValues;
import com.markadamson.taskerplugin.homeassistant.model.HAAPI;
import com.markadamson.taskerplugin.homeassistant.model.HAAPIException;
import com.markadamson.taskerplugin.homeassistant.model.HAEntity;
import com.markadamson.taskerplugin.homeassistant.model.HAServerStore;
import com.twofortyfouram.log.Lumberjack;

/**
 * Created by marka on 14/06/2019.
 */

public class ActionService extends JobIntentService {
    public static final String EXT_BUNDLE = "com.markadamson.taskerplugin.homeassistant.service.ActionService.EXT_BUNDLE";

    /**
     * Unique job ID for this service.
     */
    static final int JOB_ID = 1000;

    /**
     * Convenience method for enqueuing work in to this service.
     */
    public static void enqueueWork(Context context, Intent work) {
        Lumberjack.d("ActionService.enqueueWork");
        enqueueWork(context, ActionService.class, JOB_ID, work);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        Lumberjack.d("ActionService.onHandleWork");
        Bundle bundle = intent.getBundleExtra(EXT_BUNDLE);
        HAServerStore servers = new HAServerStore(this);

        try {
            if (bundle.getInt(PluginBundleValues.BUNDLE_EXTRA_INT_VERSION_CODE) < 3 || bundle.getInt(Constants.BUNDLE_EXTRA_BUNDLE_TYPE) == Constants.BUNDLE_CALL_SERVICE) {
                Lumberjack.d("Call Service", bundle);
                String[] service = PluginBundleValues.getService(bundle).split("\\.");
                Lumberjack.d("Calling api...");
                new HAAPI(servers.getServers().get(PluginBundleValues.getServer(bundle)))
                        .callService(service[0], service[1], PluginBundleValues.getData(bundle));
                Lumberjack.d("Signalling finish...");
                TaskerPlugin.Setting.signalFinish(this, intent, TaskerPlugin.Setting.RESULT_CODE_OK, null);
            } else {
                Lumberjack.d("Get State", bundle);
                String entityId = GetStatePluginBundleValues.getEntity(bundle);
                Lumberjack.d("Calling api...");
                HAEntity entity = new HAAPI(servers.getServers().get(PluginBundleValues.getServer(bundle)))
                        .getEntity(entityId);
                Bundle vars = new Bundle();
                vars.putString(GetStatePluginBundleValues.getStateVariable(bundle), entity.getState());

                String attrsVar = GetStatePluginBundleValues.getAttrsVariable(bundle);
                if (!attrsVar.isEmpty())
                    vars.putString(attrsVar, entity.getAttributes());

                Lumberjack.d("Signalling finish...");
                TaskerPlugin.Setting.signalFinish(this, intent, TaskerPlugin.Setting.RESULT_CODE_OK, vars);
            }
        } catch (HAAPIException e) {
            Lumberjack.e(e.getMessage());
            e.printStackTrace();
            TaskerPlugin.Setting.signalFinish(this, intent, TaskerPlugin.Setting.RESULT_CODE_FAILED, null);
        }
    }
}
