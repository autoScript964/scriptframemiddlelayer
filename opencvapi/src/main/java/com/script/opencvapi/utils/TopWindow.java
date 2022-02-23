package com.script.opencvapi.utils;

import android.content.Context;
import android.graphics.PixelFormat;
import android.util.DisplayMetrics;
import android.view.*;

/**
 * Created by     on 15-11-26.
 */
public class TopWindow {
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mParams;
    private int mDownInScreenX;
    private int mDownInScreenY;

    private OnTopViewClickListener mListener ;
    private int mScreenWidth;
    private int mScreenHeight;
    private View mCurrentView ;
    public TopWindow(Context context){
        mWindowManager = (WindowManager) context.getApplicationContext()
                .getSystemService(Context.WINDOW_SERVICE);
        mParams = new WindowManager.LayoutParams();
        mParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        mParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;// | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
        mParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        mParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        mParams.format = PixelFormat.RGBA_8888;
        mParams.gravity = Gravity.START | Gravity.TOP;

        DisplayMetrics dm = context.getResources().getDisplayMetrics();//获取屏幕的宽高
        mScreenWidth = dm.widthPixels;
        mScreenHeight = dm.heightPixels;
    }
    public void setOnViewClickListener(OnTopViewClickListener listener){
        mListener = listener ;
    }
    public void fullScreen(){
        mParams.width = ViewGroup.LayoutParams.MATCH_PARENT ;
        mParams.height = ViewGroup.LayoutParams.MATCH_PARENT ;
    }
    public void defaultWindow(){
        mParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        mParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
    }
    public void setSize(int w, int h){
        mParams.width = w;
        mParams.height = h;

    }
    public void show(int x, int y, View view){
        mParams.x = x ;
        mParams.y = y ;
        if(mCurrentView != null){
            mWindowManager.removeView(mCurrentView);
        }
        mCurrentView = view ;
        mWindowManager.addView(view, mParams);
    }
    public void showWithMove(int x, int y, View view){
        mParams.x = x ;
        mParams.y = y ;
        if(mCurrentView != null){
            mWindowManager.removeView(mCurrentView);
        }
        mCurrentView = view ;
        mWindowManager.addView(view, mParams);



        view.setOnTouchListener(new View.OnTouchListener() {

            int[] temp = new int[]{0, 0};

            public boolean onTouch(View v, MotionEvent event) {

                int x = (int) event.getRawX();
                int y = (int) event.getRawY();
                int eventaction = event.getAction();
                switch (eventaction) {
                    case MotionEvent.ACTION_DOWN: // touch down so check if the
                        temp[0] = (int) event.getX();
                        temp[1] = y - mParams.y;
                        mDownInScreenX = (int) event.getRawX();
                        mDownInScreenY = (int) event.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE: // touch drag with the ball
                        int tY = y - temp[1];
                        if (tY > mScreenHeight / 2) {
                            tY = mScreenHeight / 2;
                        }
                        mParams.x = x - temp[0];
                        mParams.y = y - temp[1];
                        mWindowManager.updateViewLayout(v, mParams);
                        break;
                    case MotionEvent.ACTION_UP:
                        if (Math.abs(mDownInScreenX - x) <= 5 && Math.abs(mDownInScreenY - y) <= 5) {
                            if(mListener != null){
                                mListener.onClickListener(event);
                            }
                        }

                        break;
                }
                return false;
            }

        });
    }
    public void hide(){
        if(mCurrentView != null) {
            mWindowManager.removeView(mCurrentView);
        }
        mCurrentView = null ;

    }
    public void update(int x, int y){
        mParams.x = x ;
        mParams.y = y ;
        mWindowManager.updateViewLayout(mCurrentView, mParams);
    }
    public interface OnTopViewClickListener{
        public void onClickListener(MotionEvent event) ;
    }
}
