package com.android.mms.attachment.datamodel.media;

import android.database.Cursor;
import com.android.mms.attachment.Factory;
import java.util.ArrayList;

public class GalleryCursorManager {
    private Cursor mGalleryCursor;
    private ArrayList<GalleryCursorListener> mGalleryCursorListeners = new ArrayList();

    public interface GalleryCursorListener {
        void refreshCursor();
    }

    public static GalleryCursorManager get() {
        return Factory.get().getGalleryCursorManager();
    }

    public void registeGalleryCursorListener(GalleryCursorListener galleryCursorListener) {
        if (!(this.mGalleryCursorListeners == null || this.mGalleryCursorListeners.contains(galleryCursorListener))) {
            this.mGalleryCursorListeners.add(galleryCursorListener);
        }
    }

    public void unregisterGalleryCursorListener(GalleryCursorListener galleryCursorListener) {
        if (this.mGalleryCursorListeners != null && this.mGalleryCursorListeners.size() != 0) {
            this.mGalleryCursorListeners.remove(galleryCursorListener);
        }
    }

    public void swapGalleryCursor(Cursor changedCursor) {
        this.mGalleryCursor = changedCursor;
        if (this.mGalleryCursorListeners != null && this.mGalleryCursorListeners.size() != 0) {
            for (int i = 0; i < this.mGalleryCursorListeners.size(); i++) {
                ((GalleryCursorListener) this.mGalleryCursorListeners.get(i)).refreshCursor();
            }
        }
    }

    public void clearGalleryCursor() {
        this.mGalleryCursor = null;
    }

    public Cursor getGalleryCursor() {
        return this.mGalleryCursor;
    }

    public boolean isGalleryCursorVailbie() {
        if (this.mGalleryCursor == null || this.mGalleryCursor.isClosed()) {
            return false;
        }
        return true;
    }

    public int getGalleryCursorCount() {
        if (this.mGalleryCursor == null || this.mGalleryCursor.isClosed()) {
            return 0;
        }
        return this.mGalleryCursor.getCount();
    }
}
