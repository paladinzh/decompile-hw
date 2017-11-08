package com.android.systemui.statusbar.phone;

import com.google.protobuf.nano.CodedInputByteBufferNano;
import com.google.protobuf.nano.CodedOutputByteBufferNano;
import com.google.protobuf.nano.InternalNano;
import com.google.protobuf.nano.MessageNano;
import com.google.protobuf.nano.WireFormatNano;
import fyusion.vislib.BuildConfig;
import java.io.IOException;

public final class TouchAnalyticsProto$Session extends MessageNano {
    private int bitField0_;
    private String build_;
    private long durationMillis_;
    public PhoneEvent[] phoneEvents;
    private int result_;
    public SensorEvent[] sensorEvents;
    private long startTimestampMillis_;
    private int touchAreaHeight_;
    private int touchAreaWidth_;
    public TouchEvent[] touchEvents;
    private int type_;

    public static final class PhoneEvent extends MessageNano {
        private static volatile PhoneEvent[] _emptyArray;
        private int bitField0_;
        private long timeOffsetNanos_;
        private int type_;

        public static PhoneEvent[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new PhoneEvent[0];
                    }
                }
            }
            return _emptyArray;
        }

        public PhoneEvent setType(int value) {
            this.type_ = value;
            this.bitField0_ |= 1;
            return this;
        }

        public PhoneEvent setTimeOffsetNanos(long value) {
            this.timeOffsetNanos_ = value;
            this.bitField0_ |= 2;
            return this;
        }

        public PhoneEvent() {
            clear();
        }

        public PhoneEvent clear() {
            this.bitField0_ = 0;
            this.type_ = 0;
            this.timeOffsetNanos_ = 0;
            this.cachedSize = -1;
            return this;
        }

        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            if ((this.bitField0_ & 1) != 0) {
                output.writeInt32(1, this.type_);
            }
            if ((this.bitField0_ & 2) != 0) {
                output.writeUInt64(2, this.timeOffsetNanos_);
            }
            super.writeTo(output);
        }

        protected int computeSerializedSize() {
            int size = super.computeSerializedSize();
            if ((this.bitField0_ & 1) != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(1, this.type_);
            }
            if ((this.bitField0_ & 2) != 0) {
                return size + CodedOutputByteBufferNano.computeUInt64Size(2, this.timeOffsetNanos_);
            }
            return size;
        }

        public PhoneEvent mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                switch (tag) {
                    case 0:
                        return this;
                    case 8:
                        int value = input.readInt32();
                        switch (value) {
                            case 0:
                            case 1:
                            case 2:
                            case 3:
                            case 4:
                            case 5:
                            case 6:
                            case 7:
                            case 8:
                            case 9:
                            case 10:
                            case 11:
                            case 12:
                            case 13:
                            case 14:
                            case 15:
                            case 16:
                            case 17:
                            case 18:
                            case 19:
                            case 20:
                            case 21:
                            case 22:
                            case 23:
                            case 24:
                            case 25:
                            case 26:
                            case 27:
                            case 28:
                                this.type_ = value;
                                this.bitField0_ |= 1;
                                break;
                            default:
                                break;
                        }
                    case 16:
                        this.timeOffsetNanos_ = input.readUInt64();
                        this.bitField0_ |= 2;
                        break;
                    default:
                        if (WireFormatNano.parseUnknownField(input, tag)) {
                            break;
                        }
                        return this;
                }
            }
        }
    }

    public static final class SensorEvent extends MessageNano {
        private static volatile SensorEvent[] _emptyArray;
        private int bitField0_;
        private long timeOffsetNanos_;
        private long timestamp_;
        private int type_;
        public float[] values;

        public static SensorEvent[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new SensorEvent[0];
                    }
                }
            }
            return _emptyArray;
        }

        public SensorEvent setType(int value) {
            this.type_ = value;
            this.bitField0_ |= 1;
            return this;
        }

        public SensorEvent setTimeOffsetNanos(long value) {
            this.timeOffsetNanos_ = value;
            this.bitField0_ |= 2;
            return this;
        }

        public SensorEvent setTimestamp(long value) {
            this.timestamp_ = value;
            this.bitField0_ |= 4;
            return this;
        }

        public SensorEvent() {
            clear();
        }

        public SensorEvent clear() {
            this.bitField0_ = 0;
            this.type_ = 1;
            this.timeOffsetNanos_ = 0;
            this.values = WireFormatNano.EMPTY_FLOAT_ARRAY;
            this.timestamp_ = 0;
            this.cachedSize = -1;
            return this;
        }

        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            if ((this.bitField0_ & 1) != 0) {
                output.writeInt32(1, this.type_);
            }
            if ((this.bitField0_ & 2) != 0) {
                output.writeUInt64(2, this.timeOffsetNanos_);
            }
            if (this.values != null && this.values.length > 0) {
                for (float writeFloat : this.values) {
                    output.writeFloat(3, writeFloat);
                }
            }
            if ((this.bitField0_ & 4) != 0) {
                output.writeUInt64(4, this.timestamp_);
            }
            super.writeTo(output);
        }

        protected int computeSerializedSize() {
            int size = super.computeSerializedSize();
            if ((this.bitField0_ & 1) != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(1, this.type_);
            }
            if ((this.bitField0_ & 2) != 0) {
                size += CodedOutputByteBufferNano.computeUInt64Size(2, this.timeOffsetNanos_);
            }
            if (this.values != null && this.values.length > 0) {
                size = (size + (this.values.length * 4)) + (this.values.length * 1);
            }
            if ((this.bitField0_ & 4) != 0) {
                return size + CodedOutputByteBufferNano.computeUInt64Size(4, this.timestamp_);
            }
            return size;
        }

        public SensorEvent mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                int arrayLength;
                int i;
                float[] newArray;
                switch (tag) {
                    case 0:
                        return this;
                    case 8:
                        int value = input.readInt32();
                        switch (value) {
                            case 1:
                            case 4:
                            case 5:
                            case 8:
                            case 11:
                                this.type_ = value;
                                this.bitField0_ |= 1;
                                break;
                            default:
                                break;
                        }
                    case 16:
                        this.timeOffsetNanos_ = input.readUInt64();
                        this.bitField0_ |= 2;
                        break;
                    case 26:
                        int length = input.readRawVarint32();
                        int limit = input.pushLimit(length);
                        arrayLength = length / 4;
                        i = this.values == null ? 0 : this.values.length;
                        newArray = new float[(i + arrayLength)];
                        if (i != 0) {
                            System.arraycopy(this.values, 0, newArray, 0, i);
                        }
                        while (i < newArray.length) {
                            newArray[i] = input.readFloat();
                            i++;
                        }
                        this.values = newArray;
                        input.popLimit(limit);
                        break;
                    case 29:
                        arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 29);
                        i = this.values == null ? 0 : this.values.length;
                        newArray = new float[(i + arrayLength)];
                        if (i != 0) {
                            System.arraycopy(this.values, 0, newArray, 0, i);
                        }
                        while (i < newArray.length - 1) {
                            newArray[i] = input.readFloat();
                            input.readTag();
                            i++;
                        }
                        newArray[i] = input.readFloat();
                        this.values = newArray;
                        break;
                    case 32:
                        this.timestamp_ = input.readUInt64();
                        this.bitField0_ |= 4;
                        break;
                    default:
                        if (WireFormatNano.parseUnknownField(input, tag)) {
                            break;
                        }
                        return this;
                }
            }
        }
    }

    public static final class TouchEvent extends MessageNano {
        private static volatile TouchEvent[] _emptyArray;
        private int actionIndex_;
        private int action_;
        private int bitField0_;
        public Pointer[] pointers;
        public BoundingBox removedBoundingBox;
        private boolean removedRedacted_;
        private long timeOffsetNanos_;

        public static final class BoundingBox extends MessageNano {
            private int bitField0_;
            private float height_;
            private float width_;

            public BoundingBox() {
                clear();
            }

            public BoundingBox clear() {
                this.bitField0_ = 0;
                this.width_ = 0.0f;
                this.height_ = 0.0f;
                this.cachedSize = -1;
                return this;
            }

            public void writeTo(CodedOutputByteBufferNano output) throws IOException {
                if ((this.bitField0_ & 1) != 0) {
                    output.writeFloat(1, this.width_);
                }
                if ((this.bitField0_ & 2) != 0) {
                    output.writeFloat(2, this.height_);
                }
                super.writeTo(output);
            }

            protected int computeSerializedSize() {
                int size = super.computeSerializedSize();
                if ((this.bitField0_ & 1) != 0) {
                    size += CodedOutputByteBufferNano.computeFloatSize(1, this.width_);
                }
                if ((this.bitField0_ & 2) != 0) {
                    return size + CodedOutputByteBufferNano.computeFloatSize(2, this.height_);
                }
                return size;
            }

            public BoundingBox mergeFrom(CodedInputByteBufferNano input) throws IOException {
                while (true) {
                    int tag = input.readTag();
                    switch (tag) {
                        case 0:
                            return this;
                        case 13:
                            this.width_ = input.readFloat();
                            this.bitField0_ |= 1;
                            break;
                        case 21:
                            this.height_ = input.readFloat();
                            this.bitField0_ |= 2;
                            break;
                        default:
                            if (WireFormatNano.parseUnknownField(input, tag)) {
                                break;
                            }
                            return this;
                    }
                }
            }
        }

        public static final class Pointer extends MessageNano {
            private static volatile Pointer[] _emptyArray;
            private int bitField0_;
            private int id_;
            private float pressure_;
            public BoundingBox removedBoundingBox;
            private float removedLength_;
            private float size_;
            private float x_;
            private float y_;

            public static Pointer[] emptyArray() {
                if (_emptyArray == null) {
                    synchronized (InternalNano.LAZY_INIT_LOCK) {
                        if (_emptyArray == null) {
                            _emptyArray = new Pointer[0];
                        }
                    }
                }
                return _emptyArray;
            }

            public Pointer setX(float value) {
                this.x_ = value;
                this.bitField0_ |= 1;
                return this;
            }

            public Pointer setY(float value) {
                this.y_ = value;
                this.bitField0_ |= 2;
                return this;
            }

            public Pointer setSize(float value) {
                this.size_ = value;
                this.bitField0_ |= 4;
                return this;
            }

            public Pointer setPressure(float value) {
                this.pressure_ = value;
                this.bitField0_ |= 8;
                return this;
            }

            public Pointer setId(int value) {
                this.id_ = value;
                this.bitField0_ |= 16;
                return this;
            }

            public Pointer() {
                clear();
            }

            public Pointer clear() {
                this.bitField0_ = 0;
                this.x_ = 0.0f;
                this.y_ = 0.0f;
                this.size_ = 0.0f;
                this.pressure_ = 0.0f;
                this.id_ = 0;
                this.removedLength_ = 0.0f;
                this.removedBoundingBox = null;
                this.cachedSize = -1;
                return this;
            }

            public void writeTo(CodedOutputByteBufferNano output) throws IOException {
                if ((this.bitField0_ & 1) != 0) {
                    output.writeFloat(1, this.x_);
                }
                if ((this.bitField0_ & 2) != 0) {
                    output.writeFloat(2, this.y_);
                }
                if ((this.bitField0_ & 4) != 0) {
                    output.writeFloat(3, this.size_);
                }
                if ((this.bitField0_ & 8) != 0) {
                    output.writeFloat(4, this.pressure_);
                }
                if ((this.bitField0_ & 16) != 0) {
                    output.writeInt32(5, this.id_);
                }
                if ((this.bitField0_ & 32) != 0) {
                    output.writeFloat(6, this.removedLength_);
                }
                if (this.removedBoundingBox != null) {
                    output.writeMessage(7, this.removedBoundingBox);
                }
                super.writeTo(output);
            }

            protected int computeSerializedSize() {
                int size = super.computeSerializedSize();
                if ((this.bitField0_ & 1) != 0) {
                    size += CodedOutputByteBufferNano.computeFloatSize(1, this.x_);
                }
                if ((this.bitField0_ & 2) != 0) {
                    size += CodedOutputByteBufferNano.computeFloatSize(2, this.y_);
                }
                if ((this.bitField0_ & 4) != 0) {
                    size += CodedOutputByteBufferNano.computeFloatSize(3, this.size_);
                }
                if ((this.bitField0_ & 8) != 0) {
                    size += CodedOutputByteBufferNano.computeFloatSize(4, this.pressure_);
                }
                if ((this.bitField0_ & 16) != 0) {
                    size += CodedOutputByteBufferNano.computeInt32Size(5, this.id_);
                }
                if ((this.bitField0_ & 32) != 0) {
                    size += CodedOutputByteBufferNano.computeFloatSize(6, this.removedLength_);
                }
                if (this.removedBoundingBox != null) {
                    return size + CodedOutputByteBufferNano.computeMessageSize(7, this.removedBoundingBox);
                }
                return size;
            }

            public Pointer mergeFrom(CodedInputByteBufferNano input) throws IOException {
                while (true) {
                    int tag = input.readTag();
                    switch (tag) {
                        case 0:
                            return this;
                        case 13:
                            this.x_ = input.readFloat();
                            this.bitField0_ |= 1;
                            break;
                        case 21:
                            this.y_ = input.readFloat();
                            this.bitField0_ |= 2;
                            break;
                        case 29:
                            this.size_ = input.readFloat();
                            this.bitField0_ |= 4;
                            break;
                        case 37:
                            this.pressure_ = input.readFloat();
                            this.bitField0_ |= 8;
                            break;
                        case 40:
                            this.id_ = input.readInt32();
                            this.bitField0_ |= 16;
                            break;
                        case 53:
                            this.removedLength_ = input.readFloat();
                            this.bitField0_ |= 32;
                            break;
                        case 58:
                            if (this.removedBoundingBox == null) {
                                this.removedBoundingBox = new BoundingBox();
                            }
                            input.readMessage(this.removedBoundingBox);
                            break;
                        default:
                            if (WireFormatNano.parseUnknownField(input, tag)) {
                                break;
                            }
                            return this;
                    }
                }
            }
        }

        public static TouchEvent[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new TouchEvent[0];
                    }
                }
            }
            return _emptyArray;
        }

        public TouchEvent setTimeOffsetNanos(long value) {
            this.timeOffsetNanos_ = value;
            this.bitField0_ |= 1;
            return this;
        }

        public TouchEvent setAction(int value) {
            this.action_ = value;
            this.bitField0_ |= 2;
            return this;
        }

        public TouchEvent setActionIndex(int value) {
            this.actionIndex_ = value;
            this.bitField0_ |= 4;
            return this;
        }

        public TouchEvent() {
            clear();
        }

        public TouchEvent clear() {
            this.bitField0_ = 0;
            this.timeOffsetNanos_ = 0;
            this.action_ = 0;
            this.actionIndex_ = 0;
            this.pointers = Pointer.emptyArray();
            this.removedRedacted_ = false;
            this.removedBoundingBox = null;
            this.cachedSize = -1;
            return this;
        }

        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            if ((this.bitField0_ & 1) != 0) {
                output.writeUInt64(1, this.timeOffsetNanos_);
            }
            if ((this.bitField0_ & 2) != 0) {
                output.writeInt32(2, this.action_);
            }
            if ((this.bitField0_ & 4) != 0) {
                output.writeInt32(3, this.actionIndex_);
            }
            if (this.pointers != null && this.pointers.length > 0) {
                for (Pointer element : this.pointers) {
                    if (element != null) {
                        output.writeMessage(4, element);
                    }
                }
            }
            if ((this.bitField0_ & 8) != 0) {
                output.writeBool(5, this.removedRedacted_);
            }
            if (this.removedBoundingBox != null) {
                output.writeMessage(6, this.removedBoundingBox);
            }
            super.writeTo(output);
        }

        protected int computeSerializedSize() {
            int size = super.computeSerializedSize();
            if ((this.bitField0_ & 1) != 0) {
                size += CodedOutputByteBufferNano.computeUInt64Size(1, this.timeOffsetNanos_);
            }
            if ((this.bitField0_ & 2) != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(2, this.action_);
            }
            if ((this.bitField0_ & 4) != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(3, this.actionIndex_);
            }
            if (this.pointers != null && this.pointers.length > 0) {
                for (Pointer element : this.pointers) {
                    if (element != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(4, element);
                    }
                }
            }
            if ((this.bitField0_ & 8) != 0) {
                size += CodedOutputByteBufferNano.computeBoolSize(5, this.removedRedacted_);
            }
            if (this.removedBoundingBox != null) {
                return size + CodedOutputByteBufferNano.computeMessageSize(6, this.removedBoundingBox);
            }
            return size;
        }

        public TouchEvent mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                switch (tag) {
                    case 0:
                        return this;
                    case 8:
                        this.timeOffsetNanos_ = input.readUInt64();
                        this.bitField0_ |= 1;
                        break;
                    case 16:
                        int value = input.readInt32();
                        switch (value) {
                            case 0:
                            case 1:
                            case 2:
                            case 3:
                            case 4:
                            case 5:
                            case 6:
                                this.action_ = value;
                                this.bitField0_ |= 2;
                                break;
                            default:
                                break;
                        }
                    case 24:
                        this.actionIndex_ = input.readInt32();
                        this.bitField0_ |= 4;
                        break;
                    case 34:
                        int i;
                        int arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 34);
                        if (this.pointers == null) {
                            i = 0;
                        } else {
                            i = this.pointers.length;
                        }
                        Pointer[] newArray = new Pointer[(i + arrayLength)];
                        if (i != 0) {
                            System.arraycopy(this.pointers, 0, newArray, 0, i);
                        }
                        while (i < newArray.length - 1) {
                            newArray[i] = new Pointer();
                            input.readMessage(newArray[i]);
                            input.readTag();
                            i++;
                        }
                        newArray[i] = new Pointer();
                        input.readMessage(newArray[i]);
                        this.pointers = newArray;
                        break;
                    case 40:
                        this.removedRedacted_ = input.readBool();
                        this.bitField0_ |= 8;
                        break;
                    case 50:
                        if (this.removedBoundingBox == null) {
                            this.removedBoundingBox = new BoundingBox();
                        }
                        input.readMessage(this.removedBoundingBox);
                        break;
                    default:
                        if (WireFormatNano.parseUnknownField(input, tag)) {
                            break;
                        }
                        return this;
                }
            }
        }
    }

    public TouchAnalyticsProto$Session setStartTimestampMillis(long value) {
        this.startTimestampMillis_ = value;
        this.bitField0_ |= 1;
        return this;
    }

    public TouchAnalyticsProto$Session setDurationMillis(long value) {
        this.durationMillis_ = value;
        this.bitField0_ |= 2;
        return this;
    }

    public TouchAnalyticsProto$Session setBuild(String value) {
        if (value == null) {
            throw new NullPointerException();
        }
        this.build_ = value;
        this.bitField0_ |= 4;
        return this;
    }

    public TouchAnalyticsProto$Session setResult(int value) {
        this.result_ = value;
        this.bitField0_ |= 8;
        return this;
    }

    public TouchAnalyticsProto$Session setTouchAreaWidth(int value) {
        this.touchAreaWidth_ = value;
        this.bitField0_ |= 16;
        return this;
    }

    public TouchAnalyticsProto$Session setTouchAreaHeight(int value) {
        this.touchAreaHeight_ = value;
        this.bitField0_ |= 32;
        return this;
    }

    public TouchAnalyticsProto$Session setType(int value) {
        this.type_ = value;
        this.bitField0_ |= 64;
        return this;
    }

    public TouchAnalyticsProto$Session() {
        clear();
    }

    public TouchAnalyticsProto$Session clear() {
        this.bitField0_ = 0;
        this.startTimestampMillis_ = 0;
        this.durationMillis_ = 0;
        this.build_ = BuildConfig.FLAVOR;
        this.result_ = 0;
        this.touchEvents = TouchEvent.emptyArray();
        this.sensorEvents = SensorEvent.emptyArray();
        this.touchAreaWidth_ = 0;
        this.touchAreaHeight_ = 0;
        this.type_ = 0;
        this.phoneEvents = PhoneEvent.emptyArray();
        this.cachedSize = -1;
        return this;
    }

    public void writeTo(CodedOutputByteBufferNano output) throws IOException {
        if ((this.bitField0_ & 1) != 0) {
            output.writeUInt64(1, this.startTimestampMillis_);
        }
        if ((this.bitField0_ & 2) != 0) {
            output.writeUInt64(2, this.durationMillis_);
        }
        if ((this.bitField0_ & 4) != 0) {
            output.writeString(3, this.build_);
        }
        if ((this.bitField0_ & 8) != 0) {
            output.writeInt32(4, this.result_);
        }
        if (this.touchEvents != null && this.touchEvents.length > 0) {
            for (TouchEvent element : this.touchEvents) {
                if (element != null) {
                    output.writeMessage(5, element);
                }
            }
        }
        if (this.sensorEvents != null && this.sensorEvents.length > 0) {
            for (SensorEvent element2 : this.sensorEvents) {
                if (element2 != null) {
                    output.writeMessage(6, element2);
                }
            }
        }
        if ((this.bitField0_ & 16) != 0) {
            output.writeInt32(9, this.touchAreaWidth_);
        }
        if ((this.bitField0_ & 32) != 0) {
            output.writeInt32(10, this.touchAreaHeight_);
        }
        if ((this.bitField0_ & 64) != 0) {
            output.writeInt32(11, this.type_);
        }
        if (this.phoneEvents != null && this.phoneEvents.length > 0) {
            for (PhoneEvent element3 : this.phoneEvents) {
                if (element3 != null) {
                    output.writeMessage(12, element3);
                }
            }
        }
        super.writeTo(output);
    }

    protected int computeSerializedSize() {
        int size = super.computeSerializedSize();
        if ((this.bitField0_ & 1) != 0) {
            size += CodedOutputByteBufferNano.computeUInt64Size(1, this.startTimestampMillis_);
        }
        if ((this.bitField0_ & 2) != 0) {
            size += CodedOutputByteBufferNano.computeUInt64Size(2, this.durationMillis_);
        }
        if ((this.bitField0_ & 4) != 0) {
            size += CodedOutputByteBufferNano.computeStringSize(3, this.build_);
        }
        if ((this.bitField0_ & 8) != 0) {
            size += CodedOutputByteBufferNano.computeInt32Size(4, this.result_);
        }
        if (this.touchEvents != null && this.touchEvents.length > 0) {
            for (TouchEvent element : this.touchEvents) {
                if (element != null) {
                    size += CodedOutputByteBufferNano.computeMessageSize(5, element);
                }
            }
        }
        if (this.sensorEvents != null && this.sensorEvents.length > 0) {
            for (SensorEvent element2 : this.sensorEvents) {
                if (element2 != null) {
                    size += CodedOutputByteBufferNano.computeMessageSize(6, element2);
                }
            }
        }
        if ((this.bitField0_ & 16) != 0) {
            size += CodedOutputByteBufferNano.computeInt32Size(9, this.touchAreaWidth_);
        }
        if ((this.bitField0_ & 32) != 0) {
            size += CodedOutputByteBufferNano.computeInt32Size(10, this.touchAreaHeight_);
        }
        if ((this.bitField0_ & 64) != 0) {
            size += CodedOutputByteBufferNano.computeInt32Size(11, this.type_);
        }
        if (this.phoneEvents != null && this.phoneEvents.length > 0) {
            for (PhoneEvent element3 : this.phoneEvents) {
                if (element3 != null) {
                    size += CodedOutputByteBufferNano.computeMessageSize(12, element3);
                }
            }
        }
        return size;
    }

    public TouchAnalyticsProto$Session mergeFrom(CodedInputByteBufferNano input) throws IOException {
        while (true) {
            int tag = input.readTag();
            int value;
            int arrayLength;
            int i;
            switch (tag) {
                case 0:
                    return this;
                case 8:
                    this.startTimestampMillis_ = input.readUInt64();
                    this.bitField0_ |= 1;
                    break;
                case 16:
                    this.durationMillis_ = input.readUInt64();
                    this.bitField0_ |= 2;
                    break;
                case 26:
                    this.build_ = input.readString();
                    this.bitField0_ |= 4;
                    break;
                case 32:
                    value = input.readInt32();
                    switch (value) {
                        case 0:
                        case 1:
                        case 2:
                            this.result_ = value;
                            this.bitField0_ |= 8;
                            break;
                        default:
                            break;
                    }
                case 42:
                    arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 42);
                    if (this.touchEvents == null) {
                        i = 0;
                    } else {
                        i = this.touchEvents.length;
                    }
                    TouchEvent[] newArray = new TouchEvent[(i + arrayLength)];
                    if (i != 0) {
                        System.arraycopy(this.touchEvents, 0, newArray, 0, i);
                    }
                    while (i < newArray.length - 1) {
                        newArray[i] = new TouchEvent();
                        input.readMessage(newArray[i]);
                        input.readTag();
                        i++;
                    }
                    newArray[i] = new TouchEvent();
                    input.readMessage(newArray[i]);
                    this.touchEvents = newArray;
                    break;
                case 50:
                    arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 50);
                    if (this.sensorEvents == null) {
                        i = 0;
                    } else {
                        i = this.sensorEvents.length;
                    }
                    SensorEvent[] newArray2 = new SensorEvent[(i + arrayLength)];
                    if (i != 0) {
                        System.arraycopy(this.sensorEvents, 0, newArray2, 0, i);
                    }
                    while (i < newArray2.length - 1) {
                        newArray2[i] = new SensorEvent();
                        input.readMessage(newArray2[i]);
                        input.readTag();
                        i++;
                    }
                    newArray2[i] = new SensorEvent();
                    input.readMessage(newArray2[i]);
                    this.sensorEvents = newArray2;
                    break;
                case 72:
                    this.touchAreaWidth_ = input.readInt32();
                    this.bitField0_ |= 16;
                    break;
                case 80:
                    this.touchAreaHeight_ = input.readInt32();
                    this.bitField0_ |= 32;
                    break;
                case 88:
                    value = input.readInt32();
                    switch (value) {
                        case 0:
                        case 1:
                        case 2:
                        case 3:
                            this.type_ = value;
                            this.bitField0_ |= 64;
                            break;
                        default:
                            break;
                    }
                case 98:
                    arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 98);
                    if (this.phoneEvents == null) {
                        i = 0;
                    } else {
                        i = this.phoneEvents.length;
                    }
                    PhoneEvent[] newArray3 = new PhoneEvent[(i + arrayLength)];
                    if (i != 0) {
                        System.arraycopy(this.phoneEvents, 0, newArray3, 0, i);
                    }
                    while (i < newArray3.length - 1) {
                        newArray3[i] = new PhoneEvent();
                        input.readMessage(newArray3[i]);
                        input.readTag();
                        i++;
                    }
                    newArray3[i] = new PhoneEvent();
                    input.readMessage(newArray3[i]);
                    this.phoneEvents = newArray3;
                    break;
                default:
                    if (WireFormatNano.parseUnknownField(input, tag)) {
                        break;
                    }
                    return this;
            }
        }
    }
}
