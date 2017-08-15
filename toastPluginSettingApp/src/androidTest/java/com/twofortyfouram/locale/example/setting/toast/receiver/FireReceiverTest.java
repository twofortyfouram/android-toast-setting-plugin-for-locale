/*
 * android-toast-setting-plugin-for-locale https://github.com/twofortyfouram/android-toast-setting-plugin-for-locale
 * Copyright (C) 2009–2018 two forty four a.m. LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.twofortyfouram.locale.example.setting.toast.receiver;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.os.Bundle;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.filters.SmallTest;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.twofortyfouram.locale.api.LocalePluginIntent;
import com.twofortyfouram.locale.example.setting.toast.bundle.PluginJsonValues;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public final class FireReceiverTest {
    /*
     * These test cases perform sanity checks. These tests are not very extensive and additional
     * testing is required to verify the BroadcastReceiver works correctly. For example, a human
     * would need to manually verify that a Toast message appears when a correct Intent is sent to
     * the receiver. Depending on what your setting implements, you may be able to verify more
     * easily that the setting triggered the desired result via unit tests than this sample setting
     * can.
     */

    @SmallTest
    @Test
    public void bad_json() {
        final BroadcastReceiver fireReceiver = new FireReceiver();

        final JSONObject json = new JSONObject();

        final Bundle bundle = new Bundle();
        bundle.putString(LocalePluginIntent.EXTRA_STRING_JSON, json.toString());

        /*
         * The receiver shouldn't crash if the JSON is incorrect
         */
        fireReceiver.onReceive(ApplicationProvider.getApplicationContext(), new Intent(
                LocalePluginIntent.ACTION_FIRE_SETTING).putExtra(
                LocalePluginIntent.EXTRA_BUNDLE, bundle));
    }

    @SmallTest
    @Test
    public void normal() {
        final BroadcastReceiver fireReceiver = new FireReceiver();

        final JSONObject json = PluginJsonValues
                .generateJson(ApplicationProvider.getApplicationContext(), "test_message"); //$NON-NLS-1$

        final Bundle bundle = new Bundle();
        bundle.putString(LocalePluginIntent.EXTRA_STRING_JSON, json.toString());

        fireReceiver.onReceive(ApplicationProvider.getApplicationContext(), new Intent(
                LocalePluginIntent.ACTION_FIRE_SETTING).putExtra(
                LocalePluginIntent.EXTRA_BUNDLE, bundle));
    }
}
