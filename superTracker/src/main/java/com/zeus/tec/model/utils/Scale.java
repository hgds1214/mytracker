package com.zeus.tec.model.utils;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.fonts.Font;

public class Scale {
    public int background = Color.argb(255,192,192,192);//当前颜色
    public boolean orientation = false;// true 为垂直 false为水平
    public float scaleMax = 1000;
    public float scaleMin = 0;
    public boolean setverticallocation = true;//true为右 false为左
    public float setscaleLength = 7;
    public float sacleinterval = 10;
    public int textToScaleDistance = 2;
   // Pen pen = new Pen(Color.Black, 1);
    Paint pen = new Paint();
    Paint Tenpen = new Paint();
    Paint Fivepen = new Paint();

  //  Pen Tenpen = new Pen(Color.Red, 2);
  //  Pen Fivepen = new Pen(Color.Blue, 1);
    public int scaleTenColor = Color.RED;
    public int scaleFiveColor = Color.BLUE;
    public int scaleTenLength = 15;
    public int scaleFiveLength = 10;
    Paint backgroundPaint = new Paint();
   // Brush Brush = new SolidBrush(Color.Black);
    size Size = new size();
    /// <summary>
    ///
    /// </summary>
    /// <param name="width">宽度</param>
    /// <param name="height">高度</param>
    /// <param name="background">背景颜色</param>
    /// <param name="orientation">方向（true 为垂直 false为水平）</param>
    /// <param name="scaleMax">刻度最大值</param>
    /// <param name="scalemin">刻度最小值</param>
    /// <param name="setverticallocation">刻度方向（true为右 false为左）</param>
    /// <param name="setscaleinterval">刻度长度</param>
    /// <param name="pen">刻度颜色</param>
    /// <param name="Tenpen">十位刻度颜色</param>
    /// <param name="Fivepen">五位刻度颜色</param>
    /// <param name="scaleTenLength">十位刻度长度</param>
    /// <param name="scaleFiveLength">五位刻度长度</param>

    /**
     *
     * @param width:宽度
     * @param height :高度
     * @param background:背景颜色
     * @param orientation:方向（true 为垂直 false为水平）
     * @param scaleMax:刻度最大值
     * @param scalemin:刻度最小值
     * @param setverticallocation:刻度方向（true为右 false为左）
     * @param setscaleinterval:刻度长度
     * @param penColor:刻度颜色
     * @param scaleTenColor:十位刻度颜色
     * @param scaleFiveColor:五位刻度颜色
     * @param scaleTenLength:十位刻度长度
     * @param scaleFiveLength1:五位刻度长度
     */
    public Scale(float width ,float height, int background,boolean orientation,float scaleMax,float scalemin,boolean setverticallocation, float setscaleinterval,int penColor,int scaleTenColor, int scaleFiveColor, int scaleTenLength,int scaleFiveLength1)
    {
        Size.Width = width;
        Size.Height = height;
        this.background = background;
        this.orientation = orientation;
        this.scaleMax = scaleMax;
        this.scaleMin = scalemin;
        this.setverticallocation = setverticallocation;
        this.sacleinterval = setscaleinterval;
        this.scaleTenColor = scaleTenColor;
        this.scaleTenLength = scaleTenLength;
        this.scaleFiveLength = scaleFiveLength1;
        Tenpen.setColor(Color.RED);
        Fivepen.setColor(Color.BLUE);
        pen.setColor(penColor);
        pen.setAntiAlias(true);
      //  pen.setTypeface()
        backgroundPaint.setColor(background);

    }



