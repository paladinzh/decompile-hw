package android.support.v4.app;

public abstract class FragmentTransaction {
    public abstract FragmentTransaction add(Fragment fragment, String str);

    public abstract int commit();

    public abstract int commitAllowingStateLoss();

    public abstract FragmentTransaction remove(Fragment fragment);
}
