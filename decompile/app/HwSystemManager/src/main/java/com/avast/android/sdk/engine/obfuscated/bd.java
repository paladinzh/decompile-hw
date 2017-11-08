package com.avast.android.sdk.engine.obfuscated;

import android.annotation.TargetApi;
import android.view.accessibility.AccessibilityNodeInfo;
import android.webkit.WebView;
import android.widget.EditText;
import com.avast.android.sdk.shield.webshield.AccessibilitySupportedBrowser;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

@TargetApi(18)
/* compiled from: Unknown */
public class bd {
    public AccessibilityNodeInfo a(AccessibilitySupportedBrowser accessibilitySupportedBrowser, AccessibilityNodeInfo accessibilityNodeInfo) {
        if (accessibilityNodeInfo == null) {
            return null;
        }
        List findAccessibilityNodeInfosByViewId = accessibilityNodeInfo.findAccessibilityNodeInfosByViewId("com.android.chrome:id/url_bar");
        if (findAccessibilityNodeInfosByViewId == null || findAccessibilityNodeInfosByViewId.isEmpty()) {
            LinkedList linkedList = new LinkedList();
            for (int i = 0; i < accessibilityNodeInfo.getChildCount(); i++) {
                AccessibilityNodeInfo child = accessibilityNodeInfo.getChild(i);
                if (child != null) {
                    linkedList.push(child);
                }
            }
            while (!linkedList.isEmpty()) {
                try {
                    AccessibilityNodeInfo accessibilityNodeInfo2 = (AccessibilityNodeInfo) linkedList.pop();
                    if (accessibilityNodeInfo2 != null) {
                        if (accessibilityNodeInfo2.getClassName() == null) {
                            accessibilityNodeInfo2.recycle();
                        } else if (accessibilityNodeInfo2.getClassName().equals(EditText.class.getName())) {
                            while (!linkedList.isEmpty()) {
                                ((AccessibilityNodeInfo) linkedList.pop()).recycle();
                            }
                            return accessibilityNodeInfo2;
                        } else {
                            for (int i2 = 0; i2 < accessibilityNodeInfo2.getChildCount(); i2++) {
                                AccessibilityNodeInfo child2 = accessibilityNodeInfo2.getChild(i2);
                                if (child2 != null) {
                                    linkedList.push(child2);
                                }
                            }
                            accessibilityNodeInfo2.recycle();
                        }
                    }
                } catch (Throwable th) {
                    Throwable th2 = th;
                    while (!linkedList.isEmpty()) {
                        ((AccessibilityNodeInfo) linkedList.pop()).recycle();
                    }
                }
            }
            while (!linkedList.isEmpty()) {
                ((AccessibilityNodeInfo) linkedList.pop()).recycle();
            }
            return null;
        }
        if (findAccessibilityNodeInfosByViewId.size() > 1) {
            ao.a("Multiple address bars, wth!");
        }
        ao.a("Sweet address bar, yum yum!");
        return (AccessibilityNodeInfo) findAccessibilityNodeInfosByViewId.get(0);
    }

