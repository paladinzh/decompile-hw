package ucd.ui.framework.core;

public interface ParentImp {
    void del(GLObject gLObject);

    int getChildrenCount();

    void requestRender();
}
