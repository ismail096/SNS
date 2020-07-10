package ma.snrt.news.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.ImageView;

public class RoundRectCornerImageView extends ImageView {

    private float radius = 18.0f;
    private Path path;
    private RectF rect;

    public RoundRectCornerImageView(Context context) {
        super(context);
        init();
    }

    public RoundRectCornerImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RoundRectCornerImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        path = new Path();
    }

    @Override
    protected void onDraw(Canvas canvas) {

        float[] corners = new float[]{
                radius, radius,        // Top left radius in px
                radius, radius,        // Top right radius in px
                0, 0,          // Bottom right radius in px
                0, 0           // Bottom left radius in px
        };

       
        rect = new RectF(0, 0, this.getWidth(), this.getHeight());
       // path.addRoundRect(rect, radius, radius, Path.Direction.CW);
        path.addRoundRect(rect, corners, Path.Direction.CW);

       // path.addRoundRect(new RectF(0, 0, 0, 0), radius, radius, Path.Direction.CW);
        canvas.clipPath(path);


        super.onDraw(canvas);
    }
}