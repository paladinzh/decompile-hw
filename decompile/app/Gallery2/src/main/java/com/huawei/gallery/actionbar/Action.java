package com.huawei.gallery.actionbar;

import com.android.gallery3d.R;
import com.android.gallery3d.util.GalleryLog;

public enum Action {
    NONE(0, 0),
    OK((String) R.drawable.ic_public_ok, (int) R.drawable.ic_public_ok_dark, (int) R.string.ok),
    NO((String) R.drawable.ic_public_cancel, (int) R.drawable.ic_public_cancel_dark, (int) R.string.cancel),
    BACK((String) R.drawable.ic_public_back, (int) R.drawable.ic_public_back_dark, (int) R.string.cancel),
    ALBUM((String) R.drawable.ic_menu_album, (int) R.drawable.ic_menu_album_white, (int) R.string.local_camera_list),
    MENU((String) R.drawable.ic_public_more, (int) R.drawable.ic_public_more, (int) R.string.more),
    SHARE((String) R.drawable.ic_public_share, (int) R.drawable.ic_public_share, (int) R.string.share),
    MOVE((String) R.drawable.ic_public_move, (int) R.drawable.ic_public_move, (int) R.string.cut),
    DEL((String) R.drawable.ic_public_delete, R.drawable.ic_public_delete, (int) R.string.delete),
    ADD((String) R.drawable.ic_public_add, R.drawable.ic_public_add, (int) R.string.action_title_add),
    TIME((String) R.drawable.ic_menu_timeline, (int) R.drawable.ic_menu_timeline_white, (int) R.string.time),
    EDIT(R.drawable.ic_public_edit, R.string.edit),
    COPY(0, R.string.copy),
    RENAME(0, R.string.rename),
    WITH_UPDATE(R.drawable.ic_public_download_tips_gallery, R.string.action_title_update),
    WITHOUT_UPDATE(R.drawable.ic_public_download, R.string.action_title_update),
    DETAIL(R.drawable.ic_gallery_info, R.string.details),
    CANCEL_DETAIL(R.drawable.ic_gallery_info_actived, R.string.details),
    SLIDESHOW(0, R.string.slideshow),
    SETAS(0, R.string.set_as),
    SETTINGS((String) R.drawable.ic_public_settings, (int) R.drawable.ic_public_settings, (int) R.string.settings),
    LOOPPLAY(0, R.string.loop_play_video),
    SAVE(R.drawable.ic_public_save, R.string.save),
    INFO(R.drawable.ic_gallery_info, R.string.freeshare_info_action_title),
    RE_SEARCH(R.drawable.ic_public_refresh, R.string.freeshare_info_action_title),
    ALL(false, R.drawable.ic_public_select_all, R.drawable.ic_public_select_all, R.string.select_all),
    DEALL((String) true, (int) R.drawable.ic_public_deselect_all, (boolean) R.string.deselect_all),
    MYFAVORITE((String) true, (int) R.drawable.ic_public_favor_selected, (boolean) R.string.favorite_cancel_favorite_general),
    NOT_MYFAVORITE((String) false, (int) R.drawable.ic_public_favor, (boolean) R.string.favorite_set_as_favorite_general),
    MOVEOUT((String) R.drawable.ic_gallery_move_out, (int) R.drawable.ic_gallery_move_out, (int) R.string.move_out),
    MOVEIN((String) R.drawable.ic_gallery_add_to_others, (int) R.drawable.ic_gallery_add_to_others, (int) R.string.move_in),
    HIDE(0, R.string.hide_albums),
    ADD_ALBUM((String) R.drawable.ic_public_add, R.drawable.ic_public_add, (int) R.string.new_album),
    AIRSHARE(R.drawable.ic_menu_airshar_white, R.string.comment),
    COMMENT(R.drawable.ic_menu_comment_white, R.string.comment),
    GOTO_GALLERY(R.drawable.ic_gallery_gallery, R.string.app_name),
    PHOTOSHARE_CREATE_NEW_SHARE((String) R.drawable.ic_public_add, R.drawable.ic_public_add, (int) R.string.photoshare_create_album),
    PHOTOSHARE_RENAME((String) R.drawable.ic_gallery_rename, (int) R.drawable.ic_gallery_rename, (int) R.string.rename),
    PHOTOSHARE_SETTINGS((String) R.drawable.ic_public_settings, (int) R.drawable.ic_public_settings, (int) R.string.settings),
    PHOTOSHARE_DOWNLOAD(false, R.drawable.ic_public_download, R.drawable.ic_public_download, R.string.photoshare_download_short, R.string.photoshare_has_download),
    PHOTOSHARE_DOWNLOADING(false, R.drawable.ic_public_download, R.drawable.ic_public_download, R.string.photoshare_downloading, R.string.photoshare_downloading, true),
    PHOTOSHARE_ADDPICTURE((String) R.drawable.ic_public_add, R.drawable.ic_public_add, (int) R.string.photoshare_add_picture),
    PHOTOSHARE_CONTACT((String) R.drawable.ic_public_contacts, (int) R.drawable.ic_public_contacts, (int) R.string.photoshare_modify_album_members),
    PHOTOSHARE_DELETE((String) R.drawable.ic_public_delete, R.drawable.ic_public_delete, (int) R.string.delete),
    PHOTOSHARE_CANCEL_RECEIVE((String) R.drawable.ic_gallery_share_album_quit, (int) R.drawable.ic_gallery_share_album_quit, (int) R.string.photoshare_cancel_my_receive),
    PHOTOSHARE_MULTI_DOWNLOAD((String) R.drawable.ic_public_download, R.drawable.ic_public_download, (int) R.string.photoshare_download_short),
    PHOTOSHARE_EDITSHARE(0, R.string.photoshare_edit_my_share),
    PHOTOSHARE_BACKUP(0, R.string.menu_photoshare_uploadtocloud),
    PHOTOSHARE_ACCOUNT((String) R.drawable.ic_gallery_contacts, (int) R.drawable.ic_gallery_contacts, (int) R.string.photoshare_huawei_account),
    PHOTOSHARE_MESSAGE((String) R.drawable.ic_gallery_message, (int) R.drawable.ic_gallery_message, (int) R.string.photoshare_message_invite),
    PHOTOSHARE_EMAIL((String) R.drawable.ic_gallery_mail, (int) R.drawable.ic_gallery_mail, (int) R.string.photoshare_email_invite),
    PHOTOSHARE_LINK((String) R.drawable.ic_gallery_link, (int) R.drawable.ic_gallery_link, (int) R.string.photoshare_uri_invite),
    PHOTOSHARE_MANAGE_UPLOAD(0, R.string.photoshare_down_up_tab_upload),
    PHOTOSHARE_MANAGE_DOWNLOAD(0, R.string.photoshare_down_up_tab_download),
    PHOTOSHARE_PAUSE((String) R.drawable.ic_gallery_download_pause, (int) R.drawable.ic_gallery_download_pause, (int) R.string.photoshare_multichoose_pause),
    PHOTOSHARE_DOWNLOAD_START((String) R.drawable.ic_public_download, R.drawable.ic_public_download, (int) R.string.photoshare_down_up_start),
    PHOTOSHARE_UPLOAD_START((String) R.drawable.ic_gallery_upload, (int) R.drawable.ic_gallery_upload, (int) R.string.photoshare_down_up_start),
    PHOTOSHARE_CLEAR((String) R.drawable.ic_gallery_clean, (int) R.drawable.ic_gallery_clean, (int) R.string.delete),
    PHOTOSHARE_MOVE((String) R.drawable.ic_gallery_move_out, (int) R.drawable.ic_gallery_move_out, (int) R.string.photoshare_move_classify),
    PHOTOSHARE_COMBINE((String) R.drawable.ic_gallery_album_compose, (int) R.drawable.ic_gallery_album_compose, (int) R.string.photoshare_combine),
    PHOTOSHARE_NOT_THIS_PERSON((String) R.drawable.ic_gallery_not_this_person, (int) R.drawable.ic_gallery_not_this_person, (int) R.string.photoshare_not_this_person),
    PHOTOSHARE_CREATE_NEW_PEOPLE_TAG((String) R.drawable.ic_public_add, R.drawable.ic_public_add, (int) R.string.photoshare_create_album),
    PHOTOSHARE_REMOVE_PEOPLE_TAG((String) R.drawable.ic_gallery_move_out, (int) R.drawable.ic_gallery_move_out, (int) R.string.move_out),
    SAVE_BURST(R.drawable.ic_public_save, R.string.save_burst),
    TOGIF(R.drawable.ic_menu_creat_movie_white, R.string.burst_to_gif),
    SETAS_UNLOCK(R.drawable.ic_menu_set_unlock_white, R.string.setas_lock),
    SETAS_HOME(R.drawable.ic_menu_set_wallpaper_white, R.string.setas_home),
    SETAS_BOTH(R.drawable.ic_menu_set_together_white, R.string.setas_both),
    SETAS_FIXED(R.drawable.ic_gallery_wallpaper_fixed, R.string.setas_home_fixed),
    SETAS_SCROLLABLE(R.drawable.ic_gallery_wallpaper_scrollable, R.string.setas_home_scrollable),
    SETAS_FIXED_ACTIVED((String) true, (int) R.drawable.ic_gallery_wallpaper_fixed_actived, (boolean) R.string.setas_home_fixed),
    SETAS_SCROLLABLE_ACTIVED((String) true, (int) R.drawable.ic_gallery_wallpaper_scrollable_actived, (boolean) R.string.setas_home_scrollable),
    MULTI_SELECTION(R.drawable.allfocus_mono, R.string.select_image),
    MULTI_SELECTION_ON(R.drawable.allfocus_mono_pinfocus, R.string.select_image),
    SINGLE_SELECTION(R.drawable.btn_radio_off_emui_black, R.string.selection_done),
    SINGLE_SELECTION_ON(R.drawable.btn_radio_on_emui_black, R.string.selection_done),
    MULTISCREEN(R.drawable.ic_gallery_multiscreen, R.string.multiscreen_title),
    MULTISCREEN_ACTIVITED(R.drawable.ic_gallery_multiscreen_activited, R.string.multiscreen_title),
    PRINT(0, R.string.print_or_export),
    SHOW_ON_MAP(0, R.string.show_on_map),
    MAP((String) R.drawable.ic_gallery_map_all, (int) R.drawable.ic_gallery_map_all, (int) R.string.browse_mode_map),
    DYNAMIC_ALBUM((String) R.drawable.ic_menu_dynamic_album, (int) R.drawable.ic_menu_dynamic_album_white, (int) R.string.dynamic_album),
    COLLAGE((String) R.drawable.ic_gallery_collage, (int) R.drawable.ic_gallery_collage, (int) R.string.collage),
    ROTATE_LEFT(0, R.string.rotate_left),
    ROTATE_RIGHT(0, R.string.rotate),
    MORE_EDIT(0, R.string.more_edit),
    ADD_COMMENT(0, R.string.add_comment),
    EDIT_COMMENT(0, R.string.edit_comment),
    REMOVE((String) R.drawable.ic_public_delete, R.drawable.ic_public_delete, (int) R.string.action_remove_title),
    KEYGUARD_LIKE((String) true, (int) R.drawable.ic_public_favor_selected, (boolean) R.string.favorite_cancel_favorite_general),
    KEYGUARD_NOT_LIKE((String) false, (int) R.drawable.ic_public_favor, (boolean) R.string.favorite_set_as_favorite_general),
    SEE_BARCODE_INFO(0, R.string.barcode_recognize),
    UNDO(R.drawable.ic_gallery_edit_undo, R.string.filtershow_undo),
    REDO(R.drawable.ic_gallery_edit_redo, R.string.filtershow_redo),
    RANGE_MEASURE(0, R.string.range_measure),
    STORY_ITEM_REMOVE((String) R.drawable.ic_gallery_move_out, (int) R.drawable.ic_gallery_move_out, (int) R.string.move_out),
    STORY_ALBUM_REMOVE((String) R.drawable.ic_public_delete, R.drawable.ic_public_delete, (int) R.string.delete),
    STORY_RENAME(R.drawable.ic_gallery_rename, R.string.rename),
    RECYCLE_RECOVERY((String) R.drawable.ic_gallery_trash_recover1, (int) R.drawable.ic_gallery_trash_recover1, (int) R.string.toolbarbutton_recover),
    RECYCLE_DELETE((String) R.drawable.ic_public_delete, R.drawable.ic_public_delete, (int) R.string.delete),
    RECYCLE_CLEAN_BIN((String) R.drawable.ic_public_delete, R.drawable.ic_public_delete, (int) R.string.button_recentlydeletedclearall);
    
