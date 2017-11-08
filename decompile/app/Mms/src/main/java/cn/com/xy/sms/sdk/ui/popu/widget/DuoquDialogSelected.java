package cn.com.xy.sms.sdk.ui.popu.widget;

public class DuoquDialogSelected {
    private int mSelectIndex = -1;
    private String mSelectName = null;

    public int getSelectIndex() {
        return this.mSelectIndex;
    }

    public void setSelectIndex(int selectIndex) {
        this.mSelectIndex = selectIndex;
        this.mSelectName = null;
    }

    public String getSelectName() {
        return this.mSelectName;
    }

    public void setSelectName(String selectName) {
        this.mSelectName = selectName;
        this.mSelectIndex = -1;
    }
}
