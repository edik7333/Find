package com.bagrut.find;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class ResetPass extends AppCompatActivity
{
    /**
     * enter email
     */
    EditText email;
    /**
     * reset a password
     */
    Button resetPass;
    /**
     * cancel reset pass
     */
    Button cancel;

    /**
     * initialization
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_pass);

        email = findViewById(R.id.emailReset);
        resetPass = findViewById(R.id.sendReset);
        cancel = findViewById(R.id.cancel_reset);

        cancel.setVisibility(View.INVISIBLE);
        resetPass.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(!((ResetPass)view.getContext()).resetPass.getText().toString().equalsIgnoreCase("reset pass"))
                {
                    email.setVisibility(View.INVISIBLE);
                    cancel.setVisibility(View.VISIBLE);
                    resetPass.setVisibility(View.INVISIBLE);

                    if(!email.getText().toString().isEmpty())
                    {
                        DBConn.resetPass(email.getText().toString(), new getPassResetConf()
                        {
                            @Override
                            public void onPasswordReset()
                            {
                                Toast.makeText(email.getContext(), "password reset, check email!", Toast.LENGTH_LONG).show();
                                toMain(null);
                            }
                        });
                    }

                }
                else
                {

                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
            email.setVisibility(View.VISIBLE);
            cancel.setVisibility(View.INVISIBLE);
            resetPass.setVisibility(View.VISIBLE);

            }
        });
    }

    /**
     * back to MainActivity
     * @param view
     */
    public void toMain(View view)
    {
        finish();
    }
}