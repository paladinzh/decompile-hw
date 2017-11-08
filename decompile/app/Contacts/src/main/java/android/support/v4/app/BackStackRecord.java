package android.support.v4.app;

import android.os.Build.VERSION;
import android.support.v4.app.FragmentTransitionCompat21.EpicenterView;
import android.support.v4.app.FragmentTransitionCompat21.ViewRetriever;
import android.support.v4.util.ArrayMap;
import android.support.v4.util.LogWriter;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnPreDrawListener;
import com.android.contacts.hap.sprint.preload.HwCustPreloadContacts;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Map;

final class BackStackRecord extends FragmentTransaction implements Runnable {
    static final boolean SUPPORTS_TRANSITIONS = (VERSION.SDK_INT >= 21);
    boolean mAddToBackStack;
    boolean mAllowAddToBackStack = true;
    int mBreadCrumbShortTitleRes;
    CharSequence mBreadCrumbShortTitleText;
    int mBreadCrumbTitleRes;
    CharSequence mBreadCrumbTitleText;
    boolean mCommitted;
    int mEnterAnim;
    int mExitAnim;
    Op mHead;
    int mIndex = -1;
    final FragmentManagerImpl mManager;
    String mName;
    int mNumOp;
    int mPopEnterAnim;
    int mPopExitAnim;
    ArrayList<String> mSharedElementSourceNames;
    ArrayList<String> mSharedElementTargetNames;
    Op mTail;
    int mTransition;
    int mTransitionStyle;

    static final class Op {
        int cmd;
        int enterAnim;
        int exitAnim;
        Fragment fragment;
        Op next;
        int popEnterAnim;
        int popExitAnim;
        Op prev;
        ArrayList<Fragment> removed;

        Op() {
        }
    }

    public class TransitionState {
        public EpicenterView enteringEpicenterView = new EpicenterView();
        public ArrayList<View> hiddenFragmentViews = new ArrayList();
        public ArrayMap<String, String> nameOverrides = new ArrayMap();
        public View nonExistentView;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(128);
        sb.append("BackStackEntry{");
        sb.append(Integer.toHexString(System.identityHashCode(this)));
        if (this.mIndex >= 0) {
            sb.append(" #");
            sb.append(this.mIndex);
        }
        if (this.mName != null) {
            sb.append(HwCustPreloadContacts.EMPTY_STRING);
            sb.append(this.mName);
        }
        sb.append("}");
        return sb.toString();
    }

    public void dump(String prefix, FileDescriptor fd, PrintWriter writer, String[] args) {
        dump(prefix, writer, true);
    }

