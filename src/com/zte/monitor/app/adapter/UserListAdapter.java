package com.zte.monitor.app.adapter;

import android.app.Activity;
import android.graphics.Color;
import android.view.View;
import android.widget.*;
import com.zte.monitor.app.R;
import com.zte.monitor.app.SystemConstants;
import com.zte.monitor.app.model.UserModel;

/**
 * Created by Sylar on 8/25/14.
 */
public class UserListAdapter extends AbstractListViewAdapter<UserModel> {
    public UserListAdapter(Activity activity, int layoutResId) {
        super(activity, layoutResId);
    }

    @Override
    protected IViewHolder<UserModel> getViewHolder() {
        return new ViewHolder();
    }

    private class ViewHolder implements IViewHolder<UserModel> {
        private TextView mUserStatus;
        private TextView mUserName;
        private TextView mImsi;
        private TextView mImei;
        private TextView mPhoneNum;
        private RelativeLayout relativeLayout;
        private ProgressBar mPowerProgress;
        private TextView mPowerText;
        private ImageView mUserProIcon;
        private ImageView hasSensitiveWordImage;
        private ImageView hasSensitiveNumImage;
        private RelativeLayout powerLayout;
        private LinearLayout phoneLayout;
        private LinearLayout statusLayout;


        @Override
        public void init(View view, int position) {
            mUserStatus = (TextView) view.findViewById(R.id.tv_status);
            mUserName = (TextView) view.findViewById(R.id.user_name);
            mImsi = (TextView) view.findViewById(R.id.tv_imsi);
            mImei = (TextView) view.findViewById(R.id.tv_imei);
            mPhoneNum = (TextView) view.findViewById(R.id.tv_phone_number);
            relativeLayout = (RelativeLayout) view.findViewById(R.id.layout);
            mPowerProgress = (ProgressBar) view.findViewById(R.id.power_progress);
            mPowerText = (TextView) view.findViewById(R.id.power_text);
            mUserProIcon = (ImageView) view.findViewById(R.id.user_pro_icon);
            hasSensitiveNumImage = (ImageView) view.findViewById(R.id.has_sensitive_phone);
            hasSensitiveWordImage = (ImageView) view.findViewById(R.id.has_sensitive_word);
            powerLayout = (RelativeLayout) view.findViewById(R.id.power_layout);
            phoneLayout = (LinearLayout) view.findViewById(R.id.phone_layout);
            statusLayout = (LinearLayout) view.findViewById(R.id.status_layout);
        }

        @Override
        public void update(UserModel userModel, int position) {
            mImsi.setText(userModel.imsi);
            if (userModel.property.equals(SystemConstants.USER_PROPERTY.UNKNOWN_LIST)) {
                mUserName.setText("未知");
                mUserProIcon.setBackgroundResource(R.drawable.icon_unknown_user);
                mPhoneNum.setVisibility(View.GONE);
                mPowerProgress.setVisibility(View.GONE);
                mPowerText.setVisibility(View.GONE);
                hasSensitiveNumImage.setVisibility(View.GONE);
                hasSensitiveWordImage.setVisibility(View.GONE);
                powerLayout.setVisibility(View.GONE);
                phoneLayout.setVisibility(View.GONE);
                statusLayout.setVisibility(View.GONE);
                mImei.setText(userModel.imei);
            } else {
                if (userModel.property.equals(SystemConstants.USER_PROPERTY.WHITE_LIST)) {
                    mUserProIcon.setBackgroundResource(R.drawable.icon_white_user);
                } else {
                    mUserProIcon.setBackgroundResource(R.drawable.icon_black_user);
                }
                mUserName.setText(userModel.username);
                mPhoneNum.setText(userModel.phoneNumber);
                mImei.setText(userModel.imei);
                mPowerProgress.setProgress(userModel.power);
                mPowerText.setText(userModel.power + "");
                mUserStatus.setText(SystemConstants.USER_STATUS.get(userModel.status));
                if (userModel.status != 0 && userModel.isStatusChanged == 1) {
                    mUserStatus.setTextColor(Color.RED);
                    mPhoneNum.setTextColor(Color.RED);
                    mUserName.setTextColor(Color.RED);
                    mImei.setTextColor(Color.RED);
                    mImsi.setTextColor(Color.RED);
                } else {
                    mUserStatus.setTextColor(Color.WHITE);
                    mPhoneNum.setTextColor(Color.WHITE);
                    mUserName.setTextColor(Color.WHITE);
                    mImei.setTextColor(Color.WHITE);
                    mImsi.setTextColor(Color.WHITE);
                }

                if (userModel.status == 6) {//脱网
                    mUserStatus.setTextColor(Color.GRAY);
                    mPhoneNum.setTextColor(Color.GRAY);
                    mUserName.setTextColor(Color.GRAY);
                    mImei.setTextColor(Color.GRAY);
                    mImsi.setTextColor(Color.GRAY);
                } else {
                    mUserStatus.setTextColor(Color.WHITE);
                    mPhoneNum.setTextColor(Color.WHITE);
                    mUserName.setTextColor(Color.WHITE);
                    mImei.setTextColor(Color.WHITE);
                    mImsi.setTextColor(Color.WHITE);
                }

                if (userModel.hasSensitiveNumber == 1) {
                    hasSensitiveNumImage.setVisibility(View.VISIBLE);
                } else {
                    hasSensitiveNumImage.setVisibility(View.GONE);
                }
                if (userModel.hasSensitiveWord == 1) {
                    hasSensitiveWordImage.setVisibility(View.VISIBLE);
                } else {
                    hasSensitiveWordImage.setVisibility(View.GONE);
                }
            }
        }
    }
}
