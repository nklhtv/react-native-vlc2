package com.stremio.vlc;

import android.content.Context;
import android.view.View;

import androidx.databinding.ObservableField;

import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ViewManager;
import com.stremio.vlc.renderer.VLCCastingModule;
import com.stremio.vlc.video.VLCVideoCallbackManager;
import com.stremio.vlc.video.VLCVideoViewManager;

import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.RendererItem;

import java.util.Arrays;
import java.util.List;

public final class VLCPackage implements ReactPackage {

    private final LibVLC mLibVLC;
    private final View.OnKeyListener mOnKeyListener;
    private final VLCVideoCallbackManager mCallbackManager;
    private final ObservableField<RendererItem> mSelectedRenderer;

    public VLCPackage(final Context context, final List<String> options, final View.OnKeyListener onKeyListener, final VLCVideoCallbackManager callbackManager, final ObservableField<RendererItem> selectedRenderer) {
        mLibVLC = new LibVLC(context, options);
        mOnKeyListener = onKeyListener;
        mCallbackManager = callbackManager;
        mSelectedRenderer = selectedRenderer;
    }

    @Override
    public List<NativeModule> createNativeModules(final ReactApplicationContext reactApplicationContext) {
        return Arrays.<NativeModule>asList(new VLCCastingModule(reactApplicationContext, mLibVLC, mSelectedRenderer));
    }

    @Override
    public List<ViewManager> createViewManagers(final ReactApplicationContext reactApplicationContext) {
        return Arrays.<ViewManager>asList(new VLCVideoViewManager(mLibVLC, mOnKeyListener, mCallbackManager, mSelectedRenderer));
    }

}
