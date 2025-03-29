package ru.ifsoft.network.app;

import android.app.Application;
import android.os.Parcel;
import android.os.Parcelable;

import ru.ifsoft.network.constants.Constants;

public class Tooltips extends Application implements Constants, Parcelable {

    private Boolean show_otp_tooltip = true;
    private Boolean show_notifications_permission_request = true;
    private Boolean show_capture_video_time_tooltip = true;

    private Boolean show_login_create_tooltip = true;

    public Tooltips() {

    }

    public void setShowLoginCreateTooltip(Boolean show_login_create_tooltip) {

        this.show_login_create_tooltip = show_login_create_tooltip;
    }

    public Boolean isAllowShowLoginCreateTooltip() {

        return this.show_login_create_tooltip;
    }

    public void setShowOtpTooltip(Boolean show_otp_tooltip) {

        this.show_otp_tooltip = show_otp_tooltip;
    }

    public Boolean isAllowShowOtpTooltip() {

        return this.show_otp_tooltip;
    }

    public void setShowNotificationsPermissionRequst(Boolean show_notifications_permission_request) {

        this.show_notifications_permission_request = show_notifications_permission_request;
    }

    public Boolean isAllowShowNotificationsPermissionRequest() {

        return this.show_notifications_permission_request;
    }

    public void setShowCaptureVideoTimeTooltip(Boolean show_follow_tooltip) {

        this.show_capture_video_time_tooltip = show_follow_tooltip;
    }

    public Boolean isAllowShowCaptureVideoTimeTooltip() {

        return this.show_capture_video_time_tooltip;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeValue(this.show_otp_tooltip);
    }

    protected Tooltips(Parcel in) {

        this.show_otp_tooltip = (Boolean) in.readValue(Boolean.class.getClassLoader());
    }

    public static final Creator<Tooltips> CREATOR = new Creator<Tooltips>() {
        @Override
        public Tooltips createFromParcel(Parcel source) {
            return new Tooltips(source);
        }

        @Override
        public Tooltips[] newArray(int size) {
            return new Tooltips[size];
        }
    };
}