    public void dump(String prefix, PrintWriter writer, boolean full) {
        if (full) {
            writer.print(prefix);
            writer.print("mName=");
            writer.print(this.mName);
            writer.print(" mIndex=");
            writer.print(this.mIndex);
            writer.print(" mCommitted=");
            writer.println(this.mCommitted);
            if (this.mTransition != 0) {
                writer.print(prefix);
                writer.print("mTransition=#");
                writer.print(Integer.toHexString(this.mTransition));
                writer.print(" mTransitionStyle=#");
                writer.println(Integer.toHexString(this.mTransitionStyle));
            }
            if (!(this.mEnterAnim == 0 && this.mExitAnim == 0)) {
                writer.print(prefix);
                writer.print("mEnterAnim=#");
                writer.print(Integer.toHexString(this.mEnterAnim));
                writer.print(" mExitAnim=#");
                writer.println(Integer.toHexString(this.mExitAnim));
            }
            if (!(this.mPopEnterAnim == 0 && this.mPopExitAnim == 0)) {
                writer.print(prefix);
                writer.print("mPopEnterAnim=#");
                writer.print(Integer.toHexString(this.mPopEnterAnim));
                writer.print(" mPopExitAnim=#");
                writer.println(Integer.toHexString(this.mPopExitAnim));
            }
            if (!(this.mBreadCrumbTitleRes == 0 && this.mBreadCrumbTitleText == null)) {
                writer.print(prefix);
                writer.print("mBreadCrumbTitleRes=#");
                writer.print(Integer.toHexString(this.mBreadCrumbTitleRes));
                writer.print(" mBreadCrumbTitleText=");
                writer.println(this.mBreadCrumbTitleText);
            }
            if (!(this.mBreadCrumbShortTitleRes == 0 && this.mBreadCrumbShortTitleText == null)) {
                writer.print(prefix);
                writer.print("mBreadCrumbShortTitleRes=#");
                writer.print(Integer.toHexString(this.mBreadCrumbShortTitleRes));
                writer.print(" mBreadCrumbShortTitleText=");
                writer.println(this.mBreadCrumbShortTitleText);
            }
        }
        if (this.mHead != null) {
            writer.print(prefix);
            writer.println("Operations:");
            String innerPrefix = prefix + "    ";
            Op op = this.mHead;
            int num = 0;
            while (op != null) {
                String cmdStr;
                switch (op.cmd) {
                    case 0:
                        cmdStr = "NULL";
                        break;
                    case 1:
                        cmdStr = "ADD";
                        break;
                    case 2:
                        cmdStr = "REPLACE";
                        break;
                    case 3:
                        cmdStr = "REMOVE";
                        break;
                    case 4:
                        cmdStr = "HIDE";
                        break;
                    case 5:
                        cmdStr = "SHOW";
                        break;
                    case 6:
                        cmdStr = "DETACH";
                        break;
                    case 7:
                        cmdStr = "ATTACH";
                        break;
                    default:
                        cmdStr = "cmd=" + op.cmd;
                        break;
                }
                writer.print(prefix);
                writer.print("  Op #");
                writer.print(num);
                writer.print(": ");
                writer.print(cmdStr);
                writer.print(HwCustPreloadContacts.EMPTY_STRING);
                writer.println(op.fragment);
                if (full) {
                    if (!(op.enterAnim == 0 && op.exitAnim == 0)) {
                        writer.print(prefix);
                        writer.print("enterAnim=#");
                        writer.print(Integer.toHexString(op.enterAnim));
                        writer.print(" exitAnim=#");
                        writer.println(Integer.toHexString(op.exitAnim));
                    }
                    if (!(op.popEnterAnim == 0 && op.popExitAnim == 0)) {
                        writer.print(prefix);
                        writer.print("popEnterAnim=#");
                        writer.print(Integer.toHexString(op.popEnterAnim));
                        writer.print(" popExitAnim=#");
                        writer.println(Integer.toHexString(op.popExitAnim));
                    }
                }
                if (op.removed != null && op.removed.size() > 0) {
                    for (int i = 0; i < op.removed.size(); i++) {
                        writer.print(innerPrefix);
                        if (op.removed.size() == 1) {
                            writer.print("Removed: ");
                        } else {
                            if (i == 0) {
                                writer.println("Removed:");
                            }
                            writer.print(innerPrefix);
                            writer.print("  #");
                            writer.print(i);
                            writer.print(": ");
                        }
                        writer.println(op.removed.get(i));
                    }
                }
                op = op.next;
                num++;
            }
        }
    }

    public BackStackRecord(FragmentManagerImpl manager) {
        this.mManager = manager;
    }

    void addOp(Op op) {
        if (this.mHead == null) {
            this.mTail = op;
            this.mHead = op;
        } else {
            op.prev = this.mTail;
            this.mTail.next = op;
            this.mTail = op;
        }
        op.enterAnim = this.mEnterAnim;
        op.exitAnim = this.mExitAnim;
        op.popEnterAnim = this.mPopEnterAnim;
        op.popExitAnim = this.mPopExitAnim;
        this.mNumOp++;
    }

    public FragmentTransaction add(Fragment fragment, String tag) {
        doAddOp(0, fragment, tag, 1);
        return this;
    }

    private void doAddOp(int containerViewId, Fragment fragment, String tag, int opcmd) {
        fragment.mFragmentManager = this.mManager;
        if (tag != null) {
            if (fragment.mTag == null || tag.equals(fragment.mTag)) {
                fragment.mTag = tag;
            } else {
                throw new IllegalStateException("Can't change tag of fragment " + fragment + ": was " + fragment.mTag + " now " + tag);
            }
        }
        if (containerViewId != 0) {
            if (containerViewId == -1) {
                throw new IllegalArgumentException("Can't add fragment " + fragment + " with tag " + tag + " to container view with no id");
            } else if (fragment.mFragmentId == 0 || fragment.mFragmentId == containerViewId) {
                fragment.mFragmentId = containerViewId;
                fragment.mContainerId = containerViewId;
            } else {
                throw new IllegalStateException("Can't change container ID of fragment " + fragment + ": was " + fragment.mFragmentId + " now " + containerViewId);
            }
        }
        Op op = new Op();
        op.cmd = opcmd;
        op.fragment = fragment;
        addOp(op);
    }

    public FragmentTransaction remove(Fragment fragment) {
        Op op = new Op();
        op.cmd = 3;
        op.fragment = fragment;
        addOp(op);
        return this;
    }

