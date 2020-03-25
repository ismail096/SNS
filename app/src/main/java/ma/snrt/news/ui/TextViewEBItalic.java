package ma.snrt.news.ui;

import android.content.Context;
import android.graphics.Color;
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
            if(AppController.getSharedPreferences().getString("lang", "").equals("ar")) {
                Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/ArbFONTS-DroidKufi-Bold.ttf");
                setTypeface(tf);
            }
            else {
                Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/Averta-ExtraBoldItalic.otf");
                setTypeface(tf);
            }
            setIncludeFontPadding(false);
        }
    }
}
