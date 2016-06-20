package com.danielmerrill.gettingwarmer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.MenuItem;
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

/**
 * Created by danielmerrill on 6/16/16.
 */
public class SignupActivity extends AppCompatActivity {

    EditText pass,username;
    int activityResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        activityResult = 0;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        pass=(EditText)findViewById(R.id.input_password);
        username=(EditText)findViewById(R.id.input_username);


        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#ffffff")));
        getSupportActionBar().setTitle(Html.fromHtml("<font color='#e12929'>Sign Up</font>"));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


    }

    public void toLoginScreen(View v) {


        finish();
    }


    public void Signup(View v){


        //calling field validation method
        if(CheckFieldValidation()) {

            //setContentView(R.layout.progressbar_layout);
            //making object of RestAdapter
            RestAdapter adapter = new RestAdapter.Builder().setEndpoint(RestInterface.url).build();

            //Creating Rest Services
            final RestInterface restInterface = adapter.create(RestInterface.class);

            //Calling method to signup
            restInterface.SignUp(username.getText().toString(), pass.getText().toString(), new Callback<LoginModel>() {


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
                                editor.commit();

                                Intent i = new Intent(SignupActivity.this, Homepage.class);
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
