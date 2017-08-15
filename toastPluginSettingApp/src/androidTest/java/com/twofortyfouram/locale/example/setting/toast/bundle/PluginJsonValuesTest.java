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

import androidx.test.core.app.ApplicationProvider;
import androidx.test.filters.SmallTest;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.twofortyfouram.spackle.AppBuildInfo;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;


@RunWith(AndroidJUnit4.class)
public final class PluginJsonValuesTest {

    @SmallTest
    @Test
    public void extraConstants() {
        /*
         * NOTE: This test is expected to fail initially when you are adapting this example to your
         * own plug-in. Once you've settled on constant names for your Intent extras, go ahead and
         * update this test case.
         *
         * The goal of this test case is to prevent accidental renaming of the Intent extras. If the
         * extra is intentionally changed, then this unit test needs to be intentionally updated.
         */
        assertThat(PluginJsonValues.STRING_MESSAGE,
                is("message")); //$NON-NLS-1$
        assertThat(PluginJsonValues.LONG_VERSION_CODE, is("version_code")); //$NON-NLS-1$
    }

    @SmallTest
    @Test
    public void generateJson() {
        final JSONObject json = PluginJsonValues.generateJson(ApplicationProvider.getApplicationContext(), "Foo"); //$NON-NLS-1$
        assertThat(json, notNullValue());

        assertThat(json.length(), is(2));

        assertThat(PluginJsonValues.getMessage(json), is("Foo")); //$NON-NLS-1$
        assertThat(PluginJsonValues.getVersionCode(json),
                is(AppBuildInfo.getVersionCode(ApplicationProvider.getApplicationContext())));
    }

    @SmallTest
    @Test
    public void verifyJson_correct() {
        final JSONObject json = PluginJsonValues.generateJson(ApplicationProvider.getApplicationContext(), "Foo"); //$NON-NLS-1$
        assertThat(PluginJsonValues.isJsonValid(json), is(true));
    }

    @SmallTest
    @Test
    public void verifyJson_null() {
        assertThat(PluginJsonValues.isJsonValid(null), is(false));
    }

    @SmallTest
    @Test
    public void verifyBundle_missing_extra() {
        assertThat(PluginJsonValues.isJsonValid(new JSONObject()), is(false));
    }

    @SmallTest
    @Test
    public void verifyJson_extra_items() throws JSONException {
        final JSONObject json = PluginJsonValues.generateJson(ApplicationProvider.getApplicationContext(), "Foo"); //$NON-NLS-1$

        json.put("bar", 1); //$NON-NLS-1$
        assertThat(PluginJsonValues.isJsonValid(json), is(false));
    }

    @SmallTest
    @Test
    public void verifyJson_null_message() throws JSONException {
        final JSONObject json = PluginJsonValues.generateJson(ApplicationProvider.getApplicationContext(), "Foo"); //$NON-NLS-1$
        json.put(PluginJsonValues.STRING_MESSAGE, JSONObject.NULL);

        assertThat(PluginJsonValues.isJsonValid(json), is(false));
    }

    @SmallTest
    @Test
    public void verifyJson_empty_message() throws JSONException {
        final JSONObject json = PluginJsonValues.generateJson(ApplicationProvider.getApplicationContext(), "Foo"); //$NON-NLS-1$
        json.put(PluginJsonValues.STRING_MESSAGE, "");

        assertThat(PluginJsonValues.isJsonValid(json), is(false));
    }

    @SmallTest
    @Test
    public void verifyJson_wrong_type_version() throws JSONException {
        final JSONObject json = PluginJsonValues.generateJson(ApplicationProvider.getApplicationContext(), "Foo"); //$NON-NLS-1$
        json.put(PluginJsonValues.LONG_VERSION_CODE, "asdf"); //$NON-NLS-1$

        assertThat(PluginJsonValues.isJsonValid(json), is(false));
    }
}
