package com.polevpn.application;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.JsonReader;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.orhanobut.logger.Logger;
import com.polevpn.application.tools.SharePref;
import com.polevpn.application.tools.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import polevpnmobile.Polevpnmobile;

public class LoginActivity extends AppCompatActivity {

    private ImageView btn_login;
    private EditText emailText;
    private EditText pwdText;
    private TextView loginTitle;
    private TextView loginAction;
    private boolean loginFlag;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.requestPermission(this);

        setContentView(R.layout.activity_login);
        emailText = findViewById(R.id.editEmail);
        pwdText = findViewById(R.id.editPassword);
        loginTitle = findViewById(R.id.login_title);
        loginAction = findViewById(R.id.login_action);
        btn_login = (ImageView)findViewById(R.id.btn_login);
        loginFlag = true;
        emailText.setText(SharePref.getInstance().getString("email"));

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                String email = emailText.getText().toString().trim();
                String password = pwdText.getText().toString().trim();

                if(email.equals("") || password.equals("")){
                    Toast("email or password invalid");
                    return;
                }

                if(email.length()<4|| email.length() > 50 || password.length() > 16||password.length()<6){
                    Toast("email or password length invalid");
                    return;
                }



                if(loginFlag){

                    login(email,password);

                }else{
                    register(email,password);
                }
            }
        });

        loginAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(loginFlag){
                    loginTitle.setText("账户注册");
                    loginAction.setText("已有账户?点这里登陆");
                    emailText.setText("");
                    pwdText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    loginFlag = false;
                }else{
                    loginTitle.setText("账户登陆");
                    loginAction.setText("没有账户?点这里注册");
                    pwdText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    loginFlag = true;
                }
            }
        });
    }

    private void register(String email,String password){

        JSONObject req = new JSONObject();
        try{
            req.put("Email",email);
            req.put("Password",password);
        }catch (JSONException e){
            Logger.e(e,e.getMessage());
        }

        String apiHost =  SharePref.getInstance().getString("api_host");

        if(apiHost.equals("")){
            App.getSystemConfig();
            apiHost =  SharePref.getInstance().getString("api_host");
        }

        String header = "{\"X-User-Agent\":\""+ Utils.getUserAgent() +"\"}";
        Polevpnmobile.api(apiHost,"/api/user/register",header,req.toString(), (long ret, String msg, String resp) ->{
            if (ret != Polevpnmobile.HTTP_OK){
                if(ret == 1004){
                    Toast(msg);
                }else{
                    Toast("Network Error");
                    Logger.e(msg);
                }
            }
            login(email,password);
        });
    }

    private void login(String email,String password){

        JSONObject req = new JSONObject();
        try{
            req.put("Email",email);
            req.put("Password",password);
        }catch (JSONException e){
            Logger.e(e,e.getMessage());
        }

        String apiHost =  SharePref.getInstance().getString("api_host");

        if(apiHost.equals("")){
            App.getSystemConfig();
            apiHost =  SharePref.getInstance().getString("api_host");
        }

        String header = "{\"X-User-Agent\":\""+ Utils.getUserAgent() +"\"}";
        Polevpnmobile.api(apiHost,"/api/user/login",header,req.toString(), (long ret, String msg, String resp) ->{

            if (ret != Polevpnmobile.HTTP_OK){
                if(ret == 1004){
                    Toast(msg);
                }else{
                    Toast("Network Error");
                    Logger.e(msg);
                }
                return;
            }
            String token = "";
            long uid = 0L;
            try{
                JSONObject obj = new JSONObject(resp);
                token = obj.getString("Token");
                uid = obj.getLong("Uid");
            }catch (JSONException e){
                Logger.e(e,e.getMessage());
                return;
            }


            Handler handler = new Handler(Looper.getMainLooper());
            String finalToken = token;
            long finalUid = uid;
            handler.post(() -> {
                SharePref.getInstance().setString("email",email);
                SharePref.getInstance().setString("password",password);
                SharePref.getInstance().setBoolean("login",true);
                SharePref.getInstance().setString("token", finalToken);
                SharePref.getInstance().setLong("uid", finalUid);
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                LoginActivity.this.finish();
            });
        });
    }

    private void Toast(String msg){
        new Handler(Looper.getMainLooper()).post(()->{
            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
        });
    }
}
