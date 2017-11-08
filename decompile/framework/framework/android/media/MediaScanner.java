package android.media;

import android.app.AlarmManager;
import android.common.HwFrameworkFactory;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.drm.DrmManagerClient;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.hardware.Camera.Parameters;
import android.hwtheme.HwThemeManager;
import android.media.MediaFile.MediaFileType;
import android.mtp.MtpConstants;
import android.net.ProxyInfo;
import android.net.Uri;
import android.net.Uri.Builder;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.Environment;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.AudioColumns;
import android.provider.MediaStore.Audio.Media;
import android.provider.MediaStore.Audio.Playlists;
import android.provider.MediaStore.Audio.Playlists.Members;
import android.provider.MediaStore.Files;
import android.provider.MediaStore.Files.FileColumns;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Images.ImageColumns;
import android.provider.MediaStore.Images.Thumbnails;
import android.provider.MediaStore.MediaColumns;
import android.provider.MediaStore.Video;
import android.provider.MediaStore.Video.VideoColumns;
import android.provider.Settings.SettingNotFoundException;
import android.provider.Settings.System;
import android.sax.ElementListener;
import android.sax.RootElement;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;
import dalvik.system.CloseGuard;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

public class MediaScanner implements AutoCloseable {
    private static final String ALARMS_DIR = "/alarms/";
    private static final int DATE_MODIFIED_PLAYLISTS_COLUMN_INDEX = 2;
    private static final String DEFAULT_RINGTONE_PROPERTY_PREFIX = "ro.config.";
    private static final boolean ENABLE_BULK_INSERTS = true;
    private static final int FILES_PRESCAN_DATE_MODIFIED_COLUMN_INDEX = 3;
    private static final int FILES_PRESCAN_FORMAT_COLUMN_INDEX = 2;
    private static final int FILES_PRESCAN_ID_COLUMN_INDEX = 0;
    private static final int FILES_PRESCAN_PATH_COLUMN_INDEX = 1;
    private static final String[] FILES_PRESCAN_PROJECTION = new String[]{"_id", "_data", FileColumns.FORMAT, "date_modified"};
    private static final String[] ID3_GENRES = new String[]{"Blues", "Classic Rock", "Country", "Dance", "Disco", "Funk", "Grunge", "Hip-Hop", "Jazz", "Metal", "New Age", "Oldies", "Other", "Pop", "R&B", "Rap", "Reggae", "Rock", "Techno", "Industrial", "Alternative", "Ska", "Death Metal", "Pranks", "Soundtrack", "Euro-Techno", "Ambient", "Trip-Hop", "Vocal", "Jazz+Funk", "Fusion", "Trance", "Classical", "Instrumental", "Acid", "House", "Game", "Sound Clip", "Gospel", "Noise", "AlternRock", "Bass", "Soul", "Punk", "Space", "Meditative", "Instrumental Pop", "Instrumental Rock", "Ethnic", "Gothic", "Darkwave", "Techno-Industrial", "Electronic", "Pop-Folk", "Eurodance", "Dream", "Southern Rock", "Comedy", "Cult", "Gangsta", "Top 40", "Christian Rap", "Pop/Funk", "Jungle", "Native American", "Cabaret", "New Wave", "Psychadelic", "Rave", "Showtunes", "Trailer", "Lo-Fi", "Tribal", "Acid Punk", "Acid Jazz", "Polka", "Retro", "Musical", "Rock & Roll", "Hard Rock", "Folk", "Folk-Rock", "National Folk", "Swing", "Fast Fusion", "Bebob", "Latin", "Revival", "Celtic", "Bluegrass", "Avantgarde", "Gothic Rock", "Progressive Rock", "Psychedelic Rock", "Symphonic Rock", "Slow Rock", "Big Band", "Chorus", "Easy Listening", "Acoustic", "Humour", "Speech", "Chanson", "Opera", "Chamber Music", "Sonata", "Symphony", "Booty Bass", "Primus", "Porn Groove", "Satire", "Slow Jam", "Club", "Tango", "Samba", "Folklore", "Ballad", "Power Ballad", "Rhythmic Soul", "Freestyle", "Duet", "Punk Rock", "Drum Solo", "A capella", "Euro-House", "Dance Hall", "Goa", "Drum & Bass", "Club-House", "Hardcore", "Terror", "Indie", "Britpop", null, "Polsk Punk", "Beat", "Christian Gangsta", "Heavy Metal", "Black Metal", "Crossover", "Contemporary Christian", "Christian Rock", "Merengue", "Salsa", "Thrash Metal", "Anime", "JPop", "Synthpop"};
    private static final int ID_PLAYLISTS_COLUMN_INDEX = 0;
    private static final String[] ID_PROJECTION = new String[]{"_id"};
    private static final int MAX_ENTRY_SIZE = 40000;
    private static final String MUSIC_DIR = "/music/";
    private static final String NOTIFICATIONS_DIR = "/notifications/";
    private static final int PATH_PLAYLISTS_COLUMN_INDEX = 1;
    private static final String[] PLAYLIST_MEMBERS_PROJECTION = new String[]{Members.PLAYLIST_ID};
    private static final String PODCAST_DIR = "/podcasts/";
    private static final String RINGTONES_DIR = "/ringtones/";
    private static final String TAG = "MediaScanner";
    private static HashMap<String, String> mMediaPaths = new HashMap();
    private static HashMap<String, String> mNoMediaPaths = new HashMap();
    private final Uri mAudioUri;
    private final Options mBitmapOptions = new Options();
    private final MyMediaScannerClient mClient = new MyMediaScannerClient();
    private final CloseGuard mCloseGuard = CloseGuard.get();
    private final AtomicBoolean mClosed = new AtomicBoolean();
    private final Context mContext;
    private String mDefaultAlarmAlertFilename;
    private boolean mDefaultAlarmSet;
    private String mDefaultNotificationFilename;
    private boolean mDefaultNotificationSet;
    private String mDefaultRingtoneFilename;
    private boolean mDefaultRingtoneSet;
    private DrmManagerClient mDrmManagerClient = null;
    private String mExtStroagePath;
    private HashMap<String, FileEntry> mFileCache;
    private final Uri mFilesUri;
    private final Uri mFilesUriNoNotify;
    private final Uri mImagesUri;
    private MediaInserter mMediaInserter;
    private ContentProviderClient mMediaProvider;
    private int mMtpObjectHandle;
    private long mNativeContext;
    private int mOriginalCount;
    private final String mPackageName;
    private final ArrayList<FileEntry> mPlayLists = new ArrayList();
    private final ArrayList<PlaylistEntry> mPlaylistEntries = new ArrayList();
    private final Uri mPlaylistsUri;
    private final boolean mProcessGenres;
    private final boolean mProcessPlaylists;
    private boolean mSkipExternelQuery = false;
    private final Uri mThumbsUri;
    private final Uri mVideoUri;
    private final String mVolumeName;

    private static class FileEntry {
        int mFormat;
        long mLastModified;
        boolean mLastModifiedChanged = false;
        String mPath;
        long mRowId;

        FileEntry(long rowId, String path, long lastModified, int format) {
            this.mRowId = rowId;
            this.mPath = path;
            this.mLastModified = lastModified;
            this.mFormat = format;
        }

        public String toString() {
            return this.mPath + " mRowId: " + this.mRowId;
        }
    }

    static class MediaBulkDeleter {
        final Uri mBaseUri;
        final ContentProviderClient mProvider;
        ArrayList<String> whereArgs = new ArrayList(100);
        StringBuilder whereClause = new StringBuilder();

        public MediaBulkDeleter(ContentProviderClient provider, Uri baseUri) {
            this.mProvider = provider;
            this.mBaseUri = baseUri;
        }

        public void delete(long id) throws RemoteException {
            if (this.whereClause.length() != 0) {
                this.whereClause.append(",");
            }
            this.whereClause.append("?");
            this.whereArgs.add(ProxyInfo.LOCAL_EXCL_LIST + id);
            if (this.whereArgs.size() > 100) {
                flush();
            }
        }

        public void flush() throws RemoteException {
            int size = this.whereArgs.size();
            if (size > 0) {
                int numrows = this.mProvider.delete(this.mBaseUri, "_id IN (" + this.whereClause.toString() + ")", (String[]) this.whereArgs.toArray(new String[size]));
                this.whereClause.setLength(0);
                this.whereArgs.clear();
            }
        }
    }

    public class MyMediaScannerClient implements MediaScannerClient {
        private static final int ALBUM = 1;
        private static final int ARTIST = 2;
        private static final int TITLE = 3;
        private String mAlbum;
        private String mAlbumArtist;
        private String mArtist;
        private int mCompilation;
        private String mComposer;
        private int mDuration;
        private long mFileSize;
        private int mFileType;
        private String mGenre;
        private int mHeight;
        private boolean mIsAlbumMessy;
        private boolean mIsArtistMessy;
        private boolean mIsDrm;
        private boolean mIsTitleMessy;
        private long mLastModified;
        private String mMimeType;
        private boolean mNoMedia;
        private String mPath;
        private String mTitle;
        private int mTrack;
        private int mWidth;
        private String mWriter;
        private int mYear;

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public FileEntry beginFile(String path, String mimeType, long lastModified, long fileSize, boolean isDirectory, boolean noMedia) {
            this.mMimeType = mimeType;
            this.mFileType = 0;
            this.mFileSize = fileSize;
            this.mIsDrm = false;
            if (!isDirectory) {
                if (!noMedia && MediaScanner.isNoMediaFile(path)) {
                    noMedia = true;
                }
                this.mNoMedia = noMedia;
                if (mimeType != null) {
                    this.mFileType = MediaFile.getFileTypeForMimeType(mimeType);
                }
                if (this.mFileType == 0) {
                    MediaFileType mediaFileType = MediaFile.getFileType(path);
                    if (mediaFileType != null) {
                        this.mFileType = mediaFileType.fileType;
                        if (this.mMimeType == null) {
                            this.mMimeType = mediaFileType.mimeType;
                        }
                    }
                }
                if (MediaScanner.this.isDrmEnabled() && MediaFile.isDrmFileType(this.mFileType)) {
                    this.mFileType = getFileTypeFromDrm(path);
                }
            }
            String key = path;
            FileEntry entry = (FileEntry) MediaScanner.this.mFileCache.remove(path);
            if (entry == null) {
                if (MediaScanner.this.mSkipExternelQuery) {
                }
                entry = MediaScanner.this.makeEntryFor(path);
            }
            long delta = entry != null ? lastModified - entry.mLastModified : 0;
            boolean wasModified = delta > 1 || delta < -1;
            if (entry == null || wasModified) {
                if (wasModified) {
                    entry.mLastModified = lastModified;
                } else {
                    entry = new FileEntry(0, path, lastModified, isDirectory ? 12289 : 0);
                }
                entry.mLastModifiedChanged = true;
            }
            if (MediaScanner.this.mProcessPlaylists && MediaFile.isPlayListFileType(this.mFileType)) {
                MediaScanner.this.mPlayLists.add(entry);
                return null;
            }
            this.mArtist = null;
            this.mAlbumArtist = null;
            this.mAlbum = null;
            this.mTitle = null;
            this.mComposer = null;
            this.mGenre = null;
            this.mTrack = 0;
            this.mYear = 0;
            this.mDuration = 0;
            this.mPath = path;
            this.mLastModified = lastModified;
            this.mWriter = null;
            this.mCompilation = 0;
            this.mWidth = 0;
            this.mHeight = 0;
            this.mIsAlbumMessy = false;
            this.mIsArtistMessy = false;
            this.mIsTitleMessy = false;
            return entry;
        }