    public Scale(float width,float height)
    {
        Size.Width = width;
        Size.Height = height;
    }
    public Scale()
    {
    }
    public class size
    {
        public float Height;
        public float Width;
    }
    public void drawScale(Canvas g, int locationX, int locationY, float width, float height)
    {
        Size.Width = width;
        Size.Height = height;
        g.drawRect( locationX, locationY, Size.Width,Size.Height,backgroundPaint);
        float count = ((scaleMax - scaleMin) / sacleinterval);
        if (orientation)//垂直
        {
            float interval = (Size.Height / count);
            count++;
            if (setverticallocation)
            {
                for (int i = 0; i < (int)count; i++)
                {
                    float Y = i * interval + locationY;

                    if ((i % 5) == 0)
                    {
                        if ((i % 10) == 0)
                        {

                            if (i == 0)
                            {
                                Font font ;
                                g.drawLine( Size.Width,Y, Size.Width - scaleTenLength,Y,Tenpen);
                                g.drawText(String.valueOf(i * sacleinterval),  Size.Width - scaleTenLength - ((String.valueOf(i * sacleinterval).length() - 1)),Y,pen);
                            }
                            else if (i == count - 1)
                            {
                                g.drawLine(Size.Width, Y, Size.Width - scaleTenLength, Y,Tenpen);
                                g.drawText(String.valueOf(i * sacleinterval), Size.Width - scaleTenLength - ( (String.valueOf(i * sacleinterval).length() - 1)),Y  - 2,pen);
                            }
                            else
                            {
                                g.drawLine(Size.Width, Y, Size.Width - scaleTenLength, Y,Tenpen);
                                g.drawText(String.valueOf(i * sacleinterval),Size.Width - scaleTenLength - ((String.valueOf(i * sacleinterval).length() -1)), Y,pen );
                            }

                        }
                        else
                        {
                            g.drawLine( Size.Width, Y, Size.Width - scaleFiveLength, Y,Fivepen);
                        }
                    }
                    else
                    {
                        g.drawLine( Size.Width, Y, Size.Width - setscaleLength, Y,pen);
                    }
                }
            }
            else
            {
                for (int i = 0; i < (int)count; i++)
                {
                    float Y = i * interval + locationY;
                    if ((i % 5) == 0)
                    {
                        if ((i % 10) == 0)
                        {

                            if (i == 0)
                            {
                                g.drawLine(0, Y , scaleTenLength, Y,Tenpen);
                                g.drawText(String.valueOf(i * sacleinterval), scaleTenLength + 2, Y,pen);
                            }
                            else if (i == count - 1)
                            {
                                g.drawLine( 0, Y, scaleTenLength, Y,Tenpen);
                                g.drawText(String.valueOf(i * sacleinterval), scaleTenLength + 2, Y- 2+locationY,pen);
                            }
                            else
                            {
                                g.drawLine( 0, Y, scaleTenLength, Y,Tenpen);
                                g.drawText(String.valueOf(i * sacleinterval), scaleTenLength + 2, Y - 6,pen);
                            }

                        }
                        else
                        {
                            g.drawLine( 0, Y, scaleFiveLength, Y,Fivepen);
                        }
                    }
                    else
                    {
                        g.drawLine(0, Y, setscaleLength, Y,pen);
                    }
                }
            }
        }
        else//水平
        {
            float interval = (Size.Width / count);
            count++;
            if (setverticallocation)//下侧
            {
                for (int i = 0; i < (int)count; i++)
                {
                    float X = i * interval + locationX;
                    if ((i % 5) == 0)
                    {
                        if ((i % 10) == 0)
                        {
                            if (i == 0)//第一个刻度
                            {
                                g.drawLine( X, Size.Height, X, Size.Height - scaleTenLength,Tenpen);
                                g.drawText(String.valueOf(i * sacleinterval),  X,  Size.Height - scaleTenLength-textToScaleDistance,pen);
                            }
                            else if (i == count - 1)//最后一个刻度
                            {
                                g.drawLine(X, Size.Height, X, Size.Height - scaleTenLength,Tenpen);
                                g.drawText(String.valueOf(i * sacleinterval),X-15 - ( (String.valueOf(i * sacleinterval).length())), Size.Height - scaleTenLength-textToScaleDistance ,pen);
                            }
                            else//中间10刻度
                            {
                                g.drawLine(X, Size.Height,X, Size.Height - scaleTenLength,Tenpen);
                                g.drawText(String.valueOf(i * sacleinterval), X - 8, Size.Height - scaleTenLength-textToScaleDistance,pen);
                            }
                        }
                        else//5刻度
                        {
                            g.drawLine(X, Size.Height, X, Size.Height - scaleFiveLength,Fivepen);
                        }
                    }
                    else//单位刻度
                    {
                        g.drawLine( X,Size.Height,X, Size.Height - setscaleLength,pen);
                    }
                }
            }
            else//上侧
            {
                for (int i = 0; i < (int)count; i++)
                {
                    float X = i * interval + locationX;
                    if ((i % 5) == 0)
                    {
                        if ((i % 10) == 0)
                        {
                            if (i == 0)//第一个刻度
                            {
                                g.drawLine( X, 0,X, scaleTenLength,Tenpen);
                                g.drawText(String.valueOf(i * sacleinterval),X, scaleTenLength ,pen);
                            }
                            else if (i == count - 1)//最后刻度
                            {
                                g.drawLine(X, 0, X, scaleTenLength,Tenpen);
                                g.drawText(String.valueOf(i * sacleinterval), X - ((String.valueOf(i * sacleinterval).length() - 1)), scaleTenLength ,pen);
                            }
                            else//中间10刻度
                            {
                                g.drawLine(X, 0, X, scaleTenLength,Tenpen);
                               g.drawText(String.valueOf(i * sacleinterval), X - 8, scaleTenLength ,pen);
                            }
                        }
                        else//5刻度
                        {
                            g.drawLine( X, 0, X, scaleFiveLength,Fivepen);
                        }
                    }
                    else//单位刻度
                    {
                        g.drawLine(X, 0, X, setscaleLength,pen);
                    }
                }
            }
        }
    }
}
