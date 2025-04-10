package ru.ifsoft.network.model;

import android.app.Application;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import ru.ifsoft.network.app.App;
import ru.ifsoft.network.constants.Constants;
import ru.ifsoft.network.util.CustomRequest;


public class Profile implements Constants, Parcelable {

    private long id;

    private int mood, state, sex, year, month, day, verify, pro, itemsCount, galleryItemsCount, likesCount, giftsCount, friendsCount, followingsCount, followersCount, allowComments, allowMessages, lastAuthorize, accountType;

    private int allowVideoCalls = 0, allowShowMyInfo, allowShowMyFriends, allowShowMyGallery, allowShowMyGifts;

    private int allowGalleryComments;

    private double distance = 0;

    private int reaction = 0;

    private String username, fullname, lowPhotoUrl, bigPhotoUrl, normalPhotoUrl, normalCoverUrl, location, facebookPage, instagramPage, bio, lastAuthorizeDate, lastAuthorizeTimeAgo;

    private Boolean blocked = false;

    private Boolean inBlackList = false;

    private Boolean follower = false;

    private Boolean follow = false;

    private Boolean friend = false;

    private Boolean online = false;

    public Profile() {


    }

    public Profile(JSONObject jsonData) {

        try {

            if (!jsonData.getBoolean("error")) {

                this.setId(jsonData.getLong("id"));
                this.setState(jsonData.getInt("state"));
                this.setType(jsonData.getInt("accountType"));
                this.setSex(jsonData.getInt("sex"));
                this.setYear(jsonData.getInt("year"));
                this.setMonth(jsonData.getInt("month"));
                this.setDay(jsonData.getInt("day"));
                this.setUsername(jsonData.getString("username"));
                this.setFullname(jsonData.getString("fullname"));
                this.setLocation(jsonData.getString("location"));
                this.setFacebookPage(jsonData.getString("fb_page"));
                this.setInstagramPage(jsonData.getString("instagram_page"));
                this.setBio(jsonData.getString("status"));
                this.setVerify(jsonData.getInt("verified"));

                this.setMood(jsonData.getInt("mood"));

                this.setLowPhotoUrl(jsonData.getString("lowPhotoUrl"));
                this.setNormalPhotoUrl(jsonData.getString("normalPhotoUrl"));
                this.setBigPhotoUrl(jsonData.getString("bigPhotoUrl"));

                this.setNormalCoverUrl(jsonData.getString("normalCoverUrl"));

                this.setFollowersCount(jsonData.getInt("followersCount"));
                this.setFollowingsCount(jsonData.getInt("friendsCount"));
                this.setFriendsCount(jsonData.getInt("friendsCount"));
                this.setItemsCount(jsonData.getInt("postsCount"));
                this.setLikesCount(jsonData.getInt("likesCount"));
                this.setGalleryItemsCount(jsonData.getInt("galleryItemsCount"));
                this.setGiftsCount(jsonData.getInt("giftsCount"));

                this.setAllowShowMyInfo(jsonData.getInt("allowShowMyInfo"));
                this.setAllowShowMyFriends(jsonData.getInt("allowShowMyFriends"));
                this.setAllowShowMyGallery(jsonData.getInt("allowShowMyGallery"));
                this.setAllowShowMyGifts(jsonData.getInt("allowShowMyGifts"));

                if (jsonData.has("allowVideoCalls")) {

                    this.setAllowVideoCalls(jsonData.getInt("allowVideoCalls"));
                }

                this.setAllowComments(jsonData.getInt("allowComments"));
                this.setAllowMessages(jsonData.getInt("allowMessages"));

                if (jsonData.has("inBlackList")) {

                    this.setInBlackList(jsonData.getBoolean("inBlackList"));
                }

                if (jsonData.has("follower")) {

                    this.setFollower(jsonData.getBoolean("follower"));
                }

                if (jsonData.has("follow")) {

                    this.setFollow(jsonData.getBoolean("follow"));
                }

                if (jsonData.has("friend")) {

                    this.setFriend(jsonData.getBoolean("friend"));
                }

                if (jsonData.has("online")) {

                    this.setOnline(jsonData.getBoolean("online"));
                }

                if (jsonData.has("blocked")) {

                    this.setBlocked(jsonData.getBoolean("blocked"));
                }

                this.setLastActive(jsonData.getInt("lastAuthorize"));
                this.setLastActiveDate(jsonData.getString("lastAuthorizeDate"));
                this.setLastActiveTimeAgo(jsonData.getString("lastAuthorizeTimeAgo"));

                if (jsonData.has("allowGalleryComments")) {

                    this.setAllowGalleryComments(jsonData.getInt("allowGalleryComments"));
                }

                if (jsonData.has("distance")) {

                    this.setDistance(jsonData.getDouble("distance"));
                }

                if (jsonData.has("pro")) {

                    this.setProMode(jsonData.getInt("pro"));
                }

                if (jsonData.has("reaction")) {

                    this.setReaction(jsonData.getInt("reaction"));
                }
            }

        } catch (Throwable t) {

            Log.e("Profile", "Could not parse malformed JSON: \"" + jsonData.toString() + "\"");

        } finally {

            Log.d("Profile", jsonData.toString());
        }
    }

