package java.util;

public class Observable {
    private boolean changed = false;
    private final ArrayList<Observer> observers = new ArrayList();

    public synchronized void addObserver(Observer o) {
        if (o == null) {
            throw new NullPointerException();
        } else if (!this.observers.contains(o)) {
            this.observers.add(o);
        }
    }

    public synchronized void deleteObserver(Observer o) {
        this.observers.remove((Object) o);
    }

    public void notifyObservers() {
        notifyObservers(null);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void notifyObservers(Object arg) {
        synchronized (this) {
            if (hasChanged()) {
                Observer[] arrLocal = (Observer[]) this.observers.toArray(new Observer[this.observers.size()]);
                clearChanged();
            }
        }
    }

    public synchronized void deleteObservers() {
        this.observers.clear();
    }

    protected synchronized void setChanged() {
        this.changed = true;
    }

    protected synchronized void clearChanged() {
        this.changed = false;
    }

    public synchronized boolean hasChanged() {
        return this.changed;
    }

    public synchronized int countObservers() {
        return this.observers.size();
    }
}
