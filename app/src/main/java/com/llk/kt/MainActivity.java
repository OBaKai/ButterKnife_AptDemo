package com.llk.kt;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.llk.annotation.BindClick;
import com.llk.annotation.BindLayout;
import com.llk.annotation.BindUtils;
import com.llk.annotation.BindView;

@BindLayout(id = R.layout.activity_main)
public class MainActivity extends Activity {

    @BindView(id = R.id.btn)
    public Button btn;
    @BindView(id = R.id.btn2)
    public Button btn2;

    @BindClick(ids = {R.id.btn, R.id.btn2})
    public void click(View view){
        switch (view.getId()){
            case R.id.btn:
                Toast.makeText(this, "我按了1", Toast.LENGTH_SHORT).show();
                break;
            case R.id.btn2:
                Toast.makeText(this, "我按了2", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);
        //BindUtils.bind(this);
        BindUtils.bind(this);

        btn.setText("我是1");
        btn2.setText("我是2");

//        findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) { click(v); }
//        });
    }
}
