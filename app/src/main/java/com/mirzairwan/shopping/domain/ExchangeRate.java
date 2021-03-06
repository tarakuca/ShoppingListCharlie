package com.mirzairwan.shopping.domain;

import android.os.Parcel;
import android.os.Parcelable;

import com.mirzairwan.shopping.FormatHelper;

import java.util.Date;


 /**
 * Created by Mirza Irwan on 18/12/16.
 * Copyright 2017, Mirza Irwan Bin Osman , All rights reserved.
 * Contact owner at mirza.irwan.osman@gmail.com
 */

public class ExchangeRate implements Parcelable
{
    private String mSourceCurrencyCode; //source currency
    private String mDestCurrencyCode;  //destination currency or base currency
    private double mRate; //One unit of mDestCurrency or mBaseCurrency = mRate X mSourceCurrency
    private Date mDate;

    public final static String FOREIGN_CURRENCY_CODES = "FOREIGN_CURRENCY_CODES";
    public final static String FOREX_API_URL = "FOREX_API_URL";
    public final static String DESTINATION_CURRENCY_CODE = "DESTINATION_CURRENCY_CODE";

    public ExchangeRate(String sourceCurrencyCode, String destinationCurrencyCode, double rate,
                        Date date)
    {
        mSourceCurrencyCode = sourceCurrencyCode;
        mDestCurrencyCode = destinationCurrencyCode;
        mRate = rate;
        mDate = date;
    }

    protected ExchangeRate(Parcel in)
    {
        mSourceCurrencyCode = in.readString();
        mDestCurrencyCode = in.readString();
        mRate = in.readDouble();
        mDate = new Date(in.readLong());
    }

//    public double compute(double sourceCurrencyVal)
//    {
//        return (1 / mRate) * sourceCurrencyVal;
//    }

    public String getDestCurrencyCode()
    {
        return mDestCurrencyCode;
    }

    public String getSourceCurrencyCode()
    {
        return mSourceCurrencyCode;
    }

    /**
     * Translate to equivalent value in destination currency.
     * @param sourceCurrencyVal Value to be translated
     * @param destCurrencyCode Currency code of destination currency
     * @return Translated value. If source destination currency code is different from base
     * currency, an IllegalArgumentException is thrown
     */
    public double compute(double sourceCurrencyVal, String destCurrencyCode)
    {
        if (destCurrencyCode.equals(mDestCurrencyCode))
        {
            return (1 / mRate) * sourceCurrencyVal;
        }
        else
        {
            throw new IllegalArgumentException("Destination currency is " + destCurrencyCode +
                                                " but base currency of exchange rate is " + mDestCurrencyCode);
            //return sourceCurrencyVal;
        }
    }

    public static final Creator<ExchangeRate> CREATOR = new Creator<ExchangeRate>()
    {
        @Override
        public ExchangeRate createFromParcel(Parcel in)
        {
            return new ExchangeRate(in);
        }

        @Override
        public ExchangeRate[] newArray(int size)
        {
            return new ExchangeRate[size];
        }
    };

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeString(mSourceCurrencyCode);
        dest.writeString(mDestCurrencyCode);
        dest.writeDouble(mRate);
        dest.writeLong(mDate.getTime());
    }

    @Override
    public String toString()
    {
        return mDestCurrencyCode + " = "  + FormatHelper.formatToTwoDecimalPlaces(1/mRate)
                                                            + " x " + mSourceCurrencyCode;
    }
}
