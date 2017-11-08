package com.google.protobuf;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map.Entry;

class LazyField {
    private ByteString bytes;
    private final MessageLite defaultInstance;
    private final ExtensionRegistryLite extensionRegistry;
    private volatile boolean isDirty = false;
    private volatile MessageLite value;

    static class LazyEntry<K> implements Entry<K, Object> {
        private Entry<K, LazyField> entry;

        private LazyEntry(Entry<K, LazyField> entry) {
            this.entry = entry;
        }

        public K getKey() {
            return this.entry.getKey();
        }

        public Object getValue() {
            LazyField lazyField = (LazyField) this.entry.getValue();
            if (lazyField != null) {
                return lazyField.getValue();
            }
            return null;
        }

        public LazyField getField() {
            return (LazyField) this.entry.getValue();
        }

        public Object setValue(Object obj) {
            if (obj instanceof MessageLite) {
                return ((LazyField) this.entry.getValue()).setValue((MessageLite) obj);
            }
            throw new IllegalArgumentException("LazyField now only used for MessageSet, and the value of MessageSet must be an instance of MessageLite");
        }
    }

    static class LazyIterator<K> implements Iterator<Entry<K, Object>> {
        private Iterator<Entry<K, Object>> iterator;

        public LazyIterator(Iterator<Entry<K, Object>> it) {
            this.iterator = it;
        }

        public boolean hasNext() {
            return this.iterator.hasNext();
        }

        public Entry<K, Object> next() {
            Entry<K, Object> entry = (Entry) this.iterator.next();
            if (entry.getValue() instanceof LazyField) {
                return new LazyEntry(entry);
            }
            return entry;
        }

        public void remove() {
            this.iterator.remove();
        }
    }

    public LazyField(MessageLite messageLite, ExtensionRegistryLite extensionRegistryLite, ByteString byteString) {
        this.defaultInstance = messageLite;
        this.extensionRegistry = extensionRegistryLite;
        this.bytes = byteString;
    }

    public MessageLite getValue() {
        ensureInitialized();
        return this.value;
    }

    public MessageLite setValue(MessageLite messageLite) {
        MessageLite messageLite2 = this.value;
        this.value = messageLite;
        this.bytes = null;
        this.isDirty = true;
        return messageLite2;
    }

    public int getSerializedSize() {
        if (this.isDirty) {
            return this.value.getSerializedSize();
        }
        return this.bytes.size();
    }

    public ByteString toByteString() {
        if (!this.isDirty) {
            return this.bytes;
        }
        synchronized (this) {
            if (this.isDirty) {
                this.bytes = this.value.toByteString();
                this.isDirty = false;
                ByteString byteString = this.bytes;
                return byteString;
            }
            byteString = this.bytes;
            return byteString;
        }
    }

    public int hashCode() {
        ensureInitialized();
        return this.value.hashCode();
    }

    public boolean equals(Object obj) {
        ensureInitialized();
        return this.value.equals(obj);
    }

    public String toString() {
        ensureInitialized();
        return this.value.toString();
    }

    private void ensureInitialized() {
        if (this.value == null) {
            synchronized (this) {
                if (this.value == null) {
                    try {
                        if (this.bytes != null) {
                            this.value = (MessageLite) this.defaultInstance.getParserForType().parseFrom(this.bytes, this.extensionRegistry);
                        }
                    } catch (IOException e) {
                    }
                    return;
                }
            }
        }
    }
}
