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

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.test.ActivityInstrumentationTestCase2;
import android.test.UiThreadTest;
import android.test.suitebuilder.annotation.MediumTest;
import android.text.TextUtils;
import android.widget.EditText;

import com.twofortyfouram.locale.example.setting.toast.R;
import com.twofortyfouram.locale.example.setting.toast.bundle.PluginBundleValues;
import com.twofortyfouram.test.ui.activity.ActivityTestUtil;

/**
 * Tests the {@link EditActivity}.
 */
public final class EditActivityTest extends ActivityInstrumentationTestCase2<EditActivity> {

    /**
     * Context of the target application. This is initialized in {@link #setUp()}.
     */
    private Context mTargetContext;

    /**
     * Instrumentation for the test. This is initialized in {@link #setUp()}.
     */
    private Instrumentation mInstrumentation;

    /**
     * Constructor for the test class; required by Android.
     */
    public EditActivityTest() {
        super(EditActivity.class);
    }

    /**
     * Setup that executes before every test case
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();

        mInstrumentation = getInstrumentation();
        mTargetContext = mInstrumentation.getTargetContext();

        /*
         * Perform test case specific initialization. This is required to be set up here because
         * setActivityIntent has no effect inside a method annotated with @UiThreadTest
         */
        if ("testNewSettingCancel".equals(getName())) { //$NON-NLS-1$
            setActivityIntent(new Intent(com.twofortyfouram.locale.api.Intent.ACTION_EDIT_SETTING)
                    .putExtra(com.twofortyfouram.locale.api.Intent.EXTRA_STRING_BREADCRUMB,
                            "Locale > Edit Situation")); //$NON-NLS-1$
        } else if ("testNewSettingSave".equals(getName())) { //$NON-NLS-1$
            setActivityIntent(new Intent(com.twofortyfouram.locale.api.Intent.ACTION_EDIT_SETTING)
                    .putExtra(com.twofortyfouram.locale.api.Intent.EXTRA_STRING_BREADCRUMB,
                            "Locale > Edit Situation")); //$NON-NLS-1$
        } else if ("testOldSetting".equals(getName())) {  //$NON-NLS-1$
            final Bundle bundle = PluginBundleValues.generateBundle(mTargetContext,
                    "I am a toast message!"); //$NON-NLS-1$

            setActivityIntent(new Intent(com.twofortyfouram.locale.api.Intent.ACTION_EDIT_SETTING)
                    .putExtra(com.twofortyfouram.locale.api.Intent.EXTRA_STRING_BREADCRUMB,
                            "Locale > Edit Situation")
                    .putExtra(com.twofortyfouram.locale.api.Intent.EXTRA_STRING_BLURB,
                            "I am a toast message!")
                    .putExtra(com.twofortyfouram.locale.api.Intent.EXTRA_BUNDLE,
                            bundle)); //$NON-NLS-1$
        } else if ("testBadBundle".equals(getName())) {  //$NON-NLS-1$
            final Bundle bundle = PluginBundleValues.generateBundle(mTargetContext,
                    "I am a toast message!"); //$NON-NLS-1$
            bundle.putString(PluginBundleValues.BUNDLE_EXTRA_STRING_MESSAGE, null);

            setActivityIntent(new Intent(com.twofortyfouram.locale.api.Intent.ACTION_EDIT_SETTING)
                    .putExtra(com.twofortyfouram.locale.api.Intent.EXTRA_STRING_BREADCRUMB,
                            "Locale > Edit Situation")
                    .putExtra(com.twofortyfouram.locale.api.Intent.EXTRA_BUNDLE,
                            bundle)); //$NON-NLS-1$
        }
    }

    @MediumTest
    @UiThreadTest
    public void testNewSettingCancel() throws Throwable {
        final Activity activity = getActivity();

        assertMessageAutoSync(""); //$NON-NLS-1$
        assertHintAutoSync(mTargetContext.getString(R.string.message_hint));

        activity.finish();

        assertEquals(Activity.RESULT_CANCELED, ActivityTestUtil.getActivityResultCode(activity));
    }

    @MediumTest
    @UiThreadTest
    public void testNewSettingSave() throws Throwable {
        final Activity activity = getActivity();

        assertMessageAutoSync(""); //$NON-NLS-1$
        assertHintAutoSync(mTargetContext.getString(R.string.message_hint));

        setMessageAutoSync(getName());

        activity.finish();

        assertActivityResultAutoSync(getName());
    }

    @MediumTest
    @UiThreadTest
    public void testOldSetting() throws Throwable {
        final Activity activity = getActivity();

        /*
         * It is necessary to call this manually; the test case won't call
         * onPostCreate() for us :-(
         */
        getActivity().onPostCreateWithPreviousResult(
                getActivity().getIntent().getBundleExtra(
                        com.twofortyfouram.locale.api.Intent.EXTRA_BUNDLE),
                getActivity().getIntent().getStringExtra(
                        com.twofortyfouram.locale.api.Intent.EXTRA_STRING_BLURB));

        assertMessageAutoSync("I am a toast message!"); //$NON-NLS-1$

        activity.finish();

        assertEquals(Activity.RESULT_CANCELED, ActivityTestUtil.getActivityResultCode
                (activity));
    }

    /**
     * Verifies the Activity properly handles a bundle with a bad value embedded in it.
     */
    @MediumTest
    @UiThreadTest
    public void testBadBundle() throws Throwable {
        final Activity activity = getActivity();

        assertMessageAutoSync(""); //$NON-NLS-1$
        assertHintAutoSync(mTargetContext.getString(R.string.message_hint));

        activity.finish();
        assertEquals(Activity.RESULT_CANCELED, ActivityTestUtil.getActivityResultCode
                (activity));
    }

    /**
     * Asserts the Activity result contains the expected values for the given display state.
     *
     * @param message The message the plug-in is supposed to show.
     */
    private void assertActivityResultAutoSync(final String message) throws Throwable {
        final Activity activity = getActivity();

        final Runnable runnable = new Runnable() {
            public void run() {
                activity.finish();

                assertEquals(Activity.RESULT_OK, ActivityTestUtil.getActivityResultCode
                        (activity));

                final Intent result = ActivityTestUtil.getActivityResultData
                        (activity);
                assertNotNull(result);

                final Bundle extras = result.getExtras();
                assertNotNull(extras);
                assertEquals(
                        String.format(
                                "Extras should only contain %s and %s but actually contain %s",
                                com.twofortyfouram.locale.api.Intent.EXTRA_BUNDLE,
                                com.twofortyfouram.locale.api.Intent.EXTRA_STRING_BLURB,
                                extras.keySet()), 2, extras.keySet() //$NON-NLS-1$
                                .size());

                assertFalse(TextUtils.isEmpty(extras
                        .getString(com.twofortyfouram.locale.api.Intent.EXTRA_STRING_BLURB)));

                final Bundle pluginBundle = extras
                        .getBundle(com.twofortyfouram.locale.api.Intent.EXTRA_BUNDLE);
                assertNotNull(pluginBundle);

                assertTrue(PluginBundleValues.isBundleValid(pluginBundle));
                assertEquals(message,
                        pluginBundle.getString(PluginBundleValues.BUNDLE_EXTRA_STRING_MESSAGE));
            }
        };

        autoSyncRunnable(runnable);
    }

    /**
     * Asserts provided message is what the UI shows.
     *
     * @param message Message to assert equals the EditText.
     */
    private void assertMessageAutoSync(final String message) throws Throwable {
        final Runnable runnable = new Runnable() {
            private final Activity mActivity = getActivity();

            public void run() {
                assertEquals(message, ((EditText) mActivity.findViewById(android.R.id.text1))
                        .getText().toString());
            }
        };

        autoSyncRunnable(runnable);
    }

    /**
     * Asserts provided hint is what the UI shows.
     *
     * @param hint Hint to assert equals the EditText.
     */
    private void assertHintAutoSync(final String hint) throws Throwable {
        final Runnable runnable = new Runnable() {
            private final Activity mActivity = getActivity();

            public void run() {
                assertEquals(hint, ((EditText) mActivity.findViewById(android.R.id.text1))
                        .getHint().toString());
            }
        };

        autoSyncRunnable(runnable);
    }

    /**
     * Sets the message.
     *
     * @param message The message to set.
     */
    private void setMessageAutoSync(final String message) throws Throwable {
        final Runnable runnable = new Runnable() {
            private final Activity mActivity = getActivity();

            public void run() {
                final EditText editText = (EditText) mActivity.findViewById(android.R.id.text1);

                editText.setText(message);
            }
        };

        autoSyncRunnable(runnable);
    }

    /**
     * Executes a runnable on the main thread. This method works even if the current thread is
     * already the main thread.
     *
     * @param runnable to execute.
     */
    protected final void autoSyncRunnable(final Runnable runnable) {
        //noinspection ObjectEquality
        if (Looper.getMainLooper() == Looper.myLooper()) {
            runnable.run();
        } else {
            getInstrumentation().runOnMainSync(runnable);
            getInstrumentation().waitForIdleSync();
        }
    }
}
