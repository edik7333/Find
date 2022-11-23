package com.bagrut.find;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.biometrics.BiometricManager;
import android.hardware.biometrics.BiometricPrompt;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Timer;
import java.util.TimerTask;

/**
 * a class for the login page activity(the first activity)
 */
public class MainActivity extends AppCompatActivity
{

    /**
     * exit app dialog builder
     */
    private AlertDialog.Builder builder;
    /**
     * shared preference to save user login details
     */
    private SharedPreferences sp;

    /**
     * save account switch
     */
    private Switch saveAccount;
    /**
     * write email to login
     */
    private EditText phoneOrEmail;
    /**
     * write password to login
     */
    private EditText pass;
    /**
     * to show loading
     */
    private ProgressBar progressBar;
    /**
     * show that user logged in
     */
    public static TextView loggedInCheck;
    /**
     * login to account
     */
    public static Button loginButton;
    /**
     * timeout if server is unreachable
     */
    public Timer loginTimeout = new Timer();
    /**
     * user data request task
     */
    public Task userDataRequest;
    /**
     * notifies user if login problem
     */
    private TextView warning;


    /**
     * initializes
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //initialization
        warning = findViewById(R.id.warning);
        warning.setVisibility(View.INVISIBLE);
        saveAccount = findViewById(R.id.saveAccount);
        sp = getSharedPreferences("savedUser", MODE_PRIVATE);
        phoneOrEmail = findViewById(R.id.userIdLogin);
        pass = findViewById(R.id.passwordLogin);
        progressBar = findViewById(R.id.loginProgress);
        progressBar.setVisibility(View.INVISIBLE);
        loginButton = findViewById(R.id.loginButton_at_login);
        loggedInCheck = findViewById(R.id.loggedIn);
        loggedInCheck.setVisibility(View.INVISIBLE);

        //sets saved account in login fields
        if(sp.contains("emailOrPhone")&&sp.contains("pass"))
        {
            phoneOrEmail.setText(sp.getString("emailOrPhone",""));
            saveAccount.setChecked(sp.getBoolean("isSave", false));
        }

        // changes phoneOrEmail field length when focused for aesthetics
        phoneOrEmail.setOnFocusChangeListener(new View.OnFocusChangeListener()
        {
            @Override
            public void onFocusChange(View view, boolean b)
            {
                if(b)
                {
                    ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
                    layoutParams.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 280, getResources().getDisplayMetrics());
                    view.setLayoutParams(layoutParams);
                }
                else
                {
                    ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
                    layoutParams.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 270, getResources().getDisplayMetrics());
                    view.setLayoutParams(layoutParams);
                }
            }
        });

        // changes password field length when focused for aesthetics
        pass.setOnFocusChangeListener(new View.OnFocusChangeListener()
        {
            @Override
            public void onFocusChange(View view, boolean b)
            {
                if(b)
                {
                    ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
                    layoutParams.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 280, getResources().getDisplayMetrics());
                    view.setLayoutParams(layoutParams);
                }
                else
                {
                    ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
                    layoutParams.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 270, getResources().getDisplayMetrics());
                    view.setLayoutParams(layoutParams);
                }
            }
        });

        //automatically deletes saved login details when save switch is turned off
        saveAccount.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b)
            {
                if(!b)
                {
                    SharedPreferences.Editor editor = sp.edit();
                    editor.remove("emailOrPhone");
                    editor.remove("isSave");
                    editor.apply();

                }
            }
        });

        //creates alert dialog for exiting the app
        builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to exit the application?");
        builder.setTitle("Exit application");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                finish();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                dialogInterface.cancel();
            }
        });
    }

    /**
     * creates exit app dialog
     */
    @Override
    public void onBackPressed()
    {
        builder.show();
    }

    /**
     * goes to register activity
     * @param view
     */
    public void toRegister(View view)
    {
        Intent intent = new Intent(this, Register.class);
        startActivityForResult(intent, 1);
    }

    /**
     * goes to about activity
     * @param view
     */
    public void toContactDev(View view)
    {
        Intent intent = new Intent(this, About.class);
        startActivity(intent);
    }

    /**
     * goes to reset pass activity
     * @param view
     */
    public void toReset(View view)
    {
        Intent intent =  new Intent(this, ResetPass.class);
        startActivity(intent);
    }

    /**
     * goes to settings activity
     * @param view
     */
    public void toSettings(View view)
    {
        Intent intent = new Intent(this, Settings.class);
        startActivity(intent);
    }

    /**
     * login into account
     * @param view
     */
    public void login(View view)
    {
        loginButton.setClickable(false);
        warning.setVisibility(View.INVISIBLE);

        //stops function if at least one of the fields is empty
        if(phoneOrEmail.getText().toString().isEmpty()||pass.getText().toString().isEmpty())
        {
            loginButton.setClickable(true);
            return;
        }


        //saves login details if save switch is checked
        if(saveAccount.isChecked())
        {
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("emailOrPhone", phoneOrEmail.getText().toString());
            editor.putBoolean("isSave", true);
            editor.apply();
        }
        else
        {
            SharedPreferences.Editor editor = sp.edit();
            editor.remove("emailOrPhone");
            editor.remove("isSave");
            editor.apply();
        }

        progressBar.setVisibility(View.VISIBLE);

        DBConn.firebaseAuth.signInWithEmailAndPassword(phoneOrEmail.getText().toString(), pass.getText().toString()).addOnCompleteListener(this, new OnCompleteListener<AuthResult>()
        {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task)
            {
                if(task.isSuccessful())
                {
                    loginTimeout.cancel();
                    DBConn.setOnAccountUpdateListener(new AccountUpdateListener()
                    {
                        @Override
                        public void onAccountUpdate(UserAccount userAccount)
                        {
                            loginTimeout.cancel();
                            if(userAccount!=null)
                            {
                                loggedInCheck.setVisibility(View.VISIBLE);
                                registerReceiver(DBConn.noSignalReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
                                DBConn.removeOnAccountUpdateListener(this);
                                toLists();
                            }
                            progressBar.setVisibility(View.INVISIBLE);
                        }
                    });

                    userDataRequest = DBConn.firstAccountUpdate();
                }
                else
                {
                    Log.w("login", task.getException());
                    showFailedLogin(task.getException());
                }

            }
        });

    }


    /**
     *  goes to main account activity
     */
    public void toLists()
    {
        Intent intent = new Intent(this, AccountMainView.class);
        startActivity(intent);
    }

    /**
     * notifies user that login details are incorrect
     */
    public void showFailedLogin(Throwable throwable)
    {
        String toastMsg = "";
        if(throwable instanceof FirebaseException)
            toastMsg = "no connection. try checking network";

        if(!toastMsg.isEmpty())
        {
            Toast.makeText(this, toastMsg, Toast.LENGTH_LONG).show();
            warning.setText(toastMsg);
            warning.setVisibility(View.VISIBLE);
        }
        progressBar.setVisibility(View.INVISIBLE);
        loginButton.setClickable(true);
        userDataRequest = null;
    }

    /**
     * gets result from other activities
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==1&&resultCode==1)
        {
            phoneOrEmail.setText(data.getStringExtra("email"));
            pass.setText(data.getStringExtra("pass"));
            saveAccount.setChecked(true);
        }
    }
}