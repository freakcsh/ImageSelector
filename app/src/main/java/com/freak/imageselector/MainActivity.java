package com.freak.imageselector;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.freak.imageselector.util.ImageSelector;

/**
 * @author freak
 * @date 2019/01/14
 */
public class MainActivity extends AppCompatActivity {
    private Button btn_select, btn_web_view;
    private ImageView img_text;

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
                ImageSelector.getInstance().showSelector(MainActivity.this, btn_select);
            }
        });
        btn_web_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ImageSelector.getInstance().onSelectorResult(this,requestCode,resultCode,data);
        Log.e("TAG","URI=    "+ImageSelector.getInstance().getUri());
        img_text.setImageURI(ImageSelector.getInstance().getUri());
    }
}
