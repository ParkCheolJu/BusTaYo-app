package com.bhsd.bustayo.activity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.bhsd.bustayo.R;
import com.bhsd.bustayo.application.APIManager;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class JoinActivity extends AppCompatActivity {

    Button btncomplete;
    EditText insertID,insertPasswd,insertName, checkPasswd, birthday, phoneNumber, checkemail;
    TextView checkID, checkPW;
    RadioGroup checkgender;
    String id, passwd, name, birth, gender, phoneNum, email, _result;
    boolean id_check = false;
    boolean pw_check = false;
    ImageView drawerinfo;
    ConstraintLayout optioninfo;
    Spinner emailform;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);

        findViewById(R.id.searchGoBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btncomplete = findViewById(R.id.Completionjoin);
        insertID = findViewById(R.id.insertID);
        insertPasswd = findViewById(R.id.insertPW);
        insertName = findViewById(R.id.insertName);
        checkPasswd = findViewById(R.id.checkingPW);
        optioninfo = findViewById(R.id.optioninfo);
        birthday = findViewById(R.id.insertBirthday);
        phoneNumber = findViewById(R.id.insertPhoneNum);
        checkgender = findViewById(R.id.userGender);
        checkemail = findViewById(R.id.insertEmail);
        emailform = findViewById(R.id.emailForm);
        drawerinfo = findViewById(R.id.drawerinfo);
        checkID = findViewById(R.id.checkID);
        checkPW = findViewById(R.id.checkPW);

        insertEmail();

        /* ????????? ?????? ???????????? ?????? ?????? ?????? ????????? */
        insertID.addTextChangedListener(new InputText() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                id_check = false;
                checkID.setVisibility(View.GONE);
            }
        });

        /* ????????? ?????? ?????? */
        insertID.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus) {
                    new Thread() {
                        @Override
                        public void run() {
                            id = insertID.getText().toString();
                            synchronized ((Object) id_check) {
                                getDB();
                            }

                            String msg = "";
                            final int color;
                            // id_check ??????! db?????? ??? ???????????? ???????????? ????????? true, ????????? false

                            synchronized ((Object) id_check) {
                                if (!id_check) {
                                    color = getColor(R.color.bus_red);
                                    msg = "????????? ??????????????????.";
                                } else {
                                    color = getColor(R.color.bus_green);
                                    msg = "?????? ????????? ??????????????????.";
                                }
                            }

                            final String finalMsg = msg;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    checkID.setVisibility(View.VISIBLE);
                                    checkID.setTextColor(color);
                                    checkID.setText(finalMsg);
                                }
                            });

                            interrupt();    // ?????? ???????????? ??????
                        }
                    }.start();
                }
            }
        });

        /* ????????????, ???????????? ?????? ?????? ???????????? ???????????? ?????? ?????? ????????? */
        insertPasswd.addTextChangedListener(new InputText() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                pw_check = false;
                checkPW.setVisibility(View.GONE);
            }
        });
        checkPasswd.addTextChangedListener(new InputText() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                pw_check = false;
                checkPW.setVisibility(View.GONE);
            }
        });

        /* ???????????? ?????? */
        checkPasswd.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus) {
                    int color;
                    String check = checkPasswd.getText().toString(), msg = "";
                    passwd = insertPasswd.getText().toString();

                    checkPW.setVisibility(View.VISIBLE);
                    if(!check.equals(passwd)) {
                        color = getColor(R.color.bus_red);
                        msg = "??????????????? ???????????? ????????????.";
                        pw_check = false;
                    } else {
                        color = getColor(R.color.bus_green);
                        msg = "??????????????? ???????????????.";
                        pw_check = true;
                    }
                    checkPW.setTextColor(color);
                    checkPW.setText(msg);
                }
            }
        });

        btncomplete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                id = insertID.getText().toString();
                passwd = insertPasswd.getText().toString();
                name = insertName.getText().toString();

                if (id.equals("")) {            // ????????? ?????? ??????
                    Toast.makeText(JoinActivity.this, "???????????? ??????????????????.", Toast.LENGTH_SHORT).show();
                } else if(!id_check) {           // ????????? ???????????? ??????
                    Toast.makeText(JoinActivity.this, "????????? ??????????????? ????????????.", Toast.LENGTH_SHORT).show();
                } else if(passwd.equals("")) {  // ???????????? ?????? ??????
                    Toast.makeText(JoinActivity.this, "??????????????? ??????????????????.", Toast.LENGTH_SHORT).show();
                } else if(!pw_check) {          // ???????????? ?????? ??????
                    Toast.makeText(JoinActivity.this, "??????????????? ???????????? ????????????.", Toast.LENGTH_SHORT).show();
                } else if(name.equals("")) {    // ?????? ?????? ??????
                    Toast.makeText(JoinActivity.this, "????????? ??????????????????.", Toast.LENGTH_SHORT).show();
                } else {
                    birth = birthday.getText().toString();
                    phoneNum = phoneNumber.getText().toString();
                    int check = checkgender.getCheckedRadioButtonId();
                    if (check == R.id.female)       // ?????? ??????
                        gender = "w";
                    else if (check == R.id.male)    // ?????? ??????
                        gender = "m";
                    else                            // ???????????? ?????? ??????
                        gender = "";
                    String temp = checkemail.getText().toString();
                    email = temp.equals("")? "" : temp + "@" + emailform.getSelectedItem().toString();
                    // ????????? ?????????????????? ???????????? ???????????? ????????????? "" ????????? ???????????????

                    new Thread() {
                        @Override
                        public void run() {
                            insertDB();

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if(_result.equals("1")) {
                                        Toast.makeText(JoinActivity.this, "????????? ?????????????????????.", Toast.LENGTH_LONG).show();
                                        finish();
                                    } else {
                                        Toast.makeText(JoinActivity.this, "????????? ??????????????????.", Toast.LENGTH_LONG).show();
                                    }

                                }
                            });

                            interrupt();    // ?????? ???????????? ??????
                        }
                    }.start();
                }
            }
        });


        drawerinfo.setOnClickListener(new View.OnClickListener() {  // ??? ??????
            @Override
            public void onClick(View v) {
                  if(optioninfo.getVisibility()==View.VISIBLE) {
                      optioninfo.setVisibility(View.GONE);
                  } else {
                      optioninfo.setVisibility(View.VISIBLE);
                  }
            }
        });

    }

    void getDB() {
        String result_msg = "";
        try {
            // Open the connection
            URL url = new URL(APIManager.GET_USER_ID + id);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            InputStream is = conn.getInputStream();

            // Get the stream
            StringBuilder builder = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }

            // Set the result
            result_msg = builder.toString();

            JSONObject base = new JSONObject(result_msg);
            JSONObject result = (JSONObject) base.get("result");

            id_check = result.getInt("rowCount") == 0;

            Log.e("yj", ""+id_check);
        } catch (Exception e) {
            Log.e("yj", ""+e);
        }
    }

    void insertDB() {
        String result_msg = "";
        try {
            String i_pw = "&pw=" + passwd;
            String i_name = "&name=" + name;
            String i_birthdate = birth.equals("")? "" : "&birthdate=" + birth;
            String i_tel = phoneNum.equals("")? "" : "&tel=" + phoneNum;
            String i_gender = gender.equals("")? "" : "&gender=" + gender;
            String i_email = email.equals("")? "" : "&email=" + email;
            URL url = new URL(APIManager.JOIN_USER + id +
                    i_pw + i_name + i_birthdate + i_tel + i_gender + i_email);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            InputStream is = conn.getInputStream();

            // Get the stream
            StringBuilder builder = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }

            // Set the result
            result_msg = builder.toString();

            JSONObject base = new JSONObject(result_msg);
            _result = base.getString("result");

        } catch (Exception e) {
            Log.e("yj", ""+e);
        }
    }

    public void insertEmail(){
        String[] email ={"naver.com", "daum.net", "empal.com", "nate.com", "dreamwiz.com", "hanmail.net"};

        ArrayAdapter arrayAdapter = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, email);
        emailform.setAdapter(arrayAdapter);
    }
}

/* TextWatcher?????? ????????? ?????? ??????????????? InputText ?????????! */
abstract class InputText implements TextWatcher {
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        // ?????? ???
    }
    @Override
    public void afterTextChanged(Editable s) {
        // ?????? ???
    }
}
