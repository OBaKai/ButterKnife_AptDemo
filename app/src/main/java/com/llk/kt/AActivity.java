package com.llk.kt;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;

import com.llk.annotation.BindLayout;
import com.llk.annotation.BindView;

@BindLayout(id = R.layout.activity_a)
public class AActivity extends Activity {

    @BindView(id = R.id.a_btn)
    public Button aBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
