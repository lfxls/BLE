
/**************************************************************************************
* [Project]
*       MyProgressDialog
* [Package]
*       com.lxd.widgets
* [FileName]
*       CustomProgressDialog.java
* [Copyright]
*       Copyright 2012 LXD All Rights Reserved.
* [History]
*       Version          Date              Author                        Record
*--------------------------------------------------------------------------------------
*       1.0.0           2012-4-27         lxd (rohsuton@gmail.com)        Create
**************************************************************************************/
	
package com.utils;



import java.util.Timer;
import java.util.TimerTask;

import com.example.bluetooth.le.R;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.TextView;



public class CustomProgressDialog extends Dialog {
	private Context context = null;
	private static CustomProgressDialog customProgressDialog = null;
	public static final String TAG = "ProgressDialog";
    private long mTimeOut = 0;// 默认timeOut�?即无限大
    private OnTimeOutListener mTimeOutListener = null;// timeOut后的处理�?
    private Timer mTimer = null;// 定时�?
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            if(mTimeOutListener != null){
                mTimeOutListener.onTimeOut(CustomProgressDialog.this);
            }
        }
    };
	
	
	public CustomProgressDialog(Context context){
		super(context);
		this.context = context;
	}
	
	public CustomProgressDialog(Context context, int theme) {
        super(context, theme);
    }
	
	public static CustomProgressDialog createDialog(Context context){
		customProgressDialog = new CustomProgressDialog(context,R.style.CustomProgressDialog);
		customProgressDialog.setContentView(R.layout.customprogressdialog);
		customProgressDialog.getWindow().getAttributes().gravity = Gravity.CENTER;
		
		return customProgressDialog;
	}
 
    public void onWindowFocusChanged(boolean hasFocus){
    	
    	if (customProgressDialog == null){
    		return;
    	}
    	
//        ImageView imageView = (ImageView) customProgressDialog.findViewById(R.id.loadingImageView);
//        AnimationDrawable animationDrawable = (AnimationDrawable) imageView.getBackground();
//        animationDrawable.start();
    }
 
    /**
     * 
     * [Summary]
     *       setTitile ����
     * @param strTitle
     * @return
     *
     */
    public CustomProgressDialog setTitle(String strTitle){
    	((TextView) customProgressDialog.findViewById(R.id.title)).setText(strTitle);
    	return customProgressDialog;
    }
    public void setTitle(int strTitle){
    	((TextView) customProgressDialog.findViewById(R.id.title)).setText(strTitle);
    }
    
    /**
     * 
     * [Summary]
     *       setMessage ��ʾ����
     * @param strMessage
     * @return
     *
     */
    public CustomProgressDialog setMessage(String strMessage){
    	TextView tvMsg = (TextView)customProgressDialog.findViewById(R.id.id_tv_loadingmsg);
    	
    	if (tvMsg != null){
    		tvMsg.setText(strMessage);
    	}
    	
    	return customProgressDialog;
    }
    
    public void setTimeOut(long t, OnTimeOutListener timeOutListener) {
        mTimeOut = t;
        if (timeOutListener != null) {
            this.mTimeOutListener = timeOutListener;
        }
    }
    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
        if (mTimer != null) {

            mTimer.cancel();
            mTimer = null;
        }
    }

    @Override
    public void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
        if (mTimeOut != 0) {
            mTimer = new Timer();
            TimerTask timerTast = new TimerTask() {
                @Override
                public void run() {
                    // TODO Auto-generated method stub
                //    dismiss();
                        Message msg = mHandler.obtainMessage();
                        mHandler.sendMessage(msg);
                }
            };
            mTimer.schedule(timerTast, mTimeOut);
        }

    }

    /**
     * 通过静�?Create的方式创建一个实例对�?
     * 
     * @param context
     * @param time    
     *                 timeout时间长度
     * @param listener    
     *                 timeOutListener 超时后的处理�?
     * @return MyProgressDialog 对象
     */
    public static CustomProgressDialog createProgressDialog(Context context,
            long time, OnTimeOutListener listener) {
//    	CustomProgressDialog progressDialog = new CustomProgressDialog(context);
    	customProgressDialog  = new CustomProgressDialog(context,R.style.CustomProgressDialog);
    	customProgressDialog.setContentView(R.layout.customprogressdialog);
    	customProgressDialog.getWindow().getAttributes().gravity = Gravity.CENTER;
        if (time != 0) {
        	customProgressDialog.setTimeOut(time, listener);
        }
        return customProgressDialog;
    }

    /**
     * 
     * 处理超时的的接口
     *
     */
    public interface OnTimeOutListener {
        
        /**
         * 当progressDialog超时时调用此方法
         */
        abstract public void onTimeOut(CustomProgressDialog dialog);
    }
}
