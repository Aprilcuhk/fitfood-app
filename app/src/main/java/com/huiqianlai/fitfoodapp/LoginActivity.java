package com.huiqianlai.fitfoodapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.dd.processbutton.iml.ActionProcessButton;
import com.google.gson.Gson;
import com.huiqianlai.fitfoodapp.bean.LoginBean;
import com.huiqianlai.fitfoodapp.okhttp.OkHttpUtils;
import com.huiqianlai.fitfoodapp.okhttp.callback.StringCallback;
import com.huiqianlai.fitfoodapp.utils.data.SPUtils;
import com.qmuiteam.qmui.util.QMUIStatusBarHelper;

import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.MediaType;

public class LoginActivity extends AppCompatActivity {
    private EditText mUserName;
    private EditText mPassword;
    private ActionProcessButton mLogin;

    private TextView mGoToRegister;
    Timer timer = new Timer();

    private String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        QMUIStatusBarHelper.translucent(this);
        QMUIStatusBarHelper.setStatusBarLightMode(this);
        setContentView(R.layout.activity_login);
        setUI();
    }

    private void setUI() {
        mUserName = findViewById(R.id.edit_user_name);
        mPassword = findViewById(R.id.edit_password);
        mLogin = findViewById(R.id.login);


        mGoToRegister = findViewById(R.id.go_to_register);

        mGoToRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(mUserName.getText())) {
                    Toast.makeText(view.getContext(), "User name is null", Toast.LENGTH_LONG).show();
                } else if (TextUtils.isEmpty(mPassword.getText())) {
                    Toast.makeText(view.getContext(), "User password is null", Toast.LENGTH_LONG).show();
                } else {
                    doLogin();
                }
            }
        });
    }

    private void startLoading() {
        mLogin.setMode(ActionProcessButton.Mode.ENDLESS);
        if (timer == null) {
            timer = new Timer();
        }
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                mLogin.setProgress(50);
            }
        }, 1);
    }

    private void endLoading() {
        timer = null;
        mLogin.setProgress(0);
    }

    private void doLogin() {

        startLoading();

        postLogin();
    }

    private void postLogin() {
        String url = Consts.BASE_URL + "login";


        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("email", mUserName.getText());
            jsonObject.put("password", mPassword.getText());
        } catch (Exception e) {
            e.printStackTrace();
        }


        OkHttpUtils
                .postString()
                .url(url)
                .mediaType(MediaType.parse("application/json; charset=utf-8"))
                .content(jsonObject.toString())
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        e.printStackTrace();
                        Log.e("laihuiqian", "onError:" + e.getMessage());

                        endLoading();
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        Log.e(TAG, "onResponse???complete");
                        Log.d("laihuiqian", "onResponse:" + response);

                        endLoading();

                        try {
                            LoginBean loginbean = new Gson().fromJson(response, LoginBean.class);

                            SPUtils.put(LoginActivity.this, "token", loginbean.getData().getAccessToken());


                            if (TextUtils.equals(loginbean.getMessage(), "fail")) {
                                // login fail
                                Toast.makeText(LoginActivity.this, "Login failed!!", Toast.LENGTH_SHORT).show();
                            } else if (TextUtils.equals(loginbean.getMessage(), "success")) {
                                Toast.makeText(LoginActivity.this, "Login success!!", Toast.LENGTH_SHORT).show();
                                saveToken(loginbean.getData().getAccessToken());

                                Intent intent = new Intent(LoginActivity.this, ActionActivity.class);
                                startActivity(intent);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                });
    }

    /**
     * ??????token?????????sp
     *
     * @param token
     */
    private void saveToken(String token) {
        SPUtils.put(this, "token", token);
    }
}