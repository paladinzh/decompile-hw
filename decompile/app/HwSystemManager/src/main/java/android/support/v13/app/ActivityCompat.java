package android.support.v13.app;

import android.app.Activity;
import android.support.v13.view.DragAndDropPermissionsCompat;
import android.view.DragEvent;

public class ActivityCompat extends android.support.v4.app.ActivityCompat {
    public static DragAndDropPermissionsCompat requestDragAndDropPermissions(Activity activity, DragEvent dragEvent) {
        return DragAndDropPermissionsCompat.request(activity, dragEvent);
    }

    private ActivityCompat() {
    }
}
