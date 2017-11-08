package com.android.mms.exif;

import java.io.BufferedOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

class ExifOutputStream extends FilterOutputStream {
    private final ByteBuffer mBuffer = ByteBuffer.allocate(4);
    private int mByteToCopy;
    private int mByteToSkip;
    private ExifData mExifData;
    private final ExifInterface mInterface;
    private final byte[] mSingleByteArray = new byte[1];
    private int mState = 0;

    protected ExifOutputStream(OutputStream ou, ExifInterface iRef) {
        super(new BufferedOutputStream(ou, 65536));
        this.mInterface = iRef;
    }

    protected void setExifData(ExifData exifData) {
        this.mExifData = exifData;
    }

    private int requestByteToBuffer(int requestByteCount, byte[] buffer, int offset, int length) {
        int byteNeeded = requestByteCount - this.mBuffer.position();
        int byteToRead = length > byteNeeded ? byteNeeded : length;
        this.mBuffer.put(buffer, offset, byteToRead);
        return byteToRead;
    }

    public void write(byte[] buffer, int offset, int length) throws IOException {
        while (true) {
            if ((this.mByteToSkip > 0 || this.mByteToCopy > 0 || this.mState != 2) && length > 0) {
                int byteToProcess;
                if (this.mByteToSkip > 0) {
                    if (length > this.mByteToSkip) {
                        byteToProcess = this.mByteToSkip;
                    } else {
                        byteToProcess = length;
                    }
                    length -= byteToProcess;
                    this.mByteToSkip -= byteToProcess;
                    offset += byteToProcess;
                }
                if (this.mByteToCopy > 0) {
                    byteToProcess = length > this.mByteToCopy ? this.mByteToCopy : length;
                    this.out.write(buffer, offset, byteToProcess);
                    length -= byteToProcess;
                    this.mByteToCopy -= byteToProcess;
                    offset += byteToProcess;
                }
                if (length != 0) {
                    int byteRead;
                    switch (this.mState) {
                        case 0:
                            byteRead = requestByteToBuffer(2, buffer, offset, length);
                            offset += byteRead;
                            length -= byteRead;
                            if (this.mBuffer.position() >= 2) {
                                this.mBuffer.rewind();
                                if (this.mBuffer.getShort() == (short) -40) {
                                    this.out.write(this.mBuffer.array(), 0, 2);
                                    this.mState = 1;
                                    this.mBuffer.rewind();
                                    writeExifData();
                                    break;
                                }
                                throw new IOException("Not a valid jpeg image, cannot write exif");
                            }
                            return;
                        case 1:
                            byteRead = requestByteToBuffer(4, buffer, offset, length);
                            offset += byteRead;
                            length -= byteRead;
                            if (this.mBuffer.position() == 2 && this.mBuffer.getShort() == (short) -39) {
                                this.out.write(this.mBuffer.array(), 0, 2);
                                this.mBuffer.rewind();
                            }
                            if (this.mBuffer.position() >= 4) {
                                this.mBuffer.rewind();
                                short marker = this.mBuffer.getShort();
                                if (marker == (short) -31) {
                                    this.mByteToSkip = (this.mBuffer.getShort() & 65535) - 2;
                                    this.mState = 2;
                                } else if (JpegHeader.isSofMarker(marker)) {
                                    this.out.write(this.mBuffer.array(), 0, 4);
                                    this.mState = 2;
                                } else {
                                    this.out.write(this.mBuffer.array(), 0, 4);
                                    this.mByteToCopy = (this.mBuffer.getShort() & 65535) - 2;
                                }
                                this.mBuffer.rewind();
                                break;
                            }
                            return;
                        default:
                            break;
                    }
                }
                return;
            }
            if (length > 0) {
                this.out.write(buffer, offset, length);
            }
            return;
        }
    }

    public void write(int oneByte) throws IOException {
        this.mSingleByteArray[0] = (byte) (oneByte & 255);
        write(this.mSingleByteArray);
    }

    public void write(byte[] buffer) throws IOException {
        write(buffer, 0, buffer.length);
    }

