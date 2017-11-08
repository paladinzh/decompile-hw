package com.huawei.systemmanager.spacecleanner.ui.photomanager;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import com.huawei.systemmanager.spacecleanner.engine.trash.Trash;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.CommonTrashItem.TrashTransFunc;
import com.huawei.systemmanager.spacecleanner.ui.secondaryui.ListTrashSetActivity;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ListPhotoSetActivity extends ListTrashSetActivity implements onCallPhotoTrashSetListener {
    private static final String TAG = "ListPhotoSetActivity";
    private Fragment defaultFragment = new ListAlbumSetFragment();
    private ListPhotoGridSetFragment gridsetFragment = new ListPhotoGridSetFragment();
    private List<PhotoFolder> mPhotoFolders = new ArrayList();

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if (savedInstanceState != null || intent == null) {
            HwLog.d(TAG, "intent is invalida te or save");
            finish();
            return;
        }
        resetTrashSet();
    }

    protected Fragment buildDefaultFragment() {
        return this.defaultFragment;
    }

    public List<PhotoFolder> getPhotoFolders() {
        return this.mPhotoFolders;
    }

    public void startGridSetFragment(String path) {
        HwLog.i(TAG, "startGridSetFragment");
        this.gridsetFragment.initFolderPath(path);
        switchContent(this.defaultFragment, this.gridsetFragment);
    }

    public void startDefaultFragment() {
        HwLog.i(TAG, "startDefaultFragment");
        switchContent(this.gridsetFragment, this.defaultFragment);
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode != 4 || getContainedFragment() == this.defaultFragment) {
            return super.onKeyDown(keyCode, event);
        }
        HwLog.i(TAG, "startDefaultFragment");
        startDefaultFragment();
        return true;
    }

    public void resetTrashSet() {
        setAlbumList();
    }

    public void setAlbumList() {
        this.mPhotoFolders.clear();
        List<Trash> trashs = initAndGetData();
        TrashTransFunc<PhotoFolder> transFunc = PhotoFolder.sTransFunc;
        for (Trash trash : trashs) {
            PhotoFolder photoFolder = (PhotoFolder) transFunc.apply(trash);
            if (!(photoFolder == null || photoFolder.isCleaned())) {
                photoFolder.checkIndex();
                this.mPhotoFolders.add(photoFolder);
            }
        }
        Collections.sort(this.mPhotoFolders, PhotoFolder.PHOTO_FOLDER_COMPARATOR);
    }
}
