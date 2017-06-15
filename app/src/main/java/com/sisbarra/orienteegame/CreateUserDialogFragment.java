package com.sisbarra.orienteegame;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatDialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;

import static android.content.ContentValues.TAG;

/**
 * Created by Antonio Sisbarra on 15/06/2017.
 */

public class CreateUserDialogFragment extends AppCompatDialogFragment {

    private EditText mEditUser;

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_create_user, container);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Get field from view
        mEditUser = (EditText) view.findViewById(R.id.edit_user);
        // Fetch arguments from bundle and set title
        String title = getArguments().getString("title", getString(R.string.lbl_user_text));
        getDialog().setTitle(title);
        // Show soft keyboard automatically and request focus to field
        mEditUser.requestFocus();
        try {
            getDialog().getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }
        catch (NullPointerException e) {
            Log.e(TAG, e.toString());
        }
    }
}
