package ucd.apps.Demos;

import ucd.ui.framework.core.GLBase;
import ucd.ui.framework.core.Group;

public abstract class BaseEffectView {
    public abstract void createView(GLBase gLBase, Group group);

    public void destroyView(GLBase base, Group group) {
    }
}
