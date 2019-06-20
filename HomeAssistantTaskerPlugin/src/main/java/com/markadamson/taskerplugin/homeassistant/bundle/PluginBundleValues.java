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

package com.markadamson.taskerplugin.homeassistant.bundle;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.markadamson.taskerplugin.homeassistant.Constants;
import com.markadamson.taskerplugin.homeassistant.TaskerPlugin;
import com.twofortyfouram.assertion.BundleAssertions;
import com.twofortyfouram.log.Lumberjack;
import com.twofortyfouram.spackle.AppBuildInfo;

import net.jcip.annotations.ThreadSafe;

import java.util.UUID;

import static com.twofortyfouram.assertion.Assertions.assertNotEmpty;
import static com.twofortyfouram.assertion.Assertions.assertNotNull;

/**
 * Manages the {@link com.twofortyfouram.locale.api.Intent#EXTRA_BUNDLE EXTRA_BUNDLE} for this
 * plug-in.
 */
@ThreadSafe
public final class PluginBundleValues {

    /**
     * Type: {@code String}.
     * <p>
     * Server UUID as string.
     */
    @NonNull
    public static final String BUNDLE_EXTRA_STRING_SERVER
            = "com.markadamson.taskerplugin.homeassistant.extra.STRING_SERVER"; //$NON-NLS-1$

    /**
     * Type: {@code String}.
     * <p>
     * Domain/service to call.
     */
    @NonNull
    public static final String BUNDLE_EXTRA_STRING_SERVICE
            = "com.markadamson.taskerplugin.homeassistant.extra.STRING_SERVICE"; //$NON-NLS-1$

    /**
     * Type: {@code String}.
     * <p>
     * Service data (JSON, optional).
     */
    @NonNull
    public static final String BUNDLE_EXTRA_STRING_DATA
            = "com.markadamson.taskerplugin.homeassistant.extra.STRING_DATA"; //$NON-NLS-1$

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
    public static final String BUNDLE_EXTRA_INT_VERSION_CODE
            = "com.markadamson.taskerplugin.homeassistant.extra.INT_VERSION_CODE"; //$NON-NLS-1$

    /**
     * Method to verify the content of the bundle are correct.
     * <p>
     * This method will not mutate {@code bundle}.
     *
     * @param bundle bundle to verify. May be null, which will always return false.
     * @return true if the Bundle is valid, false if the bundle is invalid.
     */
    public static boolean isBundleValid(@Nullable final Bundle bundle) {
        if (null == bundle) {
            return false;
        }

        try {
            BundleAssertions.assertHasString(bundle, BUNDLE_EXTRA_STRING_SERVER, false, false);
            BundleAssertions.assertHasString(bundle, BUNDLE_EXTRA_STRING_SERVICE, false, false);
            BundleAssertions.assertHasString(bundle, BUNDLE_EXTRA_STRING_DATA, false, true);
            BundleAssertions.assertHasInt(bundle, BUNDLE_EXTRA_INT_VERSION_CODE);

            int bundleVer = bundle.getInt(BUNDLE_EXTRA_INT_VERSION_CODE), expectedCount = 4;

            if (bundleVer >= 3) {
                BundleAssertions.assertHasInt(bundle, Constants.BUNDLE_EXTRA_BUNDLE_TYPE,
                        Constants.BUNDLE_CALL_SERVICE, Constants.BUNDLE_CALL_SERVICE);
                expectedCount++;
            }

            // Bundle may now have replacement vars key:
            if (bundleVer >= 5 && TaskerPlugin.Setting.hasVariableReplaceKeys(bundle))
                expectedCount++;

            BundleAssertions.assertKeyCount(bundle, expectedCount);

        } catch (final AssertionError e) {
            Lumberjack.e("Bundle failed verification%s", e); //$NON-NLS-1$
            return false;
        }

        return true;
    }

    /**
     * @param context Application context.
     * @param server The server UUID.
     * @param service The domain/service to call.
     * @param data The service data to send.
     * @return A plug-in bundle.
     */
    @NonNull
    public static Bundle generateBundle(@NonNull final Context context,
                                        @NonNull final UUID server,
                                        @NonNull final String service,
                                        @NonNull final String data) {
        assertNotNull(context, "context"); //$NON-NLS-1$
        assertNotNull(server, "server"); //$NON-NLS-1$
        assertNotEmpty(service, "service"); //$NON-NLS-1$

        final Bundle result = new Bundle();
        result.putInt(BUNDLE_EXTRA_INT_VERSION_CODE, AppBuildInfo.getVersionCode(context));
        result.putInt(Constants.BUNDLE_EXTRA_BUNDLE_TYPE, Constants.BUNDLE_CALL_SERVICE);
        result.putString(BUNDLE_EXTRA_STRING_SERVER, server.toString());
        result.putString(BUNDLE_EXTRA_STRING_SERVICE, service);
        result.putString(BUNDLE_EXTRA_STRING_DATA, data);
        TaskerPlugin.Setting.setVariableReplaceKeys(result, new String[] {BUNDLE_EXTRA_STRING_SERVICE, BUNDLE_EXTRA_STRING_DATA});

        return result;
    }

    /**
     * @param bundle A valid plug-in bundle.
     * @return The message inside the plug-in bundle.
     */
    @NonNull
    public static UUID getServer(@NonNull final Bundle bundle) {
        return UUID.fromString(bundle.getString(BUNDLE_EXTRA_STRING_SERVER));
    }

    @NonNull
    public static String getService(@NonNull final Bundle bundle) {
        return bundle.getString(BUNDLE_EXTRA_STRING_SERVICE);
    }

    @NonNull
    public static String getData(@NonNull final Bundle bundle) {
        return bundle.getString(BUNDLE_EXTRA_STRING_DATA);
    }

    /**
     * Private constructor prevents instantiation
     *
     * @throws UnsupportedOperationException because this class cannot be instantiated.
     */
    private PluginBundleValues() {
        throw new UnsupportedOperationException("This class is non-instantiable"); //$NON-NLS-1$
    }
}
