package hashed.app.ampassadors.NotificationUtil;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import java.util.List;

import hashed.app.ampassadors.BuildConfig;

public class BadgeUtil {

  static int notificationCount = 0;

  public static void setBadge(Context context, int count) {
    notificationCount = count;
    String launcherClassName = getLauncherClassName(context);
    if (launcherClassName == null) {
      return;
    }
    Intent intent = new Intent("android.intent.action.BADGE_COUNT_UPDATE");
    intent.putExtra("badge_count", count);
    intent.putExtra("badge_count_package_name", BuildConfig.APPLICATION_ID);
    intent.putExtra("badge_count_class_name", launcherClassName);
    context.sendBroadcast(intent);

  }


  public static void clearBadge(Context context) {
    setBadge(context, 0);
  }

  public static void incrementBadgeNum(Context context) {
    setBadge(context, ++notificationCount);
  }

  public static void decrementBadgeNum(Context context) {
    setBadge(context, --notificationCount);
  }

  private static String getLauncherClassName(Context context) {

    PackageManager pm = context.getPackageManager();

    Intent intent = new Intent(Intent.ACTION_MAIN);
    intent.addCategory(Intent.CATEGORY_LAUNCHER);

    List<ResolveInfo> resolveInfos = pm.queryIntentActivities(intent, 0);
    for (ResolveInfo resolveInfo : resolveInfos) {
      String pkgName = resolveInfo.activityInfo.applicationInfo.packageName;
      if (pkgName.equalsIgnoreCase(context.getPackageName())) {
        return resolveInfo.activityInfo.name;
      }
    }
    return null;
  }


}