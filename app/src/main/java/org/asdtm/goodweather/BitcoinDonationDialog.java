package org.asdtm.goodweather;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.asdtm.goodweather.utils.Constants;
import org.asdtm.goodweather.utils.Utils;

public class BitcoinDonationDialog extends DialogFragment {

    public static BitcoinDonationDialog newInstance() {
        return new BitcoinDonationDialog();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.dialog_donation_bitcoin, null);
        TextView bitcoinAddress = (TextView) v.findViewById(R.id.bitcoin_address);
        Button copyBitcoinAddress = (Button) v.findViewById(R.id.copy_bitcoin_address_button);
        Button openBitcoinApp = (Button) v.findViewById(R.id.open_bitcoin_app_button);

        bitcoinAddress.setText(Constants.BITCOIN_ADDRESS);
        copyBitcoinAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Utils.copyToClipboard(getActivity(), Constants.BITCOIN_ADDRESS);
                Toast.makeText(getActivity(),
                               R.string.donation_bitcoin_copy_message,
                               Toast.LENGTH_SHORT).show();
            }
        });
        openBitcoinApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("bitcoin:" + Constants.BITCOIN_ADDRESS));
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(getActivity(),
                                   R.string.donation_bitcoin_copy_message,
                                   Toast.LENGTH_SHORT).show();
                }
            }
        });

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setTitle("Bitcoin");
        alertDialog.setView(v);
        alertDialog.setNegativeButton(android.R.string.cancel,
                                      new DialogInterface.OnClickListener() {
                                          @Override
                                          public void onClick(DialogInterface dialogInterface,
                                                              int i) {
                                              dialogInterface.cancel();
                                          }
                                      });
        return alertDialog.create();
    }
}
