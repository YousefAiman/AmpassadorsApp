package hashed.app.ampassadors.Fragments;

import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;
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

import hashed.app.ampassadors.Activities.GroupMessagingActivity;
import hashed.app.ampassadors.Objects.ZoomMeeting;
import hashed.app.ampassadors.R;
import hashed.app.ampassadors.Utils.ZoomRequester;
import hashed.app.ampassadors.Utils.ZoomUtil;


public class ZoomMeetingCreationFragment extends Fragment implements View.OnClickListener {

  private EditText topicEd;
  private EditText descriptionEd;
  private NumberPicker meetingDurationNumberPicker;
  private TextView zoomLinkTv;
  private Button createMeetBtn;
  private EditText messagingPickerEd;
  private ImageView messagingPickerSendIv;
  private ZoomMeeting zoomMeeting;

  public ZoomMeetingCreationFragment() {
    // Required empty public constructor
  }


  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_zoom_meeting_creation, container,
            false);

    final Toolbar fullScreenToolbar = view.findViewById(R.id.fullScreenToolbar);

    fullScreenToolbar.setNavigationOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        getActivity().onBackPressed();
      }
    });

    topicEd = view.findViewById(R.id.topicEd);
    descriptionEd = view.findViewById(R.id.descriptionEd);
    meetingDurationNumberPicker = view.findViewById(R.id.meetingDurationNumberPicker);
    zoomLinkTv = view.findViewById(R.id.zoomLinkTv);
    createMeetBtn = view.findViewById(R.id.createMeetBtn);
    messagingPickerEd = view.findViewById(R.id.messagingPickerEd);
    messagingPickerSendIv = view.findViewById(R.id.messagingPickerSendIv);

    meetingDurationNumberPicker.setMinValue(1);
    meetingDurationNumberPicker.setMaxValue(40);

    return view;
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    initializeClickers();

  }


  private void initializeClickers(){

    zoomLinkTv.setOnClickListener(this);
    createMeetBtn.setOnClickListener(this);
    messagingPickerSendIv.setOnClickListener(this);

  }


  @Override
  public void onClick(View view) {
    if(view.getId() == R.id.zoomLinkTv){

      if(!zoomLinkTv.getText().toString().trim().isEmpty()){

        final ClipboardManager clipboard = (ClipboardManager)
                getContext().getSystemService(Context.CLIPBOARD_SERVICE);

        clipboard.setPrimaryClip(
                ClipData.newPlainText("zoomJoinUrl",zoomLinkTv.getText().toString()));

        Toast.makeText(getContext(),
                "Meeting url has been copied to the clipboard!",
                Toast.LENGTH_SHORT).show();

      }else{

        Toast.makeText(getContext(),
                "You need to create a meeting to get the meeting join url",
                Toast.LENGTH_SHORT).show();

      }

    }else if(view.getId() == R.id.createMeetBtn){

      requestMeetingCreation();

    }else if(view.getId() == R.id.messagingPickerSendIv){

      final String message = messagingPickerEd.getText().toString();
      if(!message.isEmpty() && zoomMeeting != null){
        ((GroupMessagingActivity)requireActivity()).sendZoomMessage(message,zoomMeeting);
      }
    }
  }

  private void requestMeetingCreation(){

    final String topic = topicEd.getText().toString();
    final String description = descriptionEd.getText().toString();
    final int duration = meetingDurationNumberPicker.getValue();

    if(!topic.isEmpty() && !description.isEmpty()){
      createMeetBtn.setClickable(false);

      final ProgressDialog zoomDialog = new ProgressDialog(getContext());
      zoomDialog.setTitle("Creating meeting!");
      zoomDialog.setCancelable(false);
      zoomDialog.show();

      final Response.Listener<JSONObject> responseListener = response -> {
        try {

          zoomMeeting = new ZoomMeeting(
                          response.getString("id"),
                          response.getString("host_id"),
                          response.getString("host_email"),
                          response.getString("topic"),
                          duration,
                          response.getString("status"),
                          response.getString("start_url"),
                          response.getString("join_url"));

          Log.d("ttt","zoomMeeting: "+ zoomMeeting.toString());

          Log.d("ttt","zoomMeeting: "+ zoomMeeting.toString());

          zoomLinkTv.post(()-> zoomLinkTv.setText(zoomMeeting.getJoinUrl()));

          createMeetBtn.post(()-> createMeetBtn.setVisibility(View.INVISIBLE));

          zoomDialog.dismiss();

        } catch (JSONException e) {

          zoomDialog.dismiss();
          createMeetBtn.setClickable(true);
          Toast.makeText(getContext(), "Meeting creation failed! Please try again",
                  Toast.LENGTH_SHORT).show();
          Log.d("ttt","JSONException: "+e.getMessage());
          e.printStackTrace();
        }
      };

      final RequestQueue queue = Volley.newRequestQueue(getContext());

      new Thread(new Runnable() {
        @Override
        public void run() {

          final JSONObject jsonBodyObj = new JSONObject();

          try{

            jsonBodyObj.put("topic", topic);
            jsonBodyObj.put("type", 1);
            jsonBodyObj.put("duration", duration);
            jsonBodyObj.put("agenda", description);

          }catch (JSONException e){
            e.printStackTrace();
          }

          final String requestBody = jsonBodyObj.toString();

          JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST,
                  ZoomUtil.url, null,responseListener, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

              createMeetBtn.setClickable(true);
              zoomDialog.dismiss();


              Toast.makeText(getContext(), "Meeting creation failed! Please try again",
                      Toast.LENGTH_SHORT).show();

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

          queue.add(request);
          queue.start();

        }
      }).start();


    }else{
      Toast.makeText(getContext(), "Please fill in the fields!", Toast.LENGTH_SHORT).show();
    }
  }





}