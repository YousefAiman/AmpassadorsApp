package hashed.app.ampassadors.Utils;

import android.content.Context;
import android.util.Log;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import hashed.app.ampassadors.Objects.ZoomMeetingResponse;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public class ZoomUtil {

  public static void createZoomMeeting(Context context, String topic, int duration, String description,
                                       TextView joinTv,TextView startTv){

    final RequestQueue queue = Volley.newRequestQueue(context.getApplicationContext());

    final JSONObject jsonBodyObj = new JSONObject();

    final String url = ZoomRequester.BASE_URL + ZoomRequester.USER_URL +
             ZoomRequester.REQUESTER_EMAIL+ ZoomRequester.MEETING_URL;

//    final String startTime =
//            TimeFormatter.formatWithPattern(System.currentTimeMillis(),
//                    "yyyy-MM-dd`T`HH:mm:ssZ");

    try{

      jsonBodyObj.put("topic", topic);
      jsonBodyObj.put("type", 1);
      jsonBodyObj.put("duration", duration);
      jsonBodyObj.put("agenda", description);

    }catch (JSONException e){
      e.printStackTrace();
    }

    final String requestBody = jsonBodyObj.toString();

    new Thread(new Runnable() {
      @Override
      public void run() {

    JsonObjectRequest meetingRequest = new JsonObjectRequest(Request.Method.POST,
            url, null, new Response.Listener<JSONObject>() {
      @Override
      public void onResponse(JSONObject response) {


        try {

          ZoomMeetingResponse zoomMeetingResponse =
                  new ZoomMeetingResponse(
                          response.getString("id"),
                          response.getString("host_id"),
                          response.getString("host_email"),
                          response.getString("topic"),
                          response.getString("status"),
                          response.getString("start_url"),
                          response.getString("join_url"));

          Log.d("ttt","zoomMeetingResponse: "+ zoomMeetingResponse.toString());

          joinTv.post(()->joinTv.setText(zoomMeetingResponse.getJoinUrl()));
          startTv.post(()->startTv.setText(zoomMeetingResponse.getStartUrl()));

        } catch (JSONException e) {

          Log.d("ttt","JSONException: "+e.getMessage());
          e.printStackTrace();
        }

      }
    }, new Response.ErrorListener() {
      @Override
      public void onErrorResponse(VolleyError error) {
        Log.d("ttt","creating meeting failed: "+error.toString());
      }
    }){
      @Override
      public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String> params = new HashMap<>();
        params.put("Authorization", "Bearer "+ ZoomRequester.JWT_TOKEN);
        params.put("Content-Type", "application/json");
        return params;
      }

      @Override
      public byte[] getBody() {
        return requestBody.getBytes(StandardCharsets.UTF_8);
      }

    };


    queue.add(meetingRequest);
    queue.start();

      }
    });

  }


}
