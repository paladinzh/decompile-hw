package ucd.ui.framework.coreEx.video;

import android.content.Context;
import java.util.ArrayList;

public class MediaManager {
    private Context context;
    private int counter = 0;
    private ArrayList<MPlayer> list;

    public MediaManager(Context c) {
        this.context = c;
        this.list = new ArrayList();
    }
}
