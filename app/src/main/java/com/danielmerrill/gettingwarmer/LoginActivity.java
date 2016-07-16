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
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.Toast;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class LoginActivity extends AppCompatActivity {

    EditText username,pass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        username=(EditText)findViewById(R.id.input_username);
        pass=(EditText)findViewById(R.id.input_password);


        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#ffffff")));
        getSupportActionBar().setTitle(Html.fromHtml("<font color='#e12929'>Log In</font>"));

        pass.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    LogIn(v);
                    return true;
                }
                return false;
            }
        });


    }




    // When Button Login clicked
    public void LogIn(View v){

        //Calling method of field validation
        if(CheckFieldValidation()) {

            //progressBar.setVisibility(View.VISIBLE);
            //setContentView(R.layout.progressbar_layout);
            //making object of RestAdapter
            RestAdapter adapter = new RestAdapter.Builder().setEndpoint(RestInterface.url).build();

            //Creating Rest Services
            final RestInterface restInterface = adapter.create(RestInterface.class);

            //Calling method to get check login
            restInterface.Login(username.getText().toString(), pass.getText().toString(), new Callback<LoginModel>() {
                @Override
                public void success(LoginModel model, Response response) {
                    String user = username.getText().toString();
                    //startActivity(getIntent());

                    //username.setText("");
                   // pass.setText("");


                    if (model.getStatus().equals("1")) {  //login Success

                        //Toast.makeText(LoginActivity.this, "Logged In SuccessFully", Toast.LENGTH_SHORT).show();
                        // do something after logIn
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("username", user);
                        editor.commit();
                        //Toast.makeText(LoginActivity.this, prefs.getString("username", ""), Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(LoginActivity.this, ColorActivity.class);
                        startActivity(i);
                        finish();

                    } else if (model.getStatus().equals("0"))  // login failure
                    {
                        Animation shake = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.shake);
                        username.startAnimation(shake);
                        Toast.makeText(LoginActivity.this, "Invalid UserName or Password ", Toast.LENGTH_SHORT).show();
                    }


                }

                @Override
                public void failure(RetrofitError error) {
                    //finish();
                    //startActivity(getIntent());
                    String merror = error.getMessage();
                    Toast.makeText(LoginActivity.this, merror, Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    //When Button Sign up clicked
    public void SignUp(View v){

        Intent i = new Intent(LoginActivity.this,SignupActivity.class);
        startActivityForResult(i, 0);



    }

    //checking field are empty
    private boolean CheckFieldValidation(){

        boolean valid=true;
        if(username.getText().toString().equals("")){
            username.setError("Can't be Empty");
            valid=false;
        }else if(pass.getText().toString().equals("")){
            pass.setError("Can't be Empty");
            valid=false;
        }

        return valid;

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == 2){
            finish();
        }
    }

}
