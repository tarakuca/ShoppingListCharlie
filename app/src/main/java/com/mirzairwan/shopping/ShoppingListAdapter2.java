package com.mirzairwan.shopping;

import android.content.Context;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mirzairwan.shopping.data.Contract.ItemsEntry;
import com.mirzairwan.shopping.data.Contract.PicturesEntry;
import com.mirzairwan.shopping.data.Contract.PricesEntry;
import com.mirzairwan.shopping.data.Contract.ToBuyItemsEntry;

import java.io.File;

/**
 * Created by Mirza Irwan on 18/12/16.
 */

public class ShoppingListAdapter2 extends CursorAdapter
{
    private static final String LOG_TAG = ShoppingListAdapter2.class.getSimpleName();
    private ImageResizer mImageResizer;
    private OnCheckBuyItemListener mOnFragmentInteractionListener;

    public ShoppingListAdapter2(Context context, Cursor cursor,
                                OnCheckBuyItemListener onFragmentInteractionListener, ImageResizer imageResizer
    )
    {
        super(context, cursor, 0);
        mOnFragmentInteractionListener = onFragmentInteractionListener;
        mImageResizer = imageResizer;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent)
    {
        View convertView = LayoutInflater.from(context).inflate(R.layout.row_buy_item2, parent, false);
        TagBuyItemViews tag = new TagBuyItemViews();
        tag.ivItem = (ImageView) convertView.findViewById(R.id.iv_item_pic_thb);
        tag.tvItemName = (TextView) convertView.findViewById(R.id.item_name_row);
        tag.tvItemBrand = (TextView) convertView.findViewById(R.id.item_brand_row);
        tag.tvSelectedPrice = (TextView) convertView.findViewById(R.id.item_selected_price_row);
        tag.tvItemQty = (TextView) convertView.findViewById(R.id.item_qty_row);
        tag.checkItem = (CheckBox) convertView.findViewById(R.id.check_item);

        //Create a listener for checkbox checked events
        OnItemCheckedChangeListener onItemCheckedChangeListener =
                new OnItemCheckedChangeListener(mOnFragmentInteractionListener);

        tag.onItemCheckedChangeListener = onItemCheckedChangeListener;
        convertView.setTag(tag);
        return convertView;
    }

    @Override
    public void bindView(View convertView, Context context, Cursor cursor)
    {

        TagBuyItemViews tagViews = (TagBuyItemViews) convertView.getTag();

        int colPicPath = cursor.getColumnIndex(PicturesEntry.COLUMN_FILE_PATH);
        String pathPic = cursor.getString(colPicPath);

        Log.d(LOG_TAG, ">>> bindView " + pathPic);

        //tagViews.ivItem.setImageResource(R.drawable.empty_photo);
        setImageView(pathPic, tagViews.ivItem);

        int colNameIdx = cursor.getColumnIndex(ItemsEntry.COLUMN_NAME);
        tagViews.tvItemName.setText(cursor.getString(colNameIdx));

        int colBrandIdx = cursor.getColumnIndex(ItemsEntry.COLUMN_BRAND);
        String brand = cursor.getString(colBrandIdx);
        if (!TextUtils.isEmpty(brand))
            tagViews.tvItemBrand.setText(brand);
        else
            tagViews.tvItemBrand.setText(R.string.default_brand_name);

        tagViews.checkItem.setOnCheckedChangeListener(null); //Disable checkbox listener to disable firing pre-existing checked items on the buy list
        int colIsChecked = cursor.getColumnIndex(ToBuyItemsEntry.COLUMN_IS_CHECKED);
        tagViews.checkItem.setChecked(cursor.getInt(colIsChecked) == 1); //Now it is safe to associate the 'checked' status of buy  with the checkbox
        tagViews.onItemCheckedChangeListener.setCursorPosition(cursor.getPosition()); //Associate the listener with the position on the shopping list
        tagViews.checkItem.setOnCheckedChangeListener(tagViews.onItemCheckedChangeListener); //Enable checkbox listener
        tagViews.checkItem.setFocusable(false);

        int colCurrencyCodeIdx = cursor.getColumnIndex(PricesEntry.COLUMN_CURRENCY_CODE);
        String currencyCode = cursor.getString(colCurrencyCodeIdx);
        int colSelectedPriceTagIdx = cursor.getColumnIndex(PricesEntry.COLUMN_PRICE);
        double priceTag = cursor.getDouble(colSelectedPriceTagIdx);
        String countryCode = PreferenceManager.getDefaultSharedPreferences(context).getString("home_country_preference", null);
        tagViews.tvSelectedPrice.setText(FormatHelper.formatCountryCurrency(countryCode,
                currencyCode, priceTag / 100));

        int colBuyItemQty = cursor.getColumnIndex(ToBuyItemsEntry.COLUMN_QUANTITY);
        tagViews.tvItemQty.setText(String.valueOf(cursor.getInt(colBuyItemQty)));
        tagViews.tvItemQty.setFocusable(false);
    }

    private void setImageView(String pathPic, ImageView ivItem)
    {
        if (TextUtils.isEmpty(pathPic)) {
            ivItem.setImageResource(R.drawable.empty_photo);
            return;
        }
        mImageResizer.loadImage(new File(pathPic), ivItem);
    }

    private static class TagBuyItemViews
    {
        ImageView ivItem;
        TextView tvItemName;
        TextView tvItemBrand;
        TextView tvItemQty;
        CheckBox checkItem;
        OnItemCheckedChangeListener onItemCheckedChangeListener;
        public TextView tvSelectedPrice;
    }

    private static class OnItemCheckedChangeListener implements CompoundButton.OnCheckedChangeListener
    {
        private OnCheckBuyItemListener mOnFragmentInteractionListener;
        private int mBuyItemPosition;

        public OnItemCheckedChangeListener(OnCheckBuyItemListener onFragmentInteractionListener)
        {
            mOnFragmentInteractionListener = onFragmentInteractionListener;
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
        {
            mOnFragmentInteractionListener.onCheckBuyItem(isChecked, mBuyItemPosition);
        }

        public void setCursorPosition(int position)
        {
            mBuyItemPosition = position;
        }
    }

    public interface OnCheckBuyItemListener
    {
        void onCheckBuyItem(boolean isChecked, int mBuyItemPosition);
    }
}
