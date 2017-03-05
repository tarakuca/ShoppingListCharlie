package com.mirzairwan.shopping;

import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import com.mirzairwan.shopping.data.Contract;

import static com.mirzairwan.shopping.LoaderHelper.ITEM_LOADER_ID;

/**
 * Created by Mirza Irwan on 13/1/17.
 * Copyright 2017, Mirza Irwan Bin Osman , All rights reserved.
 * Contact owner at mirza.irwan.osman@gmail.com
 */

public class HistoryEditingActivity extends ItemActivity implements HistoryItemEditorControl.ItemEditorContext
{
        private static final String URI_ITEM = "uri"; /* Used for saving instance state */
        private Uri mUriItem;
        private HistoryItemEditorControl mItemEditorControl;

        @Override
        protected void onCreate(Bundle savedInstanceState)
        {
                mItemEditorControl = new HistoryItemEditorControl(this);
                mItemControl = mItemEditorControl;

                super.onCreate(savedInstanceState);
                mItemEditorControl.setItemEditFieldControl(mItemEditFieldControl);
                mItemEditorControl.setPriceEditFieldControl(mPriceEditFieldControl);

                mContainer = findViewById(R.id.item_editing_container);

                if (savedInstanceState != null) /* Restore from previous activity */
                {
                        mUriItem = savedInstanceState.getParcelable(URI_ITEM);
                }
                else
                {
                        Intent intent = getIntent();
                        mUriItem = intent.getData();
                }

                mItemEditorControl.onExistingItem();
                initLoaders(mUriItem);
        }

        protected void initLoaders(Uri uri)
        {
                if (uri != null)
                {
                        Bundle arg = new Bundle();
                        arg.putParcelable(ITEM_URI, uri);
                        getLoaderManager().initLoader(ITEM_LOADER_ID, arg, this);
                        super.initPictureLoader(uri, this);
                        super.initPriceLoader(uri, this);
                }
        }

        @Override
        protected int getLayoutXml()
        {
                return R.layout.activity_item_editing;
        }

        public void delete(long itemId)
        {
                mDbMsg = daoManager.delete(itemId, mPictureMgr);
        }

        @Override
        public Loader<Cursor> onCreateLoader(int loaderId, Bundle args)
        {
                String[] projection;
                Uri uri = args.getParcelable(ITEM_URI);
                Loader<Cursor> loader;
                switch (loaderId)
                {
                        case ITEM_LOADER_ID:
                                projection = new String[]{
                                        Contract.ItemsEntry._ID, Contract.ItemsEntry.COLUMN_NAME, Contract.ItemsEntry.COLUMN_BRAND, Contract.ItemsEntry.COLUMN_COUNTRY_ORIGIN, Contract.ItemsEntry.COLUMN_DESCRIPTION,};
                                loader = new CursorLoader(this, uri, projection, null, null, null);
                                break;

                        default:
                                loader = super.onCreateLoader(loaderId, args);
                }
                return loader;
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor cursor)
        {
                if (cursor == null || cursor.getCount() < 1)
                {
                        return;
                }

                int loaderId = loader.getId();
                switch (loaderId)
                {
                        case ITEM_LOADER_ID:
                                ItemManager mItemManager = new ItemManager(cursor, getIntent().getBooleanExtra(ITEM_IS_IN_SHOPPING_LIST, false));
                                mItemEditorControl.onLoadItemFinished(mItemManager);
                                mItemEditFieldControl.onLoadItemFinished(mItemManager.getItem());
                                mPictureMgr.setItemId(mItemManager.getItem().getId());
                                break;

                        default:
                                super.onLoadFinished(loader, cursor);
                }
        }

        @Override
        public void onSaveInstanceState(Bundle outState)
        {
                outState.putParcelable(URI_ITEM, mUriItem);
                super.onSaveInstanceState(outState);
        }

        public void update(ItemManager mItemManager)
        {
                mDbMsg = daoManager.update(mItemManager.getItem(), mPriceMgr.getPrices(), mPictureMgr);
                mDbMsg = mItemManager.getItem().getName() + " " + mDbMsg;
        }

}