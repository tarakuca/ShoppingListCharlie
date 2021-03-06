package com.mirzairwan.shopping.firebase;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mirzairwan.shopping.R;
import com.mirzairwan.shopping.domain.User;

/**
 * Created by Mirza Irwan on 18/12/16.
 * Copyright 2017, Mirza Irwan Bin Osman , All rights reserved.
 * Contact owner at mirza.irwan.osman@gmail.com
 */

public class MainFirebaseActivity extends AppCompatActivity implements OnFragmentAuthentication
{
        public static final int FIREBASE_SIGN_OUT = 1;
        public static final String FIREBASE_REQUEST_CODE = "FIREBASE_REQUEST_CODE";
        private FirebaseAuth mAuth;
        private DatabaseReference mFireDatabase;
        private String mUserId;
        private Toolbar mToolbar;

        @Override
        protected void onCreate(Bundle savedInstanceState)
        {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_main_firebase);
                mToolbar = (Toolbar)findViewById(R.id.toolbar);
                setSupportActionBar(mToolbar);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);

                //Get the firebase database
                mFireDatabase = FirebaseDatabase.getInstance().getReference();

                //Get firebase authentication
                mAuth = FirebaseAuth.getInstance();

                if (getIntent().getIntExtra(FIREBASE_REQUEST_CODE, 0) == FIREBASE_SIGN_OUT)
                {
                        SignOutDialogFrag signOutDialogFrag = new SignOutDialogFrag();
                        signOutDialogFrag.show(getFragmentManager(), "SIGN_OUT");
                }
                else
                {
                        //Check if user is authenticated
                        if (mAuth.getCurrentUser() != null)
                        {
                                mUserId = mAuth.getCurrentUser().getUid();
                                onAuthenticationSuccess(mAuth.getCurrentUser());
                        }
                        else
                        {
                                SignInDialogFrag signInDialogFrag = new SignInDialogFrag();
                                signInDialogFrag.show(getFragmentManager(), "SIGN_IN");
                        }
                }
        }

        @Override
        protected void onStart()
        {
                super.onStart();
        }

        /**
         * Writes to shared preference.
         * Writes user email under under branch in Firebase database.
         * @param currentUser
         */
        @Override
        public void onAuthenticationSuccess(FirebaseUser currentUser)
        {
                mUserId = currentUser.getUid();

                //Write new iser
                String emailCurrentUser = currentUser.getEmail();
                writeNewUser(currentUser.getUid(), currentUser.getDisplayName(), emailCurrentUser);

                //Update shared preference
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
                editor.putString(getString(R.string.key_cloud_email), emailCurrentUser);
                editor.commit();
                startFragment();
        }

        @Override
        public void onSignUp()
        {
                SignUpDialogFrag signUpDialogFrag = new SignUpDialogFrag();
                signUpDialogFrag.show(getFragmentManager(), "SIGN_UP");
        }

        protected  void startFragment()
        {}

        private void writeNewUser(String uid, String userName, String email)
        {
                User user = new User(userName, email);
                mFireDatabase.child("users").child(uid).setValue(user);
        }
}
