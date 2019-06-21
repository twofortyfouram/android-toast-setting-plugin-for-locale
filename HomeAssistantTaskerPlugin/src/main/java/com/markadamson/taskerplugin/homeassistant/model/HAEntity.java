package com.markadamson.taskerplugin.homeassistant.model;

public class HAEntity {
    private String mState, mAttributes;

    public HAEntity(String mState, String mAttributes) {
        this.mState = mState;
        this.mAttributes = mAttributes;
    }

    public String getState() {
        return mState;
    }

    public String getAttributes() {
        return mAttributes;
    }
}
