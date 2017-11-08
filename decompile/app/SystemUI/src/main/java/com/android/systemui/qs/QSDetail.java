package com.android.systemui.qs;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.Animatable;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import com.android.internal.logging.MetricsLogger;
import com.android.systemui.FontSizeUtils;
import com.android.systemui.R;
import com.android.systemui.qs.QSPanel.Callback;
import com.android.systemui.qs.QSTile.DetailAdapter;
import com.android.systemui.statusbar.phone.BaseStatusBarHeader;
import com.android.systemui.statusbar.phone.QSTileHost;
import com.android.systemui.utils.analyze.BDReporter;

public class QSDetail extends LinearLayout {
    private QSDetailClipper mClipper;
    private boolean mClosingDetail;
    private DetailAdapter mDetailAdapter;
    private ViewGroup mDetailContent;
    private TextView mDetailDoneButton;
    private TextView mDetailSettingsButton;
    private final SparseArray<View> mDetailViews = new SparseArray();
    private boolean mFullyExpanded;
    private BaseStatusBarHeader mHeader;
    private final AnimatorListenerAdapter mHideGridContentWhenDone = new AnimatorListenerAdapter() {
        public void onAnimationCancel(Animator animation) {
            animation.removeListener(this);
        }

        public void onAnimationEnd(Animator animation) {
            if (QSDetail.this.mDetailAdapter != null) {
                QSDetail.this.mQsPanel.setGridContentVisibility(false);
                QSDetail.this.mHeader.setVisibility(4);
            }
        }
    };
    private QSTileHost mHost;
    private int mOpenX;
    private int mOpenY;
    private QSContainer mQsContainer;
    private View mQsDetailHeader;
    private View mQsDetailHeaderBack;
    private ImageView mQsDetailHeaderProgress;
    private Switch mQsDetailHeaderSwitch;
    private TextView mQsDetailHeaderTitle;
    private QSPanel mQsPanel;
    private final Callback mQsPanelCallback = new Callback() {
        public void onToggleStateChanged(final boolean state) {
            QSDetail.this.post(new Runnable() {
                public void run() {
                    QSDetail.this.handleToggleStateChanged(state);
                }
            });
        }

        public void onShowingDetail(final DetailAdapter detail, final int x, final int y) {
            QSDetail.this.post(new Runnable() {
                public void run() {
                    QSDetail.this.handleShowingDetail(detail, x, y);
                }
            });
        }

        public void onScanStateChanged(final boolean state) {
            QSDetail.this.post(new Runnable() {
                public void run() {
                    QSDetail.this.handleScanStateChanged(state);
                }
            });
        }
    };
    private boolean mScanState;
    private boolean mShow;
    private final AnimatorListenerAdapter mTeardownDetailWhenDone = new AnimatorListenerAdapter() {
        public void onAnimationStart(Animator animation) {
            QSDetail.this.mDetailContent.removeAllViews();
            QSDetail.this.setVisibility(8);
        }

        public void onAnimationEnd(Animator animation) {
            QSDetail.this.mClosingDetail = false;
            QSDetail.this.mShow = false;
            QSDetail.this.mQsContainer.setDetailShowing(QSDetail.this.mShow);
        }
    };
    private boolean mTriggeredExpand;

    public QSDetail(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        FontSizeUtils.updateFontSize(this.mDetailDoneButton, R.dimen.qs_detail_button_text_size);
        FontSizeUtils.updateFontSize(this.mDetailSettingsButton, R.dimen.qs_detail_button_text_size);
        for (int i = 0; i < this.mDetailViews.size(); i++) {
            ((View) this.mDetailViews.valueAt(i)).dispatchConfigurationChanged(newConfig);
        }
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mDetailContent = (ViewGroup) findViewById(16908290);
        this.mDetailSettingsButton = (TextView) findViewById(16908314);
        this.mDetailDoneButton = (TextView) findViewById(16908313);
        this.mQsDetailHeader = findViewById(R.id.qs_detail_header);
        this.mQsDetailHeaderBack = this.mQsDetailHeader.findViewById(16908363);
        this.mQsDetailHeaderTitle = (TextView) this.mQsDetailHeader.findViewById(16908310);
        this.mQsDetailHeaderSwitch = (Switch) this.mQsDetailHeader.findViewById(16908311);
        this.mQsDetailHeaderProgress = (ImageView) findViewById(R.id.qs_detail_header_progress);
        updateDetailText();
        this.mClipper = new QSDetailClipper(this);
        OnClickListener doneListener = new OnClickListener() {
            public void onClick(View v) {
                BDReporter.c(QSDetail.this.mContext, 369);
                QSDetail.this.announceForAccessibility(QSDetail.this.mContext.getString(R.string.accessibility_desc_quick_settings));
                QSDetail.this.mQsPanel.closeDetail();
            }
        };
        this.mQsDetailHeaderBack.setOnClickListener(doneListener);
        this.mDetailDoneButton.setOnClickListener(doneListener);
    }

    public void setQsPanel(QSPanel panel, BaseStatusBarHeader header) {
        this.mQsPanel = panel;
        this.mHeader = header;
        this.mHeader.setCallback(this.mQsPanelCallback);
        this.mQsPanel.setCallback(this.mQsPanelCallback);
    }

    public void setQsContainer(QSContainer qsContainer) {
        this.mQsContainer = qsContainer;
    }

