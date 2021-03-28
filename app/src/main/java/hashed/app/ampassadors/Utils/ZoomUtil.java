package hashed.app.ampassadors.Utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class ZoomUtil {

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

  public static void createMeeting(String topic, int duration, String description,
                                    Response.Listener<JSONObject> responseListener,
                                    ProgressDialog zoomDialog, Context context){

    final RequestQueue queue = Volley.newRequestQueue(context);

    new Thread(new Runnable() {
      @Override
      public void run() {
        final JSONObject jsonBodyObj = new JSONObject();

        try {
          jsonBodyObj.put("topic", topic);
          jsonBodyObj.put("type", 1);
          jsonBodyObj.put("duration", duration);
          jsonBodyObj.put("agenda", description);

        } catch (JSONException e) {
          e.printStackTrace();
        }

        final String url = ZoomRequester.BASE_URL +
                "/users/"+ ZoomRequester.REQUESTER_EMAIL+"/meetings";


        final String requestBody = jsonBodyObj.toString();

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST,url,
                null, responseListener,
                new Response.ErrorListener() {
          @Override
          public void onErrorResponse(VolleyError error) {
            zoomDialog.dismiss();
            Toast.makeText(context, "Meeting creation failed! Please try again",
                    Toast.LENGTH_SHORT).show();

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
        queue.start();

      }
    }).start();

  }


  private static Map<String,String> getAuthHeaders(){
    Map<java.lang.String, java.lang.String> params = new HashMap<>();
    params.put("Authorization", "Bearer " + ZoomRequester.JWT_TOKEN);
    params.put("Content-Type", "application/json");
    return params;
  }


}
