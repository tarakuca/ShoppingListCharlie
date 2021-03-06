package com.mirzairwan.shopping;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.Snackbar;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.mirzairwan.shopping.data.Contract.PicturesEntry;
import com.mirzairwan.shopping.data.Contract.PricesEntry;
import com.mirzairwan.shopping.data.DaoManager;
import com.mirzairwan.shopping.domain.ExchangeRate;
import com.mirzairwan.shopping.domain.Picture;
import com.mirzairwan.shopping.domain.PictureMgr;
import com.mirzairwan.shopping.domain.PriceMgr;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.mirzairwan.shopping.LoaderHelper.ITEM_PICTURE_LOADER_ID;
import static com.mirzairwan.shopping.LoaderHelper.ITEM_PRICE_LOADER_ID;
import static com.mirzairwan.shopping.ShoppingActivity.EXCHANGE_RATE;

/**
 * Created by Mirza Irwan on 13/1/17.
 * Copyright 2017, Mirza Irwan Bin Osman , All rights reserved.
 * Contact owner at mirza.irwan.osman@gmail.com
 * <p>
 * This is a base class for subclasses to leverage on the following:
 * <p>
 * Loader for Prices table. Subclass just need to call initLoader with the correct loader id.
 * <p>
 * Loader for Pictures table. Subclass just need to call initLoader with the correct loader id.
 * <p>
 * Create Item objects and populate screen.
 * <p>
 * Ask permission for file access
 * <p>
 * Provide picture toolbar to capture image and pick photos from device storage.
 * <p>
 * Provide app bar for save and delete operation
 * <p>
 * Validate fields of provided xml layouts.
 * <p>
 * Provide user preference for country code
 * <p>
 * Provide user preference for sort by criteria
 * <p>
 * Subclass must include in their screen the following xml layout provided:
 * <p>
 * item_image_editor.xml
 * <p>
 * item_editing.xml
 * <p>
 * price_editing.xml
 */
