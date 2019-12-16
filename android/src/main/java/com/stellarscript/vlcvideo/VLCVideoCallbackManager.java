package com.stellarscript.vlcvideo;

import android.content.Intent;
import android.view.KeyEvent;

import java.util.HashSet;
import java.util.Set;

public class VLCVideoCallbackManager {

    interface IntentCallback {
        boolean onNewIntent(final Intent intent);
    }

    interface OnKeyDownCallback {
        boolean onKeyDown(final int keyCode, final KeyEvent keyEvent)
    }

    private Set<IntentCallback> intentCallbacks;
    private Set<OnKeyDownCallback> onKeyDownCallbacks;

    public VLCVideoCallbackManager() {
        intentCallbacks = new HashSet<>();
        onKeyDownCallbacks = new HashSet<>();
    }

    public boolean onNewIntent(final Intent intent) {
        boolean handled = false;
        for (final IntentCallback callback : intentCallbacks) {
            if (callback.onNewIntent(intent)) {
                handled = true;
            }
        }

        return handled;
    }

    public boolean onKeyDown(final int keyCode, final KeyEvent keyEvent) {
        boolean handled = false;
        for (final OnKeyDownCallback callback : onKeyDownCallbacks) {
            if (callback.onKeyDown(keyCode, keyEvent)) {
                handled = true;
            }
        }

        return handled;
    }

    void addCallback(final IntentCallback callback) {
        intentCallbacks.add(callback);
    }

    void removeCallback(final IntentCallback callback) {
        intentCallbacks.remove(callback);
    }

    void addCallback(final OnKeyDownCallback callback) {
        onKeyDownCallbacks.add(callback);
    }

    void removeCallback(final OnKeyDownCallback callback) {
        onKeyDownCallbacks.remove(callback);
    }
}