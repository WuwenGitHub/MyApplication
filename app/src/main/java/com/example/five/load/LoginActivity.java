package com.example.five.load;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.example.five.myapplication.R;
import com.example.five.register.RegisterActivity;
import com.example.five.util.XEditText;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_CODE_GO_TO_REGIST = 111;
    private static final int RESULT_OK = 222;

    private XEditText usr;
    private XEditText pwd;

    private Button but_load;
    private Button but_register;

    private ImageView img_head;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
    }

    /**
     * 初始化页面控件
     */
    public void initViews(){

        usr = (XEditText)findViewById(R.id.usr);
        pwd = (XEditText)findViewById(R.id.pwd);

        but_load = (Button)findViewById(R.id.load);
        but_register = (Button)findViewById(R.id.register);

        img_head = (ImageView)findViewById(R.id.heading);

        usr.setOnClickListener(this);
        pwd.setOnClickListener(this);

        but_load.setOnClickListener(this);
        but_register.setOnClickListener(this);
    }

    public void onClick(View v){
        switch (v.getId()){
            case R.id.usr:
                usr.setText("");
                break;
            case R.id.pwd:
                pwd.setText("");
                break;
            case R.id.load:
                break;
            case R.id.register:
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivityForResult(intent, REQUEST_CODE_GO_TO_REGIST);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_GO_TO_REGIST:
                //判断注册是否成功  如果注册成功
                if (resultCode == RESULT_OK) {
                    //则获取data中的账号和密码  动态设置到EditText中
                    usr.setText(data.getExtras().getString("username"));
                    pwd.setText(data.getExtras().getString("password"));
                    if (null != data.getParcelableExtra("bitmap"))
                        img_head.setImageBitmap((Bitmap) data.getParcelableExtra("bitmap"));
                }
                break;
        }
    }
}
