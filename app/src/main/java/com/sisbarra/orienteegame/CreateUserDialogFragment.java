package com.sisbarra.orienteegame;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.KeyEvent;
import android.widget.Toast;

/**
 * Created by Antonio Sisbarra on 15/06/2017.
 * Incapsula il dialog fragment che prende in input lo user all'avvio.
 */

public class CreateUserDialogFragment extends AppCompatDialogFragment {

    public CreateUserDialogFragment() {
        // Empty constructor is required for DialogFragment
        // Make sure not to add arguments to the constructor
        // Use `newInstance` instead as shown below
    }

    public static CreateUserDialogFragment newInstance(String title) {
        CreateUserDialogFragment frag = new CreateUserDialogFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        frag.setArguments(args);
        return frag;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String title = getArguments().getString("title");
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity(), R.style.AppAlertTheme);
        alertDialogBuilder.setTitle(title);
        alertDialogBuilder.setIcon(R.drawable.ic_my_info);
        alertDialogBuilder.setView(R.layout.fragment_create_user);
        alertDialogBuilder.setPositiveButton(getString(R.string.confirm_text_dialog),  new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //TODO: on success
            }
        });
        alertDialogBuilder.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if(keyCode == KeyEvent.KEYCODE_BACK
                        && event.getAction() == KeyEvent.ACTION_UP){
                    Toast.makeText(getContext(), R.string.no_exit_user, Toast.LENGTH_SHORT).show();
                    return true;
                }
                return false;
            }
        });
        alertDialogBuilder.setCancelable(false);

        return alertDialogBuilder.create();
    }
}
