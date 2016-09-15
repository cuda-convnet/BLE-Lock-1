package com.iboxshare.testble.ui.activity;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.dd.CircularProgressButton;
import com.iboxshare.testble.R;

/**
 * Created by KN on 16/9/13.
 */
public class UnlockActivity extends AppCompatActivity {
    private String TAG = "UnlockActivity";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unlock);
        final CircularProgressButton CPB = (CircularProgressButton) findViewById(R.id.activity_unlock_btn);
        CPB.setIndeterminateProgressMode(true);
        CPB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG,"点击按钮");
                if (CPB.getProgress() == 100)
                    CPB.setProgress(50);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        CPB.setProgress(100);
                    }
                }, 3000);
            }
        });
    }
}
