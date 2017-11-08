package android.support.v4.content;

import android.content.SharedPreferences.Editor;
import android.os.Build.VERSION;
import android.support.annotation.NonNull;

public final class SharedPreferencesCompat$EditorCompat {
    private static SharedPreferencesCompat$EditorCompat sInstance;
    private final Helper mHelper;

    private interface Helper {
        void apply(@NonNull Editor editor);
    }

    private static class EditorHelperApi9Impl implements Helper {
        private EditorHelperApi9Impl() {
        }

        public void apply(@NonNull Editor editor) {
            EditorCompatGingerbread.apply(editor);
        }
    }

    private static class EditorHelperBaseImpl implements Helper {
        private EditorHelperBaseImpl() {
        }

        public void apply(@NonNull Editor editor) {
            editor.commit();
        }
    }

    private SharedPreferencesCompat$EditorCompat() {
        if (VERSION.SDK_INT >= 9) {
            this.mHelper = new EditorHelperApi9Impl();
        } else {
            this.mHelper = new EditorHelperBaseImpl();
        }
    }

    public static SharedPreferencesCompat$EditorCompat getInstance() {
        if (sInstance == null) {
            sInstance = new SharedPreferencesCompat$EditorCompat();
        }
        return sInstance;
    }

    public void apply(@NonNull Editor editor) {
        this.mHelper.apply(editor);
    }
}