    public void setDistance(double distance) {

        this.distance = distance;
    }

    public double getDistance() {

        return this.distance;
    }

    public void setId(long profile_id) {

        this.id = profile_id;
    }

    public long getId() {

        return this.id;
    }

    public void setState(int profileState) {

        this.state = profileState;
    }

    public int getState() {

        return this.state;
    }

    public void setType(int accountType) {

        this.accountType = accountType;
    }

    public int getType() {

        return this.accountType;
    }

    public void setSex(int sex) {

        this.sex = sex;
    }

    public int getSex() {

        return this.sex;
    }

    public void setYear(int year) {

        this.year = year;
    }

    public int getYear() {

        return this.year;
    }

    public void setMonth(int month) {

        this.month = month;
    }

    public int getMonth() {

        return this.month;
    }

    public void setDay(int day) {

        this.day = day;
    }

    public int getDay() {

        return this.day;
    }

    public void setProMode(int proMode) {

        this.pro = proMode;
    }

    public int getProMode() {

        return this.pro;
    }

    public Boolean isProMode() {

        if (this.pro > 0) {

            return true;
        }

        return false;
    }

    public void setVerify(int profileVerify) {

        this.verify = profileVerify;
    }

    public int getVerify() {

        return this.verify;
    }

    public Boolean isVerify() {

        if (this.verify > 0) {

            return true;
        }

        return false;
    }

    public void setUsername(String profile_username) {

        this.username = profile_username;
    }

    public String getUsername() {

        return this.username;
    }

    public void setFullname(String profile_fullname) {

        this.fullname = profile_fullname;
    }

    public String getFullname() {

        if (fullname == null) {

            fullname = "";
        }

        return this.fullname;
    }

    public void setLocation(String location) {

        this.location = location;
    }

    public String getLocation() {

        if (this.location == null) {

            this.location = "";
        }

        return this.location;
    }

    public void setFacebookPage(String facebookPage) {

        this.facebookPage = facebookPage;
    }

    public String getFacebookPage() {

        return this.facebookPage;
    }

    public void setInstagramPage(String instagramPage) {

        this.instagramPage = instagramPage;
    }

    public String getInstagramPage() {

        return this.instagramPage;
    }

    public void setBio(String bio) {

        this.bio = bio;
    }

    public String getBio() {

        return this.bio;
    }

    public void setLowPhotoUrl(String lowPhotoUrl) {

        if (API_DOMAIN.equals("http://10.0.2.2/")) {

            this.lowPhotoUrl = lowPhotoUrl.replace("http://localhost/","http://10.0.2.2/");

        } else {

            this.lowPhotoUrl = lowPhotoUrl;
        }
    }

    public String getLowPhotoUrl() {

        if (this.lowPhotoUrl == null) {

            this.lowPhotoUrl = "";
        }

        return this.lowPhotoUrl;
    }

    public void setBigPhotoUrl(String bigPhotoUrl) {

        this.bigPhotoUrl = bigPhotoUrl;
    }

    public String getBigPhotoUrl() {

        return this.bigPhotoUrl;
    }

    public void setNormalPhotoUrl(String normalPhotoUrl) {

        this.normalPhotoUrl = normalPhotoUrl;
    }

    public String getNormalPhotoUrl() {

        return this.normalPhotoUrl;
    }

    public void setNormalCoverUrl(String normalCoverUrl) {

        this.normalCoverUrl = normalCoverUrl;
    }

    public String getNormalCoverUrl() {

        return this.normalCoverUrl;
    }

    public void setFollowersCount(int followersCount) {

        this.followersCount = followersCount;
    }

    public int getFollowersCount() {

        return this.followersCount;
    }

