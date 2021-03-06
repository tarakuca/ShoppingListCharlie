package com.mirzairwan.shopping.firebase;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.LoaderManager;
import android.app.NotificationManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mirzairwan.shopping.OnPictureRequestListener;
import com.mirzairwan.shopping.R;
import com.mirzairwan.shopping.data.Contract;
import com.mirzairwan.shopping.data.Contract.ItemsEntry;
import com.mirzairwan.shopping.data.Contract.PricesEntry;
import com.mirzairwan.shopping.data.Contract.ToBuyItemsEntry;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Created by Mirza Irwan on 18/12/16.
 * Copyright 2017, Mirza Irwan Bin Osman , All rights reserved.
 * Contact owner at mirza.irwan.osman@gmail.com
 */

public class SendShareFragment extends BaseFragment implements LoaderManager.LoaderCallbacks<Cursor>
{
        public static final String ITEM_TO_SHARE = "ITEM_TO_SHARE";
        public static final String SHAREE_EMAIL = "SHAREE_EMAIL";
        private static final String LOG_TAG = SendShareFragment.class.getSimpleName();
        private OnPictureRequestListener mOnPictureRequestListener;
        private DatabaseReference mRootRef;
        private ArrayList<Item> mItemsToShare = new ArrayList<>();
        private String mShareeEmail;
        private int mItemSaveShareCount;
        private NotificationManager mNotificationManager;
        private NotificationCompat.Builder mNotificationBuilder;
        private NotificationCompat.InboxStyle inboxStyle;

        public static SendShareFragment getInstance(Long[] ids, String shareeEmail)
        {
                SendShareFragment shareFragment = new SendShareFragment();
                Bundle args = new Bundle();
                long[] ide = new long[ids.length];
                for (int k = 0; k < ids.length; ++k)
                {
                        ide[k] = ids[k];
                }
                args.putLongArray(ITEM_TO_SHARE, ide);
                args.putString(SHAREE_EMAIL, shareeEmail);
                shareFragment.setArguments(args);
                return shareFragment;
        }

        @Override
        public void onCreate(Bundle savedInstanceState)
        {
                super.onCreate(savedInstanceState);
                mRootRef = FirebaseDatabase.getInstance().getReference();

                inboxStyle = new NotificationCompat.InboxStyle();
                mNotificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);

