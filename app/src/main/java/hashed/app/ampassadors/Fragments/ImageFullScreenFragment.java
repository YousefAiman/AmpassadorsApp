package hashed.app.ampassadors.Fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.squareup.picasso.Picasso;

import java.util.Objects;

import hashed.app.ampassadors.Activities.PostNewsActivity;
import hashed.app.ampassadors.R;
import hashed.app.ampassadors.Utils.FileDownloadUtil;

public class ImageFullScreenFragment extends DialogFragment {

  private static final boolean AUTO_HIDE = true;

  private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

  private static final int UI_ANIMATION_DELAY = 300;
  private final Handler mHideHandler = new Handler();
  private final Runnable mHidePart2Runnable = new Runnable() {
    @SuppressLint("InlinedApi")
    @Override
    public void run() {

      int flags = View.SYSTEM_UI_FLAG_LOW_PROFILE
              | View.SYSTEM_UI_FLAG_FULLSCREEN
              | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
              | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
              | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
              | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;

      Activity activity = getActivity();
      if (activity != null
              && activity.getWindow() != null) {
        activity.getWindow().getDecorView().setSystemUiVisibility(flags);
      }

    }
  };
  private  String imageUrl;
  private  String attachmentName;
  private  Uri imageUri;
  private ImageView fullScreenIv;
  private boolean mVisible;
  private FileDownloadUtil fileDownloadUtil;

  private final Runnable mHideRunnable = new Runnable() {
    @Override
    public void run() {
      hide();
    }
  };
  private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
      if (AUTO_HIDE) {
        delayedHide(AUTO_HIDE_DELAY_MILLIS);
      }
      return false;
    }
  };

  public ImageFullScreenFragment(String imageUrl) {
    this.imageUrl = imageUrl;
  }

  public ImageFullScreenFragment(String imageUrl,String attachmentName) {
    this.imageUrl = imageUrl;
    this.attachmentName = attachmentName;
  }

  public ImageFullScreenFragment(Uri imageUri) {
    this.imageUri = imageUri;
  }


  @Override
  public void onStart() {
    super.onStart();
  }

  @Override
  public int getTheme() {
    return R.style.FullScreenDialog;
  }

//  @NonNull
//  @Override
//  public Dialog onCreateDialog(final Bundle savedInstanceState) {
//
//    // the content
//    final FrameLayout root = new FrameLayout(getActivity());
//    root.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
//            ViewGroup.LayoutParams.MATCH_PARENT));
//
//    // creating the fullscreen dialog
//    final Dialog dialog = new Dialog(getActivity());
//    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//    dialog.setContentView(root);
//    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//    dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
//            ViewGroup.LayoutParams.MATCH_PARENT);
//
//    return dialog;
//  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

//    Dialog dialog = getDialog();
//    if (dialog != null) {
//      int width = ViewGroup.LayoutParams.MATCH_PARENT;
//      int height = ViewGroup.LayoutParams.MATCH_PARENT;
//      dialog.getWindow().setLayout(width, height);
//
////      dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
////              WindowManager.LayoutParams.FLAG_FULLSCREEN);
//
//    }
//    setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialogTheme);

  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater,
                           @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_image_full_screen, container,
            false);
//
//    Objects.requireNonNull(getDialog()).getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
//            ViewGroup.LayoutParams.MATCH_PARENT);



    fullScreenIv = view.findViewById(R.id.fullScreenIv);

    final Toolbar fullScreenToolbar = view.findViewById(R.id.fullScreenToolbar);
    fullScreenToolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());

    if(attachmentName!=null){

      fullScreenToolbar.inflateMenu(R.menu.download_menu);
      fullScreenToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
          if(item.getItemId() == R.id.action_download){

            if(fileDownloadUtil == null){
              fileDownloadUtil = new FileDownloadUtil(requireContext(),
                      imageUrl, attachmentName,null);
            }

            fileDownloadUtil.showDownloadAlert();
//            if (downloadCompleteReceiver == null) {
//              setUpDownloadReceiver();
//            }
          }
          return false;
        }
      });

    }

    return view;
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    mVisible = true;


    fullScreenIv.setOnClickListener(v -> toggle());

    if (imageUri != null) {
      Picasso.get().load(imageUri).fit().centerInside().into(fullScreenIv);
    } else if (imageUrl != null) {
      Picasso.get().load(imageUrl).fit().centerInside().into(fullScreenIv);
    }

  }

  private void toggle() {
    if (mVisible) {
      hide();
    } else {

      mVisible = true;
      if (getActivity() != null && getActivity().getWindow() != null) {

        getActivity().getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );

        delayedHide(3000);
      }


    }
  }

  private void hide() {
    mVisible = false;
    mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
  }

  @SuppressLint("InlinedApi")
  public void show() {
    // Show the system bar
    if (fullScreenIv != null) {
      fullScreenIv.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
              | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
      mVisible = true;

      // Schedule a runnable to display UI elements after a delay
      mHideHandler.removeCallbacks(mHidePart2Runnable);

    }
  }


  private void delayedHide(int delayMillis) {

    mHideHandler.removeCallbacks(mHideRunnable);
    mHideHandler.postDelayed(mHideRunnable, delayMillis);
  }

  @Override
  public void onPause() {
    super.onPause();

    if (getActivity() != null && getActivity().getWindow() != null) {
      getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

      getActivity().getWindow().getDecorView().setSystemUiVisibility(0);

      getActivity().setRequestedOrientation(
              ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }
    show();

  }


//  private void setUpDownloadReceiver() {
//
//    downloadCompleteReceiver = new BroadcastReceiver() {
//      @Override
//      public void onReceive(Context context, Intent intent) {
//
//        long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
//        if (id != -1) {
//          attachmentImage.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//              fileDownloadUtil.showDownloadAlert();
//            }
//          });
//        }
//      }
//    };
//
//    registerReceiver(downloadCompleteReceiver,
//            new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
//
//  }


  @Override
  public void onResume() {
    super.onResume();

    if (getActivity() != null && getActivity().getWindow() != null) {
      getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

      getActivity().setRequestedOrientation(
              ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);

    }

    delayedHide(0);

  }


}