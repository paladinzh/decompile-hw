package com.huawei.keyguard.third.exif;

import com.huawei.keyguard.util.HwLog;
import fyusion.vislib.BuildConfig;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.Map.Entry;
import java.util.TreeMap;

class ExifParser {
    private static final short TAG_EXIF_IFD = ExifInterface.getTrueTagKey(ExifInterface.TAG_EXIF_IFD);
    private static final short TAG_GPS_IFD = ExifInterface.getTrueTagKey(ExifInterface.TAG_GPS_IFD);
    private static final short TAG_INTEROPERABILITY_IFD = ExifInterface.getTrueTagKey(ExifInterface.TAG_INTEROPERABILITY_IFD);
    private static final short TAG_JPEG_INTERCHANGE_FORMAT = ExifInterface.getTrueTagKey(ExifInterface.TAG_JPEG_INTERCHANGE_FORMAT);
    private static final short TAG_JPEG_INTERCHANGE_FORMAT_LENGTH = ExifInterface.getTrueTagKey(ExifInterface.TAG_JPEG_INTERCHANGE_FORMAT_LENGTH);
    private static final short TAG_STRIP_BYTE_COUNTS = ExifInterface.getTrueTagKey(ExifInterface.TAG_STRIP_BYTE_COUNTS);
    private static final short TAG_STRIP_OFFSETS = ExifInterface.getTrueTagKey(ExifInterface.TAG_STRIP_OFFSETS);
    private int mApp1End;
    private boolean mContainExifData = false;
    private final TreeMap<Integer, Object> mCorrespondingEvent = new TreeMap();
    private byte[] mDataAboveIfd0;
    private int mIfd0Position;
    private int mIfdStartOffset = 0;
    private int mIfdType;
    private ImageEvent mImageEvent;
    private final ExifInterface mInterface;
    private ExifTag mJpegSizeTag;
    private boolean mNeedToParseOffsetsInCurrentIfd;
    private int mNumOfTagInIfd = 0;
    private int mOffsetToApp1EndFromSOF = 0;
    private final int mOptions;
    private int mStripCount = 0;
    private ExifTag mStripSizeTag;
    private ExifTag mTag;
    private int mTiffStartPosition;
    private final CountedDataInputStream mTiffStream;

    private static class ExifTagEvent {
        boolean isRequested;
        ExifTag tag;

        ExifTagEvent(ExifTag tag, boolean isRequireByUser) {
            this.tag = tag;
            this.isRequested = isRequireByUser;
        }
    }

    private static class IfdEvent {
        int ifd;
        boolean isRequested;

        IfdEvent(int ifd, boolean isInterestedIfd) {
            this.ifd = ifd;
            this.isRequested = isInterestedIfd;
        }
    }

    private static class ImageEvent {
        int stripIndex;
        int type;

        ImageEvent(int type) {
            this.stripIndex = 0;
            this.type = type;
        }

        ImageEvent(int type, int stripIndex) {
            this.type = type;
            this.stripIndex = stripIndex;
        }
    }

    private boolean isIfdRequested(int ifdType) {
        boolean z = true;
        switch (ifdType) {
            case 0:
                if ((this.mOptions & 1) == 0) {
                    z = false;
                }
                return z;
            case 1:
                if ((this.mOptions & 2) == 0) {
                    z = false;
                }
                return z;
            case 2:
                if ((this.mOptions & 4) == 0) {
                    z = false;
                }
                return z;
            case 3:
                if ((this.mOptions & 16) == 0) {
                    z = false;
                }
                return z;
            case 4:
                if ((this.mOptions & 8) == 0) {
                    z = false;
                }
                return z;
            default:
                return false;
        }
    }

    private boolean isThumbnailRequested() {
        return (this.mOptions & 32) != 0;
    }

