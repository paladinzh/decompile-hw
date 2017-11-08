package com.android.contacts.widget;

import android.view.MenuItem;
import android.view.View.OnCreateContextMenuListener;

public interface ContextMenuAdapter extends OnCreateContextMenuListener {
    boolean onContextItemSelected(MenuItem menuItem);
}
