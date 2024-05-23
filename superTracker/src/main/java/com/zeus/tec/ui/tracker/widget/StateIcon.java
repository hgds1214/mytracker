package com.zeus.tec.ui.tracker.widget;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import com.wang.avi.AVLoadingIndicatorView;
import com.zeus.tec.R;
/**
 * TODO: document your custom view class.
 */
@SuppressLint("AppCompatCustomView")
public class StateIcon extends FrameLayout {
    private ImageView iv;
    private AVLoadingIndicatorView avi;
    public StateIcon(Context context) {
        super(context);
        init(null, 0);
    }

    public StateIcon(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public StateIcon(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        LayoutInflater.from(getContext()).inflate(R.layout.layout_state_icon, this);
        iv = findViewById(R.id.iv_icon);
        avi = findViewById(R.id.avi);
        setState(0);
    }
    private void init(AttributeSet attrs, int defStyle) {

    }

    public int getState() {
        return state;
    }

    private int state;
    public void setState(int state) {
        this.state = state;
        avi.setVisibility(View.GONE);
        iv.setVisibility(View.VISIBLE);
        switch (state) {
            case 1:
                avi.setVisibility(View.VISIBLE);
                iv.setVisibility(View.GONE);
                break;
            case 2:
                iv.setImageResource(R.mipmap.check_circle_active);
                break;
            default:
                iv.setImageResource(R.mipmap.check_circle_disable);
                break;
        }
    }

}