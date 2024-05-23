package com.zeus.tec.model.utils;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import com.blankj.utilcode.util.ToastUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

/**
 * Created by AllenWang on 2022/8/18.
 */
public class JumpToPermissionUtil {
 private String packageName;

 public JumpToPermissionUtil(String packageName) {
  this.packageName = packageName;
 }
 /**
  * 跳转至权限设置页面
  * @param activity
  */
 public void goPermissionSet(Activity activity) {
  String name = Build.MANUFACTURER;
  switch (name) {
   case "HUAWEI":
    goHuaWeiManager(activity);
    break;
   case "vivo":
    goVivoManager(activity);
    break;
   case "OPPO":
    goOppoManager(activity);
    break;
   case "Coolpad":
    goCoolpadManager(activity);
    break;
   case "Meizu":
    goMeizuManager(activity);
    break;
   case "Xiaomi":
    goXiaoMiManager(activity);
    break;
   case "samsung":
    goSangXinManager(activity);
    break;
   case "Sony":
    goSonyManager(activity);
    break;
   case "LG":
    goLGManager(activity);
    break;
   default:
    goIntentSetting(activity);
    break;
  }
 }

 private void goLGManager(Activity activity){
  try {
   Intent intent = new Intent(packageName);
   ComponentName comp = new ComponentName("com.android.settings", "com.android.settings.Settings$AccessLockSummaryActivity");
   intent.setComponent(comp);
   activity.startActivity(intent);
  } catch (Exception e) {
   ToastUtils.showLong("跳转失败");
   e.printStackTrace();
   goIntentSetting(activity);
  }
 }
 private void goSonyManager(Activity activity){
  try {
   Intent intent = new Intent(packageName);
   ComponentName comp = new ComponentName("com.sonymobile.cta", "com.sonymobile.cta.SomcCTAMainActivity");
   intent.setComponent(comp);
   activity.startActivity(intent);
  } catch (Exception e) {
   ToastUtils.showLong("跳转失败");
   e.printStackTrace();
   goIntentSetting(activity);
  }
 }

 private void goHuaWeiManager(Activity activity) {
  try {
   Intent intent = new Intent(packageName);
   intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
   ComponentName comp = new ComponentName("com.huawei.systemmanager", "com.huawei.permissionmanager.ui.MainActivity");
   intent.setComponent(comp);
   activity.startActivity(intent);
  } catch (Exception e) {
   ToastUtils.showLong("跳转失败");
   e.printStackTrace();
   goIntentSetting(activity);
  }
 }

 private static String getMiuiVersion(Activity activity) {
  String propName = "ro.miui.ui.version.name";
  String line;
  BufferedReader input = null;
  try {
   Process p = Runtime.getRuntime().exec("getprop " + propName);
   input = new BufferedReader(
           new InputStreamReader(p.getInputStream()), 1024);
   line = input.readLine();
   input.close();
  } catch (IOException ex) {
   ex.printStackTrace();
   return null;
  } finally {
   try {
    input.close();
   } catch (IOException e) {
    e.printStackTrace();
   }
  }
  return line;
 }

 private void goXiaoMiManager(Activity activity) {
  String rom = getMiuiVersion(activity);
  Intent intent=new Intent();
  if ("V6".equals(rom) || "V7".equals(rom)) {
   intent.setAction("miui.intent.action.APP_PERM_EDITOR");
   intent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.AppPermissionsEditorActivity");
   intent.putExtra("extra_pkgname", packageName);
  } else if ("V8".equals(rom) || "V9".equals(rom)) {
   intent.setAction("miui.intent.action.APP_PERM_EDITOR");
   intent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.PermissionsEditorActivity");
   intent.putExtra("extra_pkgname", packageName);
  } else {
   goIntentSetting(activity);
  }
 }

 private void goMeizuManager(Activity activity) {
  try {
   Intent intent = new Intent("com.meizu.safe.security.SHOW_APPSEC");
   intent.addCategory(Intent.CATEGORY_DEFAULT);
   intent.putExtra("packageName", packageName);
   activity.startActivity(intent);
  } catch (ActivityNotFoundException localActivityNotFoundException) {
   localActivityNotFoundException.printStackTrace();
   goIntentSetting(activity);
  }
 }

 private void goSangXinManager(Activity activity) {
  //三星4.3可以直接跳转
  goIntentSetting(activity);
 }

 private void goIntentSetting(Activity activity) {
  Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
  Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
  intent.setData(uri);
  try {
   activity.startActivity(intent);
  } catch (Exception e) {
   e.printStackTrace();
  }
 }

 private void goOppoManager(Activity activity) {
  doStartApplicationWithPackageName("com.coloros.safecenter",activity);
 }

 /**
  * doStartApplicationWithPackageName("com.yulong.android.security:remote")
  * 和Intent open = getPackageManager().getLaunchIntentForPackage("com.yulong.android.security:remote");
  * startActivity(open);
  */
 private void goCoolpadManager(Activity activity) {
  doStartApplicationWithPackageName("com.yulong.android.security:remote",activity);
      /*  Intent openQQ = getPackageManager().getLaunchIntentForPackage("com.yulong.android.security:remote");
        startActivity(openQQ);*/
 }

 private void goVivoManager(Activity activity) {
  doStartApplicationWithPackageName("com.bairenkeji.icaller",activity);
     /*   Intent openQQ = getPackageManager().getLaunchIntentForPackage("com.vivo.securedaemonservice");
        startActivity(openQQ);*/
 }

 private void doStartApplicationWithPackageName(String packagename,Activity activity) {
  // 通过包名获取此APP详细信息，包括Activities、services、versioncode、name等等
  PackageInfo packageinfo = null;
  try {
   packageinfo = activity.getPackageManager().getPackageInfo(packagename, 0);
  } catch (PackageManager.NameNotFoundException e) {
   e.printStackTrace();
  }
  if (packageinfo == null) {
   return;
  }
  // 创建一个类别为CATEGORY_LAUNCHER的该包名的Intent
  Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
  resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
  resolveIntent.setPackage(packageinfo.packageName);
  // 通过getPackageManager()的queryIntentActivities方法遍历
  List<ResolveInfo> resolveinfoList = activity.getPackageManager()
          .queryIntentActivities(resolveIntent, 0);
  System.out.println("resolve info List" + resolveinfoList.size());
  for (int i = 0; i < resolveinfoList.size(); i++) {
   System.out.println("PermissionPageManager"+resolveinfoList.get(i).activityInfo.packageName + resolveinfoList.get(i).activityInfo.name);
  }
  ResolveInfo resolveinfo = resolveinfoList.iterator().next();
  if (resolveinfo != null) {
   // packageName参数2 = 参数 packname
   String packageName = resolveinfo.activityInfo.packageName;
   // 该APP的LAUNCHER的Activity[组织形式：packageName参数2.mainActivityName]
   String className = resolveinfo.activityInfo.name;
   // LAUNCHER Intent
   Intent intent = new Intent(Intent.ACTION_MAIN);
   intent.addCategory(Intent.CATEGORY_LAUNCHER);
   // 设置ComponentName参数1:packageName参数2:MainActivity路径
   ComponentName cn = new ComponentName(packageName, className);
   intent.setComponent(cn);
   try {
    activity.startActivity(intent);
   } catch (Exception e) {
    goIntentSetting(activity);
    e.printStackTrace();
   }
  }
 }
}
