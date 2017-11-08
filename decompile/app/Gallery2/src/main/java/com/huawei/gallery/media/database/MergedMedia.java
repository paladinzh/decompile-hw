package com.huawei.gallery.media.database;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.MediaStore.Files;
import android.provider.MediaStore.Images.Media;
import android.provider.MediaStore.Video;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.GalleryUtils;
import com.huawei.gallery.provider.GalleryProvider;
import com.huawei.gallery.util.MyPrinter;

public class MergedMedia {
    public static final Uri FILES_URI = Files.getContentUri("external");
    public static final Uri IMAGE_URI = Media.EXTERNAL_CONTENT_URI;
    private static final MyPrinter LOG = new MyPrinter("MergedMedia");
    public static final Uri OPERATION_URI = Uri.parse("content://media/external/media/operation");
    public static final Uri SYNC_URI = Uri.parse("content://com.huawei.gallery.provider/none/sycnner");
    public static final Uri URI = GalleryProvider.BASE_URI.buildUpon().appendPath("merge").build();
    public static final Uri VIDEO_URI = Video.Media.EXTERNAL_CONTENT_URI;

    public static void createOrUpdateTable(SQLiteDatabase db) {
        GalleryLog.d("MergedMedia", "upgradeDatabase is called.");
        db.execSQL("CREATE TABLE t_local_sync_token (_id  INTEGER NOT NULL,content_uri  TEXT,bucket_id  TEXT,id_max  INTEGER,id_min  INTEGER,item_count  INTEGER,bucket_display_name TEXT,PRIMARY KEY (_id));");
        db.execSQL("CREATE INDEX idx_content_uri  ON t_local_sync_token (content_uri ASC);");
        db.execSQL("CREATE TABLE gallery_media (_id  INTEGER PRIMARY KEY AUTOINCREMENT,local_media_id INTEGER,cloud_media_id INTEGER, _data  TEXT COLLATE NOCASE ,_size  INTEGER,date_added  INTEGER,date_modified  INTEGER,mime_type  TEXT,title  TEXT,description  TEXT,_display_name  TEXT,orientation  INTEGER,latitude  DOUBLE,longitude  DOUBLE,datetaken  INTEGER,bucket_id  TEXT,bucket_display_name  TEXT,duration  INTEGER,resolution  TEXT,media_type  INTEGER,storage_id  INTEGER,width  INTEGER,height  INTEGER,is_hdr  INTEGER,is_hw_privacy  INTEGER,hw_voice_offset  INTEGER,is_hw_favorite  INTEGER,hw_image_refocus  INTEGER,is_hw_burst  INTEGER DEFAULT 0,hw_rectify_offset  INTEGER,contenturi  TEXT,hash  TEXT,special_file_list INTEGER,special_file_type INTEGER,dirty INTEGER DEFAULT 1,cloud_bucket_id TEXT );");
        db.execSQL("CREATE INDEX bucket_index ON gallery_media (bucket_id ASC, media_type ASC, datetaken ASC, _id ASC);");
        db.execSQL("CREATE INDEX bucket_name ON gallery_media (bucket_id ASC, media_type ASC, bucket_display_name ASC);");
        db.execSQL("CREATE INDEX is_hw_burst_index ON gallery_media (is_hw_burst ASC);");
        db.execSQL("CREATE INDEX media_type_index ON gallery_media (media_type ASC);");
        db.execSQL("CREATE INDEX path_index ON gallery_media (_data ASC);");
        db.execSQL("CREATE INDEX sort_index ON gallery_media (datetaken ASC, _id ASC);");
        db.execSQL("CREATE INDEX title_idx ON gallery_media (title ASC);");
        db.execSQL("CREATE TABLE IF NOT EXISTS search ( _id  INTEGER NOT NULL, bucket_id  INTEGER, title  TEXT, datetaken  TEXT, location_country_name  TEXT, location_country_code  TEXT, location_postal_code  TEXT, location_premises  TEXT, location_thoroughfare  TEXT, location_subthoroughfare  TEXT, location_locality  TEXT, location_sublocality  TEXT, location_admin_area  TEXT, location_subadmin_area  TEXT, tagId  TEXT, tagName  TEXT, info  TEXT,deleted INTEGER, dirty  INTEGER, PRIMARY KEY (_id ASC) ); ");
        db.execSQL("CREATE TRIGGER tr_insert_after AFTER INSERT ON gallery_media  BEGIN    insert into search(_id, dirty) values(new._id, 1) ;  END;");
        db.execSQL("CREATE TRIGGER tr_update_after AFTER UPDATE ON gallery_media  FOR EACH ROW  BEGIN  update search set dirty = 1 where _id=new._id;  END; ");
        db.execSQL("CREATE TRIGGER tr_delete_after AFTER DELETE ON gallery_media  FOR EACH ROW  BEGIN  update search set deleted = 1 where _id=old._id;  END;");
        db.execSQL("CREATE TABLE  classify_input  (  id   INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,  userId   TEXT,  hash   TEXT,  jobStatus   INTEGER );  ");
        db.execSQL("CREATE UNIQUE INDEX index_hash ON  classify_input  ( hash  ASC);");
        db.execSQL("CREATE TABLE cloud_album (albumId TEXT PRIMARY KEY NOT NULL,albumName TEXT,createTime INTEGER,lpath TEXT,photoNum INTEGER,source TEXT,flversion INTEGER,iversion INTEGER,totalSize INTEGER);");
        db.execSQL("CREATE TABLE cloud_file (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,size INTEGER,hash TEXT,localThumbPath TEXT,localBigThumbPath TEXT,localRealPath TEXT,fileName Text,orientation INTEGER,albumId TEXT NOT NULL,fyuseAttach TEXT,duration INTEGER,latitude DOUBLE,longitude DOUBLE,fileType INTEGER,fileId TEXT,source TEXT,videoThumbId TEXT,createTime INTEGER,expand TEXT,thumbType INTEGER default 0,deleteFlag INTEGER default 0,unique(hash, albumId));");
        db.execSQL("CREATE TABLE auto_upload_album (relativePath TEXT NOT NULL,albumName TEXT NOT NULL,albumID TEXT,PRIMARY KEY (relativePath, albumName));");
        db.execSQL("insert into auto_upload_album values(\"/DCIM/Camera\",\"Camera\",\"default-album-1\");");
        db.execSQL("insert into auto_upload_album values(\"/Pictures/Screenshots\",\"Screenshots\",\"default-album-2\");");
        db.execSQL("CREATE TABLE cloud_tag (userId  TEXT NOT NULL,tagId  TEXT NOT NULL,tagName  TEXT,createTime INTEGER,categoryId  TEXT,version  INTEGER,PRIMARY KEY (userId, tagId));");
        db.execSQL("CREATE TABLE cloud_tag_file(hash  TEXT NOT NULL,fileId  TEXT NOT NULL,createTime INTEGER,albumList  TEXT,tagId TEXT NOT NULL,categoryId  INTEGER NOT NULL,faceId TEXT,x INTEGER,y INTEGER,width INTEGER,height INTEGER,features BLOB,localThumbPath TEXT,localLcdPath TEXT,localRealPath TEXT,faceThumbPath TEXT,PRIMARY KEY (hash ASC));");
        db.execSQL("create trigger tr_cloudFileDelete_after after delete on cloud_file when 1 = (select count(*) from gallery_media where cloud_media_id = old.id) BEGIN  delete from gallery_media where cloud_media_id = old.id;END;");
        db.execSQL("create trigger tr_cloudAlbumDelete_after after delete on cloud_album BEGIN  delete from cloud_file where  albumId = old.albumId; END;");
    }

