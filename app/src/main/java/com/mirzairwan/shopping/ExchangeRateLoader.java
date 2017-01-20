package com.mirzairwan.shopping;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.mirzairwan.shopping.domain.ExchangeRate;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by Mirza Irwan on 13/1/17.
 * It fetches ECB exchange rates using web API.
 */

class ExchangeRateLoader extends AsyncTaskLoader<Map<String, ExchangeRate>>
{
    private static final String LOG_TAG = ExchangeRateLoader.class.getSimpleName();
    private String mDestCurrencyCode;
    private String mBaseUri;
    private Set<String> mSourceCurrencies; //Input to fetch exchange rate

    ExchangeRateLoader(Context context, Set<String> sourceCurrencies, String baseUri, String
            destCurrencyCode)
    {
        super(context);
        Log.d(LOG_TAG, ">>>>>>> Construct ExchangeRateLoader()");
        Log.d(LOG_TAG, ">>>>>>> base uri " + baseUri);
        Log.d(LOG_TAG, ">>>>>>> destCurrencyCode " + destCurrencyCode);
        Log.d(LOG_TAG, ">>>>>>> sourceCurrencies " + sourceCurrencies);

        if (baseUri == null)
        {
            throw new IllegalArgumentException("Web uri cannot be NULL");
        }

        if (destCurrencyCode == null)
        {
            throw new IllegalArgumentException("Destination currency code cannot be NULL");
        }
        mSourceCurrencies = sourceCurrencies;
        mBaseUri = baseUri;
        mDestCurrencyCode = destCurrencyCode;
    }

    /**
     * Good place to cache the exchange rate. Just remember this is on the UI thread so nothing lengthy!
     */
    @Override
    public void deliverResult(Map<String, ExchangeRate> exchangeRates)
    {
        //TODO: Cache the exchange rate
        super.deliverResult(exchangeRates);
    }


    @Override
    protected void onStartLoading()
    {
        Log.d(LOG_TAG, ">>>>>>> onStartLoading: " + getId());

        if (mSourceCurrencies == null || mSourceCurrencies.size() == 0)
        {
            Log.d(LOG_TAG, ">>>>>>> onStartLoading source currency is empty. Abort");
            deliverResult(null);
        }
        else
        {
            Log.d(LOG_TAG, ">>>>>>> source currency: " + getId() + " " + mSourceCurrencies);
            forceLoad();
        }

    }

    @Override
    public Map<String, ExchangeRate> loadInBackground()
    {
        Log.d(LOG_TAG, ">>>>>>> loadInBackground start");

        String queryWeb = createQueryUri();
        Log.d(LOG_TAG, ">>>>>>> web uri " + queryWeb);
        String jsonResp = null;

        try
        {
            jsonResp = makeHttpExchangeRateRequest(queryWeb);
        } catch (IOException e)
        {
            e.printStackTrace();
        } catch (Exception ex)
        {
            return null;
        }
        if (jsonResp == null)
        {
            return null;
        }

        Map<String, ExchangeRate> rates = null;
        try
        {
            rates = extractJsonResponse(jsonResp);
        } catch (JSONException e)
        {
            e.printStackTrace();
        }

        Log.d(LOG_TAG, ">>>>>>> loadInBackground end");
        return rates;
    }

    private Map<String, ExchangeRate> extractJsonResponse(String jsonResp) throws JSONException
    {
        Map<String, ExchangeRate> exchangeRates = new HashMap<>();
        JSONObject root = new JSONObject(jsonResp);
        String base = root.getString("base");
        String strDate = root.getString("date");
        Date date = FormatHelper.strToDate(strDate, "-");
        JSONObject rates = root.getJSONObject("rates");
        for (String sourceCurrency : mSourceCurrencies)
        {
            double rateVal = rates.getDouble(sourceCurrency);
            ExchangeRate fc = new ExchangeRate(sourceCurrency, base, rateVal, date);
            exchangeRates.put(sourceCurrency, fc);
        }

        return exchangeRates;
    }

    private String makeHttpExchangeRateRequest(String urlApi) throws IOException
    {
        URL url;
        HttpURLConnection conn = null;
        InputStream is = null;
        try
        {
            url = new URL(urlApi);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(60000 /*1min*/);
            int respCode = conn.getResponseCode();
            if (respCode >= 200 && respCode < 300)
            {
                is = conn.getInputStream();
            }

        } catch (MalformedURLException e)
        {
            e.printStackTrace();
            throw new IllegalArgumentException("Illegal url", e);
        } finally
        {

            if (conn != null)
            {
                conn.disconnect();
            }
        }

        String resp = null;
        StringBuilder sb = new StringBuilder();
        if (is != null)
        {
            BufferedInputStream bis = new BufferedInputStream(is);
            InputStreamReader inputStreamReader = new InputStreamReader(bis);
            BufferedReader br = new BufferedReader(inputStreamReader);
            try
            {
                resp = br.readLine();

                while (resp != null)
                {
                    sb.append(resp);
                    resp = br.readLine();
                }
            } finally
            {
                try
                {
                    is.close();
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
            }

        }
        return sb.toString();
    }

    private String createQueryUri()
    {
        Uri.Builder builder = Uri.parse(mBaseUri).buildUpon().appendQueryParameter("base",
                mDestCurrencyCode);
        String symbols = "";
        Iterator<String> iterator = mSourceCurrencies.iterator();

        while (iterator.hasNext())
        {
            String srcCurrencyCode = iterator.next();
            symbols += srcCurrencyCode;
            if (iterator.hasNext())
            {
                symbols += ",";
            }
        }
        builder.appendQueryParameter("symbols", symbols);
        String queryUri = builder.build().toString();
        Log.d(LOG_TAG, ">>>" + queryUri);
        return queryUri;
    }

}