    public static final int ACTION_ID_ADD = 0;
    public static final int ACTION_ID_ADD_ALBUM = 0;
    public static final int ACTION_ID_ADD_COMMENT = 0;
    public static final int ACTION_ID_AIRSHARE = 0;
    public static final int ACTION_ID_ALBUM = 0;
    public static final int ACTION_ID_ALL = 0;
    public static final int ACTION_ID_BACK = 0;
    public static final int ACTION_ID_CANCEL_DETAIL = 0;
    public static final int ACTION_ID_COLLAGE = 0;
    public static final int ACTION_ID_COMMENT = 0;
    public static final int ACTION_ID_COPY = 0;
    public static final int ACTION_ID_DEALL = 0;
    public static final int ACTION_ID_DEL = 0;
    public static final int ACTION_ID_DETAIL = 0;
    public static final int ACTION_ID_DYNAMIC_ALBUM = 0;
    public static final int ACTION_ID_EDIT = 0;
    public static final int ACTION_ID_EDIT_COMMENT = 0;
    public static final int ACTION_ID_HIDE = 0;
    public static final int ACTION_ID_KEYGUARD_LIKE = 0;
    public static final int ACTION_ID_KEYGUARD_NOT_LIKE = 0;
    public static final int ACTION_ID_LOOP_PLAY_VIDEO = 0;
    public static final int ACTION_ID_MAP = 0;
    public static final int ACTION_ID_MENU = 0;
    public static final int ACTION_ID_MORE_EDIT = 0;
    public static final int ACTION_ID_MOVE = 0;
    public static final int ACTION_ID_MOVEIN = 0;
    public static final int ACTION_ID_MOVEOUT = 0;
    public static final int ACTION_ID_MULTISCREEN = 0;
    public static final int ACTION_ID_MULTISCREEN_ACTIVITED = 0;
    public static final int ACTION_ID_MYFAVORITE = 0;
    public static final int ACTION_ID_NO = 0;
    public static final int ACTION_ID_NONE = 0;
    public static final int ACTION_ID_NOT_MYFAVORITE = 0;
    public static final int ACTION_ID_OK = 0;
    public static final int ACTION_ID_PHOTOSHARE_ACCOUNT = 0;
    public static final int ACTION_ID_PHOTOSHARE_ADDPICTURE = 0;
    public static final int ACTION_ID_PHOTOSHARE_BACKUP = 0;
    public static final int ACTION_ID_PHOTOSHARE_CANCEL_RECEIVE = 0;
    public static final int ACTION_ID_PHOTOSHARE_CLEAR = 0;
    public static final int ACTION_ID_PHOTOSHARE_COMBINE = 0;
    public static final int ACTION_ID_PHOTOSHARE_CONTACT = 0;
    public static final int ACTION_ID_PHOTOSHARE_CREATE_NEW_SHARE = 0;
    public static final int ACTION_ID_PHOTOSHARE_DELETE = 0;
    public static final int ACTION_ID_PHOTOSHARE_DOWNLOAD = 0;
    public static final int ACTION_ID_PHOTOSHARE_DOWNLOADING = 0;
    public static final int ACTION_ID_PHOTOSHARE_DOWNLOAD_START = 0;
    public static final int ACTION_ID_PHOTOSHARE_EDITSHARE = 0;
    public static final int ACTION_ID_PHOTOSHARE_EMAIL = 0;
    public static final int ACTION_ID_PHOTOSHARE_LINK = 0;
    public static final int ACTION_ID_PHOTOSHARE_MANAGE_DOWNLOAD = 0;
    public static final int ACTION_ID_PHOTOSHARE_MESSAGE = 0;
    public static final int ACTION_ID_PHOTOSHARE_MOVE = 0;
    public static final int ACTION_ID_PHOTOSHARE_MUITI_DOWNLOAD = 0;
    public static final int ACTION_ID_PHOTOSHARE_PAUSE = 0;
    public static final int ACTION_ID_PHOTOSHARE_RENAME = 0;
    public static final int ACTION_ID_PHOTOSHARE_SETTINGS = 0;
    public static final int ACTION_ID_PHOTOSHARE_UPLOAD_START = 0;
    public static final int ACTION_ID_PRINT = 0;
    public static final int ACTION_ID_RANGE_MEASURE = 0;
    public static final int ACTION_ID_REMOVE = 0;
    public static final int ACTION_ID_RENAME = 0;
    public static final int ACTION_ID_ROTATE_LEFT = 0;
    public static final int ACTION_ID_ROTATE_RIGHT = 0;
    public static final int ACTION_ID_SAVE = 0;
    public static final int ACTION_ID_SEE_BARCODE_INFO = 0;
    public static final int ACTION_ID_SETAS = 0;
    public static final int ACTION_ID_SETTINGS = 0;
    public static final int ACTION_ID_SHARE = 0;
    public static final int ACTION_ID_SHOW_ON_MAP = 0;
    public static final int ACTION_ID_SLIDESHOW = 0;
    public static final int ACTION_ID_TIME = 0;
    public final int disableTextResID;
    public final boolean hasProgress;
    public final boolean highLight;
    public final int iconResID;
    public final int iconWhiteResID;
    public final int id;
    public final int textResID;

