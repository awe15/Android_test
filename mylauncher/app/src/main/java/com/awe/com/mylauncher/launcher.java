package com.awe.com.mylauncher;

import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;

import java.util.ArrayList;
import java.util.List;
import android.os.Message;
import android.os.Handler;
import android.text.format.Time;
import android.widget.TextView;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

// Launcher主菜单中应用程序图标的显示顺序
// refer:  https://blog.csdn.net/pengpeng8216/article/details/80123035
// refer:  https://blog.csdn.net/freelingjun/article/details/53308421
public class launcher extends AppCompatActivity {
    //权限
    private final int PERMISSION_REQUEST_CODE = 1100;
    public static final int UPDATA_TIME_MSG = 0;

    private void requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Toast.makeText(this, "Write External Storage permission allows us to do store images. Please allow this permission in App Settings.", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }
    }

    private enum INTERFACE_PAGE {
        HOME_PAGE,
        APPS_PAGE             //卸载和移动app状态
    };

    enum APP_OPERATING_STATUS {
        NORMAL_STATUS,
        SELECT_STATUS                    //
    };

    class TouchPoint {
        public float x;
        public float y;
        TouchPoint(float set_x, float set_y) {
            x = set_x;
            y = set_y;
        }
        TouchPoint() { }
    };

    //主界面
    private AppsPositionMangement posMangement = AppsPositionMangement.getInstance();
    private int width;
    private int height;
    private int pageRow = 1;
    private int pageColumn = 1;
    private int pagAppsNum = 1;
    private INTERFACE_PAGE interfacePage = INTERFACE_PAGE.HOME_PAGE;
    private APP_OPERATING_STATUS pageStatus = APP_OPERATING_STATUS.NORMAL_STATUS;
    private TextView timeTxtView;
    private TextView dateTxtView;
    private ImageView iconTipView;

    //抽屉
    private int current_page = 1;
    private int totle_page = 1;
    private int edgeMoveCount = 0;
    private TouchPoint pressPoint = new TouchPoint();

    private AppsLayoutBackup appsLayoutBackup = null;
    private FrameLayout appsLayout = null;
    private AppView selectedMovingApp = null;
    private AppView replaceMovingApp = null;
    private List<AppView> l_appsView = new ArrayList<AppView>();
    private List<AppPositionInfo> l_appsPositonInfo = new ArrayList<AppPositionInfo>();
    private LinearLayout pageLayout;
    private TableLayout tableLayout;
    private ImageView currentImg;
    private ImageView nextImg;
    private ImageView lastImg;
    private FrameLayout pageMoveLayout;

    //卸载对话框refer：https://www.cnblogs.com/gzdaijie/p/5222191.html
    private void showNormalDialog(final String packageName, String appName) {
        final AlertDialog.Builder normalDialog =
                new AlertDialog.Builder(this);
        normalDialog.setTitle("卸载app");
        normalDialog.setMessage("确定卸载 \"" + appName + "\"");
        normalDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //...To-do
                        AppUtils.uninstallApp(getApplicationContext(), packageName);
                    }
                });
        normalDialog.setNegativeButton("取消",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //...To-do

                    }
                });
        normalDialog.show();
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case UPDATA_TIME_MSG: {
                    Time t = new Time();
                    t.setToNow();
                    timeTxtView.setText(t.hour + ":" + String.format("%2d",(t.minute)) + ":" + String.format("%2d",(t.second)));
                    dateTxtView.setText(t.year + "年" + String.format("%2d",(t.month + 1))
                            + "月" + String.format("%2d",(t.monthDay)) + "日");
                    break;
                }
                case AppView.UNINSTALL_APP_STA: {           //进入选择模式，支持卸载和移动
                    for (AppView app : l_appsView)
                        app.setUninstallStatus(true);
                    pageStatus = APP_OPERATING_STATUS.SELECT_STATUS;
                    Log.e("Activity", "in selected mode");
                    break;
                }
                case AppView.UNINSTALL_APP: {
                    iconTipView.setVisibility(View.INVISIBLE);
                    if (selectedMovingApp != null) {
                        selectedMovingApp.setViewVisiable(true);
                        selectedMovingApp.setUninstallStatus(false);
                    }
                    Bundle b = msg.getData();
                    String packageName = b.getString("package");
                    String name = b.getString("app_name");
                    showNormalDialog(packageName, name);
                    break;
                }
                case AppView.MOVE_APP: {
                    Bundle b = msg.getData();
                    String packageName = b.getString("package");
                    for (AppView e: l_appsView) {
                        if (e.getAppPackageName() == packageName){
                            selectedMovingApp = e;
                            iconTipView.setImageBitmap( e.getIconBitmap());
                            int[] location = new int[2];
                            e.getLocationOnScreen(location);
                            iconTipView.setX(location[0]);
                            iconTipView.setY(location[1]);
                            iconTipView.setVisibility(View.VISIBLE);
                            break;
                        }
                    }
                    break;
                }
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
        requestPermission();
        timeTxtView = (TextView) findViewById(R.id.time_txt);
        dateTxtView = (TextView) findViewById(R.id.date_txt);
        pageLayout = (LinearLayout) findViewById(R.id.page_layout);
        pageMoveLayout = (FrameLayout) findViewById(R.id.page_move_layout);
        appsLayout = (FrameLayout) findViewById(R.id.apps_layout);
        tableLayout = (TableLayout) findViewById(R.id.apps_table);
        tableLayout.setDrawingCacheEnabled(true);
        appsLayoutBackup = AppsLayoutBackup.getInstance(this, appsLayout);

        currentImg = (ImageView) findViewById(R.id.current_image);
        currentImg.setX(0);
        currentImg.setY(0);
        nextImg = (ImageView) findViewById(R.id.next_image);
        nextImg.setY(0);
        nextImg.setY(0);
        nextImg.setMaxWidth(width);
        lastImg = (ImageView) findViewById(R.id.last_image);
        lastImg.setY(0);
        lastImg.setY(0);
        lastImg.setMaxWidth(width);
        iconTipView = (ImageView) findViewById(R.id.icon_tip_view);

        getScreen();
        getPageRowColumn();
        posMangement.setPageSize(pageRow, pageColumn);
        l_appsPositonInfo = posMangement.readPositonFile(this);

        new TimeThread().start();
        appsLayout.setOnTouchListener(new View.OnTouchListener() {
           @Override
           public boolean onTouch(View v, MotionEvent event) {
               switch (event.getAction()) {
                   case MotionEvent.ACTION_DOWN:  //按下
                       if (APP_OPERATING_STATUS.NORMAL_STATUS == pageStatus){
                            Log.e("AppFrameLayout", "on:=======" );
                            pressPoint.x = event.getX();
                            pressPoint.y = event.getY();
                            getBackViewDrawingCache();
                            lastImg.setImageBitmap(appsLayoutBackup.getPageCache(current_page-1));
                            nextImg.setImageBitmap(appsLayoutBackup.getPageCache(current_page+1));
                       }
                       return  true;
                   case MotionEvent.ACTION_MOVE: //移动
                       if (APP_OPERATING_STATUS.SELECT_STATUS == pageStatus){
                            Log.e("AppFrameLayout", "select move:=======" );
                            if (selectedMovingApp != null)
                                onTouchMocingAppMove(event);
                       }
                       else if (APP_OPERATING_STATUS.NORMAL_STATUS == pageStatus){
                            Log.e("AppFrameLayout", "normal move:=======" );
                            changePageShow(event);
                       }
                       return  true;
                   case MotionEvent.ACTION_UP://松开
                       lastImg.setImageBitmap(null);
                       nextImg.setImageBitmap(null);
                       appsLayoutBackup.clearAllLayout();
                       if (APP_OPERATING_STATUS.NORMAL_STATUS == pageStatus) {
                           pageMoveLayout.setVisibility(View.INVISIBLE);
                           tableLayout.setVisibility(View.VISIBLE);
                           changePage(event);
                           Log.e("AppFrameLayout", "normal release:=======");
                       }
                       else if (APP_OPERATING_STATUS.SELECT_STATUS == pageStatus){          //提示本次移动结束
                           Log.e("AppFrameLayout", "select release:=======");
                           iconTipView.setVisibility(View.INVISIBLE);
                           if (selectedMovingApp != null) {
                               selectedMovingApp.setIsMoved(false);
                               selectedMovingApp.setVisibility(View.VISIBLE);
                               selectedMovingApp.setViewVisiable(true);
                               if (replaceMovingApp != null) {
                                   replaceMovingApp.setViewVisiable(true);
                                   changeAppsPlace();
                                   rebindPositionInfo();
                                   loadAppPage(tableLayout, current_page);
                               }
                               for (AppView app : l_appsView)
                                   app.startShakeView();
                               selectedMovingApp = null;
                           }
                           else {
                               pageStatus = APP_OPERATING_STATUS.NORMAL_STATUS;
                               for (AppView app : l_appsView)
                                   app.setUninstallStatus(false);
                           }
                        }
                       appsLayoutBackup.setPage(totle_page, current_page);
                       appsLayoutBackup.loadAllAppPage(l_appsView, pageRow, pageColumn, width);
                       return  true;
               }
               return false;
           }
       });
    }

    public void onClickMoreApps(View view) {
        FrameLayout frameLayout = (FrameLayout) findViewById(R.id.apps_layout);
        frameLayout.setVisibility(View.VISIBLE);
        FrameLayout frameLayout1 = (FrameLayout) findViewById(R.id.first_layout);
        frameLayout1.setVisibility(View.INVISIBLE);

        //加载页面
        showPageIcon();
        resizeAllAppView();
        rebindAppViewData();
        loadAppPage(tableLayout, current_page);
        appsLayoutBackup.setPage(totle_page, current_page);
        appsLayoutBackup.loadAllAppPage(l_appsView, pageRow, pageColumn, width);

        //切换到APP页
        interfacePage = INTERFACE_PAGE.APPS_PAGE;
    }

    //定时更新时间
    public class TimeThread extends Thread {
        @Override
        public void run() {
            super.run();
            while (true) {
                try {
                    Message msg = new Message();
                    msg.what = UPDATA_TIME_MSG;
                    handler.sendMessage(msg);
                    Thread.sleep(1000);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    //默认当前页
    private void loadAppPage(TableLayout tbLayout, int page) {
        for (int i = 0; i < tbLayout.getChildCount(); ++i) {  //removeview
            TableRow tb = (TableRow)tbLayout.getChildAt(i);
            tb.removeAllViews();
        }
        tbLayout.removeAllViews();

        //放置APP
        int index = 0+(page-1)*pagAppsNum;
        for (int i = 0; i < pageRow; ++i) {
            TableRow tbRow = new TableRow(getApplicationContext());
            for (int j = 0; j < pageColumn; ++j) {
                AppView i_app = l_appsView.get(index++);
                tbRow.addView(i_app);
            }
            tbLayout.addView(tbRow);
        }
        //APP中间对齐
        tbLayout.setPadding((width-pageColumn*AppView.getViewWidth())/2,10,0,0);
    }

    //获得滑动动画时截图
    private void getBackViewDrawingCache(){
        tableLayout.destroyDrawingCache();
        tableLayout.buildDrawingCache();
        Bitmap bitmap = tableLayout.getDrawingCache();
        currentImg.setImageBitmap(bitmap);
    }

    //正常情况下的翻页手指跟随效果
    private void changePageShow(MotionEvent event){
        TouchPoint movePoint = new TouchPoint();
        movePoint.x = event.getX();              //获得偏移距离
        float distance = movePoint.x - pressPoint.x; //移动截图
        currentImg.setX(distance);
        nextImg.setX(distance + width);
        lastImg.setX(distance - width);
        pageMoveLayout.setVisibility(View.VISIBLE);
        tableLayout.setVisibility(View.INVISIBLE);
    }

    //修改页
    private void changePage(MotionEvent event){
        TouchPoint releasePoint = new TouchPoint();
        releasePoint.x = event.getX();
        releasePoint.y = event.getY();
        if (releasePoint.x >= pressPoint.x)      //right
        {
            if ((releasePoint.x - pressPoint.x) > 80) {
                if (current_page != 1)
                    --current_page;
                showPageIcon();
                loadAppPage(tableLayout, current_page);
                appsLayoutBackup.setPage(totle_page, current_page);
                appsLayoutBackup.loadAllAppPage(l_appsView, pageRow, pageColumn, width);
            }
        } else {           //left
            if ((pressPoint.x - releasePoint.x) > 80) {
                if (current_page != totle_page)
                    ++current_page;
                showPageIcon();
                loadAppPage(tableLayout, current_page);
                appsLayoutBackup.setPage(totle_page, current_page);
                appsLayoutBackup.loadAllAppPage(l_appsView, pageRow, pageColumn, width);
            }
        }
    }

    //在移动APP时的操作
    private void onTouchMocingAppMove(MotionEvent event){
        iconTipView.setX(event.getRawX()-50);
        iconTipView.setY(event.getRawY()-100);
        //判断位置
        replaceMovingApp = null;
        int x = (int)event.getX();
        int y = (int)event.getY();
        int page_max = pageColumn * pageRow;
        int offset = page_max * (current_page-1);
        if (offset + page_max <= l_appsView.size() ){
            for (int i = 0; i < page_max; ++i) {
                AppView app = l_appsView.get(i + offset);
                int[] location = new int[2];                      //https://blog.csdn.net/ys743276112/article/details/51396319
                app.getLocationOnScreen(location);
                if (app.isInIconRange(x-location[0], y-location[1])) {
                    app.setShowTipCheckbox(true);
                    replaceMovingApp = app;
                    break;
                }
                app.setShowTipCheckbox(false);
            }
        }
        //移动到最左/右边的区域，前后页翻转
        edgeMoveCount++;
        if (x < 80) {         //左边界
            if (edgeMoveCount > 20) { //翻到上一页
                if (current_page > 1) {
                    current_page--;
                    lastImg.setImageBitmap(null);
                    nextImg.setImageBitmap(null);
                    appsLayoutBackup.clearAllLayout();
                    loadAppPage(tableLayout, current_page);
                    showPageIcon();
                    for (AppView app : l_appsView)
                        app.startShakeView();
                }
                edgeMoveCount = 0;
            }
        }
        else if (x > width - 80){      //右边界
             if (edgeMoveCount > 20) {
                 if (current_page < totle_page) {
                     current_page++;
                     lastImg.setImageBitmap(null);
                     nextImg.setImageBitmap(null);
                     appsLayoutBackup.clearAllLayout();
                     loadAppPage(tableLayout, current_page);
                     showPageIcon();
                     for (AppView app : l_appsView)
                         app.startShakeView();
                 }
                 edgeMoveCount = 0;
             }
        }
        else
            edgeMoveCount = 0;
    }

    //修改app移动后的位置
    private void changeAppsPlace(){
        //和原有的交互位置
        int replaceIndex = l_appsView.indexOf(replaceMovingApp);
        int selectIndex = l_appsView.indexOf(selectedMovingApp);
        AppView tmp = l_appsView.get(replaceIndex);
        if (replaceIndex >= 0)
            l_appsView.set(replaceIndex, selectedMovingApp);
        if (selectIndex >= 0)
            l_appsView.set(selectIndex, tmp);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
     }

    //创建最下方页图标
    private void showPageIcon() {
        AppPositionInfo last = l_appsPositonInfo.get(l_appsPositonInfo.size()-1);
        totle_page = last.pageIndex;
        pageLayout.removeAllViews();
        for(int i = 1; i <=totle_page; ++i) {
                if (i == current_page) {
                    ImageView img = new ImageView(this);
                    img.setImageDrawable(getResources().getDrawable(R.drawable.circle_red));
                    pageLayout.addView(img);
                } else {
                    ImageView img1 = new ImageView(this);
                    img1.setImageDrawable(getResources().getDrawable(R.drawable.circle_grey));
                    pageLayout.addView(img1);
                }
        }
    }

    //调整所有appview
    private void resizeAllAppView(){
        int num = totle_page * pageRow * pageColumn;
        int exist_num = l_appsView.size();
        if (exist_num < num) {
            for (int i = 0; i < num-exist_num; ++i) {
                AppView tmp = new AppView(this, handler);
                tmp.setHandler(handler);
                l_appsView.add(tmp);
            }
        }
        else  if (exist_num > num)
        {
            for (int i = num-1; i < exist_num-num; ++i)
                l_appsView.remove(i);
        }
    }

    //绑定APPVIEW对应的数据
    private void rebindAppViewData() {
        int viewSize = l_appsView.size();
        int pageMaxAppsNum  = pageColumn * pageRow;
        int i = 0;
        for (AppPositionInfo app : l_appsPositonInfo) {
            int page = app.pageIndex;
            int order = app.orderIndex;
            if (page == 0 || order == 0) continue;
            if (viewSize < (pageMaxAppsNum * page))
                continue;
            AppView bindApp = l_appsView.get(pageMaxAppsNum*(page-1)+(order-1));
            bindApp.bindData(app.packageName, app.appName, app.bitmap);
            i++;
        }
    }

    //从APPVIE重新设置位置信息
    private void rebindPositionInfo(){
        List<AppPositionInfo> appsInfo = new ArrayList<AppPositionInfo>();
        for (AppView v: l_appsView){
            if (!v.isEmpty()){
                int index = l_appsView.indexOf(v);
                if (index >= 0 ) {
                    int page = index/pagAppsNum;
                    int order = index-page*pagAppsNum+1;
                    if (order == 0) order = 1;
                    AppPositionInfo tmp = new AppPositionInfo(v.getAppPackageName(),
                            page+1, order);
                    appsInfo.add(tmp);
                }
            }
        }
        posMangement.saveAppOrderXmlFile(appsInfo);
    }

    private void getPageRowColumn() {
        int w = AppView.getViewWidth();
        int h = AppView.getViewHeight();
        if ((w == 0) || (h == 0)) {
            Log.e("onClickMoreApps", "error app view size!");
            return;
        }
        pageColumn = (int) (width / w);
        pageRow = (int) ((height - 30) / h);
        pagAppsNum = pageColumn * pageRow;
    }

    private void getScreen() {
        Resources resources = this.getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        width = dm.widthPixels;
        height = dm.heightPixels;
        Log.d("screen", width + " , " + height);
    }

}
