package hashed.app.ampassadors.Utils;

import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.List;

import hashed.app.ampassadors.BroadcastReceivers.WifiReceiver;
import hashed.app.ampassadors.R;

public class WifiUtil {

  static boolean checkWifiConnection(Context context) {
    if (!GlobalVariables.isWifiIsOn()) {
      Toast.makeText(context, R.string.please_check_your_internet_connection_and_try_again,
              Toast.LENGTH_SHORT).show();
      return false;
    }
    return true;
  }

  @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
  static void registerNetworkCallback(ConnectivityManager cm){

    final NetworkRequest.Builder builder = new NetworkRequest.Builder();

    builder.addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);


    builder.addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR);


    ConnectivityManager.NetworkCallback networkCallback;


    cm.registerNetworkCallback(builder.build(),
            networkCallback = new ConnectivityManager.NetworkCallback() {

              final List<Network> activeNetworks = new ArrayList<>();

              @Override
              public void onUnavailable() {
                super.onUnavailable();
                Log.d("ttt","no internet avilalblble man");
              }

              @Override
              public void onAvailable(@NonNull Network network) {
                super.onAvailable(network);

                Log.d("ttt","network onAvailable");

                if(!activeNetworks.contains(network)){
                  Log.d("ttt","adding netowrk to list: "+network.toString());
                  activeNetworks.add(network);
                }

                if(activeNetworks.size() > 0){

                  if(!GlobalVariables.isWifiIsOn()){
//                    FirebaseFirestore.getInstance().enableNetwork()
//                            .addOnSuccessListener(new OnSuccessListener<Void>() {
//                              @Override
//                              public void onSuccess(Void aVoid) {
//                                Log.d("ttt","enabled netowrk man hehe");
//                              }
//                            });

                    GlobalVariables.setWifiIsOn(true);
                  }

                  Log.d("ttt","network is on man");


                }

              }
              @Override
              public void onLost(@NonNull Network network) {
                super.onLost(network);

                if(activeNetworks.contains(network)){
                  Log.d("ttt","removing netowrk from list: "+network.toString());
                  activeNetworks.remove(network);
                }

                if(activeNetworks.size() == 0){

                  if(GlobalVariables.isWifiIsOn()){
                    GlobalVariables.setWifiIsOn(false);
                  }

                  Log.d("ttt", "wifi offline: "+network.toString());

                }


              }
            });


    GlobalVariables.setRegisteredNetworkCallback(networkCallback);


  }

  static void registerReceiver(Context context){

    final IntentFilter intentFilter = new IntentFilter();
    intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
    final WifiReceiver wifiReceiver = new WifiReceiver();
    GlobalVariables.setCurrentWifiReceiver(wifiReceiver);
    context.registerReceiver(wifiReceiver, intentFilter);

  }

  public static boolean isConnectedToInternet(Context context) {

    final ConnectivityManager cm = (ConnectivityManager) context
            .getSystemService(Context.CONNECTIVITY_SERVICE);

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

      final NetworkCapabilities capabilities = cm.getNetworkCapabilities(cm.getActiveNetwork());

      if(capabilities != null &&
              (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                      || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                      || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET))){

        registerNetworkCallback(cm);

        return true;
      }else{

        return false;
      }

    } else {

      final NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

      if(activeNetwork != null && activeNetwork.isConnected()){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
          registerNetworkCallback(cm);
        }else{
          registerReceiver(context);
        }

        return true;
      }else{

        return false;

      }

    }
  }

}
