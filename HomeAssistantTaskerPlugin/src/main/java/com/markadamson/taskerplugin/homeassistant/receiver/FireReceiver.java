/*
 * home-assistant-plugin-for-tasker <https://github.com/MarkAdamson/home-assistant-plugin-for-tasker>
 * Copyright 2019 Mark Adamson
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.markadamson.taskerplugin.homeassistant.receiver;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.markadamson.taskerplugin.homeassistant.Constants;
import com.markadamson.taskerplugin.homeassistant.TaskerPlugin;
import com.markadamson.taskerplugin.homeassistant.bundle.GetStatePluginBundleValues;
import com.markadamson.taskerplugin.homeassistant.bundle.PluginBundleValues;
import com.markadamson.taskerplugin.homeassistant.model.HAAPITask;
import com.markadamson.taskerplugin.homeassistant.model.HAServer;
import com.markadamson.taskerplugin.homeassistant.model.HAServerStore;

import java.lang.ref.WeakReference;

public final class FireReceiver extends AbstractPluginSettingReceiver {

    private abstract static class MyAPITask<Params,Progress,Result> extends HAAPITask<Params,Progress,Result> {
        WeakReference<Context> mContext;

        MyAPITask(Context context, HAServer server) {
            super(server);
            mContext = new WeakReference<>(context);
        }
    }

    private static class CallServiceTask extends MyAPITask<String,Void,Void> {

        CallServiceTask(Context context, HAServer server) {
            super(context, server);
        }

        @Override
        protected Void doInBackground(String... strings) {
            mAPI.callService(strings[0], strings[1], strings[2]);
            return null;
        }
    }

    private static class GetStateTask extends MyAPITask<String,Void,String> {
        private final Intent mFireIntent;
        private final String mVariable;

        GetStateTask(Context context, HAServer server, Intent fireIntent, String variable) {
            super(context, server);
            mFireIntent = fireIntent;
            mVariable = variable;
        }

        @Override
        protected String doInBackground(String... strings) {
            return mAPI.getState(strings[0]);
        }

        @Override
        protected void onPostExecute(String state) {
            Context context = mContext.get();
            if (context == null) return;

            Bundle vars = new Bundle();
            vars.putString(mVariable, state);
            TaskerPlugin.Setting.signalFinish(context, mFireIntent, TaskerPlugin.Setting.RESULT_CODE_OK, vars);
        }
    }

    @Override
    protected boolean isBundleValid(@NonNull final Bundle bundle) {
        if (bundle.getInt(PluginBundleValues.BUNDLE_EXTRA_INT_VERSION_CODE) < 3 || bundle.getInt(Constants.BUNDLE_EXTRA_BUNDLE_TYPE) == Constants.BUNDLE_CALL_SERVICE)
            return PluginBundleValues.isBundleValid(bundle);
        else
            return GetStatePluginBundleValues.isBundleValid(bundle);
    }

    @Override
    protected boolean isAsync() {
        return false;
    }

    @Override
    protected void firePluginSetting(@NonNull final Context context, @NonNull final Intent intent, @NonNull final Bundle bundle) {
        if (bundle.getInt(PluginBundleValues.BUNDLE_EXTRA_INT_VERSION_CODE) < 3 || bundle.getInt(Constants.BUNDLE_EXTRA_BUNDLE_TYPE) == Constants.BUNDLE_CALL_SERVICE) {
            String[] service = PluginBundleValues.getService(bundle).split("\\.");

            new CallServiceTask(context, HAServerStore.getInstance().getServers().get(PluginBundleValues.getServer(bundle)))
                    .execute(service[0], service[1], PluginBundleValues.getData(bundle));
        } else {
//            if (!TaskerPlugin.Setting.hostSupportsVariableReturn(bundle)) {
//                setResultCode(TaskerPlugin.Setting.RESULT_CODE_FAILED);
//                return;
//            }

            String entity = GetStatePluginBundleValues.getEntity(bundle);
            new GetStateTask(context, HAServerStore.getInstance().getServers().get(PluginBundleValues.getServer(bundle)), intent, GetStatePluginBundleValues.getVariable(bundle)).execute(entity);
            setResultCode(TaskerPlugin.Setting.RESULT_CODE_PENDING);
        }
    }


}
