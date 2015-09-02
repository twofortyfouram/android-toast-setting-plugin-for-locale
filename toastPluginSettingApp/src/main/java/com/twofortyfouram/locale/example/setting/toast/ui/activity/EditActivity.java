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

package com.twofortyfouram.locale.example.setting.toast.ui.activity;

import com.twofortyfouram.locale.example.setting.toast.R;
import com.twofortyfouram.locale.example.setting.toast.bundle.PluginBundleValues;
import com.twofortyfouram.locale.sdk.client.ui.activity.AbstractLocalePluginActivity;

import net.jcip.annotations.NotThreadSafe;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.EditText;

@NotThreadSafe
public final class EditActivity extends AbstractLocalePluginActivity {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);
    }

    @Override
    public void onPostCreateWithPreviousResult(@NonNull final Bundle previousBundle,
            @NonNull final String previousBlurb) {
        final String message = PluginBundleValues.getMessage(previousBundle);
        ((EditText) findViewById(android.R.id.text1)).setText(message);
    }

    @Override
    public boolean isBundleValid(@NonNull final Bundle bundle) {
        return PluginBundleValues.isBundleValid(bundle);
    }

    @Override
    public Bundle getResultBundle() {
        Bundle result = null;

        final String message = ((EditText) findViewById(android.R.id.text1)).getText().toString();
        if (0 < message.length()) {
            result = PluginBundleValues.generateBundle(getApplicationContext(), message);
        }

        return result;
    }

    @NonNull
    @Override
    public String getResultBlurb(@NonNull final Bundle bundle) {
        final String message = PluginBundleValues.getMessage(bundle);

        final int maxBlurbLength = getResources().getInteger(
                R.integer.com_twofortyfouram_locale_sdk_client_maximum_blurb_length);

        if (message.length() > maxBlurbLength) {
            return message.substring(0, maxBlurbLength);
        }

        return message;
    }
}
