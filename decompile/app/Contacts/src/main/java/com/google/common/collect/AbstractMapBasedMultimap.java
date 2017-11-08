package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.base.Preconditions;
import com.google.common.collect.AbstractMapBasedMultimap$WrappedCollection.WrappedIterator;
import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.RandomAccess;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import javax.annotation.Nullable;

@GwtCompatible(emulated = true)
abstract class AbstractMapBasedMultimap<K, V> extends AbstractMultimap<K, V> implements Serializable {
    private static final long serialVersionUID = 2447537837011683357L;
    private transient Map<K, Collection<V>> map;
    private transient int totalSize;

    private abstract class Itr<T> implements Iterator<T> {
        Collection<V> collection = null;
        K key = null;
        final Iterator<Entry<K, Collection<V>>> keyIterator;
        Iterator<V> valueIterator = Iterators.emptyModifiableIterator();

        abstract T output(K k, V v);

        Itr() {
            this.keyIterator = AbstractMapBasedMultimap.this.map.entrySet().iterator();
        }

        public boolean hasNext() {
            return !this.keyIterator.hasNext() ? this.valueIterator.hasNext() : true;
        }

        public T next() {
            if (!this.valueIterator.hasNext()) {
                Entry<K, Collection<V>> mapEntry = (Entry) this.keyIterator.next();
                this.key = mapEntry.getKey();
                this.collection = (Collection) mapEntry.getValue();
                this.valueIterator = this.collection.iterator();
            }
            return output(this.key, this.valueIterator.next());
        }

        public void remove() {
            this.valueIterator.remove();
            if (this.collection.isEmpty()) {
                this.keyIterator.remove();
            }
            AbstractMapBasedMultimap abstractMapBasedMultimap = AbstractMapBasedMultimap.this;
            abstractMapBasedMultimap.totalSize = abstractMapBasedMultimap.totalSize - 1;
        }
    }

    private class AsMap extends ImprovedAbstractMap<K, Collection<V>> {
        final transient Map<K, Collection<V>> submap;

        class AsMapEntries extends EntrySet<K, Collection<V>> {
            AsMapEntries() {
            }

            Map<K, Collection<V>> map() {
                return AsMap.this;
            }

            public Iterator<Entry<K, Collection<V>>> iterator() {
                return new AsMapIterator();
            }

            public boolean contains(Object o) {
                return Collections2.safeContains(AsMap.this.submap.entrySet(), o);
            }

            public boolean remove(Object o) {
                if (!contains(o)) {
                    return false;
                }
                AbstractMapBasedMultimap.this.removeValuesForKey(((Entry) o).getKey());
                return true;
            }
        }

        class AsMapIterator implements Iterator<Entry<K, Collection<V>>> {
            Collection<V> collection;
            final Iterator<Entry<K, Collection<V>>> delegateIterator = AsMap.this.submap.entrySet().iterator();

            AsMapIterator() {
            }

            public boolean hasNext() {
                return this.delegateIterator.hasNext();
            }

            public Entry<K, Collection<V>> next() {
                Entry<K, Collection<V>> entry = (Entry) this.delegateIterator.next();
                this.collection = (Collection) entry.getValue();
                return AsMap.this.wrapEntry(entry);
            }

            public void remove() {
                this.delegateIterator.remove();
                AbstractMapBasedMultimap abstractMapBasedMultimap = AbstractMapBasedMultimap.this;
                abstractMapBasedMultimap.totalSize = abstractMapBasedMultimap.totalSize - this.collection.size();
                this.collection.clear();
            }
        }

        AsMap(Map<K, Collection<V>> submap) {
            this.submap = submap;
        }

        protected Set<Entry<K, Collection<V>>> createEntrySet() {
            return new AsMapEntries();
        }

        public boolean containsKey(Object key) {
            return Maps.safeContainsKey(this.submap, key);
        }

        public Collection<V> get(Object key) {
            Collection<V> collection = (Collection) Maps.safeGet(this.submap, key);
            if (collection == null) {
                return null;
            }
            K k = key;
            return AbstractMapBasedMultimap.this.wrapCollection(key, collection);
        }

        public Set<K> keySet() {
            return AbstractMapBasedMultimap.this.keySet();
        }

        public int size() {
            return this.submap.size();
        }

