package com.outlook.notyetapp.utilities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Environment;

import com.outlook.notyetapp.MainActivity;
import com.outlook.notyetapp.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Neil on 11/29/2016.
 */

public class EULAUtils {
    public static final SimpleDateFormat eulaAgreementFormat = new SimpleDateFormat("MMMMM dd, yyyy");

    public static void ShowEULAIfNecessary(final Context context, String lastAgreed)
    {
        Date lastAgreedDate = null;
        Date agreementLastUpdatedDate = null;
        try {
            lastAgreedDate = eulaAgreementFormat.parse(lastAgreed);
            agreementLastUpdatedDate = eulaAgreementFormat.parse(context.getString(R.string.eula_date_updated));
        }
        catch (ParseException e){}
        if(lastAgreedDate != null && agreementLastUpdatedDate != null && agreementLastUpdatedDate.after(lastAgreedDate)){
            String message = context.getString(R.string.eula_date_updated_label) + context.getString(R.string.eula_date_updated) + "\n\n" +
                    context.getString(R.string.eula_content);
            new AlertDialog.Builder(context)
                    .setTitle(context.getString(R.string.eula_title))
                    .setMessage(message)
                    .setPositiveButton(context.getString(R.string.eula_agree), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ((MainActivity)context).mEulaAgreedDate = context.getString(R.string.eula_date_updated);
                            SharedPreferences sharedPref = ((MainActivity) context).getPreferences(Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putString(MainActivity.EULA_KEY, context.getString(R.string.eula_date_updated));
                            editor.commit();
                            dialog.dismiss();
                        }
                    })
                    .setCancelable(false)
                    .setNegativeButton(context.getString(R.string.eula_cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ((MainActivity)context).finish();
                        }
                    })
                    .show();

        }
    }
}
