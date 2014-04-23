package cn.nwpu.museum.fragment;

import cn.nwpu.museum.activity.R;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;



public class WifiDialogFragment extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.wifi_dialog_title)
               .setPositiveButton(R.string.wifi_dialog_btn_ok, new DialogInterface.OnClickListener() {
                   @Override
				public void onClick(DialogInterface dialog, int id) {
                	   WifiDialogFragment.this.startActivity(new  Intent(android.provider.Settings.ACTION_SETTINGS));
                   }
               })
               .setNegativeButton(R.string.wifi_dialog_btn_cancel, new DialogInterface.OnClickListener() {
                   @Override
				public void onClick(DialogInterface dialog, int id) {
                	   WifiDialogFragment.this.dismiss();
                   }
               });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
