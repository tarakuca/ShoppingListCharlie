package com.mirzairwan.shopping.firebase;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.mirzairwan.shopping.R;

/**
 * Created by Mirza Irwan on 18/12/16.
 * Copyright 2017, Mirza Irwan Bin Osman , All rights reserved.
 * Contact owner at mirza.irwan.osman@gmail.com
 */

public class SignOutDialogFrag extends DialogFragment
{
        private FirebaseAuth mAuth;

        @Override
        public void onCreate(Bundle savedInstanceState)
        {
                super.onCreate(savedInstanceState);
                //Get firebase authentication
                mAuth = FirebaseAuth.getInstance();
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState)
        {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage(R.string.sign_out_msg).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                                mAuth.signOut();
                                SignOutDialogFrag.this.dismiss();
                        }
                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                                SignOutDialogFrag.this.dismiss();
                        }
                });

                return builder.create();
        }
}
