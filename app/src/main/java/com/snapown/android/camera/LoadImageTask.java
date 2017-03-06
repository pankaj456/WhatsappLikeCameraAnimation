
package com.snapown.android.camera;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ImageView;

import java.util.concurrent.ThreadPoolExecutor;


/**
 * Created by Praveen on 22/02/17.
 */

public class LoadImageTask extends AsyncTask <byte[],Void,Bitmap>
{
    private  boolean is_front;
    private ImageView imageView;
    private int required_width;
    private int required_height;
    private  int orientation;
    private byte[] imageData;




    public LoadImageTask(byte[] data,boolean is_front, ImageView imageView, int required_width, int required_height)
    {


        this.is_front = is_front;
        this.imageView = imageView;
        this.required_width = required_width;
        this.required_height = required_height;
         this.orientation = getOrientation(data);



    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();

    }

    @Override
    protected Bitmap doInBackground(final byte[]... data)
    {


        return rotateBitmap(decodeSampledBitmapFromResource(data[0],required_width,required_height),orientation);


    }

    @Override
    protected void onPostExecute(final Bitmap bitmap)
    {
         showImage(imageView,bitmap);



    }

    public void showImage(final ImageView imageView, final Bitmap bitmap)
    {

        imageView.setImageBitmap(bitmap);




    }
    public static Bitmap decodeSampledBitmapFromResource(byte[] data, int w, int h)
    {


        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(data,0,data.length,options);
        options.inSampleSize = Math.max(options.outWidth/w, options.outHeight/h);
        options.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeByteArray(data,0,data.length,options);
        return bitmap;
    }

    private Bitmap rotateBitmap(Bitmap bitmap, int orientation)
    {
        Matrix matrix = new Matrix();

        if (is_front)
        {
            matrix.setRotate(-90);
            matrix.postScale(-1, 1);
            try {
                Bitmap bmRotated = Bitmaps.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                bitmap.recycle();
                return bmRotated;
            } catch (OutOfMemoryError ignore) {
                return null;
            }

        }
        else
        {


            switch (orientation)
            {
                case ExifInterface.ORIENTATION_NORMAL:
                    return bitmap;
                case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                    matrix.setScale(-1, 1);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    matrix.setRotate(-90);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    matrix.setRotate(90);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    matrix.setRotate(180);
                    break;
                case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                    matrix.setRotate(180);
                    matrix.postScale(-1, 1);
                    break;
                case ExifInterface.ORIENTATION_TRANSPOSE:
                    matrix.setRotate(90);
                    matrix.postScale(-1, 1);
                    break;

                case ExifInterface.ORIENTATION_TRANSVERSE:
                    matrix.setRotate(-90);
                    matrix.postScale(-1, 1);
                    break;

                default:
                    return bitmap;

            }


            try
            {
                Bitmap bmRotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                Log.d("BitmapAfterDecoded",+bitmap.getWidth()+"&"+bitmap.getHeight());
                Log.d("BitmapRoatatedFinal",+bmRotated.getWidth()+"&"+bmRotated.getHeight());
                bitmap.recycle();
                return bmRotated;
            }
            catch (OutOfMemoryError ignore)
            {
                return null;
            }
        }

    }


    private static int getOrientation(byte[] jpeg) {
        if (jpeg == null) {
            return 0;
        }

        int offset = 0;
        int length = 0;

        while (offset + 3 < jpeg.length && (jpeg[offset++] & 0xFF) == 0xFF) {
            int marker = jpeg[offset] & 0xFF;

            if (marker == 0xFF) {
                continue;
            }
            offset++;

            if (marker == 0xD8 || marker == 0x01) {
                continue;
            }
            if (marker == 0xD9 || marker == 0xDA) {
                break;
            }

            length = pack(jpeg, offset, 2, false);
            if (length < 2 || offset + length > jpeg.length) {
                return 0;
            }

            // Break if the marker is EXIF in APP1.
            if (marker == 0xE1 && length >= 8 &&
                    pack(jpeg, offset + 2, 4, false) == 0x45786966 &&
                    pack(jpeg, offset + 6, 2, false) == 0) {
                offset += 8;
                length -= 8;
                break;
            }

            offset += length;
            length = 0;
        }

        if (length > 8) {
            int tag = pack(jpeg, offset, 4, false);
            if (tag != 0x49492A00 && tag != 0x4D4D002A) {
                return 0;
            }
            boolean littleEndian = (tag == 0x49492A00);

            int count = pack(jpeg, offset + 4, 4, littleEndian) + 2;
            if (count < 10 || count > length) {
                return 0;
            }
            offset += count;
            length -= count;

            count = pack(jpeg, offset - 2, 2, littleEndian);
            while (count-- > 0 && length >= 12) {
                tag = pack(jpeg, offset, 2, littleEndian);
                if (tag == 0x0112) {
                    int orientation = pack(jpeg, offset + 8, 2, littleEndian);
                    switch (orientation)
                    {
                        case 1:
                            return ExifInterface.ORIENTATION_NORMAL;
                        case 3:
                            return ExifInterface.ORIENTATION_ROTATE_180;
                        case 6:
                            return ExifInterface.ORIENTATION_ROTATE_90;
                        case 8:
                            return ExifInterface.ORIENTATION_ROTATE_270;
                        case 4:
                            return ExifInterface.ORIENTATION_FLIP_VERTICAL;
                        case 2:
                            return ExifInterface.ORIENTATION_FLIP_HORIZONTAL;
                        case 5:
                            return ExifInterface.ORIENTATION_TRANSPOSE;
                        case 7:
                            return ExifInterface.ORIENTATION_TRANSVERSE;

                    }
                    return ExifInterface.ORIENTATION_UNDEFINED;
                }
                offset += 12;
                length -= 12;
            }
        }
        return 0;
    }



    private static int pack(byte[] bytes, int offset, int length, boolean littleEndian)
    {
        int step = 1;
        if (littleEndian)
        {
            offset += length - 1;
            step = -1;
        }

        int value = 0;
        while (length-- > 0)
        {
            value = (value << 8) | (bytes[offset] & 0xFF);
            offset += step;
        }
        return value;
    }






}