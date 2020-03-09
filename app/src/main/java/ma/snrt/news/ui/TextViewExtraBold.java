package ma.snrt.news.ui;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.TextView;

import ma.snrt.news.AppController;
import ma.snrt.news.util.Utils;

public class TextViewExtraBold extends TextView {

    public TextViewExtraBold(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public TextViewExtraBold(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TextViewExtraBold(Context context) {
        super(context);
        init();
    }

    private void init() {
        if (!isInEditMode()) {
            Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/Averta-ExtraBold.otf");
            if(AppController.getSharedPreferences().getString("lang", "").equals("ar")) {
                tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/Hacen-Beirut-Poster.ttf");
                setTextSize(TypedValue.COMPLEX_UNIT_PX, getTextSize() + Utils.spToPx(getResources(), 4));
            }
            setTypeface(tf);
            //setClickable(true);
        }
    }
}
