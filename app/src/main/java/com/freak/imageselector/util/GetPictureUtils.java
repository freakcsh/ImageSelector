package com.freak.imageselector.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.widget.Toast;

import com.freak.imageselector.BuildConfig;

import java.io.File;


/**
 *
 * @author freak
 * @date 2019/1/14
 */

public class GetPictureUtils {

    public interface NeedCutPicture {
        /**
         * 是否需要剪切
         * @param isNeed 是否需要剪切
         * @param file 需要剪切的图片文件
         */
        void isNeedCut(boolean isNeed, File file);
    }

    /**
     * 发起拍照
     */
    public static File takePicture(Context mContext, int requestCode) {
        String imgUrl = "";
        File f = null;
        try {
            File dir = ImageSelector.getInstance().getTakePhotoCacheDir();
            String key = String.valueOf(System.currentTimeMillis());
            if (!dir.exists()) {
                dir.mkdir();
            }
            f = new File(dir, key + ".jpg");
            imgUrl = f.getAbsolutePath();
            Uri u = toURI(mContext, f);
            Intent intent = new Intent(
                    MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.Images.Media.ORIENTATION, 0);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, u);
            intent.putExtra("imgurl", imgUrl);
            ((Activity) mContext).startActivityForResult(intent, requestCode);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return f;
    }

    /**
     * 相册选择图片
     *
     * @return
     */
    public static void selectPhoto(Context mContext, int requestCode) {
        try {
            Intent getAlbum = new Intent(Intent.ACTION_PICK);
            getAlbum.setType("image/*");
            ((Activity) mContext).startActivityForResult(getAlbum,
                    requestCode);
        } catch (Exception e) {

        }
    }

    /***
     * 从intent获取图片
     * @param data
     * @param mContext
     * @return
     */
    public static File getPhotoFromIntent(Intent data, Context mContext) {
        if (data == null) {
            return null;
        }
        try {
            File file = null;
            String path = "";
            if (data.getDataString().contains("content")) {
                Uri originalUri = data.getData();
                Cursor cursor = mContext.getContentResolver().query(originalUri,
                        null, null, null, null);
                // Source is Dropbox or other similar
                if (cursor == null) {
                    // local file path
                    path = originalUri.getPath();
                } else {
                    cursor.moveToFirst();
                    int idx = cursor
                            .getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                    path = cursor.getString(idx);
                    cursor.close();
                }
            } else {
                path = data.getDataString().replace("file://", "");
            }
            if (!TextUtils.isEmpty(path)) {
                file = new File(path);
                return file;
            } else {
                return null;
            }
        } catch (Exception e) {
            Toast.makeText(mContext,"获取图片失败",Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    /**
     * 是否需要裁剪
     *
     * @param mContext
     * @param selectPhoto
     * @param needCutPicture
     */
    public static void NeedCutPicture(final Context mContext, final File selectPhoto, final NeedCutPicture needCutPicture) {
        if (needCutPicture != null) {
            AlertDialog dialog = new AlertDialog.Builder(mContext)
                    .setTitle("是否需要裁剪?")
                    .setPositiveButton("需要", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            needCutPicture.isNeedCut(true, cutPicture(mContext, selectPhoto));
                        }
                    })
                    .setNegativeButton("否", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            needCutPicture.isNeedCut(false, selectPhoto);
                        }
                    }).create();
            dialog.show();
        }

    }

    /**
     * 发起裁剪
     */
    public static File cutPicture(Context mContext, File selectPhoto) {
        try {
            if (selectPhoto == null) {
                return null;
            }

            File dir = ImageSelector.getInstance().getTakePhotoCacheDir();
            String key = String.valueOf(System.currentTimeMillis());
            if (!dir.exists()) {
                dir.mkdir();
            }
            File outfile = new File(dir, key + ".jpg");
            Uri uri = toURI(mContext, selectPhoto);
            Uri outUri = Uri.fromFile(outfile);
            Intent cropIntent = new Intent("com.android.camera.action.CROP");
            //添加这一句表示对目标应用临时授权该Uri所代表的文件
            cropIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            // 图片来源
            cropIntent.setDataAndType(uri, "image/*");
            // 设置剪裁剪属性
            cropIntent.putExtra("crop", "true");
            //    cropIntent.putExtra("aspectX", 1);
            //   cropIntent.putExtra("aspectY", 3);
            // 输出的坐标
            cropIntent.putExtra("outputX", 500);
            cropIntent.putExtra("outputY", 500);
            // 返回剪裁的图片数据
            cropIntent.putExtra(MediaStore.EXTRA_OUTPUT, outUri);
            cropIntent.putExtra("return-data", false);
            ((Activity) mContext).startActivityForResult(cropIntent,
                    Constant.CUT_PHOTO);
            return outfile;
        } catch (Exception e) {
            return selectPhoto;
        }
    }

    public static Uri toURI(Context mContext, File file) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            return Uri.fromFile(file);
        } else {
            return FileProvider.getUriForFile(
                    mContext,
                    "com.freak.imageselector.fileProvider",
                    file);
        }
    }


    /**
     * 转换 content:// uri
     *
     * @param imageFile
     * @return
     */
    public static Uri getImageContentUri(Context context, File imageFile) {
        String filePath = imageFile.getAbsolutePath();
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Images.Media._ID},
                MediaStore.Images.Media.DATA + "=? ",
                new String[]{filePath}, null);

        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor
                    .getColumnIndex(MediaStore.MediaColumns._ID));
            Uri baseUri = Uri.parse("content://media/external/images/media");
            return Uri.withAppendedPath(baseUri, "" + id);
        } else {
            if (imageFile.exists()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, filePath);
                return context.getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                return null;
            }
        }
    }

}
