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

package com.twofortyfouram.locale.example.setting.toast.ui.activity;

import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import com.twofortyfouram.locale.example.setting.toast.R;
import com.twofortyfouram.locale.example.setting.toast.bundle.PluginJsonValues;
import com.twofortyfouram.locale.sdk.client.ui.activity.AbstractAppCompatPluginActivity;
import com.twofortyfouram.log.Lumberjack;

import net.jcip.annotations.NotThreadSafe;

import org.json.JSONObject;

@NotThreadSafe
public final class EditActivity extends AbstractAppCompatPluginActivity {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

        /*
         * To help the user keep context, the title shows the host's name and the subtitle
         * shows the plug-in's name.
         */
        CharSequence callingApplicationLabel = null;
        try {
            callingApplicationLabel =
                    getPackageManager().getApplicationLabel(
                            getPackageManager().getApplicationInfo(getCallingPackage(),
                                    0));
        } catch (final PackageManager.NameNotFoundException e) {
            Lumberjack.e("Calling package couldn't be found%s", e); //$NON-NLS-1$
        }
        if (null != callingApplicationLabel) {
            setTitle(callingApplicationLabel);
        }

        getSupportActionBar().setSubtitle(R.string.plugin_name);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onPostCreateWithPreviousResult(@NonNull final JSONObject previousJson,
            @NonNull final String previousBlurb) {
        final String message = PluginJsonValues.getMessage(previousJson);
        ((EditText) findViewById(android.R.id.text1)).setText(message);
    }

    @Override
    public boolean isJsonValid(@NonNull final JSONObject json) {
        return PluginJsonValues.isJsonValid(json);
    }

    @Nullable
    @Override
    public JSONObject getResultJson() {
        JSONObject result = null;

        final String message = ((EditText) findViewById(android.R.id.text1)).getText().toString();
        if (!TextUtils.isEmpty(message)) {
            result = PluginJsonValues.generateJson(getApplicationContext(), message);
        }

        return result;
    }

    @NonNull
    @Override
    public String getResultBlurb(@NonNull final JSONObject json) {
        final String message = PluginJsonValues.getMessage(json);

        final int maxBlurbLength = getResources().getInteger(
                R.integer.com_twofortyfouram_locale_sdk_client_maximum_blurb_length);

        if (message.length() > maxBlurbLength) {
            return message.substring(0, maxBlurbLength);
        }

        return message;
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (android.R.id.home == item.getItemId()) {
            finish();
        } else if (R.id.menu_discard_changes == item.getItemId()) {
            // Signal to AbstractAppCompatPluginActivity that the user canceled.
            mIsCancelled = true;
            finish();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