        public void scanFile(String path, long lastModified, long fileSize, boolean isDirectory, boolean noMedia) {
            doScanFile(path, null, lastModified, fileSize, isDirectory, false, noMedia);
        }

        public Uri doScanFile(String path, String mimeType, long lastModified, long fileSize, boolean isDirectory, boolean scanAlways, boolean noMedia) {
            Uri result = null;
            try {
                FileEntry entry = beginFile(path, mimeType, lastModified, fileSize, isDirectory, noMedia);
                if (entry == null) {
                    return null;
                }
                if (MediaScanner.this.mMtpObjectHandle != 0) {
                    entry.mRowId = 0;
                }
                if (entry.mPath != null && ((!MediaScanner.this.mDefaultNotificationSet && doesPathHaveFilename(entry.mPath, MediaScanner.this.mDefaultNotificationFilename)) || ((!MediaScanner.this.mDefaultRingtoneSet && doesPathHaveFilename(entry.mPath, MediaScanner.this.mDefaultRingtoneFilename)) || (!MediaScanner.this.mDefaultAlarmSet && doesPathHaveFilename(entry.mPath, MediaScanner.this.mDefaultAlarmAlertFilename))))) {
                    Log.w(MediaScanner.TAG, "forcing rescan , since ringtone setting didn't finish");
                    scanAlways = true;
                }
                if (entry != null && (entry.mLastModifiedChanged || r29)) {
                    if (noMedia) {
                        result = endFile(entry, false, false, false, false, false);
                    } else {
                        String lowpath = path.toLowerCase(Locale.ROOT);
                        boolean ringtones = lowpath.indexOf(MediaScanner.RINGTONES_DIR) > 0;
                        boolean notifications = lowpath.indexOf(MediaScanner.NOTIFICATIONS_DIR) > 0;
                        boolean alarms = lowpath.indexOf(MediaScanner.ALARMS_DIR) > 0;
                        boolean podcasts = lowpath.indexOf(MediaScanner.PODCAST_DIR) > 0;
                        boolean music = lowpath.indexOf(MediaScanner.MUSIC_DIR) <= 0 ? (ringtones || notifications || alarms || podcasts) ? false : true : true;
                        ringtones |= HwThemeManager.isTRingtones(lowpath);
                        notifications |= HwThemeManager.isTNotifications(lowpath);
                        alarms |= HwThemeManager.isTAlarms(lowpath);
                        boolean isaudio = MediaFile.isAudioFileType(this.mFileType);
                        boolean isvideo = MediaFile.isVideoFileType(this.mFileType);
                        boolean isimage = MediaFile.isImageFileType(this.mFileType);
                        if (isaudio || isvideo || isimage) {
                            path = Environment.maybeTranslateEmulatedPathToInternal(new File(path)).getAbsolutePath();
                        }
                        if (isaudio || isvideo) {
                            MediaScanner.this.processFile(path, mimeType, this);
                        }
                        if (isimage) {
                            processImageFile(path);
                        }
                        if (isaudio && (this.mIsAlbumMessy || this.mIsArtistMessy || this.mIsTitleMessy)) {
                            HwFrameworkFactory.getHwMediaScannerManager().initializeSniffer(this.mPath);
                            if (this.mIsAlbumMessy) {
                                this.mAlbum = HwFrameworkFactory.getHwMediaScannerManager().postHandleStringTag(this.mAlbum, this.mPath, 1);
                            }
                            if (this.mIsArtistMessy) {
                                this.mArtist = HwFrameworkFactory.getHwMediaScannerManager().postHandleStringTag(this.mArtist, this.mPath, 2);
                            }
                            if (this.mIsTitleMessy) {
                                this.mTitle = HwFrameworkFactory.getHwMediaScannerManager().postHandleStringTag(this.mTitle, this.mPath, 3);
                            }
                            HwFrameworkFactory.getHwMediaScannerManager().resetSniffer();
                        }
                        result = endFile(entry, ringtones, notifications, alarms, music, podcasts);
                    }
                }
                return result;
            } catch (RemoteException e) {
                Log.e(MediaScanner.TAG, "RemoteException in MediaScanner.scanFile()", e);
            }
        }

        private int parseSubstring(String s, int start, int defaultValue) {
            int length = s.length();
            if (start == length) {
                return defaultValue;
            }
            int start2 = start + 1;
            char ch = s.charAt(start);
            if (ch < '0' || ch > '9') {
                return defaultValue;
            }
            int result = ch - 48;
            while (start2 < length) {
                start = start2 + 1;
                ch = s.charAt(start2);
                if (ch < '0' || ch > '9') {
                    return result;
                }
                result = (result * 10) + (ch - 48);
                start2 = start;
            }
            return result;
        }

        public void handleStringTag(String name, String value) {
            boolean z = true;
            boolean startsWith = !name.equalsIgnoreCase("album") ? name.startsWith("album;") : true;
            boolean startsWith2 = !name.equalsIgnoreCase("artist") ? name.startsWith("artist;") : true;
            boolean startsWith3 = !name.equalsIgnoreCase("title") ? name.startsWith("title;") : true;
            if (startsWith) {
                this.mIsAlbumMessy = HwFrameworkFactory.getHwMediaScannerManager().preHandleStringTag(value, this.mMimeType);
            }
            if (startsWith2) {
                this.mIsArtistMessy = HwFrameworkFactory.getHwMediaScannerManager().preHandleStringTag(value, this.mMimeType);
            }
            if (startsWith3) {
                this.mIsTitleMessy = HwFrameworkFactory.getHwMediaScannerManager().preHandleStringTag(value, this.mMimeType);
            }
            if (name.equalsIgnoreCase("title") || name.startsWith("title;")) {
                this.mTitle = value;
            } else if (name.equalsIgnoreCase("artist") || name.startsWith("artist;")) {
                this.mArtist = value.trim();
            } else if (name.equalsIgnoreCase("albumartist") || name.startsWith("albumartist;") || name.equalsIgnoreCase("band") || name.startsWith("band;")) {
                this.mAlbumArtist = value.trim();
            } else if (name.equalsIgnoreCase("album") || name.startsWith("album;")) {
                this.mAlbum = value.trim();
            } else if (name.equalsIgnoreCase(AudioColumns.COMPOSER) || name.startsWith("composer;")) {
                this.mComposer = value.trim();
            } else if (MediaScanner.this.mProcessGenres && (name.equalsIgnoreCase(AudioColumns.GENRE) || name.startsWith("genre;"))) {
                this.mGenre = getGenreName(value);
            } else if (name.equalsIgnoreCase(AudioColumns.YEAR) || name.startsWith("year;")) {
                this.mYear = parseSubstring(value, 0, 0);
            } else if (name.equalsIgnoreCase("tracknumber") || name.startsWith("tracknumber;")) {
                this.mTrack = ((this.mTrack / 1000) * 1000) + parseSubstring(value, 0, 0);
            } else if (name.equalsIgnoreCase("discnumber") || name.equals("set") || name.startsWith("set;")) {
                this.mTrack = (parseSubstring(value, 0, 0) * 1000) + (this.mTrack % 1000);
            } else if (name.equalsIgnoreCase("duration")) {
                this.mDuration = parseSubstring(value, 0, 0);
            } else if (name.equalsIgnoreCase("writer") || name.startsWith("writer;")) {
                this.mWriter = value.trim();
            } else if (name.equalsIgnoreCase(AudioColumns.COMPILATION)) {
                this.mCompilation = parseSubstring(value, 0, 0);
            } else if (name.equalsIgnoreCase("isdrm")) {
                if (parseSubstring(value, 0, 0) != 1) {
                    z = false;
                }
                this.mIsDrm = z;
            } else if (name.equalsIgnoreCase("width")) {
                this.mWidth = parseSubstring(value, 0, 0);
            } else if (name.equalsIgnoreCase("height")) {
                this.mHeight = parseSubstring(value, 0, 0);
            }
        }

        private boolean convertGenreCode(String input, String expected) {
            String output = getGenreName(input);
            if (output.equals(expected)) {
                return true;
            }
            Log.d(MediaScanner.TAG, "'" + input + "' -> '" + output + "', expected '" + expected + "'");
            return false;
        }

        private void testGenreNameConverter() {
            convertGenreCode("2", "Country");
            convertGenreCode("(2)", "Country");
            convertGenreCode("(2", "(2");
            convertGenreCode("2 Foo", "Country");
            convertGenreCode("(2) Foo", "Country");
            convertGenreCode("(2 Foo", "(2 Foo");
            convertGenreCode("2Foo", "2Foo");
            convertGenreCode("(2)Foo", "Country");
            convertGenreCode("200 Foo", "Foo");
            convertGenreCode("(200) Foo", "Foo");
            convertGenreCode("200Foo", "200Foo");
            convertGenreCode("(200)Foo", "Foo");
            convertGenreCode("200)Foo", "200)Foo");
            convertGenreCode("200) Foo", "200) Foo");
        }

        public String getGenreName(String genreTagValue) {
            if (genreTagValue == null) {
                return null;
            }
            int length = genreTagValue.length();
            if (length > 0) {
                boolean parenthesized = false;
                StringBuffer number = new StringBuffer();
                int i = 0;
                while (i < length) {
                    char c = genreTagValue.charAt(i);
                    if (i != 0 || c != '(') {
                        if (!Character.isDigit(c)) {
                            break;
                        }
                        number.append(c);
                    } else {
                        parenthesized = true;
                    }
                    i++;
                }
                char charAt = i < length ? genreTagValue.charAt(i) : ' ';
                if ((parenthesized && charAt == ')') || (!parenthesized && Character.isWhitespace(charAt))) {
                    try {
                        short genreIndex = Short.parseShort(number.toString());
                        if (genreIndex >= (short) 0) {
                            if (genreIndex < MediaScanner.ID3_GENRES.length && MediaScanner.ID3_GENRES[genreIndex] != null) {
                                return MediaScanner.ID3_GENRES[genreIndex];
                            }
                            if (genreIndex == (short) 255) {
                                return null;
                            }
                            if (genreIndex >= (short) 255 || i + 1 >= length) {
                                return number.toString();
                            }
                            if (parenthesized && charAt == ')') {
                                i++;
                            }
                            String ret = genreTagValue.substring(i).trim();
                            if (ret.length() != 0) {
                                return ret;
                            }
                        }
                    } catch (NumberFormatException e) {
                    }
                }
            }
            return genreTagValue;
        }

