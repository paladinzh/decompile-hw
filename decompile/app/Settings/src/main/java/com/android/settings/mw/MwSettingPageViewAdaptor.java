package com.android.settings.mw;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MwSettingPageViewAdaptor extends PagerAdapter {
    private List<String> lHeaderList = new ArrayList();
    private Context mContext;
    private int[] mDrawable = new int[]{2130838549, 2130838550, 2130838551, 2130838552};

    public MwSettingPageViewAdaptor(Context aContext) {
        this.mContext = aContext;
        init();
    }

    private void init() {
        this.lHeaderList.add(this.mContext.getResources().getString(2131627929));
        this.lHeaderList.add(this.mContext.getResources().getString(2131627930));
        this.lHeaderList.add(this.mContext.getResources().getString(2131627931));
        this.lHeaderList.add(this.mContext.getResources().getString(2131627932));
    }

    public int getCount() {
        return 4;
    }

    public boolean isViewFromObject(View aPageView, Object aObject) {
        return aPageView == ((RelativeLayout) aObject);
    }

    public Object instantiateItem(ViewGroup aContainer, int aPosition) {
        View aView = ((Activity) this.mContext).getLayoutInflater().inflate(2130968875, null);
        ImageView lDesc = (ImageView) aView.findViewById(2131886811);
        TextView lTitle = (TextView) aView.findViewById(2131886809);
        BitmapDrawable lbmd = decodeImageRes(this.mDrawable[aPosition]);
        if (lbmd != null) {
            lDesc.setImageDrawable(lbmd);
        }
        lTitle.setText((CharSequence) this.lHeaderList.get(aPosition));
        ((ViewPager) aContainer).addView(aView);
        return aView;
    }

    public void destroyItem(View container, int position, Object object) {
        BitmapDrawable lBitmapDrawable = (BitmapDrawable) ((ImageView) ((View) object).findViewById(2131886811)).getDrawable();
        if (!(lBitmapDrawable == null || lBitmapDrawable.getBitmap() == null)) {
            recycleBitmap(lBitmapDrawable.getBitmap());
        }
        ((ViewPager) container).removeView((View) object);
    }

    private BitmapDrawable decodeImageRes(int aId) {
        InputStream inputStream = null;
        try {
            inputStream = this.mContext.getResources().openRawResource(aId);
            Options lOptions = new Options();
            lOptions.inJustDecodeBounds = false;
            Bitmap lBitmap = BitmapFactory.decodeStream(inputStream, null, lOptions);
            if (lBitmap != null) {
                BitmapDrawable lbmd = new BitmapDrawable(this.mContext.getResources(), lBitmap);
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        Log.e("MwSettingPageViewAdaptor", "Exception while closing the InputStream");
                    }
                }
                return lbmd;
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e2) {
                    Log.e("MwSettingPageViewAdaptor", "Exception while closing the InputStream");
                }
            }
            return null;
        } catch (Throwable th) {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e3) {
                    Log.e("MwSettingPageViewAdaptor", "Exception while closing the InputStream");
                }
            }
        }
    }

    private void recycleBitmap(Bitmap aBitmap) {
        if (aBitmap != null) {
            try {
                if (!aBitmap.isRecycled()) {
                    aBitmap.recycle();
                }
            } catch (Exception e) {
                Log.e("MwSettingPageViewAdaptor", "Exception while recycling the bitmap");
            }
        }
    }
}
