package com.awe.com.mylauncher;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.os.Message;
import android.widget.TextView;

//refer: https://www.cnblogs.com/caobotao/p/5035844.html
public class AppView extends FrameLayout {
    public static final int UNINSTALL_APP_STA = 1;
    public static final int UNINSTALL_APP = 2;
    public static final int MOVE_APP = 3;

    private static int ICON_PX = 128;
    private static int ICON_PY = 128;
    private static int TOP_BLK = 30;
    private static int BOTTOM_BLK = 30;
    private static int LEFT_BLK = 30;
    private static int RIGHT_BLK = 30;
    private static int NAME_WIDTH = ICON_PX+60;
    private static int NAME_HEIGHT = 60;
    private static int NAME_FONTSIZE = 12;

    private boolean isEmpty = true;
    private volatile boolean isUninstallSta = false;
    private volatile boolean isMove  = false;

    private FrameLayout  closeLayout;
    private LinearLayout linearLayout;

    private ImageView iconImg;
    private ImageView closeBtn;
    private ImageView checkImg;
    private TextView nameView;
    private String package_name;
    private Bitmap resizeBmp;

    private Handler handler = null;
    private int uninstallCount = 0;

    //获得宽高
    public static int getViewWidth(){
        int view_w = NAME_WIDTH + LEFT_BLK + RIGHT_BLK;
        return  view_w;
    }
    public static int getViewHeight(){
        int view_h = ICON_PY + TOP_BLK + BOTTOM_BLK + NAME_HEIGHT;
        return  view_h;
    }

    //如果需要回传数据
    public  void setHandler(Handler h) {
        handler = h;
    }

    //设置隐藏或者显示接口
    public void setViewVisiable(boolean status){
        checkImg.setVisibility(INVISIBLE);
        if (status) {
            if (!isEmpty)
                closeBtn.setVisibility(VISIBLE);
            nameView.setVisibility(VISIBLE);
            iconImg.setVisibility(VISIBLE);
        }
        else {
            closeBtn.setVisibility(INVISIBLE);
            nameView.setVisibility(INVISIBLE);
            iconImg.setVisibility(INVISIBLE);
        }
    }

    public void setIsMoved(boolean status){
        if (status)
            isMove = true;
        else
            isMove = false;
    }

    public boolean isEmpty(){
        return isEmpty;
    }

    //获得图标名称和信息
    public void getAppInfo(Bitmap appIcon, String appPkgName, String appName){
        appIcon = resizeBmp;
        appPkgName = package_name;
        appName =  nameView.getText().toString();
    }

    public Bitmap getIconBitmap(){
        return resizeBmp;
    }

    public String getAppPackageName(){
        return package_name;
    }

    //判断是否在点击范围内
    public boolean isInIconRange( MotionEvent event)
    {
        int x = (int)event.getX();
        int y = (int)event.getY();
        if (x < iconImg.getLeft() || y < iconImg.getTop()
                || x > iconImg.getRight() || y > iconImg.getBottom())
            return  false;
        return true;
    }

    public boolean isInIconRange( int x, int y)
    {
        int l = iconImg.getLeft();
        int r = iconImg.getTop();
        if (x < iconImg.getLeft() || y < iconImg.getTop()
                || x > iconImg.getRight() || y > iconImg.getBottom())
            return  false;
        return true;
    }

    private boolean isIncloseImgRange( MotionEvent event)
    {
        int x = (int)event.getX();
        int y = (int)event.getY();
        if (x < closeBtn.getLeft() || y < closeBtn.getTop()
                || x > closeBtn.getRight() || y > closeBtn.getBottom())
            return  false;
        return true;
    }

    //显示选中提示框
    public  void setShowTipCheckbox(boolean status){
        if (status)
            checkImg.setVisibility(VISIBLE);
        else
            checkImg.setVisibility(INVISIBLE);
    }

