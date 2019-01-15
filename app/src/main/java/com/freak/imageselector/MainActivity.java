package com.freak.imageselector;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;

import com.freak.imageselector.util.BitmapUtil;
import com.freak.imageselector.util.ICompressImageResponse;
import com.freak.imageselector.util.ImageSelector;
import com.freak.imageselector.util.PermissionUtils;
import com.freak.imageselector.util.popup.PopupGetPictureView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.freak.imageselector.util.ImageSelector.CUT_PHOTO;
import static com.freak.imageselector.util.ImageSelector.GET_PICTURE_SELECT_PHOTO;
import static com.freak.imageselector.util.ImageSelector.GET_PICTURE_TAKE_PHOTO;

/**
 * @author freak
 * @date 2019/01/14
 */
public class MainActivity extends AppCompatActivity {
    private Button btn_select, btn_web_view;
    private ImageView img_text;
    private File userImgFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn_select = findViewById(R.id.btn_select);
        img_text = findViewById(R.id.img_text);
        btn_web_view = findViewById(R.id.btn_web_view);
        btn_select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSelector(MainActivity.this, btn_select);
            }
        });
        btn_web_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WebViewActivity.startAction(MainActivity.this);
            }
        });
    }

    /**
     * 显示图片选择器
     *
     * @param context
     * @param view
     */
    public void showSelector(final Context context, View view) {
        PopupGetPictureView popupGetPictureView = new PopupGetPictureView(context, new
                PopupGetPictureView.GetPicture() {
                    @Override
                    public void takePhoto(View v) {
                        if (PermissionUtils.checkTakePhotoPermission(context)) {
                            userImgFile = ImageSelector.takePicture(context, GET_PICTURE_TAKE_PHOTO);
                        }
                    }

                    @Override
                    public void selectPhoto(View v) {
                        if (PermissionUtils.checkAlbumStroagePermission(context)) {
                            ImageSelector.photoPick(context, GET_PICTURE_SELECT_PHOTO);
                        }
                    }

                    @Override
                    public void cancel(PopupWindow popupWindow) {
                        if (popupWindow.isShowing()) {
                            popupWindow.dismiss();
                        }
                    }
                });
        popupGetPictureView.showPop(view);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 0) {
            return;
        }
        switch (requestCode) {
            //拍照
            case GET_PICTURE_TAKE_PHOTO:
                userImgFile = ImageSelector.cutPicture(MainActivity.this, userImgFile);
                break;
            //选择照片
            case GET_PICTURE_SELECT_PHOTO:
                userImgFile = ImageSelector.getPhotoFromIntent(data, MainActivity.this);
                userImgFile = ImageSelector.cutPicture(MainActivity.this, userImgFile);
                break;
            //裁剪照片
            case CUT_PHOTO:
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
     *
     * @param file
     */
    public void compressImage(File file) {
        List<File> list = new ArrayList<>();
        list.add(file);
        BitmapUtil.compressFiles(list, new ICompressImageResponse() {
            @Override
            public void onSuccess(List<File> images) {
                File imgFile = images.get(0);
                Uri uri = ImageSelector.toURI(MainActivity.this, imgFile);
                img_text.setImageURI(uri);
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