    @TargetApi(18)
    public boolean a(AccessibilityNodeInfo accessibilityNodeInfo, LinkedList<String> linkedList) {
        if (accessibilityNodeInfo == null) {
            return false;
        }
        List<AccessibilityNodeInfo> findAccessibilityNodeInfosByViewId = accessibilityNodeInfo.findAccessibilityNodeInfosByViewId("com.android.systemui:id/task_view_bar");
        if (findAccessibilityNodeInfosByViewId == null || findAccessibilityNodeInfosByViewId.isEmpty()) {
            return false;
        }
        ao.a(Arrays.toString(linkedList.toArray()));
        Iterator descendingIterator = linkedList.descendingIterator();
        boolean z = false;
        while (descendingIterator.hasNext() && !z) {
            String str = (String) descendingIterator.next();
            ao.a("Trying to block " + str);
            boolean z2 = z;
            for (AccessibilityNodeInfo accessibilityNodeInfo2 : findAccessibilityNodeInfosByViewId) {
                AccessibilityNodeInfo accessibilityNodeInfo3 = null;
                AccessibilityNodeInfo accessibilityNodeInfo4 = null;
                for (int i = 0; i < accessibilityNodeInfo2.getChildCount(); i++) {
                    AccessibilityNodeInfo child = accessibilityNodeInfo2.getChild(i);
                    if ("com.android.systemui:id/activity_description".equals(child.getViewIdResourceName())) {
                        accessibilityNodeInfo3 = child;
                    } else if ("com.android.systemui:id/dismiss_task".equals(child.getViewIdResourceName())) {
                        accessibilityNodeInfo4 = child;
                    } else {
                        child.recycle();
                    }
                }
                if (accessibilityNodeInfo3 == null || accessibilityNodeInfo4 == null) {
                    if (accessibilityNodeInfo3 != null) {
                        accessibilityNodeInfo3.recycle();
                    }
                    if (accessibilityNodeInfo4 != null) {
                        accessibilityNodeInfo4.recycle();
                    }
                } else {
                    if (accessibilityNodeInfo3.getText().toString().contains(str)) {
                        try {
                            z2 = accessibilityNodeInfo4.performAction(16);
                        } catch (Throwable th) {
                            if (accessibilityNodeInfo3 != null) {
                                accessibilityNodeInfo3.recycle();
                            }
                            if (accessibilityNodeInfo4 != null) {
                                accessibilityNodeInfo4.recycle();
                            }
                        }
                    }
                    if (accessibilityNodeInfo3 != null) {
                        accessibilityNodeInfo3.recycle();
                    }
                    if (accessibilityNodeInfo4 != null) {
                        accessibilityNodeInfo4.recycle();
                    }
                }
            }
            z = z2;
        }
        for (AccessibilityNodeInfo recycle : findAccessibilityNodeInfosByViewId) {
            recycle.recycle();
        }
        return z;
    }

    public AccessibilityNodeInfo b(AccessibilitySupportedBrowser accessibilitySupportedBrowser, AccessibilityNodeInfo accessibilityNodeInfo) {
        if (accessibilityNodeInfo == null) {
            return null;
        }
        LinkedList linkedList = new LinkedList();
        for (int i = 0; i < accessibilityNodeInfo.getChildCount(); i++) {
            AccessibilityNodeInfo child = accessibilityNodeInfo.getChild(i);
            if (child != null) {
                linkedList.push(child);
            }
        }
        while (!linkedList.isEmpty()) {
            try {
                AccessibilityNodeInfo accessibilityNodeInfo2 = (AccessibilityNodeInfo) linkedList.pop();
                if (accessibilityNodeInfo2 != null) {
                    if (accessibilityNodeInfo2.getClassName() == null) {
                        accessibilityNodeInfo2.recycle();
                    } else if (accessibilityNodeInfo2.getClassName().equals(WebView.class.getName())) {
                        while (!linkedList.isEmpty()) {
                            ((AccessibilityNodeInfo) linkedList.pop()).recycle();
                        }
                        return accessibilityNodeInfo2;
                    } else {
                        for (int i2 = 0; i2 < accessibilityNodeInfo2.getChildCount(); i2++) {
                            AccessibilityNodeInfo child2 = accessibilityNodeInfo2.getChild(i2);
                            if (child2 != null) {
                                linkedList.push(child2);
                            }
                        }
                        accessibilityNodeInfo2.recycle();
                    }
                }
            } catch (Throwable th) {
                Throwable th2 = th;
                while (!linkedList.isEmpty()) {
                    ((AccessibilityNodeInfo) linkedList.pop()).recycle();
                }
            }
        }
        while (!linkedList.isEmpty()) {
            ((AccessibilityNodeInfo) linkedList.pop()).recycle();
        }
        return null;
    }

    public AccessibilityNodeInfo c(AccessibilitySupportedBrowser accessibilitySupportedBrowser, AccessibilityNodeInfo accessibilityNodeInfo) {
        if (accessibilityNodeInfo == null) {
            return null;
        }
        List findAccessibilityNodeInfosByViewId = accessibilityNodeInfo.findAccessibilityNodeInfosByViewId("com.android.chrome:id/back_button");
        if (findAccessibilityNodeInfosByViewId == null || findAccessibilityNodeInfosByViewId.isEmpty()) {
            return null;
        }
        if (findAccessibilityNodeInfosByViewId.size() > 1) {
            ao.a("Multiple back buttons, wth!");
        }
        ao.a("Diz back button, I like it!");
        return (AccessibilityNodeInfo) findAccessibilityNodeInfosByViewId.get(0);
    }
}
