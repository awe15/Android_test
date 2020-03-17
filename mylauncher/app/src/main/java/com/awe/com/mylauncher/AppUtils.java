package com.awe.com.mylauncher;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.util.Log;


import java.util.List;

public class AppUtils {

    public static int getAllAppsInfo(Context obj, List<String> l_name,  List<Bitmap> l_icon, List<String> l_packageName){
        PackageManager pm = obj.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
            List<ResolveInfo> list = pm.queryIntentActivities(intent, PackageManager.PERMISSION_GRANTED);
            for (ResolveInfo rInfo : list) {
                l_name.add(rInfo.activityInfo.applicationInfo.loadLabel(pm).toString());
                Bitmap bitmap = ((BitmapDrawable)pm.getApplicationIcon(rInfo.activityInfo.applicationInfo)).getBitmap();//获得应用的图标
                //pm.getApplicationLabel(rInfo.activityInfo.applicationInfo).toString();//获得应用名
                String packageName = rInfo.activityInfo.applicationInfo.packageName;//获得应用包名
                l_packageName.add(packageName);
                l_icon.add(bitmap);
        }
        return list.size();
    }

    public static boolean checkPackInfo(Context obj, String packname, PackageManager pm){
        PackageInfo packageInfo = null;
        try {
            packageInfo = obj.getPackageManager().getPackageInfo(packname, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return true;
    }

    public static boolean openApp(Context obj, String packname){
        PackageManager pm = obj.getPackageManager();
        if (checkPackInfo(obj, packname, pm)) {
            Intent intent = pm.getLaunchIntentForPackage(packname);
            if (intent == null) return false;
            obj.startActivity(intent);
            return true;
        } else {
            Log.e("AppUtils", "openApp:can not find package!");
           return false;
        }
    }

    public static boolean uninstallApp(Context context, String packageName){
        Uri uri = Uri.parse("package:" + packageName);
        Intent intent = new Intent(Intent.ACTION_DELETE, uri);
        context.startActivity(intent);
        return true;
    }

    public static Bitmap scaleBitmap(Bitmap orign_pic, int aim_x, int aim_y){
        float sx = ((float) aim_x) / orign_pic.getWidth();
        float sy = ((float) aim_y) /orign_pic.getHeight();
        Matrix matrix = new Matrix();
        matrix.postScale(sx, sy);
        Bitmap resizeBmp = Bitmap.createBitmap(orign_pic, 0, 0, orign_pic.getWidth(), orign_pic.getHeight(), matrix, true);
        return resizeBmp;
    }
}