    private ExifParser(InputStream inputStream, int options, ExifInterface iRef) throws IOException, ExifInvalidFormatException {
        if (inputStream == null) {
            throw new IOException("Null argument inputStream to ExifParser");
        }
        this.mInterface = iRef;
        this.mContainExifData = seekTiffData(inputStream);
        this.mTiffStream = new CountedDataInputStream(inputStream);
        this.mOptions = options;
        if (this.mContainExifData) {
            parseTiffHeader();
            long offset = this.mTiffStream.readUnsignedInt();
            if (offset > 2147483647L) {
                throw new ExifInvalidFormatException("Invalid offset " + offset);
            }
            this.mIfd0Position = (int) offset;
            this.mIfdType = 0;
            if (isIfdRequested(0) || needToParseOffsetsInCurrentIfd()) {
                registerIfd(0, offset);
                if (offset != 8) {
                    this.mDataAboveIfd0 = new byte[(((int) offset) - 8)];
                    read(this.mDataAboveIfd0);
                }
            }
        }
    }

    protected static ExifParser parse(InputStream inputStream, ExifInterface iRef) throws IOException, ExifInvalidFormatException {
        return new ExifParser(inputStream, 63, iRef);
    }

    protected int next() throws IOException, ExifInvalidFormatException {
        if (!this.mContainExifData) {
            return 5;
        }
        int offset = this.mTiffStream.getReadByteCount();
        int endOfTags = (this.mIfdStartOffset + 2) + (this.mNumOfTagInIfd * 12);
        if (offset < endOfTags) {
            this.mTag = readTag();
            if (this.mTag == null) {
                return next();
            }
            if (this.mNeedToParseOffsetsInCurrentIfd) {
                checkOffsetOrImageTag(this.mTag);
            }
            return 1;
        }
        if (offset == endOfTags) {
            long ifdOffset;
            if (this.mIfdType == 0) {
                ifdOffset = readUnsignedLong();
                if ((isIfdRequested(1) || isThumbnailRequested()) && ifdOffset != 0) {
                    registerIfd(1, ifdOffset);
                }
            } else {
                int offsetSize = 4;
                if (this.mCorrespondingEvent.size() > 0) {
                    offsetSize = ((Integer) this.mCorrespondingEvent.firstEntry().getKey()).intValue() - this.mTiffStream.getReadByteCount();
                }
                if (offsetSize < 4) {
                    HwLog.w("ExifParser", "Invalid size of link to next IFD: " + offsetSize);
                } else {
                    ifdOffset = readUnsignedLong();
                    if (ifdOffset != 0) {
                        HwLog.w("ExifParser", "Invalid link to next IFD: " + ifdOffset);
                    }
                }
            }
        }
        while (this.mCorrespondingEvent.size() != 0) {
            Entry<Integer, Object> entry = this.mCorrespondingEvent.pollFirstEntry();
            ExifTagEvent event = entry.getValue();
            try {
                skipTo(((Integer) entry.getKey()).intValue());
                if (event instanceof IfdEvent) {
                    this.mIfdType = ((IfdEvent) event).ifd;
                    this.mNumOfTagInIfd = this.mTiffStream.readUnsignedShort();
                    this.mIfdStartOffset = ((Integer) entry.getKey()).intValue();
                    if (((this.mNumOfTagInIfd * 12) + this.mIfdStartOffset) + 2 > this.mApp1End) {
                        HwLog.w("ExifParser", "Invalid size of IFD " + this.mIfdType);
                        return 5;
                    }
                    this.mNeedToParseOffsetsInCurrentIfd = needToParseOffsetsInCurrentIfd();
                    if (((IfdEvent) event).isRequested) {
                        return 0;
                    }
                    skipRemainingTagsInCurrentIfd();
                } else if (event instanceof ImageEvent) {
                    this.mImageEvent = (ImageEvent) event;
                    return this.mImageEvent.type;
                } else {
                    ExifTagEvent tagEvent = event;
                    this.mTag = tagEvent.tag;
                    if (this.mTag.getDataType() != (short) 7) {
                        readFullTagValue(this.mTag);
                        checkOffsetOrImageTag(this.mTag);
                    }
                    if (tagEvent.isRequested) {
                        return 2;
                    }
                }
            } catch (IOException e) {
                HwLog.w("ExifParser", "Failed to skip to data at: " + entry.getKey() + " for " + event.getClass().getName() + ", the file may be broken.");
            }
        }
        return 5;
    }

