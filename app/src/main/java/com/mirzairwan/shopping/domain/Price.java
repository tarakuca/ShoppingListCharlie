package com.mirzairwan.shopping.domain;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

import static com.mirzairwan.shopping.domain.Price.Type.BUNDLE_PRICE;
import static com.mirzairwan.shopping.domain.Price.Type.UNIT_PRICE;

/**
 * Created by Mirza Irwan on 18/12/16.
 * Copyright 2017, Mirza Irwan Bin Osman , All rights reserved.
 * Contact owner at mirza.irwan.osman@gmail.com
 *
 * Price information of item added to Shopping List. This class in independent of quantity.
 * Created by Mirza Irwan on 17/11/16.
 */
public class Price implements Parcelable
{


    public enum Type
    {
        //All price types must follow ordered sequence
        UNIT_PRICE(0), BUNDLE_PRICE(1);

        private int mType;

        Type(int type)
        {
            mType = type;
        }

        public int getType()
        {
            return mType;
        }

    }

    private long _id;
    private double mUnitPrice = 0.00d;
    private double mBundlePrice = 0.00d;
    private double mBundleQuantity = 0.00d;
    private Price.Type mPriceType = UNIT_PRICE; //Use unit price as default
    private String mCurrencyCode;
    private long mShopId = 1;
    private Date mLastUpdatedOn;

    public Price(long id)
    {
        _id = id;
    }

    /**
     * Constructor for bundle price
     * @param bundlePrice Price of one bundle of items
     * @param bundleQuantity Number of items in one bundle
     * @param currencyCode Code of currency
     * @param shopId The identifier of the shop
     */
    public Price(double bundlePrice, double bundleQuantity,
                 String currencyCode, long shopId)
    {
        mBundlePrice = bundlePrice;
        mBundleQuantity = bundleQuantity;
        mPriceType = BUNDLE_PRICE;
        mCurrencyCode = currencyCode;
        mShopId = shopId;
    }

    /**
     * Constructor for bundle price
     * @param  _id Issued by database persistence
     * @param bundlePrice Price of one bundle of items
     * @param bundleQuantity Number of items in one bundle
     * @param currencyCode Code of currency
     * @param shopId The identifier of the shop
     * @param lastUpdatedOn Timestamp of database update
     */
    public Price(long _id, double bundlePrice, double bundleQuantity,
                 String currencyCode, long shopId, Date lastUpdatedOn)
    {
        this._id = _id;
        mBundlePrice = bundlePrice;
        mBundleQuantity = bundleQuantity;
        mPriceType = BUNDLE_PRICE;
        mCurrencyCode = currencyCode;
        mShopId = shopId;;
        mLastUpdatedOn = lastUpdatedOn;
    }

    /**
     * Construct Unit Price
     * @param unitPrice Price of pne unit
     * @param currencyCode The currency code
     * @param shopId The identifier of the shop
     */
    public Price(double unitPrice, String currencyCode, long shopId)
    {
        mUnitPrice = unitPrice;
        mPriceType = UNIT_PRICE;
        mCurrencyCode = currencyCode;
        mShopId = shopId;
    }

    /**
     * Construct Unit Price
     * @param  _id Issued by database persistence
     * @param unitPrice Price of pne unit
     * @param currencyCode The currency code
     * @param shopId The identifier of the shop
     * @param lastUpdatedOn Timestamp of database update
     */
    public Price(long _id, double unitPrice, String currencyCode, long shopId, Date lastUpdatedOn)
    {
        this._id = _id;
        mUnitPrice = unitPrice;
        mPriceType = UNIT_PRICE;
        mCurrencyCode = currencyCode;
        mShopId = shopId;
        mLastUpdatedOn = lastUpdatedOn;
    }

    public long getId()
    {
        return _id;
    }

    public void setId(long _id)
    {
        this._id = _id;
    }

    public void setCurrencyCode(String currencyCode)
    {
        mCurrencyCode = currencyCode;
    }

    public String getCurrencyCode()
    {
        return mCurrencyCode;
    }

    public Price.Type getPriceType()
    {
        return mPriceType;
    }

    long getShopId()
    {
        return mShopId;
    }

    public void setLastUpdatedOn(Date lastUpdatedOn)
    {
        mLastUpdatedOn = lastUpdatedOn;
    }

    public void setUnitPrice(double unitPrice)
    {
        mUnitPrice = unitPrice;
    }

    public double getUnitPrice()
    {
        return mUnitPrice;
    }

    public void setBundlePrice(double bundlePrice, double bundleQuantity)
    {
        mBundleQuantity = bundleQuantity;
        mBundlePrice = bundlePrice;
    }

    public double getBundlePrice()
    {
        return mBundlePrice;
    }

    public double getBundleQuantity()
    {
        return mBundleQuantity;
    }


    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeLong(_id);
        dest.writeDouble(mUnitPrice);
        dest.writeDouble(mBundlePrice);
        dest.writeDouble(mBundleQuantity);
        dest.writeSerializable(mPriceType);
        dest.writeString(mCurrencyCode);
        dest.writeLong(mShopId);
        if(mLastUpdatedOn != null)
            dest.writeLong(mLastUpdatedOn.getTime());
    }

    public static final Creator<Price> CREATOR
            = new Creator<Price>() {
        public Price createFromParcel(Parcel in) {
            return new Price(in);
        }

        public Price[] newArray(int size) {
            return new Price[size];
        }
    };

    private Price(Parcel in)
    {
        _id = in.readLong();
        mUnitPrice = in.readDouble();
        mBundlePrice = in.readDouble();
        mBundleQuantity = in.readDouble();
        mPriceType = (Price.Type)in.readSerializable();
        mCurrencyCode = in.readString();
        mShopId = in.readLong();
        mLastUpdatedOn = in.readLong() > 0? new Date(in.readLong()) : null;

    }

}
