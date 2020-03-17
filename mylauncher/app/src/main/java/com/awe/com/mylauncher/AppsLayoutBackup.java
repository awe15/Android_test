package com.awe.com.mylauncher;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.Layout;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TableLayout;
import android.widget.TableRow;

import java.util.ArrayList;
import java.util.List;

public class AppsLayoutBackup {
    private  static  AppsLayoutBackup instance;
    private List<TableLayout> layoutList = new ArrayList<TableLayout>();        //当前页的layout不放进去，所以需要index去标记
    private Context context;
    private  int totalPage = 0;
    private  int currentPage = 0;
    private  FrameLayout toplayout = null;

    private  AppsLayoutBackup(Context cx, FrameLayout v){
        context = cx;
        toplayout = v;
    }

    public static synchronized AppsLayoutBackup getInstance(Context cx, FrameLayout v)
    {
        if (instance == null)
            instance = new AppsLayoutBackup(cx, v);
        return instance;
    }

    //新增的page页都是空的  调用在一开始或增页的情况，如果少页默认超出的页不处理
    public void setPage(int total, int current){
        if ((context == null) && (toplayout == null)) return;
        if (totalPage < total-1) {
            for (int i  = 1; i < total-totalPage; ++i) {
                TableLayout tmp = new TableLayout(context);
                tmp.setDrawingCacheEnabled(true);
                tmp.setVisibility(View.INVISIBLE);
                toplayout.addView(tmp);
                layoutList.add(tmp);
            }
        }
        totalPage = total-1;
        currentPage = current;
    }

    //清除布局  一定要先清除VIEW，否则会报错
    public void clearOneLayout(int page){
        if (page > totalPage-1) return;
        if (page > currentPage)
            page -= 2;
        else
            page--;
        TableLayout tbLayout = layoutList.get(page);
        for (int i = 0; i < tbLayout.getChildCount(); ++i) {
            TableRow tb = (TableRow)tbLayout.getChildAt(i);
            tb.removeAllViews();
        }
        //layoutList.remove(page);
    }

    public void clearAllLayout(){
        for (int j = 0 ; j < layoutList.size(); ++j) {
            TableLayout tbLayout = layoutList.get(j);
            for (int i = 0; i < tbLayout.getChildCount(); ++i) {
                TableRow tb = (TableRow) tbLayout.getChildAt(i);
                tb.removeAllViews();
            }
            //layoutList.remove(j);
        }
    }

    //加载app页
    public void loadAppPage(List<AppView>  list, int page, int pageRow, int pageColumn, int width) {
        if (page > currentPage)
            page -= 2;
        else
            page--;
        TableLayout tbLayout = layoutList.get(page);
        //放置APP
        int index = 0+(page-1)*pageRow*pageColumn;
        for (int i = 0; i < pageRow; ++i) {
            TableRow tbRow = new TableRow(context);
            for (int j = 0; j < pageColumn; ++j) {
                AppView i_app = list.get(index++);
                tbRow.addView(i_app);
            }
            tbLayout.addView(tbRow);
        }
        //APP中间对齐
        tbLayout.setPadding((width-pageColumn*AppView.getViewWidth())/2,10,0,0);
    }

    //加载all app页
    public void loadAllAppPage(List<AppView>  list, int pageRow, int pageColumn, int width){
        clearAllLayout();
        int listIndex = 0;
        for (int k = 0; k < totalPage+1; ++k){
            if (k+1 != currentPage){
                TableLayout tbLayout = layoutList.get(listIndex);   //获得List
                int index = 0 + k * pageRow * pageColumn;
                for (int i = 0; i < pageRow; ++i) {
                    TableRow tbRow = new TableRow(context);
                    for (int j = 0; j < pageColumn; ++j) {
                        AppView i_app = list.get(index++);
                        tbRow.addView(i_app);
                    }
                    tbLayout.addView(tbRow);
                }
                //APP中间对齐
                tbLayout.setPadding((width-pageColumn*AppView.getViewWidth())/2,10,0,0);
                ++listIndex;
            }
        }
    }

    //获得页缓冲
    public Bitmap getPageCache(int needPage){
        if (needPage > currentPage)
            needPage -= 2;
        else
            needPage--;
        if (needPage < 0) return null;
        if (needPage >= totalPage) return null;
        TableLayout tableLayout = layoutList.get(needPage);
        tableLayout.destroyDrawingCache();
        tableLayout.buildDrawingCache();
        Bitmap img = tableLayout.getDrawingCache();
        return img;
    }

}