        private void processImageFile(String path) {
            try {
                MediaScanner.this.mBitmapOptions.outWidth = 0;
                MediaScanner.this.mBitmapOptions.outHeight = 0;
                if (HwFrameworkFactory.getHwMediaScannerManager().isBitmapSizeTooLarge(path)) {
                    this.mWidth = -1;
                    this.mHeight = -1;
                    return;
                }
                BitmapFactory.decodeFile(path, MediaScanner.this.mBitmapOptions);
                this.mWidth = MediaScanner.this.mBitmapOptions.outWidth;
                this.mHeight = MediaScanner.this.mBitmapOptions.outHeight;
            } catch (Throwable th) {
            }
        }

        public void setMimeType(String mimeType) {
            if (!"audio/mp4".equals(this.mMimeType) || !mimeType.startsWith("video")) {
                this.mMimeType = mimeType;
                this.mFileType = MediaFile.getFileTypeForMimeType(mimeType);
            }
        }

        private ContentValues toValues() {
            ContentValues map = new ContentValues();
            map.put("_data", this.mPath);
            map.put("title", this.mTitle);
            map.put("date_modified", Long.valueOf(this.mLastModified));
            map.put("_size", Long.valueOf(this.mFileSize));
            map.put("mime_type", this.mMimeType);
            map.put(MediaColumns.IS_DRM, Boolean.valueOf(this.mIsDrm));
            String resolution = null;
            if (this.mWidth > 0 && this.mHeight > 0) {
                map.put("width", Integer.valueOf(this.mWidth));
                map.put("height", Integer.valueOf(this.mHeight));
                resolution = this.mWidth + "x" + this.mHeight;
            }
            if (!this.mNoMedia) {
                String str;
                String str2;
                if (MediaFile.isVideoFileType(this.mFileType)) {
                    str = "artist";
                    str2 = (this.mArtist == null || this.mArtist.length() <= 0) ? MediaStore.UNKNOWN_STRING : this.mArtist;
                    map.put(str, str2);
                    str = "album";
                    str2 = (this.mAlbum == null || this.mAlbum.length() <= 0) ? MediaStore.UNKNOWN_STRING : this.mAlbum;
                    map.put(str, str2);
                    map.put("duration", Integer.valueOf(this.mDuration));
                    if (resolution != null) {
                        map.put(VideoColumns.RESOLUTION, resolution);
                    }
                } else if (!MediaFile.isImageFileType(this.mFileType) && MediaFile.isAudioFileType(this.mFileType)) {
                    String str3 = "artist";
                    str2 = (this.mArtist == null || this.mArtist.length() <= 0) ? MediaStore.UNKNOWN_STRING : this.mArtist;
                    map.put(str3, str2);
                    str3 = AudioColumns.ALBUM_ARTIST;
                    if (this.mAlbumArtist == null || this.mAlbumArtist.length() <= 0) {
                        str2 = null;
                    } else {
                        str2 = this.mAlbumArtist;
                    }
                    map.put(str3, str2);
                    str = "album";
                    str2 = (this.mAlbum == null || this.mAlbum.length() <= 0) ? MediaStore.UNKNOWN_STRING : this.mAlbum;
                    map.put(str, str2);
                    map.put(AudioColumns.COMPOSER, this.mComposer);
                    map.put(AudioColumns.GENRE, this.mGenre);
                    if (this.mYear != 0) {
                        map.put(AudioColumns.YEAR, Integer.valueOf(this.mYear));
                    }
                    map.put(AudioColumns.TRACK, Integer.valueOf(this.mTrack));
                    map.put("duration", Integer.valueOf(this.mDuration));
                    map.put(AudioColumns.COMPILATION, Integer.valueOf(this.mCompilation));
                }
            }
            return map;
        }

        private Uri endFile(FileEntry entry, boolean ringtones, boolean notifications, boolean alarms, boolean music, boolean podcasts) throws RemoteException {
            if (this.mArtist == null || this.mArtist.length() == 0) {
                this.mArtist = this.mAlbumArtist;
            }
            ContentValues values = toValues();
            String title = values.getAsString("title");
            if (title == null || TextUtils.isEmpty(title.trim())) {
                title = MediaFile.getFileTitle(values.getAsString("_data"));
                values.put("title", title);
            }
            if (MediaStore.UNKNOWN_STRING.equals(values.getAsString("album"))) {
                String album = values.getAsString("_data");
                int lastSlash = album.lastIndexOf(47);
                if (lastSlash >= 0) {
                    ContentValues contentValues;
                    int previousSlash = 0;
                    while (true) {
                        int idx = album.indexOf(47, previousSlash + 1);
                        if (idx >= 0 && idx < lastSlash) {
                            previousSlash = idx;
                        } else if (previousSlash != 0) {
                            contentValues = values;
                            contentValues.put("album", album.substring(previousSlash + 1, lastSlash));
                        }
                    }
                    if (previousSlash != 0) {
                        contentValues = values;
                        contentValues.put("album", album.substring(previousSlash + 1, lastSlash));
                    }
                }
            }
            long rowId = entry.mRowId;
            if (MediaFile.isAudioFileType(this.mFileType) && (rowId == 0 || MediaScanner.this.mMtpObjectHandle != 0)) {
                values.put(AudioColumns.IS_RINGTONE, Boolean.valueOf(ringtones));
                values.put(AudioColumns.IS_NOTIFICATION, Boolean.valueOf(notifications));
                values.put(AudioColumns.IS_ALARM, Boolean.valueOf(alarms));
                values.put(AudioColumns.IS_MUSIC, Boolean.valueOf(music));
                values.put(AudioColumns.IS_PODCAST, Boolean.valueOf(podcasts));
            } else if ((this.mFileType == 34 || MediaFile.isRawImageFileType(this.mFileType)) && !this.mNoMedia) {
                ExifInterface exifInterface = null;
                try {
                    exifInterface = new ExifInterface(entry.mPath);
                } catch (IOException e) {
                }
                if (exifInterface != null) {
                    float[] latlng = new float[2];
                    boolean mHasLatLong = exifInterface.getLatLong(latlng);
                    if (mHasLatLong) {
                        values.put("latitude", Float.valueOf(latlng[0]));
                        values.put("longitude", Float.valueOf(latlng[1]));
                    }
                    long time = exifInterface.getGpsDateTime();
                    if (time == -1 || !mHasLatLong) {
                        time = exifInterface.getDateTime();
                        if (time != -1 && Math.abs((this.mLastModified * 1000) - time) >= AlarmManager.INTERVAL_DAY) {
                            values.put("datetaken", Long.valueOf(time));
                        }
                    } else {
                        values.put("datetaken", Long.valueOf(time));
                    }
                    int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1);
                    if (orientation != -1) {
                        int degree;
                        switch (orientation) {
                            case 3:
                                degree = 180;
                                break;
                            case 6:
                                degree = 90;
                                break;
                            case 8:
                                degree = 270;
                                break;
                            default:
                                degree = 0;
                                break;
                        }
                        values.put(ImageColumns.ORIENTATION, Integer.valueOf(degree));
                    }
                    values.put(ImageColumns.IS_HDR, Boolean.valueOf(Parameters.SCENE_MODE_HDR.equals(exifInterface.getAttribute(ExifInterface.TAG_IMAGE_DESCRIPTION))));
                }
                HwFrameworkFactory.getHwMediaScannerManager().initializeHwVoiceAndFocus(entry.mPath, values);
            }
            MediaScanner.this.updateValues(entry.mPath, values);
            Uri tableUri = MediaScanner.this.mFilesUri;
            MediaInserter inserter = MediaScanner.this.mMediaInserter;
            if (!this.mNoMedia) {
                if (MediaFile.isVideoFileType(this.mFileType)) {
                    tableUri = MediaScanner.this.mVideoUri;
                } else if (MediaFile.isImageFileType(this.mFileType)) {
                    tableUri = MediaScanner.this.mImagesUri;
                } else if (MediaFile.isAudioFileType(this.mFileType)) {
                    tableUri = MediaScanner.this.mAudioUri;
                }
            }
            Uri result = null;
            boolean needToSetSettings = false;
            boolean needToSetSettings2 = false;
            if (!notifications || MediaScanner.this.mDefaultNotificationSet) {
                if (ringtones) {
                    if ((!MediaScanner.this.mDefaultRingtoneSet && TextUtils.isEmpty(MediaScanner.this.mDefaultRingtoneFilename)) || doesPathHaveFilename(entry.mPath, MediaScanner.this.mDefaultRingtoneFilename)) {
                        needToSetSettings = true;
                    }
                    needToSetSettings2 = HwFrameworkFactory.getHwMediaScannerManager().hwNeedSetSettings(entry.mPath);
                } else if (alarms && !MediaScanner.this.mDefaultAlarmSet && (TextUtils.isEmpty(MediaScanner.this.mDefaultAlarmAlertFilename) || doesPathHaveFilename(entry.mPath, MediaScanner.this.mDefaultAlarmAlertFilename))) {
                    needToSetSettings = true;
                }
            } else if (TextUtils.isEmpty(MediaScanner.this.mDefaultNotificationFilename) || doesPathHaveFilename(entry.mPath, MediaScanner.this.mDefaultNotificationFilename)) {
                needToSetSettings = true;
            }
            if (rowId == 0) {
                if (MediaScanner.this.mMtpObjectHandle != 0) {
                    values.put(MediaColumns.MEDIA_SCANNER_NEW_OBJECT_ID, Integer.valueOf(MediaScanner.this.mMtpObjectHandle));
                }
                if (tableUri == MediaScanner.this.mFilesUri) {
                    int format = entry.mFormat;
                    if (format == 0) {
                        format = MediaFile.getFormatCode(entry.mPath, this.mMimeType);
                    }
                    values.put(FileColumns.FORMAT, Integer.valueOf(format));
                }
                if (inserter == null || needToSetSettings || needToSetSettings2) {
                    if (inserter != null) {
                        inserter.flushAll();
                    }
                    result = MediaScanner.this.mMediaProvider.insert(tableUri, values);
                } else if (entry.mFormat == 12289) {
                    inserter.insertwithPriority(tableUri, values);
                } else {
                    inserter.insert(tableUri, values);
                }
                if (result != null) {
                    rowId = ContentUris.parseId(result);
                    entry.mRowId = rowId;
                }
            } else {
                result = ContentUris.withAppendedId(tableUri, rowId);
                values.remove("_data");
                int mediaType = 0;
                if (!MediaScanner.isNoMediaPath(entry.mPath)) {
                    int fileType = MediaFile.getFileTypeForMimeType(this.mMimeType);
                    if (MediaFile.isAudioFileType(fileType)) {
                        mediaType = 2;
                    } else if (MediaFile.isVideoFileType(fileType)) {
                        mediaType = 3;
                    } else if (MediaFile.isImageFileType(fileType)) {
                        mediaType = 1;
                    } else if (MediaFile.isPlayListFileType(fileType)) {
                        mediaType = 4;
                    }
                    values.put("media_type", Integer.valueOf(mediaType));
                }
                MediaScanner.this.mMediaProvider.update(result, values, null, null);
            }
            if (needToSetSettings) {
                if (notifications) {
                    setRingtoneIfNotSet(System.NOTIFICATION_SOUND, tableUri, rowId);
                    MediaScanner.this.mDefaultNotificationSet = true;
                } else if (ringtones) {
                    setRingtoneIfNotSet(System.RINGTONE, tableUri, rowId);
                    MediaScanner.this.mDefaultRingtoneSet = true;
                } else if (alarms) {
                    setRingtoneIfNotSet(System.ALARM_ALERT, tableUri, rowId);
                    MediaScanner.this.mDefaultAlarmSet = true;
                }
            }
            HwFrameworkFactory.getHwMediaScannerManager().hwSetRingtone2Settings(needToSetSettings2, ringtones, tableUri, rowId, MediaScanner.this.mContext);
            return result;
        }

