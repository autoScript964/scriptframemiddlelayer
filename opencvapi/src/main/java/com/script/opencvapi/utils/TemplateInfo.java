package com.script.opencvapi.utils;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by     on 2018/3/30.
 */
public class TemplateInfo implements Parcelable ,Cloneable{

    public static final int VERSION_MDL_STARTED = 1;

    public int version = 0;

    /**
     * 模板图片的x坐标
     * */
    public int x ;
    /**
     * 模板图片的y坐标
     * */
    public int y ;
    /***
     * 模板图片的宽
     */
    public int width ;
    /**
     * 模板图片的高
     * */
    public int height ;
    /**
     * 模板数据的类型
     * */
    public int flag ;

    public int thresh ;
    public int maxval ;
    public int type ;

    public float sim ;

    @Deprecated
    public int quality;
    @Deprecated
    public float scale;
    @Deprecated
    public String md5;
    @Deprecated
    public String tmpFile;

    public TemplateInfo(){}
    protected TemplateInfo(Parcel in) {
        version = in.readInt();
        x = in.readInt();
        y = in.readInt();
        width = in.readInt();
        height = in.readInt();
        flag = in.readInt();
        thresh = in.readInt();
        maxval = in.readInt();
        type = in.readInt();
        sim = in.readFloat() ;
        quality = in.readInt();
        scale = in.readFloat();
        md5 = in.readString();
        tmpFile = in.readString();
    }

    public static final Creator<TemplateInfo> CREATOR = new Creator<TemplateInfo>() {
        @Override
        public TemplateInfo createFromParcel(Parcel in) {
            return new TemplateInfo(in);
        }

        @Override
        public TemplateInfo[] newArray(int size) {
            return new TemplateInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(version);
        parcel.writeInt(x);
        parcel.writeInt(y);
        parcel.writeInt(width);
        parcel.writeInt(height);
        parcel.writeInt(flag);
        parcel.writeInt(thresh);
        parcel.writeInt(maxval);
        parcel.writeInt(type);
        parcel.writeFloat(sim);
        parcel.writeInt(quality);
        parcel.writeFloat(scale);
        parcel.writeString(md5);
        parcel.writeString(tmpFile);
    }

    @Override
    public Object clone()  {
        TemplateInfo info  = null ;
        try {
            info = (TemplateInfo) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return  info ;
    }

}
