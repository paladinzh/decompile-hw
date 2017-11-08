package android.support.v4.widget;

import android.widget.SearchView;

public class SearchViewCompatIcs$MySearchView extends SearchView {
    public void onActionViewCollapsed() {
        setQuery("", false);
        super.onActionViewCollapsed();
    }
}
