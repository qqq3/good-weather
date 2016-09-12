package org.asdtm.goodweather;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;

public class DonationBitcoinDialog extends DialogFragment {

    DonationBitcoinDialog newInstance() {
        return new DonationBitcoinDialog();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity())
                .setTitle("Bitcoin")


    }
}
