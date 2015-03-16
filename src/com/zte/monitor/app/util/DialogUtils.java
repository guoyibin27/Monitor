package com.zte.monitor.app.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;

/**
 * Created by Sylar on 8/27/14.
 */
public class DialogUtils {

    private static ProgressDialog mProgressDialog;

    public static void showProgressDialog(Activity activity, String message) {
        mProgressDialog = new ProgressDialog(activity);
        mProgressDialog.setMessage(message);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setCanceledOnTouchOutside(false);
        if (!activity.isFinishing()) {
            mProgressDialog.show();
        }
    }

    public static void dismissProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    public static void showAlertDialog(Activity activity, String title, String message, DialogInterface.OnClickListener ok, DialogInterface.OnClickListener cancel) {
        if (!activity.isFinishing()) {
            new AlertDialog.Builder(activity).setTitle(title).setMessage(message).setPositiveButton(android.R.string.ok, ok)
                    .setNegativeButton(android.R.string.cancel, cancel).create().show();
        }
    }

    public static void showAlertDialog(Activity activity, String title, String message, DialogInterface.OnClickListener ok) {
        if (!activity.isFinishing()) {
            new AlertDialog.Builder(activity).setTitle(title).setMessage(message).setPositiveButton(android.R.string.ok, ok)
                    .create().show();
        }
    }

    public static void showAlertDialog(Activity activity, String message, DialogInterface.OnClickListener ok, DialogInterface.OnClickListener cancel) {
        if (!activity.isFinishing()) {
            new AlertDialog.Builder(activity).setMessage(message).setPositiveButton(android.R.string.ok, ok)
                    .setNegativeButton(android.R.string.cancel, cancel).create().show();
        }
    }

    public static void showAlertDialog(Activity activity, String message, DialogInterface.OnClickListener ok) {
        if (!activity.isFinishing()) {
            new AlertDialog.Builder(activity).setMessage(message).setPositiveButton(android.R.string.ok, ok)
                    .create().show();
        }
    }
}
