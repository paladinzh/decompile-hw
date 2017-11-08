package com.android.systemui.statusbar;

import android.app.Notification;
import android.graphics.PorterDuff.Mode;
import android.text.TextUtils;
import android.view.NotificationHeaderView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class NotificationHeaderUtil {
    private static final ResultApplicator mGreyApplicator = new ResultApplicator() {
        public void apply(View view, boolean apply) {
            NotificationHeaderView header = (NotificationHeaderView) view;
            ImageView expand = (ImageView) view.findViewById(16909228);
            applyToChild((ImageView) view.findViewById(16908294), apply, header.getOriginalIconColor());
            applyToChild(expand, apply, header.getOriginalNotificationColor());
        }

        private void applyToChild(View view, boolean shouldApply, int originalColor) {
            if (originalColor != -1) {
                ImageView imageView = (ImageView) view;
                imageView.getDrawable().mutate();
                if (shouldApply) {
                    imageView.getDrawable().setColorFilter(view.getContext().getColor(17170512), Mode.SRC_ATOP);
                    return;
                }
                imageView.getDrawable().setColorFilter(originalColor, Mode.SRC_ATOP);
            }
        }
    };
    private static final IconComparator sGreyComparator = new IconComparator() {
        public boolean compare(View parent, View child, Object parentData, Object childData) {
            if (hasSameIcon(parentData, childData)) {
                return hasSameColor(parentData, childData);
            }
            return true;
        }
    };
    private static final DataExtractor sIconExtractor = new DataExtractor() {
        public Object extractData(ExpandableNotificationRow row) {
            return row.getStatusBarNotification().getNotification();
        }
    };
    private static final IconComparator sIconVisibilityComparator = new IconComparator() {
        public boolean compare(View parent, View child, Object parentData, Object childData) {
            if (hasSameIcon(parentData, childData)) {
                return hasSameColor(parentData, childData);
            }
            return false;
        }
    };
    private static final TextViewComparator sTextViewComparator = new TextViewComparator();
    private static final VisibilityApplicator sVisibilityApplicator = new VisibilityApplicator();
    private final ArrayList<HeaderProcessor> mComparators = new ArrayList();
    private final HashSet<Integer> mDividers = new HashSet();
    private final ExpandableNotificationRow mRow;

    private interface DataExtractor {
        Object extractData(ExpandableNotificationRow expandableNotificationRow);
    }

    private interface ViewComparator {
        boolean compare(View view, View view2, Object obj, Object obj2);

        boolean isEmpty(View view);
    }

    private static abstract class IconComparator implements ViewComparator {
        private IconComparator() {
        }

        public boolean compare(View parent, View child, Object parentData, Object childData) {
            return false;
        }

        protected boolean hasSameIcon(Object parentData, Object childData) {
            return ((Notification) parentData).getSmallIcon().sameAs(((Notification) childData).getSmallIcon());
        }

        protected boolean hasSameColor(Object parentData, Object childData) {
            return ((Notification) parentData).color == ((Notification) childData).color;
        }

        public boolean isEmpty(View view) {
            return false;
        }
    }

    private interface ResultApplicator {
        void apply(View view, boolean z);
    }

    private static class HeaderProcessor {
        private final ResultApplicator mApplicator;
        private boolean mApply;
        private ViewComparator mComparator;
        private final DataExtractor mExtractor;
        private final int mId;
        private Object mParentData;
        private final ExpandableNotificationRow mParentRow;
        private View mParentView;

        public static HeaderProcessor forTextView(ExpandableNotificationRow row, int id) {
            return new HeaderProcessor(row, id, null, NotificationHeaderUtil.sTextViewComparator, NotificationHeaderUtil.sVisibilityApplicator);
        }

        HeaderProcessor(ExpandableNotificationRow row, int id, DataExtractor extractor, ViewComparator comparator, ResultApplicator applicator) {
            this.mId = id;
            this.mExtractor = extractor;
            this.mApplicator = applicator;
            this.mComparator = comparator;
            this.mParentRow = row;
        }

        public void init() {
            Object obj = null;
            this.mParentView = this.mParentRow.getNotificationHeader().findViewById(this.mId);
            if (this.mExtractor != null) {
                obj = this.mExtractor.extractData(this.mParentRow);
            }
            this.mParentData = obj;
            this.mApply = !this.mComparator.isEmpty(this.mParentView);
        }

        public void compareToHeader(ExpandableNotificationRow row) {
            if (this.mApply) {
                NotificationHeaderView header = row.getNotificationHeader();
                if (header == null) {
                    this.mApply = false;
                } else {
                    this.mApply = this.mComparator.compare(this.mParentView, header.findViewById(this.mId), this.mParentData, this.mExtractor == null ? null : this.mExtractor.extractData(row));
                }
            }
        }

        public void apply(ExpandableNotificationRow row) {
            apply(row, false);
        }

        public void apply(ExpandableNotificationRow row, boolean reset) {
            boolean apply = this.mApply && !reset;
            if (row.isSummaryWithChildren()) {
                applyToView(apply, row.getNotificationHeader());
                return;
            }
            applyToView(apply, row.getPrivateLayout().getContractedChild());
            applyToView(apply, row.getPrivateLayout().getHeadsUpChild());
            applyToView(apply, row.getPrivateLayout().getExpandedChild());
        }

        private void applyToView(boolean apply, View parent) {
            if (parent != null) {
                View view = parent.findViewById(this.mId);
                if (view != null && !this.mComparator.isEmpty(view)) {
                    this.mApplicator.apply(view, apply);
                }
            }
        }
    }

    private static class TextViewComparator implements ViewComparator {
        private TextViewComparator() {
        }

        public boolean compare(View parent, View child, Object parentData, Object childData) {
            return ((TextView) parent).getText().equals(((TextView) child).getText());
        }

        public boolean isEmpty(View view) {
            return TextUtils.isEmpty(((TextView) view).getText());
        }
    }

    private static class VisibilityApplicator implements ResultApplicator {
        private VisibilityApplicator() {
        }

        public void apply(View view, boolean apply) {
            view.setVisibility(apply ? 8 : 0);
        }
    }

    public NotificationHeaderUtil(ExpandableNotificationRow row) {
        this.mRow = row;
        this.mComparators.add(new HeaderProcessor(this.mRow, 16908294, sIconExtractor, sIconVisibilityComparator, sVisibilityApplicator));
        this.mComparators.add(new HeaderProcessor(this.mRow, 16909220, sIconExtractor, sGreyComparator, mGreyApplicator));
        this.mComparators.add(new HeaderProcessor(this.mRow, 16909229, null, new ViewComparator() {
            public boolean compare(View parent, View child, Object parentData, Object childData) {
                return parent.getVisibility() != 8;
            }

            public boolean isEmpty(View view) {
                boolean z = false;
                if (!(view instanceof ImageView)) {
                    return false;
                }
                if (((ImageView) view).getDrawable() == null) {
                    z = true;
                }
                return z;
            }
        }, sVisibilityApplicator));
        this.mComparators.add(HeaderProcessor.forTextView(this.mRow, 16909221));
        this.mComparators.add(HeaderProcessor.forTextView(this.mRow, 16909428));
        this.mDividers.add(Integer.valueOf(16909427));
        this.mDividers.add(Integer.valueOf(16909226));
    }

    public void updateChildrenHeaderAppearance() {
        List<ExpandableNotificationRow> notificationChildren = this.mRow.getNotificationChildren();
        if (notificationChildren != null) {
            int compI;
            int i;
            ExpandableNotificationRow row;
            for (compI = 0; compI < this.mComparators.size(); compI++) {
                ((HeaderProcessor) this.mComparators.get(compI)).init();
            }
            for (i = 0; i < notificationChildren.size(); i++) {
                row = (ExpandableNotificationRow) notificationChildren.get(i);
                for (compI = 0; compI < this.mComparators.size(); compI++) {
                    ((HeaderProcessor) this.mComparators.get(compI)).compareToHeader(row);
                }
            }
            for (i = 0; i < notificationChildren.size(); i++) {
                row = (ExpandableNotificationRow) notificationChildren.get(i);
                for (compI = 0; compI < this.mComparators.size(); compI++) {
                    ((HeaderProcessor) this.mComparators.get(compI)).apply(row);
                }
                sanitizeHeaderViews(row);
            }
        }
    }

    private void sanitizeHeaderViews(ExpandableNotificationRow row) {
        if (row.isSummaryWithChildren()) {
            sanitizeHeader(row.getNotificationHeader());
            return;
        }
        NotificationContentView layout = row.getPrivateLayout();
        sanitizeChild(layout.getContractedChild());
        sanitizeChild(layout.getHeadsUpChild());
        sanitizeChild(layout.getExpandedChild());
    }

    private void sanitizeChild(View child) {
        if (child != null) {
            sanitizeHeader((NotificationHeaderView) child.findViewById(16909220));
        }
    }

    private void sanitizeHeader(NotificationHeaderView rowHeader) {
        if (rowHeader != null) {
            int i;
            View child;
            int timeVisibility;
            int childCount = rowHeader.getChildCount();
            View time = rowHeader.findViewById(16908436);
            boolean hasVisibleText = false;
            for (i = 1; i < childCount - 1; i++) {
                child = rowHeader.getChildAt(i);
                if ((child instanceof TextView) && child.getVisibility() != 8 && !this.mDividers.contains(Integer.valueOf(child.getId())) && child != time) {
                    hasVisibleText = true;
                    break;
                }
            }
            if (!hasVisibleText || this.mRow.getStatusBarNotification().getNotification().showsTime()) {
                timeVisibility = 0;
            } else {
                timeVisibility = 8;
            }
            time.setVisibility(timeVisibility);
            View left = null;
            i = 1;
            while (i < childCount - 1) {
                child = rowHeader.getChildAt(i);
                if (this.mDividers.contains(Integer.valueOf(child.getId()))) {
                    int i2;
                    boolean visible = false;
                    i++;
                    while (i < childCount - 1) {
                        View right = rowHeader.getChildAt(i);
                        if (this.mDividers.contains(Integer.valueOf(right.getId()))) {
                            i--;
                            break;
                        } else if (right.getVisibility() == 8 || !(right instanceof TextView)) {
                            i++;
                        } else {
                            visible = left != null;
                            left = right;
                        }
                    }
                    if (visible) {
                        i2 = 0;
                    } else {
                        i2 = 8;
                    }
                    child.setVisibility(i2);
                } else if (child.getVisibility() != 8 && (child instanceof TextView)) {
                    left = child;
                }
                i++;
            }
        }
    }

    public void restoreNotificationHeader(ExpandableNotificationRow row) {
        for (int compI = 0; compI < this.mComparators.size(); compI++) {
            ((HeaderProcessor) this.mComparators.get(compI)).apply(row, true);
        }
        sanitizeHeaderViews(row);
    }
}
