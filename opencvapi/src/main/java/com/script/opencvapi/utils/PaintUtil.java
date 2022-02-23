package com.script.opencvapi.utils;

import android.content.Context;
import android.graphics.*;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;

/**
 * Created by     on 2018/3/29.
 */
public class PaintUtil extends View implements View.OnClickListener {
    private static final int MIN_WIDTH =  175 ;
    TopWindow topWindow ;
    private int x, y, w, h ;
    private int rect_x ;
    private int rect_y ;
    private int text_x ;
    private int text_y ;
    private int sim  ;
    private int timestamp ;
    private Context mContext ;
    private Handler mHandler ;

    public PaintUtil(Context context){
        super(context);
        topWindow = new TopWindow(context) ;
        mHandler = new Handler() ;
        mContext = context ;
        setOnClickListener(this);
    }
    public void drawRect(final int x, final int y, int w, int h, int sim, int timestamp, int showTime){
        this.x = x ;
        this.y = y ;
        this.w = w ;
        this.h = h ;
        this.sim = sim ;
        this.timestamp = timestamp ;

        text_x = 0 ;
        rect_x = 0 ;
        rect_y = 0 ;
        if(y + h > getScreenHeight()){
            text_y = h - 10 ;//文字显示在方框内部
        }else{
            text_y = h + 15 ;//文字显示在方框下方
        }

        if( w < MIN_WIDTH){
            topWindow.setSize(MIN_WIDTH,h+50);
        }else{
            topWindow.setSize(w,h+50);
        }
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                topWindow.show(x,y,PaintUtil.this);
                invalidate();
            }
        });
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                topWindow.hide();
            }
        },showTime) ;
    }
    private int getScreenWidth(){
        WindowManager windowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);

        DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getRealMetrics(metrics);
        return metrics.widthPixels ;
    }
    private int getScreenHeight(){
        WindowManager windowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);

        DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getRealMetrics(metrics);
        return metrics.heightPixels ;
    }
    public void onDraw(Canvas canvas){
        Paint paint = new Paint();
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        canvas.drawPaint(paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
        paint.setColor(Color.RED);

        Rect rect = new Rect(rect_x,rect_y,w,h);
        paint.setStyle(Paint.Style.STROKE);
        float strokeWidth = 0.5f+ sim*1.0f/50 ;
        paint.setStrokeWidth(strokeWidth);
        canvas.drawRect(rect, paint);


        String text = "相似度:"+sim*1.0f/100 +" 耗时:"+timestamp+"毫秒";
        paint.setStrokeWidth(1.0f);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(15);
        canvas.drawText(text,text_x,text_y,paint);

    }

    @Override
    public void onClick(View view) {
        topWindow.hide();
    }
}
