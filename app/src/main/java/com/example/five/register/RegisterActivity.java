package com.example.five.register;

import java.io.File;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.five.load.LoginActivity;
import com.example.five.myapplication.R;
import com.example.five.util.CleanableEditText;
import com.example.five.util.XEditText;
import com.makeramen.roundedimageview.RoundedImageView;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    private Button register_but;
    private Button register_cls;

    private XEditText reg_usr;
    private XEditText reg_pwd;
    private XEditText reg_pwd2;

    private RoundedImageView imageHeading;

    private Bitmap photo = null;

    // 自定义的头像编辑弹出框
    private SelectPicPopupWindow menuWindow;

    // 上传服务器的路径【一般不硬编码到程序中】
    private String imgUrl = "";
    private String resultStr = "";	// 服务端返回结果集
    private String urlpath;
    private static final String IMAGE_FILE_NAME = "headingImage.jpg";// 头像文件名称
    private static ProgressDialog pd;// 等待进度圈
    private static final int REQUESTCODE_PICK = 0;		// 相册选图标记
    private static final int REQUESTCODE_TAKE = 1;		// 相机拍照标记
    private static final int REQUESTCODE_CUTTING = 2;	// 图片裁切标记

    private static final int RESULT_OK = 222;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);

        initViews();
    }

    /**
     * 初始化页面控件
     */
    private void initViews() {
        register_but = (Button)findViewById(R.id.register_but);
        register_cls= (Button)findViewById(R.id.register_cls);
        imageHeading = (RoundedImageView)findViewById(R.id.imgheading);

        reg_usr = (XEditText)findViewById(R.id.reg_usr);
        reg_pwd = (XEditText)findViewById(R.id.reg_pwd);
        reg_pwd2 = (XEditText)findViewById(R.id.reg_pwd2);

        register_but.setOnClickListener(this);
        register_cls.setOnClickListener(this);
        imageHeading.setOnClickListener(this);
    }

    public void onClick(View view){
//        Intent intent = null;
        switch (view.getId()){
            case R.id.imgheading:
                menuWindow = new SelectPicPopupWindow(RegisterActivity.this, itemsOnClick);
                menuWindow.showAtLocation(findViewById(R.id.layout_register),
                        Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0, 0);
                break;
            case R.id.register_but:
                Intent data =  new Intent();

                String usr = reg_usr.getText().toString();
                String pwd = reg_pwd.getText().toString();
                String pwd2 = reg_pwd2.getText().toString();

                if (usr.equals("")){
                    Toast.makeText(getApplicationContext(), "请填写用户名", Toast.LENGTH_SHORT).show();
                }
                else if (!pwd.equals(pwd2)){
                    Toast.makeText(getApplicationContext(), "密码错误，请重填！", Toast.LENGTH_LONG).show();
                }else{
                    data.putExtra("username", reg_usr.getText().toString());
                    data.putExtra("password", reg_pwd.getText().toString());
                    data.putExtra("bitmap", photo);
                    setResult(RESULT_OK, data);
                    finish();
                }
                break;
            case R.id.register_cls:
                finish();
                break;
        }
    }

    //为弹出窗口实现监听类
    private View.OnClickListener itemsOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            menuWindow.dismiss();
            switch (v.getId()) {
                // 拍照
                case R.id.takePhotoBtn:
                    Intent takeIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    //下面这句指定调用相机拍照后的照片存储的路径
//                    takeIntent.putExtra(MediaStore.EXTRA_OUTPUT,
//                            Uri.fromFile(new File(Environment.getExternalStorageDirectory(), IMAGE_FILE_NAME)));
                    startActivityForResult(takeIntent, REQUESTCODE_TAKE);
                    break;
                // 相册选择图片
                case R.id.pickPhotoBtn:
                    Intent pickIntent = new Intent(Intent.ACTION_PICK, null);
                    // 如果朋友们要限制上传到服务器的图片类型时可以直接写如："image/jpeg 、 image/png等的类型"
                    pickIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                    startActivityForResult(pickIntent, REQUESTCODE_PICK);
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case REQUESTCODE_PICK:// 直接从相册获取
                try {
                    startPhotoZoom(data.getData());
                } catch (NullPointerException e) {
                    e.printStackTrace();// 用户点击取消操作
                }
                break;
            case REQUESTCODE_TAKE:// 调用相机拍照
                File temp = new File(Environment.getExternalStorageDirectory() + "/" + IMAGE_FILE_NAME);
                startPhotoZoom(Uri.fromFile(temp));
                break;
            case REQUESTCODE_CUTTING:// 取得裁剪后的图片
                if (data != null) {
                    setPicToView(data);
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 裁剪图片方法实现
     * @param uri
     */
    public void startPhotoZoom(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        // crop=true是设置在开启的Intent中设置显示的VIEW可裁剪
        intent.putExtra("crop", "true");
        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // outputX outputY 是裁剪图片宽高
        intent.putExtra("outputX", 300);
        intent.putExtra("outputY", 300);
        intent.putExtra("return-data", true);
        startActivityForResult(intent, REQUESTCODE_CUTTING);
    }

    /**
     * 保存裁剪之后的图片数据
     * @param picdata
     */
    private void setPicToView(Intent picdata) {
        Bundle extras = picdata.getExtras();
        if (extras != null) {
            // 取得SDCard图片路径做显示
            photo = extras.getParcelable("data");
            Drawable drawable = new BitmapDrawable(null, photo);
            //urlpath = FileUtil.saveFile(RegisterActivity.this, "temphead.jpg", photo);
            imageHeading.setImageDrawable(drawable);

            // 新线程后台上传服务端
//            pd = ProgressDialog.show(RegisterActivity.this, null, "正在上传图片，请稍候...");
//            new Thread(uploadImageRunnable).start();
        }
    }

    /**
     * 使用HttpUrlConnection模拟post表单进行文件
     * 上传平时很少使用，比较麻烦
     * 原理是： 分析文件上传的数据格式，然后根据格式构造相应的发送给服务器的字符串。
     */
    /**Runnable uploadImageRunnable = new Runnable() {
        @Override
        public void run() {

            if(TextUtils.isEmpty(imgUrl)){
                Toast.makeText(RegisterActivity.this, "还没有设置上传服务器的路径！", Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, String> textParams = new HashMap<String, String>();
            Map<String, File> fileparams = new HashMap<String, File>();

            try {
                // 创建一个URL对象
                URL url = new URL(imgUrl);
                textParams = new HashMap<String, String>();
                fileparams = new HashMap<String, File>();
                // 要上传的图片文件
                File file = new File(urlpath);
                fileparams.put("image", file);
                // 利用HttpURLConnection对象从网络中获取网页数据
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                // 设置连接超时（记得设置连接超时,如果网络不好,Android系统在超过默认时间会收回资源中断操作）
                conn.setConnectTimeout(5000);
                // 设置允许输出（发送POST请求必须设置允许输出）
                conn.setDoOutput(true);
                // 设置使用POST的方式发送
                conn.setRequestMethod("POST");
                // 设置不使用缓存（容易出现问题）
                conn.setUseCaches(false);
                conn.setRequestProperty("Charset", "UTF-8");//设置编码
                // 在开始用HttpURLConnection对象的setRequestProperty()设置,就是生成HTML文件头
                conn.setRequestProperty("ser-Agent", "Fiddler");
                // 设置contentType
                conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + NetUtil.BOUNDARY);
                OutputStream os = conn.getOutputStream();
                DataOutputStream ds = new DataOutputStream(os);
                NetUtil.writeStringParams(textParams, ds);
                NetUtil.writeFileParams(fileparams, ds);
                NetUtil.paramsEnd(ds);
                // 对文件流操作完,要记得及时关闭
                os.close();
                // 服务器返回的响应吗
                int code = conn.getResponseCode(); // 从Internet获取网页,发送请求,将网页以流的形式读回来
                // 对响应码进行判断
                if (code == 200) {// 返回的响应码200,是成功
                    // 得到网络返回的输入流
                    InputStream is = conn.getInputStream();
                    resultStr = NetUtil.readString(is);
                } else {
                    Toast.makeText(RegisterActivity.this, "请求URL失败！", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            handler.sendEmptyMessage(0);// 执行耗时的方法之后发送消给handler
        }
    };

    Handler handler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    pd.dismiss();

                    try {
                        // 返回数据示例，根据需求和后台数据灵活处理
                        // {"status":"1","statusMessage":"上传成功","imageUrl":"http://120.24.219.49/726287_temphead.jpg"}
                        JSONObject jsonObject = new JSONObject(resultStr);

                        // 服务端以字符串“1”作为操作成功标记
                        if (jsonObject.optString("status").equals("1")) {
                            BitmapFactory.Options option = new BitmapFactory.Options();
                            // 压缩图片:表示缩略图大小为原始图片大小的几分之一，1为原图，3为三分之一
                            option.inSampleSize = 1;

                            // 服务端返回的JsonObject对象中提取到图片的网络URL路径
                            String imageUrl = jsonObject.optString("imageUrl");
                            Toast.makeText(RegisterActivity.this, imageUrl, Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(RegisterActivity.this, jsonObject.optString("statusMessage"), Toast.LENGTH_SHORT).show();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    break;

                default:
                    break;
            }
            return false;
        }
    });*/
}