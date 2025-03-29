package ru.ifsoft.network.model;


import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONObject;

import ru.ifsoft.network.constants.Constants;


public class GalleryItem implements Constants, Parcelable {

    private long id, fromUserId;
    private int itemType, createAt, likesCount, commentsCount;
    private String timeAgo, date, comment, imgUrl, previewImgUrl, originImgUrl, previewVideoImgUrl, videoUrl, area, country, city;
    private Double lat = 0.000000, lng = 0.000000;
    private Boolean myLike = false;
    private Profile owner = new Profile();

    public GalleryItem() {

    }

    public GalleryItem(JSONObject jsonData) {

        try {

            if (!jsonData.getBoolean("error")) {

                this.setId(jsonData.getLong("id"));
                this.setItemType(jsonData.getInt("itemType"));
                this.setFromUserId(jsonData.getLong("fromUserId"));
                this.setComment(jsonData.getString("comment"));
                this.setImgUrl(jsonData.getString("imgUrl"));
                this.setPreviewImgUrl(jsonData.getString("previewImgUrl"));
                this.setOriginImgUrl(jsonData.getString("originImgUrl"));
                this.setPreviewVideoImgUrl(jsonData.getString("previewVideoImgUrl"));
                this.setVideoUrl(jsonData.getString("videoUrl"));
                this.setArea(jsonData.getString("area"));
                this.setCountry(jsonData.getString("country"));
                this.setCity(jsonData.getString("city"));
                this.setCommentsCount(jsonData.getInt("commentsCount"));
                this.setLikesCount(jsonData.getInt("likesCount"));
                this.setLat(jsonData.getDouble("lat"));
                this.setLng(jsonData.getDouble("lng"));
                this.setCreateAt(jsonData.getInt("createAt"));
                this.setDate(jsonData.getString("date"));
                this.setTimeAgo(jsonData.getString("timeAgo"));

                this.setMyLike(jsonData.getBoolean("myLike"));

                if (jsonData.has("owner")) {

                    JSONObject ownerObj = (JSONObject) jsonData.getJSONObject("owner");

                    this.setOwner(new Profile(ownerObj));
                }
            }

        } catch (Throwable t) {

            Log.e("GalleryItem", "Could not parse malformed JSON: \"" + jsonData.toString() + "\"");

        } finally {

            Log.d("GalleryItem", jsonData.toString());
        }
    }

    protected GalleryItem(Parcel in) {
        id = in.readLong();
        fromUserId = in.readLong();
        itemType = in.readInt();
        createAt = in.readInt();
        likesCount = in.readInt();
        commentsCount = in.readInt();
        timeAgo = in.readString();
        date = in.readString();
        comment = in.readString();
        imgUrl = in.readString();
        previewImgUrl = in.readString();
        originImgUrl = in.readString();
        previewVideoImgUrl = in.readString();
        videoUrl = in.readString();
        area = in.readString();
        country = in.readString();
        city = in.readString();
        if (in.readByte() == 0) {
            lat = null;
        } else {
            lat = in.readDouble();
        }
        if (in.readByte() == 0) {
            lng = null;
        } else {
            lng = in.readDouble();
        }
        byte tmpMyLike = in.readByte();
        myLike = tmpMyLike == 0 ? null : tmpMyLike == 1;
        owner = in.readParcelable(Profile.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeLong(fromUserId);
        dest.writeInt(itemType);
        dest.writeInt(createAt);
        dest.writeInt(likesCount);
        dest.writeInt(commentsCount);
        dest.writeString(timeAgo);
        dest.writeString(date);
        dest.writeString(comment);
        dest.writeString(imgUrl);
        dest.writeString(previewImgUrl);
        dest.writeString(originImgUrl);
        dest.writeString(previewVideoImgUrl);
        dest.writeString(videoUrl);
        dest.writeString(area);
        dest.writeString(country);
        dest.writeString(city);
        if (lat == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeDouble(lat);
        }
        if (lng == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeDouble(lng);
        }
        dest.writeByte((byte) (myLike == null ? 0 : myLike ? 1 : 2));
        dest.writeParcelable(owner, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<GalleryItem> CREATOR = new Creator<GalleryItem>() {
        @Override
        public GalleryItem createFromParcel(Parcel in) {
            return new GalleryItem(in);
        }

        @Override
        public GalleryItem[] newArray(int size) {
            return new GalleryItem[size];
        }
    };

    public Profile getOwner() {

        return this.owner;
    }

    public void setOwner(Profile owner) {

        this.owner = owner;
    }


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getItemType() {
        return itemType;
    }

    public void setItemType(int itemType) {
        this.itemType = itemType;
    }

    public long getFromUserId() {

        return fromUserId;
    }

    public void setFromUserId(long fromUserId) {

        this.fromUserId = fromUserId;
    }

    public int getCommentsCount() {

        return commentsCount;
    }

    public void setCommentsCount(int commentsCount) {
        this.commentsCount = commentsCount;
    }

    public int getLikesCount() {

        return likesCount;
    }

    public void setLikesCount(int likesCount) {
        this.likesCount = likesCount;
    }

    public int getCreateAt() {

        return createAt;
    }

    public void setCreateAt(int createAt) {
        this.createAt = createAt;
    }

    public String getTimeAgo() {
        return timeAgo;
    }

    public void setTimeAgo(String timeAgo) {
        this.timeAgo = timeAgo;
    }

    public String getComment() {

        if (this.comment == null) {

            this.comment = "";
        }

        return this.comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getImgUrl() {

        if (this.imgUrl == null) {

            this.imgUrl = "";
        }

        return this.imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getPreviewImgUrl() {

        if (this.previewImgUrl == null) {

            this.previewImgUrl = "";
        }

        return previewImgUrl;
    }

    public void setPreviewImgUrl(String previewImgUrl) {
        this.previewImgUrl = previewImgUrl;
    }

    public String getOriginImgUrl() {

        if (this.originImgUrl == null) {

            this.originImgUrl = "";
        }

        return originImgUrl;
    }

    public void setOriginImgUrl(String originImgUrl) {
        this.originImgUrl = originImgUrl;
    }

    public String getPreviewVideoImgUrl() {

        if (this.previewVideoImgUrl == null) {

            this.previewVideoImgUrl = "";
        }

        return previewVideoImgUrl;
    }

    public void setPreviewVideoImgUrl(String previewVideoImgUrl) {
        this.previewVideoImgUrl = previewVideoImgUrl;
    }

    public String getVideoUrl() {

        if (this.videoUrl == null) {

            this.videoUrl = "";
        }

        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getArea() {

        if (this.area == null) {

            this.area = "";
        }

        return this.area;
    }

    public void setArea(String area) {

        this.area = area;
    }

    public String getCountry() {

        if (this.country == null) {

            this.country = "";
        }

        return this.country;
    }

    public void setCountry(String country) {

        this.country = country;
    }

    public String getCity() {

        if (this.city == null) {

            this.city = "";
        }

        return this.city;
    }

    public void setCity(String city) {

        this.city = city;
    }

    public Double getLat() {

        return this.lat;
    }

    public void setLat(Double lat) {

        this.lat = lat;
    }

    public Double getLng() {

        return this.lng;
    }

    public void setLng(Double lng) {

        this.lng = lng;
    }

    public String getLink() {

        return WEB_SITE + this.owner.getUsername() + "/post/" + Long.toString(this.getId());
    }

    public Boolean isMyLike() {
        return myLike;
    }

    public void setMyLike(Boolean myLike) {

        this.myLike = myLike;
    }

}
