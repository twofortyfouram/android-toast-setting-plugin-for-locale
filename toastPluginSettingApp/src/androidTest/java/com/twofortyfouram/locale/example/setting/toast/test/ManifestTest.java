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

package com.twofortyfouram.locale.example.setting.toast.test;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import androidx.annotation.NonNull;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.filters.SmallTest;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertTrue;

import com.twofortyfouram.locale.api.LocalePluginIntent;

/**
 * Tests to verify proper entries in the plug-in's Android Manifest.
 */
@RunWith(AndroidJUnit4.class)
public final class ManifestTest {

    @SmallTest
    @Test
    public void applicationEnabled() {
        assertThat(ApplicationProvider.getApplicationContext().getApplicationInfo().enabled, is(true));
    }

    @SmallTest
    @Test
    public void pluginActivityPresent() {
        final List<ResolveInfo> activities = getPluginActivities(ApplicationProvider.getApplicationContext());
        assertThat(activities, not(empty()));

        for (final ResolveInfo x : activities) {
            assertTrue(x.activityInfo.enabled);
            assertTrue(x.activityInfo.exported);

            /*
             * Verify that the plug-in doesn't request permissions not available to the host
             */
            assertThat(x.activityInfo.permission, nullValue());

            /*
             * Verify that the plug-in has a label attribute in the AndroidManifest
             */
            assertThat(x.activityInfo.labelRes, not(0));

            /*
             * Verify that the plug-in has a icon attribute in the AndroidManifest
             */
            assertThat(x.activityInfo.icon, not(0));
        }
    }

    @SmallTest
    @Test
    public void pluginReceiver() {
        final List<ResolveInfo> receivers = getPluginReceivers(ApplicationProvider.getApplicationContext());

        assertThat(receivers, hasSize(1));

        for (final ResolveInfo x : receivers) {
            assertTrue(x.activityInfo.enabled);
            assertTrue(x.activityInfo.exported);

            /*
             * Verify that the plug-in doesn't request permissions not available to the host
             */
            assertThat(x.activityInfo.permission, nullValue());
        }
    }

    /**
     * Gets a list of all Activities in {@code context}'s package that export
     * {@link LocalePluginIntent#ACTION_EDIT_SETTING}.
     *
     * @param context Application context.
     */
    @NonNull
    private static List<ResolveInfo> getPluginActivities(@NonNull final Context context) {

        final String packageName = context.getPackageName();

        final List<ResolveInfo> result = new LinkedList<>();

        for (final ResolveInfo x : context.getPackageManager().queryIntentActivities(
                new Intent(LocalePluginIntent.ACTION_EDIT_SETTING),
                0)) {
            if (packageName.equals(x.activityInfo.packageName)) {
                result.add(x);
            }
        }

        return result;
    }

    /**
     * Gets a list of all BroadcastReceivers in {@code context}'s package that export
     * {@link LocalePluginIntent#ACTION_FIRE_SETTING ACTION_FIRE_SETTING}.
     *
     * @param context Application context.
     */
    @NonNull
    private static List<ResolveInfo> getPluginReceivers(@NonNull final Context context) {
        final String packageName = context.getPackageName();

        final List<ResolveInfo> result = new LinkedList<>();

        for (final ResolveInfo x : context.getPackageManager().queryBroadcastReceivers(
                new Intent(LocalePluginIntent.ACTION_FIRE_SETTING),
                0)) {
            if (packageName.equals(x.activityInfo.packageName)) {
                result.add(x);
            }

        }

        return result;
    }
}