        private boolean doesPathHaveFilename(String path, String filename) {
            int pathFilenameStart = path.lastIndexOf(File.separatorChar) + 1;
            int filenameLength = filename.length();
            if (path.regionMatches(pathFilenameStart, filename, 0, filenameLength) && pathFilenameStart + filenameLength == path.length()) {
                return true;
            }
            return false;
        }

        private void setRingtoneIfNotSet(String settingName, Uri uri, long rowId) {
            Log.v(MediaScanner.TAG, "setRingtoneIfNotSet.name:" + settingName + " value:" + uri + rowId);
            if (!MediaScanner.this.wasRingtoneAlreadySet(settingName)) {
                ContentResolver cr = MediaScanner.this.mContext.getContentResolver();
                if (TextUtils.isEmpty(System.getString(cr, settingName))) {
                    Log.v(MediaScanner.TAG, "setSetting when NotSet");
                    Uri settingUri = System.getUriFor(settingName);
                    RingtoneManager.setActualDefaultRingtoneUri(MediaScanner.this.mContext, RingtoneManager.getDefaultType(settingUri), ContentUris.withAppendedId(uri, rowId));
                }
                System.putInt(cr, MediaScanner.this.settingSetIndicatorName(settingName), 1);
            }
        }

        private int getFileTypeFromDrm(String path) {
            if (!MediaScanner.this.isDrmEnabled()) {
                return 0;
            }
            int resultFileType = 0;
            if (MediaScanner.this.mDrmManagerClient == null) {
                MediaScanner.this.mDrmManagerClient = new DrmManagerClient(MediaScanner.this.mContext);
            }
            if (MediaScanner.this.mDrmManagerClient.canHandle(path, null)) {
                this.mIsDrm = true;
                String drmMimetype = MediaScanner.this.mDrmManagerClient.getOriginalMimeType(path);
                if (drmMimetype != null) {
                    this.mMimeType = drmMimetype;
                    resultFileType = MediaFile.getFileTypeForMimeType(drmMimetype);
                }
            }
            return resultFileType;
        }
    }

    private static class PlaylistEntry {
        long bestmatchid;
        int bestmatchlevel;
        String path;

        private PlaylistEntry() {
        }
    }

    class WplHandler implements ElementListener {
        final ContentHandler handler;
        String playListDirectory;

        public WplHandler(String playListDirectory, Uri uri, Cursor fileList) {
            this.playListDirectory = playListDirectory;
            RootElement root = new RootElement("smil");
            root.getChild(TtmlUtils.TAG_BODY).getChild("seq").getChild(MediaStore.AUTHORITY).setElementListener(this);
            this.handler = root.getContentHandler();
        }

        public void start(Attributes attributes) {
            String path = attributes.getValue(ProxyInfo.LOCAL_EXCL_LIST, "src");
            if (path != null) {
                MediaScanner.this.cachePlaylistEntry(path, this.playListDirectory);
            }
        }

        public void end() {
        }

        ContentHandler getContentHandler() {
            return this.handler;
        }
    }

    private void deleteFilesIfPossible() {
        /* JADX: method processing error */
/*
Error: java.util.NoSuchElementException
	at java.util.HashMap$HashIterator.nextNode(HashMap.java:1431)
	at java.util.HashMap$KeyIterator.next(HashMap.java:1453)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.applyRemove(BlockFinallyExtract.java:535)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.extractFinally(BlockFinallyExtract.java:175)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.processExceptionHandler(BlockFinallyExtract.java:80)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.visit(BlockFinallyExtract.java:51)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
        /*
        r9 = this;
        r3 = "_data is null and media_type != 4";
        r7 = 0;
        r0 = r9.mMediaProvider;	 Catch:{ RemoteException -> 0x0027, all -> 0x0037 }
        r1 = r9.mFilesUri;	 Catch:{ RemoteException -> 0x0027, all -> 0x0037 }
        r2 = FILES_PRESCAN_PROJECTION;	 Catch:{ RemoteException -> 0x0027, all -> 0x0037 }
        r4 = 0;	 Catch:{ RemoteException -> 0x0027, all -> 0x0037 }
        r5 = 0;	 Catch:{ RemoteException -> 0x0027, all -> 0x0037 }
        r6 = 0;	 Catch:{ RemoteException -> 0x0027, all -> 0x0037 }
        r7 = r0.query(r1, r2, r3, r4, r5, r6);	 Catch:{ RemoteException -> 0x0027, all -> 0x0037 }
        if (r7 == 0) goto L_0x0021;	 Catch:{ RemoteException -> 0x0027, all -> 0x0037 }
    L_0x0013:
        r0 = r7.getCount();	 Catch:{ RemoteException -> 0x0027, all -> 0x0037 }
        if (r0 <= 0) goto L_0x0021;	 Catch:{ RemoteException -> 0x0027, all -> 0x0037 }
    L_0x0019:
        r0 = r9.mMediaProvider;	 Catch:{ RemoteException -> 0x0027, all -> 0x0037 }
        r1 = r9.mFilesUri;	 Catch:{ RemoteException -> 0x0027, all -> 0x0037 }
        r2 = 0;	 Catch:{ RemoteException -> 0x0027, all -> 0x0037 }
        r0.delete(r1, r3, r2);	 Catch:{ RemoteException -> 0x0027, all -> 0x0037 }
    L_0x0021:
        if (r7 == 0) goto L_0x0026;
    L_0x0023:
        r7.close();
    L_0x0026:
        return;
    L_0x0027:
        r8 = move-exception;
        r0 = "MediaScanner";	 Catch:{ RemoteException -> 0x0027, all -> 0x0037 }
        r1 = "deleteFilesIfPossible catch RemoteException ";	 Catch:{ RemoteException -> 0x0027, all -> 0x0037 }
        android.util.Log.d(r0, r1);	 Catch:{ RemoteException -> 0x0027, all -> 0x0037 }
        if (r7 == 0) goto L_0x0026;
    L_0x0033:
        r7.close();
        goto L_0x0026;
    L_0x0037:
        r0 = move-exception;
        if (r7 == 0) goto L_0x003d;
    L_0x003a:
        r7.close();
    L_0x003d:
        throw r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.media.MediaScanner.deleteFilesIfPossible():void");
    }

    private final native void native_finalize();

    private static final native void native_init();

    private final native void native_setup();

    private native void processDirectory(String str, MediaScannerClient mediaScannerClient);

    private native void processFile(String str, String str2, MediaScannerClient mediaScannerClient);

    private void processPlayLists() throws android.os.RemoteException {
        /* JADX: method processing error */
/*
Error: java.util.NoSuchElementException
	at java.util.HashMap$HashIterator.nextNode(HashMap.java:1431)
	at java.util.HashMap$KeyIterator.next(HashMap.java:1453)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.applyRemove(BlockFinallyExtract.java:535)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.extractFinally(BlockFinallyExtract.java:175)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.processExceptionHandler(BlockFinallyExtract.java:80)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.visit(BlockFinallyExtract.java:51)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
        /*
        r11 = this;
        r0 = r11.mPlayLists;
        r10 = r0.iterator();
        r9 = 0;
        r0 = r11.mMediaProvider;	 Catch:{ RemoteException -> 0x002b, all -> 0x0038 }
        r1 = r11.mFilesUri;	 Catch:{ RemoteException -> 0x002b, all -> 0x0038 }
        r2 = FILES_PRESCAN_PROJECTION;	 Catch:{ RemoteException -> 0x002b, all -> 0x0038 }
        r3 = "media_type=2";	 Catch:{ RemoteException -> 0x002b, all -> 0x0038 }
        r4 = 0;	 Catch:{ RemoteException -> 0x002b, all -> 0x0038 }
        r5 = 0;	 Catch:{ RemoteException -> 0x002b, all -> 0x0038 }
        r6 = 0;	 Catch:{ RemoteException -> 0x002b, all -> 0x0038 }
        r9 = r0.query(r1, r2, r3, r4, r5, r6);	 Catch:{ RemoteException -> 0x002b, all -> 0x0038 }
    L_0x0017:
        r0 = r10.hasNext();	 Catch:{ RemoteException -> 0x002b, all -> 0x0038 }
        if (r0 == 0) goto L_0x0032;	 Catch:{ RemoteException -> 0x002b, all -> 0x0038 }
    L_0x001d:
        r8 = r10.next();	 Catch:{ RemoteException -> 0x002b, all -> 0x0038 }
        r8 = (android.media.MediaScanner.FileEntry) r8;	 Catch:{ RemoteException -> 0x002b, all -> 0x0038 }
        r0 = r8.mLastModifiedChanged;	 Catch:{ RemoteException -> 0x002b, all -> 0x0038 }
        if (r0 == 0) goto L_0x0017;	 Catch:{ RemoteException -> 0x002b, all -> 0x0038 }
    L_0x0027:
        r11.processPlayList(r8, r9);	 Catch:{ RemoteException -> 0x002b, all -> 0x0038 }
        goto L_0x0017;
    L_0x002b:
        r7 = move-exception;
        if (r9 == 0) goto L_0x0031;
    L_0x002e:
        r9.close();
    L_0x0031:
        return;
    L_0x0032:
        if (r9 == 0) goto L_0x0031;
    L_0x0034:
        r9.close();
        goto L_0x0031;
    L_0x0038:
        r0 = move-exception;
        if (r9 == 0) goto L_0x003e;
    L_0x003b:
        r9.close();
    L_0x003e:
        throw r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.media.MediaScanner.processPlayLists():void");
    }

    private void pruneDeadThumbnailFiles() {
        /* JADX: method processing error */
/*
Error: java.util.NoSuchElementException
	at java.util.HashMap$HashIterator.nextNode(HashMap.java:1431)
	at java.util.HashMap$KeyIterator.next(HashMap.java:1453)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.applyRemove(BlockFinallyExtract.java:535)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.extractFinally(BlockFinallyExtract.java:175)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.processExceptionHandler(BlockFinallyExtract.java:80)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.visit(BlockFinallyExtract.java:51)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
        /*
        r18 = this;
        r12 = new java.util.HashSet;
        r12.<init>();
        r9 = "/sdcard/DCIM/.thumbnails";
        r1 = new java.io.File;
        r1.<init>(r9);
        r15 = r1.list();
        r8 = 0;
        if (r15 != 0) goto L_0x0017;
    L_0x0014:
        r1 = 0;
        r15 = new java.lang.String[r1];
    L_0x0017:
        r17 = 0;
    L_0x0019:
        r1 = r15.length;
        r0 = r17;
        if (r0 >= r1) goto L_0x0040;
    L_0x001e:
        r1 = new java.lang.StringBuilder;
        r1.<init>();
        r1 = r1.append(r9);
        r2 = "/";
        r1 = r1.append(r2);
        r2 = r15[r17];
        r1 = r1.append(r2);
        r16 = r1.toString();
        r0 = r16;
        r12.add(r0);
        r17 = r17 + 1;
        goto L_0x0019;
    L_0x0040:
        r0 = r18;	 Catch:{ RemoteException -> 0x00c6, all -> 0x00cd }
        r1 = r0.mMediaProvider;	 Catch:{ RemoteException -> 0x00c6, all -> 0x00cd }
        r0 = r18;	 Catch:{ RemoteException -> 0x00c6, all -> 0x00cd }
        r2 = r0.mThumbsUri;	 Catch:{ RemoteException -> 0x00c6, all -> 0x00cd }
        r3 = 1;	 Catch:{ RemoteException -> 0x00c6, all -> 0x00cd }
        r3 = new java.lang.String[r3];	 Catch:{ RemoteException -> 0x00c6, all -> 0x00cd }
        r4 = "_data";	 Catch:{ RemoteException -> 0x00c6, all -> 0x00cd }
        r5 = 0;	 Catch:{ RemoteException -> 0x00c6, all -> 0x00cd }
        r3[r5] = r4;	 Catch:{ RemoteException -> 0x00c6, all -> 0x00cd }
        r4 = 0;	 Catch:{ RemoteException -> 0x00c6, all -> 0x00cd }
        r5 = 0;	 Catch:{ RemoteException -> 0x00c6, all -> 0x00cd }
        r6 = 0;	 Catch:{ RemoteException -> 0x00c6, all -> 0x00cd }
        r7 = 0;	 Catch:{ RemoteException -> 0x00c6, all -> 0x00cd }
        r8 = r1.query(r2, r3, r4, r5, r6, r7);	 Catch:{ RemoteException -> 0x00c6, all -> 0x00cd }
        r1 = "MediaScanner";	 Catch:{ RemoteException -> 0x00c6, all -> 0x00cd }
        r2 = new java.lang.StringBuilder;	 Catch:{ RemoteException -> 0x00c6, all -> 0x00cd }
        r2.<init>();	 Catch:{ RemoteException -> 0x00c6, all -> 0x00cd }
        r3 = "pruneDeadThumbnailFiles... ";	 Catch:{ RemoteException -> 0x00c6, all -> 0x00cd }
        r2 = r2.append(r3);	 Catch:{ RemoteException -> 0x00c6, all -> 0x00cd }
        r2 = r2.append(r8);	 Catch:{ RemoteException -> 0x00c6, all -> 0x00cd }
        r2 = r2.toString();	 Catch:{ RemoteException -> 0x00c6, all -> 0x00cd }
        android.util.Log.v(r1, r2);	 Catch:{ RemoteException -> 0x00c6, all -> 0x00cd }
        if (r8 == 0) goto L_0x008b;	 Catch:{ RemoteException -> 0x00c6, all -> 0x00cd }
    L_0x0075:
        r1 = r8.moveToFirst();	 Catch:{ RemoteException -> 0x00c6, all -> 0x00cd }
        if (r1 == 0) goto L_0x008b;	 Catch:{ RemoteException -> 0x00c6, all -> 0x00cd }
    L_0x007b:
        r1 = 0;	 Catch:{ RemoteException -> 0x00c6, all -> 0x00cd }
        r16 = r8.getString(r1);	 Catch:{ RemoteException -> 0x00c6, all -> 0x00cd }
        r0 = r16;	 Catch:{ RemoteException -> 0x00c6, all -> 0x00cd }
        r12.remove(r0);	 Catch:{ RemoteException -> 0x00c6, all -> 0x00cd }
        r1 = r8.moveToNext();	 Catch:{ RemoteException -> 0x00c6, all -> 0x00cd }
        if (r1 != 0) goto L_0x007b;	 Catch:{ RemoteException -> 0x00c6, all -> 0x00cd }
    L_0x008b:
        r14 = r12.iterator();	 Catch:{ RemoteException -> 0x00c6, all -> 0x00cd }
    L_0x008f:
        r1 = r14.hasNext();	 Catch:{ RemoteException -> 0x00c6, all -> 0x00cd }
        if (r1 == 0) goto L_0x00a6;	 Catch:{ RemoteException -> 0x00c6, all -> 0x00cd }
    L_0x0095:
        r13 = r14.next();	 Catch:{ RemoteException -> 0x00c6, all -> 0x00cd }
        r13 = (java.lang.String) r13;	 Catch:{ RemoteException -> 0x00c6, all -> 0x00cd }
        r1 = new java.io.File;	 Catch:{ SecurityException -> 0x00a4 }
        r1.<init>(r13);	 Catch:{ SecurityException -> 0x00a4 }
        r1.delete();	 Catch:{ SecurityException -> 0x00a4 }
        goto L_0x008f;
    L_0x00a4:
        r11 = move-exception;
        goto L_0x008f;
    L_0x00a6:
        r1 = "MediaScanner";	 Catch:{ RemoteException -> 0x00c6, all -> 0x00cd }
        r2 = new java.lang.StringBuilder;	 Catch:{ RemoteException -> 0x00c6, all -> 0x00cd }
        r2.<init>();	 Catch:{ RemoteException -> 0x00c6, all -> 0x00cd }
        r3 = "/pruneDeadThumbnailFiles... ";	 Catch:{ RemoteException -> 0x00c6, all -> 0x00cd }
        r2 = r2.append(r3);	 Catch:{ RemoteException -> 0x00c6, all -> 0x00cd }
        r2 = r2.append(r8);	 Catch:{ RemoteException -> 0x00c6, all -> 0x00cd }
        r2 = r2.toString();	 Catch:{ RemoteException -> 0x00c6, all -> 0x00cd }
        android.util.Log.v(r1, r2);	 Catch:{ RemoteException -> 0x00c6, all -> 0x00cd }
        if (r8 == 0) goto L_0x00c5;
    L_0x00c2:
        r8.close();
    L_0x00c5:
        return;
    L_0x00c6:
        r10 = move-exception;
        if (r8 == 0) goto L_0x00c5;
    L_0x00c9:
        r8.close();
        goto L_0x00c5;
    L_0x00cd:
        r1 = move-exception;
        if (r8 == 0) goto L_0x00d3;
    L_0x00d0:
        r8.close();
    L_0x00d3:
        throw r1;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.media.MediaScanner.pruneDeadThumbnailFiles():void");
    }

    private native void setLocale(String str);

    public native void addSkipCustomDirectory(String str, int i);

    public native void clearSkipCustomDirectory();

    public native byte[] extractAlbumArt(FileDescriptor fileDescriptor);

    public void scanMtpFile(java.lang.String r23, int r24, int r25) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x0118 in list []
	at jadx.core.utils.BlockUtils.getBlockByOffset(BlockUtils.java:43)
	at jadx.core.dex.instructions.IfNode.initBlocks(IfNode.java:60)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.initBlocksInIfNodes(BlockFinish.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.visit(BlockFinish.java:33)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
        /*
        r22 = this;
        r19 = android.media.MediaFile.getFileType(r23);
        if (r19 != 0) goto L_0x0075;
    L_0x0006:
        r17 = 0;
    L_0x0008:
        r15 = new java.io.File;
        r0 = r23;
        r15.<init>(r0);
        r2 = r15.lastModified();
        r4 = 1000; // 0x3e8 float:1.401E-42 double:4.94E-321;
        r6 = r2 / r4;
        r2 = android.media.MediaFile.isAudioFileType(r17);
        if (r2 != 0) goto L_0x0023;
    L_0x001d:
        r2 = android.media.MediaFile.isVideoFileType(r17);
        if (r2 == 0) goto L_0x007c;
    L_0x0023:
        r0 = r24;
        r1 = r22;
        r1.mMtpObjectHandle = r0;
        r16 = 0;
        r2 = android.media.MediaFile.isPlayListFileType(r17);	 Catch:{ RemoteException -> 0x0104, all -> 0x011f }
        if (r2 == 0) goto L_0x00dd;	 Catch:{ RemoteException -> 0x0104, all -> 0x011f }
    L_0x0031:
        r2 = 0;	 Catch:{ RemoteException -> 0x0104, all -> 0x011f }
        r3 = 1;	 Catch:{ RemoteException -> 0x0104, all -> 0x011f }
        r0 = r22;	 Catch:{ RemoteException -> 0x0104, all -> 0x011f }
        r0.prescan(r2, r3);	 Catch:{ RemoteException -> 0x0104, all -> 0x011f }
        r18 = r23;	 Catch:{ RemoteException -> 0x0104, all -> 0x011f }
        r0 = r22;	 Catch:{ RemoteException -> 0x0104, all -> 0x011f }
        r2 = r0.mFileCache;	 Catch:{ RemoteException -> 0x0104, all -> 0x011f }
        r0 = r23;	 Catch:{ RemoteException -> 0x0104, all -> 0x011f }
        r14 = r2.remove(r0);	 Catch:{ RemoteException -> 0x0104, all -> 0x011f }
        r14 = (android.media.MediaScanner.FileEntry) r14;	 Catch:{ RemoteException -> 0x0104, all -> 0x011f }
        if (r14 != 0) goto L_0x004c;	 Catch:{ RemoteException -> 0x0104, all -> 0x011f }
    L_0x0048:
        r14 = r22.makeEntryFor(r23);	 Catch:{ RemoteException -> 0x0104, all -> 0x011f }
    L_0x004c:
        if (r14 == 0) goto L_0x0067;	 Catch:{ RemoteException -> 0x0104, all -> 0x011f }
    L_0x004e:
        r0 = r22;	 Catch:{ RemoteException -> 0x0104, all -> 0x011f }
        r2 = r0.mMediaProvider;	 Catch:{ RemoteException -> 0x0104, all -> 0x011f }
        r0 = r22;	 Catch:{ RemoteException -> 0x0104, all -> 0x011f }
        r3 = r0.mFilesUri;	 Catch:{ RemoteException -> 0x0104, all -> 0x011f }
        r4 = FILES_PRESCAN_PROJECTION;	 Catch:{ RemoteException -> 0x0104, all -> 0x011f }
        r5 = 0;	 Catch:{ RemoteException -> 0x0104, all -> 0x011f }
        r6 = 0;	 Catch:{ RemoteException -> 0x0104, all -> 0x011f }
        r7 = 0;	 Catch:{ RemoteException -> 0x0104, all -> 0x011f }
        r8 = 0;	 Catch:{ RemoteException -> 0x0104, all -> 0x011f }
        r16 = r2.query(r3, r4, r5, r6, r7, r8);	 Catch:{ RemoteException -> 0x0104, all -> 0x011f }
        r0 = r22;	 Catch:{ RemoteException -> 0x0104, all -> 0x011f }
        r1 = r16;	 Catch:{ RemoteException -> 0x0104, all -> 0x011f }
        r0.processPlayList(r14, r1);	 Catch:{ RemoteException -> 0x0104, all -> 0x011f }
    L_0x0067:
        r2 = 0;
        r0 = r22;
        r0.mMtpObjectHandle = r2;
        if (r16 == 0) goto L_0x0071;
    L_0x006e:
        r16.close();
    L_0x0071:
        r22.releaseResources();
    L_0x0074:
        return;
    L_0x0075:
        r0 = r19;
        r0 = r0.fileType;
        r17 = r0;
        goto L_0x0008;
    L_0x007c:
        r2 = android.media.MediaFile.isImageFileType(r17);
        if (r2 != 0) goto L_0x0023;
    L_0x0082:
        r2 = android.media.MediaFile.isPlayListFileType(r17);
        if (r2 != 0) goto L_0x0023;
    L_0x0088:
        r2 = android.media.MediaFile.isDrmFileType(r17);
        if (r2 != 0) goto L_0x0023;
    L_0x008e:
        r20 = new android.content.ContentValues;
        r20.<init>();
        r2 = "_size";
        r4 = r15.length();
        r3 = java.lang.Long.valueOf(r4);
        r0 = r20;
        r0.put(r2, r3);
        r2 = "date_modified";
        r3 = java.lang.Long.valueOf(r6);
        r0 = r20;
        r0.put(r2, r3);
        r2 = 1;
        r0 = new java.lang.String[r2];	 Catch:{ RemoteException -> 0x00d2 }
        r21 = r0;	 Catch:{ RemoteException -> 0x00d2 }
        r2 = java.lang.Integer.toString(r24);	 Catch:{ RemoteException -> 0x00d2 }
        r3 = 0;	 Catch:{ RemoteException -> 0x00d2 }
        r21[r3] = r2;	 Catch:{ RemoteException -> 0x00d2 }
        r0 = r22;	 Catch:{ RemoteException -> 0x00d2 }
        r2 = r0.mMediaProvider;	 Catch:{ RemoteException -> 0x00d2 }
        r0 = r22;	 Catch:{ RemoteException -> 0x00d2 }
        r3 = r0.mVolumeName;	 Catch:{ RemoteException -> 0x00d2 }
        r3 = android.provider.MediaStore.Files.getMtpObjectsUri(r3);	 Catch:{ RemoteException -> 0x00d2 }
        r4 = "_id=?";	 Catch:{ RemoteException -> 0x00d2 }
        r0 = r20;	 Catch:{ RemoteException -> 0x00d2 }
        r1 = r21;	 Catch:{ RemoteException -> 0x00d2 }
        r2.update(r3, r0, r4, r1);	 Catch:{ RemoteException -> 0x00d2 }
    L_0x00d1:
        return;
    L_0x00d2:
        r13 = move-exception;
        r2 = "MediaScanner";
        r3 = "RemoteException in scanMtpFile";
        android.util.Log.e(r2, r3, r13);
        goto L_0x00d1;
    L_0x00dd:
        r2 = 0;
        r0 = r22;	 Catch:{ RemoteException -> 0x0104, all -> 0x011f }
        r1 = r23;	 Catch:{ RemoteException -> 0x0104, all -> 0x011f }
        r0.prescan(r1, r2);	 Catch:{ RemoteException -> 0x0104, all -> 0x011f }
        r0 = r22;	 Catch:{ RemoteException -> 0x0104, all -> 0x011f }
        r3 = r0.mClient;	 Catch:{ RemoteException -> 0x0104, all -> 0x011f }
        r0 = r19;	 Catch:{ RemoteException -> 0x0104, all -> 0x011f }
        r5 = r0.mimeType;	 Catch:{ RemoteException -> 0x0104, all -> 0x011f }
        r8 = r15.length();	 Catch:{ RemoteException -> 0x0104, all -> 0x011f }
        r2 = 12289; // 0x3001 float:1.722E-41 double:6.0716E-320;	 Catch:{ RemoteException -> 0x0104, all -> 0x011f }
        r0 = r25;	 Catch:{ RemoteException -> 0x0104, all -> 0x011f }
        if (r0 != r2) goto L_0x011d;	 Catch:{ RemoteException -> 0x0104, all -> 0x011f }
    L_0x00f7:
        r10 = 1;	 Catch:{ RemoteException -> 0x0104, all -> 0x011f }
    L_0x00f8:
        r12 = isNoMediaPath(r23);	 Catch:{ RemoteException -> 0x0104, all -> 0x011f }
        r11 = 1;	 Catch:{ RemoteException -> 0x0104, all -> 0x011f }
        r4 = r23;	 Catch:{ RemoteException -> 0x0104, all -> 0x011f }
        r3.doScanFile(r4, r5, r6, r8, r10, r11, r12);	 Catch:{ RemoteException -> 0x0104, all -> 0x011f }
        goto L_0x0067;
    L_0x0104:
        r13 = move-exception;
        r2 = "MediaScanner";	 Catch:{ RemoteException -> 0x0104, all -> 0x011f }
        r3 = "RemoteException in MediaScanner.scanFile()";	 Catch:{ RemoteException -> 0x0104, all -> 0x011f }
        android.util.Log.e(r2, r3, r13);	 Catch:{ RemoteException -> 0x0104, all -> 0x011f }
        r2 = 0;
        r0 = r22;
        r0.mMtpObjectHandle = r2;
        if (r16 == 0) goto L_0x0118;
    L_0x0115:
        r16.close();
    L_0x0118:
        r22.releaseResources();
        goto L_0x0074;
    L_0x011d:
        r10 = 0;
        goto L_0x00f8;
    L_0x011f:
        r2 = move-exception;
        r3 = 0;
        r0 = r22;
        r0.mMtpObjectHandle = r3;
        if (r16 == 0) goto L_0x012a;
    L_0x0127:
        r16.close();
    L_0x012a:
        r22.releaseResources();
        throw r2;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.media.MediaScanner.scanMtpFile(java.lang.String, int, int):void");
    }

    static {
        System.loadLibrary("media_jni");
        native_init();
    }

    public MediaScanner(Context c, String volumeName) {
        native_setup();
        this.mContext = c;
        this.mPackageName = c.getPackageName();
        this.mVolumeName = volumeName;
        this.mBitmapOptions.inSampleSize = 1;
        this.mBitmapOptions.inJustDecodeBounds = true;
        setDefaultRingtoneFileNames();
        this.mMediaProvider = this.mContext.getContentResolver().acquireContentProviderClient(MediaStore.AUTHORITY);
        this.mAudioUri = Media.getContentUri(volumeName);
        this.mVideoUri = Video.Media.getContentUri(volumeName);
        this.mImagesUri = Images.Media.getContentUri(volumeName);
        this.mThumbsUri = Thumbnails.getContentUri(volumeName);
        this.mFilesUri = Files.getContentUri(volumeName);
        this.mFilesUriNoNotify = this.mFilesUri.buildUpon().appendQueryParameter("nonotify", WifiEnterpriseConfig.ENGINE_ENABLE).build();
        if (volumeName.equals("internal")) {
            this.mProcessPlaylists = false;
            this.mProcessGenres = false;
            this.mPlaylistsUri = null;
        } else {
            this.mProcessPlaylists = true;
            this.mProcessGenres = true;
            this.mPlaylistsUri = Playlists.getContentUri(volumeName);
            this.mExtStroagePath = HwFrameworkFactory.getHwMediaScannerManager().getExtSdcardVolumePath(this.mContext);
            this.mSkipExternelQuery = HwFrameworkFactory.getHwMediaScannerManager().isSkipExtSdcard(this.mMediaProvider, this.mExtStroagePath, this.mPackageName, this.mFilesUriNoNotify);
        }
        Locale locale = this.mContext.getResources().getConfiguration().locale;
        if (locale != null) {
            String language = locale.getLanguage();
            String country = locale.getCountry();
            if (language != null) {
                if (country != null) {
                    setLocale(language + "_" + country);
                } else {
                    setLocale(language);
                }
            }
        }
        this.mCloseGuard.open("close");
    }

    private void setDefaultRingtoneFileNames() {
        this.mDefaultRingtoneFilename = SystemProperties.get("ro.config.ringtone");
        HwFrameworkFactory.getHwMediaScannerManager().setHwDefaultRingtoneFileNames();
        this.mDefaultNotificationFilename = SystemProperties.get("ro.config.notification_sound");
        this.mDefaultAlarmAlertFilename = SystemProperties.get("ro.config.alarm_alert");
    }

    private boolean isDrmEnabled() {
        String prop = SystemProperties.get("drm.service.enabled");
        return prop != null ? prop.equals("true") : false;
    }

    private String settingSetIndicatorName(String base) {
        return base + "_set";
    }

    private boolean wasRingtoneAlreadySet(String name) {
        boolean z = false;
        try {
            if (System.getInt(this.mContext.getContentResolver(), settingSetIndicatorName(name)) != 0) {
                z = true;
            }
            return z;
        } catch (SettingNotFoundException e) {
            return false;
        }
    }

    private void prescan(String filePath, boolean prescanFiles) throws RemoteException {
        String where;
        String[] selectionArgs;
        Cursor c = null;
        this.mPlayLists.clear();
        if (this.mFileCache == null) {
            this.mFileCache = new HashMap();
        } else {
            this.mFileCache.clear();
        }
        if (filePath != null) {
            where = "_id>? AND _data=?";
            selectionArgs = new String[]{ProxyInfo.LOCAL_EXCL_LIST, filePath};
        } else {
            where = "_id>?";
            selectionArgs = new String[]{ProxyInfo.LOCAL_EXCL_LIST};
        }
        this.mDefaultRingtoneSet = wasRingtoneAlreadySet(System.RINGTONE);
        this.mDefaultNotificationSet = wasRingtoneAlreadySet(System.NOTIFICATION_SOUND);
        this.mDefaultAlarmSet = wasRingtoneAlreadySet(System.ALARM_ALERT);
        Builder builder = this.mFilesUri.buildUpon();
        builder.appendQueryParameter(MediaStore.PARAM_DELETE_DATA, "false");
        MediaBulkDeleter mediaBulkDeleter = new MediaBulkDeleter(this.mMediaProvider, builder.build());
        if (prescanFiles) {
            long lastId = Long.MIN_VALUE;
            Uri limitUri = this.mFilesUri.buildUpon().appendQueryParameter("limit", "1000").build();
            deleteFilesIfPossible();
            int count = 0;
            while (true) {
                selectionArgs[0] = ProxyInfo.LOCAL_EXCL_LIST + lastId;
                if (c != null) {
                    c.close();
                }
                c = this.mMediaProvider.query(limitUri, FILES_PRESCAN_PROJECTION, where, selectionArgs, "_id", null);
                if (c != null) {
                    if (c.getCount() == 0) {
                        break;
                    }
                    while (c.moveToNext()) {
                        long rowId = c.getLong(0);
                        String path = c.getString(1);
                        int format = c.getInt(2);
                        long lastModified = c.getLong(3);
                        lastId = rowId;
                        if (path != null && path.startsWith("/")) {
                            boolean exists = false;
                            try {
                                exists = Os.access(path, OsConstants.F_OK);
                            } catch (ErrnoException e) {
                            }
                            if (!exists) {
                                if (!MtpConstants.isAbstractObject(format)) {
                                    int fileType;
                                    MediaFileType mediaFileType = MediaFile.getFileType(path);
                                    if (mediaFileType == null) {
                                        fileType = 0;
                                    } else {
                                        try {
                                            fileType = mediaFileType.fileType;
                                        } catch (Throwable th) {
                                            if (c != null) {
                                                c.close();
                                            }
                                            mediaBulkDeleter.flush();
                                        }
                                    }
                                    if (!MediaFile.isPlayListFileType(fileType)) {
                                        mediaBulkDeleter.delete(rowId);
                                        if (path.toLowerCase(Locale.US).endsWith("/.nomedia")) {
                                            mediaBulkDeleter.flush();
                                            this.mMediaProvider.call(MediaStore.UNHIDE_CALL, new File(path).getParent(), null);
                                        }
                                    }
                                }
                            }
                            if (count < 40000) {
                                String key = path;
                                this.mFileCache.put(path, new FileEntry(rowId, path, lastModified, format));
                            }
                            count++;
                        }
                    }
                } else {
                    break;
                }
            }
        }
        if (c != null) {
            c.close();
        }
        mediaBulkDeleter.flush();
        this.mOriginalCount = 0;
        c = this.mMediaProvider.query(this.mImagesUri, new String[]{"COUNT(*)"}, null, null, null, null);
        if (c != null) {
            if (c.moveToFirst()) {
                this.mOriginalCount = c.getInt(0);
            }
            c.close();
        }
    }

    private boolean inScanDirectory(String path, String[] directories) {
        for (String directory : directories) {
            if (path.startsWith(directory)) {
                return true;
            }
        }
        return false;
    }

    public void postscan(String[] directories) throws RemoteException {
        if (this.mProcessPlaylists) {
            processPlayLists();
        }
        if (this.mOriginalCount == 0 && this.mImagesUri.equals(Images.Media.getContentUri("external"))) {
            pruneDeadThumbnailFiles();
        }
        HwFrameworkFactory.getHwMediaScannerManager().pruneDeadThumbnailsFolder();
        this.mPlayLists.clear();
    }

    private void releaseResources() {
        if (this.mDrmManagerClient != null) {
            this.mDrmManagerClient.close();
            this.mDrmManagerClient = null;
        }
    }

    private void deleteFile(File file) {
        if (file.exists()) {
            if (file.isFile()) {
                if (!file.delete()) {
                    Log.w(TAG, "delete file failed.");
                }
            } else if (file.isDirectory()) {
                File[] files = file.listFiles();
                for (File deleteFile : files) {
                    deleteFile(deleteFile);
                }
                if (!file.delete()) {
                    Log.w(TAG, "delete file failed.");
                }
            }
            Log.i(TAG, "Delete the .nomedia file in the root directory.");
        }
    }

    public void scanDirectories(String[] directories) {
        try {
            long start = System.currentTimeMillis();
            prescan(null, true);
            long prescan = System.currentTimeMillis();
            this.mMediaInserter = new MediaInserter(this.mMediaProvider, 500);
            Log.d(TAG, "delete nomedia File when scanDirectories");
            HwFrameworkFactory.getHwMediaScannerManager().deleteNomediaFile();
            for (String processDirectory : directories) {
                processDirectory(processDirectory, this.mClient);
            }
            this.mMediaInserter.flushAll();
            this.mMediaInserter = null;
            long scan = System.currentTimeMillis();
            postscan(directories);
            long end = System.currentTimeMillis();
            releaseResources();
            if (this.mFileCache != null) {
                this.mFileCache.clear();
                this.mFileCache = null;
            }
        } catch (SQLException e) {
            Log.e(TAG, "SQLException in MediaScanner.scan()", e);
            releaseResources();
            if (this.mFileCache != null) {
                this.mFileCache.clear();
                this.mFileCache = null;
            }
        } catch (UnsupportedOperationException e2) {
            Log.e(TAG, "UnsupportedOperationException in MediaScanner.scan()", e2);
            releaseResources();
            if (this.mFileCache != null) {
                this.mFileCache.clear();
                this.mFileCache = null;
            }
        } catch (RemoteException e3) {
            Log.e(TAG, "RemoteException in MediaScanner.scan()", e3);
            releaseResources();
            if (this.mFileCache != null) {
                this.mFileCache.clear();
                this.mFileCache = null;
            }
        } catch (Throwable th) {
            releaseResources();
            if (this.mFileCache != null) {
                this.mFileCache.clear();
                this.mFileCache = null;
            }
            this.mSkipExternelQuery = false;
        }
        this.mSkipExternelQuery = false;
    }

    public void scanCustomDirectories(String[] directories, String volumeName, String[] whiteList, String[] blackList) {
        this.mMediaProvider = this.mContext.getContentResolver().acquireContentProviderClient(MediaStore.AUTHORITY);
        this.mMediaInserter = new MediaInserter(this.mMediaProvider, 500);
        HwFrameworkFactory.getHwMediaScannerManager().setMediaInserter(this.mMediaInserter);
        Log.d(TAG, "delete nomedia File when scanCustomDirectories");
        HwFrameworkFactory.getHwMediaScannerManager().deleteNomediaFile();
        HwFrameworkFactory.getHwMediaScannerManager().scanCustomDirectories(this, this.mClient, directories, volumeName, whiteList, blackList);
        clearSkipCustomDirectory();
        if (this.mFileCache != null) {
            this.mFileCache.clear();
            this.mFileCache = null;
        }
        HwFrameworkFactory.getHwMediaScannerManager().setMediaInserter(null);
        this.mMediaInserter = null;
        this.mSkipExternelQuery = false;
    }

    public Uri scanSingleFile(String path, String mimeType) {
        try {
            prescan(path, true);
            File file = new File(path);
            if (!file.exists()) {
                return null;
            }
            Log.d(TAG, "delete nomedia File when scanSingleFile");
            HwFrameworkFactory.getHwMediaScannerManager().deleteNomediaFile();
            String str = path;
            String str2 = mimeType;
            Uri doScanFile = this.mClient.doScanFile(str, str2, file.lastModified() / 1000, file.length(), false, true, isNoMediaPath(path));
            releaseResources();
            return doScanFile;
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException in MediaScanner.scanFile()", e);
            return null;
        } finally {
            releaseResources();
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static boolean isNoMediaFile(String path) {
        if (new File(path).isDirectory()) {
            return false;
        }
        int lastSlash = path.lastIndexOf(47);
        if (lastSlash >= 0 && lastSlash + 2 < path.length()) {
            if (path.regionMatches(lastSlash + 1, "._", 0, 2)) {
                return true;
            }
            if (path.regionMatches(true, path.length() - 4, ".jpg", 0, 4)) {
                if (!path.regionMatches(true, lastSlash + 1, "AlbumArt_{", 0, 10)) {
                    if (!path.regionMatches(true, lastSlash + 1, "AlbumArt.", 0, 9)) {
                        int length = (path.length() - lastSlash) - 1;
                        if (length == 17) {
                        }
                        if (length == 10) {
                            if (path.regionMatches(true, lastSlash + 1, "Folder", 0, 6)) {
                                return true;
                            }
                        }
                    }
                }
                return true;
            }
        }
        return false;
    }

    public static void clearMediaPathCache(boolean clearMediaPaths, boolean clearNoMediaPaths) {
        synchronized (MediaScanner.class) {
            if (clearMediaPaths) {
                mMediaPaths.clear();
            }
            if (clearNoMediaPaths) {
                mNoMediaPaths.clear();
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean isNoMediaPath(String path) {
        if (path == null) {
            return false;
        }
        if (path.indexOf("/.") >= 0) {
            return true;
        }
        int firstSlash = path.lastIndexOf(47);
        if (firstSlash <= 0) {
            return false;
        }
        String parent = path.substring(0, firstSlash);
        synchronized (MediaScanner.class) {
            if (mNoMediaPaths.containsKey(parent)) {
                return true;
            } else if (!mMediaPaths.containsKey(parent)) {
                int offset = 1;
                while (offset >= 0) {
                    int slashIndex = path.indexOf(47, offset);
                    if (slashIndex > offset) {
                        slashIndex++;
                        if (new File(path.substring(0, slashIndex) + MediaStore.MEDIA_IGNORE_FILENAME).exists()) {
                            mNoMediaPaths.put(parent, ProxyInfo.LOCAL_EXCL_LIST);
                            return true;
                        }
                    }
                    offset = slashIndex;
                }
                mMediaPaths.put(parent, ProxyInfo.LOCAL_EXCL_LIST);
            }
        }
    }

    FileEntry makeEntryFor(String path) {
        Cursor cursor = null;
        try {
            String[] selectionArgs = new String[]{path};
            cursor = this.mMediaProvider.query(this.mFilesUriNoNotify, FILES_PRESCAN_PROJECTION, "_data=?", selectionArgs, null, null);
            if (cursor.moveToFirst()) {
                String str = path;
                FileEntry fileEntry = new FileEntry(cursor.getLong(0), str, cursor.getLong(3), cursor.getInt(2));
                if (cursor != null) {
                    cursor.close();
                }
                return fileEntry;
            }
            if (cursor != null) {
                cursor.close();
            }
            return null;
        } catch (RemoteException e) {
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private int matchPaths(String path1, String path2) {
        int result = 0;
        int end1 = path1.length();
        int end2 = path2.length();
        while (end1 > 0 && end2 > 0) {
            int slash1 = path1.lastIndexOf(47, end1 - 1);
            int slash2 = path2.lastIndexOf(47, end2 - 1);
            int backSlash1 = path1.lastIndexOf(92, end1 - 1);
            int backSlash2 = path2.lastIndexOf(92, end2 - 1);
            int start1 = slash1 > backSlash1 ? slash1 : backSlash1;
            int start2 = slash2 > backSlash2 ? slash2 : backSlash2;
            start1 = start1 < 0 ? 0 : start1 + 1;
            start2 = start2 < 0 ? 0 : start2 + 1;
            int length = end1 - start1;
            if (end2 - start2 != length || !path1.regionMatches(true, start1, path2, start2, length)) {
                break;
            }
            result++;
            end1 = start1 - 1;
            end2 = start2 - 1;
        }
        return result;
    }

    private boolean matchEntries(long rowId, String data) {
        int len = this.mPlaylistEntries.size();
        boolean done = true;
        for (int i = 0; i < len; i++) {
            PlaylistEntry entry = (PlaylistEntry) this.mPlaylistEntries.get(i);
            if (entry.bestmatchlevel != Integer.MAX_VALUE) {
                done = false;
                if (data.equalsIgnoreCase(entry.path)) {
                    entry.bestmatchid = rowId;
                    entry.bestmatchlevel = Integer.MAX_VALUE;
                } else {
                    int matchLength = matchPaths(data, entry.path);
                    if (matchLength > entry.bestmatchlevel) {
                        entry.bestmatchid = rowId;
                        entry.bestmatchlevel = matchLength;
                    }
                }
            }
        }
        return done;
    }

    private void cachePlaylistEntry(String line, String playListDirectory) {
        boolean z = true;
        PlaylistEntry entry = new PlaylistEntry();
        int entryLength = line.length();
        while (entryLength > 0 && Character.isWhitespace(line.charAt(entryLength - 1))) {
            entryLength--;
        }
        if (entryLength >= 3) {
            boolean fullPath;
            if (entryLength < line.length()) {
                line = line.substring(0, entryLength);
            }
            char ch1 = line.charAt(0);
            if (ch1 == '/') {
                fullPath = true;
            } else if (Character.isLetter(ch1) && line.charAt(1) == ':') {
                if (line.charAt(2) != '\\') {
                    z = false;
                }
                fullPath = z;
            } else {
                fullPath = false;
            }
            if (!fullPath) {
                line = playListDirectory + line;
            }
            entry.path = line;
            this.mPlaylistEntries.add(entry);
        }
    }

    private void processCachedPlaylist(Cursor fileList, ContentValues values, Uri playlistUri) {
        fileList.moveToPosition(-1);
        while (fileList.moveToNext()) {
            if (matchEntries(fileList.getLong(0), fileList.getString(1))) {
                break;
            }
        }
        int len = this.mPlaylistEntries.size();
        int index = 0;
        for (int i = 0; i < len; i++) {
            PlaylistEntry entry = (PlaylistEntry) this.mPlaylistEntries.get(i);
            if (entry.bestmatchlevel > 0) {
                try {
                    values.clear();
                    values.put("play_order", Integer.valueOf(index));
                    values.put("audio_id", Long.valueOf(entry.bestmatchid));
                    this.mMediaProvider.insert(playlistUri, values);
                    index++;
                } catch (RemoteException e) {
                    Log.e(TAG, "RemoteException in MediaScanner.processCachedPlaylist()", e);
                    return;
                }
            }
        }
        this.mPlaylistEntries.clear();
    }

    private void processM3uPlayList(String path, String playListDirectory, Uri uri, ContentValues values, Cursor fileList) {
        IOException e;
        Throwable th;
        BufferedReader bufferedReader = null;
        try {
            File f = new File(path);
            if (f.exists()) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f)), 8192);
                try {
                    String line = reader.readLine();
                    this.mPlaylistEntries.clear();
                    while (line != null) {
                        if (line.length() > 0 && line.charAt(0) != '#') {
                            cachePlaylistEntry(line, playListDirectory);
                        }
                        line = reader.readLine();
                    }
                    processCachedPlaylist(fileList, values, uri);
                    bufferedReader = reader;
                } catch (IOException e2) {
                    e = e2;
                    bufferedReader = reader;
                    try {
                        Log.e(TAG, "IOException in MediaScanner.processM3uPlayList()", e);
                        if (bufferedReader != null) {
                            try {
                                bufferedReader.close();
                            } catch (IOException e3) {
                                Log.e(TAG, "IOException in MediaScanner.processM3uPlayList()", e3);
                                return;
                            }
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        if (bufferedReader != null) {
                            try {
                                bufferedReader.close();
                            } catch (IOException e32) {
                                Log.e(TAG, "IOException in MediaScanner.processM3uPlayList()", e32);
                            }
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    bufferedReader = reader;
                    if (bufferedReader != null) {
                        bufferedReader.close();
                    }
                    throw th;
                }
            }
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e322) {
                    Log.e(TAG, "IOException in MediaScanner.processM3uPlayList()", e322);
                }
            }
        } catch (IOException e4) {
            e322 = e4;
            Log.e(TAG, "IOException in MediaScanner.processM3uPlayList()", e322);
            if (bufferedReader != null) {
                bufferedReader.close();
            }
        }
    }

    private void processPlsPlayList(String path, String playListDirectory, Uri uri, ContentValues values, Cursor fileList) {
        IOException e;
        Throwable th;
        BufferedReader bufferedReader = null;
        try {
            File f = new File(path);
            if (f.exists()) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f)), 8192);
                try {
                    this.mPlaylistEntries.clear();
                    for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                        if (line.startsWith("File")) {
                            int equals = line.indexOf(61);
                            if (equals > 0) {
                                cachePlaylistEntry(line.substring(equals + 1), playListDirectory);
                            }
                        }
                    }
                    processCachedPlaylist(fileList, values, uri);
                    bufferedReader = reader;
                } catch (IOException e2) {
                    e = e2;
                    bufferedReader = reader;
                    try {
                        Log.e(TAG, "IOException in MediaScanner.processPlsPlayList()", e);
                        if (bufferedReader != null) {
                            try {
                                bufferedReader.close();
                            } catch (IOException e3) {
                                Log.e(TAG, "IOException in MediaScanner.processPlsPlayList()", e3);
                                return;
                            }
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        if (bufferedReader != null) {
                            try {
                                bufferedReader.close();
                            } catch (IOException e32) {
                                Log.e(TAG, "IOException in MediaScanner.processPlsPlayList()", e32);
                            }
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    bufferedReader = reader;
                    if (bufferedReader != null) {
                        bufferedReader.close();
                    }
                    throw th;
                }
            }
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e322) {
                    Log.e(TAG, "IOException in MediaScanner.processPlsPlayList()", e322);
                }
            }
        } catch (IOException e4) {
            e322 = e4;
            Log.e(TAG, "IOException in MediaScanner.processPlsPlayList()", e322);
            if (bufferedReader != null) {
                bufferedReader.close();
            }
        }
    }

    private void processWplPlayList(String path, String playListDirectory, Uri uri, ContentValues values, Cursor fileList) {
        SAXException e;
        IOException e2;
        Throwable th;
        FileInputStream fileInputStream = null;
        try {
            File f = new File(path);
            if (f.exists()) {
                FileInputStream fis = new FileInputStream(f);
                try {
                    this.mPlaylistEntries.clear();
                    Xml.parse(fis, Xml.findEncodingByName("UTF-8"), new WplHandler(playListDirectory, uri, fileList).getContentHandler());
                    processCachedPlaylist(fileList, values, uri);
                    fileInputStream = fis;
                } catch (SAXException e3) {
                    e = e3;
                    fileInputStream = fis;
                    e.printStackTrace();
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e22) {
                            Log.e(TAG, "IOException in MediaScanner.processWplPlayList()", e22);
                            return;
                        }
                    }
                } catch (IOException e4) {
                    e22 = e4;
                    fileInputStream = fis;
                    try {
                        e22.printStackTrace();
                        if (fileInputStream != null) {
                            try {
                                fileInputStream.close();
                            } catch (IOException e222) {
                                Log.e(TAG, "IOException in MediaScanner.processWplPlayList()", e222);
                                return;
                            }
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        if (fileInputStream != null) {
                            try {
                                fileInputStream.close();
                            } catch (IOException e2222) {
                                Log.e(TAG, "IOException in MediaScanner.processWplPlayList()", e2222);
                            }
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    fileInputStream = fis;
                    if (fileInputStream != null) {
                        fileInputStream.close();
                    }
                    throw th;
                }
            }
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e22222) {
                    Log.e(TAG, "IOException in MediaScanner.processWplPlayList()", e22222);
                }
            }
        } catch (SAXException e5) {
            e = e5;
            e.printStackTrace();
            if (fileInputStream != null) {
                fileInputStream.close();
            }
        } catch (IOException e6) {
            e22222 = e6;
            e22222.printStackTrace();
            if (fileInputStream != null) {
                fileInputStream.close();
            }
        }
    }

    private void processPlayList(FileEntry entry, Cursor fileList) throws RemoteException {
        String path = entry.mPath;
        ContentValues values = new ContentValues();
        int lastSlash = path.lastIndexOf(47);
        if (lastSlash < 0) {
            throw new IllegalArgumentException("bad path ");
        }
        Uri membersUri;
        long rowId = entry.mRowId;
        String name = values.getAsString("name");
        if (name == null) {
            name = values.getAsString("title");
            if (name == null) {
                int lastDot = path.lastIndexOf(46);
                if (lastDot < 0) {
                    name = path.substring(lastSlash + 1);
                } else {
                    name = path.substring(lastSlash + 1, lastDot);
                }
            }
        }
        values.put("name", name);
        values.put("date_modified", Long.valueOf(entry.mLastModified));
        Uri uri;
        if (rowId == 0) {
            values.put("_data", path);
            uri = this.mMediaProvider.insert(this.mPlaylistsUri, values);
            rowId = ContentUris.parseId(uri);
            membersUri = Uri.withAppendedPath(uri, "members");
        } else {
            uri = ContentUris.withAppendedId(this.mPlaylistsUri, rowId);
            this.mMediaProvider.update(uri, values, null, null);
            membersUri = Uri.withAppendedPath(uri, "members");
            this.mMediaProvider.delete(membersUri, null, null);
        }
        String playListDirectory = path.substring(0, lastSlash + 1);
        MediaFileType mediaFileType = MediaFile.getFileType(path);
        int fileType = mediaFileType == null ? 0 : mediaFileType.fileType;
        if (fileType == 44) {
            processM3uPlayList(path, playListDirectory, membersUri, values, fileList);
        } else if (fileType == 45) {
            processPlsPlayList(path, playListDirectory, membersUri, values, fileList);
        } else if (fileType == 46) {
            processWplPlayList(path, playListDirectory, membersUri, values, fileList);
        }
    }

    public void close() {
        this.mCloseGuard.close();
        if (this.mClosed.compareAndSet(false, true)) {
            this.mMediaProvider.close();
            native_finalize();
        }
    }

    protected void finalize() throws Throwable {
        try {
            this.mCloseGuard.warnIfOpen();
            close();
        } finally {
            super.finalize();
        }
    }

    protected void updateValues(String path, ContentValues contentValues) {
    }
}