    public void setItemsCount(int itemsCount) {

        this.itemsCount = itemsCount;
    }

    public int getItemsCount() {

        return this.itemsCount;
    }

    public void setGalleryItemsCount(int galleryItemsCount) {

        this.galleryItemsCount = galleryItemsCount;
    }

    public int getGalleryItemsCount() {

        return this.galleryItemsCount;
    }

    public void setLikesCount(int likesCount) {

        this.likesCount = likesCount;
    }

    public int getLikesCount() {

        return this.likesCount;
    }

    public void setGiftsCount(int giftsCount) {

        this.giftsCount = giftsCount;
    }

    public int getGiftsCount() {

        return this.giftsCount;
    }

    public void setFollowingsCount(int followingsCount) {

        this.followingsCount = followingsCount;
    }

    public int getFollowingsCount() {

        return this.followingsCount;
    }

    public void setFriendsCount(int friendsCount) {

        this.friendsCount = friendsCount;
    }

    public int getFriendsCount() {

        return this.friendsCount;
    }

    public void setAllowComments(int allowComments) {

        this.allowComments = allowComments;
    }

    public int getAllowComments() {

        return this.allowComments;
    }

    public void setAllowMessages(int allowMessages) {

        this.allowMessages = allowMessages;
    }

    public int getAllowMessages() {

        return this.allowMessages;
    }

    public void setMood(int mood) {

        this.mood = mood;
    }

    public int getMood() {

        return this.mood;
    }

    public void setLastActive(int lastAuthorize) {

        this.lastAuthorize = lastAuthorize;
    }

    public int getLastActive() {

        return this.lastAuthorize;
    }

    public void setLastActiveDate(String lastAuthorizeDate) {

        this.lastAuthorizeDate = lastAuthorizeDate;
    }

    public String getLastActiveDate() {

        return this.lastAuthorizeDate;
    }

    public void setLastActiveTimeAgo(String lastAuthorizeTimeAgo) {

        this.lastAuthorizeTimeAgo = lastAuthorizeTimeAgo;
    }

    public String getLastActiveTimeAgo() {

        return this.lastAuthorizeTimeAgo;
    }

    public void setBlocked(Boolean blocked) {

        this.blocked = blocked;
    }

    public Boolean isBlocked() {

        return this.blocked;
    }

    public void setFollower(Boolean follower) {

        this.follower = follower;
    }

    public Boolean isFollower() {

        return this.follower;
    }

    public void setFollow(Boolean follow) {

        this.follow = follow;
    }

    public Boolean isFollow() {

        return this.follow;
    }

    public void setFriend(Boolean friend) {

        this.friend = friend;
    }

    public Boolean isFriend() {

        return this.friend;
    }

    public void setOnline(Boolean online) {

        this.online = online;
    }

    public Boolean isOnline() {

        return this.online;
    }

    public void setInBlackList(Boolean inBlackList) {

        this.inBlackList = inBlackList;
    }

    public Boolean isInBlackList() {

        return this.inBlackList;
    }

    // Privacy

    public void setAllowVideoCalls(int allowVideoCalls) {

        this.allowVideoCalls = allowVideoCalls;
    }

    public int getAllowVideoCalls() {

        return this.allowVideoCalls;
    }

    public void setAllowShowMyInfo(int allowShowMyInfo) {

        this.allowShowMyInfo = allowShowMyInfo;
    }

    public int getAllowShowMyInfo() {

        return this.allowShowMyInfo;
    }

    public void setAllowShowMyFriends(int allowShowMyFriends) {

        this.allowShowMyFriends = allowShowMyFriends;
    }

    public int getAllowShowMyFriends() {

        return this.allowShowMyFriends;
    }

    public void setAllowShowMyGallery(int allowShowMyGallery) {

        this.allowShowMyGallery = allowShowMyGallery;
    }

    public int getAllowShowMyGallery() {

        return this.allowShowMyGallery;
    }

    public void setAllowShowMyGifts(int allowShowMyGifts) {

        this.allowShowMyGifts = allowShowMyGifts;
    }

    public int getAllowShowMyGifts() {

        return this.allowShowMyGifts;
    }

    public int getAllowGalleryComments() {

        return this.allowGalleryComments;
    }

    public void setAllowGalleryComments(int allowGalleryComments) {

        this.allowGalleryComments = allowGalleryComments;
    }

    public void setReaction(int reaction) {

        this.reaction = reaction;
    }

