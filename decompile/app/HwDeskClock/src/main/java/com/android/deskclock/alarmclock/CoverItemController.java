package com.android.deskclock.alarmclock;

import android.content.Context;
import android.view.View;
import com.android.util.Log;
import java.lang.reflect.InvocationTargetException;

public class CoverItemController {
    private static CoverItemController sController;
    private boolean mCoverAdded = false;
    private View mCoverItemView;
    private Class<?> mCoverManagerClass;
    private Object mCoverManagerObject;

    public static synchronized CoverItemController getInstance(Context context) {
        CoverItemController coverItemController;
        synchronized (CoverItemController.class) {
            if (sController == null) {
                sController = new CoverItemController(context);
            }
            coverItemController = sController;
        }
        return coverItemController;
    }

    private CoverItemController(Context context) {
        Log.i("CoverItemController", "CoverItemController intialized");
        init();
    }

    private void init() {
        try {
            this.mCoverManagerClass = Class.forName("android.cover.CoverManager");
            this.mCoverManagerObject = this.mCoverManagerClass.newInstance();
        } catch (ClassNotFoundException e) {
            Log.e("CoverItemController", "init : ClassNotFoundException = " + e.getMessage());
        } catch (InstantiationException e2) {
            Log.e("CoverItemController", "init : InstantiationException = " + e2.getMessage());
        } catch (IllegalAccessException e3) {
            Log.e("CoverItemController", "init : IllegalAccessException = " + e3.getMessage());
        } catch (Exception e4) {
            Log.e("CoverItemController", "init : Exception = " + e4.getMessage());
        }
    }

    public boolean isCoverOpen() {
        Boolean coverOpen = Boolean.valueOf(true);
        try {
            coverOpen = (Boolean) this.mCoverManagerClass.getMethod("isCoverOpen", new Class[0]).invoke(this.mCoverManagerObject, new Object[0]);
        } catch (NoSuchMethodException e) {
            Log.e("CoverItemController", "isCoverOpen : NoSuchMethodException = " + e.getMessage());
        } catch (IllegalArgumentException e2) {
            Log.e("CoverItemController", "isCoverOpen : IllegalArgumentException = " + e2.getMessage());
        } catch (IllegalAccessException e3) {
            Log.e("CoverItemController", "isCoverOpen : IllegalAccessException = " + e3.getMessage());
        } catch (InvocationTargetException e4) {
            Log.e("CoverItemController", "isCoverOpen : InvocationTargetException = " + e4.getMessage());
        } catch (Exception e5) {
            Log.e("CoverItemController", "isCoverOpen : Exception = " + e5.getMessage());
        }
        return coverOpen.booleanValue();
    }

    public void addCoverItem(View view, boolean isStateBar) {
        Log.i("CoverItemController", "addCoverItem : view = " + view);
        if (!this.mCoverAdded && view != null && view.getParent() == null && !isCoverOpen()) {
            try {
                this.mCoverManagerClass.getMethod("addCoverItemView", new Class[]{View.class, Boolean.TYPE}).invoke(this.mCoverManagerObject, new Object[]{view, Boolean.valueOf(isStateBar)});
                this.mCoverAdded = true;
                this.mCoverItemView = view;
            } catch (NoSuchMethodException e) {
                Log.e("CoverItemController", "addCoverItem : NoSuchMethodException = " + e.getMessage());
            } catch (IllegalArgumentException e2) {
                Log.e("CoverItemController", "addCoverItem : IllegalArgumentException = " + e2.getMessage());
            } catch (IllegalAccessException e3) {
                Log.e("CoverItemController", "addCoverItem : IllegalAccessException = " + e3.getMessage());
            } catch (InvocationTargetException e4) {
                Log.e("CoverItemController", "addCoverItem : InvocationTargetException = " + e4.getMessage());
            }
        }
    }

    public void removeCoverItem() {
        Log.i("CoverItemController", "removeCoverItem : mCoverItemView = " + this.mCoverItemView);
        if (this.mCoverAdded && this.mCoverItemView != null && this.mCoverItemView.getParent() != null) {
            try {
                this.mCoverManagerClass.getMethod("removeCoverItemView", new Class[]{View.class}).invoke(this.mCoverManagerObject, new Object[]{this.mCoverItemView});
                this.mCoverAdded = false;
                this.mCoverItemView = null;
            } catch (NoSuchMethodException e) {
                Log.e("CoverItemController", "removeCoverItem : NoSuchMethodException = " + e.getMessage());
            } catch (IllegalArgumentException e2) {
                Log.e("CoverItemController", "removeCoverItem : IllegalArgumentException = " + e2.getMessage());
            } catch (IllegalAccessException e3) {
                Log.e("CoverItemController", "removeCoverItem : IllegalAccessException = " + e3.getMessage());
            } catch (InvocationTargetException e4) {
                Log.e("CoverItemController", "removeCoverItem : InvocationTargetException = " + e4.getMessage());
            } catch (Exception e5) {
                Log.e("CoverItemController", "removeCoverItem : Exception = " + e5.getMessage());
            }
        }
    }
}