        public Collection<V> remove(Object key) {
            Collection<V> collection = (Collection) this.submap.remove(key);
            if (collection == null) {
                return null;
            }
            Collection<V> output = AbstractMapBasedMultimap.this.createCollection();
            output.addAll(collection);
            AbstractMapBasedMultimap abstractMapBasedMultimap = AbstractMapBasedMultimap.this;
            abstractMapBasedMultimap.totalSize = abstractMapBasedMultimap.totalSize - collection.size();
            collection.clear();
            return output;
        }

        public boolean equals(@Nullable Object object) {
            return this != object ? this.submap.equals(object) : true;
        }

        public int hashCode() {
            return this.submap.hashCode();
        }

        public String toString() {
            return this.submap.toString();
        }

        public void clear() {
            if (this.submap == AbstractMapBasedMultimap.this.map) {
                AbstractMapBasedMultimap.this.clear();
            } else {
                Iterators.clear(new AsMapIterator());
            }
        }

        Entry<K, Collection<V>> wrapEntry(Entry<K, Collection<V>> entry) {
            K key = entry.getKey();
            return Maps.immutableEntry(key, AbstractMapBasedMultimap.this.wrapCollection(key, (Collection) entry.getValue()));
        }
    }

    private class KeySet extends KeySet<K, Collection<V>> {
        KeySet(Map<K, Collection<V>> subMap) {
            super(subMap);
        }

        public Iterator<K> iterator() {
            final Iterator<Entry<K, Collection<V>>> entryIterator = map().entrySet().iterator();
            return new Iterator<K>() {
                Entry<K, Collection<V>> entry;

                public boolean hasNext() {
                    return entryIterator.hasNext();
                }

                public K next() {
                    this.entry = (Entry) entryIterator.next();
                    return this.entry.getKey();
                }

                public void remove() {
                    CollectPreconditions.checkRemove(this.entry != null);
                    Collection<V> collection = (Collection) this.entry.getValue();
                    entryIterator.remove();
                    AbstractMapBasedMultimap abstractMapBasedMultimap = AbstractMapBasedMultimap.this;
                    abstractMapBasedMultimap.totalSize = abstractMapBasedMultimap.totalSize - collection.size();
                    collection.clear();
                }
            };
        }

        public boolean remove(Object key) {
            int count = 0;
            Collection<V> collection = (Collection) map().remove(key);
            if (collection != null) {
                count = collection.size();
                collection.clear();
                AbstractMapBasedMultimap abstractMapBasedMultimap = AbstractMapBasedMultimap.this;
                abstractMapBasedMultimap.totalSize = abstractMapBasedMultimap.totalSize - count;
            }
            if (count > 0) {
                return true;
            }
            return false;
        }

        public void clear() {
            Iterators.clear(iterator());
        }

        public boolean containsAll(Collection<?> c) {
            return map().keySet().containsAll(c);
        }

        public boolean equals(@Nullable Object object) {
            return this != object ? map().keySet().equals(object) : true;
        }

        public int hashCode() {
            return map().keySet().hashCode();
        }
    }

    private class WrappedCollection extends AbstractCollection<V> {
        final WrappedCollection ancestor;
        final Collection<V> ancestorDelegate;
        Collection<V> delegate;
        final K key;
        final /* synthetic */ AbstractMapBasedMultimap this$0;

        class WrappedIterator implements Iterator<V> {
            final Iterator<V> delegateIterator;
            final Collection<V> originalDelegate = WrappedCollection.this.delegate;

            WrappedIterator() {
                this.delegateIterator = WrappedCollection.this.this$0.iteratorOrListIterator(WrappedCollection.this.delegate);
            }

            WrappedIterator(Iterator<V> delegateIterator) {
                this.delegateIterator = delegateIterator;
            }

            void validateIterator() {
                WrappedCollection.this.refreshIfEmpty();
                if (WrappedCollection.this.delegate != this.originalDelegate) {
                    throw new ConcurrentModificationException();
                }
            }

            public boolean hasNext() {
                validateIterator();
                return this.delegateIterator.hasNext();
            }

            public V next() {
                validateIterator();
                return this.delegateIterator.next();
            }

            public void remove() {
                this.delegateIterator.remove();
                AbstractMapBasedMultimap abstractMapBasedMultimap = WrappedCollection.this.this$0;
                abstractMapBasedMultimap.totalSize = abstractMapBasedMultimap.totalSize - 1;
                WrappedCollection.this.removeIfEmpty();
            }

