package org.helpapaw.helpapaw.authentication.register;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import org.helpapaw.helpapaw.R;

/**
 * Created by iliyan on 7/27/16
 */
public class WhyPhoneDialogFragment extends DialogFragment {

    public static WhyPhoneDialogFragment newInstance() {
        return new WhyPhoneDialogFragment();
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        return new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.txt_why_want_phone))
                .setMessage(getString(R.string.txt_why_phone_description))
                .setPositiveButton(R.string.txt_i_see,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                            }
                        }
                ).create();
    }
}
