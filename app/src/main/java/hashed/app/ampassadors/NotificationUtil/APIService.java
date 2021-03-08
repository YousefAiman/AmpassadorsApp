package hashed.app.ampassadors.NotificationUtil;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
  @Headers(
          {
                  "Content-Type:application/json",
                  "Authorization:key=AAAAEcJ1eK0:APA91bFeLN1ngIQYT-6rQIp6lW2V_eGlcBDCczHkxdUY8Pu6IuhcXlkjJZIVEZmmfZQ9xdfBG8qwxMHMAh_rL6uaqqmefDLz_S6Nh6eVCwE44RV-PSKogDdzae96wXhioVkdEcf5B-9x"
          }
  )
  @POST("fcm/send")
  Call<MyResponse> sendNotification(@Body Sender body);
}
