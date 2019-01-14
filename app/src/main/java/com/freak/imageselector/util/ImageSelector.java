package com.freak.imageselector.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import com.freak.imageselector.util.popup.PopupGetPictureView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author freak
 * @date 2019/1/14
 */

public class ImageSelector {
    private static ImageSelector mInstance;
    private File userImgFile;
    /**
     * 缓存拍照图片路径
     */
    public  File takePhotoCacheDir = null;
    private Uri mUri;

    public Uri getUri() {
        return mUri;
    }

    public void setUri(Uri uri) {
        mUri = uri;
    }

    public static ImageSelector getInstance() {
        if (mInstance == null) {
            synchronized (ImageSelector.class) {
                if (mInstance == null) {
                    mInstance = new ImageSelector();
                }
            }
        }
        return mInstance;
    }
    /**
     * 获取图片存储路径
     * @return
     */
    public  File getTakePhotoCacheDir() {
        return takePhotoCacheDir;
    }

    /**
     * 设置图片存储路径
     * @param cacheAddress
     */
    public void setTakePhotoCacheDir(String cacheAddress) {
        takePhotoCacheDir = CachePathUtil.getCachePathFile(cacheAddress);
    }

    /**
     * 初始化尺寸工具
     * @param context
     */
    public void initDisplayOpinion(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        DisplayUtil.density = dm.density;
        DisplayUtil.densityDPI = dm.densityDpi;
        DisplayUtil.screenWidthPx = dm.widthPixels;
        DisplayUtil.screenHeightPx = dm.heightPixels;
        DisplayUtil.screenWidthDip = DisplayUtil.px2dip(context, dm.widthPixels);
        DisplayUtil.screenHeightDip = DisplayUtil.px2dip(context, dm.heightPixels);
    }
    /**
     * 显示图片选择器
     * @param context
     * @param view
     */
    public void showSelector(final Context context, View view){
        PopupGetPictureView popupGetPictureView = new PopupGetPictureView(context, new
                PopupGetPictureView.GetPicture() {
                    @Override
                    public void takePhoto(View v) {
                        if (PermissionUtils.checkTakePhotoPermission(context)) {
                            userImgFile = GetPictureUtils.takePicture(context, Constant.GETPICTURE_TAKEPHOTO);
                        }
                    }

                    @Override
                    public void selectPhoto(View v) {
                        if (PermissionUtils.checkAlbumStroagePermission(context)) {
                            GetPictureUtils.selectPhoto(context, Constant.GETPICTURE_SELECTPHOTO);
                        }
                    }
                });
        popupGetPictureView.showPop(view);
    }

    /**
     * 选择图片回调
     * @param context
     * @param requestCode
     * @param resultCode
     * @param data
     */
    public void onSelectorResult(Context context, int requestCode, int resultCode, Intent data){
        if (resultCode == 0) {
            return ;
        }
        switch (requestCode) {
            //拍照
            case Constant.GETPICTURE_TAKEPHOTO:
                userImgFile = GetPictureUtils.cutPicture(context, userImgFile);
                break;
            //选择照片
            case Constant.GETPICTURE_SELECTPHOTO:
                userImgFile = GetPictureUtils.getPhotoFromIntent(data, context);
                userImgFile = GetPictureUtils.cutPicture(context, userImgFile);
                break;
            //裁剪照片
            case Constant.CUT_PHOTO:
                if (resultCode == Activity.RESULT_OK) {
                 compressImage(userImgFile);
                }
                break;
            default:
                break;
        }
    }

    /**
     * 压缩图片
     * @param file
     */
    public void compressImage(File file) {
        List<File> list = new ArrayList<>();
        list.add(file);
        BitmapUtil.compressFiles(list, new ICompressImageResponse() {
            @Override
            public void onSuccess(List<File> images) {
                File imgFile = images.get(0);
                setUri(Uri.fromFile(imgFile));
                Log.e("TAG","compressImage"+mUri);
            }

            @Override
            public void onMarch() {

            }

            @Override
            public void onFail() {

            }

            @Override
            public void onFinish() {

            }
        });
    }
}
