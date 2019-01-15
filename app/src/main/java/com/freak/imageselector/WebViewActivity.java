package com.freak.imageselector;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.DownloadListener;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.freak.imageselector.util.BitmapUtil;
import com.freak.imageselector.util.ICompressImageResponse;
import com.freak.imageselector.util.ImageSelector;
import com.freak.imageselector.util.PermissionUtils;
import com.freak.imageselector.util.popup.PopupGetPictureView;
import com.hellokiki.rrorequest.SimpleCallBack;
import com.just.agentweb.AgentWeb;
import com.just.agentweb.AgentWebConfig;
import com.just.agentweb.DefaultWebClient;
import com.tbruyelle.rxpermissions2.Permission;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.freak.imageselector.util.ImageSelector.CUT_PHOTO;
import static com.freak.imageselector.util.ImageSelector.GET_PICTURE_SELECT_PHOTO;
import static com.freak.imageselector.util.ImageSelector.GET_PICTURE_TAKE_PHOTO;

public class WebViewActivity extends AppCompatActivity {
    public static void startAction(Context context) {
        Intent intent = new Intent();
        intent.setClass(context, WebViewActivity.class);
        context.startActivity(intent);
    }

    private String url = "file:///android_asset/text.html";
    private AgentWeb mAgentWeb;
    private LinearLayout mLinearLayoutWebView;
    private WebView mWebView;
    private long mTime;
    private ValueCallback<Uri[]> mUploadCallbackAboveFive;
    private ValueCallback<Uri> mUploadMessage;
    private final int RESULT_CODE_IMAGE = 1005;
    private File userImgFile;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setContentView(R.layout.activity_web_view);
        super.onCreate(savedInstanceState);
        initView();
        initData();
        new RxPermissions(this)
                .requestEach(Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_PHONE_STATE)
                .subscribe(new SimpleCallBack<Permission>() {
                    @Override
                    public void onSuccess(Permission permission) {
                        if (permission.granted) {

                        } else {
                            Toast.makeText(WebViewActivity.this, "请允许打开需要的权限", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }

                    @Override
                    public void onFailure(Throwable throwable) {

                    }
                });
    }



    public void initData() {
        mAgentWeb = AgentWeb.with(this)
                .setAgentWebParent(mLinearLayoutWebView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
                .useDefaultIndicator(-1, 3)
                .setWebViewClient(mWebViewClient)
                .setWebChromeClient(mWebChromeClient)
                .setSecurityType(AgentWeb.SecurityType.STRICT_CHECK)
                .setMainFrameErrorView(R.layout.agentweb_error_page, -1)
                .setOpenOtherPageWays(DefaultWebClient.OpenOtherPageWays.DISALLOW)
                .interceptUnkownUrl()
                .createAgentWeb()
                .ready()
                .go(url);
        mWebView = mAgentWeb.getWebCreator().getWebView();

        if (url.contains("<html>")) {
            mWebView.loadDataWithBaseURL(null, url.toString(), "text/html", "utf-8", null);
        }

        mAgentWeb.getWebCreator().getWebView().setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                Uri uri = Uri.parse(url);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });
    }

    private WebViewClient mWebViewClient = new WebViewClient() {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
        }
    };
    private WebChromeClient mWebChromeClient = new WebChromeClient() {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            //do you work
        }

        @SuppressWarnings("unused")
        public void openFileChooser(ValueCallback<Uri> uploadMsg, String AcceptType, String capture) {
            this.openFileChooser(uploadMsg);
        }

        @SuppressWarnings("unused")
        public void openFileChooser(ValueCallback<Uri> uploadMsg, String AcceptType) {
            this.openFileChooser(uploadMsg);
        }

        public void openFileChooser(ValueCallback<Uri> uploadMsg) {
            mUploadMessage = uploadMsg;
            showSelector(WebViewActivity.this, mWebView);
        }

        @Override
        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
            mUploadCallbackAboveFive = filePathCallback;
            showSelector(WebViewActivity.this, mWebView);
            return true;
        }

    };

    @Override
    public void onBackPressed() {
        if (!mAgentWeb.back()) {
//            if (System.currentTimeMillis() - mTime > 1500) {
//                Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
//                mTime = System.currentTimeMillis();
//            } else {
//            }
            finish();
        } else {
            AgentWebConfig.clearDiskCache(this);
        }
    }


    public void initView() {
        mLinearLayoutWebView = findViewById(R.id.linear_layout_webview);
    }


    @Override
    protected void onResume() {
        mAgentWeb.getWebLifeCycle().onResume();
        super.onResume();
    }

    @Override
    protected void onPause() {
        mAgentWeb.getWebLifeCycle().onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mAgentWeb.getWebLifeCycle().onDestroy();
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e("TAG", "结果码-->" + resultCode + "\n请求码--》" + requestCode);
        if (resultCode == 0) {
            if (mUploadCallbackAboveFive != null) {
                mUploadCallbackAboveFive.onReceiveValue(null);
                mUploadCallbackAboveFive = null;
            }

            if (mUploadMessage != null) {
                mUploadMessage.onReceiveValue(null);
                mUploadMessage = null;
            }
            return;
        }
        switch (requestCode) {
            //拍照
            case GET_PICTURE_TAKE_PHOTO:
                userImgFile = ImageSelector.cutPicture(this, userImgFile);
                break;
            //选择照片
            case GET_PICTURE_SELECT_PHOTO:
                userImgFile = ImageSelector.getPhotoFromIntent(data, this);
                userImgFile = ImageSelector.cutPicture(this, userImgFile);
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
                Uri result = ImageSelector.toURI(WebViewActivity.this, imgFile);
                Log.e("TAG", "文件URI-->" + result);
                if (null != mUploadMessage && null == mUploadCallbackAboveFive) {
                    mUploadMessage.onReceiveValue(result);
                    mUploadMessage = null;
                }

                if (null == mUploadMessage && null != mUploadCallbackAboveFive) {
                    mUploadCallbackAboveFive.onReceiveValue(new Uri[]{result});
                    mUploadCallbackAboveFive = null;
                }
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
                            if (mUploadCallbackAboveFive != null) {
                                mUploadCallbackAboveFive.onReceiveValue(null);
                                mUploadCallbackAboveFive = null;
                            }

                            if (mUploadMessage != null) {
                                mUploadMessage.onReceiveValue(null);
                                mUploadMessage = null;
                            }
                            popupWindow.dismiss();
                        }
                    }
                });
        popupGetPictureView.showPop(view);
    }
}