                mNotificationBuilder = new NotificationCompat.Builder(getActivity());
                mNotificationBuilder.setContentTitle("Shopping item(s) shared:").setSmallIcon(android.R.drawable.stat_notify_chat);
        }

        @Override
        protected void processSocialShoppingList()
        {
                getLoaderManager().initLoader(64, null, this);
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState)
        {
                super.onActivityCreated(savedInstanceState);
        }


        public Loader<Cursor> onCreateLoader2(int id, Bundle args)
        {
                Log.d(LOG_TAG, ">>>>>>> onCreateLoader");
                String[] projection = new String[]{ToBuyItemsEntry._ID,
                                                   ToBuyItemsEntry.COLUMN_ITEM_ID,
                                                   ToBuyItemsEntry.COLUMN_QUANTITY,
                                                   ToBuyItemsEntry.COLUMN_IS_CHECKED,
                                                   ItemsEntry.COLUMN_NAME,
                                                   ItemsEntry.COLUMN_BRAND,
                                                   ItemsEntry.COLUMN_COUNTRY_ORIGIN,
                                                   ItemsEntry.COLUMN_DESCRIPTION,
                                                   Contract.PicturesEntry.COLUMN_FILE_PATH,
                                                   PricesEntry.COLUMN_PRICE_TYPE_ID,
                                                   PricesEntry.COLUMN_PRICE,
                                                   PricesEntry.COLUMN_CURRENCY_CODE};

                Uri uri = Contract.ShoppingList.CONTENT_URI;
                Bundle arguments = getArguments();
                HashSet<Long> itemIds = (HashSet<Long>) arguments.getSerializable(ITEM_TO_SHARE);

                Iterator<Long> iterator = itemIds.iterator();
                String subSelection = null;
                if (itemIds.size() > 0)
                {
                        subSelection = " IN (";
                        while (iterator.hasNext())
                        {
                                iterator.next();
                                subSelection += "?";
                                if (!iterator.hasNext())
                                {
                                        subSelection += ")";
                                }
                                else
                                {
                                        subSelection += ",";
                                }
                        }
                }

                //Summary screen shows only selected price
                String selection = PricesEntry.TABLE_NAME + "." + PricesEntry._ID + "=" +
                        ToBuyItemsEntry.TABLE_NAME + "." + ToBuyItemsEntry.COLUMN_SELECTED_PRICE_ID +
                        " AND " + ToBuyItemsEntry.TABLE_NAME + "." + ToBuyItemsEntry._ID + subSelection;

                String[] selectionArgs = new String[itemIds.size()];
                int k = 0;
                for (Long idt : itemIds)
                {
                        selectionArgs[k] = String.valueOf(idt);
                        ++k;
                }

                return new CursorLoader(getActivity(), uri, projection, selection, selectionArgs, null);
        }

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args)
        {
                Log.d(LOG_TAG, ">>>>>>> onCreateLoader");
                String[] projection = new String[]{ToBuyItemsEntry._ID,
                                                   ToBuyItemsEntry.COLUMN_ITEM_ID,
                                                   ToBuyItemsEntry.COLUMN_QUANTITY,
                                                   ToBuyItemsEntry.COLUMN_IS_CHECKED,
                                                   ItemsEntry.COLUMN_NAME,
                                                   ItemsEntry.COLUMN_BRAND,
                                                   ItemsEntry.COLUMN_COUNTRY_ORIGIN,
                                                   ItemsEntry.COLUMN_DESCRIPTION,
                                                   Contract.PicturesEntry.COLUMN_FILE_PATH,
                                                   PricesEntry.COLUMN_PRICE_TYPE_ID,
                                                   PricesEntry.COLUMN_PRICE,
                                                   PricesEntry.COLUMN_CURRENCY_CODE};

                Uri uri = Contract.ShoppingList.CONTENT_URI;
                Bundle arguments = getArguments();
                long[] itemIds = arguments.getLongArray(ITEM_TO_SHARE);
                int k = 0;
                String subSelection = null;
                if (itemIds.length > 0)
                {
                        subSelection = " IN (";
                        while (k < itemIds.length)
                        {
                                ++k;
                                subSelection += "?";
                                if (k == itemIds.length)
                                {
                                        subSelection += ")";
                                }
                                else
                                {
                                        subSelection += ",";
                                }
                        }
                }

                //Summary screen shows only selected price
                String selection = PricesEntry.TABLE_NAME + "." + PricesEntry._ID + "=" +
                        ToBuyItemsEntry.TABLE_NAME + "." + ToBuyItemsEntry.COLUMN_SELECTED_PRICE_ID +
                        " AND " + ToBuyItemsEntry.TABLE_NAME + "." + ToBuyItemsEntry._ID + subSelection;

                String[] selectionArgs = new String[itemIds.length];
                int q = 0;
                for (Long idt : itemIds)
                {
                        selectionArgs[q] = String.valueOf(idt);
                        ++q;
                }

                return new CursorLoader(getActivity(), uri, projection, selection, selectionArgs, null);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor cursor)
        {
                Log.d(LOG_TAG, ">>>>>>> onLoadFinished Cursor");
                prepareSharedItems(cursor);
                mShareeEmail = getArguments().getString(SHAREE_EMAIL);
                submitShoppingList(mShareeEmail);
        }

        private void prepareSharedItems(Cursor cursor)
        {
                FirebaseUser firebaserUser = mAuth.getCurrentUser();
                while (cursor.moveToNext())
                {
                        String itemName = cursor.getString(cursor.getColumnIndex(ItemsEntry.COLUMN_NAME));
                        String brand = cursor.getString(cursor.getColumnIndex(ItemsEntry.COLUMN_BRAND));
                        int colSelectedPriceTagIdx = cursor.getColumnIndex(PricesEntry.COLUMN_PRICE);
                        long priceTag = cursor.getLong(colSelectedPriceTagIdx);
                        int colCurrencyCodeIdx = cursor.getColumnIndex(PricesEntry.COLUMN_CURRENCY_CODE);
                        String currencyCode = cursor.getString(colCurrencyCodeIdx);
                        int colBuyItemQty = cursor.getColumnIndex(ToBuyItemsEntry.COLUMN_QUANTITY);
                        int qty = cursor.getInt(colBuyItemQty);
                        mItemsToShare.add(new Item(firebaserUser.getEmail(), firebaserUser.getUid(), itemName, brand, priceTag, currencyCode, qty));
                        Log.d(LOG_TAG, "Item name = " + itemName);
                }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader)
        {
        }

        private void submitShoppingList(String userEmail)
        {
                Log.d(LOG_TAG, ">>> submitShoppingList");
                Query userQuery = getTargetUser(userEmail);

                userQuery.addListenerForSingleValueEvent(new ValueEventListener()
                {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot)
                        {
                                String key = dataSnapshot.getKey();
                                Log.d(LOG_TAG, ">>> The key is  " + key);
                                Log.d(LOG_TAG, ">>> The value is  " + dataSnapshot.getValue());

                                Iterable<DataSnapshot> childen = dataSnapshot.getChildren();

                                String shareeUserId = null;

                                for (DataSnapshot data : childen)
                                {
                                        Log.d(LOG_TAG, ">>> The children key  is  " + data.getKey()); //This is the key to json user object
                                        shareeUserId = data.getKey();
                                        Log.d(LOG_TAG, ">>> The children value  is  " + data.getValue());
                                }

                                if (!TextUtils.isEmpty(shareeUserId))
                                {
                                        saveToCloud(shareeUserId);
                                        getFragmentManager().popBackStack(getString(R.string.shopping_list), 0);
                                }
                                else
                                {
                                        //mProgressBar.setVisibility(View.GONE);
                                        EmailDoesNotExistDialogFrag error = EmailDoesNotExistDialogFrag.newInstance(mShareeEmail);
                                        error.show(getFragmentManager(), "email_error");
                                }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError)
                        {

                        }
                });
        }

        private void saveToCloud(final String shareeUserId)
        {
                DatabaseReference shoppingListShareWithRef = mRootRef.child("shopping_list_share_with").child(shareeUserId);
                final int countItemsToShare = mItemsToShare.size();
                for (int j = 0; j < countItemsToShare; ++j)
                {
                        // Push the item, it will appear in the list
                        Item item = mItemsToShare.get(j);
                        final String itemName = item.getName();
                        shoppingListShareWithRef.push().setValue(item).addOnSuccessListener(new OnSuccessListener<Void>()
                        {
                                @Override
                                public void onSuccess(Void aVoid)
                                {
                                        Log.d(LOG_TAG, ">>>  saveToCloud  onSuccess");
                                        if (countItemsToShare == 1)
                                        {
                                                mNotificationBuilder.setContentText(itemName + " OK");
                                        }
                                        else
                                        {
                                                mNotificationBuilder.setStyle(inboxStyle.addLine(itemName + " OK"));
                                        }

                                        mNotificationManager.notify(1, mNotificationBuilder.build());
                                }
                        }).addOnFailureListener(new OnFailureListener()
                        {
                                @Override
                                public void onFailure(@NonNull Exception e)
                                {
                                        Log.d(LOG_TAG, ">>>  saveToCloud  onFailure " + e.getMessage());
                                        if (countItemsToShare == 1)
                                        {
                                                mNotificationBuilder.setContentText(itemName + " Failed");
                                        }
                                        else
                                        {
                                                mNotificationBuilder.setStyle(inboxStyle.addLine(itemName + " Failed"));
                                        }
                                        mNotificationManager.notify(1, mNotificationBuilder.build());
                                }
                        });
                }

                mItemsToShare.clear();
        }

        private Query getTargetUser(String userEmail)
        {
                //Get RootRef
                DatabaseReference rootRef = mRootRef;

                //Nest down to users. So users is our parent key
                DatabaseReference users = rootRef.child("users");

                Query usersQuery = users.orderByChild("email");

                Query user = usersQuery.equalTo(userEmail).limitToFirst(1);
                return user;
        }

        public Query getQuery(DatabaseReference databaseReference)
        {
                // All my posts
                return databaseReference.child("user-posts").child(getUid());
        }

        public String getUid()
        {
                return FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        public static class EmailDoesNotExistDialogFrag extends DialogFragment
        {
                public static EmailDoesNotExistDialogFrag newInstance(String email)
                {
                        Bundle args = new Bundle();
                        args.putString(SHAREE_EMAIL, email);
                        EmailDoesNotExistDialogFrag fragment = new EmailDoesNotExistDialogFrag();
                        fragment.setArguments(args);
                        return fragment;
                }

                @Override
                public Dialog onCreateDialog(Bundle savedInstanceState)
                {
                        // Use the Builder class for convenient dialog construction
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        String message = getArguments().getString(SHAREE_EMAIL) + " " + getString(R.string.person_does_not_exist);
                        builder.setMessage(message).setPositiveButton("Dismiss", new DialogInterface.OnClickListener()
                        {
                                public void onClick(DialogInterface dialog, int id)
                                {
                                        dismiss();
                                        getActivity().finish();
                                }
                        });


                        return builder.create();
                }
        }
}