    public int getReaction() {

        return this.reaction;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeInt(this.mood);
        dest.writeInt(this.state);
        dest.writeInt(this.sex);
        dest.writeInt(this.year);
        dest.writeInt(this.month);
        dest.writeInt(this.day);
        dest.writeInt(this.verify);
        dest.writeInt(this.pro);
        dest.writeInt(this.itemsCount);
        dest.writeInt(this.galleryItemsCount);
        dest.writeInt(this.likesCount);
        dest.writeInt(this.giftsCount);
        dest.writeInt(this.friendsCount);
        dest.writeInt(this.followingsCount);
        dest.writeInt(this.followersCount);
        dest.writeInt(this.allowComments);
        dest.writeInt(this.allowMessages);
        dest.writeInt(this.lastAuthorize);
        dest.writeInt(this.accountType);
        dest.writeInt(this.allowShowMyInfo);
        dest.writeInt(this.allowShowMyFriends);
        dest.writeInt(this.allowShowMyGallery);
        dest.writeInt(this.allowShowMyGifts);
        dest.writeDouble(this.distance);
        dest.writeString(this.username);
        dest.writeString(this.fullname);
        dest.writeString(this.lowPhotoUrl);
        dest.writeString(this.bigPhotoUrl);
        dest.writeString(this.normalPhotoUrl);
        dest.writeString(this.normalCoverUrl);
        dest.writeString(this.location);
        dest.writeString(this.facebookPage);
        dest.writeString(this.instagramPage);
        dest.writeString(this.bio);
        dest.writeString(this.lastAuthorizeDate);
        dest.writeString(this.lastAuthorizeTimeAgo);
        dest.writeValue(this.blocked);
        dest.writeValue(this.inBlackList);
        dest.writeValue(this.follower);
        dest.writeValue(this.follow);
        dest.writeValue(this.friend);
        dest.writeValue(this.online);
        dest.writeInt(this.allowGalleryComments);
        dest.writeInt(this.reaction);
        dest.writeInt(this.allowVideoCalls);
    }

    protected Profile(Parcel in) {
        this.id = in.readLong();
        this.mood = in.readInt();
        this.state = in.readInt();
        this.sex = in.readInt();
        this.year = in.readInt();
        this.month = in.readInt();
        this.day = in.readInt();
        this.verify = in.readInt();
        this.pro = in.readInt();
        this.itemsCount = in.readInt();
        this.galleryItemsCount = in.readInt();
        this.likesCount = in.readInt();
        this.giftsCount = in.readInt();
        this.friendsCount = in.readInt();
        this.followingsCount = in.readInt();
        this.followersCount = in.readInt();
        this.allowComments = in.readInt();
        this.allowMessages = in.readInt();
        this.lastAuthorize = in.readInt();
        this.accountType = in.readInt();
        this.allowShowMyInfo = in.readInt();
        this.allowShowMyFriends = in.readInt();
        this.allowShowMyGallery = in.readInt();
        this.allowShowMyGifts = in.readInt();
        this.distance = in.readDouble();
        this.username = in.readString();
        this.fullname = in.readString();
        this.lowPhotoUrl = in.readString();
        this.bigPhotoUrl = in.readString();
        this.normalPhotoUrl = in.readString();
        this.normalCoverUrl = in.readString();
        this.location = in.readString();
        this.facebookPage = in.readString();
        this.instagramPage = in.readString();
        this.bio = in.readString();
        this.lastAuthorizeDate = in.readString();
        this.lastAuthorizeTimeAgo = in.readString();
        this.blocked = (Boolean) in.readValue(Boolean.class.getClassLoader());
        this.inBlackList = (Boolean) in.readValue(Boolean.class.getClassLoader());
        this.follower = (Boolean) in.readValue(Boolean.class.getClassLoader());
        this.follow = (Boolean) in.readValue(Boolean.class.getClassLoader());
        this.friend = (Boolean) in.readValue(Boolean.class.getClassLoader());
        this.online = (Boolean) in.readValue(Boolean.class.getClassLoader());
        this.allowGalleryComments = in.readInt();
        this.reaction = in.readInt();
        this.allowVideoCalls = in.readInt();
    }

    public static final Creator<Profile> CREATOR = new Creator<Profile>() {
        @Override
        public Profile createFromParcel(Parcel source) {
            return new Profile(source);
        }

        @Override
        public Profile[] newArray(int size) {
            return new Profile[size];
        }
    };
}