public abstract class ItemActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>,
                                                                        ItemContext
{
        public static final String ITEM_IS_IN_SHOPPING_LIST = "ITEM_IS_IN_SHOPPING_LIST";
        protected static final String ITEM_URI = "ITEM_URI";
        private static final String LOG_TAG = ItemActivity.class.getSimpleName();
        private static final int REQUEST_SNAP_PICTURE = 15;
        private static final int REQUEST_PICK_PHOTO = 16;
        private static final String PICTURE_MANAGER = "picture_mgr";
        protected View.OnTouchListener mOnTouchListener;
        protected DaoManager daoManager;
        protected PictureMgr mPictureMgr;
        protected PriceMgr mPriceMgr;
        protected String mSettingsCountryCode;
        protected String mDbMsg;
        protected View mContainer;
        private ItemControl mItemControl;
        private ImageView mImgItemPic;
        private long mItemId;
        private ExchangeRateInput mExchangeRateInput;

        /* During orientation, the exchange rate fields are not populated by the exchange rate loader. So need to save its instance
            and restore when device orientates to landscape. */
        private ExchangeRate mExchangeRate;

        private String mWebApiBase;
        private Toolbar mToolbarPicture;

        @Override
        protected void onPause()
        {
                super.onPause();
                Log.d(LOG_TAG, "onPause");
        }

        @Override
        protected void onResume()
        {
                super.onResume();
                Log.d(LOG_TAG, "onResume");
        }

        @Override
        protected void onStop()
        {
                super.onStop();
                Log.d(LOG_TAG, "onStop");
        }

        @Override
        protected void onRestart()
        {
                super.onRestart();
                Log.d(LOG_TAG, "onRestart");
        }

        @Override
        protected void onRestoreInstanceState(Bundle savedInstanceState)
        {
                Log.d(LOG_TAG, "onRestoreInstanceState");
                super.onRestoreInstanceState(savedInstanceState);
        }

        @Override
        public String getDefaultCountryCode()
        {
                return mSettingsCountryCode;
        }

        @Override
        protected void onCreate(Bundle savedInstanceState)
        {
                super.onCreate(savedInstanceState);
                setContentView(getLayoutXml());
                setupEnterAnimationTransition();
                PreferenceManager.setDefaultValues(this, R.xml.preferences, true);
                SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
                mSettingsCountryCode = sharedPrefs.getString(getString(R.string.user_country_pref), null);
                mItemControl = getItemControl();
                Toolbar mainToolbar = (Toolbar) findViewById(R.id.toolbar);
                setSupportActionBar(mainToolbar);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);

                Uri data = getIntent().getData();
                if (data != null)
                {
                        mItemId = ContentUris.parseId(data);
                }

                setupPictureToolbar();

                daoManager = Builder.getDaoManager(this);
                mPriceMgr = new PriceMgr(mSettingsCountryCode);

                String webApiKeyPref = getString(R.string.key_forex_web_api_1);
                mWebApiBase = sharedPrefs.getString(webApiKeyPref, null);
                mExchangeRateInput = new ExchangeRateInput();
                mExchangeRateInput.setBaseWebApi(mWebApiBase);
                mExchangeRateInput.setBaseCurrency(FormatHelper.getCurrencyCode(mSettingsCountryCode));

                mExchangeRate = getIntent().getParcelableExtra(EXCHANGE_RATE);
                if (mExchangeRate != null)
                {
                        mExchangeRateInput.addSourceCurrency(mExchangeRate.getSourceCurrencyCode());
                }

                setupViews();

                if (savedInstanceState == null)
                {
                        Log.d(LOG_TAG, "OnCreate with NULL savedInstanceState");
                        mPictureMgr = new PictureMgr(daoManager);
                }
                else
                {
                        Log.d(LOG_TAG, "OnCreate with savedInstanceState");
                        mPictureMgr = savedInstanceState.getParcelable(PICTURE_MANAGER);
                }
        }

        protected abstract ItemControl getItemControl();

        protected void setupViews()
        {
                mOnTouchListener = new View.OnTouchListener()
                {
                        @Override
                        public boolean onTouch(View v, MotionEvent event)
                        {

                                int action = event.getAction();
                                if (action == MotionEvent.ACTION_UP)
                                {
                                        mItemControl.onChange();
                                }
                                return false;
                        }
                };

                mImgItemPic = (ImageView) findViewById(R.id.img_item);
                ItemExchangeRateLoaderCallback pxExLoaderCb = new ItemExchangeRateLoaderCallback(this);
                getLoaderManager().initLoader(78, null, pxExLoaderCb);
        }

        @Override
        public Transition inflateTransition(int transitionResId)
        {
                return TransitionInflater.from(this).inflateTransition(transitionResId);
        }

        protected abstract int getLayoutXml();

        protected void setupPictureToolbar()
        {
                mToolbarPicture = (Toolbar) findViewById(R.id.picture_toolbar);

                /* Inflate a menu to be displayed in the toolbar */
                mToolbarPicture.inflateMenu(R.menu.picture_item_toolbar);

                /* Check for the availability of the camera at runtime. If unavailable, disable the affected action item */
                PackageManager pMgr = getPackageManager();
                if (!pMgr.hasSystemFeature(PackageManager.FEATURE_CAMERA))
                {
                        MenuItem itemCamera = mToolbarPicture.getMenu().findItem(R.id.menu_camera);
                        itemCamera.setEnabled(false);
                }

                /* Add handler to handle menu item clicks */
                mToolbarPicture.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener()
                {
                        @Override
                        public boolean onMenuItemClick(MenuItem item)
                        {
                                switch (item.getItemId())
                                {
                                        case R.id.menu_camera:
                                                mItemControl.onCameraAction();
                                                return true;

                                        case R.id.choose_picture:
                                                boolean hasReadStoragePermission = PermissionHelper.hasReadStoragePermission(ItemActivity.this);
                                                if (hasReadStoragePermission)
                                                {
                                                        mItemControl.onPickPictureAction();
                                                        //startPickPictureActivity();
                                                }
                                                else
                                                {
                                                        PermissionHelper.setupStorageReadPermission(ItemActivity.this);
                                                }
                                                return true;

                                        case R.id.remove_picture:
                                                mItemControl.onChange();
                                                mItemControl.onDeletePictureInView();
                                                return true;

                                        default:
                                                return false;
                                }

                        }
                });

                PermissionHelper.setupStorageReadPermission(this);
        }

        @Override
        public void setPictureMenuItemEnabled(int menuId, boolean enabled)
        {
                mToolbarPicture.getMenu().findItem(menuId).setEnabled(enabled);
        }

        @Override
        public void setPictureMenuItemVisible(int menuId, boolean visible)
        {
                mToolbarPicture.getMenu().findItem(menuId).setVisible(visible);
        }

        protected void initPriceLoader(Uri uri, LoaderManager.LoaderCallbacks<Cursor> callback)
        {
                if (ContentUris.parseId(uri) == -1)
                {
                        throw new IllegalArgumentException("uri and item id cannot be empty or -1");
                }

                Bundle arg = new Bundle();
                arg.putParcelable(ITEM_URI, uri);
                getLoaderManager().initLoader(ITEM_PRICE_LOADER_ID, arg, callback);
        }

        protected void initPictureLoader(Uri uri, LoaderManager.LoaderCallbacks<Cursor> callback)
        {
                if (ContentUris.parseId(uri) == -1)
                {
                        throw new IllegalArgumentException("uri and item id cannot be empty or -1");
                }

                Bundle arg = new Bundle();
                arg.putParcelable(ITEM_URI, uri);
                getLoaderManager().initLoader(ITEM_PICTURE_LOADER_ID, arg, callback);
        }

        @Override
        public boolean onCreateOptionsMenu(Menu menu)
        {
                getMenuInflater().inflate(R.menu.item_details, menu);
                mItemControl.onCreateOptionsMenu(menu);
                return true;
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem menuItem)
        {
                switch (menuItem.getItemId())
                {
                        case R.id.save_item_details:
                                mItemControl.onOk();
                                return true;
                        case R.id.menu_remove_item_from_list:
                                mItemControl.onDelete();
                                return true;
                        case android.R.id.home:
                                mItemControl.onUp();
                                return true;
                        default:
                                return super.onOptionsItemSelected(menuItem);
                }
        }

        public void warnChangesMade()
        {
                showUnsavedDialog(new DialogInterface.OnClickListener()
                {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                                dialog.dismiss();
                                mItemControl.onLeave();
                        }
                });
        }

        public void setExitTransition()
        {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                {
                        Transition slide = TransitionInflater.from(this).inflateTransition(R.transition.screen_slide_out_right);
                        getWindow().setReturnTransition(slide);
                }
        }

        public void setupEnterAnimationTransition()
        {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                {

                        getWindow().getEnterTransition().addListener(new Transition.TransitionListener()
                        {
                                @Override
                                public void onTransitionStart(Transition transition)
                                {

                                }

                                @Override
                                public void onTransitionEnd(Transition transition)
                                {
                                        mItemControl.onEnterTransitionEnd();
                                }

                                @Override
                                public void onTransitionCancel(Transition transition)
                                {

                                }

                                @Override
                                public void onTransitionPause(Transition transition)
                                {

                                }

                                @Override
                                public void onTransitionResume(Transition transition)
                                {

                                }
                        });

                }
        }

        public void finishItemEditing()
        {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                {
                        finishAfterTransition();
                }
                else
                {
                        finish();
                }
        }

        public void showTransientDbMessage()
        {
                Snackbar.make(mContainer, mDbMsg, 300).addCallback(new BaseTransientBottomBar.BaseCallback<Snackbar>()
                {
                        @Override
                        public void onDismissed(Snackbar transientBottomBar, int event)
                        {
                                finishItemEditing();
                        }
                }).show();
        }

        /**
         * Invoked when user click system back or navigate up with no intention of saving any pictures.
         * Does not delete original picture because it is not the intention of the user
         */
        public void removeUnsavedPicturesFromApp()
        {
                Picture newPicture = mPictureMgr.getNewPicture();
                if (newPicture != null)
                {
                        long picId = newPicture.getId();

                        /* If id  < 0, then this picture does not exist in database*/
                        if (picId == -1 && !PictureMgr.isExternalFile(newPicture))
                        {
                                int deletedFromFs = daoManager.deleteFileFromFilesystem(newPicture.getFile());
                        }
                }
        }

        public void startSnapShotActivity(File itemPicFile)
        {
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                /*
                  Performing this check is important because if you call startActivityForResult() using an intent that no app can handle, your app will crash.
                 */
                if (cameraIntent.resolveActivity(getPackageManager()) != null)
                {
                        /*
                          Returns a content:// URI. For more recent apps targeting Android 7.0 (API level 24) and higher, passing a file:// URI across a package boundary causes a
                         FileUriExposedException. Therefore, we now use a more generic way of storing images using a FileProvider.
                         We need to configure the FileProvider. In app's manifest, add a provider to your application

                         content://com.mirzairwan.shopping.fileprovider/item_images/Item__30012017_190919_-283901926.jpg
                         */
                        String appPackage = getApplicationContext().getPackageName();
                        Uri itemPicUri = FileProvider.getUriForFile(this, appPackage + ".fileprovider", itemPicFile);

                        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, itemPicUri);
                        startActivityForResult(cameraIntent, REQUEST_SNAP_PICTURE);
                }
                else
                {
                        Toast.makeText(this, "No camera app(s) found. Aborting camera operation", Toast.LENGTH_SHORT).show();
                }
        }

        /**
         * Use this when picking picture from media gallery. I do not need to create a
         * filename for the picture as it was created outside this app. This method query the filepath
         * of the picture using ContentResolver.
         */
        public void startPickPictureActivity()
        {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, REQUEST_PICK_PHOTO);
        }

        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data)
        {
                switch (resultCode)
                {
                        case Activity.RESULT_OK:
                                mItemControl.onChange();
                                switch (requestCode)
                                {
                                        case REQUEST_SNAP_PICTURE:
                                                mItemControl.onCameraResult();
                                                break;
                                        case REQUEST_PICK_PHOTO:
                                                Picture picture = getPictureFromGallery(data);
                                                mItemControl.onPickPictureResult(picture);
                                                break;
                                }
                                break;

                        default: /* Assume the worst. No picture from camera. Delete the useless file. */
                                mPictureMgr.deleteNewPicture();
                                setPictureView(mPictureMgr.getPictureInDb());
                }

                super.onActivityResult(requestCode, resultCode, data);
        }

        private Picture getPictureFromGallery(Intent data)
        {
                Uri photoUri = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                Cursor cursor = getContentResolver().query(photoUri, filePathColumn, null, null, null);
                String filePath = null;
                if (cursor.moveToFirst())
                {
                        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                        filePath = cursor.getString(columnIndex);
                        cursor.close();
                }

                if (filePath == null)
                {
                        return null;
                }
                else
                {
                        return new Picture(filePath);
                }

        }

        /**
         * Called after external camera task or picture picking task.
         * Also called when activity is being created and loading data.
         *
         * @param picture
         */
        public void setPictureView(Picture picture)
        {
                if (picture == null)
                {
                        mImgItemPic.setImageBitmap(null);
                        return;
                }

                /* Get the dimensions of the desired scale dimension */
                int targetW = getResources().getDimensionPixelSize(R.dimen.picture_item_detail_width);
                int targetH = getResources().getDimensionPixelSize(R.dimen.picture_item_detail_height);

                /* If picture is an external file, make sure read storage permission is granted */
                if (!PictureMgr.isExternalFile(picture) | PermissionHelper.hasReadStoragePermission(this))
                {
                        String path = picture.getFile().getPath();
                        mImgItemPic.setTag(path);

                        /* Spin a background thread to display picture */
                        ImageResizer imageResizer = new ImageResizer(this, targetW, targetH);
                        imageResizer.loadImage(picture.getFile(), mImgItemPic);
                }
        }

        @Override
        protected void onStart()
        {
                super.onStart();
        }

        @Override
        public void onBackPressed()
        {
                mItemControl.onBackPressed();
        }

        /**
         * Create and display a dialog with positive and negative button.
         * Clicking positive button will delete unsaved pictures and finish the activity.
         *
         * @param onLeaveClickListener
         */
        private void showUnsavedDialog(DialogInterface.OnClickListener onLeaveClickListener)
        {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.unsaved_changes);
                builder.setPositiveButton(R.string.discard, onLeaveClickListener);
                builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener()
                {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                                mItemControl.onStay();
                                dialog.dismiss();
                        }
                });
                builder.show();
        }

        protected void alertItemInShoppingList(int messageId)
        {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(messageId);
                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener()
                {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                                dialog.dismiss();
                        }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
        }

        public void showSoftKeyboard(View view)
        {
                if(view.requestFocus())
                {
                        InputMethodManager inputMtdMgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputMtdMgr.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
                }
        }
        /**
         * Select following child records of  item:
         * 1. Prices
         * 2. Pictures
         *
         * @param loaderId Identify type of SQL query
         * @param args     Store item id
         * @return
         */
        @Override
        public Loader<Cursor> onCreateLoader(int loaderId, Bundle args)
        {
                String[] projection = null;
                Uri uri = args.getParcelable(ITEM_URI);
                Loader<Cursor> loader = null;
                mItemId = -1;
                String selection = null;
                String[] selectionArgs = null;

                switch (loaderId)
                {
                        case ITEM_PRICE_LOADER_ID:
                                projection = new String[]{
                                        PricesEntry._ID, PricesEntry.COLUMN_ITEM_ID, PricesEntry.COLUMN_PRICE_TYPE_ID, PricesEntry.COLUMN_PRICE, PricesEntry.COLUMN_BUNDLE_QTY, PricesEntry.COLUMN_CURRENCY_CODE, PricesEntry.COLUMN_SHOP_ID};
                                mItemId = ContentUris.parseId(uri);
                                if (mItemId == -1)
                                {
                                        throw new IllegalArgumentException("uri and item id cannot be empty or -1");
                                }

                                selection = PricesEntry.COLUMN_ITEM_ID + "=?";
                                selectionArgs = new String[]{String.valueOf(mItemId)};
                                loader = new CursorLoader(this, PricesEntry.CONTENT_URI, projection, selection, selectionArgs, null);
                                break;

                        case ITEM_PICTURE_LOADER_ID:
                                projection = new String[]{
                                        PicturesEntry._ID, PicturesEntry.COLUMN_FILE_PATH, PicturesEntry.COLUMN_ITEM_ID};
                                mItemId = ContentUris.parseId(uri);
                                if (mItemId == -1)
                                {
                                        throw new IllegalArgumentException("uri and item id cannot be empty or -1");
                                }

                                selection = PicturesEntry.COLUMN_ITEM_ID + "=?";
                                selectionArgs = new String[]{String.valueOf(mItemId)};
                                loader = new CursorLoader(this, PicturesEntry.CONTENT_URI, projection, selection, selectionArgs, null);
                                break;

                        default:
                                throw new IllegalArgumentException("Unsupported query");

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
                int colItemId;
                int loaderId = loader.getId();
                switch (loaderId)
                {
                        case ITEM_PRICE_LOADER_ID:
                                mPriceMgr.createPrices(cursor);
                                onLoadPriceFinished(mPriceMgr);

                                /*
                                Important to move cursor back before the first record because when device switches to landscape, it gives back the same cursor with
                                 the pre-exisiting index
                                 */
                                cursor.moveToPosition(-1);
                                break;

                        case ITEM_PICTURE_LOADER_ID:
                                Picture pictureInDb = createPicture(cursor);
                                colItemId = cursor.getColumnIndex(PicturesEntry.COLUMN_ITEM_ID);
                                mItemId = cursor.getLong(colItemId);
                                mPictureMgr.setItemId(mItemId);
                                mPictureMgr.setDbPicture(pictureInDb);
                                mItemControl.onLoadPictureFinished(mPictureMgr);

                                /* Important to move cursor back before the first record because when device switches to landscape, it gives back the same cursor with
                                 the pre-exisiting index */
                                cursor.moveToPosition(-1);
                                break;
                }
        }

        protected abstract void onLoadPriceFinished(PriceMgr priceMgr);

        protected Picture createPicture(Cursor cursor)
        {
                int colRowId = cursor.getColumnIndex(PicturesEntry._ID);
                int colPicturePath = cursor.getColumnIndex(PicturesEntry.COLUMN_FILE_PATH);
                Picture pictureInDb = null;
                if (cursor.moveToFirst())
                {
                        pictureInDb = new Picture(cursor.getLong(colRowId), cursor.getString(colPicturePath));
                }

                return pictureInDb;
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader)
        {
                clearPictureField();
        }

        private void clearPictureField()
        {
                mImgItemPic.setImageBitmap(null);
        }

        @Override
        public void onSaveInstanceState(Bundle outState)
        {
                Log.d(LOG_TAG, "onSaveInstanceState");
                super.onSaveInstanceState(outState);
                //outState.putParcelable(PICTURE_MANAGER, mPictureMgr);
                outState.putParcelable(EXCHANGE_RATE, mExchangeRate);
        }

        protected abstract String getCurrencyCode();

        class ImageFsResizer extends AsyncTask<String, Void, Bitmap>
        {
                private WeakReference<ImageView> imageViewReference;

                public ImageFsResizer(ImageView imageView)
                {
                        imageViewReference = new WeakReference<ImageView>(imageView);
                }

                @Override
                protected Bitmap doInBackground(String... picturePath)
                {
                        int width = getResources().getDimensionPixelSize(R.dimen.picture_item_detail_width);
                        int height = getResources().getDimensionPixelSize(R.dimen.picture_item_detail_height);
                        Bitmap resizedBitmap = PictureUtil.decodeSampledBitmapFile(picturePath[0], width, height);
                        String appPackage = getApplicationContext().getPackageName();
                        Uri itemPicUri = FileProvider.getUriForFile(ItemActivity.this, appPackage + ".fileprovider", mPictureMgr.getNewPicture().getFile());

                        /* Delete the big picture file */
                        int deleted = getContentResolver().delete(itemPicUri, null, null);

                        /* Save the resized image */
                        PictureUtil.savePictureInFilesystem(resizedBitmap, picturePath[0]);
                        return resizedBitmap;
                }

                @Override
                protected void onPostExecute(Bitmap bitmap)
                {
                        if (imageViewReference != null && bitmap != null)
                        {
                                ImageView imageView = imageViewReference.get();
                                if (imageView != null)
                                {
                                        imageView.setImageBitmap(bitmap);
                                }
                        }
                }
        }

        protected class ItemExchangeRateLoaderCallback extends ExchangeRateLoaderCallback
        {
                private final String LOG_TAG = ItemExchangeRateLoaderCallback.class.getSimpleName();
                private List<PriceField> priceFields = new ArrayList<>();

                ItemExchangeRateLoaderCallback(Context context)
                {
                        super(context, mExchangeRateInput);
                }

                @Override
                public void onLoadFinished(Loader<Map<String, ExchangeRate>> loader, Map<String, ExchangeRate> exchangeRates)
                {
                        Log.d(LOG_TAG, "onLoadFinished");
                        Log.d(LOG_TAG, ">>> Loader id: " + loader.getId());
                        Log.d("CurrencyCode", "onLoadFinished");
                        if (exchangeRates != null)
                        {
                                String sourceCurrencyCode = getCurrencyCode();
                                mExchangeRate = exchangeRates.get(sourceCurrencyCode);
                        }
                        else
                        {
                                Log.d(LOG_TAG, ">>> Exchange rates is null");
                        }
                }

                @Override
                public void onLoaderReset(Loader<Map<String, ExchangeRate>> loader)
                {
                        Log.d(LOG_TAG, "onLoaderReset");
                        mExchangeRate = null;
                }
        }
}