            Iterator<V> getDelegateIterator() {
                validateIterator();
                return this.delegateIterator;
            }
        }

        WrappedCollection(AbstractMapBasedMultimap this$0, @Nullable K key, Collection<V> delegate, @Nullable WrappedCollection ancestor) {
            Collection collection = null;
            this.this$0 = this$0;
            this.key = key;
            this.delegate = delegate;
            this.ancestor = ancestor;
            if (ancestor != null) {
                collection = ancestor.getDelegate();
            }
            this.ancestorDelegate = collection;
        }

        void refreshIfEmpty() {
            if (this.ancestor != null) {
                this.ancestor.refreshIfEmpty();
                if (this.ancestor.getDelegate() != this.ancestorDelegate) {
                    throw new ConcurrentModificationException();
                }
            } else if (this.delegate.isEmpty()) {
                Collection<V> newDelegate = (Collection) this.this$0.map.get(this.key);
                if (newDelegate != null) {
                    this.delegate = newDelegate;
                }
            }
        }

        void removeIfEmpty() {
            if (this.ancestor != null) {
                this.ancestor.removeIfEmpty();
            } else if (this.delegate.isEmpty()) {
                this.this$0.map.remove(this.key);
            }
        }

        K getKey() {
            return this.key;
        }

        void addToMap() {
            if (this.ancestor != null) {
                this.ancestor.addToMap();
            } else {
                this.this$0.map.put(this.key, this.delegate);
            }
        }

        public int size() {
            refreshIfEmpty();
            return this.delegate.size();
        }

        public boolean equals(@Nullable Object object) {
            if (object == this) {
                return true;
            }
            refreshIfEmpty();
            return this.delegate.equals(object);
        }

        public int hashCode() {
            refreshIfEmpty();
            return this.delegate.hashCode();
        }

        public String toString() {
            refreshIfEmpty();
            return this.delegate.toString();
        }

        Collection<V> getDelegate() {
            return this.delegate;
        }

        public Iterator<V> iterator() {
            refreshIfEmpty();
            return new WrappedIterator();
        }

        public boolean add(V value) {
            refreshIfEmpty();
            boolean wasEmpty = this.delegate.isEmpty();
            boolean changed = this.delegate.add(value);
            if (changed) {
                AbstractMapBasedMultimap abstractMapBasedMultimap = this.this$0;
                abstractMapBasedMultimap.totalSize = abstractMapBasedMultimap.totalSize + 1;
                if (wasEmpty) {
                    addToMap();
                }
            }
            return changed;
        }

        WrappedCollection getAncestor() {
            return this.ancestor;
        }

        public boolean addAll(Collection<? extends V> collection) {
            if (collection.isEmpty()) {
                return false;
            }
            int oldSize = size();
            boolean changed = this.delegate.addAll(collection);
            if (changed) {
                int newSize = this.delegate.size();
                AbstractMapBasedMultimap abstractMapBasedMultimap = this.this$0;
                abstractMapBasedMultimap.totalSize = abstractMapBasedMultimap.totalSize + (newSize - oldSize);
                if (oldSize == 0) {
                    addToMap();
                }
            }
            return changed;
        }

        public boolean contains(Object o) {
            refreshIfEmpty();
            return this.delegate.contains(o);
        }

        public boolean containsAll(Collection<?> c) {
            refreshIfEmpty();
            return this.delegate.containsAll(c);
        }

        public void clear() {
            int oldSize = size();
            if (oldSize != 0) {
                this.delegate.clear();
                AbstractMapBasedMultimap abstractMapBasedMultimap = this.this$0;
                abstractMapBasedMultimap.totalSize = abstractMapBasedMultimap.totalSize - oldSize;
                removeIfEmpty();
            }
        }

        public boolean remove(Object o) {
            refreshIfEmpty();
            boolean changed = this.delegate.remove(o);
            if (changed) {
                AbstractMapBasedMultimap abstractMapBasedMultimap = this.this$0;
                abstractMapBasedMultimap.totalSize = abstractMapBasedMultimap.totalSize - 1;
                removeIfEmpty();
            }
            return changed;
        }

