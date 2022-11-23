package com.bagrut.find;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;


public class CompanyActivity extends AppCompatActivity
{
    /**
     * company data
     */
    public static Company company;
    /**
     * products that belong to company
     */
    public static ArrayList<Product> companyProducts = new ArrayList<>();
    /**
     * the shares that user have in company
     */
    public static Share accountShare;
    /**
     * company name
     */
    private TextView companyName;
    /**
     * displays ownership percentage in company
     */
    private TextView ownership;
    /**
     * displays role of user in company
     */
    private TextView role;
    /**
     * displays daily profit\loss
     */
    private TextView dailyPnL;
    /**
     * shows loading bar
     */
    private ProgressBar loading;
    /**
     * tab adapter for company tabs
     */
    private CompanyTabAdapter tabAdapter;
    /**
     * displays tabs
     */
    private TabLayout tabLayout;
    /**
     * displays content of tabs
     */
    private ViewPager2 viewPager;
    /**
     * initializes activity
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_company);

        //initialization
        companyName = findViewById(R.id.companyName);
        ownership = findViewById(R.id.ownership);
        role = findViewById(R.id.role);
        dailyPnL = findViewById(R.id.dailyPnL);
        loading = findViewById(R.id.loading);
        viewPager = findViewById(R.id.company_tabs_pager);
        tabLayout = findViewById(R.id.tabLayout);
        tabAdapter = new CompanyTabAdapter(this.getSupportFragmentManager(), getLifecycle());
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

        //retrieve company data by companyId
        Intent intent = getIntent();
        companyProducts = new ArrayList<>();
        getCompanyListener listener =  new getCompanyListener()
        {
            @Override
            public void onCompanyRetrieved(Company company1)
            {

            }

            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onCompanyRetrievedAndProducts(Company company1, ArrayList<Product> companyProductss)
            {
                loading.setVisibility(View.INVISIBLE);
                company = company1;

                for(Share share:company.getOwners())
                {
                    if(share.getOwnerID().equals(DBConn.account.accountId))
                    {
                        accountShare = share;
                        break;
                    }
                }
                companyName.setText(company.getName());
                ownership.setText((Math.round(((double)accountShare.getSharesAmount()/company.getTotalShares()) * (double)100))+"%");
                role.setText(accountShare.getRole());

                double shareDailyProfit = Math.round(((double)accountShare.getSharesAmount()/company.getTotalShares())*company.getDailyPnL());
                if(shareDailyProfit>0)
                    dailyPnL.setText("+"+shareDailyProfit+"$");
                else if(shareDailyProfit<0)
                    dailyPnL.setText("-"+shareDailyProfit+"$");
                else
                    dailyPnL.setText(shareDailyProfit+"$");

                DBConn.removeOnCompanyListener(this);
                company.resetDailyData(companyProducts);
                updateDailyData();
            }
        };
        DBConn.setOnCompaniesListener(listener);


        DBConn.startCompanyUpdateById(intent.getExtras().get("companyId").toString(), companyProducts, new getCompanyListener()
        {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onCompanyRetrieved(Company company1)
            {

            }

            @Override
            public void onCompanyRetrievedAndProducts(Company company, ArrayList<Product> companyProductss)
            {

            }
        });

    }

    /**
     * updates daily data of company
     */
    public void updateDailyData()
    {
        company.setDailyPnL(0);
        company.setDailyViews(0);
        company.setDailySales(0);
        for(Product p:companyProducts)
        {
            company.setDailyPnL(company.getDailyPnL()+p.getDailyPnL());
            company.setDailySales(company.getDailySales()+p.getDailySales());
            company.setDailyViews(company.getDailyViews()+p.getDailyViews());
        }
        DBConn.updateCompanyToDB(company);



    }


}