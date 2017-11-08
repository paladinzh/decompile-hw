package com.huawei.systemmanager.spacecleanner.ui.secondaryui.uiitem;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.FileUtil;
import com.huawei.systemmanager.spacecleanner.engine.trash.AudioTrash;
import com.huawei.systemmanager.spacecleanner.engine.trash.Trash;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.CommonTrashItem.TrashTransFunc;
import com.huawei.systemmanager.util.HwLog;
import java.util.Locale;

public class AudioTrashItem extends FileTrashItem<AudioTrash> {
    public static final TrashTransFunc<AudioTrashItem> sTransFunc = new TrashTransFunc<AudioTrashItem>() {
        public AudioTrashItem apply(Trash input) {
            if (input == null) {
                HwLog.e(TrashTransFunc.TAG, "AudioTrashItem trans, input is null!");
                return null;
            } else if (input instanceof AudioTrash) {
                return new AudioTrashItem((AudioTrash) input);
            } else {
                HwLog.e(TrashTransFunc.TAG, "AudioTrashItem trans, trans error, origin type:" + input.getType());
                return null;
            }
        }

        public int getTrashType() {
            return 512;
        }
    };

    public AudioTrashItem(AudioTrash trash) {
        super(trash);
    }

    public String getDescription(Context ctx) {
        String artist = ((AudioTrash) this.mTrash).getArtist();
        if (TextUtils.isEmpty(artist) || artist.toLowerCase(Locale.getDefault()).contains("unknown")) {
            return FileUtil.getFileSize(getTrashSize());
        }
        return FileUtil.getFileSize(getTrashSize()) + "  " + artist;
    }

    public Drawable getIcon(Context context) {
        return context.getResources().getDrawable(R.drawable.ic_storagecleaner_music);
    }

    public boolean isUseIconAlways() {
        return true;
    }
}
