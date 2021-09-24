package hashed.app.ampassadors.Utils;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import hashed.app.ampassadors.Objects.ZoomMeeting;

public class ZoomRequestCreator {

    public static final int ZOOM_MEETING_ENDED = 1;

    public interface ZoomRequester {

        // Endpoints
        String BASE_URL = "https://api.zoom.us/v2/";
          String REQUESTER_EMAIL = "info@icspr.ps";
//        String REQUESTER_EMAIL = "yousefaimanjarada@hotmail.com";
        //  String USER_URL = "/users/";
//  String MEETING_URL = "/meetings";
        String JWT_TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOm51bGwsImlzcyI6IjlHZndUV3hFVDMySG5jMHpySnM4aHciLCJleHAiOjE2NzIzODcyMDAsImlhdCI6MTYyOTQ3MzU1MH0.V022BU2SRzx9afuqlYj7oiJwGI2l8tQR7fPDgG0SLT4";
//        String JWT_TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOm51bGwsImlzcyI6InhCbU1lRVVOUWJLNFAxQnFHUUNTYmciLCJleHAiOjE2MzAwNjM5OTAsImlhdCI6MTYyOTk3NzYwOH0.7pvso_g5tLh0ctFm91LKhFO4LsHTzuPxrWiKydj9Nvg";

//        @Headers({"Content-Type: application/json"})
//        @POST("/users/{userId}/meetings/")
//        Call<ZoomMeeting> createZoomMeeting(@Path("userId") String userId, @Body byte[] zoomRequest
//                , @Header("Authorization") String authHeader
//        );

    }

//  public final static int CREATE_MEETING_REQUEST = 1,GET_MEETING_REQUEST = 2;
//
//  public final static String CREATE_MEETING_URL = ZoomRequester.BASE_URL + ZoomRequester.USER_URL +
//          ZoomRequester.REQUESTER_EMAIL + ZoomRequester.MEETING_URL;
//
//  private static void getMeeting(long id,Context context,
//                                 Response.Listener<JSONObject> responseListener){
//
//    final RequestQueue queue = Volley.newRequestQueue(context);
//
//    new Thread(new Runnable() {
//      @Override
//      public void run() {
//
//
//        final String url = ZoomRequester.BASE_URL +
//                "/metrics/meetings/"+ id + ZoomRequester.REQUESTER_EMAIL+"";
//
//        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET,
//                ZoomUtil.CREATE_MEETING_URL, null, responseListener,
//                new Response.ErrorListener() {
//                  @Override
//                  public void onErrorResponse(VolleyError error) {
//                  }
//                }) {
//          @Override
//          public Map<String, String> getHeaders() throws AuthFailureError {
//            return getAuthHeaders();
//          }
//        };
//
//        queue.add(request);
//        queue.start();
//
//      }
//    }).start();
//
//  }

    private final MutableLiveData<ZoomMeeting> zoomMeeting;
    private final String topic,description;

    public ZoomRequestCreator(String topic, String description){
        this.topic = topic;
        this.description = description;

        zoomMeeting = new MutableLiveData<>();
    }


    public MutableLiveData<ZoomMeeting> createMeeting(Context context){

//    final OkHttpClient client = new OkHttpClient.Builder().addInterceptor(new Interceptor() {
//      @Override
//      public okhttp3.Response intercept(@NonNull Chain chain) throws IOException {
//
//        okhttp3.Request request = chain.request().newBuilder()
//                .addHeader("Authorization","Bearer " + ZoomRequester.JWT_TOKEN)
//                .addHeader("Content-Type","application/json").build();
//
//        return chain.proceed(request);
//      }
//    }).build();
//
//
//    Retrofit retrofit = new Retrofit.Builder()
//            .client(client)
//            .baseUrl(ZoomRequester.BASE_URL)
//            .addConverterFactory(GsonConverterFactory.create())
//            .build();
//
//
//        Map<String,String> requestMap = new HashMap<>();
//        requestMap.put("topic",topic);
//        requestMap.put("type","1");
//        requestMap.put("agenda",description);
//
//        RequestBody body = RequestBody.create(MediaType.parse("application/octet-stream"),(new JSONObject(requestMap)).toString());
//
//
//        retrofit.create(ZoomRequester.class)
//                .createZoomMeeting(ZoomRequester.REQUESTER_EMAIL, new JSONObject(requestMap).toString().getBytes(StandardCharsets.UTF_8)
//                        ,ZoomRequester.JWT_TOKEN
//                )
//                .enqueue(new Callback<ZoomMeeting>() {
//                  @Override
//                  public void onResponse(Call<ZoomMeeting> call, retrofit2.Response<ZoomMeeting> response) {
//
//                    if(response.isSuccessful() && response.body()!=null){
//
//
//                        zoomMeeting.setValue(response.body());
//
//                    }else{
//
//                        zoomMeeting.postValue(null);
//
//                        if(response.errorBody()!=null){
//
//                            Log.d("ttt","zoom meeting creationg failed1 : "+
//                                    response.errorBody().toString());
//
//                            Log.d("ttt","response.raw(): "+response.raw());
//                        }
//
//                    }
//                  }
//
//                  @Override
//                  public void onFailure(Call<ZoomMeeting> call, Throwable t) {
//
//                      Log.d("ttt","zoom meeting creationg failed 2: "+
//                              t.getMessage());
//
//                      zoomMeeting.postValue(null);
//                  }
//                });

    final RequestQueue queue = Volley.newRequestQueue(context);

    new Thread(new Runnable() {
      @Override
      public void run() {
        final JSONObject jsonBodyObj = new JSONObject();

        try {
          jsonBodyObj.put("topic", topic);
          jsonBodyObj.put("type", 1);
//          jsonBodyObj.put("duration", duration);
          jsonBodyObj.put("agenda", description);

        } catch (JSONException e) {
          e.printStackTrace();
        }

        final String url = ZoomRequester.BASE_URL +
                "/users/"+ ZoomRequester.REQUESTER_EMAIL+"/meetings";


        final String requestBody = jsonBodyObj.toString();

        JsonObjectRequest request = new JsonObjectRequest(com.android.volley.Request.Method.POST, url,
                null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                Gson gson=new Gson();
                zoomMeeting.setValue(gson.fromJson(response.toString(),ZoomMeeting.class));

            }
        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        zoomMeeting.postValue(null);
                        Log.d("ttt", "creating meeting failed: " + error.toString());
                    }
                }) {
          @Override
          public Map<String, String> getHeaders() throws AuthFailureError {
           return getAuthHeaders();
          }

          @Override
          public byte[] getBody() {
            return requestBody.getBytes(StandardCharsets.UTF_8);
          }
        };

        queue.add(request);

      }
    }).start();

        return zoomMeeting;
  }


  private static Map<String,String> getAuthHeaders(){
    Map<java.lang.String, java.lang.String> params = new HashMap<>();
    params.put("Authorization", "Bearer " + ZoomRequester.JWT_TOKEN);
    params.put("Content-Type", "application/json");
    return params;
  }


}
