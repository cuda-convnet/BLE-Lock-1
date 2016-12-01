package com.iboxshare.testble.custom_views;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.iboxshare.testble.R;

/**
 * Created by KN on 16/10/27.
 */

public class BaseActivity extends AppCompatActivity {
    private Toolbar toolbar;
    public Toolbar getToolbar() {
        return toolbar;
    }
    protected void setToolbar(boolean returnable){
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if(toolbar!=null){
            toolbar.setTitle("");
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(returnable);
            }
        }else{
            Log.e("Toolbar","Null");
        }
    }
}
