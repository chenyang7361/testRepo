package com.mivideo.mifm.data.models.jsondata.common;

import android.os.Parcel;
import android.os.Parcelable;

import timber.log.Timber;

/**
 * Created by aaron on 2016/11/21.
 */

public class VideoInfoParams implements Parcelable {

    private int positionY = 0;
    private boolean isList = true;
    private String senderFrom = null;
    private String from = null;
    private int senderFromPosition = 0;
    private boolean isVideoIdOpen = false;
    //视频是否已经缓存
    private boolean videoCached = false;
    private CommonVideo commonVideo = new CommonVideo();

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.positionY);
        dest.writeByte(this.isList ? (byte) 1 : (byte) 0);
        dest.writeByte(this.isVideoIdOpen ? (byte) 1 : (byte) 0);
        dest.writeByte(this.videoCached ? (byte) 1 : (byte) 0);
        dest.writeString(this.senderFrom);
        dest.writeString(this.from);
        dest.writeInt(this.senderFromPosition);
        dest.writeParcelable(this.commonVideo, flags);
    }

    public VideoInfoParams() {
    }

    protected VideoInfoParams(Parcel in) {
        this.positionY = in.readInt();
        this.isList = in.readByte() != 0;
        this.isVideoIdOpen = in.readByte() != 0;
        this.videoCached = in.readByte() != 0;
        this.senderFrom = in.readString();
        this.from = in.readString();
        this.senderFromPosition = in.readInt();
        this.commonVideo = in.readParcelable(CommonVideo.class.getClassLoader());
    }

    public static final Creator<VideoInfoParams> CREATOR = new Creator<VideoInfoParams>() {
        @Override
        public VideoInfoParams createFromParcel(Parcel source) {
            return new VideoInfoParams(source);
        }

        @Override
        public VideoInfoParams[] newArray(int size) {
            return new VideoInfoParams[size];
        }
    };

    public int getPositionY() {
        return positionY;
    }

    public void setPositionY(int positionY) {
        this.positionY = positionY;
    }

    public boolean isList() {
        return isList;
    }

    public void setList(boolean list) {
        isList = list;
    }

    public String getSenderFrom() {
        return senderFrom;
    }

    public void setSenderFrom(String senderFrom) {
        this.senderFrom = senderFrom;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }


    public int getSenderFromPosition() {
        return senderFromPosition;
    }

    public void setSenderFromPosition(int senderFromPosition) {
        this.senderFromPosition = senderFromPosition;
    }

    public CommonVideo getCommonVideo() {
        return commonVideo;
    }

    public void setCommonVideo(CommonVideo commonVideo) {
        Timber.i("setCommonVideo");
        this.commonVideo = commonVideo;
    }

    public boolean isVideoIdOpen() {
        return isVideoIdOpen;
    }

    public void setVideoIdOpen(boolean videoIdOpen) {
        isVideoIdOpen = videoIdOpen;
    }

    public boolean isVideoCached() {
        return videoCached;
    }

    public void setVideoCached(boolean videoCached) {
        this.videoCached = videoCached;
    }

    @Override
    public String toString() {
        return "VideoInfoParams{" +
                "hashCode="+hashCode()+
                "commonVideo=" + commonVideo +
                '}';
    }
}
