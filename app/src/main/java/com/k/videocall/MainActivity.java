package com.k.videocall;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.easemob.EMCallBack;
import com.easemob.EMError;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMContactListener;
import com.easemob.chat.EMContactManager;
import com.easemob.chat.EMGroupManager;
import com.easemob.exceptions.EaseMobException;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private EditText editTel;

    private Button btnCall;

    private EditText editUserName;

    private EditText editPwd;

    private Button btnSignUp;

    private Button btnLogin;

    private EditText editName;

    private Button btnAdd;

    private EditText editToName;

    private Button btnAccept;

    private ExecutorService executors;

    private Message msg = null;

    public static boolean isLogin = false;

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            Toast.makeText(MainActivity.this, (String)msg.obj, Toast.LENGTH_SHORT).show();
            return false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editTel = (EditText) findViewById(R.id.editTel);
        btnCall = (Button) findViewById(R.id.btnCall);
        editUserName = (EditText) findViewById(R.id.editUsername);
        editPwd = (EditText) findViewById(R.id.editPwd);
        btnSignUp = (Button) findViewById(R.id.btnSignUp);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        editName = (EditText) findViewById(R.id.editName);
        btnAdd = (Button) findViewById(R.id.btnAdd);
        editToName = (EditText) findViewById(R.id.editToName);
        btnAccept = (Button) findViewById(R.id.btnAccept);

        executors = Executors.newSingleThreadExecutor();

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String userName = editUserName.getText().toString();
                final String pwd = editPwd.getText().toString();

                executors.execute(new Runnable() {
                    @Override
                    public void run() {
                        msg = new Message();
                        try {
                            EMChatManager.getInstance().createAccountOnServer(userName, pwd);
                        } catch (EaseMobException e) {
//                            e.printStackTrace();
                            int errorCode = e.getErrorCode();
                            if (errorCode == EMError.NONETWORK_ERROR) {
                                msg.obj = "网络异常，请检查网络！";
                                handler.sendMessage(msg);
                            } else if (errorCode == EMError.USER_ALREADY_EXISTS) {
                                msg.obj = "用户已存在！";
                                handler.sendMessage(msg);
                            } else if (errorCode == EMError.UNAUTHORIZED) {
                                msg.obj = "注册失败，无权限！";
                                handler.sendMessage(msg);
                            } else {
                                msg.obj = "注册失败: " + e.getMessage();
                                handler.sendMessage(msg);
                            }
                        }
                        msg.obj = "注册成功";
                        handler.sendMessage(msg);
                    }
                });
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EMChatManager.getInstance().login(editUserName.getText().toString(), editPwd.getText().toString(), new EMCallBack() {
                    @Override
                    public void onSuccess() {
                        EMGroupManager.getInstance().loadAllGroups();
                        EMChatManager.getInstance().loadAllConversations();
                        isLogin = true;
                        msg = handler.obtainMessage();
                        msg.obj = "登录成功: ";
                        msg.sendToTarget();

                    }

                    @Override
                    public void onError(int i, String s) {
                        isLogin = false;
                        msg = handler.obtainMessage();
                        msg.obj = "登录失败: ";
                        msg.sendToTarget();
                    }

                    @Override
                    public void onProgress(int i, String s) {

                    }
                });
            }
        });

        btnCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String s = editTel.getText().toString();
                startActivity(new Intent(MainActivity.this, VideoCallActivity.class).putExtra("username", s).putExtra(
                        "isComingCall", false));
            }
        });

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    EMContactManager.getInstance().addContact(editName.getText().toString(), "Hello world");
                } catch (EaseMobException e) {
                    e.printStackTrace();
                }
            }
        });

        btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    EMChatManager.getInstance().acceptInvitation(editToName.getText().toString());
                } catch (EaseMobException e) {
                    e.printStackTrace();
                }
            }
        });

        EMContactManager.getInstance().setContactListener(new EMContactListener() {
            @Override
            public void onContactAdded(List<String> list) {

            }

            @Override
            public void onContactDeleted(List<String> list) {

            }

            @Override
            public void onContactInvited(String s, String s1) {

            }

            @Override
            public void onContactAgreed(String s) {
                msg = handler.obtainMessage();
                msg.obj = "好友请求被同意";
                msg.sendToTarget();
            }

            @Override
            public void onContactRefused(String s) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter(EMChatManager.getInstance().getContactInviteEventBroadcastAction());
        registerReceiver(contactInviteReceiver, intentFilter);
    }

    public BroadcastReceiver contactInviteReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String reason = intent.getStringExtra("reason");
            boolean isResponse = intent.getBooleanExtra("isResponse", false);
            String from = intent.getStringExtra("username");
            Log.e("TAG", reason + " ; " + isResponse + " ; " + from);
            if (!isResponse){
                editToName.setText(from);
                msg = handler.obtainMessage();
                msg.obj = from + "请求加你为好友,reason: " + reason;
                msg.sendToTarget();
            }else {
                msg = handler.obtainMessage();
                msg.obj = from + "同意了你的好友请求";
                msg.sendToTarget();
            }
        }
    };

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(contactInviteReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
