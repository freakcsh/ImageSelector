package com.freak.imageselector;

import android.app.Application;

import com.freak.imageselector.util.ImageSelector;

/**
 *
 * @author freak
 * @date 2019/1/14
 */

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ImageSelector imageSelector=ImageSelector.getInstance();
        imageSelector.setTakePhotoCacheDir("/picture/photo");
        imageSelector.initDisplayOpinion(this);
    }
}
