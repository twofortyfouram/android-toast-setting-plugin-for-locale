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

package com.twofortyfouram.locale.example.setting.toast.bundle;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;

import com.twofortyfouram.spackle.AppBuildInfo;

import net.jcip.annotations.ThreadSafe;

import org.json.JSONException;
import org.json.JSONObject;

import static com.twofortyfouram.assertion.Assertions.assertNotEmpty;
import static com.twofortyfouram.assertion.Assertions.assertNotNull;

/**
 * Manages the {@link com.twofortyfouram.locale.api.LocalePluginIntent#EXTRA_STRING_JSON EXTRA_STRING_JSON} for this
 * plug-in.
 */
@ThreadSafe
public final class PluginJsonValues {

    /**
     * Type: {@code String}.
     * <p>
     * String message to display in a Toast message.
     */
    @NonNull
    public static final String STRING_MESSAGE = "message"; //$NON-NLS-1$

    /**
     * Type: {@code int}.
     * <p>
     * versionCode of the plug-in that saved the Bundle.
     */
    /*
     * This extra is not strictly required, however it makes backward and forward compatibility
     * significantly easier. For example, suppose a bug is found in how some version of the plug-in
     * stored its Bundle. By having the version, the plug-in can better detect when such bugs occur.
     */
    @NonNull
    /*package*/ static final String LONG_VERSION_CODE = "version_code";//$NON-NLS-1$

    /**
     * Method to verify the content of the JSON is correct.
     * <p>
     * This method will not mutate {@code json}.
     *
     * @param json JSON to verify. May be null, which will always return false.
     * @return true if the JSON is valid, false if the JSON is invalid.
     */
    public static boolean isJsonValid(@Nullable final JSONObject json) {
        if (null == json) {
            return false;
        }

        if (2 != json.length()) {
            return false;
        }

        if (json.isNull(STRING_MESSAGE)) {
            return false;
        }

        String value = null;
        try {
            value = json.getString(STRING_MESSAGE);
        } catch (final JSONException e) {
            return false;
        }

        if (TextUtils.isEmpty(value)) {
            return false;
        }

        if (json.isNull(LONG_VERSION_CODE)) {
            return false;
        }

        int versionCode = 0;
        try {
            versionCode = json.getInt(LONG_VERSION_CODE);
        } catch (final JSONException e) {
            return false;
        }

        return true;
    }

    /**
     * @param context Application context.
     * @param message The toast message to be displayed by the plug-in.
     * @return A plug-in bundle.
     */
    @NonNull
    public static JSONObject generateJson(@NonNull final Context context,
            @NonNull final String message) {
        assertNotNull(context, "context"); //$NON-NLS-1$
        assertNotEmpty(message, "message"); //$NON-NLS-1$

        final JSONObject result = new JSONObject();
        try {
            result.put(LONG_VERSION_CODE, AppBuildInfo.getVersionCode(context));
            result.put(STRING_MESSAGE, message);

            return result;
        } catch (final JSONException e) {
            //A failure creating the JSON object isn't expected.
            throw new RuntimeException(e);
        }
    }

    /**
     * @param json A valid plug-in JSON.
     * @return The message inside the plug-in bundle.
     */
    @NonNull
    public static String getMessage(@NonNull final JSONObject json) {
        try {
            return json.getString(STRING_MESSAGE);
        } catch (final JSONException e) {
            // Users are expected to validate with isValid() first
            throw new RuntimeException(e);
        }
    }

    /**
     * @param json A valid plug-in JSON.
     * @return The versionCode of the APK that created {@code json}.
     */
    public static long getVersionCode(@NonNull final JSONObject json) {
        try {
            return json.getLong(LONG_VERSION_CODE);
        } catch (final JSONException e) {
            // Users are expected to validate with isValid() first
            throw new RuntimeException(e);
        }
    }

    /**
     * Private constructor prevents instantiation
     *
     * @throws UnsupportedOperationException because this class cannot be instantiated.
     */
    private PluginJsonValues() {
        throw new UnsupportedOperationException("This class is non-instantiable"); //$NON-NLS-1$
    }
}
