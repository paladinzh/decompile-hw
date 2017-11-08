package ucd.ui.framework.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class ChildrenUtil {
    private static final Comparator<GLObject> sortMethod = new Comparator<GLObject>() {
        public int compare(GLObject a, GLObject b) {
            return Float.compare(a.getZ(), b.getZ());
        }
    };

    /* renamed from: ucd.ui.framework.core.ChildrenUtil$2 */
    class AnonymousClass2 implements Runnable {
        private final /* synthetic */ GLObject val$glObj;

        AnonymousClass2(GLObject gLObject) {
            this.val$glObj = gLObject;
        }

        public void run() {
            this.val$glObj.del();
        }
    }

    protected static void add(ParentImp parent, GLObject glObj, int index) {
        if (parent == null || glObj == null) {
            if (index < 0 || index > parent.getChildrenCount()) {
                throw new RuntimeException("index out of the bounds");
            }
        } else if (glObj.getParent() == null) {
            ArrayList<GLObject> list;
            if (parent instanceof GLBase) {
                list = ((GLBase) parent).list;
            } else {
                list = ((Group) parent).list;
            }
            list.add(index, glObj);
            order(list);
            glObj.setParent(parent);
            if (parent instanceof Group) {
                ((Group) parent).setDirty();
            }
            parent.requestRender();
        } else {
            throw new RuntimeException("param has been added to DOM.");
        }
    }

    protected static final void order(ArrayList<GLObject> list) {
        if (list != null && list.size() > 0) {
            Collections.sort(list, sortMethod);
        }
    }

    protected static GLObject del(ParentImp parent, GLObject glObj, boolean autoDestroyed) {
        if (parent == null || glObj == null) {
            return glObj;
        }
        boolean success;
        if (parent instanceof GLBase) {
            success = ((GLBase) parent).list.remove(glObj);
        } else {
            success = ((Group) parent).list.remove(glObj);
        }
        if (success) {
            if (autoDestroyed) {
                GLBase root;
                if (parent instanceof GLBase) {
                    root = (GLBase) parent;
                } else {
                    root = ((Group) parent).root;
                }
                root.queueEvent(new AnonymousClass2(glObj));
            }
            glObj.setParent(null);
            if (parent instanceof Group) {
                ((Group) parent).setDirty();
            }
            parent.requestRender();
        }
        return glObj;
    }

    protected static GLObject _findByPos(ArrayList<GLObject> list, float[] pos, boolean findSmallest, boolean includeDisabledItems) {
        if (pos == null || pos.length != 2) {
            return null;
        }
        float x = pos[0];
        float y = pos[1];
        for (int i = list.size() - 1; i >= 0; i--) {
            pos[0] = x;
            pos[1] = y;
            GLObject item = (GLObject) list.get(i);
            if (!includeDisabledItems) {
                if (!item.isEnabled()) {
                    continue;
                }
            }
            if (item.isVisible() && item.inRect(pos, false)) {
                if ((item instanceof Group) && findSmallest) {
                    return ((Group) item).findByPos(pos, findSmallest, includeDisabledItems);
                }
                return item;
            }
        }
        return null;
    }
}
