package com.bagrut.find;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class CompanyInfoTab extends Fragment
{
    /**
     * displays daily profit\loss
     */
    TextView dailyPnL;
    /**
     * displays daily sales
     */
    TextView dailySales;
    /**
     * displays daily views
     */
    TextView dailyViews;
    /**
     * displays daily personal profit\loss
     */
    TextView dailyPersonalPnL;
    /**
     * displays payout ratio
     */
    TextView payoutRatio;
    /**
     * displays payment date
     */
    TextView paymentDate;
    /**
     * displays next payment
     */
    TextView savingsNextPayment;
    /**
     * displays company worth
     */
    TextView companyWorth;

    /**
     * constructor
     */
    public CompanyInfoTab()
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
        return inflater.inflate(R.layout.fragment_company_info_tab, container, false);
    }

    /**
     * initialize fragment views
     * @param view
     * @param savedInstanceState
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        dailyPnL = view.findViewById(R.id.daily_pnl);
        dailySales = view.findViewById(R.id.daily_sales);
        dailyViews = view.findViewById(R.id.daily_views);
        dailyPersonalPnL = view.findViewById(R.id.daily_personal_pnl);
        payoutRatio = view.findViewById(R.id.payout_ratio);
        paymentDate = view.findViewById(R.id.payment_date);
        savingsNextPayment = view.findViewById(R.id.savings_next_payment);
        companyWorth = view.findViewById(R.id.company_worth);

        DBConn.setOnCompaniesListener(new getCompanyListener()
        {
            @Override
            public void onCompanyRetrieved(Company company)
            {
                updateData(company);
            }

            @Override
            public void onCompanyRetrievedAndProducts(Company company, ArrayList<Product> companyProductss)
            {

            }
        });

    }

    /**
     * updates views on data update
     * @param company
     */
    private void updateData(Company company)
    {
        dailyPnL.setText(company.getDailyPnL()+"");
        dailySales.setText(company.getDailySales()+"");
        dailyViews.setText(company.getDailyViews()+"");
        dailyPersonalPnL.setText(company.getDailyPnL()+"");
        payoutRatio.setText(company.getPayoutRatio()+"");
        paymentDate.setText(company.getLastPaymentDate()+"");
        savingsNextPayment.setText(company.getDailyPnL()+"");
        companyWorth.setText(company.getBudget() +"$");
    }
}