package cn.com.xy.sms.sdk.ui.popu.widget;

public class DuoquFlightNumsDataSource implements DuoquSource {
    private String[] mDataSource = null;

    public DuoquFlightNumsDataSource(String[] dataSource) {
        if (dataSource != null) {
            this.mDataSource = (String[]) dataSource.clone();
        }
    }

    public int getLength() {
        return this.mDataSource == null ? 0 : this.mDataSource.length;
    }

    public Object getValue(int index) {
        if (this.mDataSource == null || this.mDataSource.length <= index) {
            return null;
        }
        return this.mDataSource[index];
    }
}
