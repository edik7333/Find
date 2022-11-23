package com.bagrut.find;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

/**
 * about activity class
 */
public class About extends AppCompatActivity
{

    /**
     * initializes the Activity
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        //initialization
        Button email1 = findViewById(R.id.email1);
        Button email2 = findViewById(R.id.email2);
        Button call = findViewById(R.id.call);
        Button copyPhone = findViewById(R.id.copy_phone);
        Button copyEmail1 = findViewById(R.id.copy_email1);
        Button copyEmail2 = findViewById(R.id.copy_email2);
        Button goBack = findViewById(R.id.contact_us_to_login);

        //go back listener
        goBack.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                finish();
            }
        });

        //intent to email1 on click
        email1.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:bug-report@Find.com"));
                try
                {
                    startActivity(intent);
                }catch (android.content.ActivityNotFoundException ex)
                {
                    Toast.makeText(About.this,"no email application was found", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //copy email1 to clipboard on click
        copyEmail1.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("email", "bug-report@Find.com");
                clipboard.setPrimaryClip(clip);

                Toast.makeText(view.getContext(), "copied to clipboard", Toast.LENGTH_SHORT).show();
            }
        });

        //intent to email2 on click
        email2.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:business@Find.com"));
                try
                {
                    startActivity(intent);
                }catch (android.content.ActivityNotFoundException ex)
                {
                    Toast.makeText(About.this,"no email application was found", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //copy email2 to clipboard on click
        copyEmail2.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("email", "business@Find.com");
                clipboard.setPrimaryClip(clip);

                Toast.makeText(view.getContext(), "copied to clipboard", Toast.LENGTH_SHORT).show();
            }
        });

        //call dial number on click
        call.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:+4546316368"));

                try
                {
                    startActivity(intent);
                }catch (Exception ex)
                {
                    Toast.makeText(About.this,"no phone app was found",Toast.LENGTH_LONG).show();
                }

            }
        });

        //copy phone number on click
        copyPhone.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("phone", "+4546316368");
                clipboard.setPrimaryClip(clip);

                Toast.makeText(view.getContext(), "copied to clipboard", Toast.LENGTH_SHORT).show();
            }
        });
    }

}