    private void writeExifData() throws IOException {
        if (this.mExifData != null) {
            ArrayList<ExifTag> nullTags = stripNullValueTags(this.mExifData);
            createRequiredIfdAndTag();
            int exifSize = calculateAllOffset();
            if (exifSize + 8 > 65535) {
                throw new IOException("Exif header is too large (>64Kb)");
            }
            OrderedDataOutputStream dataOutputStream = new OrderedDataOutputStream(this.out);
            dataOutputStream.setByteOrder(ByteOrder.BIG_ENDIAN);
            dataOutputStream.writeShort((short) -31);
            dataOutputStream.writeShort((short) (exifSize + 8));
            dataOutputStream.writeInt(1165519206);
            dataOutputStream.writeShort((short) 0);
            if (this.mExifData.getByteOrder() == ByteOrder.BIG_ENDIAN) {
                dataOutputStream.writeShort((short) 19789);
            } else {
                dataOutputStream.writeShort((short) 18761);
            }
            dataOutputStream.setByteOrder(this.mExifData.getByteOrder());
            dataOutputStream.writeShort((short) 42);
            dataOutputStream.writeInt(8);
            writeAllTags(dataOutputStream);
            writeThumbnail(dataOutputStream);
            for (ExifTag t : nullTags) {
                this.mExifData.addTag(t);
            }
        }
    }

    private ArrayList<ExifTag> stripNullValueTags(ExifData data) {
        ArrayList<ExifTag> nullTags = new ArrayList();
        List<ExifTag> exifTagList = data.getAllTags();
        if (exifTagList == null) {
            return nullTags;
        }
        for (ExifTag t : exifTagList) {
            if (t.getValue() == null && !ExifInterface.isOffsetTag(t.getTagId())) {
                data.removeTag(t.getTagId(), t.getIfd());
                nullTags.add(t);
            }
        }
        return nullTags;
    }

    private void writeThumbnail(OrderedDataOutputStream dataOutputStream) throws IOException {
        if (this.mExifData.hasCompressedThumbnail()) {
            dataOutputStream.write(this.mExifData.getCompressedThumbnail());
        } else if (this.mExifData.hasUncompressedStrip()) {
            for (int i = 0; i < this.mExifData.getStripCount(); i++) {
                dataOutputStream.write(this.mExifData.getStrip(i));
            }
        }
    }

    private void writeAllTags(OrderedDataOutputStream dataOutputStream) throws IOException {
        writeIfd(this.mExifData.getIfdData(0), dataOutputStream);
        writeIfd(this.mExifData.getIfdData(2), dataOutputStream);
        IfdData interoperabilityIfd = this.mExifData.getIfdData(3);
        if (interoperabilityIfd != null) {
            writeIfd(interoperabilityIfd, dataOutputStream);
        }
        IfdData gpsIfd = this.mExifData.getIfdData(4);
        if (gpsIfd != null) {
            writeIfd(gpsIfd, dataOutputStream);
        }
        if (this.mExifData.getIfdData(1) != null) {
            writeIfd(this.mExifData.getIfdData(1), dataOutputStream);
        }
    }

    private void writeIfd(IfdData ifd, OrderedDataOutputStream dataOutputStream) throws IOException {
        int i = 0;
        ExifTag[] tags = ifd.getAllTags();
        dataOutputStream.writeShort((short) tags.length);
        for (ExifTag tag : tags) {
            ExifTag tag2;
            dataOutputStream.writeShort(tag2.getTagId());
            dataOutputStream.writeShort(tag2.getDataType());
            dataOutputStream.writeInt(tag2.getComponentCount());
            if (tag2.getDataSize() > 4) {
                dataOutputStream.writeInt(tag2.getOffset());
            } else {
                writeTagValue(tag2, dataOutputStream);
                int n = 4 - tag2.getDataSize();
                for (int i2 = 0; i2 < n; i2++) {
                    dataOutputStream.write(0);
                }
            }
        }
        dataOutputStream.writeInt(ifd.getOffsetToNextIfd());
        int length = tags.length;
        while (i < length) {
            tag2 = tags[i];
            if (tag2.getDataSize() > 4) {
                writeTagValue(tag2, dataOutputStream);
            }
            i++;
        }
    }

    private int calculateOffsetOfIfd(IfdData ifd, int offset) {
        offset += ((ifd.getTagCount() * 12) + 2) + 4;
        for (ExifTag tag : ifd.getAllTags()) {
            if (tag.getDataSize() > 4) {
                tag.setOffset(offset);
                offset += tag.getDataSize();
            }
        }
        return offset;
    }

