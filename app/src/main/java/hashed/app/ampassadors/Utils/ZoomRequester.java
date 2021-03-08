package hashed.app.ampassadors.Utils;

import hashed.app.ampassadors.Objects.ZoomMeetingResponse;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface ZoomRequester {

  // Endpoints
  String BASE_URL = "https://api.zoom.us/v2";
  String REQUESTER_EMAIL = "yousefaimanjarada@hotmail.com";
  String USER_URL = "/users/";
  String MEETING_URL = "/meetings";

  String JWT_TOKEN = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJhdWQiOm51bGwsImlzcyI6InhCbU1lRVVOUWJLNFAxQnFHUUNTYmciLCJleHAiOjE2MTU4MzkzMzQsImlhdCI6MTYxNTIzNDUzNX0.MTJbm3x5FwsFXK5Ob8sVYp6j6OX9U8npCkrJWMAKJCs";
//
//  @Headers({
//          "Content-type: application/json",
//          "Authorization:Bearer Token="
//  })
//  @FormUrlEncoded
//  @POST("/meetings")
//  Call<ZoomMeetingResponse> createMeeting(@Field("userId") String userId, @Field("topic") String topic, @Field("type") Integer type);


}