    void bumpBackStackNesting(int amt) {
        if (this.mAddToBackStack) {
            if (FragmentManagerImpl.DEBUG) {
                Log.v("FragmentManager", "Bump nesting in " + this + " by " + amt);
            }
            for (Op op = this.mHead; op != null; op = op.next) {
                if (op.fragment != null) {
                    Fragment fragment = op.fragment;
                    fragment.mBackStackNesting += amt;
                    if (FragmentManagerImpl.DEBUG) {
                        Log.v("FragmentManager", "Bump nesting of " + op.fragment + " to " + op.fragment.mBackStackNesting);
                    }
                }
                if (op.removed != null) {
                    for (int i = op.removed.size() - 1; i >= 0; i--) {
                        Fragment r = (Fragment) op.removed.get(i);
                        r.mBackStackNesting += amt;
                        if (FragmentManagerImpl.DEBUG) {
                            Log.v("FragmentManager", "Bump nesting of " + r + " to " + r.mBackStackNesting);
                        }
                    }
                }
            }
        }
    }

    public int commit() {
        return commitInternal(false);
    }

    public int commitAllowingStateLoss() {
        return commitInternal(true);
    }

    int commitInternal(boolean allowStateLoss) {
        if (this.mCommitted) {
            throw new IllegalStateException("commit already called");
        }
        if (FragmentManagerImpl.DEBUG) {
            Log.v("FragmentManager", "Commit: " + this);
            dump("  ", null, new PrintWriter(new LogWriter("FragmentManager")), null);
        }
        this.mCommitted = true;
        if (this.mAddToBackStack) {
            this.mIndex = this.mManager.allocBackStackIndex(this);
        } else {
            this.mIndex = -1;
        }
        this.mManager.enqueueAction(this, allowStateLoss);
        return this.mIndex;
    }

    public void run() {
        if (FragmentManagerImpl.DEBUG) {
            Log.v("FragmentManager", "Run: " + this);
        }
        if (!this.mAddToBackStack || this.mIndex >= 0) {
            bumpBackStackNesting(1);
            TransitionState state = null;
            if (SUPPORTS_TRANSITIONS && this.mManager.mCurState >= 1) {
                SparseArray<Fragment> firstOutFragments = new SparseArray();
                SparseArray<Fragment> lastInFragments = new SparseArray();
                calculateFragments(firstOutFragments, lastInFragments);
                state = beginTransition(firstOutFragments, lastInFragments, false);
            }
            int transitionStyle = state != null ? 0 : this.mTransitionStyle;
            int transition = state != null ? 0 : this.mTransition;
            Op op = this.mHead;
            while (op != null) {
                int enterAnim = state != null ? 0 : op.enterAnim;
                int exitAnim = state != null ? 0 : op.exitAnim;
                Fragment f;
                switch (op.cmd) {
                    case 1:
                        f = op.fragment;
                        f.mNextAnim = enterAnim;
                        this.mManager.addFragment(f, false);
                        break;
                    case 2:
                        f = op.fragment;
                        int containerId = f.mContainerId;
                        if (this.mManager.mAdded != null) {
                            for (int i = this.mManager.mAdded.size() - 1; i >= 0; i--) {
                                Fragment old = (Fragment) this.mManager.mAdded.get(i);
                                if (FragmentManagerImpl.DEBUG) {
                                    Log.v("FragmentManager", "OP_REPLACE: adding=" + f + " old=" + old);
                                }
                                if (old.mContainerId == containerId) {
                                    if (old == f) {
                                        f = null;
                                        op.fragment = null;
                                    } else {
                                        if (op.removed == null) {
                                            op.removed = new ArrayList();
                                        }
                                        op.removed.add(old);
                                        old.mNextAnim = exitAnim;
                                        if (this.mAddToBackStack) {
                                            old.mBackStackNesting++;
                                            if (FragmentManagerImpl.DEBUG) {
                                                Log.v("FragmentManager", "Bump nesting of " + old + " to " + old.mBackStackNesting);
                                            }
                                        }
                                        this.mManager.removeFragment(old, transition, transitionStyle);
                                    }
                                }
                            }
                        }
                        if (f == null) {
                            break;
                        }
                        f.mNextAnim = enterAnim;
                        this.mManager.addFragment(f, false);
                        break;
                    case 3:
                        f = op.fragment;
                        f.mNextAnim = exitAnim;
                        this.mManager.removeFragment(f, transition, transitionStyle);
                        break;
                    case 4:
                        f = op.fragment;
                        f.mNextAnim = exitAnim;
                        this.mManager.hideFragment(f, transition, transitionStyle);
                        break;
                    case 5:
                        f = op.fragment;
                        f.mNextAnim = enterAnim;
                        this.mManager.showFragment(f, transition, transitionStyle);
                        break;
                    case 6:
                        f = op.fragment;
                        f.mNextAnim = exitAnim;
                        this.mManager.detachFragment(f, transition, transitionStyle);
                        break;
                    case 7:
                        f = op.fragment;
                        f.mNextAnim = enterAnim;
                        this.mManager.attachFragment(f, transition, transitionStyle);
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown cmd: " + op.cmd);
                }
                op = op.next;
            }
            this.mManager.moveToState(this.mManager.mCurState, transition, transitionStyle, true);
            if (this.mAddToBackStack) {
                this.mManager.addBackStackState(this);
                return;
            }
            return;
        }
        throw new IllegalStateException("addToBackStack() called after commit()");
    }

