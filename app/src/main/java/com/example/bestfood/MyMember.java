package com.example.bestfood;

import android.os.Parcel;
import android.os.Parcelable;

public class MyMember implements Parcelable {
    private int name;

    public int describeContents(){
        return 0;
    }

    public void writeToParcel(Parcel out, int flags){
        out.writeInt(name);
    }
    public static final Parcelable.Creator<MyMember> CREATOR
            = new Parcelable.Creator<MyMember>(){
        public MyMember createFromParcel(Parcel in){
            return new MyMember(in);
        }

        public MyMember[] newArray(int size){
            return new MyMember[size];
        }
    };
    private MyMember(Parcel in){
        name = in.readInt();
    }
}
