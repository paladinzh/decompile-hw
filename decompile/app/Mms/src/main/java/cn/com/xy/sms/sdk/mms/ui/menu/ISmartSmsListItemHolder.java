package cn.com.xy.sms.sdk.mms.ui.menu;

import android.app.Activity;
import android.text.SpannableStringBuilder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import com.android.mms.ui.SmartSmsBubbleManager;

public interface ISmartSmsListItemHolder {
    boolean bindCommonItem();

    boolean bindItemAfter(boolean z);

    View findViewById(int i);

    Activity getActivityContext();

    SpannableStringBuilder getCachedLinkingMsg();

    ListView getListView();

    ViewGroup getRichBubbleLayoutParent();

    SmartSmsBubbleManager getSmartSmsBubble();

    boolean isEditAble();

    boolean isScrollFing();

    boolean isShowingRich();

    void itemLayoutCallBack();

    boolean onSmartSmsEvent(short s);

    void setListView(ListView listView);

    void setRichViewLongClick(View view);

    boolean showDefaultListItem();
}
