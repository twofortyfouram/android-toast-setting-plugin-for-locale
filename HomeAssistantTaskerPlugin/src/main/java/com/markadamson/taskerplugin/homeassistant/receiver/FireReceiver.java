/*
 * home-assistant-plugin-for-tasker <https://github.com/MarkAdamson/home-assistant-plugin-for-tasker>
 * Copyright 2019 Mark Adamson
 *
 * Original author:
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

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.markadamson.taskerplugin.homeassistant.Constants;
import com.markadamson.taskerplugin.homeassistant.TaskerPlugin;
import com.markadamson.taskerplugin.homeassistant.bundle.GetStatePluginBundleValues;
import com.markadamson.taskerplugin.homeassistant.bundle.PluginBundleValues;
import com.markadamson.taskerplugin.homeassistant.service.ActionService;
import com.twofortyfouram.log.Lumberjack;

public final class FireReceiver extends AbstractPluginSettingReceiver {

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
        Lumberjack.d("FireReceiver.firePluginSetting");
        intent.putExtra(ActionService.EXT_BUNDLE, bundle);
        ActionService.enqueueWork(context, intent);
        Lumberjack.d("Set result code \" Pending\"");
        if (isOrderedBroadcast())
            setResultCode(TaskerPlugin.Setting.RESULT_CODE_PENDING);
    }


}
