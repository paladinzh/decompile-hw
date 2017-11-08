package android.support.v13.view;

import android.app.Activity;
import android.view.DragAndDropPermissions;
import android.view.DragEvent;

class DragAndDropPermissionsCompatApi24 {
    DragAndDropPermissionsCompatApi24() {
    }

    public static Object request(Activity activity, DragEvent dragEvent) {
        return activity.requestDragAndDropPermissions(dragEvent);
    }

    public static void release(Object dragAndDropPermissions) {
        ((DragAndDropPermissions) dragAndDropPermissions).release();
    }
}
