package ma.snrt.news.ui;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import ma.snrt.news.AppController;

public class TextViewSemiBold extends TextView {

    public TextViewSemiBold(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public TextViewSemiBold(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TextViewSemiBold(Context context) {
        super(context);
        init();
    }

    private void init() {
        if (!isInEditMode()) {
            Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/Averta-Semibold.otf");
            if(AppController.getSharedPreferences().getString("lang", "").equals("ar")) {
                tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/ArbFONTS-DroidKufi-Bold.ttf");
                setIncludeFontPadding(false);
            }
            setTypeface(tf);
        }
    }
}
