/*
 * home-assistant-plugin-for-tasker <https://github.com/MarkAdamson/home-assistant-plugin-for-tasker>
 * Copyright 2019 Mark Adamson
 *
 * Original author:
 * android-plugin-client-sdk-for-locale https://github.com/twofortyfouram/android-plugin-client-sdk-for-locale
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

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.os.Build.VERSION_CODES;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Pair;

import com.twofortyfouram.spackle.AndroidSdkVersion;
import com.twofortyfouram.spackle.ThreadUtil;
import com.twofortyfouram.spackle.ThreadUtil.ThreadPriority;

import net.jcip.annotations.ThreadSafe;

import static com.twofortyfouram.assertion.Assertions.assertInRangeInclusive;
import static com.twofortyfouram.assertion.Assertions.assertNotNull;

/**
 * Simplifies asynchronous broadcast handling. Subclasses call
 * {@link #goAsyncWithCallback(AsyncCallback, boolean)}, and the abstract class takes
 * care of executing the callback on a background thread.
 */
@ThreadSafe
/* package */ abstract class AbstractAsyncReceiver extends BroadcastReceiver {

    /*
     * This method is package visible rather than protected so that it will be
     * obfuscated by ProGuard.
     *
     * @param callback Callback to execute on a background thread.
     * @param isOrdered Indicates whether an ordered broadcast is being processed.
     */
    @TargetApi(VERSION_CODES.HONEYCOMB)
    /* package */ final void goAsyncWithCallback(@NonNull final AsyncCallback callback,
                                                 final boolean isOrdered) {
        assertNotNull(callback, "callback"); //$NON-NLS-1$

        final PendingResult pendingResult = goAsync();
        if (null == pendingResult) {
            throw new AssertionError(
                    "PendingResult was null.  Was goAsync() called previously?"); //$NON-NLS-1$
        }

        final Handler.Callback handlerCallback = new AsyncHandlerCallback();
        final HandlerThread thread = ThreadUtil.newHandlerThread(getClass().getName(),
                ThreadPriority.BACKGROUND);
        final Handler handler = new Handler(thread.getLooper(), handlerCallback);

        final Object obj = new Pair<PendingResult, AsyncCallback>(pendingResult, callback);
        final int isOrderedInt = isOrdered ? 1 : 0;
        final Message msg = handler
                .obtainMessage(AsyncHandlerCallback.MESSAGE_HANDLE_CALLBACK, isOrderedInt, 0, obj);

        final boolean isMessageSent = handler.sendMessage(msg);
        if (!isMessageSent) {
            throw new AssertionError();
        }
    }

    @TargetApi(VERSION_CODES.HONEYCOMB)
    private static final class AsyncHandlerCallback implements Handler.Callback {

        /**
         * Message MUST contain a {@code Pair<PendingResult, AsyncCallback>} as the {@code msg.obj}
         * and a boolean encoded in the {@code msg.arg1} to indicate whether the broadcast was
         * ordered.
         */
        public static final int MESSAGE_HANDLE_CALLBACK = 0;

        @Override
        public boolean handleMessage(final Message msg) {
            assertNotNull(msg, "msg"); //$NON-NLS-1$
            switch (msg.what) {
                case MESSAGE_HANDLE_CALLBACK: {
                    assertNotNull(msg.obj, "msg.obj"); //$NON-NLS-1$
                    assertInRangeInclusive(msg.arg1, 0, 1, "msg.arg1");  //$NON-NLS-1$

                    final Pair<PendingResult, AsyncCallback> pair = castObj(msg.obj);
                    final boolean isOrdered = 0 != msg.arg1;

                    final PendingResult pendingResult = pair.first;
                    final AsyncCallback asyncCallback = pair.second;

                    try {
                        final int resultCode = asyncCallback.runAsync();

                        if (isOrdered) {
                            pendingResult.setResultCode(resultCode);
                        }
                    } finally {
                        pendingResult.finish();
                    }

                    quit();

                    break;
                }
            }
            return true;
        }

        @NonNull
        @SuppressWarnings("unchecked")
        private static Pair<PendingResult, AsyncCallback> castObj(@NonNull final Object o) {
            return (Pair<PendingResult, AsyncCallback>) o;
        }

        private static void quit() {
            if (AndroidSdkVersion.isAtLeastSdk(VERSION_CODES.JELLY_BEAN_MR2)) {
                quitJellybeanMr2();
            } else {
                Looper.myLooper().quit();
            }
        }

        @TargetApi(VERSION_CODES.JELLY_BEAN_MR2)
        private static void quitJellybeanMr2() {
            Looper.myLooper().quitSafely();
        }
    }

    /* package */static interface AsyncCallback {

        /**
         * @return The result code to be set if this is an ordered broadcast.
         */
        public int runAsync();
    }
}