    protected void skipRemainingTagsInCurrentIfd() throws IOException, ExifInvalidFormatException {
        int endOfTags = (this.mIfdStartOffset + 2) + (this.mNumOfTagInIfd * 12);
        int offset = this.mTiffStream.getReadByteCount();
        if (offset <= endOfTags) {
            if (this.mNeedToParseOffsetsInCurrentIfd) {
                while (offset < endOfTags) {
                    this.mTag = readTag();
                    offset += 12;
                    if (this.mTag != null) {
                        checkOffsetOrImageTag(this.mTag);
                    }
                }
            } else {
                skipTo(endOfTags);
            }
            long ifdOffset = readUnsignedLong();
            if (this.mIfdType == 0 && ((isIfdRequested(1) || isThumbnailRequested()) && ifdOffset > 0)) {
                registerIfd(1, ifdOffset);
            }
        }
    }

    private boolean needToParseOffsetsInCurrentIfd() {
        boolean z = true;
        switch (this.mIfdType) {
            case 0:
                if (!(isIfdRequested(2) || isIfdRequested(4) || isIfdRequested(3))) {
                    z = isIfdRequested(1);
                }
                return z;
            case 1:
                return isThumbnailRequested();
            case 2:
                return isIfdRequested(3);
            default:
                return false;
        }
    }

    protected ExifTag getTag() {
        return this.mTag;
    }

    protected int getCurrentIfd() {
        return this.mIfdType;
    }

    protected int getStripIndex() {
        return this.mImageEvent.stripIndex;
    }

    protected int getStripSize() {
        if (this.mStripSizeTag == null) {
            return 0;
        }
        return (int) this.mStripSizeTag.getValueAt(0);
    }

    protected int getCompressedImageSize() {
        if (this.mJpegSizeTag == null) {
            return 0;
        }
        return (int) this.mJpegSizeTag.getValueAt(0);
    }

    private void skipTo(int offset) throws IOException {
        this.mTiffStream.skipTo((long) offset);
        while (!this.mCorrespondingEvent.isEmpty() && ((Integer) this.mCorrespondingEvent.firstKey()).intValue() < offset) {
            this.mCorrespondingEvent.pollFirstEntry();
        }
    }

    protected void registerForTagValue(ExifTag tag) {
        if (tag.getOffset() >= this.mTiffStream.getReadByteCount()) {
            this.mCorrespondingEvent.put(Integer.valueOf(tag.getOffset()), new ExifTagEvent(tag, true));
        }
    }

    private void registerIfd(int ifdType, long offset) {
        this.mCorrespondingEvent.put(Integer.valueOf((int) offset), new IfdEvent(ifdType, isIfdRequested(ifdType)));
    }

    private void registerCompressedImage(long offset) {
        this.mCorrespondingEvent.put(Integer.valueOf((int) offset), new ImageEvent(3));
    }

    private void registerUncompressedStrip(int stripIndex, long offset) {
        this.mCorrespondingEvent.put(Integer.valueOf((int) offset), new ImageEvent(4, stripIndex));
    }