    //shake       refer https://www.jianshu.com/p/7d262563edda
    public void startShakeView() {
        float shakeDegrees = 8;
        long duration = 500;
        //从左向右
        Animation rotateAnim = new RotateAnimation(-shakeDegrees, shakeDegrees, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);

        rotateAnim.setDuration(duration / 10);
        rotateAnim.setRepeatMode(Animation.REVERSE);
        rotateAnim.setRepeatCount(-1);

        AnimationSet smallAnimationSet = new AnimationSet(false);
        smallAnimationSet.addAnimation(rotateAnim);
        iconImg.startAnimation(smallAnimationSet);
        if (!isEmpty)
            closeBtn.startAnimation(smallAnimationSet);
    }
    private void stopShakeView() {
        iconImg.clearAnimation();
        closeBtn.clearAnimation();
    }
    private void onTouchAnimation()
    {
        float scaleSmall = (float)1.0;
        float scaleLarge = (float)1.2;
        long duration = 500;
        Animation scaleAnim = new ScaleAnimation(scaleSmall, scaleLarge, scaleSmall, scaleLarge);
        scaleAnim.setDuration(duration);
        scaleAnim.setRepeatCount(1);
        Animation scaleAnim1 = new ScaleAnimation(scaleLarge, scaleSmall, scaleLarge, scaleSmall);
        scaleAnim1.setDuration(duration);
        scaleAnim1.setRepeatCount(1);
        AnimationSet smallAnimationSet = new AnimationSet(false);
        smallAnimationSet.addAnimation(scaleAnim);
        smallAnimationSet.addAnimation(scaleAnim1);
     //   nameView.startAnimation(smallAnimationSet);
        iconImg.startAnimation(smallAnimationSet);
    }

    //设置模式
    public void setUninstallStatus(boolean sta){
        if (sta) {
            isUninstallSta = true;
            isMove = false;
            if (!isEmpty) {
                closeBtn.setVisibility(View.VISIBLE);
                startShakeView();
            }
        }
        else{
            isUninstallSta = false;
            isMove = false;
            closeBtn.setVisibility(View.INVISIBLE);
            checkImg.setVisibility(INVISIBLE);
            stopShakeView();
        }
    }

    //绑定app数据
    public void bindData(final String appPkgName, final String appName, final Bitmap appIcon) {
        if ((appPkgName == "") || (appIcon == null))
            return;
        package_name = appPkgName;
        resizeBmp =  AppUtils.scaleBitmap(appIcon, ICON_PX, ICON_PY);
        iconImg.setImageBitmap(resizeBmp);
        nameView.setText(appName);
        isEmpty = false;
    }

    //清除绑定数据
    public void clearBindData() {
        package_name = "";
        resizeBmp = null;
        iconImg.setImageBitmap(null);
        nameView.setText("");
        isEmpty = true;
    }

    private synchronized void sendMsg2Out(Message msg){
        handler.sendMessage(msg);
    }

