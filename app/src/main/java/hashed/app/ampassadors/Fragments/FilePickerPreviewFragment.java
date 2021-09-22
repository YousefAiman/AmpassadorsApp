package hashed.app.ampassadors.Fragments;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.squareup.picasso.Picasso;

import java.util.Map;

import hashed.app.ampassadors.Activities.MessagingActivities.MessagingActivity;
import hashed.app.ampassadors.R;
import hashed.app.ampassadors.Utils.Files;

public class FilePickerPreviewFragment extends Fragment {


  private final Uri uri;
  private final int type;

  public FilePickerPreviewFragment(Uri uri, int type) {
    this.uri = uri;
    this.type = type;
  }


  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater,
                           @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {

    int layout;
    switch (type) {
      case Files.IMAGE:

      case Files.AUDIO:
        layout = R.layout.fragment_image_picker_preview;
        break;

      case Files.DOCUMENT:
        layout = R.layout.fragment_file_picker_preview;
        break;

      default:
        throw new IllegalStateException("Unexpected value: " + type);
    }

    return inflater.inflate(layout, container, false);

  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    ImageView fullScreenIv = view.findViewById(R.id.fullScreenIv);
    EditText messagingPickerEd = view.findViewById(R.id.messagingPickerEd);
    ImageView messagingPickerSendIv = view.findViewById(R.id.messagingPickerSendIv);


    final Toolbar fullScreenToolbar = view.findViewById(R.id.fullScreenToolbar);

    fullScreenToolbar.setNavigationOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        getActivity().onBackPressed();
      }
    });

    if (type == Files.IMAGE) {
      Picasso.get().load(uri).fit().into(fullScreenIv);

      messagingPickerSendIv.setOnClickListener(new sendClickListener(messagingPickerEd));

    } else if (type == Files.DOCUMENT) {

      TextView documentNameTv = view.findViewById(R.id.documentNameTv);

      Map<String, Object> fileInfoMap = Files.getFileInfo(getContext(), uri);

      final String fileName = (String) fileInfoMap.get("fileName");

//        final String fileType = (String) fileInfoMap.get("fileType");
//        Log.d("ttt","fileName: "+fileName);
//        Log.d("ttt","fileType: "+fileType);
      documentNameTv.setText(fileName);

      messagingPickerSendIv.setOnClickListener(new sendClickListener(messagingPickerEd, fileName));

    }


  }

  private class sendClickListener implements View.OnClickListener {
    EditText messagingPickerEd;
    String fileName;

    sendClickListener(EditText messagingPickerEd) {
      this.messagingPickerEd = messagingPickerEd;
    }

    sendClickListener(EditText messagingPickerEd, String fileName) {
      this.messagingPickerEd = messagingPickerEd;
      this.fileName = fileName;
    }

    @Override
    public void onClick(View view) {

      final String content = messagingPickerEd.getText().toString().trim();
      if (uri != null) {

        if(getActivity()!=null){

          ((MessagingActivity) getActivity()).sendFileMessage(
                  uri,
                  type,
                  !content.isEmpty()?content:"",
                  0,
                  fileName);

          getActivity().onBackPressed();
        }

//        if (getActivity() instanceof PrivateMessagingActivity) {
//
//          ((MessagingActivity) getActivity()).sendFileMessage(
//                  uri,
//                  type,
//                  content,
//                  0,
//                  fileName);
//
//        } else if (getActivity() instanceof GroupMessagingActivity) {
//
//          ((MessagingActivity) getActivity()).sendFileMessage(
//                  uri,
//                  type,
//                  content,
//                  0,
//                  fileName);
//
//        }



      } else {

        //problem with image

      }
    }
  }

//  @Override
//  public void onResume() {
//    super.onResume();
//
//    isStatusBarVisible = false;
//    mHideHandler.removeCallbacks(hideRunnable);
//    mHideHandler.postDelayed(hideRunnable,UI_ANIMATION_DELAY);
//
//  }
//
//  @Override
//  public void onPause() {
//    super.onPause();
//
//    show(false);
//
//  }
//
//  void show(boolean hideAgain){
//    isStatusBarVisible = true;
//    mHideHandler.removeCallbacks(showRunnable);
//    mHideHandler.postDelayed(showRunnable,UI_ANIMATION_DELAY);
//
//    if(hideAgain){
//      mHideHandler.removeCallbacks(hideRunnable);
//      mHideHandler.postDelayed(hideRunnable,AUTO_HIDE_DELAY_MILLIS);
//    }
//  }


}