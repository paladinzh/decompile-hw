package com.android.contacts.activities;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.Loader;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.contacts.ContactDpiAdapter;
import com.android.contacts.ContactPhotoManager;
import com.android.contacts.ContactSaveService;
import com.android.contacts.ContactSaveService.Listener;
import com.android.contacts.ContactSplitUtils;
import com.android.contacts.ContactsApplication;
import com.android.contacts.detail.ContactDetailDisplayUtils;
import com.android.contacts.detail.ContactDetailHelper;
import com.android.contacts.detail.ContactDetailPhotoSetter;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.hap.util.AlertDialogFragmet;
import com.android.contacts.hap.utils.ScreenUtils;
import com.android.contacts.model.Contact;
import com.android.contacts.model.ContactLoader;
import com.android.contacts.profile.ProfileUtils;
import com.android.contacts.profile.ProfileUtils.ContactEntriesObject;
import com.android.contacts.profile.ProfileUtils.HalfType;
import com.android.contacts.profile.ProfileUtils.ProfileListener;
import com.android.contacts.statistical.StatisticalHelper;
import com.android.contacts.util.HwLog;
import com.google.android.gms.R;
import com.huawei.zxing.WriterException;
import com.huawei.zxing.encode.QRCodeEncoder;
import java.io.File;
import java.util.List;

