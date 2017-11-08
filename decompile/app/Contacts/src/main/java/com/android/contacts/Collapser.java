package com.android.contacts;

import java.util.Iterator;
import java.util.List;

public final class Collapser {

    public interface Collapsible<T> {
        boolean collapseWith(T t);

        boolean shouldCollapseWith(T t);
    }

    private Collapser() {
    }

    public static <T extends Collapsible<T>> void collapseList(List<T> list) {
        collapseList(list, true);
    }

    public static <T extends Collapsible<T>> void collapseList(List<T> list, boolean isLimitSize) {
        int listSize = list.size();
        if (!isLimitSize || listSize <= 20) {
            for (int i = 0; i < listSize; i++) {
                Collapsible iItem = (Collapsible) list.get(i);
                if (iItem != null) {
                    for (int j = i + 1; j < listSize; j++) {
                        Collapsible jItem = (Collapsible) list.get(j);
                        if (jItem != null) {
                            if (iItem.shouldCollapseWith(jItem)) {
                                iItem.collapseWith(jItem);
                                list.set(j, null);
                            } else if (jItem.shouldCollapseWith(iItem)) {
                                jItem.collapseWith(iItem);
                                list.set(i, null);
                                break;
                            }
                        }
                    }
                }
            }
            Iterator<T> itr = list.iterator();
            while (itr.hasNext()) {
                if (itr.next() == null) {
                    itr.remove();
                }
            }
        }
    }
}
