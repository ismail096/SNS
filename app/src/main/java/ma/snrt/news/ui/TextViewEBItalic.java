package ma.snrt.news.ui;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.TextView;

import ma.snrt.news.AppController;
import ma.snrt.news.util.Utils;

public class TextViewEBItalic extends TextView {

    public TextViewEBItalic(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public TextViewEBItalic(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TextViewEBItalic(Context context) {
        super(context);
        init();
    }

    private void init() {
        if (!isInEditMode()) {
            Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/Averta-ExtraBoldItalic.otf");
            if(AppController.getSharedPreferences().getString("lang", "").equals("ar")) {
                tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/ApercuArabicPro-Bold.otf");
                setIncludeFontPadding(false);
                //setTextSize(TypedValue.COMPLEX_UNIT_PX, getTextSize() + Utils.spToPx(getResources(), 2));
            }
            setTypeface(tf);
            //setClickable(true);
        }
    }
}
