package com.sisbarra.orienteegame;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import static com.sisbarra.orienteegame.MainActivity.PREFERENCE_FILENAME;

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
        alertDialogBuilder.setPositiveButton(getString(R.string.confirm_text_dialog), null);
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

        //Bisogna verificare che l'utente abbia scritto qualcosa
        AlertDialog mydialog = alertDialogBuilder.create();
        mydialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialog) {

                Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        //Verifica che l'utente abbia scritto almeno un carattere
                        String text = verifyText();
                        if(text == null)
                            return;

                        //Aggiorna la preference
                        updateUser(text);

                        //Chiudi il fragment
                        dismiss();
                    }
                });
            }
        });

        return mydialog;
    }

    private void updateUser(String user){
        SharedPreferences gameSettings = getActivity().getSharedPreferences(PREFERENCE_FILENAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor prefEditor = gameSettings.edit();
        prefEditor.putString(getString(R.string.username_pref), user);

        //Setta a false la pref di FirstTime perché si è settato lo user
        prefEditor.putBoolean(getString(R.string.first_time_pref), false);

        prefEditor.apply();

        //TODO: AGGIORNARE LA TEXTVIEW NEL PRIMO FRAGMENT
    }

    private String verifyText(){
        EditText editText = (EditText) getDialog().findViewById(R.id.edit_user);
        String text = editText.getText().toString();
        if(text.isEmpty()) {
            Toast.makeText(getContext(), R.string.no_exit_user, Toast.LENGTH_SHORT).show();
            return null;
        }
        return text;
    }
}
