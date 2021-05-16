package hashed.app.ampassadors.Utils;

import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.widget.Toolbar;

import com.squareup.picasso.Picasso;

import javax.annotation.Nullable;

import hashed.app.ampassadors.R;

public class FullScreenImagesUtil {

    public static void showImageFullScreen(Context context,
                                           @Nullable String imageUrl,
                                           @Nullable Uri imageUri,
                                           @Nullable String attachmentName) {

        final Dialog imageDialog = new Dialog(context,
                android.R.style.Theme_Black_NoTitleBar_Fullscreen);

        imageDialog.setContentView(R.layout.fragment_image_full_screen);

        final Toolbar toolbar = ((Toolbar)imageDialog.findViewById(R.id.fullScreenToolbar));

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageDialog.dismiss();
            }
        });

        final ImageView fullScreenIv = imageDialog.findViewById(R.id.fullScreenIv);
        if (imageUrl != null) {
            Picasso.get().load(imageUrl).fit().centerInside().into(fullScreenIv);
        } else if (imageUri != null) {
            Picasso.get().load(imageUri).fit().centerInside().into(fullScreenIv);
        }

        if(attachmentName != null){

            toolbar.inflateMenu(R.menu.download_menu);

            toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                FileDownloadUtil fileDownloadUtil = null;
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    if(item.getItemId() == R.id.action_download){

                        if(fileDownloadUtil == null){
                            fileDownloadUtil = new FileDownloadUtil(context,
                                    imageUrl, attachmentName,null);
                        }

                        fileDownloadUtil.showDownloadAlert();
                    }
                    return false;
                }
            });

        }


        imageDialog.show();
//        final ScaleGestureDetector scaleGestureDetector
//                = new ScaleGestureDetector(context, new ScaleListener(fullScreenIv));
//
//        if (imageDialog.getWindow() != null) {
//
//            imageDialog.getWindow().getDecorView().setOnTouchListener(new View.OnTouchListener() {
//                long lastClicked = 0;
//
//                @Override
//                public boolean onTouch(View view, MotionEvent motionEvent) {
//
//                    if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
//
//                        if (lastClicked != 0) {
//                            if (System.currentTimeMillis() - lastClicked <= 300) {
//                                if (fullScreenIv.getScaleX() == 1.0f) {
//                                    fullScreenIv.setScaleX(2.0f);
//                                    fullScreenIv.setScaleY(2.0f);
//                                } else {
//                                    fullScreenIv.setScaleX(1.0f);
//                                    fullScreenIv.setScaleY(1.0f);
//                                }
//                                lastClicked = 0;
//                            }
//                        }
//                        lastClicked = System.currentTimeMillis();
//                    }
//
//                    scaleGestureDetector.onTouchEvent(motionEvent);
//                    return false;
//                }
//            });
//
//        }
    }



//    private static class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
//        static float mScaleFactor = 1.0f;
//        final ImageView fullScreenIv;
//
//        public ScaleListener(ImageView fullScreenIv) {
//            this.fullScreenIv = fullScreenIv;
//        }
//
//        @Override
//        public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
//
//            mScaleFactor *= scaleGestureDetector.getScaleFactor();
//            mScaleFactor = Math.max(0.5f, Math.min(mScaleFactor, 2.0f));
//            fullScreenIv.setScaleX(mScaleFactor);
//            fullScreenIv.setScaleY(mScaleFactor);
//            return true;
//        }
//    }

}
