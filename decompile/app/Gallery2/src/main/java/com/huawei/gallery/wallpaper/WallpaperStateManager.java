package com.huawei.gallery.wallpaper;

import android.support.v4.app.FragmentTransaction;
import com.android.gallery3d.util.FileUtils;
import com.huawei.gallery.util.MyPrinter;
import java.io.File;
import java.io.FilenameFilter;

public class WallpaperStateManager {
    private static final MyPrinter LOG = new MyPrinter(WallpaperStateManager.class.getSimpleName());
    private static FilenameFilter sWallpaperStateFilter = new FilenameFilter() {
        public boolean accept(File dir, String filename) {
            return filename.startsWith("backup_wallpaper_state");
        }
    };
    private boolean mLoaded = false;
    private int mRotation;
    private String mState;
    private String mStateFile;

    public void loadInfo() {
        for (File stateFile : getAllBackupWallpaper()) {
            if (stateFile.isFile()) {
                this.mStateFile = stateFile.getAbsolutePath();
                String fileName = stateFile.getName();
                String rawState = fileName.substring("backup_wallpaper_state".length(), fileName.length() - 4);
                String rotation = rawState.substring(0, 3);
                try {
                    this.mRotation = Integer.parseInt(rotation);
                    this.mState = rawState.substring(3);
                } catch (NumberFormatException e) {
                    LOG.w("parse rotation error", e);
                    this.mState = rawState;
                }
                LOG.d("file :" + this.mStateFile + ", state: " + this.mState + ", rotation: " + rotation);
            } else {
                LOG.d("find an unexpected directory : " + stateFile.getAbsolutePath());
            }
        }
        this.mLoaded = true;
    }

    public String getWallpaperState() {
        if (!this.mLoaded) {
            loadInfo();
        }
        LOG.d("restored state: " + this.mState);
        return this.mState;
    }

    public String getBackupWallpaper() {
        if (!this.mLoaded) {
            loadInfo();
        }
        return this.mStateFile;
    }

    public int getBackupWallpaperRotation() {
        if (!this.mLoaded) {
            loadInfo();
        }
        return this.mRotation;
    }

    public void backupWallpaper(String origionFile, String state, int rotation) {
        int i = 0;
        String fileName = String.format("%s%03d%s%s", new Object[]{"backup_wallpaper_state", Integer.valueOf(rotation % 360), state, ".jpg"});
        File temFile = new File(WallpaperConstant.PATH_WALLPAPER, state);
        File targetFile = new File(WallpaperConstant.PATH_WALLPAPER, fileName);
        FileUtils.copyFileToNewFile(new File(origionFile), temFile, FragmentTransaction.TRANSIT_ENTER_MASK);
        LOG.d("backup wallpaper file name : " + fileName);
        File[] backupWallpapers = getAllBackupWallpaper();
        int length = backupWallpapers.length;
        while (i < length) {
            LOG.d("delete useless file : " + FileUtils.deleteFile(backupWallpapers[i]));
            i++;
        }
        LOG.d("rename to target: " + temFile.renameTo(targetFile));
    }

    private File[] getAllBackupWallpaper() {
        File[] states = new File(WallpaperConstant.PATH_WALLPAPER).listFiles(sWallpaperStateFilter);
        if (states == null) {
            return new File[0];
        }
        return states;
    }
}
