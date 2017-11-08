package com.huawei.systemmanager.spacecleanner.engine.trash;

import android.content.Context;
import com.huawei.systemmanager.comm.Storage.PathEntry;
import com.huawei.systemmanager.spacecleanner.utils.MediaUtil;
import com.huawei.systemmanager.spacecleanner.utils.TrashUtils;
import java.io.IOException;

public class AudioTrash extends FileTrash {
    private String mArtist;

    public AudioTrash(String file, PathEntry pathEntry) {
        super(file, pathEntry);
    }

    public int getType() {
        return 512;
    }

    public boolean isSuggestClean() {
        return false;
    }

    public void setArtist(String artist) {
        if (TrashUtils.isInvalidString(artist)) {
            this.mArtist = "";
        } else {
            this.mArtist = artist;
        }
    }

    public String getArtist() {
        return this.mArtist;
    }

    public boolean clean(Context cotnext) {
        boolean res = super.clean(cotnext);
        MediaUtil.deleteMediaProvider(MediaUtil.AUDIO_URI, this.mPath);
        return res;
    }

    public void printf(Appendable appendable) throws IOException {
        super.printf(appendable);
        appendable.append(", artist:").append(getArtist());
    }
}
