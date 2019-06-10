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

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.os.Bundle;
import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.SmallTest;

/**
 * Tests the {@link FireReceiver}.
 */
public final class FireReceiverTest extends AndroidTestCase {
    /*
     * These test cases perform sanity checks. These tests are not very extensive and additional
     * testing is required to verify the BroadcastReceiver works correctly. For example, a human
     * would need to manually verify that a Toast message appears when a correct Intent is sent to
     * the receiver. Depending on what your setting implements, you may be able to verify more
     * easily that the setting triggered the desired result via unit tests than this sample setting
     * can.
     */

    @SmallTest
    public void testNullMessage() {
        final BroadcastReceiver fireReceiver = new FireReceiver();

        final Bundle bundle = PluginBundleValues
                .generateBundle(getContext(), "test_message"); //$NON-NLS-1$
        bundle.putString(PluginBundleValues.BUNDLE_EXTRA_STRING_MESSAGE, null);

        /*
         * The receiver shouldn't crash if the EXTRA_BUNDLE is incorrect
         */
        fireReceiver.onReceive(getContext(), new Intent(
                com.twofortyfouram.locale.api.Intent.ACTION_FIRE_SETTING).putExtra(
                com.twofortyfouram.locale.api.Intent.EXTRA_BUNDLE, bundle));
    }

    @SmallTest
    public void testNormal() {
        final BroadcastReceiver fireReceiver = new FireReceiver();

        final Bundle bundle = PluginBundleValues
                .generateBundle(getContext(), "test_message"); //$NON-NLS-1$

        fireReceiver.onReceive(getContext(), new Intent(
                com.twofortyfouram.locale.api.Intent.ACTION_FIRE_SETTING).putExtra(
                com.twofortyfouram.locale.api.Intent.EXTRA_BUNDLE, bundle));
    }
}