    private void createRequiredIfdAndTag() throws IOException {
        IfdData ifd0 = this.mExifData.getIfdData(0);
        if (ifd0 == null) {
            ifd0 = new IfdData(0);
            this.mExifData.addIfdData(ifd0);
        }
        ExifTag exifOffsetTag = this.mInterface.buildUninitializedTag(ExifInterface.TAG_EXIF_IFD);
        if (exifOffsetTag == null) {
            throw new IOException("No definition for crucial exif tag: " + ExifInterface.TAG_EXIF_IFD);
        }
        ifd0.setTag(exifOffsetTag);
        IfdData exifIfd = this.mExifData.getIfdData(2);
        if (exifIfd == null) {
            exifIfd = new IfdData(2);
            this.mExifData.addIfdData(exifIfd);
        }
        if (this.mExifData.getIfdData(4) != null) {
            ExifTag gpsOffsetTag = this.mInterface.buildUninitializedTag(ExifInterface.TAG_GPS_IFD);
            if (gpsOffsetTag == null) {
                throw new IOException("No definition for crucial exif tag: " + ExifInterface.TAG_GPS_IFD);
            }
            ifd0.setTag(gpsOffsetTag);
        }
        if (this.mExifData.getIfdData(3) != null) {
            ExifTag interOffsetTag = this.mInterface.buildUninitializedTag(ExifInterface.TAG_INTEROPERABILITY_IFD);
            if (interOffsetTag == null) {
                throw new IOException("No definition for crucial exif tag: " + ExifInterface.TAG_INTEROPERABILITY_IFD);
            }
            exifIfd.setTag(interOffsetTag);
        }
        IfdData ifd1 = this.mExifData.getIfdData(1);
        ExifTag offsetTag;
        ExifTag lengthTag;
        if (this.mExifData.hasCompressedThumbnail()) {
            if (ifd1 == null) {
                ifd1 = new IfdData(1);
                this.mExifData.addIfdData(ifd1);
            }
            offsetTag = this.mInterface.buildUninitializedTag(ExifInterface.TAG_JPEG_INTERCHANGE_FORMAT);
            if (offsetTag == null) {
                throw new IOException("No definition for crucial exif tag: " + ExifInterface.TAG_JPEG_INTERCHANGE_FORMAT);
            }
            ifd1.setTag(offsetTag);
            lengthTag = this.mInterface.buildUninitializedTag(ExifInterface.TAG_JPEG_INTERCHANGE_FORMAT_LENGTH);
            if (lengthTag == null) {
                throw new IOException("No definition for crucial exif tag: " + ExifInterface.TAG_JPEG_INTERCHANGE_FORMAT_LENGTH);
            }
            lengthTag.setValue(this.mExifData.getCompressedThumbnail().length);
            ifd1.setTag(lengthTag);
            ifd1.removeTag(ExifInterface.getTrueTagKey(ExifInterface.TAG_STRIP_OFFSETS));
            ifd1.removeTag(ExifInterface.getTrueTagKey(ExifInterface.TAG_STRIP_BYTE_COUNTS));
        } else if (this.mExifData.hasUncompressedStrip()) {
            if (ifd1 == null) {
                ifd1 = new IfdData(1);
                this.mExifData.addIfdData(ifd1);
            }
            int stripCount = this.mExifData.getStripCount();
            offsetTag = this.mInterface.buildUninitializedTag(ExifInterface.TAG_STRIP_OFFSETS);
            if (offsetTag == null) {
                throw new IOException("No definition for crucial exif tag: " + ExifInterface.TAG_STRIP_OFFSETS);
            }
            lengthTag = this.mInterface.buildUninitializedTag(ExifInterface.TAG_STRIP_BYTE_COUNTS);
            if (lengthTag == null) {
                throw new IOException("No definition for crucial exif tag: " + ExifInterface.TAG_STRIP_BYTE_COUNTS);
            }
            long[] lengths = new long[stripCount];
            for (int i = 0; i < this.mExifData.getStripCount(); i++) {
                lengths[i] = (long) this.mExifData.getStrip(i).length;
            }
            lengthTag.setValue(lengths);
            ifd1.setTag(offsetTag);
            ifd1.setTag(lengthTag);
            ifd1.removeTag(ExifInterface.getTrueTagKey(ExifInterface.TAG_JPEG_INTERCHANGE_FORMAT));
            ifd1.removeTag(ExifInterface.getTrueTagKey(ExifInterface.TAG_JPEG_INTERCHANGE_FORMAT_LENGTH));
        } else if (ifd1 != null) {
            ifd1.removeTag(ExifInterface.getTrueTagKey(ExifInterface.TAG_STRIP_OFFSETS));
            ifd1.removeTag(ExifInterface.getTrueTagKey(ExifInterface.TAG_STRIP_BYTE_COUNTS));
            ifd1.removeTag(ExifInterface.getTrueTagKey(ExifInterface.TAG_JPEG_INTERCHANGE_FORMAT));
            ifd1.removeTag(ExifInterface.getTrueTagKey(ExifInterface.TAG_JPEG_INTERCHANGE_FORMAT_LENGTH));
        }
    }

