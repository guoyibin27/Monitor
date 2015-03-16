package com.zte.monitor.app.util;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.net.Uri;
import android.widget.Toast;

import java.io.*;


/**
 * 两种方式安装服务组件
 */
public class ApkInstaller {

    /**
     * 把assets中服务组件apk传到SDcard中，再在SDcord中安装服务组件apk
     *
     * @param context
     * @param assetsApk
     */
    public static boolean installFromAssets(Context context, String assetsApk) {
        try {
            AssetManager assets = context.getAssets();
            InputStream stream;
            stream = assets.open(assetsApk);
            if (stream == null) {
                Toast.makeText(context, "assets no apk", Toast.LENGTH_SHORT).show();
                return false;
            }
            File dir = context.getExternalFilesDir("/");
            String apkPath;
            if (dir != null) {
                apkPath = dir.getAbsolutePath() + "/SpeechService.apk";
            } else {
                apkPath = "/udisk/SpeechService.apk";
            }
            File file = new File(apkPath);
            //往SDcard中写文件，需加权限
            //<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
            if (!writeStreamToFile(stream, file)) {
                return false;
            }
            //安装apk文件，需加权限
            //<uses-permission android:name="android.permission.INSTALL_PACKAGES" />
            installApk(context, apkPath);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 打开语音服务组件下载页面。
     *
     * @param context
     * @param url
     */
    public static void openDownloadWeb(Context context, String url) {
        Uri uri = Uri.parse(url);
        Intent it = new Intent(Intent.ACTION_VIEW, uri);
        context.startActivity(it);
    }

    /**
     * 从输入流中写数据到一个文件中。
     *
     * @param stream
     * @param file
     */
    private static boolean writeStreamToFile(InputStream stream, File file) {
        OutputStream output = null;
        try {
            output = new FileOutputStream(file);
            final byte[] buffer = new byte[1024];
            int read;
            while ((read = stream.read(buffer)) != -1) {
                output.write(buffer, 0, read);
            }
            output.flush();
        } catch (Exception e1) {
            e1.printStackTrace();
            return false;
        } finally {
            try {
                if (output != null) {
                    output.close();
                }
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    /**
     * 根据apk路径安装apk包。
     *
     * @param context
     * @param apkPath
     */
    private static void installApk(Context context, String apkPath) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(Uri.fromFile(new File(apkPath)),
                "application/vnd.android.package-archive");
        context.startActivity(intent);
    }
}
