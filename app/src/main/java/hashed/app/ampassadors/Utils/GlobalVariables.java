package hashed.app.ampassadors.Utils;

import android.app.Application;
import android.net.ConnectivityManager.NetworkCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hashed.app.ampassadors.BroadcastReceivers.WifiReceiver;

public class GlobalVariables extends Application {

  private static GlobalVariables globalSingleton;
  private static List<String> likesList;
  private static String currentUsername;
  private static String currentUserImageUrl;
  private static String currentToken;
  private static Map<String, Integer> messagesNotificationMap;
  private static boolean appIsRunning;
  private static WifiReceiver currentWifiReceiver;
  private static boolean wifiIsOn;
  private static NetworkCallback registeredNetworkCallback;
  private static int notificationsCount;
  private static String role ;


  public static GlobalVariables getInstance() {
    return globalSingleton;
  }

  public static List<String> getLikesList() {
    if(likesList == null)
      likesList = new ArrayList<>();

    return likesList;
  }

  public static void setLikesList(List<String> likesList) {
    GlobalVariables.likesList = likesList;
  }

  public static String getCurrentUsername() {
    return currentUsername;
  }

  public static void setCurrentUsername(String currentUsername) {
    GlobalVariables.currentUsername = currentUsername;
  }

  public static String getCurrentUserImageUrl() {
    return currentUserImageUrl;
  }

  public static void setCurrentUserImageUrl(String currentUserImageUrl) {
    GlobalVariables.currentUserImageUrl = currentUserImageUrl;
  }

  public static String getCurrentToken() {
    return currentToken;
  }

  public static void setCurrentToken(String currentToken) {
    GlobalVariables.currentToken = currentToken;
  }

  public static Map<String, Integer> getMessagesNotificationMap() {

    if(messagesNotificationMap == null)
      messagesNotificationMap = new HashMap<>();

    return messagesNotificationMap;
  }

  public static void setMessagesNotificationMap(Map<String, Integer> messagesNotificationMap) {
    GlobalVariables.messagesNotificationMap = messagesNotificationMap;
  }

  public static boolean isAppIsRunning() {
    return appIsRunning;
  }

  public static void setAppIsRunning(boolean appIsRunning) {
    GlobalVariables.appIsRunning = appIsRunning;
  }

  public static WifiReceiver getCurrentWifiReceiver() {
    return currentWifiReceiver;
  }

  public static void setCurrentWifiReceiver(WifiReceiver currentWifiReceiver) {
    GlobalVariables.currentWifiReceiver = currentWifiReceiver;
  }

  public static boolean isWifiIsOn() {
    return wifiIsOn;
  }

  public static void setWifiIsOn(boolean wifiIsOn) {
    GlobalVariables.wifiIsOn = wifiIsOn;
  }

  public static NetworkCallback getRegisteredNetworkCallback() {
    return registeredNetworkCallback;
  }

  public static void setRegisteredNetworkCallback(NetworkCallback registeredNetworkCallback) {
    GlobalVariables.registeredNetworkCallback = registeredNetworkCallback;
  }

  public static int getNotificationsCount() {
    return notificationsCount;
  }

  public static void setNotificationsCount(int notificationsCount) {
    GlobalVariables.notificationsCount = notificationsCount;
  }

  public static String getRole() {
    return role;
  }

  public static void setRole(String role) {
    GlobalVariables.role = role;
  }


  @Override
  public void onCreate() {
    super.onCreate();
    globalSingleton = this;
  }

}
