package hashed.app.ampassadors.Utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;

import com.google.firebase.auth.FirebaseAuth;

import javax.annotation.Nullable;

import hashed.app.ampassadors.Activities.sign_in;
import hashed.app.ampassadors.R;

public class SigninUtil {

  public static Dialog getInstance(Context context, @Nullable Activity activity){


      Dialog dialog = new Dialog(context);
      dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
      dialog.setContentView(R.layout.signin_alert_layout);
      dialog.findViewById(R.id.alert_close).setOnClickListener(v -> dialog.cancel());
      dialog.findViewById(R.id.alert_signin).setOnClickListener(v -> {

        FirebaseAuth.getInstance().signOut();

        dialog.dismiss();
        context.startActivity(new Intent(context, sign_in.class));

        if (activity != null) {
          activity.finish();
        }

      });

//    }

    return(dialog);
  }

}
