package com.zeus.tec.model.utils;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Choreographer;
import android.widget.TextView;

import com.blankj.utilcode.util.ReflectUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

public class MarqueeTextView extends androidx.appcompat.widget.AppCompatTextView{
    private Choreographer.FrameCallback mRealRestartCallbackObj;
    private Choreographer.FrameCallback mFakeRestartCallback;
    private OnShowTextListener mOnShowTextListener;

    public MarqueeTextView(Context context, OnShowTextListener onShowTextListener) {
        super(context);
        initView(context);
        this.mOnShowTextListener = onShowTextListener;

    }

    public MarqueeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public MarqueeTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }


    private void initView(Context context) {
        //绕过隐藏api的限制
      //  context.getApplicationContext();

     //   ReflectUtils.unseal(context.getApplicationContext());
     //   ReflectUtils.

        //设置跑马灯生效条件
        this.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        this.setSingleLine(true);
        this.setFocusable(true);


        //反射设置跑马灯监听
        try {
            //从TextView类中找到定义的字段mMarquee
            Field marqueeField =  ReflectUtils.reflect(TextView.class).field("mMarquee").get();
           // Field marqueeField = ReflectUtil.getDeclaredField(TextView.class, "mMarquee");
            //获取Marquee类的构造方法Marquee(TextView v)
            Constructor declaredConstructor = ReflectUtils.reflect(Class.forName("android.widget.TextView$Marquee").getDeclaredConstructor(TextView.class)).get();

           // Constructor declaredConstructor = ReflectUtil.getDeclaredConstructor(Class.forName("android.widget.TextView$Marquee"), TextView.class);
            //实例化一个Marquee对象，传入参数是Textview对象
            Object marqueeObj = declaredConstructor.newInstance(this);
            //从Marquee类中找到定义的字段mRestartCallback，重新开始一轮跑马灯时候会回调到这个对象doFrame（）方法
            Field restartCallbackField = ReflectUtils.reflect(Class.forName("android.widget.TextView$Marquee")).field("mRestartCallback").get();
           // Field restartCallbackField = ReflectUtil.getDeclaredField(Class.forName("android.widget.TextView$Marquee"), "mRestartCallback");
            //从Marquee实例对象中获取到真实的mRestartCallback对象
            mRealRestartCallbackObj = (Choreographer.FrameCallback) restartCallbackField.get(marqueeObj);
            //构造一个假的mRestartCallback对象，用来监听什么时候跑完一轮跑马灯效果
            mFakeRestartCallback = new Choreographer.FrameCallback() {
                @Override
                public void doFrame(long frameTimeNanos) {
                    //这里还是执行真实的mRestartCallback对象的代码逻辑
                    mRealRestartCallbackObj.doFrame(frameTimeNanos);
                    Log.i("min77","跑马灯文本显示完毕");
                    MarqueeTextView.this.setVisibility(GONE);
                    //回调通知跑完一轮
                    if(MarqueeTextView.this.mOnShowTextListener != null){
                        MarqueeTextView.this.mOnShowTextListener.onComplete(10000);
                    }

                }
            };
            //把假的mRestartCallback对象设置给Marquee对象，其实就是代理模式
            restartCallbackField.set(marqueeObj, mFakeRestartCallback);
            //把自己实例化的Marquee对象设置给Textview
            marqueeField.set(this, marqueeObj);


        } catch (Exception e) {
            e.printStackTrace();
            Log.e("min77",e.getMessage());
        }
    }



    //最关键的部分
    public boolean isFocused() {
        return true;
    }

    /**
     * 是否显示完整文本
     */
    public interface OnShowTextListener{
        void onComplete(int delayMillisecond);

    }

}
