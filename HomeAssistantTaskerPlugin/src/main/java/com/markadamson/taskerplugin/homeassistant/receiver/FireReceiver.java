/*
 * android-toast-setting-plugin-for-locale <https://github.com/twofortyfouram/android-toast-setting-plugin-for-locale>
 * Copyright 2014 two forty four a.m. LLC
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

import com.markadamson.taskerplugin.homeassistant.bundle.PluginBundleValues;
import com.markadamson.taskerplugin.homeassistant.model.HAAPI;
import com.markadamson.taskerplugin.homeassistant.model.HAServerStore;
import com.twofortyfouram.locale.sdk.client.receiver.AbstractPluginSettingReceiver;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.Toast;

public final class FireReceiver extends AbstractPluginSettingReceiver {

    @Override
    protected boolean isBundleValid(@NonNull final Bundle bundle) {
        return PluginBundleValues.isBundleValid(bundle);
    }

    @Override
    protected boolean isAsync() {
        return true;
    }

    @Override
    protected void firePluginSetting(@NonNull final Context context, @NonNull final Bundle bundle) {
//        Toast.makeText(context, PluginBundleValues.getService(bundle), Toast.LENGTH_LONG).show();

        String[] service = PluginBundleValues.getService(bundle).split("\\.");
        new HAAPI(HAServerStore.getInstance().getServers().get(PluginBundleValues.getServer(bundle)))
                .callService(service[0], service[1], PluginBundleValues.getData(bundle));
    }
}
