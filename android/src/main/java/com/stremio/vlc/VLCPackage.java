package com.stremio.vlc;

import android.app.Application;
import android.view.View;

import androidx.databinding.ObservableField;

import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ViewManager;
import com.stremio.vlc.casting.VLCCastingModule;
import com.stremio.vlc.video.VLCVideoCallbackManager;
import com.stremio.vlc.video.VLCVideoViewManager;

import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.RendererItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class VLCPackage implements ReactPackage {

    private static final ArrayList<String> DEFAULT_VLC_OPTIONS = new ArrayList<>(Arrays.asList("-vvv", "--http-reconnect"));

    private final View.OnKeyListener mOnKeyListener;
    private final LibVLC mLibVLC;
    private final VLCVideoCallbackManager mCallbackManager;
    private final ObservableField<RendererItem> mSelectedRenderer = new ObservableField<>(null);

    public VLCPackage(final Application application) {
        this(application, DEFAULT_VLC_OPTIONS, null, null);
    }

    public VLCPackage(final Application application, final ArrayList<String> libVLCOptions) {
        this(application, libVLCOptions, null, null);
    }

    public VLCPackage(final Application application, final View.OnKeyListener onKeyListener) {
        this(application, DEFAULT_VLC_OPTIONS, onKeyListener, null);
    }

    public VLCPackage(final Application application, final VLCVideoCallbackManager callbackManager) {
        this(application, DEFAULT_VLC_OPTIONS, null, callbackManager);
    }

    public VLCPackage(final Application application, final ArrayList<String> libVLCOptions, final View.OnKeyListener onKeyListener) {
        this(application, libVLCOptions, onKeyListener, null);
    }

    public VLCPackage(final Application application, final ArrayList<String> libVLCOptions, final VLCVideoCallbackManager callbackManager) {
        this(application, libVLCOptions, null, callbackManager);
    }

    public VLCPackage(final Application application, final View.OnKeyListener onKeyListener, final VLCVideoCallbackManager callbackManager) {
        this(application, DEFAULT_VLC_OPTIONS, onKeyListener, callbackManager);
    }

    public VLCPackage(final Application application, final ArrayList<String> libVLCOptions, final View.OnKeyListener onKeyListener, final VLCVideoCallbackManager callbackManager) {
        mLibVLC = new LibVLC(application, libVLCOptions);
        mOnKeyListener = onKeyListener;
        mCallbackManager = callbackManager;
    }

    @Override
    public List<NativeModule> createNativeModules(final ReactApplicationContext reactApplicationContext) {
        return Arrays.<NativeModule>asList(new VLCCastingModule(reactApplicationContext, mLibVLC, mSelectedRenderer));
    }

    @Override
    public List<ViewManager> createViewManagers(final ReactApplicationContext reactApplicationContext) {
        return Arrays.<ViewManager>asList(new VLCVideoViewManager(mOnKeyListener, mLibVLC, mCallbackManager));
    }

}