    public static void createOrUpdateTable(SQLiteDatabase db, int fromVersion) {
        if (fromVersion < 50001001) {
            db.execSQL("DROP TABLE IF EXISTS gallery_media");
            db.execSQL("CREATE TABLE gallery_media (_id  INTEGER PRIMARY KEY AUTOINCREMENT,local_media_id INTEGER DEFAULT -1,cloud_media_id INTEGER DEFAULT -1, _data  TEXT UNIQUE COLLATE NOCASE ,_size  INTEGER,date_added  INTEGER,date_modified  INTEGER,mime_type  TEXT,title  TEXT,description  TEXT,_display_name  TEXT,orientation  INTEGER,latitude  DOUBLE,longitude  DOUBLE,datetaken  INTEGER,bucket_id  TEXT,bucket_display_name  TEXT,duration  INTEGER,resolution  TEXT,media_type  INTEGER,storage_id  INTEGER,width  INTEGER,height  INTEGER,is_hdr  INTEGER,is_hw_privacy  INTEGER,hw_voice_offset  INTEGER,is_hw_favorite  INTEGER,hw_image_refocus  INTEGER,is_hw_burst  INTEGER DEFAULT 0,hw_rectify_offset  INTEGER,contenturi  TEXT,hash  TEXT,special_file_list INTEGER,special_file_type INTEGER,dirty INTEGER DEFAULT 1,cloud_bucket_id TEXT, bucket_relative_path TEXT );");
            db.execSQL("CREATE INDEX local_media_index ON gallery_media (local_media_id ASC);");
            db.execSQL("CREATE INDEX cloud_media_index ON gallery_media (cloud_media_id ASC);");
            db.execSQL("CREATE INDEX bucket_index ON gallery_media (bucket_id ASC, media_type ASC, datetaken ASC, _id ASC);");
            db.execSQL("CREATE INDEX bucket_name ON gallery_media (bucket_id ASC, media_type ASC, bucket_display_name ASC);");
            db.execSQL("CREATE INDEX is_hw_burst_index ON gallery_media (is_hw_burst ASC);");
            db.execSQL("CREATE INDEX media_type_index ON gallery_media (media_type ASC);");
            db.execSQL("CREATE INDEX path_index ON gallery_media (_data ASC);");
            db.execSQL("CREATE INDEX sort_index ON gallery_media (datetaken ASC, _id ASC);");
            db.execSQL("CREATE INDEX title_idx ON gallery_media (title ASC);");
            db.execSQL("DELETE FROM search");
            db.execSQL("CREATE TRIGGER tr_insert_after AFTER INSERT ON gallery_media  BEGIN    insert into search(_id, dirty) values(new._id, 1) ;  END;");
            db.execSQL("CREATE TRIGGER tr_update_after AFTER UPDATE ON gallery_media  FOR EACH ROW  BEGIN  update search set dirty = 1 where _id=new._id;  END; ");
            db.execSQL("CREATE TRIGGER tr_delete_after AFTER DELETE ON gallery_media  FOR EACH ROW  BEGIN  update search set deleted = 1 where _id=old._id;  END;");
            db.execSQL("DROP TABLE IF EXISTS auto_upload_album");
            db.execSQL("CREATE TABLE auto_upload_album (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,relativePath TEXT NOT NULL,albumName TEXT NOT NULL,albumId TEXT,tempId TEXT,albumType INTEGER,unique(relativePath));");
            db.execSQL("DROP TRIGGER IF EXISTS tr_cloudFileDelete_after");
            db.execSQL("DROP TRIGGER IF EXISTS tr_cloudAlbumDelete_after");
            db.execSQL("CREATE TABLE history_album_id (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,relativePath TEXT NOT NULL,albumId TEXT,unique(relativePath));");
            db.execSQL("create trigger tr_cloud_album_delete_after after delete on cloud_album when 1 = (select count(*) from history_album_id where albumId = old.albumId) BEGIN  delete from history_album_id where albumId = old.albumId;END;");
            db.execSQL("create trigger tr_auto_upload_album_insert_after after insert on auto_upload_album when 0 = (select count(*) from history_album_id where albumId = new.albumId) and new.albumType!=1 and new.albumType!=2 BEGIN   insert into history_album_id(relativePath, albumId) values(new.relativePath, new.albumId); END;");
        }
        if (fromVersion < 50002001) {
            db.execSQL("delete from gallery_media;");
            db.execSQL("ALTER TABLE gallery_media ADD COLUMN fileType INTEGER;");
            db.execSQL("ALTER TABLE gallery_media ADD COLUMN fileId TEXT;");
            db.execSQL("ALTER TABLE gallery_media ADD COLUMN videoThumbId TEXT;");
            db.execSQL("ALTER TABLE gallery_media ADD COLUMN thumbType INTEGER DEFAULT 0;");
            db.execSQL("ALTER TABLE gallery_media ADD COLUMN localThumbPath TEXT;");
            db.execSQL("ALTER TABLE gallery_media ADD COLUMN localBigThumbPath TEXT;");
            db.execSQL("ALTER TABLE gallery_media ADD COLUMN expand TEXT;");
            db.execSQL("ALTER TABLE gallery_media ADD COLUMN showDateToken INTEGER;");
            db.execSQL("CREATE TRIGGER tr_insert_after_gallery_media AFTER INSERT ON gallery_media  BEGIN  update gallery_media set showDateToken = new.datetaken where _id = new._id ;  END;");
        }
        if (fromVersion < 50003001) {
            db.execSQL("DROP TRIGGER IF EXISTS tr_insert_after");
            db.execSQL("DROP TRIGGER IF EXISTS tr_update_after");
            db.execSQL("DROP TRIGGER IF EXISTS tr_delete_after");
            db.execSQL("DROP TABLE IF EXISTS search");
        }
        if (fromVersion < 50004001) {
            db.execSQL("ALTER TABLE cloud_album ADD COLUMN deleteFlag INTEGER default 0;");
            db.execSQL("ALTER TABLE cloud_album ADD COLUMN renameFlag INTEGER default 0;");
            db.execSQL("ALTER TABLE cloud_album ADD COLUMN newName TEXT;");
            db.execSQL("ALTER TABLE cloud_album ADD COLUMN newPath TEXT;");
            db.execSQL("ALTER TABLE gallery_media ADD COLUMN source TEXT;");
        }
        if (fromVersion < 50005001) {
            db.execSQL("CREATE INDEX gallery_sort_index ON gallery_media (showDateToken ASC, _id ASC);");
        }
        if (fromVersion < 50006001) {
            db.execSQL("CREATE TABLE fversioninfo (fversion TEXT);");
        }
        if (fromVersion < 50007001) {
            db.execSQL("CREATE TABLE t_geo_dictionary (geo_code TEXT NOT NULL, language TEXT, geo_name TEXT);");
            db.execSQL("CREATE INDEX idx_gen_geo_code ON t_geo_dictionary (geo_code ASC);");
            db.execSQL("CREATE INDEX idx_search_geo_name ON t_geo_dictionary (language DESC, geo_name ASC );");
            db.execSQL("insert into t_geo_dictionary (geo_code, language, geo_name) values (0,'local','stub')");
            db.execSQL("CREATE TABLE t_geo_file (_id  INTEGER PRIMARY KEY, _data TEXT UNIQUE, bucket_relative_path TEXT, _display_name TEXT, latitude  DOUBLE,longitude  DOUBLE, geo_code TEXT, geo_name TEXT);");
            db.execSQL("CREATE INDEX idx_geo_file_geo_code ON t_geo_file (geo_code ASC);");
            db.execSQL("CREATE TABLE t_geo_knowledge(latitude  DOUBLE,longitude  DOUBLE, geo_code TEXT, geo_name_en TEXT);");
            db.execSQL("CREATE INDEX idx_geo_knowledge_key ON t_geo_knowledge (latitude ASC, longitude DESC);");
            db.execSQL("insert into t_geo_file (_id,_data,bucket_relative_path, _display_name, latitude, longitude) select _id, _data, bucket_relative_path, _display_name, latitude, longitude from gallery_media WHERE latitude !=0.0 AND longitude !=0.0  ;");
            db.execSQL("CREATE TRIGGER tr_insert_geo_after_insert AFTER INSERT ON gallery_media  BEGIN insert into t_geo_file(_id, _data, bucket_relative_path, _display_name, latitude, longitude) values(new._id, new._data, new.bucket_relative_path, new._display_name, new.latitude, new.longitude) ;  END;");
            db.execSQL("CREATE TRIGGER tr_update_geo_after_update AFTER UPDATE ON gallery_media  FOR EACH ROW  BEGIN  update t_geo_file set _data=new._data, bucket_relative_path=new.bucket_relative_path, latitude=new.latitude, longitude=new.longitude WHERE _id=new._id;  END;");
            db.execSQL("CREATE TRIGGER tr_delete_geo_after_delete AFTER DELETE ON gallery_media  FOR EACH ROW  BEGIN  delete from t_geo_file WHERE _id=old._id;  END;");
        }
        if (fromVersion < 50008001) {
            db.execSQL("ALTER TABLE t_geo_file ADD COLUMN bucket_id TEXT;");
            db.execSQL("DROP TRIGGER IF EXISTS tr_insert_geo_after_insert");
            db.execSQL("UPDATE t_geo_file SET bucket_id= (SELECT bucket_id FROM gallery_media  WHERE gallery_media._id=t_geo_file._id)");
            db.execSQL("CREATE TRIGGER tr_insert_geo_after_insert AFTER INSERT ON gallery_media  BEGIN insert into t_geo_file(_id, _data, bucket_relative_path, _display_name, latitude, longitude, bucket_id) values(new._id, new._data, new.bucket_relative_path, new._display_name, new.latitude, new.longitude, new.bucket_id) ;  END;");
        }
        if (fromVersion < 50009001) {
            db.execSQL("ALTER TABLE gallery_media ADD COLUMN visit_time INTEGER DEFAULT 0;");
        }
        if (fromVersion < 50010001) {
            db.execSQL("ALTER TABLE gallery_media ADD COLUMN relative_cloud_media_id INTEGER DEFAULT -1;");
        }
        if (fromVersion < 50011001) {
            db.execSQL("DROP TABLE IF EXISTS t_geo_dictionary;");
            db.execSQL("CREATE TABLE t_geo_dictionary (geo_code INTEGER NOT NULL, language TEXT, geo_name TEXT);");
            db.execSQL("CREATE INDEX idx_gen_geo_code ON t_geo_dictionary (geo_code ASC);");
            db.execSQL("CREATE INDEX idx_search_geo_name ON t_geo_dictionary (language DESC, geo_name ASC );");
            db.execSQL("INSERT INTO t_geo_dictionary (geo_code, language, geo_name) VALUES (0,'local','stub')");
            db.execSQL("UPDATE t_geo_file SET geo_code='', geo_name=''");
            db.execSQL("DELETE FROM t_geo_knowledge");
        }
        if (fromVersion < 50012001) {
            db.execSQL("DROP TABLE IF EXISTS t_geo_file;");
            db.execSQL("DROP TABLE IF EXISTS t_geo_dictionary;");
            db.execSQL("DROP TABLE IF EXISTS t_geo_knowledge;");
            db.execSQL("DROP TRIGGER IF EXISTS tr_insert_geo_after_insert;");
            db.execSQL("DROP TRIGGER IF EXISTS tr_update_geo_after_update;");
            db.execSQL("DROP TRIGGER IF EXISTS tr_delete_geo_after_delete;");
            db.execSQL("CREATE TABLE t_geo_dictionary (geo_code TEXT NOT NULL, language TEXT, geo_name TEXT);");
            db.execSQL("CREATE INDEX idx_search_geo_name ON t_geo_dictionary (language DESC, geo_name ASC );");
            db.execSQL("CREATE TABLE t_geo_knowledge(latitude DOUBLE, longitude DOUBLE, language TEXT NOT NULL, country TEXT, admin_area TEXT, sub_admin_area TEXT, locality TEXT, sub_locality TEXT, thoroughfare TEXT, sub_thoroughfare TEXT, feature_name TEXT);");
            db.execSQL("CREATE INDEX idx_geo_knowledge_key ON t_geo_knowledge (latitude ASC, longitude DESC, language DESC);");
            db.execSQL("ALTER TABLE gallery_media ADD COLUMN geo_code TEXT;");
            db.execSQL("CREATE INDEX geo_code_index ON gallery_media (geo_code ASC);");
        }
        if (fromVersion < 50013001) {
            db.execSQL("CREATE TRIGGER tr_update_hash_after AFTER UPDATE ON gallery_media BEGIN  update gallery_media set showDateToken = new.datetaken  where _id = new._id AND old.hash != new.hash;  END; ");
        }
        if (fromVersion < 50014001) {
            db.execSQL("DROP TABLE IF EXISTS t_geo_knowledge;");
            db.execSQL("CREATE TABLE t_geo_knowledge(latitude DOUBLE, longitude DOUBLE, location_key INTEGER, language TEXT NOT NULL, country TEXT, admin_area TEXT, sub_admin_area TEXT, locality TEXT, sub_locality TEXT, thoroughfare TEXT, sub_thoroughfare TEXT, feature_name TEXT);");
            db.execSQL("CREATE INDEX idx_geo_knowledge_key ON t_geo_knowledge (location_key DESC, language DESC);");
            db.execSQL("UPDATE gallery_media SET geo_code = NULL");
            db.execSQL("ALTER TABLE gallery_media ADD COLUMN location_key INTEGER");
            db.execSQL("ALTER TABLE gallery_media ADD COLUMN story_id TEXT;");
            db.execSQL("ALTER TABLE gallery_media ADD COLUMN story_cluster_state TEXT DEFAULT 'todo';");
            db.execSQL("CREATE INDEX story_album_index ON gallery_media (story_id ASC, story_cluster_state ASC);");
            db.execSQL("CREATE TABLE t_story_album (story_id TEXT, date TEXT, name TEXT, min_datetaken INTEGER, max_datetaken INTEGER, project_id TEXT, cover_id INTEGER);");
            db.execSQL("CREATE INDEX story_album_id_index ON t_story_album (story_id ASC);");
            db.execSQL("CREATE VIEW cluster_view as select t_story_album.story_id, t_story_album.date, t_story_album.name, t_story_album.cover_id from t_story_album left join gallery_media on gallery_media.story_id = t_story_album.story_id where gallery_media.story_id is not null and gallery_media.story_id != ''  and t_story_album.story_id is not null  and t_story_album.date is not null and t_story_album.date != ''  and t_story_album.name is not null and t_story_album.name != '' group by 1 order by t_story_album.min_datetaken DESC");
        }
        if (fromVersion < 50015001) {
            db.execSQL("CREATE TABLE IF NOT EXISTS t_search_index ( _id INTEGER NOT NULL, _data TEXT, location_key INTEGER, last_update_time INTEGER ); ");
            db.execSQL("CREATE INDEX idx_search_index_key ON t_search_index (_id DESC);");
            ContentValues indexToken = new ContentValues();
            indexToken.put("_id", Integer.valueOf(-1));
            indexToken.put("_data", "start token");
            indexToken.put("location_key", Integer.valueOf(0));
            indexToken.put("last_update_time", Integer.valueOf(0));
            db.insert("t_search_index", null, indexToken);
            db.execSQL("UPDATE gallery_media SET geo_code = NULL");
            db.execSQL("ALTER TABLE gallery_media ADD COLUMN last_update_time INTEGER");
            db.execSQL("ALTER TABLE gallery_media ADD COLUMN search_data_status INTEGER ");
            ContentValues values = new ContentValues();
            values.put("search_data_status", Integer.valueOf(5));
            values.put("last_update_time", Long.valueOf(System.currentTimeMillis()));
            db.update("gallery_media", values, null, null);
        }
        if (fromVersion < 50016001) {
            db.execSQL("ALTER TABLE gallery_media ADD COLUMN category_id INTEGER DEFAULT -1;");
            db.execSQL("DROP TABLE IF EXISTS image_collection;");
            db.execSQL("CREATE TABLE image_collection ( hash TEXT PRIMARY KEY,category_id  INTEGER NOT NULL ,sub_label  TEXT ,prob REAL NOT NULL);");
            db.execSQL("DROP TABLE IF EXISTS face;");
            db.execSQL("CREATE TABLE face (hash  TEXT ,face_id TEXT ,tag_id  TEXT NOT NULL ,scale_x REAL NOT NULL ,scale_y REAL NOT NULL ,scale_width REAL NOT NULL ,scale_height REAL NOT NULL ,landmarks BLOB NOT NULL ,features BLOB ,prob REAL ,user_operation INTEGER DEFAULT -1,PRIMARY KEY (hash, face_id));");
            db.execSQL("DROP TABLE IF EXISTS tag;");
            db.execSQL("CREATE TABLE tag (tag_id  TEXT PRIMARY KEY,tag_name TEXT DEFAULT '',user_operation  INTEGER DEFAULT 0,group_tag TEXT );");
        }
        if (fromVersion < 50017001) {
            db.execSQL("DROP VIEW IF EXISTS cluster_view;");
            db.execSQL("CREATE VIEW cluster_view as select t_story_album.story_id, t_story_album.min_datetaken, t_story_album.max_datetaken, t_story_album.name, t_story_album.cover_id from t_story_album left join gallery_media on gallery_media.story_id = t_story_album.story_id where gallery_media.story_id is not null and gallery_media.story_id != ''  and t_story_album.story_id is not null  and t_story_album.name is not null and t_story_album.name != '' group by 1 order by t_story_album.min_datetaken DESC");
        }
        if (fromVersion < 50018001) {
            String updateLocalClassifySql;
            db.execSQL("DROP TABLE IF EXISTS cloud_tag");
            db.execSQL("DROP TABLE IF EXISTS cloud_tag_file");
            db.execSQL("DROP TRIGGER IF EXISTS tr_update_hash_after;");
            if (GalleryUtils.IS_CHINESE_VERSION || GalleryUtils.PRODUCT_LITE) {
                updateLocalClassifySql = "";
            } else {
                updateLocalClassifySql = "update gallery_media set category_id = -1  where hash = old.hash and old.category_id!=-1; update image_collection set hash = new.hash  where hash = old.hash and old.category_id!=-1; update face set hash = new.hash  where hash = old.hash and old.category_id !=-1; ";
            }
            db.execSQL("CREATE TRIGGER tr_update_gallery_media_hash_after AFTER UPDATE ON gallery_media WHEN old.hash != new.hash BEGIN  update gallery_media set showDateToken = new.datetaken  where _id = new._id; " + updateLocalClassifySql + "END; ");
        }
        if (fromVersion < 50019001) {
            db.execSQL("ALTER TABLE gallery_media ADD COLUMN portrait_id TEXT;");
            db.execSQL("ALTER TABLE gallery_media ADD COLUMN portrait_cluster_state TEXT DEFAULT 'todo';");
            db.execSQL("CREATE INDEX portrait_album_index ON gallery_media (portrait_id ASC, portrait_cluster_state ASC);");
            db.execSQL("ALTER TABLE t_story_album ADD COLUMN album_type INTEGER;");
            db.execSQL("DROP VIEW IF EXISTS cluster_view;");
            db.execSQL("CREATE VIEW cluster_view as select t_story_album.story_id, t_story_album.min_datetaken, t_story_album.max_datetaken, t_story_album.name, t_story_album.cover_id from t_story_album left join gallery_media on (gallery_media.story_id = t_story_album.story_id or gallery_media.portrait_id = t_story_album.story_id) where ((gallery_media.story_id is not null and gallery_media.story_id != '') or (gallery_media.portrait_id is not null and gallery_media.portrait_id != '')) and t_story_album.story_id is not null  and t_story_album.name is not null and t_story_album.name != '' group by 1 order by t_story_album.min_datetaken DESC");
        }
        if (fromVersion < 50020001) {
            db.execSQL("ALTER TABLE gallery_media ADD COLUMN special_file_offset INTEGER;");
        }
        if (fromVersion < 50021001) {
            db.execSQL("CREATE TABLE local_recycled_file (_id  INTEGER PRIMARY KEY AUTOINCREMENT, _data  TEXT COLLATE NOCASE ,_size  INTEGER,date_added  INTEGER,date_modified  INTEGER,mime_type  TEXT,title  TEXT,orientation  INTEGER,latitude  DOUBLE,longitude  DOUBLE,datetaken  INTEGER,bucket_id  TEXT,duration  INTEGER,resolution  TEXT,_display_name TEXT,media_type  INTEGER,width  INTEGER,height  INTEGER,is_hdr  INTEGER,is_hw_burst  INTEGER DEFAULT 0,hw_voice_offset  INTEGER,hw_image_refocus  INTEGER,hw_rectify_offset  INTEGER,hash  TEXT,special_file_type INTEGER,showDateToken INTEGER DEFAULT 11,special_file_offset INTEGER,recycledTime INTEGER,galleryId INTEGER,uniqueId TEXT,sourcePath TEXT);");
            db.execSQL("CREATE TABLE cloud_recycled_file (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,size INTEGER,hash TEXT,localThumbPath TEXT,localBigThumbPath TEXT,localRealPath TEXT,fileName Text,orientation INTEGER,albumId TEXT NOT NULL,fyuseAttach TEXT,duration INTEGER,latitude DOUBLE,longitude DOUBLE,fileType INTEGER,fileId TEXT,source TEXT,videoThumbId TEXT,createTime INTEGER,expand TEXT,thumbType INTEGER default 0,recycleFlag INTEGER,recycledTime INTEGER,galleryId INTEGER,uniqueId TEXT,recycleAlbumId TEXT,sourceFileName Text, unique(uniqueId));");
            db.execSQL("ALTER TABLE cloud_file ADD COLUMN uniqueId TEXT;");
            db.execSQL("ALTER TABLE gallery_media ADD COLUMN uniqueId TEXT;");
            db.execSQL("CREATE VIEW gallery_recycled_file as select ifnull(ifnull(cloud_recycled_file.galleryId, local_recycled_file.galleryId), 0) as _id, ifnull(local_recycled_file._id, -1) as local_media_id, ifnull(cloud_recycled_file.id, -1) as cloud_media_id, local_recycled_file._data, ifnull(cloud_recycled_file.size, local_recycled_file._size) as _size, ifnull(local_recycled_file.date_added, cloud_recycled_file.createTime) as date_added, ifnull(local_recycled_file.date_modified, cloud_recycled_file.createTime) as date_modified, ifnull(local_recycled_file.title, cloud_recycled_file.fileName) as title, ifnull(local_recycled_file.orientation, cloud_recycled_file.orientation) as orientation, ifnull(cloud_recycled_file.latitude, local_recycled_file.latitude) as latitude, ifnull(cloud_recycled_file.longitude, local_recycled_file.longitude) as longitude, ifnull(local_recycled_file.datetaken, cloud_recycled_file.createTime) as datetaken, ifnull(local_recycled_file.datetaken, cloud_recycled_file.createTime) as showDateToken, ifnull(cloud_recycled_file.duration, local_recycled_file.duration) as duration, ifnull(cloud_recycled_file.hash, local_recycled_file.hash) as hash, ifnull(cloud_recycled_file.recycledTime, local_recycled_file.recycledTime) as recycledTime, ifnull(cloud_recycled_file.uniqueId, local_recycled_file.uniqueId) as uniqueId, ifnull(cloud_recycled_file.galleryId, local_recycled_file.galleryId) as galleryId, ifnull(local_recycled_file._display_name, cloud_recycled_file.fileName) as _display_name, ifnull(local_recycled_file._display_name, cloud_recycled_file.sourceFileName) as _source_display_name, ifnull(local_recycled_file.is_hw_burst, case UPPER(substr(cloud_recycled_file.sourceFileName, length(cloud_recycled_file.sourceFileName) - length(\"_COVER.JPG\") + 1))when \"_COVER.JPG\" then 1 else (case UPPER(substr(cloud_recycled_file.fileName, length(cloud_recycled_file.fileName) - length(\"_COVER.JPG\") + 1))when \"_COVER.JPG\" then 1 else 0 end) end) as is_hw_burst, local_recycled_file.resolution, local_recycled_file.is_hdr, cloud_recycled_file.albumId as cloud_bucket_id, cloud_recycled_file.fileType, cloud_recycled_file.fileId, cloud_recycled_file.videoThumbId, cloud_recycled_file.thumbType, cloud_recycled_file.localThumbPath, cloud_recycled_file.localBigThumbPath, cloud_recycled_file.localRealPath, cloud_recycled_file.expand, cloud_recycled_file.source, local_recycled_file.mime_type, local_recycled_file.media_type, local_recycled_file.bucket_id,  local_recycled_file.width, local_recycled_file.height, local_recycled_file.hw_voice_offset, local_recycled_file.hw_image_refocus, local_recycled_file.hw_rectify_offset, local_recycled_file.special_file_type, local_recycled_file.special_file_offset,  '' as description, '' as bucket_display_name,  '' as storage_id,  '0' as is_hw_privacy,  '0' as is_hw_favorite,  '' as contenturi, '' as special_file_list, '0' as dirty, '' as bucket_relative_path,  '0' as visit_time, '' as relative_cloud_media_id, '' as geo_code, '-1' as location_key,  '' as story_id,  'todo' as story_cluster_state, '0' as search_data_status, '-1' as category_id,  '0' as last_update_time, '' as portrait_id, 'todo' as portrait_cluster_state, local_recycled_file.sourcePath, cloud_recycled_file.fyuseAttach, cloud_recycled_file.recycleFlag, cloud_recycled_file.recycleAlbumId   from local_recycled_file left join cloud_recycled_file on (local_recycled_file.galleryId = cloud_recycled_file.galleryId or local_recycled_file.uniqueId = cloud_recycled_file.uniqueId)union select ifnull(ifnull(cloud_recycled_file.galleryId, local_recycled_file.galleryId), 0) as _id, ifnull(local_recycled_file._id, -1) as local_media_id, ifnull(cloud_recycled_file.id, -1) as cloud_media_id, local_recycled_file._data, ifnull(cloud_recycled_file.size, local_recycled_file._size) as _size, ifnull(local_recycled_file.date_added, cloud_recycled_file.createTime) as date_added, ifnull(local_recycled_file.date_modified, cloud_recycled_file.createTime) as date_modified, ifnull(local_recycled_file.title, cloud_recycled_file.fileName) as title, ifnull(local_recycled_file.orientation, cloud_recycled_file.orientation) as orientation, ifnull(cloud_recycled_file.latitude, local_recycled_file.latitude) as latitude, ifnull(cloud_recycled_file.longitude, local_recycled_file.longitude) as longitude, ifnull(local_recycled_file.datetaken, cloud_recycled_file.createTime) as datetaken, ifnull(local_recycled_file.datetaken, cloud_recycled_file.createTime) as showDateToken, ifnull(cloud_recycled_file.duration, local_recycled_file.duration) as duration, ifnull(cloud_recycled_file.hash, local_recycled_file.hash) as hash, ifnull(cloud_recycled_file.recycledTime, local_recycled_file.recycledTime) as recycledTime, ifnull(cloud_recycled_file.uniqueId, local_recycled_file.uniqueId) as uniqueId, ifnull(cloud_recycled_file.galleryId, local_recycled_file.galleryId) as galleryId, ifnull(local_recycled_file._display_name, cloud_recycled_file.fileName) as _display_name, ifnull(local_recycled_file._display_name, cloud_recycled_file.sourceFileName) as _source_display_name, ifnull(local_recycled_file.is_hw_burst, case UPPER(substr(cloud_recycled_file.sourceFileName, length(cloud_recycled_file.sourceFileName) - length(\"_COVER.JPG\") + 1))when \"_COVER.JPG\" then 1 else (case UPPER(substr(cloud_recycled_file.fileName, length(cloud_recycled_file.fileName) - length(\"_COVER.JPG\") + 1))when \"_COVER.JPG\" then 1 else 0 end) end) as is_hw_burst, local_recycled_file.resolution, local_recycled_file.is_hdr, cloud_recycled_file.albumId as cloud_bucket_id, cloud_recycled_file.fileType, cloud_recycled_file.fileId, cloud_recycled_file.videoThumbId, cloud_recycled_file.thumbType, cloud_recycled_file.localThumbPath, cloud_recycled_file.localBigThumbPath, cloud_recycled_file.localRealPath, cloud_recycled_file.expand, cloud_recycled_file.source, local_recycled_file.mime_type, local_recycled_file.media_type, local_recycled_file.bucket_id,  local_recycled_file.width, local_recycled_file.height, local_recycled_file.hw_voice_offset, local_recycled_file.hw_image_refocus, local_recycled_file.hw_rectify_offset, local_recycled_file.special_file_type, local_recycled_file.special_file_offset,  '' as description, '' as bucket_display_name,  '' as storage_id,  '0' as is_hw_privacy,  '0' as is_hw_favorite,  '' as contenturi, '' as special_file_list, '0' as dirty, '' as bucket_relative_path,  '0' as visit_time, '' as relative_cloud_media_id, '' as geo_code, '-1' as location_key,  '' as story_id,  'todo' as story_cluster_state, '0' as search_data_status, '-1' as category_id,  '0' as last_update_time, '' as portrait_id, 'todo' as portrait_cluster_state, local_recycled_file.sourcePath, cloud_recycled_file.fyuseAttach, cloud_recycled_file.recycleFlag, cloud_recycled_file.recycleAlbumId   from cloud_recycled_file left join local_recycled_file on (local_recycled_file.galleryId = cloud_recycled_file.galleryId or local_recycled_file.uniqueId = cloud_recycled_file.uniqueId)where (recycleFlag = -1 or recycleFlag = 1) order by recycledTime DESC ");
        }
        if (fromVersion < 50022001) {
            db.execSQL("CREATE TABLE cloud_file_new (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,size INTEGER,hash TEXT,localThumbPath TEXT,localBigThumbPath TEXT,localRealPath TEXT,fileName Text,orientation INTEGER,albumId TEXT NOT NULL,fyuseAttach TEXT,duration INTEGER,latitude DOUBLE,longitude DOUBLE,fileType INTEGER,fileId TEXT,source TEXT,videoThumbId TEXT,createTime INTEGER,expand TEXT,thumbType INTEGER default 0,deleteFlag INTEGER default 0,uniqueId TEXT,unique(hash, albumId,uniqueId));");
            db.execSQL("INSERT INTO cloud_file_new SELECT * FROM cloud_file");
            db.execSQL("DROP TABLE IF EXISTS cloud_file");
            db.execSQL("ALTER TABLE cloud_file_new RENAME to cloud_file");
            db.execSQL("CREATE VIEW general_cloud_file as SELECT cloud_file.id as id,cloud_file.size as size,cloud_file.hash as hash,cloud_file.localThumbPath as localThumbPath,cloud_file.localBigThumbPath as localBigThumbPath,cloud_file.localRealPath as localRealPath,cloud_file.fileName as fileName,cloud_file.orientation as orientation,cloud_file.albumId as albumId,cloud_file.fyuseAttach as fyuseAttach,cloud_file.duration as duration,cloud_file.latitude as latitude,cloud_file.longitude as longitude,cloud_file.fileType as fileType,cloud_file.fileId as fileId,cloud_file.source as source,cloud_file.videoThumbId as videoThumbId,cloud_file.createTime as createTime,cloud_file.expand as expand,cloud_file.thumbType as thumbType,cloud_file.deleteFlag as deleteFlag,cloud_file.uniqueId as uniqueId,cloud_recycled_file.recycleFlag as recycleFlag from cloud_file left join cloud_recycled_file on (cloud_file.uniqueId = cloud_recycled_file.uniqueId)");
        }
        if (fromVersion < 50023001) {
            db.execSQL("CREATE INDEX local_galleryId_idx ON local_recycled_file (galleryId ASC);");
            db.execSQL("CREATE INDEX local_uniqueId_idx ON local_recycled_file (uniqueId ASC);");
            db.execSQL("CREATE INDEX cloud_galleryId_idx ON cloud_recycled_file (galleryId ASC);");
            db.execSQL("CREATE INDEX cloud_uniqueId_idx ON cloud_recycled_file (uniqueId ASC);");
            db.execSQL("CREATE INDEX cloud_sourceFileName_idx ON cloud_recycled_file (sourceFileName ASC);");
            db.execSQL("CREATE INDEX cloud_fileName_idx ON cloud_recycled_file (fileName ASC);");
        }
    }
}