        public boolean removeAll(Collection<?> c) {
            if (c.isEmpty()) {
                return false;
            }
            int oldSize = size();
            boolean changed = this.delegate.removeAll(c);
            if (changed) {
                int newSize = this.delegate.size();
                AbstractMapBasedMultimap abstractMapBasedMultimap = this.this$0;
                abstractMapBasedMultimap.totalSize = abstractMapBasedMultimap.totalSize + (newSize - oldSize);
                removeIfEmpty();
            }
            return changed;
        }

        public boolean retainAll(Collection<?> c) {
            Preconditions.checkNotNull(c);
            int oldSize = size();
            boolean changed = this.delegate.retainAll(c);
            if (changed) {
                int newSize = this.delegate.size();
                AbstractMapBasedMultimap abstractMapBasedMultimap = this.this$0;
                abstractMapBasedMultimap.totalSize = abstractMapBasedMultimap.totalSize + (newSize - oldSize);
                removeIfEmpty();
            }
            return changed;
        }
    }

    private class WrappedList extends WrappedCollection implements List<V> {

        private class WrappedListIterator extends WrappedIterator implements ListIterator<V> {
            WrappedListIterator() {
                super();
            }

            public WrappedListIterator(int index) {
                super(WrappedList.this.getListDelegate().listIterator(index));
            }

            private ListIterator<V> getDelegateListIterator() {
                return (ListIterator) getDelegateIterator();
            }

            public boolean hasPrevious() {
                return getDelegateListIterator().hasPrevious();
            }

            public V previous() {
                return getDelegateListIterator().previous();
            }

            public int nextIndex() {
                return getDelegateListIterator().nextIndex();
            }

            public int previousIndex() {
                return getDelegateListIterator().previousIndex();
            }

            public void set(V value) {
                getDelegateListIterator().set(value);
            }

            public void add(V value) {
                boolean wasEmpty = WrappedList.this.isEmpty();
                getDelegateListIterator().add(value);
                AbstractMapBasedMultimap abstractMapBasedMultimap = AbstractMapBasedMultimap.this;
                abstractMapBasedMultimap.totalSize = abstractMapBasedMultimap.totalSize + 1;
                if (wasEmpty) {
                    WrappedList.this.addToMap();
                }
            }
        }

        WrappedList(K key, @Nullable List<V> delegate, WrappedCollection ancestor) {
            super(AbstractMapBasedMultimap.this, key, delegate, ancestor);
        }

        List<V> getListDelegate() {
            return (List) getDelegate();
        }

        public boolean addAll(int index, Collection<? extends V> c) {
            if (c.isEmpty()) {
                return false;
            }
            int oldSize = size();
            boolean changed = getListDelegate().addAll(index, c);
            if (changed) {
                int newSize = getDelegate().size();
                AbstractMapBasedMultimap abstractMapBasedMultimap = AbstractMapBasedMultimap.this;
                abstractMapBasedMultimap.totalSize = abstractMapBasedMultimap.totalSize + (newSize - oldSize);
                if (oldSize == 0) {
                    addToMap();
                }
            }
            return changed;
        }

        public V get(int index) {
            refreshIfEmpty();
            return getListDelegate().get(index);
        }

        public V set(int index, V element) {
            refreshIfEmpty();
            return getListDelegate().set(index, element);
        }

        public void add(int index, V element) {
            refreshIfEmpty();
            boolean wasEmpty = getDelegate().isEmpty();
            getListDelegate().add(index, element);
            AbstractMapBasedMultimap abstractMapBasedMultimap = AbstractMapBasedMultimap.this;
            abstractMapBasedMultimap.totalSize = abstractMapBasedMultimap.totalSize + 1;
            if (wasEmpty) {
                addToMap();
            }
        }

        public V remove(int index) {
            refreshIfEmpty();
            V value = getListDelegate().remove(index);
            AbstractMapBasedMultimap abstractMapBasedMultimap = AbstractMapBasedMultimap.this;
            abstractMapBasedMultimap.totalSize = abstractMapBasedMultimap.totalSize - 1;
            removeIfEmpty();
            return value;
        }

        public int indexOf(Object o) {
            refreshIfEmpty();
            return getListDelegate().indexOf(o);
        }

        public int lastIndexOf(Object o) {
            refreshIfEmpty();
            return getListDelegate().lastIndexOf(o);
        }