    private ExifTag readTag() throws IOException, ExifInvalidFormatException {
        short tagId = this.mTiffStream.readShort();
        short dataFormat = this.mTiffStream.readShort();
        long numOfComp = this.mTiffStream.readUnsignedInt();
        if (numOfComp > 2147483647L) {
            throw new ExifInvalidFormatException("Number of component is larger then Integer.MAX_VALUE");
        } else if (ExifTag.isValidType(dataFormat)) {
            ExifTag tag = new ExifTag(tagId, dataFormat, (int) numOfComp, this.mIfdType, ((int) numOfComp) != 0);
            int dataSize = tag.getDataSize();
            if (dataSize > 4) {
                long offset = this.mTiffStream.readUnsignedInt();
                if (offset > 2147483647L) {
                    throw new ExifInvalidFormatException("offset is larger then Integer.MAX_VALUE");
                } else if (offset >= ((long) this.mIfd0Position) || dataFormat != (short) 7) {
                    tag.setOffset((int) offset);
                } else {
                    byte[] buf = new byte[((int) numOfComp)];
                    if (this.mDataAboveIfd0 != null) {
                        System.arraycopy(this.mDataAboveIfd0, ((int) offset) - 8, buf, 0, (int) numOfComp);
                        tag.setValue(buf);
                    }
                }
            } else {
                boolean defCount = tag.hasDefinedCount();
                tag.setHasDefinedCount(false);
                readFullTagValue(tag);
                tag.setHasDefinedCount(defCount);
                HwLog.i("ExifParser", "mTiffStream skip = " + this.mTiffStream.skip((long) (4 - dataSize)));
                tag.setOffset(this.mTiffStream.getReadByteCount() - 4);
            }
            return tag;
        } else {
            long skip = this.mTiffStream.skip(4);
            HwLog.w("ExifParser", String.format("Tag %04x: Invalid data type %d, skip %d", new Object[]{Short.valueOf(tagId), Short.valueOf(dataFormat), Long.valueOf(skip)}));
            return null;
        }
    }

    private void checkOffsetOrImageTag(ExifTag tag) {
        if (tag.getComponentCount() != 0) {
            short tid = tag.getTagId();
            int ifd = tag.getIfd();
            if (tid == TAG_EXIF_IFD && checkAllowed(ifd, ExifInterface.TAG_EXIF_IFD)) {
                if (isIfdRequested(2) || isIfdRequested(3)) {
                    registerIfd(2, tag.getValueAt(0));
                }
            } else if (tid == TAG_GPS_IFD && checkAllowed(ifd, ExifInterface.TAG_GPS_IFD)) {
                if (isIfdRequested(4)) {
                    registerIfd(4, tag.getValueAt(0));
                }
            } else if (tid == TAG_INTEROPERABILITY_IFD && checkAllowed(ifd, ExifInterface.TAG_INTEROPERABILITY_IFD)) {
                if (isIfdRequested(3)) {
                    registerIfd(3, tag.getValueAt(0));
                }
            } else if (tid == TAG_JPEG_INTERCHANGE_FORMAT && checkAllowed(ifd, ExifInterface.TAG_JPEG_INTERCHANGE_FORMAT)) {
                if (isThumbnailRequested()) {
                    registerCompressedImage(tag.getValueAt(0));
                }
            } else if (tid == TAG_JPEG_INTERCHANGE_FORMAT_LENGTH && checkAllowed(ifd, ExifInterface.TAG_JPEG_INTERCHANGE_FORMAT_LENGTH)) {
                if (isThumbnailRequested()) {
                    this.mJpegSizeTag = tag;
                }
            } else if (tid == TAG_STRIP_OFFSETS && checkAllowed(ifd, ExifInterface.TAG_STRIP_OFFSETS)) {
                if (isThumbnailRequested()) {
                    if (tag.hasValue()) {
                        for (int i = 0; i < tag.getComponentCount(); i++) {
                            registerUncompressedStrip(i, tag.getValueAt(i));
                        }
                    } else {
                        this.mCorrespondingEvent.put(Integer.valueOf(tag.getOffset()), new ExifTagEvent(tag, false));
                    }
                }
            } else if (tid == TAG_STRIP_BYTE_COUNTS && checkAllowed(ifd, ExifInterface.TAG_STRIP_BYTE_COUNTS) && isThumbnailRequested() && tag.hasValue()) {
                this.mStripSizeTag = tag;
            }
        }
    }

