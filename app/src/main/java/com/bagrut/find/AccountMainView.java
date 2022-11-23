package com.bagrut.find;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;

/**
 * class for mainAccountView (handles bottom menu and fragments)
 */
public class AccountMainView extends AppCompatActivity
{
    /**
     * navigation view (menu)
     */
    BottomNavigationView navigationView;
    /**
     * navigation controller
     */
    NavController controller;
    /**
     * intent for lock screen service(a service that exits account when screen is at off status)
     */
    public static Intent lockScreenServiceIntent;
    /**
     * is it the first time in the account(used for initialization at AccountActivity)
     */
    public static boolean firstTime = true;
    /**
     * a broadcast receiver registered to lock screen service to receive info when logout of account
     */
    public LockScreenServiceReceiver screenServiceReceiver;

    /**
     * activity initialization
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_main_view);

        navigationView = findViewById(R.id.nav);
        controller = Navigation.findNavController(this, R.id.fragment);
        screenServiceReceiver = new LockScreenServiceReceiver(this);
        SharedPreferences sp = getSharedPreferences("settings", MODE_PRIVATE);
        if(sp.getBoolean("exitWhenClosed", true))
        {
            IntentFilter filter = new IntentFilter("com.example.Broadcast");
            registerReceiver(screenServiceReceiver, filter);
        }



        NavigationUI.setupWithNavController(navigationView, controller);
        DBConn.startAccountOnChangeUpdate();
        DBConn.requestProductSaleByClientId(DBConn.firebaseAuth.getUid(), new getProductSaleListener()
        {
            @Override
            public void onProductSaleRetrieved(ArrayList<ProductSale> productSales)
            {

            }
        });
        MainActivity.loginButton.setClickable(true);
        MainActivity.loggedInCheck.setVisibility(View.INVISIBLE);

        lockScreenServiceIntent = new Intent(this, LockScreenService.class);
        startService(lockScreenServiceIntent);

        DBConn.NoSignalReceiver.addConnStatusListener(new onConnStatusListener()
        {
            @Override
            public void onConnectionStatusChanged(ConnectivityManager connMgr)
            {
                DBConn.NoSignalReceiver.handleViewOnConnStatusChange((Activity)navigationView.getContext(), connMgr);
            }
        });
    }

    /**
     * creates a dialog that asks if the user sure he wants to exit application
     */
    @Override
    public void onBackPressed()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to exit the application?");
        builder.setTitle("Exit application");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                stopService(lockScreenServiceIntent);
                finishAffinity();
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

        builder.show();
    }

    /**
     * logs out from account
     */
    public void logout()
    {
        DBConn.removeAllOnAccountUpdateListeners();
        DBConn.logout();
        this.stopService(AccountMainView.lockScreenServiceIntent);
        firstTime = true;
        try
        {
            unregisterReceiver(screenServiceReceiver);
        }catch (IllegalArgumentException e)
        {
            Log.w("receiver", e);
        }

        finish();
    }

    /**
     * a broadcast receiver registered to lock screen service to receive info when logout of account
     */
    public static class LockScreenServiceReceiver extends BroadcastReceiver
    {
        /**
         * instance of AccountMainView
         */
        private AccountMainView accountMainView;

        /**
         * constructor
         */
        public LockScreenServiceReceiver()
        {
        }

        /**
         * constructor that gets an instance of AccountMainView
         * @param accountMainView
         */
        public LockScreenServiceReceiver(AccountMainView accountMainView)
        {
            this.accountMainView = accountMainView;
        }

        /**
         * receives info from lock screen service
         * @param context
         * @param intent
         */
        @Override
        public void onReceive(Context context, Intent intent)
        {
            if(intent.getBooleanExtra("lockScreen", false))
                accountMainView.logout();
        }
    }
}