    public void setHost(QSTileHost host) {
        this.mHost = host;
    }

    public boolean isShowingDetail() {
        return this.mDetailAdapter != null;
    }

    public void setFullyExpanded(boolean fullyExpanded) {
        this.mFullyExpanded = fullyExpanded;
    }

    public void setExpanded(boolean qsExpanded) {
        if (!qsExpanded) {
            this.mTriggeredExpand = false;
        }
    }

    private void updateDetailText() {
        this.mDetailDoneButton.setText(R.string.quick_settings_done);
        this.mDetailSettingsButton.setText(R.string.quick_settings_more_settings);
    }

    public QSDetailClipper getClipper() {
        return this.mClipper;
    }

    private void handleShowingDetail(final DetailAdapter adapter, int x, int y) {
        boolean showingDetail = adapter != null;
        setClickable(showingDetail);
        if (showingDetail) {
            this.mQsDetailHeaderTitle.setText(adapter.getTitle());
            Boolean toggleState = adapter.getToggleState();
            if (toggleState == null) {
                this.mQsDetailHeaderSwitch.setVisibility(4);
                this.mQsDetailHeader.setClickable(false);
            } else {
                this.mQsDetailHeaderSwitch.setVisibility(0);
                this.mQsDetailHeaderSwitch.setChecked(toggleState.booleanValue());
                this.mQsDetailHeader.setClickable(true);
                this.mQsDetailHeader.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        boolean checked = !QSDetail.this.mQsDetailHeaderSwitch.isChecked();
                        QSDetail.this.mQsDetailHeaderSwitch.setChecked(checked);
                        adapter.setToggleState(checked);
                    }
                });
            }
            if (this.mFullyExpanded) {
                this.mTriggeredExpand = false;
            } else {
                this.mTriggeredExpand = true;
            }
            this.mOpenX = x;
            this.mOpenY = y;
        } else {
            x = this.mOpenX;
            y = this.mOpenY;
            if (this.mTriggeredExpand) {
                this.mTriggeredExpand = false;
            }
        }
        boolean visibleDiff = (this.mDetailAdapter != null ? 1 : null) != (adapter != null ? 1 : null);
        if (visibleDiff || this.mDetailAdapter != adapter) {
            AnimatorListener listener;
            if (adapter != null) {
                int viewCacheIndex = adapter.getMetricsCategory();
                View detailView = adapter.createDetailView(this.mContext, (View) this.mDetailViews.get(viewCacheIndex), this.mDetailContent);
                if (detailView == null) {
                    throw new IllegalStateException("Must return detail view");
                }
                final Intent settingsIntent = adapter.getSettingsIntent();
                this.mDetailSettingsButton.setVisibility(settingsIntent != null ? 0 : 8);
                this.mDetailSettingsButton.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        BDReporter.c(QSDetail.this.mContext, 368);
                        QSDetail.this.mHost.startActivityDismissingKeyguard(settingsIntent);
                    }
                });
                this.mDetailContent.removeAllViews();
                this.mDetailContent.addView(detailView);
                this.mDetailViews.put(viewCacheIndex, detailView);
                MetricsLogger.visible(this.mContext, adapter.getMetricsCategory());
                announceForAccessibility(this.mContext.getString(R.string.accessibility_quick_settings_detail, new Object[]{adapter.getTitle()}));
                this.mDetailAdapter = adapter;
                listener = this.mHideGridContentWhenDone;
                setVisibility(0);
                this.mShow = true;
                this.mQsContainer.setDetailShowing(this.mShow);
            } else {
                if (this.mDetailAdapter != null) {
                    MetricsLogger.hidden(this.mContext, this.mDetailAdapter.getMetricsCategory());
                }
                this.mClosingDetail = true;
                this.mDetailAdapter = null;
                listener = this.mTeardownDetailWhenDone;
                this.mHeader.setVisibility(0);
                this.mQsPanel.setGridContentVisibility(true);
                this.mQsPanelCallback.onScanStateChanged(false);
            }
            sendAccessibilityEvent(32);
            if (visibleDiff) {
                if (this.mFullyExpanded || this.mDetailAdapter != null) {
                    setAlpha(1.0f);
                    if (showingDetail) {
                        boolean z;
                        QSDetailClipper qSDetailClipper = this.mClipper;
                        if (this.mDetailAdapter != null) {
                            z = true;
                        } else {
                            z = false;
                        }
                        qSDetailClipper.animateCircularClip(x, y, z, listener);
                    } else {
                        this.mDetailContent.removeAllViews();
                        setVisibility(8);
                        this.mClosingDetail = false;
                        this.mShow = false;
                        this.mQsContainer.setDetailShowing(this.mShow);
                    }
                } else {
                    animate().alpha(0.0f).setDuration(300).setListener(listener).start();
                }
            }
        }
    }

    private void handleToggleStateChanged(boolean state) {
        this.mQsDetailHeaderSwitch.setChecked(state);
    }

    private void handleScanStateChanged(boolean state) {
        if (this.mScanState != state) {
            this.mScanState = state;
            Animatable anim = (Animatable) this.mQsDetailHeaderProgress.getDrawable();
            if (state) {
                this.mQsDetailHeaderProgress.animate().alpha(1.0f);
                anim.start();
            } else {
                this.mQsDetailHeaderProgress.animate().alpha(0.0f);
                anim.stop();
            }
        }
    }

    public boolean isShown() {
        return this.mShow;
    }
}
