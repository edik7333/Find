package com.bagrut.find;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

/**
 * class for settings activity
 */
public class Settings extends AppCompatActivity
{
    /**
     * saving settings
     */
    SharedPreferences settingsSaved;
    /**
     * clears login details
     */
    Button clearData;
    /**
     * toggles to exit account when screen is off
     */
    Switch exitWhenClosed;

    /**
     * initialization
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        settingsSaved = this.getSharedPreferences("settings", MODE_PRIVATE);
        clearData = findViewById(R.id.clear_data);
        exitWhenClosed = findViewById(R.id.exit_when_off);
        exitWhenClosed.setChecked(settingsSaved.getBoolean("exitWhenClosed", true));

        clearData.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                SharedPreferences sp = getSharedPreferences("savedUser", MODE_PRIVATE);
                sp.edit().clear().commit();
                Toast.makeText(view.getContext(), "account data cleared", Toast.LENGTH_LONG).show();
                toMain(null);
            }
        });

        exitWhenClosed.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b)
            {
                settingsSaved.edit().putBoolean("exitWhenClosed", b).commit();
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