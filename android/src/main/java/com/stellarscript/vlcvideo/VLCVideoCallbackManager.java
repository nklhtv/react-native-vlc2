package com.stellarscript.vlcvideo;

import android.content.Intent;

import java.util.HashSet;
import java.util.Set;

public class VLCVideoCallbackManager {

    interface IntentCallback {
        boolean onNewIntent(final Intent intent);
    }

    private Set<IntentCallback> callbacks;

    public VLCVideoCallbackManager() {
        callbacks = new HashSet<>();
    }

    public boolean onNewIntent(final Intent intent) {
        boolean handled = false;
        for (final IntentCallback callback : callbacks) {
            if (callback.onNewIntent(intent)) {
                handled = true;
            }
        }

        return handled;
    }

    void addCallback(final IntentCallback callback) {
        callbacks.add(callback);
    }

    void removeCallback(final IntentCallback callback) {
        callbacks.remove(callback);
    }
}
