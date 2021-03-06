package com.danielmerrill.gettingwarmer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.Toast;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by danielmerrill on 6/16/16.
 */
public class SignupActivity extends AppCompatActivity {

    private EditText pass,username;
    private int activityResult;
    private UsernameValidator usernameValidator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        activityResult = 0;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        pass=(EditText)findViewById(R.id.input_password);
        username=(EditText)findViewById(R.id.input_username);

        usernameValidator = new UsernameValidator();


        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#ffffff")));
        getSupportActionBar().setTitle(Html.fromHtml("<font color='#e12929'>Sign Up</font>"));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        pass.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    Signup(v);
                    return true;
                }
                return false;
            }
        });


    }

    public void toLoginScreen(View v) {

        finish();
    }




    public void Signup(View v){
        String usr = username.getText().toString();
        String pw = pass.getText().toString();
        String both = usr.toLowerCase() + pw;
        String encryptedString = Utils.getEncryptedString(both);

        //calling field validation method
        if(CheckFieldValidation()) {

            //setContentView(R.layout.progressbar_layout);
            //making object of RestAdapter
            RestAdapter adapter = new RestAdapter.Builder().setEndpoint(RestInterface.url).build();

            //Creating Rest Services
            final RestInterface restInterface = adapter.create(RestInterface.class);

            //Calling method to signup
            restInterface.SignUp(usr, encryptedString, new Callback<LoginModel>() {


                        @Override
                        public void success(LoginModel model, Response response) {

                            //finish();
                            //startActivity(getIntent());

                            String user = username.getText().toString();
                            //pass.setText("");
                            //username.setText("");


                            if (model.getStatus().equals("1")) {  //Signup Success
                                setResult(2);
                                Toast.makeText(SignupActivity.this, "Registered Successfully", Toast.LENGTH_SHORT).show();
                                finish();
                                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                                SharedPreferences.Editor editor = prefs.edit();
                                editor.putString("username", user);
                                editor.putBoolean("vibrate", true);
                                editor.commit();

                                Intent i = new Intent(SignupActivity.this, ColorActivity.class);
                                startActivity(i);

                            } else if (model.getStatus().equals("0"))  // Signup failure
                            {
                                Animation shake = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.shake);
                                username.startAnimation(shake);
                                Toast.makeText(SignupActivity.this, "Username Already Registered", Toast.LENGTH_SHORT).show();
                            }


                        }

                        @Override
                        public void failure(RetrofitError error) {
                            //finish();
                            //startActivity(getIntent());
                            String merror = error.getMessage();
                            Toast.makeText(SignupActivity.this, merror, Toast.LENGTH_LONG).show();
                        }
                    });
        }

    }

    //checking field are empty
    private boolean CheckFieldValidation(){
        String un = username.getText().toString();
        String pw = pass.getText().toString();

        boolean valid = usernameValidator.validate(un) && usernameValidator.validate(pw);
        if(username.getText().toString().equals("")){
            username.setError("Can't be Empty");
            valid=false;
        }else if(pass.getText().toString().equals("")){
            pass.setError("Can't be Empty");
            valid=false;
        }

        if (!valid) {
            Toast.makeText(getApplicationContext(), "3-15 characters \nLetters, numbers, and . _ - only", Toast.LENGTH_SHORT).show();
        }

        return valid;

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(menuItem);
    }

    protected void onStop() {

        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
