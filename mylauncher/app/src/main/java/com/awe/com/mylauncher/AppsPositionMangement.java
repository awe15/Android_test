package com.awe.com.mylauncher;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AppsPositionMangement {
        //refer: https://www.cnblogs.com/linfenghp/p/5393832.html
        class AppPageComparetor implements Comparator<AppPositionInfo> {
            @Override
            public int compare(AppPositionInfo app1, AppPositionInfo app2) { //逆序排列
                int ret = app1.pageIndex - app2.pageIndex;
                if ( ret != 0)
                    return ret;
                else
                    return app1.orderIndex - app2.orderIndex;
            }
        }

        private static AppsPositionMangement instance = null;
        private int pageRow = 0;
        private int pageColumn = 0;
        private List<AppPositionInfo> appsInfo = null;

        private AppsPositionMangement(){}

        public static synchronized AppsPositionMangement getInstance()
        {
            if (instance == null)
                instance = new AppsPositionMangement();
           return instance;
        }

        public List<AppPositionInfo> getAppInfoList(){
            return appsInfo;
        }

        //确定每页最大app数
        public void setPageSize(int row, int column){
            pageRow = row;
            pageColumn = column;
        }

        //保存order文件
        public boolean saveAppOrderXmlFile(List<AppPositionInfo> list) {
            int pageMax = pageColumn * pageRow;
            for (AppPositionInfo appInfo : list) {
                if (appInfo.orderIndex > pageMax)
                    return false;
            }
            Collections.sort(list, new AppPageComparetor());
            FileOutputStream fos = null;
            try{
                XmlSerializer xs = Xml.newSerializer();
//                File file = new File(Environment.getExternalStorageDirectory(),
//                        String.valueOf(pageRow) + "_"+ String.valueOf(pageColumn) + "_order.xml");
                File file = new File("/sdcard/test",
                        String.valueOf(pageRow) + "_"+ String.valueOf(pageColumn) + "_order.xml");

                // 用输出流输出info.xml
                fos = new FileOutputStream(file);
                xs.setOutput(fos, "UTF_8"); // 指定用utf-8编码生成文件
                xs.startDocument("UTF-8", true);// 生成xml表头，两个参数表示表头属性

                int lastPageIndex = 0;
                for (AppPositionInfo appInfo : list) {
                    if (appInfo.pageIndex != lastPageIndex) {
                        lastPageIndex = appInfo.pageIndex;
                    }
                    xs.startTag(null, "app");
                    xs.startTag(null, "name");
                    xs.text(appInfo.packageName);
                    xs.endTag(null, "name");

                    xs.startTag(null, "page");
                    xs.text(String.valueOf(appInfo.pageIndex));
                    xs.endTag(null, "page");

                    xs.startTag(null, "order");
                    xs.text(String.valueOf(appInfo.orderIndex));
                    xs.endTag(null, "order");

                    xs.endTag(null, "app");
                }
                // 表示文档生成结束
                xs.endDocument();
                if (fos != null) {
                    fos.close();
                    Log.i("Back", "33333");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return true;
        }

        //读取位置文件  https://www.cnblogs.com/zxxiaoxia/p/4320913.html
        //返回的位排列好的顺序
        public List<AppPositionInfo> readPositonFile(Context obj){
            List<AppPositionInfo> list = null;
            AppPositionInfo oneApp = new AppPositionInfo();
            FileInputStream fis;
            try {
                //获取输入流
//                File file = new File(Environment.getExternalStorageDirectory(),
//                        String.valueOf(pageRow) + "_"+ String.valueOf(pageColumn) + "_order.xml");
                File file = new File("/sdcard/test",
                        String.valueOf(pageRow) + "_"+ String.valueOf(pageColumn) + "_order.xml");
                fis = new FileInputStream(file);

                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                XmlPullParser parser = factory.newPullParser();

                parser.setInput(fis,"utf-8");

                int event=parser.getEventType();
                while(event!=XmlPullParser.END_DOCUMENT) {
                    switch (event) {
                        case XmlPullParser.START_DOCUMENT: {
                                list = new ArrayList<AppPositionInfo>();
                                event = parser.next();
                                break;
                            }
                            case XmlPullParser.START_TAG: {
                            //得到属性标记通过getName
                                String str = parser.getName();
                                if (str.equals("app")) {
                                    oneApp = new AppPositionInfo();
                                    event = parser.next();
                                }
                                else if (str.equals("name")) {
                                    event = parser.next();
                                    String name = parser.getText();
                                    oneApp.packageName = name;
                                    event = parser.next();
                                }
                                else if (str.equals("page")) {
                                    event = parser.next();
                                    String page = parser.getText();
                                    oneApp.pageIndex = Integer.parseInt(page);
                                    event = parser.next();
                                }
                                else if (str.equals("order")) {
                                    event = parser.next();
                                    String order = parser.getText();
                                    oneApp.orderIndex = Integer.parseInt(order);
                                    event = parser.next();
                                }
                            break;
                        }
                        case XmlPullParser.END_TAG: {
                            String str = parser.getName();
                            if (str.equals("app"))
                                list.add(oneApp);
                            event = parser.next();
                            break;
                        }
                        default:{
                            event = parser.next();
                            break;
                        }
                    }
                }
            }
            catch (Exception e) {
                e.printStackTrace();
                // todo : file not find
            }

            List<AppPositionInfo> appsInfo = searchAndAddApp(obj, list);
            return appsInfo;
        }

         //查询现有的包
        private List<AppPositionInfo> searchAndAddApp(Context obj, List<AppPositionInfo> list){
            List<AppPositionInfo> newList = new ArrayList<AppPositionInfo>();
            List<String> l_name = new ArrayList<String>();
            List<String> l_pkgName = new ArrayList<String>();
            List<Bitmap> l_icon = new ArrayList<Bitmap>();
            AppUtils.getAllAppsInfo(obj, l_name, l_icon, l_pkgName);

            if (list == null) {
                int p = 1, o = 0;
                for (int i = 0; i < l_pkgName.size(); ++i) {
                    AppPositionInfo tmp = new AppPositionInfo();
                    tmp.bitmap = l_icon.get(i);
                    tmp.packageName = l_pkgName.get(i);
                    tmp.appName = l_name.get(i);
                    tmp.orderIndex = ++o;
                    tmp.pageIndex = p;
                    if (o >= pageRow*pageColumn) {
                        o = 0;
                        ++p;
                    }
                    newList.add(tmp);
                }
                return newList;
            }

            for (int i = 0; i < l_pkgName.size(); ++i){
                //int index = list.indexOf(l_pkgName.get(i));
                String str = l_pkgName.get(i);
                int index = -1;
                for (int j = 0; j < list.size(); ++j){
                    if (list.get(j).packageName.equals(str)) {
                        index = j;
                        break;
                    }
                }

                if (index >= 0) {                       //xml源文件中存在
                    AppPositionInfo tmp = list.get(index);
                    tmp.bitmap = l_icon.get(i);
                    tmp.appName = l_name.get(i);
                    if (tmp.orderIndex > pageRow*pageColumn) {
                        tmp.orderIndex = -1;
                        tmp.pageIndex = -1;
                    }
                    newList.add(tmp);
                }
                else {
                    AppPositionInfo tmp = new AppPositionInfo();
                    tmp.bitmap = l_icon.get(i);
                    tmp.packageName = l_pkgName.get(i);
                    tmp.appName = l_name.get(i);
                    tmp.orderIndex = -1;
                    tmp.pageIndex = -1;
                    newList.add(tmp);
                }
            }

            //返回的位排列好的顺序
            if (newList.size() < 2) return newList;
            Collections.sort(newList, new AppPageComparetor());
            int maxPage = 0, ord = 0;
            for (AppPositionInfo tmp: newList) {
                if (tmp.pageIndex == -1) {
                    tmp.pageIndex = maxPage + 1;
                    tmp.orderIndex = ++ord;
                    if (ord >= pageColumn*pageRow) {
                        maxPage++;
                        ord = 0;
                    }
                }
                else {
                    if(tmp.pageIndex > maxPage){
                        maxPage = tmp.pageIndex;
                    }
                }
            }
            return newList;
        }

}