    private boolean checkAllowed(int ifd, int tagId) {
        int info = this.mInterface.getTagInfo().get(tagId);
        if (info == 0) {
            return false;
        }
        return ExifInterface.isIfdAllowed(info, ifd);
    }

    protected void readFullTagValue(ExifTag tag) throws IOException {
        int[] value;
        int n;
        int i;
        long[] value2;
        Rational[] value3;
        short type = tag.getDataType();
        if (!(type == (short) 2 || type == (short) 7)) {
            if (type == (short) 1) {
            }
            switch (tag.getDataType()) {
                case (short) 1:
                case (short) 7:
                    byte[] buf = new byte[tag.getComponentCount()];
                    read(buf);
                    tag.setValue(buf);
                    return;
                case (short) 2:
                    tag.setValue(readString(tag.getComponentCount()));
                    return;
                case (short) 3:
                    value = new int[tag.getComponentCount()];
                    n = value.length;
                    for (i = 0; i < n; i++) {
                        value[i] = readUnsignedShort();
                    }
                    tag.setValue(value);
                    return;
                case (short) 4:
                    value2 = new long[tag.getComponentCount()];
                    n = value2.length;
                    for (i = 0; i < n; i++) {
                        value2[i] = readUnsignedLong();
                    }
                    tag.setValue(value2);
                    return;
                case (short) 5:
                    value3 = new Rational[tag.getComponentCount()];
                    n = value3.length;
                    for (i = 0; i < n; i++) {
                        value3[i] = readUnsignedRational();
                    }
                    tag.setValue(value3);
                    return;
                case (short) 9:
                    value = new int[tag.getComponentCount()];
                    n = value.length;
                    for (i = 0; i < n; i++) {
                        value[i] = readLong();
                    }
                    tag.setValue(value);
                    return;
                case (short) 10:
                    value3 = new Rational[tag.getComponentCount()];
                    n = value3.length;
                    for (i = 0; i < n; i++) {
                        value3[i] = readRational();
                    }
                    tag.setValue(value3);
                    return;
                default:
                    return;
            }
        }
        int size = tag.getComponentCount();
        if (this.mCorrespondingEvent.size() > 0 && ((Integer) this.mCorrespondingEvent.firstEntry().getKey()).intValue() < this.mTiffStream.getReadByteCount() + size) {
            Object event = this.mCorrespondingEvent.firstEntry().getValue();
            if (event instanceof ImageEvent) {
                HwLog.w("ExifParser", "Thumbnail overlaps value for tag: \n" + tag.toString());
                HwLog.w("ExifParser", "Invalid thumbnail offset: " + this.mCorrespondingEvent.pollFirstEntry().getKey());
            } else {
                if (event instanceof IfdEvent) {
                    HwLog.w("ExifParser", "Ifd " + ((IfdEvent) event).ifd + " overlaps value for tag: \n" + tag.toString());
                } else if (event instanceof ExifTagEvent) {
                    HwLog.w("ExifParser", "Tag value for tag: \n" + ((ExifTagEvent) event).tag.toString() + " overlaps value for tag: \n" + tag.toString());
                }
                size = ((Integer) this.mCorrespondingEvent.firstEntry().getKey()).intValue() - this.mTiffStream.getReadByteCount();
                HwLog.w("ExifParser", "Invalid size of tag: \n" + tag.toString() + " setting count to: " + size);
                tag.forceSetComponentCount(size);
            }
        }
        switch (tag.getDataType()) {
            case (short) 1:
            case (short) 7:
                byte[] buf2 = new byte[tag.getComponentCount()];
                read(buf2);
                tag.setValue(buf2);
                return;
            case (short) 2:
                tag.setValue(readString(tag.getComponentCount()));
                return;
            case (short) 3:
                value = new int[tag.getComponentCount()];
                n = value.length;
                for (i = 0; i < n; i++) {
                    value[i] = readUnsignedShort();
                }
                tag.setValue(value);
                return;
            case (short) 4:
                value2 = new long[tag.getComponentCount()];
                n = value2.length;
                for (i = 0; i < n; i++) {
                    value2[i] = readUnsignedLong();
                }
                tag.setValue(value2);
                return;
            case (short) 5:
                value3 = new Rational[tag.getComponentCount()];
                n = value3.length;
                for (i = 0; i < n; i++) {
                    value3[i] = readUnsignedRational();
                }
                tag.setValue(value3);
                return;
            case (short) 9:
                value = new int[tag.getComponentCount()];
                n = value.length;
                for (i = 0; i < n; i++) {
                    value[i] = readLong();
                }
                tag.setValue(value);
                return;
            case (short) 10:
                value3 = new Rational[tag.getComponentCount()];
                n = value3.length;
                for (i = 0; i < n; i++) {
                    value3[i] = readRational();
                }
                tag.setValue(value3);
                return;
            default:
                return;
        }
    }

