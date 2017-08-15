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


import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.CheckResult;
import androidx.annotation.NonNull;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.filters.MediumTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import android.text.TextUtils;

import com.twofortyfouram.locale.api.LocalePluginIntent;
import com.twofortyfouram.locale.example.setting.toast.R;
import com.twofortyfouram.locale.example.setting.toast.bundle.PluginJsonValues;
import com.twofortyfouram.test.espresso.UiTestPrerequesites;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;


import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withHint;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


@RunWith(AndroidJUnit4.class)
public final class EditActivityIntegrationTest extends UiTestPrerequesites {

    @Rule
    public final ActivityTestRule<EditActivity> mActivityRule = new ActivityTestRule<>(
            EditActivity.class, false, false);

    @MediumTest
    @Test
    public void missingBreadcrumb() {
        final Intent startIntent = new Intent(LocalePluginIntent.ACTION_EDIT_SETTING);
        final Activity activity = mActivityRule.launchActivity(startIntent);

        try {
            mActivityRule.runOnUiThread(() -> {
                assertThat(activity.getTitle(),
                        is(ApplicationProvider.getApplicationContext().getString(R.string.plugin_name)));
            });
        } catch (Throwable e) {
            fail(e.getMessage());
        }
    }

    @Test
    @MediumTest
    public void newSettingCancel() {
        final Intent startIntent = new Intent(LocalePluginIntent.ACTION_EDIT_SETTING)
                .putExtra(LocalePluginIntent.EXTRA_STRING_BREADCRUMB,
                        "Locale > Edit Situation"); //$NON-NLS-1$
        final Activity activity = mActivityRule.launchActivity(startIntent);

        checkTextMessage(""); //$NON-NLS-1$
        checkTextHint(activity.getString(R.string.message_hint));

        activity.finish();

        assertActivityResultCanceled(activity);
    }

    @MediumTest
    @Test
    public void newSettingSave() {
        final Intent startIntent = new Intent(LocalePluginIntent.ACTION_EDIT_SETTING)
                .putExtra(LocalePluginIntent.EXTRA_STRING_BREADCRUMB,
                        "Locale > Edit Situation"); //$NON-NLS-1$
        final Activity activity = mActivityRule.launchActivity(startIntent);

        checkTextMessage(""); //$NON-NLS-1$
        checkTextHint(activity.getString(R.string.message_hint));

        setTextMessage(EditActivity.class.getSimpleName());

        assertActivityResult(activity, EditActivity.class.getSimpleName());
    }

    @MediumTest
    @Test
    public void oldSetting() {
        final Bundle bundle = generateBundle(ApplicationProvider.getApplicationContext(),
                "I am a toast message!"); //$NON-NLS-1$

        final Intent startIntent = new Intent(LocalePluginIntent.ACTION_EDIT_SETTING)
                .putExtra(LocalePluginIntent.EXTRA_STRING_BREADCRUMB,
                        "Locale > Edit Situation")  //$NON-NLS-1$
                .putExtra(LocalePluginIntent.EXTRA_STRING_BLURB,
                        "I am a toast message!")  //$NON-NLS-1$
                .putExtra(LocalePluginIntent.EXTRA_BUNDLE, bundle);

        final EditActivity activity = mActivityRule.launchActivity(startIntent);

        checkTextMessage("I am a toast message!"); //$NON-NLS-1$

        assertActivityResultCanceled(activity);
    }

    /**
     * Verifies the Activity properly handles a bundle with a bad value embedded in it.
     */
    @MediumTest
    @Test
    public void badBundle() {
        final Bundle bundle = generateBundle(ApplicationProvider.getApplicationContext(),
                "I am a toast message!"); //$NON-NLS-1$
        bundle.putString(PluginJsonValues.STRING_MESSAGE, null);

        final Intent startIntent = new Intent(LocalePluginIntent.ACTION_EDIT_SETTING)
                .putExtra(LocalePluginIntent.EXTRA_STRING_BREADCRUMB,
                        "Locale > Edit Situation") //$NON-NLS-1$
                .putExtra(LocalePluginIntent.EXTRA_BUNDLE,
                        bundle);

        final Activity activity = mActivityRule.launchActivity(startIntent);

        checkTextMessage(""); //$NON-NLS-1$
        checkTextHint(activity.getString(R.string.message_hint));

        assertActivityResultCanceled(activity);
    }

    /**
     * Asserts provided hint is what the UI shows.
     *
     * @param hint Hint to assert equals the EditText.
     */
    private void checkTextHint(@NonNull final String hint) {
        assertThat(hint, notNullValue());

        onView(withId(android.R.id.text1)).check(matches(withHint(hint)));
    }

    /**
     * Asserts provided message is what the UI shows.
     *
     * @param message Message to assert equals the EditText.
     */
    private void checkTextMessage(@NonNull final String message) {
        assertThat(message, notNullValue());

        onView(withId(android.R.id.text1)).check(matches(withText(message)));
    }

    /**
     * Sets the message.
     *
     * @param message The message to set.
     */
    private void setTextMessage(@NonNull final String message) {
        assertThat(message, notNullValue());

        onView(withId(android.R.id.text1)).perform(typeText(message));
    }

    private void assertActivityResultCanceled(@NonNull final Activity activity) {
        activity.finish();

        assertThat(mActivityRule.getActivityResult().getResultCode(), is(Activity.RESULT_CANCELED));
    }

    /**
     * Asserts the Activity result contains the expected values for the given display state.
     *
     * @param message The message the plug-in is supposed to show.
     */
    private void assertActivityResult(@NonNull final Activity activity,
                                      @NonNull final String message) {
        assertThat(activity, notNullValue());
        assertThat(message, notNullValue());

        activity.finish();

        final Instrumentation.ActivityResult result = mActivityRule.getActivityResult();
        assertThat(mActivityRule.getActivityResult().getResultCode(), is(Activity.RESULT_OK));

        final Intent resultData = result.getResultData();
        assertThat(resultData, notNullValue());

        final Bundle extras = resultData.getExtras();
        assertThat(extras, notNullValue());

        assertEquals(String.format(
                "Extras should only contain %s and %s but actually contain %s",
                LocalePluginIntent.EXTRA_BUNDLE,
                LocalePluginIntent.EXTRA_STRING_BLURB,
                extras.keySet()), 2, extras.keySet() //$NON-NLS-1$
                .size());

        assertFalse(
                TextUtils.isEmpty(extras.getString(LocalePluginIntent.EXTRA_STRING_BLURB)));

        final Bundle pluginBundle = extras.getBundle(LocalePluginIntent.EXTRA_BUNDLE);
        assertThat(pluginBundle, notNullValue());

        final JSONObject pluginJson;
        try {
            pluginJson = new JSONObject(
                    pluginBundle.getString(LocalePluginIntent.EXTRA_STRING_JSON));
        } catch (final JSONException e) {
            throw new RuntimeException(e);
        }

        assertTrue(PluginJsonValues.isJsonValid(pluginJson));
        assertThat(message, is(PluginJsonValues.getMessage(pluginJson)));
    }

    @NonNull
    @CheckResult
    private static Bundle generateBundle(@NonNull final Context context,
                                         @NonNull final String value) {
        assertThat(context, notNullValue());
        assertThat(value, notNullValue());

        final Bundle result = new Bundle();
        result.putString(LocalePluginIntent.EXTRA_STRING_JSON,
                PluginJsonValues.generateJson(context, value).toString());

        return result;
    }

}