    public AppView(final Context context, Handler h) {
        super(context);
        handler = h;
        //实例化Layout 控件
        closeLayout =  new FrameLayout(context);
        linearLayout =  new LinearLayout(context);
        nameView = new TextView(context);
        iconImg = new ImageView(context);
        closeBtn = new ImageView(context);
        checkImg = new ImageView(context);

        //Bitmap bmp = BitmapFactory.decodeResource(getResources(),R.drawable.uninstall );
        //closeBtn.setImageBitmap(AppUtils.scaleBitmap(bmp, 30, 30));
        closeBtn.setImageResource(R.drawable.uninstall);
        //closeBtn.getBackground().setAlpha(0);
        //closeBtn.setBackgroundColor(0);
        //closeBtn.setX(-1*ICON_PX/2);
        //closeBtn.setY(-1*ICON_PX/2+10);
        closeBtn.setAlpha((float)0.9);
        //closeBtn.setMaxWidth(20);

        Bitmap bmp1 = BitmapFactory.decodeResource(getResources(),R.drawable.checkbox );
        checkImg.setImageBitmap(AppUtils.scaleBitmap(bmp1, ICON_PX, ICON_PY));
        checkImg.setAlpha((float) 0.4);
        checkImg.setVisibility(INVISIBLE);

        nameView.setTextSize(NAME_FONTSIZE);
        nameView.setHeight(NAME_HEIGHT);
        nameView.setBackgroundColor(0);
        nameView.setGravity(Gravity.CENTER);
        nameView.setSingleLine(true);

        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.addView(iconImg,new LayoutParams(ICON_PX, ICON_PX));
        linearLayout.addView(nameView, new LayoutParams(NAME_WIDTH, ViewGroup.LayoutParams.WRAP_CONTENT));
        linearLayout.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL);

        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(80, 80);
        closeLayout.addView(checkImg);
        closeLayout.addView(closeBtn,layoutParams);
        addView(linearLayout);
        addView(closeLayout);
        closeBtn.setVisibility(View.INVISIBLE);
        setPadding(LEFT_BLK,TOP_BLK,RIGHT_BLK,BOTTOM_BLK);

//        closeBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Log.e("InstallActivity", "uninstall:" + nameView.getText());
//                Message msg = new Message();
//                msg.what = UNINSTALL_APP;
//                Bundle b = new Bundle();                                                            //refer: https://www.cnblogs.com/yiki/p/3185863.html
//                b.putString("package",package_name);
//                b.putString("app_name", nameView.getText().toString());
//                msg.setData(b);
//                handler.sendMessage(msg);
//                isUninstallSta = false;
//            }
//        });
    }

    public AppView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:{
                uninstallCount = 0;
                if (!isUninstallSta ) {
                    Log.e("APPView", "down1:" + nameView.getText());
                    if (isInIconRange(event)) {//normal
                        onTouchAnimation();
                        return true;
                    }
                }
                else {
                    if (!isMove) {//将会处于可移动状态
                        if (isInIconRange(event)){
                            isMove = true;
                            stopShakeView();
                            closeBtn.setVisibility(INVISIBLE);
                            nameView.setVisibility(INVISIBLE);
                            iconImg.setVisibility(INVISIBLE);

                            Message msg = new Message();
                            msg.what = MOVE_APP;
                            Bundle b = new Bundle();
                            b.putString("package",package_name);
                            msg.setData(b);
                            sendMsg2Out(msg);
                            Log.e("APPView", "down3 in moving:" + nameView.getText());
                        }
                    }
                    if (isIncloseImgRange(event)) {
                        uninstallApp();
                    }
                    return false;
                }
                break;
            }
            case MotionEvent.ACTION_MOVE:{
                if (!isUninstallSta) {
                    Log.e("APPView", "move:" + nameView.getText()+ " " + uninstallCount);
                    if (isInIconRange(event)) {
                        ++uninstallCount;
                        if (uninstallCount > 20) {
                            Message msg = new Message();
                            msg.what = UNINSTALL_APP_STA;
                            sendMsg2Out(msg);
                        }
                    }
                    else
                        uninstallCount = 0;
                }
                return false;
            }
            case MotionEvent.ACTION_UP: {
                if (!isUninstallSta) {
                    if (isInIconRange(event)) {
                        Log.e("InstallActivity", "up:" + nameView.getText());
                        if (!isEmpty)
                            AppUtils.openApp(this.getContext(), package_name);
                    }
                }
                return true;
            }
        }
        return false;
    }

    private void uninstallApp(){
        Message msg = new Message();
        msg.what = UNINSTALL_APP;
        Bundle b = new Bundle();                                                            //refer: https://www.cnblogs.com/yiki/p/3185863.html
        b.putString("package",package_name);
        b.putString("app_name", nameView.getText().toString());
        msg.setData(b);
        handler.sendMessage(msg);
        isUninstallSta = false;
    }
}