    private void parseTiffHeader() throws IOException, ExifInvalidFormatException {
        short byteOrder = this.mTiffStream.readShort();
        if ((short) 18761 == byteOrder) {
            this.mTiffStream.setByteOrder(ByteOrder.LITTLE_ENDIAN);
        } else if ((short) 19789 == byteOrder) {
            this.mTiffStream.setByteOrder(ByteOrder.BIG_ENDIAN);
        } else {
            throw new ExifInvalidFormatException("Invalid TIFF header");
        }
        if (this.mTiffStream.readShort() != (short) 42) {
            throw new ExifInvalidFormatException("Invalid TIFF header");
        }
    }

    private boolean seekTiffData(InputStream inputStream) throws IOException, ExifInvalidFormatException {
        CountedDataInputStream dataStream = new CountedDataInputStream(inputStream);
        if (dataStream.readShort() != (short) -40) {
            throw new ExifInvalidFormatException("Invalid JPEG format");
        }
        short marker = dataStream.readShort();
        while (marker != (short) -39 && !JpegHeader.isSofMarker(marker)) {
            int length = dataStream.readUnsignedShort();
            if (marker == (short) -31 && length >= 8) {
                int header = dataStream.readInt();
                short headerTail = dataStream.readShort();
                length -= 6;
                if (header == 1165519206 && headerTail == (short) 0) {
                    this.mTiffStartPosition = dataStream.getReadByteCount();
                    this.mApp1End = length;
                    this.mOffsetToApp1EndFromSOF = this.mTiffStartPosition + this.mApp1End;
                    return true;
                }
            }
            if (length < 2 || ((long) (length - 2)) != dataStream.skip((long) (length - 2))) {
                HwLog.w("ExifParser", "Invalid JPEG format.");
                return false;
            }
            marker = dataStream.readShort();
        }
        return false;
    }

    protected int read(byte[] buffer) throws IOException {
        return this.mTiffStream.read(buffer);
    }

    protected String readString(int n) throws IOException {
        return readString(n, ExifTag.US_ASCII);
    }

    protected String readString(int n, Charset charset) throws IOException {
        if (n > 0) {
            return this.mTiffStream.readString(n, charset);
        }
        return BuildConfig.FLAVOR;
    }

    protected int readUnsignedShort() throws IOException {
        return this.mTiffStream.readShort() & 65535;
    }

    protected long readUnsignedLong() throws IOException {
        return ((long) readLong()) & 4294967295L;
    }

    protected Rational readUnsignedRational() throws IOException {
        return new Rational(readUnsignedLong(), readUnsignedLong());
    }

    protected int readLong() throws IOException {
        return this.mTiffStream.readInt();
    }

    protected Rational readRational() throws IOException {
        return new Rational((long) readLong(), (long) readLong());
    }

    protected ByteOrder getByteOrder() {
        return this.mTiffStream.getByteOrder();
    }
}
