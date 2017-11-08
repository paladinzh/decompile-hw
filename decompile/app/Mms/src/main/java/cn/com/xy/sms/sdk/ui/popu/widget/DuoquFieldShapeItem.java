package cn.com.xy.sms.sdk.ui.popu.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import cn.com.xy.sms.sdk.smsmessage.BusinessSmsMessage;
import cn.com.xy.sms.sdk.ui.popu.util.ThemeUtil;
import com.android.mms.R$styleable;
import com.google.android.gms.R;

public class DuoquFieldShapeItem extends DuoquBaseTable {
    private int mContentPaddingtop = 0;
    private Context mContext;
    private int mLayoutId = 1000;
    private int mMarginBottom = 0;
    private int mMarginLeft = 0;
    private int mMarginRight = 0;
    private int mMarginTop = 0;
    private int mReferencedHeight = 10;
    private int mReferencedWidth = 24;
    private String mSingleLine = null;
    private int mTitlePaddingTop = 0;

    public DuoquFieldShapeItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        initParams(context, attrs);
        this.mContext = context;
    }

    protected void initParams(Context context, AttributeSet attrs) {
        TypedArray duoquTbAttr = context.obtainStyledAttributes(attrs, R$styleable.duoqu_table_attr);
        this.mTitlePaddingTop = Math.round(duoquTbAttr.getDimension(4, 0.0f));
        this.mContentPaddingtop = Math.round(duoquTbAttr.getDimension(5, 0.0f));
        this.mSingleLine = duoquTbAttr.getString(7);
        this.mMarginTop = Math.round(duoquTbAttr.getDimension(9, 0.0f));
        this.mMarginLeft = Math.round(duoquTbAttr.getDimension(10, 0.0f));
        this.mMarginRight = Math.round(duoquTbAttr.getDimension(11, 0.0f));
        this.mMarginBottom = Math.round(duoquTbAttr.getDimension(12, 0.0f));
        duoquTbAttr.recycle();
        if (this.mMarginTop == 0 && this.mMarginLeft == 0 && this.mMarginBottom == 0) {
            if (this.mMarginRight == 0) {
                return;
            }
        }
        LayoutParams rp = new LayoutParams(-1, -2);
        rp.setMargins(this.mMarginLeft, this.mMarginTop, this.mMarginRight, this.mMarginBottom);
        setLayoutParams(rp);
    }

    protected void getHolder(int pos, BusinessSmsMessage message, int dataSize, String dataKey, boolean isRebind) {
        DuoquHorizLFTableViewHolder holder = new DuoquHorizLFTableViewHolder(getContext());
        holder.leftTitleView = new TextView(getContext());
        holder.leftContentView = new TextView(getContext());
        holder.rightTitleView = new TextView(getContext());
        holder.rightContentView = new TextView(getContext());
        holder.midView = new View(getContext());
        holder.mUsedLayout = new RelativeLayout(getContext());
        RelativeLayout relativeLayout = holder.mUsedLayout;
        int i = this.mLayoutId + 1;
        this.mLayoutId = i;
        relativeLayout.setId(i);
        View view = holder.midView;
        i = this.mChildId + 1;
        this.mChildId = i;
        view.setId(i);
        LayoutParams viewParam = new LayoutParams(this.mReferencedWidth * 3, this.mReferencedHeight * 3);
        viewParam.addRule(14, -1);
        viewParam.addRule(15, -1);
        holder.mUsedLayout.addView(holder.midView, viewParam);
        TextView textView = holder.leftTitleView;
        i = this.mChildId + 1;
        this.mChildId = i;
        textView.setId(i);
        holder.leftTitleView.setIncludeFontPadding(false);
        holder.mUsedLayout.addView(holder.leftTitleView, getLayoutParams(this.mChildId, 0));
        textView = holder.leftContentView;
        i = this.mChildId + 1;
        this.mChildId = i;
        textView.setId(i);
        holder.leftContentView.setIncludeFontPadding(false);
        holder.leftContentView.setLineSpacing(0.0f, 1.1f);
        holder.mUsedLayout.addView(holder.leftContentView, getLayoutParams(this.mChildId, 0));
        textView = holder.rightTitleView;
        i = this.mChildId + 1;
        this.mChildId = i;
        textView.setId(i);
        holder.rightTitleView.setIncludeFontPadding(false);
        holder.mUsedLayout.addView(holder.rightTitleView, getLayoutParams(this.mChildId, 0));
        textView = holder.rightContentView;
        i = this.mChildId + 1;
        this.mChildId = i;
        textView.setId(i);
        holder.rightContentView.setIncludeFontPadding(false);
        holder.rightContentView.setLineSpacing(0.0f, 1.1f);
        holder.mUsedLayout.addView(holder.rightContentView, getLayoutParams(this.mChildId, 0));
        holder.rightContentView.setTextSize(0, getContext().getResources().getDimension(R.dimen.duoqu_table_content_stringsize));
        holder.rightTitleView.setTextSize(0, getContext().getResources().getDimension(R.dimen.duoqu_table_title_stringsize));
        holder.leftContentView.setTextSize(0, getContext().getResources().getDimension(R.dimen.duoqu_table_content_stringsize));
        holder.leftTitleView.setTextSize(0, getContext().getResources().getDimension(R.dimen.duoqu_table_title_stringsize));
        holder.rightContentView.setTextColor(ThemeUtil.getColorInteger(this.mContext, "1000"));
        holder.rightTitleView.setTextColor(ThemeUtil.getColorInteger(this.mContext, "1001"));
        holder.leftContentView.setTextColor(ThemeUtil.getColorInteger(this.mContext, "1000"));
        holder.leftTitleView.setTextColor(ThemeUtil.getColorInteger(this.mContext, "1001"));
        if (pos > 0) {
            holder.rightTitleView.setPadding(0, this.mTitlePaddingTop, 0, 0);
            holder.leftTitleView.setPadding(0, this.mTitlePaddingTop, 0, 0);
        }
        holder.rightContentView.setPadding(0, this.mContentPaddingtop, 0, 0);
        holder.leftContentView.setPadding(0, this.mContentPaddingtop, 0, 0);
        addView(holder.mUsedLayout, getRelativeLayoutParams(this.mLayoutId));
        if ("true".equals(this.mSingleLine)) {
            holder.leftContentView.setSingleLine();
            holder.leftContentView.setEllipsize(TruncateAt.valueOf("END"));
        }
        holder.setContent(pos, message, dataKey, isRebind);
        this.mViewHolderList.add(holder);
    }

    private LayoutParams getRelativeLayoutParams(int viewId) {
        LayoutParams params = new LayoutParams(-1, -2);
        if (viewId > 1001) {
            params.addRule(3, viewId - 1);
        }
        return params;
    }

    public LayoutParams getLayoutParams(int childId, int customLayoutWidth) {
        LayoutParams params;
        if (customLayoutWidth != 0) {
            params = new LayoutParams(customLayoutWidth, -2);
        } else {
            params = new LayoutParams(-1, -2);
        }
        if (childId % 5 == 2) {
            params.addRule(0, childId - 1);
            params.addRule(9);
        }
        if (childId % 5 == 3) {
            params.addRule(0, childId - 2);
            params.addRule(3, childId - 1);
            params.addRule(9);
        }
        if (childId % 5 == 4) {
            params.addRule(1, childId - 3);
            params.addRule(4, childId - 2);
        }
        if (childId % 5 == 0) {
            params.addRule(1, childId - 4);
            params.addRule(3, childId - 1);
        }
        return params;
    }

    protected LayoutParams getLayoutParams(int childId) {
        return null;
    }
}
