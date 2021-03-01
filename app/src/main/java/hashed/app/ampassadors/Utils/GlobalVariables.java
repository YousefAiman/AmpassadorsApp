package hashed.app.ampassadors.Utils;

import android.app.Application;
import android.net.ConnectivityManager.NetworkCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GlobalVariables extends Application {

  private static GlobalVariables globalSingleton;
  private static List<String> likesList = new ArrayList<>();


  public static GlobalVariables getInstance() {
    return globalSingleton;
  }

  public static List<String> getLikesList() {
    return likesList;
  }

  public static void setLikesList(List<String> likesList) {
    GlobalVariables.likesList = likesList;
  }


  @Override
  public void onCreate() {
    super.onCreate();
    globalSingleton = this;
  }

}
