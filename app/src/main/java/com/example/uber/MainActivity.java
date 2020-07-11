package com.example.uber;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;
import com.parse.LogInCallback;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;

public class MainActivity extends AppCompatActivity {

    enum State{
        LOGIN, SIGNUP
    }

    private State state = State.SIGNUP;
    private Button btnSignUp, btnOTL;
    private EditText edtUsername, edtPassword, edtChoice;
    private RadioButton rdbPassenger, rdbDriver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ParseInstallation.getCurrentInstallation().saveInBackground();
        if (ParseUser.getCurrentUser() != null){
            //ParseUser.logOut();
            transitionToNextActivity();
        }

        btnSignUp = findViewById(R.id.btnSignUp);
        btnOTL = findViewById(R.id.btnOTL);
        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        edtChoice = findViewById(R.id.edtChoose);
        rdbPassenger = findViewById(R.id.rdbPassenger);
        rdbDriver = findViewById(R.id.rdbDriver);

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (state == State.SIGNUP){
                    if (!rdbDriver.isChecked() && !rdbPassenger.isChecked()){
                        Toast.makeText(MainActivity.this, "Are you a driver or passenger?", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    ParseUser appUser = new ParseUser();
                    appUser.setUsername(edtUsername.getText().toString());
                    appUser.setPassword(edtPassword.getText().toString());
                    if (rdbPassenger.isChecked()){appUser.put("as", "Passenger");}
                    else if (rdbDriver.isChecked()){appUser.put("as", "Driver");}

                    appUser.signUpInBackground(new SignUpCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null){
                                Toast.makeText(MainActivity.this, ParseUser.getCurrentUser().getUsername() + " is signed up", Toast.LENGTH_SHORT).show();
                                transitionToNextActivity();
                                finish();
                            }
                            else {
                                Toast.makeText(MainActivity.this, "Error in signing up", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                }
                else if (state == State.LOGIN){
                    if (!rdbDriver.isChecked() && !rdbPassenger.isChecked()){
                        Toast.makeText(MainActivity.this, "Are you a driver or passenger?", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    ParseUser.logInInBackground(edtUsername.getText().toString(), edtPassword.getText().toString(), new LogInCallback() {
                        @Override
                        public void done(ParseUser user, ParseException e) {
                            if (user != null && e == null){
                                Toast.makeText(MainActivity.this, ParseUser.getCurrentUser().getUsername() + " is logged in", Toast.LENGTH_SHORT).show();
                                transitionToNextActivity();
                                finish();
                            }
                        }
                    });
                }
            }
        });


        btnOTL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (edtChoice.getText().toString().equals("Driver") || edtChoice.getText().toString().equals("Passenger")){
                    if (ParseUser.getCurrentUser() == null){
                        ParseAnonymousUtils.logIn(new LogInCallback() {
                            @Override
                            public void done(ParseUser user, ParseException e) {
                                if (user != null && e == null){
                                    user.put("as", edtChoice.getText().toString());
                                    user.saveInBackground(new SaveCallback() {
                                        @Override
                                        public void done(ParseException e) {
                                            transitionToNextActivity();
                                            finish();
                                        }
                                    });
                                }
                            }
                        });
                    }
                }
                else {
                    Toast.makeText(MainActivity.this, "Are you a driver or passenger?", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.my_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.loginItem:
                if (state == State.SIGNUP){
                    state = State.LOGIN;
                    item.setTitle("Sign Up");
                    btnSignUp.setText("Login");
                }
                else{
                    state = State.SIGNUP;
                    item.setTitle("Login");
                    btnSignUp.setText("Sign up");
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void transitionToNextActivity(){
        if (ParseUser.getCurrentUser() != null){
            if (ParseUser.getCurrentUser().get("as").equals("Passenger")) {
                Intent intent = new Intent(this, PassengersActivity.class);
                startActivity(intent);
            }
            else if (ParseUser.getCurrentUser().get("as").equals("Driver")) {
                Intent intent = new Intent(this, DriversActivity.class);
                startActivity(intent);
            }
        }
    }
}