package com.markadamson.taskerplugin.homeassistant;

import android.support.annotation.NonNull;

public class Constants {
    @NonNull
    public static final String BUNDLE_EXTRA_BUNDLE_TYPE
            = "com.markadamson.taskerplugin.homeassistant.extra.BUNDLE_TYPE"; //$NON-NLS-1$

    public static final int BUNDLE_CALL_SERVICE = 0;
    public static final int BUNDLE_GET_STATE = 1;
    public static final int BUNDLE_RENDER_TEMPLATE = 2;
}
