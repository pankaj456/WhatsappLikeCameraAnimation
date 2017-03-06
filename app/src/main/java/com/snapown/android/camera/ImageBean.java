package com.snapown.android.camera;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by admin on 04/03/2017.
 */

public class ImageBean implements Parcelable {
    public void setImage(Bitmap image) {
        this.image = image;
    }

    private Bitmap image;


    public Bitmap getImage() {
        return image;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.image, flags);
    }

    public ImageBean() {
    }

    protected ImageBean(Parcel in) {
        this.image = in.readParcelable(Bitmap.class.getClassLoader());
    }

    public static final Parcelable.Creator<ImageBean> CREATOR = new Parcelable.Creator<ImageBean>() {
        @Override
        public ImageBean createFromParcel(Parcel source) {
            return new ImageBean(source);
        }

        @Override
        public ImageBean[] newArray(int size) {
            return new ImageBean[size];
        }
    };
}
