package hashed.app.ampassadors.NotificationUtil;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import hashed.app.ampassadors.Utils.GlobalVariables;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CloudMessagingNotificationsSender {

  //  private static final String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
  private static final APIService apiService =
          Client.getClient("https://fcm.googleapis.com/").create(APIService.class);


  public static void sendNotification(String toUid, Data data) {

    if (data != null) {

      Log.d("ttt", "sending to userid: " + toUid);
      FirebaseFirestore.getInstance().collection("Users").document(toUid)
              .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
        @Override
        public void onSuccess(DocumentSnapshot documentSnapshot) {

          if (documentSnapshot.contains("token")) {

            final String token = documentSnapshot.getString("token");

            if (GlobalVariables.getCurrentToken() != null &&
                    GlobalVariables.getCurrentToken().equals(token))
              return;

            Log.d("ttt", "sending to token: " + token);
            Sender sender = new Sender(data, token);
//            String msgID = token + System.currentTimeMillis();
//
//            RemoteMessage.Builder RMBuilder =
//                    new RemoteMessage.Builder(token + "@gcm.googleapis.com");
//            RMBuilder.setMessageId(msgID);
//
//            Map<String, Object> mapData = new HashMap<>();
//            mapData.put("sender",data.getSender());
//            mapData.put("body",data.getBody());
//            mapData.put("title",data.getTitle());
//            mapData.put("image",data.getImage());
//            mapData.put("type",data.getType());
//            mapData.put("source",data.getSource());
//            mapData.put("identifier",data.getIdentifier());
//
//            for (Map.Entry<String, Object> entry : mapData.entrySet()) {
//              RMBuilder.addData(entry.getKey(), entry.getValue().toString());
//            }
//
//            FirebaseMessaging.getInstance().send(RMBuilder.build());
            apiService.sendNotification(sender).enqueue(new Callback<MyResponse>() {
              @Override
              public void onResponse(@NonNull Call<MyResponse> call,
                                     @NonNull Response<MyResponse> response) {

                Log.d("ttt", "notification send: " + response.message());
              }

              @Override
              public void onFailure(@NonNull Call<MyResponse> call, @NonNull Throwable t) {
                Log.d("ttt", "notification send error: " + t.getMessage());
              }
            });


          } else {
            Log.d("ttt", "user has no token");
          }
        }
      });

    }

  }

}
