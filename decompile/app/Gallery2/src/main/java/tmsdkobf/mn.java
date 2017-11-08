package tmsdkobf;

import com.autonavi.amap.mapcore.MapTilsCacheAndResManager;

/* compiled from: Unknown */
public class mn {
    private StringBuffer Bh = new StringBuffer();
    private nc yq = new nc("CheckPoint");

    public void commit() {
        if (this.Bh.length() > 0) {
            String string = this.yq.getString(MapTilsCacheAndResManager.AUTONAVI_DATA_PATH, null);
            if (string == null) {
                string = "";
            }
            this.yq.a(MapTilsCacheAndResManager.AUTONAVI_DATA_PATH, string + this.Bh.toString(), true);
            this.Bh = new StringBuffer();
        }
    }

    public void q(int i, int i2) {
        this.Bh.append(i + ":" + i2 + ";");
    }
}
