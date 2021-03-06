package com.mirzairwan.shopping;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * Created by Mirza Irwan on 13/1/17.
 * Copyright 2017, Mirza Irwan Bin Osman , All rights reserved.
 * Contact owner at mirza.irwan.osman@gmail.com
 */

public class PictureUtil
{
        public static final String LOG_TAG = PictureUtil.class.getSimpleName();
        private static final String SHOPPING_LIST_PICS = "Item_";

        public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight)
        {
                // Raw height and width of image
                final int height = options.outHeight;
                final int width = options.outWidth;
                int inSampleSize = 1;

                if (height > reqHeight || width > reqWidth)
                {

                        final int halfHeight = height / 2;
                        final int halfWidth = width / 2;

                        // Calculate the largest inSampleSize value that is a power of 2 and keeps both
                        // height and width larger than the requested height and width.
                        while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth)
                        {
                                inSampleSize *= 2;
                        }
                }

                return inSampleSize;
        }

        /**
         * Rescale in memory
         * @param pathName
         * @param reqWidth
         * @param reqHeight
         * @return
         */
        public static Bitmap decodeSampledBitmapFile(String pathName, int reqWidth, int reqHeight)
        {

                // First decode with inJustDecodeBounds=true to check dimensions
                final BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(pathName, options);

                // Calculate inSampleSize
                options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

                // Decode bitmap with inSampleSize set
                options.inJustDecodeBounds = false;
                Bitmap bmp = BitmapFactory.decodeFile(pathName, options);
                return correctOrientation(bmp, pathName);
        }

        public static Bitmap correctOrientation(Bitmap bitmap, String path)
        {

                ExifInterface ei;
                try
                {
                        ei = new ExifInterface(path);
                        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                        switch (orientation)
                        {
                                case ExifInterface.ORIENTATION_ROTATE_90:
                                        bitmap = rotateImage(bitmap, 90);
                                        break;
                                case ExifInterface.ORIENTATION_ROTATE_180:
                                        bitmap = rotateImage(bitmap, 180);
                                        break;
                                case ExifInterface.ORIENTATION_ROTATE_270:
                                        bitmap = rotateImage(bitmap, 270);
                                        break;
                        }
                }
                catch(IOException e)
                {
                        e.printStackTrace();
                }

                return bitmap;
        }

        public static Bitmap rotateImage(Bitmap source, float angle)
        {

                Bitmap bitmap = null;
                Matrix matrix = new Matrix();
                matrix.postRotate(angle);
                try
                {
                        bitmap = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
                }
                catch(OutOfMemoryError err)
                {
                        err.printStackTrace();
                }
                return bitmap;
        }

        public static void savePictureInFilesystem(Bitmap out, String originalImagePath)
        {
                File newImageFile = new File(originalImagePath);
                FileOutputStream fOut;
                try
                {
                        fOut = new FileOutputStream(newImageFile);
                        out.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
                        fOut.flush();
                        fOut.close();
                }
                catch(IOException ioEx)
                {

                }
        }

        /**
         * Create a collision-resistant file name
         * @param dirPictures
         * @return
         * @throws IOException
         */
        public static File createFileHandle(File dirPictures) throws IOException
        {
                String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date());
                String picFilename = SHOPPING_LIST_PICS + "_" + timeStamp + "_";

                //Get file handle
                File filePicture = File.createTempFile(picFilename, ".jpg", dirPictures);

                return filePicture;
        }
}
