package com.huawei.gallery.app.plugin;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import com.android.gallery3d.app.GalleryContext;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.util.ReportToBigData;
import com.huawei.gallery.actionbar.Action;
import com.huawei.gallery.actionbar.GalleryActionBar;
import com.huawei.gallery.app.PhotoPage.ActionBarProgressActionListener;
import com.huawei.gallery.burst.BurstPhotoManager;
import com.huawei.gallery.livephoto.LivePhotoPluginManager;
import com.huawei.gallery.panorama.Panorama3DManager;
import com.huawei.gallery.photorectify.PhotoRectifyImageManager;
import com.huawei.gallery.refocus.app.RefocusPhotoManager;
import com.huawei.gallery.refocus.wideaperture.photo3dview.app.WideAperture3DPhotoManager;
import com.huawei.gallery.threedmodel.ThreeDModelImageManager;
import com.huawei.gallery.voiceimage.VoiceImageManager;
import java.util.ArrayList;

public class PhotoFragmentPluginManager {
    private PluginHost mHost;
    private ArrayList<PhotoFragmentPlugin> mPlugins = new ArrayList();

    public interface PluginHost {
        GalleryActionBar getGalleryActionBar();

        void onLeavePluginMode(int i, Intent intent);
    }

    public PluginHost getHost() {
        return this.mHost;
    }

    public void init(ViewGroup photoFragmentView, GalleryContext context, PluginHost host, ActionBarProgressActionListener listener) {
        this.mHost = host;
        this.mPlugins.clear();
        addPlugin(new BurstPhotoManager(photoFragmentView, context));
        addPlugin(new VoiceImageManager(context, listener));
        addPlugin(new RefocusPhotoManager(context));
        addPlugin(new WideAperture3DPhotoManager(context));
        addPlugin(new Panorama3DManager(context));
        addPlugin(new PhotoRectifyImageManager(context));
        addPlugin(new ThreeDModelImageManager(context));
        addPlugin(new LivePhotoPluginManager(context));
    }

    private void addPlugin(PhotoFragmentPlugin plugin) {
        plugin.setManager(this);
        this.mPlugins.add(plugin);
    }

    public void onResume() {
        for (PhotoFragmentPlugin plugin : this.mPlugins) {
            plugin.onResume();
        }
    }

    public void onPause() {
        for (PhotoFragmentPlugin plugin : this.mPlugins) {
            plugin.onPause();
        }
    }

    public void onDestroy() {
        this.mPlugins.clear();
        this.mHost = null;
    }

    public void onPhotoChanged() {
        for (PhotoFragmentPlugin plugin : this.mPlugins) {
            plugin.onPhotoChanged();
        }
    }

    public boolean onBackPressed() {
        for (PhotoFragmentPlugin plugin : this.mPlugins) {
            if (plugin.onBackPressed()) {
                return true;
            }
        }
        return false;
    }

    public boolean onInterceptActionItemClick(Action action) {
        for (PhotoFragmentPlugin plugin : this.mPlugins) {
            if (plugin.onInterceptActionItemClick(action)) {
                return true;
            }
        }
        return false;
    }

    public boolean onEventsHappens(MediaItem currentItem, View button) {
        if (this.mHost == null || currentItem == null) {
            return false;
        }
        for (PhotoFragmentPlugin plugin : this.mPlugins) {
            if (plugin.onEventsHappens(currentItem, button)) {
                reportExtraButtonClicked(currentItem, plugin);
                return true;
            }
        }
        return false;
    }

    private void reportExtraButtonClicked(MediaItem currentItem, PhotoFragmentPlugin plugin) {
        String type = "";
        if (plugin.getClass() == BurstPhotoManager.class) {
            type = "Burst";
        } else if (plugin.getClass() == VoiceImageManager.class) {
            type = "Voice";
        } else if (plugin.getClass() == RefocusPhotoManager.class) {
            if (currentItem.getRefocusPhotoType() == 2) {
                type = "DualCamera";
            } else {
                type = "Refocus";
            }
        } else if (plugin.getClass() == PhotoRectifyImageManager.class) {
            type = "DocRectify";
        }
        if (!TextUtils.isEmpty(type)) {
            ReportToBigData.report(40, String.format("{PhotoButton:%s}", new Object[]{type}));
        }
    }

    public boolean updatePhotoExtraButton(PhotoExtraButton button, MediaItem currentItem) {
        for (PhotoFragmentPlugin plugin : this.mPlugins) {
            if (plugin.updateExtraButton(button, currentItem)) {
                return true;
            }
        }
        return false;
    }
}
