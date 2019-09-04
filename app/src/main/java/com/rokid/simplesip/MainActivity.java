package com.rokid.simplesip;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.autulin.gb28181library.JNIBridge;
import com.rokid.simplesip.tools.Logger;
import com.rokid.simplesip.ua.Receiver;
import com.rokid.simplesip.ua.SipParam;
import com.rokid.simplesip.ua.SipdroidEngine;
import com.rokid.simplesip.ua.UserAgent;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class MainActivity extends AppCompatActivity {

    private EditText hostEdit;
    private EditText portEdit;
    private EditText idEdit;
    private EditText domainEdit;
    private TextView logTextView;
    private StringBuilder sb = new StringBuilder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Logger.i("JNIBridge:" + JNIBridge.stringFromJNI());

        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.CAMERA}, 1);


        //---- 账号密码
        final SipParam param = AccountConfigHelper.getInstance().getAccountConfig(AccountConfigHelper.TAG.HIK);

        hostEdit = findViewById(R.id.host);
        portEdit = findViewById(R.id.port);
        idEdit = findViewById(R.id.id);
        domainEdit = findViewById(R.id.domain);
        Button connectButton = findViewById(R.id.connect);
        Button disConnectButton = findViewById(R.id.disconnect);
        logTextView = findViewById(R.id.connect_log);
        logTextView.setMovementMethod(ScrollingMovementMethod.getInstance());

        hostEdit.setText(param.getServer());
        portEdit.setText(param.getPort());
        idEdit.setText(param.getUsername());
        domainEdit.setText(param.getDomain());

        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connect(param);
            }
        });

        disConnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disConnect();
            }
        });
    }

    private void connect(SipParam param) {
        param.setServer(hostEdit.getText().toString().trim());
        param.setPort(portEdit.getText().toString().trim());
        param.setUsername(idEdit.getText().toString().trim());
        param.setDomain(domainEdit.getText().toString().trim());

        Receiver.initSip(this, param);
        Receiver.engine(this, new Receiver.StateCallback() {
            @Override
            public void changeStatus(int state) {
                Logger.d("MainActivity  changeStatus state=" + state);
            }
        });
        final SipdroidEngine engine = Receiver.engine(this);
        engine.setSipdroidEngineCallback(new SipdroidEngine.SipdroidEngineCallback() {
            @Override
            public void onCallAccepted(UserAgent agent, UserAgent.onRingParam param) {
                Logger.d("MainActivity onCallAccepted isstart=");
                final String msg = param.toString();
                Logger.i(msg);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        addLog(msg);
                    }
                });
                Intent intent = new Intent(MainActivity.this, DemoActivity.class);
                intent.putExtra("host", param.remote_media_address);
                intent.putExtra("port", param.remote_video_port);
                startActivity(intent);
            }

            @Override
            public void onCallClosing() {
                Logger.d("MainActivity onCallClosing()");
            }
        });

    }

    private void disConnect() {
        Receiver.finishSip();
        logTextView.setText("");
    }

    private void addLog(String log) {
        sb.append(log);
        logTextView.setText(sb.toString());
    }

    @Override
    protected void onDestroy() {
        disConnect();
        super.onDestroy();
    }
}