        public ListIterator<V> listIterator() {
            refreshIfEmpty();
            return new WrappedListIterator();
        }

        public ListIterator<V> listIterator(int index) {
            refreshIfEmpty();
            return new WrappedListIterator(index);
        }

        public List<V> subList(int fromIndex, int toIndex) {
            WrappedCollection ancestor;
            refreshIfEmpty();
            AbstractMapBasedMultimap abstractMapBasedMultimap = AbstractMapBasedMultimap.this;
            Object key = getKey();
            List subList = getListDelegate().subList(fromIndex, toIndex);
            if (getAncestor() != null) {
                ancestor = getAncestor();
            }
            return abstractMapBasedMultimap.wrapList(key, subList, ancestor);
        }
    }

    private class RandomAccessWrappedList extends WrappedList implements RandomAccess {
        RandomAccessWrappedList(K key, @Nullable List<V> delegate, WrappedCollection ancestor) {
            super(key, delegate, ancestor);
        }
    }

    private class SortedAsMap extends AsMap implements SortedMap<K, Collection<V>> {
        SortedSet<K> sortedKeySet;

        SortedAsMap(SortedMap<K, Collection<V>> submap) {
            super(submap);
        }

        SortedMap<K, Collection<V>> sortedMap() {
            return (SortedMap) this.submap;
        }

        public Comparator<? super K> comparator() {
            return sortedMap().comparator();
        }

        public K firstKey() {
            return sortedMap().firstKey();
        }

        public K lastKey() {
            return sortedMap().lastKey();
        }

        public SortedMap<K, Collection<V>> headMap(K toKey) {
            return new SortedAsMap(sortedMap().headMap(toKey));
        }

        public SortedMap<K, Collection<V>> subMap(K fromKey, K toKey) {
            return new SortedAsMap(sortedMap().subMap(fromKey, toKey));
        }

        public SortedMap<K, Collection<V>> tailMap(K fromKey) {
            return new SortedAsMap(sortedMap().tailMap(fromKey));
        }

        public SortedSet<K> keySet() {
            SortedSet<K> sortedSet = this.sortedKeySet;
            if (sortedSet != null) {
                return sortedSet;
            }
            sortedSet = createKeySet();
            this.sortedKeySet = sortedSet;
            return sortedSet;
        }

        SortedSet<K> createKeySet() {
            return new SortedKeySet(sortedMap());
        }
    }

    private class SortedKeySet extends KeySet implements SortedSet<K> {
        SortedKeySet(SortedMap<K, Collection<V>> subMap) {
            super(subMap);
        }

        SortedMap<K, Collection<V>> sortedMap() {
            return (SortedMap) super.map();
        }

        public Comparator<? super K> comparator() {
            return sortedMap().comparator();
        }

        public K first() {
            return sortedMap().firstKey();
        }

        public SortedSet<K> headSet(K toElement) {
            return new SortedKeySet(sortedMap().headMap(toElement));
        }

        public K last() {
            return sortedMap().lastKey();
        }

        public SortedSet<K> subSet(K fromElement, K toElement) {
            return new SortedKeySet(sortedMap().subMap(fromElement, toElement));
        }

        public SortedSet<K> tailSet(K fromElement) {
            return new SortedKeySet(sortedMap().tailMap(fromElement));
        }
    }

    private class WrappedSet extends WrappedCollection implements Set<V> {
        WrappedSet(K key, @Nullable Set<V> delegate) {
            super(AbstractMapBasedMultimap.this, key, delegate, null);
        }

        public boolean removeAll(Collection<?> c) {
            if (c.isEmpty()) {
                return false;
            }
            int oldSize = size();
            boolean changed = Sets.removeAllImpl((Set) this.delegate, (Collection) c);
            if (changed) {
                int newSize = this.delegate.size();
                AbstractMapBasedMultimap abstractMapBasedMultimap = AbstractMapBasedMultimap.this;
                abstractMapBasedMultimap.totalSize = abstractMapBasedMultimap.totalSize + (newSize - oldSize);
                removeIfEmpty();
            }
            return changed;
        }
    }

    private class WrappedSortedSet extends WrappedCollection implements SortedSet<V> {
        WrappedSortedSet(K key, @Nullable SortedSet<V> delegate, WrappedCollection ancestor) {
            super(AbstractMapBasedMultimap.this, key, delegate, ancestor);
        }

