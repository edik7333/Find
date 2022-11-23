package com.bagrut.find;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Debug;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * a class for register activity
 */
public class Register extends AppCompatActivity
{
    /**
     * enter first name
     */
    EditText fname;
    /**
     * enter last name
     */
    EditText lname;
    /**
     * enter email
     */
    EditText email;
    /**
     * enter password
     */
    EditText password;
    /**
     * enter password conformation
     */
    EditText passwordConf;
    /**
     * enter phone
     */
    EditText phone;
    /**
     * select gender
     */
    RadioGroup genderOptions;
    /**
     * register an account
     */
    Button register;
    /**
     * user account data
     */
    UserAccount account;
    /**
     * shows loading
     */
    ProgressBar progressBar;

    /**
     * initialization
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        fname = findViewById(R.id.fname);
        lname = findViewById(R.id.lname);
        genderOptions = findViewById(R.id.gender_options);
        email = findViewById(R.id.email_in_register);
        password = findViewById(R.id.password_in_register);
        passwordConf = findViewById(R.id.rePass_in_register);
        phone = findViewById(R.id.phone_in_register);
        register = findViewById(R.id.registerButton);
        progressBar = findViewById(R.id.progressBar_register);

        progressBar.setVisibility(View.INVISIBLE);
        register.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                register();
            }
        });
    }

    /**
     * registers an account
     */
    private void register()
    {
        progressBar.setVisibility(View.VISIBLE);
        if(!password.getText().toString().equals(passwordConf.getText().toString()))
        {
            progressBar.setVisibility(View.INVISIBLE);
            Toast.makeText(Register.this, "passwords don't mach", Toast.LENGTH_LONG).show();
            return;
        }
        if(email.getText().toString().isEmpty()||password.getText().toString().isEmpty()||fname.getText().toString().isEmpty()||lname.getText().toString().isEmpty()||email.getText().toString().isEmpty()||phone.getText().toString().isEmpty())
        {
            progressBar.setVisibility(View.INVISIBLE);
            Toast.makeText(Register.this, "empty fields", Toast.LENGTH_LONG).show();
            return;
        }
        DBConn.firebaseAuth.createUserWithEmailAndPassword(email.getText().toString(),password.getText().toString()).addOnCompleteListener(this, new OnCompleteListener<AuthResult>()
        {
            @Override
            public void onComplete(Task<AuthResult> task)
            {
                if (task.isSuccessful())
                {
                    Toast.makeText(Register.this, "Successfully registered", Toast.LENGTH_LONG).show();

                    String gender = "male";
                    if(genderOptions.getCheckedRadioButtonId()==R.id.female)
                        gender = "female";
                    else if(genderOptions.getCheckedRadioButtonId()==R.id.other)
                        gender = "other";

                    account = new UserAccount(DBConn.firebaseAuth.getCurrentUser().getUid(), fname.getText().toString(), lname.getText().toString(), gender, email.getText().toString(), phone.getText().toString());
                    DBConn.pushToDB(account, "userAccounts");

                    Intent intent = new Intent();
                    intent.putExtra("email", email.getText().toString());
                    intent.putExtra("pass", password.getText().toString());
                    setResult(1, intent);
                    finish();
                }
                else
                {
                    Toast.makeText(Register.this, "Registration Error", Toast.LENGTH_LONG).show();
                }
            }
        });
        progressBar.setVisibility(View.INVISIBLE);
    }

    /**
     * back to MainActivity
     * @param view
     */
    public void backToMain(View view)
    {
        finish();
    }
}