    static {
        ACTION_ID_NONE = NONE.ordinal();
        ACTION_ID_OK = OK.ordinal();
        ACTION_ID_NO = NO.ordinal();
        ACTION_ID_BACK = BACK.ordinal();
        ACTION_ID_ALBUM = ALBUM.ordinal();
        ACTION_ID_MENU = MENU.ordinal();
        ACTION_ID_SHARE = SHARE.ordinal();
        ACTION_ID_MOVE = MOVE.ordinal();
        ACTION_ID_DEL = DEL.ordinal();
        ACTION_ID_ALL = ALL.ordinal();
        ACTION_ID_DEALL = DEALL.ordinal();
        ACTION_ID_ADD = ADD.ordinal();
        ACTION_ID_TIME = TIME.ordinal();
        ACTION_ID_EDIT = EDIT.ordinal();
        ACTION_ID_MORE_EDIT = MORE_EDIT.ordinal();
        ACTION_ID_COPY = COPY.ordinal();
        ACTION_ID_RENAME = RENAME.ordinal();
        ACTION_ID_CANCEL_DETAIL = CANCEL_DETAIL.ordinal();
        ACTION_ID_DETAIL = DETAIL.ordinal();
        ACTION_ID_SLIDESHOW = SLIDESHOW.ordinal();
        ACTION_ID_AIRSHARE = AIRSHARE.ordinal();
        ACTION_ID_COMMENT = COMMENT.ordinal();
        ACTION_ID_MYFAVORITE = MYFAVORITE.ordinal();
        ACTION_ID_SETAS = SETAS.ordinal();
        ACTION_ID_MOVEOUT = MOVEOUT.ordinal();
        ACTION_ID_MOVEIN = MOVEIN.ordinal();
        ACTION_ID_SETTINGS = SETTINGS.ordinal();
        ACTION_ID_LOOP_PLAY_VIDEO = LOOPPLAY.ordinal();
        ACTION_ID_HIDE = HIDE.ordinal();
        ACTION_ID_ADD_ALBUM = ADD_ALBUM.ordinal();
        ACTION_ID_PHOTOSHARE_SETTINGS = PHOTOSHARE_SETTINGS.ordinal();
        ACTION_ID_PHOTOSHARE_DOWNLOAD = PHOTOSHARE_DOWNLOAD.ordinal();
        ACTION_ID_PHOTOSHARE_DOWNLOADING = PHOTOSHARE_DOWNLOADING.ordinal();
        ACTION_ID_PHOTOSHARE_MUITI_DOWNLOAD = PHOTOSHARE_MULTI_DOWNLOAD.ordinal();
        ACTION_ID_PHOTOSHARE_ADDPICTURE = PHOTOSHARE_ADDPICTURE.ordinal();
        ACTION_ID_PHOTOSHARE_CONTACT = PHOTOSHARE_CONTACT.ordinal();
        ACTION_ID_PHOTOSHARE_EDITSHARE = PHOTOSHARE_EDITSHARE.ordinal();
        ACTION_ID_PHOTOSHARE_CANCEL_RECEIVE = PHOTOSHARE_CANCEL_RECEIVE.ordinal();
        ACTION_ID_PHOTOSHARE_RENAME = PHOTOSHARE_RENAME.ordinal();
        ACTION_ID_PHOTOSHARE_CREATE_NEW_SHARE = PHOTOSHARE_CREATE_NEW_SHARE.ordinal();
        ACTION_ID_PHOTOSHARE_DELETE = PHOTOSHARE_DELETE.ordinal();
        ACTION_ID_PHOTOSHARE_BACKUP = PHOTOSHARE_BACKUP.ordinal();
        ACTION_ID_PHOTOSHARE_ACCOUNT = PHOTOSHARE_ACCOUNT.ordinal();
        ACTION_ID_PHOTOSHARE_MESSAGE = PHOTOSHARE_MESSAGE.ordinal();
        ACTION_ID_PHOTOSHARE_EMAIL = PHOTOSHARE_EMAIL.ordinal();
        ACTION_ID_PHOTOSHARE_LINK = PHOTOSHARE_LINK.ordinal();
        ACTION_ID_PHOTOSHARE_PAUSE = PHOTOSHARE_PAUSE.ordinal();
        ACTION_ID_PHOTOSHARE_DOWNLOAD_START = PHOTOSHARE_DOWNLOAD_START.ordinal();
        ACTION_ID_PHOTOSHARE_UPLOAD_START = PHOTOSHARE_UPLOAD_START.ordinal();
        ACTION_ID_PHOTOSHARE_MANAGE_DOWNLOAD = PHOTOSHARE_MANAGE_DOWNLOAD.ordinal();
        ACTION_ID_PHOTOSHARE_CLEAR = PHOTOSHARE_CLEAR.ordinal();
        ACTION_ID_PHOTOSHARE_MOVE = PHOTOSHARE_MOVE.ordinal();
        ACTION_ID_PHOTOSHARE_COMBINE = PHOTOSHARE_COMBINE.ordinal();
        ACTION_ID_SAVE = SAVE.ordinal();
        ACTION_ID_NOT_MYFAVORITE = NOT_MYFAVORITE.ordinal();
        ACTION_ID_MULTISCREEN = MULTISCREEN.ordinal();
        ACTION_ID_MULTISCREEN_ACTIVITED = MULTISCREEN_ACTIVITED.ordinal();
        ACTION_ID_PRINT = PRINT.ordinal();
        ACTION_ID_SHOW_ON_MAP = SHOW_ON_MAP.ordinal();
        ACTION_ID_MAP = MAP.ordinal();
        ACTION_ID_ROTATE_LEFT = ROTATE_LEFT.ordinal();
        ACTION_ID_ROTATE_RIGHT = ROTATE_RIGHT.ordinal();
        ACTION_ID_REMOVE = REMOVE.ordinal();
        ACTION_ID_DYNAMIC_ALBUM = DYNAMIC_ALBUM.ordinal();
        ACTION_ID_COLLAGE = COLLAGE.ordinal();
        ACTION_ID_RANGE_MEASURE = RANGE_MEASURE.ordinal();
        ACTION_ID_KEYGUARD_LIKE = KEYGUARD_LIKE.ordinal();
        ACTION_ID_KEYGUARD_NOT_LIKE = KEYGUARD_NOT_LIKE.ordinal();
        ACTION_ID_ADD_COMMENT = ADD_COMMENT.ordinal();
        ACTION_ID_EDIT_COMMENT = EDIT_COMMENT.ordinal();
        ACTION_ID_SEE_BARCODE_INFO = SEE_BARCODE_INFO.ordinal();
    }