public class ProfileSimpleCardActivity extends Activity implements Listener, ProfileListener {
    private static final String TAG = ProfileSimpleCardActivity.class.getName();
    int color;
    private boolean fromEditor;
    private boolean fromEditorCreate;
    private boolean hasPhotoChange;
    private boolean isRequestIDSame;
    private View mCardView;
    private Contact mContactData;
    private TextView mEmptyView;
    private ContactEntriesObject mEntriesObject;
    public Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 257:
                    ProfileSimpleCardActivity.this.freshProfileUI();
                    break;
                case 258:
                    ProfileSimpleCardActivity.this.bindQRCodeGenerat();
                    break;
                case 259:
                    ProfileSimpleCardActivity.this.bindQRCodeGeneratFail();
                    break;
            }
            super.handleMessage(msg);
        }
    };
    private ContactLoader mLoader = null;
    private Uri mLookupUri;
    private final ContactDetailPhotoSetter mPhotoSetter = new ContactDetailPhotoSetter();
    private ImageView mPhotoView;
    LoaderCallbacks<Contact> mProfileLoadedListener = new LoaderCallbacks<Contact>() {
        public Loader<Contact> onCreateLoader(int id, Bundle args) {
            ProfileSimpleCardActivity.this.mLoader = new ContactLoader(ProfileSimpleCardActivity.this.getApplicationContext(), (Uri) args.getParcelable("contactUri"), false, false, true, true);
            ProfileSimpleCardActivity.this.mLoader.setContactLoadedListener(null);
            return ProfileSimpleCardActivity.this.mLoader;
        }

        public void onLoadFinished(Loader<Contact> loader, Contact data) {
            ProfileSimpleCardActivity.this.afterProfileDataLoaded(data);
        }

        public void onLoaderReset(Loader<Contact> loader) {
        }
    };
    private ImageView mQRCodeImage;
    private Bitmap mQrCodeBitmap;
    int mScreenHeight;
    int mScreenWidth;
    private ImageView mSmallPhotoView;
    int marginBottom;
    int marginStart;
    boolean portrait;
    int roundPixels;
    int shadowWidth;
    private OnClickListener shareItemSelectListener = new OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case 0:
                    ProfileSimpleCardActivity.this.shareBusinessCard();
                    return;
                case 1:
                    ProfileSimpleCardActivity.this.shareVcard();
                    return;
                case 2:
                    ProfileSimpleCardActivity.this.shareTextCard();
                    return;
                default:
                    return;
            }
        }
    };
    private MenuItem shareMenu;
    int targetHeight;
    int targetWidth;

    protected void onCreate(Bundle savedInstanceState) {
        if (RequestPermissionsActivity.startPermissionActivity(this)) {
            finish();
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_simple_card_activity);
        getIntentDate();
        if (getActionBar() != null) {
            getActionBar().setTitle("");
            getActionBar().setDisplayOptions(32768);
        }
        init();
        if (this.fromEditor || this.fromEditorCreate) {
            Contact contact = getContactFromEditor(getIntent());
            if (contact != null) {
                this.mContactData = contact;
                afterProfileDataLoaded(contact);
            }
        }
        if (this.isRequestIDSame && this.fromEditorCreate) {
            saveContactFromEditIntent(getIntent());
        }
        startLoader();
        ContactInfoFragment.setProfileListener(this);
        if (CommonUtilMethods.calcIfNeedSplitScreen()) {
            double sizeInch = ContactSplitUtils.calculateDeviceSize(this);
            if (HwLog.HWDBG) {
                HwLog.i(TAG, "onCreate sizeInch:" + sizeInch);
            }
            if (sizeInch >= 8.0d || 8.0d - sizeInch <= 0.1d) {
                int splitMarginStart = getResources().getDimensionPixelSize(R.dimen.profile_simple_card_split_margin_start);
                int splitMarginBottom = getResources().getDimensionPixelSize(R.dimen.profile_simple_card_split_margin_bottom);
                if (splitMarginStart != 0) {
                    this.marginStart = splitMarginStart;
                } else {
                    this.marginStart = this.mScreenWidth / 8;
                }
                if (!this.portrait) {
                    if (splitMarginBottom == 0) {
                        this.marginBottom = this.mScreenHeight / 8;
                    } else {
                        this.marginBottom = splitMarginBottom;
                    }
                    this.mScreenHeight -= this.marginBottom;
                } else if (splitMarginBottom == 0) {
                    this.marginBottom = this.mScreenHeight / 12;
                } else {
                    this.marginBottom = splitMarginBottom;
                }
                ScreenUtils.updateViewMarrginValue(this, this.mCardView, this.marginStart, this.marginBottom, this.marginStart, this.marginBottom);
            }
        }
    }

    private void saveContactFromEditIntent(Intent intent) {
        if (intent != null) {
            Intent lServiceIntent = (Intent) intent.getParcelableExtra("serviceIntent");
            if (lServiceIntent != null && "saveContact".equals(lServiceIntent.getAction())) {
                lServiceIntent.setClass(this, ContactSaveService.class);
                startService(lServiceIntent);
            }
        }
    }

    private Contact getContactFromEditor(Intent aData) {
        if (aData == null) {
            return null;
        }
        long requestid = aData.getLongExtra("requestid", -1);
        this.isRequestIDSame = ((ContactsApplication) getApplication()).isRequestIDSame(requestid);
        return ((ContactsApplication) getApplication()).getContactAndReset(requestid);
    }

    private void init() {
        int i;
        this.mPhotoView = (ImageView) findViewById(R.id.photo);
        this.mSmallPhotoView = (ImageView) findViewById(R.id.small_photo);
        this.mQRCodeImage = (ImageView) findViewById(R.id.qrcode);
        this.mEmptyView = (TextView) findViewById(16908292);
        this.mCardView = findViewById(R.id.card);
        this.roundPixels = getResources().getDimensionPixelSize(R.dimen.profile_simple_card_bg_corner);
        this.shadowWidth = getResources().getDimensionPixelSize(R.dimen.profile_simple_card_margin_total_shadow_width);
        this.color = getColor(R.color.profile_simple_card_default_bg_color);
        this.portrait = getResources().getConfiguration().orientation == 1;
        Resources resources = getResources();
        if (isInMultiWindowMode()) {
            i = R.dimen.detail_item_label_left_multiwindon_margin;
        } else {
            i = R.dimen.profile_simple_card_margin_bottom;
        }
        this.marginBottom = resources.getDimensionPixelSize(i);
        this.marginStart = getResources().getDimensionPixelSize(R.dimen.profile_simple_card_margin_start);
        this.mScreenHeight = getResources().getDisplayMetrics().heightPixels;
        this.mScreenWidth = getResources().getDisplayMetrics().widthPixels;
        if (isInMultiWindowMode()) {
            LayoutParams lp = (LayoutParams) this.mCardView.getLayoutParams();
            lp.setMargins(lp.leftMargin, lp.topMargin, lp.rightMargin, this.marginBottom);
            this.mCardView.setLayoutParams(lp);
            View profileContainer = findViewById(R.id.profile_container);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) profileContainer.getLayoutParams();
            params.setMargins(params.leftMargin, params.topMargin, params.rightMargin, 0);
            profileContainer.setLayoutParams(params);
        }
    }

    private void getIntentDate() {
        Intent intent = getIntent();
        if (intent != null) {
            this.mLookupUri = intent.getData();
            this.fromEditorCreate = intent.getBooleanExtra("from_list_profile_create", false);
            this.fromEditor = intent.getBooleanExtra("from_profile_editor", false);
            this.hasPhotoChange = intent.getBooleanExtra("intent_key_has_photo", false);
        }
        if (this.mLookupUri == null) {
            this.mLookupUri = ProfileUtils.getProfileLookupUri(getApplicationContext());
        }
    }

    private void startLoader() {
        if (this.mLookupUri != null || this.fromEditorCreate) {
            if (this.mLookupUri != null) {
                Bundle args = new Bundle();
                args.putParcelable("contactUri", this.mLookupUri);
                getLoaderManager().initLoader(1, args, this.mProfileLoadedListener);
            }
            return;
        }
        finish();
    }

    protected void afterProfileDataLoaded(Contact data) {
        if (data.isNotFound() || data.isError()) {
            finish();
            return;
        }
        if (data.isLoaded() && data.isUserProfile()) {
            this.mContactData = data;
            this.mEntriesObject = ProfileUtils.buildContactData(data, getApplicationContext());
            if (this.mEntriesObject == null) {
                finish();
                return;
            }
            this.mHandler.sendEmptyMessage(257);
        }
    }

    protected void onStart() {
        super.onStart();
        ContactSaveService.registerListener(this);
    }

    protected void onResume() {
        super.onResume();
    }

    protected void onDestroy() {
        super.onDestroy();
        ContactSaveService.unregisterListener(this);
        File file = new File(getExternalCacheDir(), "profile.jpg");
        if (file.exists()) {
            boolean deleted = file.delete();
            if (HwLog.HWDBG) {
                HwLog.i(TAG, "file delete :" + deleted);
            }
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        boolean z;
        getMenuInflater().inflate(R.menu.profile_simple_card_menu, menu);
        this.shareMenu = menu.findItem(R.id.menu_share_contact);
        MenuItem menuItem = this.shareMenu;
        if (EmuiFeatureManager.isSuperSaverMode()) {
            z = false;
        } else {
            z = true;
        }
        menuItem.setEnabled(z);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_edit_contact:
                editProfileContact();
                return true;
            case R.id.menu_share_contact:
                shareMe();
                return true;
            case R.id.menu_view_contact:
                viewProfileContact();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void shareMe() {
        AlertDialogFragmet.show(getFragmentManager(), (int) R.string.profile_dialog_share_contacts, (int) R.array.share_profile_select_items, Boolean.valueOf(true), this.shareItemSelectListener, 3);
    }

    protected void shareVcard() {
        ContactDetailHelper.shareContact(this.mContactData, getApplicationContext());
        StatisticalHelper.report(2037);
    }

    private void shareBusinessCard() {
        Uri uri = getBusinessCardUri();
        Intent intent = new Intent("android.intent.action.SEND");
        intent.setType("image/*");
        intent.putExtra("android.intent.extra.STREAM", uri);
        intent.addFlags(3);
        startActivityForResult(Intent.createChooser(intent, getResources().getString(R.string.profile_dialog_share_contacts)), 101);
        StatisticalHelper.report(2035);
    }

    private Uri getBusinessCardUri() {
        return ProfileUtils.getBitmapFileUri(getApplicationContext(), ProfileUtils.covertViewToBitmap(this.mCardView), Boolean.valueOf(true));
    }

    private void shareTextCard() {
        String textCardString = ProfileUtils.buildShareTextCard(this.mEntriesObject);
        Intent intent = new Intent("android.intent.action.SEND");
        intent.setType("text/plain");
        intent.putExtra("android.intent.extra.TEXT", textCardString);
        intent.addHwFlags(16);
        startActivity(Intent.createChooser(intent, getApplicationContext().getString(R.string.profile_dialog_share_contacts)));
        StatisticalHelper.report(2036);
    }

    private void viewProfileContact() {
        Intent intent = new Intent("android.intent.action.VIEW", this.mLookupUri);
        intent.putExtra("from_profile_card", true);
        intent.setClass(getApplicationContext(), ContactDetailActivity.class);
        intent.setFlags(67108864);
        startActivity(intent);
    }

    private void editProfileContact() {
        Intent intent = new Intent("android.intent.action.EDIT", this.mLookupUri);
        intent.setClass(getApplicationContext(), ContactEditorActivity.class);
        intent.putExtra("finishActivityOnSaveCompleted", true);
        intent.putExtra("isFromDetailActivity", true);
        startActivityForResult(intent, 102);
        StatisticalHelper.report(2039);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (-1 != resultCode) {
            HwLog.e(TAG, "onActivityResult result not ok for aResultCode:" + resultCode);
            return;
        }
        switch (requestCode) {
            case 101:
                if (HwLog.HWFLOW) {
                    HwLog.i(TAG, "profile share onActivityResult, do nothing.");
                    break;
                }
                break;
            case 102:
                if (HwLog.HWFLOW) {
                    HwLog.i(TAG, "profile editor onActivityResult, do nothing.");
                    break;
                }
                break;
        }
    }

    private void getQRCodeBitmap(final Bundle bundle) {
        Thread thread = new Thread(new Runnable() {
            public void run() {
                ProfileSimpleCardActivity.this.generatQrBitmap(bundle);
            }
        });
        thread.setName("Profile QRCode Thread");
        thread.start();
    }

    private void generatQrBitmap(Bundle bundle) {
        try {
            if (this.mContactData == null) {
                this.mHandler.sendEmptyMessage(259);
                return;
            }
            Bitmap QrCodeBitmap = new QRCodeEncoder(this).encodeQRCodeContents(bundle, null, "CONTACT_TYPE", null);
            int width = getResources().getDimensionPixelSize(R.dimen.profile_simple_card_qrcode_size);
            this.mQrCodeBitmap = ProfileUtils.colorBitmap(QrCodeBitmap, width, width, 16.0f, -16776961);
            if (!(QrCodeBitmap == null || this.mQrCodeBitmap == QrCodeBitmap)) {
                QrCodeBitmap.recycle();
            }
            this.mHandler.sendEmptyMessage(258);
        } catch (WriterException e) {
            HwLog.e(TAG, "Can not generate QRcode bitmap !");
            this.mHandler.sendEmptyMessage(259);
        }
    }

    protected void freshProfileUI() {
        loadProfilePhoto();
        bindHeaderData(this.mContactData);
        bindContainerData(this.mEntriesObject);
        getQRCodeBitmap(ProfileUtils.buildQRCodeBundle(this.mContactData, this.mEntriesObject.qrcodeDataInfo));
    }

    private void loadProfilePhoto() {
        int i = 0;
        boolean photoLoaded = this.mContactData.mPhotoBinaryData != null;
        if (this.portrait) {
            this.targetWidth = (this.mScreenWidth - (this.marginStart * 2)) - this.shadowWidth;
            this.targetHeight = getResources().getDimensionPixelSize(R.dimen.profile_simple_card_header_height) + this.roundPixels;
            this.targetHeight = ContactDpiAdapter.getNewDpiFromDimen(this.targetHeight);
        } else {
            this.targetWidth = getResources().getDimensionPixelSize(R.dimen.profile_simple_card_header_width) + this.roundPixels;
            this.targetWidth = ContactDpiAdapter.getNewDpiFromDimen(this.targetWidth);
            this.targetHeight = (this.mScreenHeight - this.marginBottom) - this.shadowWidth;
        }
        if (photoLoaded) {
            Bitmap aSource = BitmapFactory.decodeByteArray(this.mContactData.mPhotoBinaryData, 0, this.mContactData.mPhotoBinaryData.length);
            boolean hasBigPhoto = false;
            String photoUri = this.mContactData.getPhotoUri();
            if (!(photoUri == null || "photo".equals(Uri.parse(photoUri).getLastPathSegment()))) {
                hasBigPhoto = true;
            }
            if (hasBigPhoto || !this.hasPhotoChange) {
                ImageView imageView = this.mSmallPhotoView;
                if (hasBigPhoto) {
                    i = 8;
                }
                imageView.setVisibility(i);
                if (aSource == null || !hasBigPhoto) {
                    this.mSmallPhotoView.setOnClickListener(this.mPhotoSetter.setupContactPhotoForClick(this, this.mContactData, this.mSmallPhotoView));
                    setSmallPhoto(aSource);
                } else {
                    this.mPhotoView.setOnLongClickListener(this.mPhotoSetter.setupContactPhotoForClick(this, this.mContactData, this.mPhotoView));
                    this.mPhotoView.setImageBitmap(ProfileUtils.getRoundCornerImage(ProfileUtils.cutBitmapAndScale(aSource, this.targetWidth, this.targetHeight, true, true), this.roundPixels, this.portrait ? HalfType.TOP : HalfType.LEFT));
                }
            } else {
                if (this.fromEditorCreate) {
                    setDefaultPhoto();
                }
                return;
            }
        }
        setDefaultPhoto();
    }

    private void setDefaultPhoto() {
        this.mPhotoView.setOnLongClickListener(this.mPhotoSetter.setupContactPhotoForClick(this, this.mContactData, this.mPhotoView));
        this.mSmallPhotoView.setVisibility(8);
        this.mPhotoView.setImageBitmap(getDefaultBgBmp(this.color, this.targetWidth, this.targetHeight, this.roundPixels, this.portrait ? HalfType.TOP : HalfType.LEFT));
    }

    private void setSmallPhoto(Bitmap aSource) {
        if (aSource != null && this.mSmallPhotoView != null && this.mPhotoView != null) {
            this.mSmallPhotoView.setImageDrawable(ContactPhotoManager.createRoundPhotoDrawable(new BitmapDrawable(getResources(), aSource)));
            this.mPhotoView.setImageBitmap(getDefaultBgBmp(this.color, this.targetWidth, this.targetHeight, this.roundPixels, this.portrait ? HalfType.TOP : HalfType.LEFT));
        }
    }

    private Bitmap getDefaultBgBmp(int color, int targetWidth, int targetHeight, int roundPixels, HalfType top) {
        return ProfileUtils.getRoundCornerImage(ProfileUtils.drawColorBitmap(color, targetWidth, targetHeight), roundPixels, top);
    }

    private void bindHeaderData(Contact data) {
        ContactDetailDisplayUtils.setContactDisplayInfo(this, data, (TextView) findViewById(R.id.name), (TextView) findViewById(R.id.company), this.mPhotoView, findViewById(R.id.name_container), false);
    }

    private void bindContainerData(ContactEntriesObject entries) {
        boolean visible = true;
        int i = 0;
        if (entries != null) {
            TextView phoneView = (TextView) findViewById(R.id.phone);
            TextView emailView = (TextView) findViewById(R.id.email);
            View profileContainer = findViewById(R.id.profile_container);
            if (phoneView == null || emailView == null) {
                profileContainer.setVisibility(8);
            } else {
                ProfileUtils.setProfileContainerData(phoneView, entries.phoneEntries);
                ProfileUtils.setProfileContainerData(emailView, entries.emailEntries);
                if (!(phoneView.getVisibility() == 0 || emailView.getVisibility() == 0)) {
                    visible = false;
                }
                if (!visible) {
                    i = 8;
                }
                profileContainer.setVisibility(i);
            }
        }
    }

    private void bindQRCodeGenerat() {
        boolean z = false;
        if (this.mQrCodeBitmap == null) {
            bindQRCodeGeneratFail();
            return;
        }
        if (this.mQRCodeImage != null) {
            this.mQRCodeImage.setVisibility(0);
            this.mQRCodeImage.setImageBitmap(this.mQrCodeBitmap);
            this.mQRCodeImage.setContentDescription(getString(R.string.contact_two_dimensional_code));
        }
        if (this.mEmptyView != null) {
            this.mEmptyView.setVisibility(8);
        }
        if (this.shareMenu != null) {
            MenuItem menuItem = this.shareMenu;
            if (!EmuiFeatureManager.isSuperSaverMode()) {
                z = true;
            }
            menuItem.setEnabled(z);
        }
        getBusinessCardUri();
    }

    private void bindQRCodeGeneratFail() {
        if (this.mQRCodeImage != null) {
            this.mQRCodeImage.setVisibility(8);
        }
        if (this.mEmptyView != null) {
            this.mEmptyView.setVisibility(0);
        }
        if (this.shareMenu != null) {
            this.shareMenu.setEnabled(false);
        }
    }

    public void onServiceCompleted(final Intent callbackIntent) {
        this.mHandler.postDelayed(new Runnable() {
            public void run() {
                ProfileSimpleCardActivity.this.onNewIntent(callbackIntent);
            }
        }, 100);
    }

    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (!(intent == null || this.mLookupUri == intent.getData())) {
            this.mLookupUri = intent.getData();
            startLoader();
        }
    }

    public void deleteProfile() {
        finish();
    }

    public boolean onNavigateUp() {
        List<RunningTaskInfo> taskInfoList = ((ActivityManager) getSystemService("activity")).getRunningTasks(1);
        if (taskInfoList == null || taskInfoList.size() != 1 || !((RunningTaskInfo) taskInfoList.get(0)).baseActivity.getClassName().equalsIgnoreCase(PeopleActivity.class.getName())) {
            return super.onNavigateUp();
        }
        onBackPressed();
        return true;
    }

    public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
        AlertDialogFragmet alertFragment = (AlertDialogFragmet) getFragmentManager().findFragmentByTag("AlertDialogFragmet");
        if (alertFragment != null && alertFragment.mAlertDialogType == 3) {
            alertFragment.mSetedProfileListener = this.shareItemSelectListener;
        }
        return super.onCreateView(parent, name, context, attrs);
    }
}