    private static void setFirstOut(SparseArray<Fragment> firstOutFragments, SparseArray<Fragment> lastInFragments, Fragment fragment) {
        if (fragment != null) {
            int containerId = fragment.mContainerId;
            if (containerId != 0 && !fragment.isHidden()) {
                if (fragment.isAdded() && fragment.getView() != null && firstOutFragments.get(containerId) == null) {
                    firstOutFragments.put(containerId, fragment);
                }
                if (lastInFragments.get(containerId) == fragment) {
                    lastInFragments.remove(containerId);
                }
            }
        }
    }

    private void setLastIn(SparseArray<Fragment> firstOutFragments, SparseArray<Fragment> lastInFragments, Fragment fragment) {
        if (fragment != null) {
            int containerId = fragment.mContainerId;
            if (containerId != 0) {
                if (!fragment.isAdded()) {
                    lastInFragments.put(containerId, fragment);
                }
                if (firstOutFragments.get(containerId) == fragment) {
                    firstOutFragments.remove(containerId);
                }
            }
            if (fragment.mState < 1 && this.mManager.mCurState >= 1) {
                this.mManager.makeActive(fragment);
                this.mManager.moveToState(fragment, 1, 0, 0, false);
            }
        }
    }

    private void calculateFragments(SparseArray<Fragment> firstOutFragments, SparseArray<Fragment> lastInFragments) {
        if (this.mManager.mContainer.onHasView()) {
            for (Op op = this.mHead; op != null; op = op.next) {
                switch (op.cmd) {
                    case 1:
                        setLastIn(firstOutFragments, lastInFragments, op.fragment);
                        break;
                    case 2:
                        Fragment f = op.fragment;
                        if (this.mManager.mAdded != null) {
                            for (int i = 0; i < this.mManager.mAdded.size(); i++) {
                                Fragment old = (Fragment) this.mManager.mAdded.get(i);
                                if (f == null || old.mContainerId == f.mContainerId) {
                                    if (old == f) {
                                        f = null;
                                        lastInFragments.remove(old.mContainerId);
                                    } else {
                                        setFirstOut(firstOutFragments, lastInFragments, old);
                                    }
                                }
                            }
                        }
                        setLastIn(firstOutFragments, lastInFragments, op.fragment);
                        break;
                    case 3:
                        setFirstOut(firstOutFragments, lastInFragments, op.fragment);
                        break;
                    case 4:
                        setFirstOut(firstOutFragments, lastInFragments, op.fragment);
                        break;
                    case 5:
                        setLastIn(firstOutFragments, lastInFragments, op.fragment);
                        break;
                    case 6:
                        setFirstOut(firstOutFragments, lastInFragments, op.fragment);
                        break;
                    case 7:
                        setLastIn(firstOutFragments, lastInFragments, op.fragment);
                        break;
                    default:
                        break;
                }
            }
        }
    }

    public void calculateBackFragments(SparseArray<Fragment> firstOutFragments, SparseArray<Fragment> lastInFragments) {
        if (this.mManager.mContainer.onHasView()) {
            for (Op op = this.mTail; op != null; op = op.prev) {
                switch (op.cmd) {
                    case 1:
                        setFirstOut(firstOutFragments, lastInFragments, op.fragment);
                        break;
                    case 2:
                        if (op.removed != null) {
                            for (int i = op.removed.size() - 1; i >= 0; i--) {
                                setLastIn(firstOutFragments, lastInFragments, (Fragment) op.removed.get(i));
                            }
                        }
                        setFirstOut(firstOutFragments, lastInFragments, op.fragment);
                        break;
                    case 3:
                        setLastIn(firstOutFragments, lastInFragments, op.fragment);
                        break;
                    case 4:
                        setLastIn(firstOutFragments, lastInFragments, op.fragment);
                        break;
                    case 5:
                        setFirstOut(firstOutFragments, lastInFragments, op.fragment);
                        break;
                    case 6:
                        setLastIn(firstOutFragments, lastInFragments, op.fragment);
                        break;
                    case 7:
                        setFirstOut(firstOutFragments, lastInFragments, op.fragment);
                        break;
                    default:
                        break;
                }
            }
        }
    }

