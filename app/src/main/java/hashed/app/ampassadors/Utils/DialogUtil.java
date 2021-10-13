package hashed.app.ampassadors.Utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import hashed.app.ampassadors.R;

public class DialogUtil {

    public interface DialogListener{
        void onDialogConfirmed();
        void onDialogDismissed();
    }

    public static void showDialog(Context context, String text, DialogListener dialogListener){

        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(text);
        builder.setTitle(context.getString(R.string.agree_on_terms));
        builder.setPositiveButton(context.getString(R.string.OK), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
                dialogListener.onDialogConfirmed();
            }
        });

        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {

                     dialogListener.onDialogDismissed();

            }
        });
        builder.create().show();

    }

}
