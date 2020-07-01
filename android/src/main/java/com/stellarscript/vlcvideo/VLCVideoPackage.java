package com.stremio.vlcvideo;

import android.app.Application;
import android.view.View;

import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ViewManager;

import org.videolan.libvlc.LibVLC;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class VLCVideoPackage implements ReactPackage {

    private static final ArrayList<String> DEFAULT_VLC_OPTIONS = new ArrayList<>(Arrays.asList("-vvv", "--http-reconnect"));

    private final View.OnKeyListener mOnKeyListener;
    private final LibVLC mLibVLC;
    private final VLCVideoCallbackManager mCallbackManager;

    public VLCVideoPackage(final Application application) {
        this(application, DEFAULT_VLC_OPTIONS, null, null);
    }

    public VLCVideoPackage(final Application application, final ArrayList<String> libVLCOptions) {
        this(application, libVLCOptions, null, null);
    }

    public VLCVideoPackage(final Application application, final View.OnKeyListener onKeyListener) {
        this(application, DEFAULT_VLC_OPTIONS, onKeyListener, null);
    }

    public VLCVideoPackage(final Application application, final VLCVideoCallbackManager callbackManager) {
        this(application, DEFAULT_VLC_OPTIONS, null, callbackManager);
    }

    public VLCVideoPackage(final Application application, final ArrayList<String> libVLCOptions, final View.OnKeyListener onKeyListener) {
        this(application, libVLCOptions, onKeyListener, null);
    }

    public VLCVideoPackage(final Application application, final ArrayList<String> libVLCOptions, final VLCVideoCallbackManager callbackManager) {
        this(application, libVLCOptions, null, callbackManager);
    }

    public VLCVideoPackage(final Application application, final View.OnKeyListener onKeyListener, final VLCVideoCallbackManager callbackManager) {
        this(application, DEFAULT_VLC_OPTIONS, onKeyListener, callbackManager);
    }

    public VLCVideoPackage(final Application application, final ArrayList<String> libVLCOptions, final View.OnKeyListener onKeyListener, final VLCVideoCallbackManager callbackManager) {
        mLibVLC = new LibVLC(application, libVLCOptions);
        mOnKeyListener = onKeyListener;
        mCallbackManager = callbackManager;
    }

    @Override
    public List<NativeModule> createNativeModules(final ReactApplicationContext reactApplicationContext) {
        return Collections.emptyList();
    }

    @Override
    public List<ViewManager> createViewManagers(final ReactApplicationContext reactApplicationContext) {
        return Arrays.<ViewManager>asList(new VLCVideoViewManager(mOnKeyListener, mLibVLC, mCallbackManager));
    }

}