    public TransitionState popFromBackStack(boolean doStateMove, TransitionState state, SparseArray<Fragment> firstOutFragments, SparseArray<Fragment> lastInFragments) {
        if (FragmentManagerImpl.DEBUG) {
            Log.v("FragmentManager", "popFromBackStack: " + this);
            dump("  ", null, new PrintWriter(new LogWriter("FragmentManager")), null);
        }
        if (SUPPORTS_TRANSITIONS && this.mManager.mCurState >= 1) {
            if (state == null) {
                if (!(firstOutFragments.size() == 0 && lastInFragments.size() == 0)) {
                    state = beginTransition(firstOutFragments, lastInFragments, true);
                }
            } else if (!doStateMove) {
                setNameOverrides(state, this.mSharedElementTargetNames, this.mSharedElementSourceNames);
            }
        }
        bumpBackStackNesting(-1);
        int transitionStyle = state != null ? 0 : this.mTransitionStyle;
        int transition = state != null ? 0 : this.mTransition;
        Op op = this.mTail;
        while (op != null) {
            int popEnterAnim = state != null ? 0 : op.popEnterAnim;
            int popExitAnim = state != null ? 0 : op.popExitAnim;
            Fragment f;
            switch (op.cmd) {
                case 1:
                    f = op.fragment;
                    f.mNextAnim = popExitAnim;
                    this.mManager.removeFragment(f, FragmentManagerImpl.reverseTransit(transition), transitionStyle);
                    break;
                case 2:
                    f = op.fragment;
                    if (f != null) {
                        f.mNextAnim = popExitAnim;
                        this.mManager.removeFragment(f, FragmentManagerImpl.reverseTransit(transition), transitionStyle);
                    }
                    if (op.removed == null) {
                        break;
                    }
                    for (int i = 0; i < op.removed.size(); i++) {
                        Fragment old = (Fragment) op.removed.get(i);
                        old.mNextAnim = popEnterAnim;
                        this.mManager.addFragment(old, false);
                    }
                    break;
                case 3:
                    f = op.fragment;
                    f.mNextAnim = popEnterAnim;
                    this.mManager.addFragment(f, false);
                    break;
                case 4:
                    f = op.fragment;
                    f.mNextAnim = popEnterAnim;
                    this.mManager.showFragment(f, FragmentManagerImpl.reverseTransit(transition), transitionStyle);
                    break;
                case 5:
                    f = op.fragment;
                    f.mNextAnim = popExitAnim;
                    this.mManager.hideFragment(f, FragmentManagerImpl.reverseTransit(transition), transitionStyle);
                    break;
                case 6:
                    f = op.fragment;
                    f.mNextAnim = popEnterAnim;
                    this.mManager.attachFragment(f, FragmentManagerImpl.reverseTransit(transition), transitionStyle);
                    break;
                case 7:
                    f = op.fragment;
                    f.mNextAnim = popEnterAnim;
                    this.mManager.detachFragment(f, FragmentManagerImpl.reverseTransit(transition), transitionStyle);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown cmd: " + op.cmd);
            }
            op = op.prev;
        }
        if (doStateMove) {
            this.mManager.moveToState(this.mManager.mCurState, FragmentManagerImpl.reverseTransit(transition), transitionStyle, true);
            state = null;
        }
        if (this.mIndex >= 0) {
            this.mManager.freeBackStackIndex(this.mIndex);
            this.mIndex = -1;
        }
        return state;
    }

    public String getName() {
        return this.mName;
    }

    private TransitionState beginTransition(SparseArray<Fragment> firstOutFragments, SparseArray<Fragment> lastInFragments, boolean isBack) {
        int i;
        TransitionState state = new TransitionState();
        state.nonExistentView = new View(this.mManager.mHost.getContext());
        boolean anyTransitionStarted = false;
        for (i = 0; i < firstOutFragments.size(); i++) {
            if (configureTransitions(firstOutFragments.keyAt(i), state, isBack, firstOutFragments, lastInFragments)) {
                anyTransitionStarted = true;
            }
        }
        for (i = 0; i < lastInFragments.size(); i++) {
            int containerId = lastInFragments.keyAt(i);
            if (firstOutFragments.get(containerId) == null && configureTransitions(containerId, state, isBack, firstOutFragments, lastInFragments)) {
                anyTransitionStarted = true;
            }
        }
        if (anyTransitionStarted) {
            return state;
        }
        return null;
    }

    private static Object getEnterTransition(Fragment inFragment, boolean isBack) {
        if (inFragment == null) {
            return null;
        }
        return FragmentTransitionCompat21.cloneTransition(isBack ? inFragment.getReenterTransition() : inFragment.getEnterTransition());
    }