        SortedSet<V> getSortedSetDelegate() {
            return (SortedSet) getDelegate();
        }

        public Comparator<? super V> comparator() {
            return getSortedSetDelegate().comparator();
        }

        public V first() {
            refreshIfEmpty();
            return getSortedSetDelegate().first();
        }

        public V last() {
            refreshIfEmpty();
            return getSortedSetDelegate().last();
        }

        public SortedSet<V> headSet(V toElement) {
            WrappedCollection ancestor;
            refreshIfEmpty();
            AbstractMapBasedMultimap abstractMapBasedMultimap = AbstractMapBasedMultimap.this;
            Object key = getKey();
            SortedSet headSet = getSortedSetDelegate().headSet(toElement);
            if (getAncestor() != null) {
                ancestor = getAncestor();
            }
            return new WrappedSortedSet(key, headSet, ancestor);
        }

        public SortedSet<V> subSet(V fromElement, V toElement) {
            WrappedCollection ancestor;
            refreshIfEmpty();
            AbstractMapBasedMultimap abstractMapBasedMultimap = AbstractMapBasedMultimap.this;
            Object key = getKey();
            SortedSet subSet = getSortedSetDelegate().subSet(fromElement, toElement);
            if (getAncestor() != null) {
                ancestor = getAncestor();
            }
            return new WrappedSortedSet(key, subSet, ancestor);
        }

        public SortedSet<V> tailSet(V fromElement) {
            WrappedCollection ancestor;
            refreshIfEmpty();
            AbstractMapBasedMultimap abstractMapBasedMultimap = AbstractMapBasedMultimap.this;
            Object key = getKey();
            SortedSet tailSet = getSortedSetDelegate().tailSet(fromElement);
            if (getAncestor() != null) {
                ancestor = getAncestor();
            }
            return new WrappedSortedSet(key, tailSet, ancestor);
        }
    }

    abstract Collection<V> createCollection();

    final void setMap(Map<K, Collection<V>> map) {
        this.map = map;
        this.totalSize = 0;
        for (Collection<V> values : map.values()) {
            Preconditions.checkArgument(!values.isEmpty());
            this.totalSize += values.size();
        }
    }

    public int size() {
        return this.totalSize;
    }

    public void clear() {
        for (Collection<V> collection : this.map.values()) {
            collection.clear();
        }
        this.map.clear();
        this.totalSize = 0;
    }

    Collection<V> wrapCollection(@Nullable K key, Collection<V> collection) {
        if (collection instanceof SortedSet) {
            return new WrappedSortedSet(key, (SortedSet) collection, null);
        }
        if (collection instanceof Set) {
            return new WrappedSet(key, (Set) collection);
        }
        if (collection instanceof List) {
            return wrapList(key, (List) collection, null);
        }
        return new WrappedCollection(this, key, collection, null);
    }

    private List<V> wrapList(@Nullable K key, List<V> list, @Nullable WrappedCollection ancestor) {
        if (list instanceof RandomAccess) {
            return new RandomAccessWrappedList(key, list, ancestor);
        }
        return new WrappedList(key, list, ancestor);
    }

    private Iterator<V> iteratorOrListIterator(Collection<V> collection) {
        if (collection instanceof List) {
            return ((List) collection).listIterator();
        }
        return collection.iterator();
    }

    Set<K> createKeySet() {
        return this.map instanceof SortedMap ? new SortedKeySet((SortedMap) this.map) : new KeySet(this.map);
    }

    private int removeValuesForKey(Object key) {
        Collection<V> collection = (Collection) Maps.safeRemove(this.map, key);
        if (collection == null) {
            return 0;
        }
        int count = collection.size();
        collection.clear();
        this.totalSize -= count;
        return count;
    }

    public Collection<Entry<K, V>> entries() {
        return super.entries();
    }

    Iterator<Entry<K, V>> entryIterator() {
        return new Itr<Entry<K, V>>(this) {
            Entry<K, V> output(K key, V value) {
                return Maps.immutableEntry(key, value);
            }
        };
    }

    Map<K, Collection<V>> createAsMap() {
        return this.map instanceof SortedMap ? new SortedAsMap((SortedMap) this.map) : new AsMap(this.map);
    }
}
