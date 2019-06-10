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

package com.markadamson.taskerplugin.homeassistant.setting.toast.test;

import com.twofortyfouram.annotation.Slow;
import com.twofortyfouram.annotation.Slow.Speed;
import com.twofortyfouram.annotation.VisibleForTesting;
import com.twofortyfouram.annotation.VisibleForTesting.Visibility;
import com.twofortyfouram.assertion.Assertions;

import net.jcip.annotations.ThreadSafe;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.XmlResourceParser;
import android.support.annotation.NonNull;

import java.io.IOException;

/**
 * Represents the Android Manifest's possible states for install location.
 */
@ThreadSafe
public enum InstallLocation {
    /**
     * The application permits installation to either internal or external
     * storage, with Android automatically deciding.
     */
    auto,

    /**
     * The application can only be installed to internal storage.
     */
    internalOnly,

    /**
     * The application permits installation to either internal or external
     * storage, with preference for external storage.
     */
    preferExternal,

    /**
     * No install location was specified in the Android Manifest. In terms of
     * how Android interprets this, it is basically the same as
     * {@link #internalOnly}.
     */
    MISSING,

    /**
     * An unknown install location, such as a new install location added in
     * newer versions of Android.
     */
    UNKNOWN;

    /**
     * The Android Manifest int value for auto install location.
     */
    /*
     * Note: This value is a private API in Android and could change without
     * warning
     */
    @VisibleForTesting(Visibility.PRIVATE)
    /* package */ static final int MANIFEST_INSTALL_LOCATION_AUTO = 0;

    /**
     * The Android Manifest int value for internal only install location.
     */
    /*
     * Note: This value is a private API in Android and could change without
     * warning
     */
    @VisibleForTesting(Visibility.PRIVATE)
    /* package */ static final int MANIFEST_INSTALL_LOCATION_INTERNAL_ONLY = 1;

    /**
     * The Android Manifest int value for internal or external storage, with
     * preference for external storage.
     */
    /*
     * Note: This value is a private API in Android and could change without
     * warning
     */
    @VisibleForTesting(Visibility.PRIVATE)
    /* package */ static final int MANIFEST_INSTALL_LOCATION_PREFER_EXTERNAL = 2;

    /**
     * Takes the integer value of install location from the Android Manifest and
     * converts it to an enum value.
     *
     * @param location one of the Android Manifest install locations.
     * @return The enum type for the install location.
     */
    @NonNull
    /* package */ static InstallLocation getInstallLocation(final int location) {
        switch (location) {
            case MANIFEST_INSTALL_LOCATION_AUTO: {
                return auto;
            }
            case MANIFEST_INSTALL_LOCATION_INTERNAL_ONLY: {
                return internalOnly;
            }
            case MANIFEST_INSTALL_LOCATION_PREFER_EXTERNAL: {
                return preferExternal;
            }
            default: {
                return UNKNOWN;
            }
        }
    }

    /**
     * Gets a package's install location, as per the Android Manifest.
     *
     * @param context     Application context.
     * @param packageName Package whose install location is to be checked.
     * @return the install location.
     * @throws NameNotFoundException  if {@code packageName} isn't installed.
     * @throws XmlPullParserException If the target package's manifest couldn't
     *                                be parsed.
     * @throws IOException            If an error occurred reading the target package.
     */
    @NonNull
    @Slow(Speed.MILLISECONDS)
    public static InstallLocation getManifestInstallLocation(@NonNull final Context context,
            @NonNull final String packageName) throws NameNotFoundException,
            XmlPullParserException, IOException {
        Assertions.assertNotNull(context, "context"); //$NON-NLS-1$
        Assertions.assertNotNull(packageName, "packageName"); //$NON-NLS-1$

        /*
         * There isn't a public API to check the installLocation of an APK, so
         * this is a hacky implementation to read the value directly from the
         * package's AndroidManifest.
         */
        final XmlResourceParser xml = context
                .createPackageContext(packageName, Context.CONTEXT_RESTRICTED).getAssets()
                .openXmlResourceParser("AndroidManifest.xml"); //$NON-NLS-1$
        try {
            for (int eventType = xml.getEventType(); XmlPullParser.END_DOCUMENT != eventType;
                    eventType = xml
                            .nextToken()) {
                switch (eventType) {
                    case XmlPullParser.START_TAG: {
                        if (xml.getName().matches("manifest")) { //$NON-NLS-1$
                            for (int x = 0; x < xml.getAttributeCount(); x++) {
                                if (xml.getAttributeName(x)
                                        .matches("installLocation")) { //$NON-NLS-1$
                                    return InstallLocation.getInstallLocation(Integer.parseInt(xml
                                            .getAttributeValue(x)));
                                }
                            }
                        }

                        break;
                    }
                }
            }

            /*
             * Once this point is reached, it can be assumed the installLocation
             * didn't exist in the AndroidManifest
             */
            return InstallLocation.MISSING;
        } finally {
            xml.close();
        }
    }
}