    private static Object getExitTransition(Fragment outFragment, boolean isBack) {
        if (outFragment == null) {
            return null;
        }
        return FragmentTransitionCompat21.cloneTransition(isBack ? outFragment.getReturnTransition() : outFragment.getExitTransition());
    }

    private static Object getSharedElementTransition(Fragment inFragment, Fragment outFragment, boolean isBack) {
        if (inFragment == null || outFragment == null) {
            return null;
        }
        Object sharedElementReturnTransition;
        if (isBack) {
            sharedElementReturnTransition = outFragment.getSharedElementReturnTransition();
        } else {
            sharedElementReturnTransition = inFragment.getSharedElementEnterTransition();
        }
        return FragmentTransitionCompat21.wrapSharedElementTransition(sharedElementReturnTransition);
    }

    private static Object captureExitingViews(Object exitTransition, Fragment outFragment, ArrayList<View> exitingViews, ArrayMap<String, View> namedViews, View nonExistentView) {
        if (exitTransition != null) {
            return FragmentTransitionCompat21.captureExitingViews(exitTransition, outFragment.getView(), exitingViews, namedViews, nonExistentView);
        }
        return exitTransition;
    }

    private ArrayMap<String, View> remapSharedElements(TransitionState state, Fragment outFragment, boolean isBack) {
        ArrayMap namedViews = new ArrayMap();
        if (this.mSharedElementSourceNames != null) {
            FragmentTransitionCompat21.findNamedViews(namedViews, outFragment.getView());
            if (isBack) {
                namedViews.retainAll(this.mSharedElementTargetNames);
            } else {
                namedViews = remapNames(this.mSharedElementSourceNames, this.mSharedElementTargetNames, namedViews);
            }
        }
        if (isBack) {
            if (outFragment.mEnterTransitionCallback != null) {
                outFragment.mEnterTransitionCallback.onMapSharedElements(this.mSharedElementTargetNames, namedViews);
            }
            setBackNameOverrides(state, namedViews, false);
        } else {
            if (outFragment.mExitTransitionCallback != null) {
                outFragment.mExitTransitionCallback.onMapSharedElements(this.mSharedElementTargetNames, namedViews);
            }
            setNameOverrides(state, namedViews, false);
        }
        return namedViews;
    }

    private boolean configureTransitions(int containerId, TransitionState state, boolean isBack, SparseArray<Fragment> firstOutFragments, SparseArray<Fragment> lastInFragments) {
        View sceneRoot = (ViewGroup) this.mManager.mContainer.onFindViewById(containerId);
        if (sceneRoot == null) {
            return false;
        }
        final Fragment inFragment = (Fragment) lastInFragments.get(containerId);
        Fragment outFragment = (Fragment) firstOutFragments.get(containerId);
        Object enterTransition = getEnterTransition(inFragment, isBack);
        Object sharedElementTransition = getSharedElementTransition(inFragment, outFragment, isBack);
        Object exitTransition = getExitTransition(outFragment, isBack);
        Map map = null;
        ArrayList<View> sharedElementTargets = new ArrayList();
        if (sharedElementTransition != null) {
            map = remapSharedElements(state, outFragment, isBack);
            if (map.isEmpty()) {
                sharedElementTransition = null;
                map = null;
            } else {
                SharedElementCallback callback;
                if (isBack) {
                    callback = outFragment.mEnterTransitionCallback;
                } else {
                    callback = inFragment.mEnterTransitionCallback;
                }
                if (callback != null) {
                    callback.onSharedElementStart(new ArrayList(map.keySet()), new ArrayList(map.values()), null);
                }
                prepareSharedElementTransition(state, sceneRoot, sharedElementTransition, inFragment, outFragment, isBack, sharedElementTargets, enterTransition, exitTransition);
            }
        }
        if (enterTransition == null && sharedElementTransition == null && exitTransition == null) {
            return false;
        }
        boolean z;
        ArrayList<View> exitingViews = new ArrayList();
        exitTransition = captureExitingViews(exitTransition, outFragment, exitingViews, map, state.nonExistentView);
        if (!(this.mSharedElementTargetNames == null || map == null)) {
            View epicenterView = (View) map.get(this.mSharedElementTargetNames.get(0));
            if (epicenterView != null) {
                if (exitTransition != null) {
                    FragmentTransitionCompat21.setEpicenter(exitTransition, epicenterView);
                }
                if (sharedElementTransition != null) {
                    FragmentTransitionCompat21.setEpicenter(sharedElementTransition, epicenterView);
                }
            }
        }
        ViewRetriever anonymousClass1 = new ViewRetriever() {
            public View getView() {
                return inFragment.getView();
            }
        };
        ArrayList<View> enteringViews = new ArrayList();
        ArrayMap<String, View> renamedViews = new ArrayMap();
        boolean allowOverlap = true;
        if (inFragment != null) {
            if (isBack) {
                allowOverlap = inFragment.getAllowReturnTransitionOverlap();
            } else {
                allowOverlap = inFragment.getAllowEnterTransitionOverlap();
            }
        }
        Object transition = FragmentTransitionCompat21.mergeTransitions(enterTransition, exitTransition, sharedElementTransition, allowOverlap);
        if (transition != null) {
            FragmentTransitionCompat21.addTransitionTargets(enterTransition, sharedElementTransition, exitTransition, sceneRoot, anonymousClass1, state.nonExistentView, state.enteringEpicenterView, state.nameOverrides, enteringViews, exitingViews, map, renamedViews, sharedElementTargets);
            excludeHiddenFragmentsAfterEnter(sceneRoot, state, containerId, transition);
            FragmentTransitionCompat21.excludeTarget(transition, state.nonExistentView, true);
            excludeHiddenFragments(state, containerId, transition);
            FragmentTransitionCompat21.beginDelayedTransition(sceneRoot, transition);
            FragmentTransitionCompat21.cleanupTransitions(sceneRoot, state.nonExistentView, enterTransition, enteringViews, exitTransition, exitingViews, sharedElementTransition, sharedElementTargets, transition, state.hiddenFragmentViews, renamedViews);
        }
        if (transition != null) {
            z = true;
        } else {
            z = false;
        }
        return z;
    }