    private Action(int iconResID, int textResID) {
        this(r7, r8, iconResID, iconResID, textResID);
    }

    private Action(int iconResID, int iconWhiteResID, int textResID) {
        this(r8, r9, false, iconResID, iconWhiteResID, textResID);
    }

    private Action(boolean highLight, int iconResID, int textResID) {
        this(r8, r9, highLight, iconResID, iconResID, textResID);
    }

    private Action(boolean highLight, int iconResID, int iconWhiteResID, int textResID) {
        this(r9, r10, highLight, iconResID, iconWhiteResID, textResID, -1);
    }

    private Action(boolean highLight, int iconResID, int iconWhiteResID, int textResID, int disableTextResID) {
        this(r10, r11, highLight, iconResID, iconWhiteResID, textResID, disableTextResID, false);
    }

    private Action(boolean highLight, int iconResID, int iconWhiteResID, int textResID, int disableTextResID, boolean hasProgress) {
        this.highLight = highLight;
        this.id = ordinal();
        this.iconResID = iconResID;
        this.iconWhiteResID = iconWhiteResID;
        this.textResID = textResID;
        this.disableTextResID = disableTextResID;
        this.hasProgress = hasProgress;
    }

    public boolean equalAction(Action other) {
        return other != null && this.id == other.id;
    }

    public static Action getAction(int actionID) {
        if (values().length >= actionID) {
            return values()[actionID];
        }
        Action action = NONE;
        GalleryLog.e("Action", "Illegal action id.");
        return action;
    }
}
