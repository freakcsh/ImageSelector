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
 * @author freak
 * @date 2019/1/14
 */

public class GetPictureUtils {

    public interface NeedCutPicture {
        /**
         * 是否需要剪切
         *
         * @param isNeed 是否需要剪切
         * @param file   需要剪切的图片文件
         */
        void isNeedCut(boolean isNeed, File file);
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
//                            needCutPicture.isNeedCut(true, cutPicture(mContext, selectPhoto));
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
