package com.mirzairwan.shopping;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.io.File;
import java.lang.ref.WeakReference;

/**
 * Created by Mirza Irwan on 4/1/17.
 */

public abstract class ImageWorker
{
    protected Resources mResources;


    protected ImageWorker(Context context) {
        mResources = context.getResources();
    }

    /**
     * Load an image specified by the data parameter into an ImageView
     * @param file The URL of the image to download.
     * @param imageView The ImageView to bind the downloaded image to.
     */
    public void loadImage(File file, ImageView imageView) {
        if(!file.exists())
            return;
        BitmapWorkerTask task = new BitmapWorkerTask(imageView);
        task.execute(file);
    }




    /**
     * Subclasses should override this to define any processing or work that must happen to produce
     * the final bitmap. This will be executed in a background thread and be long running. For
     * example, you could resize a large bitmap here, or pull down an image from the network.
     *
     * @param file The data to identify which image to process, as provided by
     * @return The processed bitmap
     */
    protected abstract Bitmap processBitmap(File file);

    /**
     * Called when the processing is complete and the final drawable should be
     * set on the ImageView.
     *
     * @param imageView
     * @param bitmap
     */
    private void setImageDrawable(ImageView imageView, Bitmap bitmap)
    {
        imageView.setImageBitmap(bitmap);
    }



    class BitmapWorkerTask extends AsyncTask<File, Void, Bitmap>
    {
        private final WeakReference<ImageView> imageViewReference;

        public BitmapWorkerTask(ImageView imageView)
        {
            // Use a WeakReference to ensure the ImageView can be garbage collected
            imageViewReference = new WeakReference<ImageView>(imageView);

        }

        @Override
        protected Bitmap doInBackground(File... params)
        {
            return processBitmap(params[0]);

        }

        // Once complete, see if ImageView is still around and set bitmap.
        @Override
        protected void onPostExecute(Bitmap bitmap)
        {
            if (imageViewReference != null && bitmap != null) {
                final ImageView imageView = imageViewReference.get();
                if (imageView != null) {
                    setImageDrawable(imageView, bitmap);
                }
            }

        }
    }
}