    private int calculateAllOffset() {
        IfdData ifd0 = this.mExifData.getIfdData(0);
        int offset = calculateOffsetOfIfd(ifd0, 8);
        ifd0.getTag(ExifInterface.getTrueTagKey(ExifInterface.TAG_EXIF_IFD)).setValue(offset);
        IfdData exifIfd = this.mExifData.getIfdData(2);
        offset = calculateOffsetOfIfd(exifIfd, offset);
        IfdData interIfd = this.mExifData.getIfdData(3);
        if (interIfd != null) {
            exifIfd.getTag(ExifInterface.getTrueTagKey(ExifInterface.TAG_INTEROPERABILITY_IFD)).setValue(offset);
            offset = calculateOffsetOfIfd(interIfd, offset);
        }
        IfdData gpsIfd = this.mExifData.getIfdData(4);
        if (gpsIfd != null) {
            ifd0.getTag(ExifInterface.getTrueTagKey(ExifInterface.TAG_GPS_IFD)).setValue(offset);
            offset = calculateOffsetOfIfd(gpsIfd, offset);
        }
        IfdData ifd1 = this.mExifData.getIfdData(1);
        if (ifd1 != null) {
            ifd0.setOffsetToNextIfd(offset);
            offset = calculateOffsetOfIfd(ifd1, offset);
        }
        if (this.mExifData.hasCompressedThumbnail()) {
            if (ifd1 != null) {
                ifd1.getTag(ExifInterface.getTrueTagKey(ExifInterface.TAG_JPEG_INTERCHANGE_FORMAT)).setValue(offset);
            }
            return offset + this.mExifData.getCompressedThumbnail().length;
        } else if (!this.mExifData.hasUncompressedStrip()) {
            return offset;
        } else {
            long[] offsets = new long[this.mExifData.getStripCount()];
            for (int i = 0; i < this.mExifData.getStripCount(); i++) {
                offsets[i] = (long) offset;
                offset += this.mExifData.getStrip(i).length;
            }
            if (ifd1 == null) {
                return offset;
            }
            ifd1.getTag(ExifInterface.getTrueTagKey(ExifInterface.TAG_STRIP_OFFSETS)).setValue(offsets);
            return offset;
        }
    }

    static void writeTagValue(ExifTag tag, OrderedDataOutputStream dataOutputStream) throws IOException {
        byte[] buf;
        int n;
        int i;
        switch (tag.getDataType()) {
            case (short) 1:
            case (short) 7:
                buf = new byte[tag.getComponentCount()];
                tag.getBytes(buf);
                dataOutputStream.write(buf);
                return;
            case (short) 2:
                buf = tag.getStringByte();
                if (buf.length == tag.getComponentCount()) {
                    buf[buf.length - 1] = (byte) 0;
                    dataOutputStream.write(buf);
                    return;
                }
                dataOutputStream.write(buf);
                dataOutputStream.write(0);
                return;
            case (short) 3:
                n = tag.getComponentCount();
                for (i = 0; i < n; i++) {
                    dataOutputStream.writeShort((short) ((int) tag.getValueAt(i)));
                }
                return;
            case (short) 4:
            case (short) 9:
                n = tag.getComponentCount();
                for (i = 0; i < n; i++) {
                    dataOutputStream.writeInt((int) tag.getValueAt(i));
                }
                return;
            case (short) 5:
            case (short) 10:
                n = tag.getComponentCount();
                for (i = 0; i < n; i++) {
                    dataOutputStream.writeRational(tag.getRational(i));
                }
                return;
            default:
                return;
        }
    }
}
