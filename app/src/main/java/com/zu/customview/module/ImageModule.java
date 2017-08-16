package com.zu.customview.module;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by zu on 17-6-12.
 */

public class ImageModule implements Parcelable{
    public String path = null;
    public long createDate = 0;
    public long modifyData = 0;
    public String parentFolder = null;
    public boolean isCamera = false;

    public ImageModule(String path, long createDate, long modifyData, String parentFolder, boolean isCamera) {
        this.path = path;
        this.createDate = createDate;
        this.modifyData = modifyData;
        this.parentFolder = parentFolder;
        this.isCamera = isCamera;
    }

    public ImageModule(Parcel source)
    {
        path = source.readString();
        createDate = source.readLong();
        modifyData = source.readLong();
        parentFolder = source.readString();
        isCamera = source.readByte() == 0x01 ? true : false;
    }

    public Creator<ImageModule> CREATOR = new Creator<ImageModule>() {
        @Override
        public ImageModule createFromParcel(Parcel source) {
            return new ImageModule(source);
        }

        @Override
        public ImageModule[] newArray(int size) {
            return new ImageModule[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(path);
        dest.writeLong(createDate);
        dest.writeLong(modifyData);
        dest.writeString(parentFolder);
        dest.writeByte(isCamera == true ? (byte)0x01 : (byte)0x00);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ImageModule)) return false;

        ImageModule that = (ImageModule) o;

        return path != null ? path.equals(that.path) : that.path == null;

    }

    @Override
    public int hashCode() {
        return path != null ? path.hashCode() : 0;
    }
}
