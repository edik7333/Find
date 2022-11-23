package com.bagrut.find;

import android.net.ConnectivityManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;

import java.util.Calendar;

/**
 * class for activity_account activity
 */
public class AccountActivity extends Fragment
{

    /**
     * logout button
     */
    private Button logout;
    /**
     * greeting text
     */
    private TextView greeting;
    /**
     * tab adapter
     */
    private AccountTabAdapter tabAdapter;
    /**
     * displays tabs
     */
    private ViewPager2 viewPager;
    /**
     * tab layout
     */
    private TabLayout tabLayout;

    /**
     * displays amount of items bought
     */
    private TextView itemsBought;
    /**
     * displays current balance in account
     */
    private TextView balance;
    /**
     * displays amount of companies owned by account
     */
    private TextView companies;
    /**
     * loading bar when data loading
     */
    private ProgressBar loading;
    /**
     * displays no connection text when network status is offline
     */
    public static TextView noConnText;
    /**
     * displays no connection icon when network status is offline
     */
    public static TextView noConnIcon;
    /**
     * instance of interface that receives account updates from firebase
     */
    private AccountUpdateListener updateListener;

    /**
     * constructor
     */
    public AccountActivity()
    {
        // Required empty public constructor
    }

    /**
     * on create fragment
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

    }


    /**
     * on create view
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.activity_account, container, false);
    }

    /**
     * initializes activity
     * @param view
     * @param savedInstanceState
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        //initialization
        logout = getView().findViewById(R.id.logoutButton);
        balance = getView().findViewById(R.id.balance);
        greeting = getView().findViewById(R.id.greeting);
        itemsBought = getView().findViewById(R.id.items_bought);
        companies = getView().findViewById(R.id.companies);
        loading = getView().findViewById(R.id.loading);
        noConnIcon = getView().findViewById(R.id.no_conn_icon);
        noConnText = getView().findViewById(R.id.no_conn_text);
        noConnIcon.setVisibility(View.GONE);
        noConnText.setVisibility(View.GONE);


        updateListener = new AccountUpdateListener()
        {
            @Override
            public void onAccountUpdate(UserAccount userAccount)
            {
                updateData();
            }
        };
        if(AccountMainView.firstTime)
        {

            AccountMainView.firstTime = false;
        }
        else
        {
            updateData();
        }

        DBConn.setOnAccountUpdateListener(updateListener);

        //tab views initialization
        viewPager = view.findViewById(R.id.pager);
        tabLayout = view.findViewById(R.id.tabs);
        tabAdapter = new AccountTabAdapter(this);
        viewPager.setAdapter(tabAdapter);

        //syncs tabView with viewPager on page change
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener()
        {
            @Override
            public void onTabSelected(TabLayout.Tab tab)
            {
                viewPager.setCurrentItem(tab.getPosition(), true);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab)
            {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab)
            {

            }
        });
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback()
        {
            @Override
            public void onPageSelected(int position)
            {
                tabLayout.setScrollPosition(position, 0f, true);
            }

        });

        //logout on click
        logout.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                logout();
            }
        });
        super.onViewCreated(view, savedInstanceState);
    }

    /**
     * updates account data to views
     */
    public void updateData()
    {
        int currentHour = Calendar.getInstance().getTime().getHours();
        loading.setVisibility(View.GONE);
        if(currentHour>=5&&currentHour<12)
        {
            greeting.setText("Good Morning, "+DBConn.account.getFname());
        }
        else if(currentHour>=12&&currentHour<18)
        {
            greeting.setText("Good Afternoon, "+DBConn.account.getFname());
        }
        else
        {
            greeting.setText("Good Evening, "+DBConn.account.getFname());
        }
        balance.setText(Double.toString(DBConn.account.balance)+"$");
        itemsBought.setText(Integer.toString(DBConn.tempOrderedProducts.size()));
        companies.setText(Integer.toString(DBConn.account.getCompaniesKeys().size()));
    }

    /**
     * logout from account
     */
    private void logout()
    {
        ((AccountMainView)getActivity()).logout();
    }

}