    private void prepareSharedElementTransition(TransitionState state, View sceneRoot, Object sharedElementTransition, Fragment inFragment, Fragment outFragment, boolean isBack, ArrayList<View> sharedElementTargets, Object enterTransition, Object exitTransition) {
        if (sharedElementTransition != null) {
            final View view = sceneRoot;
            final Object obj = sharedElementTransition;
            final ArrayList<View> arrayList = sharedElementTargets;
            final TransitionState transitionState = state;
            final Object obj2 = enterTransition;
            final Object obj3 = exitTransition;
            final boolean z = isBack;
            final Fragment fragment = inFragment;
            final Fragment fragment2 = outFragment;
            sceneRoot.getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {
                public boolean onPreDraw() {
                    view.getViewTreeObserver().removeOnPreDrawListener(this);
                    FragmentTransitionCompat21.removeTargets(obj, arrayList);
                    arrayList.remove(transitionState.nonExistentView);
                    FragmentTransitionCompat21.excludeSharedElementViews(obj2, obj3, obj, arrayList, false);
                    arrayList.clear();
                    ArrayMap<String, View> namedViews = BackStackRecord.this.mapSharedElementsIn(transitionState, z, fragment);
                    FragmentTransitionCompat21.setSharedElementTargets(obj, transitionState.nonExistentView, namedViews, arrayList);
                    BackStackRecord.this.setEpicenterIn(namedViews, transitionState);
                    BackStackRecord.this.callSharedElementEnd(transitionState, fragment, fragment2, z, namedViews);
                    FragmentTransitionCompat21.excludeSharedElementViews(obj2, obj3, obj, arrayList, true);
                    return true;
                }
            });
        }
    }

    private void callSharedElementEnd(TransitionState state, Fragment inFragment, Fragment outFragment, boolean isBack, ArrayMap<String, View> namedViews) {
        SharedElementCallback sharedElementCallback;
        if (isBack) {
            sharedElementCallback = outFragment.mEnterTransitionCallback;
        } else {
            sharedElementCallback = inFragment.mEnterTransitionCallback;
        }
        if (sharedElementCallback != null) {
            sharedElementCallback.onSharedElementEnd(new ArrayList(namedViews.keySet()), new ArrayList(namedViews.values()), null);
        }
    }

    private void setEpicenterIn(ArrayMap<String, View> namedViews, TransitionState state) {
        if (this.mSharedElementTargetNames != null && !namedViews.isEmpty()) {
            View epicenter = (View) namedViews.get(this.mSharedElementTargetNames.get(0));
            if (epicenter != null) {
                state.enteringEpicenterView.epicenter = epicenter;
            }
        }
    }

    private ArrayMap<String, View> mapSharedElementsIn(TransitionState state, boolean isBack, Fragment inFragment) {
        ArrayMap namedViews = mapEnteringSharedElements(state, inFragment, isBack);
        if (isBack) {
            if (inFragment.mExitTransitionCallback != null) {
                inFragment.mExitTransitionCallback.onMapSharedElements(this.mSharedElementTargetNames, namedViews);
            }
            setBackNameOverrides(state, namedViews, true);
        } else {
            if (inFragment.mEnterTransitionCallback != null) {
                inFragment.mEnterTransitionCallback.onMapSharedElements(this.mSharedElementTargetNames, namedViews);
            }
            setNameOverrides(state, namedViews, true);
        }
        return namedViews;
    }

    private static ArrayMap<String, View> remapNames(ArrayList<String> inMap, ArrayList<String> toGoInMap, ArrayMap<String, View> namedViews) {
        if (namedViews.isEmpty()) {
            return namedViews;
        }
        ArrayMap<String, View> remappedViews = new ArrayMap();
        int numKeys = inMap.size();
        for (int i = 0; i < numKeys; i++) {
            View view = (View) namedViews.get(inMap.get(i));
            if (view != null) {
                remappedViews.put((String) toGoInMap.get(i), view);
            }
        }
        return remappedViews;
    }

    private ArrayMap<String, View> mapEnteringSharedElements(TransitionState state, Fragment inFragment, boolean isBack) {
        ArrayMap<String, View> namedViews = new ArrayMap();
        View root = inFragment.getView();
        if (root == null || this.mSharedElementSourceNames == null) {
            return namedViews;
        }
        FragmentTransitionCompat21.findNamedViews(namedViews, root);
        if (isBack) {
            return remapNames(this.mSharedElementSourceNames, this.mSharedElementTargetNames, namedViews);
        }
        namedViews.retainAll(this.mSharedElementTargetNames);
        return namedViews;
    }

    private void excludeHiddenFragmentsAfterEnter(View sceneRoot, TransitionState state, int containerId, Object transition) {
        final View view = sceneRoot;
        final TransitionState transitionState = state;
        final int i = containerId;
        final Object obj = transition;
        sceneRoot.getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {
            public boolean onPreDraw() {
                view.getViewTreeObserver().removeOnPreDrawListener(this);
                BackStackRecord.this.excludeHiddenFragments(transitionState, i, obj);
                return true;
            }
        });
    }

    private void excludeHiddenFragments(TransitionState state, int containerId, Object transition) {
        if (this.mManager.mAdded != null) {
            for (int i = 0; i < this.mManager.mAdded.size(); i++) {
                Fragment fragment = (Fragment) this.mManager.mAdded.get(i);
                if (!(fragment.mView == null || fragment.mContainer == null || fragment.mContainerId != containerId)) {
                    if (!fragment.mHidden) {
                        FragmentTransitionCompat21.excludeTarget(transition, fragment.mView, false);
                        state.hiddenFragmentViews.remove(fragment.mView);
                    } else if (!state.hiddenFragmentViews.contains(fragment.mView)) {
                        FragmentTransitionCompat21.excludeTarget(transition, fragment.mView, true);
                        state.hiddenFragmentViews.add(fragment.mView);
                    }
                }
            }
        }
    }

    private static void setNameOverride(ArrayMap<String, String> overrides, String source, String target) {
        if (!(source == null || target == null)) {
            for (int index = 0; index < overrides.size(); index++) {
                if (source.equals(overrides.valueAt(index))) {
                    overrides.setValueAt(index, target);
                    return;
                }
            }
            overrides.put(source, target);
        }
    }

    private static void setNameOverrides(TransitionState state, ArrayList<String> sourceNames, ArrayList<String> targetNames) {
        if (sourceNames != null) {
            for (int i = 0; i < sourceNames.size(); i++) {
                setNameOverride(state.nameOverrides, (String) sourceNames.get(i), (String) targetNames.get(i));
            }
        }
    }

    private void setBackNameOverrides(TransitionState state, ArrayMap<String, View> namedViews, boolean isEnd) {
        int count = this.mSharedElementTargetNames == null ? 0 : this.mSharedElementTargetNames.size();
        for (int i = 0; i < count; i++) {
            String source = (String) this.mSharedElementSourceNames.get(i);
            View view = (View) namedViews.get((String) this.mSharedElementTargetNames.get(i));
            if (view != null) {
                String target = FragmentTransitionCompat21.getTransitionName(view);
                if (isEnd) {
                    setNameOverride(state.nameOverrides, source, target);
                } else {
                    setNameOverride(state.nameOverrides, target, source);
                }
            }
        }
    }

    private void setNameOverrides(TransitionState state, ArrayMap<String, View> namedViews, boolean isEnd) {
        int count = namedViews.size();
        for (int i = 0; i < count; i++) {
            String source = (String) namedViews.keyAt(i);
            String target = FragmentTransitionCompat21.getTransitionName((View) namedViews.valueAt(i));
            if (isEnd) {
                setNameOverride(state.nameOverrides, source, target);
            } else {
                setNameOverride(state.nameOverrides, target, source);
            }
        }
    }
}
