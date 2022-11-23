package com.bagrut.find;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;

/**
 * class for account overview tab
 */
public class AccountOverview extends Fragment
{
    /**
     * a recycler view that displays companies owned by user
     */
    private RecyclerView recyclerView;
    /**
     * displays or hides recycler view of companies
     */
    private Button ownedCompButton;
    /**
     * displays dialog for adding a company to account
     */
    private Button addCompanyButton;
    /**
     * displays amount of companies account owns
     */
    private TextView companyCount;
    /**
     * displays amount of money invested
     */
    private TextView investedMoney;
    /**
     * displays daily profit and loss
     */
    private TextView lastDailyPnL;
    /**
     * displays monthly profit and loss
     */
    private TextView lastMonthPnL;
    /**
     * "container" for account finance info
     */
    private ConstraintLayout accountFinance;
    /**
     * "container" for account owned companies info
     */
    private ConstraintLayout companiesOwnedInfo;
    /**
     * scroll view in case the phone screen is too small to present all info
     */
    private ScrollView scrollAccountOverview;
    /**
     * disables scroll when companies list is shown
     */
    private boolean enableScroll = false;

    /**
     * constructor
     */
    public AccountOverview()
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
        return inflater.inflate(R.layout.fragment_account_overview, container, false);
    }

    /**
     * initializes fragment views
     * @param view
     * @param savedInstanceState
     */
    @SuppressLint("ClickableViewAccessibility")
    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        //initialization
        ownedCompButton = view.findViewById(R.id.ownedCompButton);
        accountFinance = view.findViewById(R.id.account_finance);
        companiesOwnedInfo = view.findViewById(R.id.companies_owned_info);
        scrollAccountOverview = view.findViewById(R.id.scroll_account_overview);
        addCompanyButton = view.findViewById(R.id.add_company);
        companyCount = view.findViewById(R.id.companies_count);
        investedMoney = view.findViewById(R.id.invested_money);
        lastDailyPnL = view.findViewById(R.id.last_daily);
        lastMonthPnL = view.findViewById(R.id.last_month_pnl);
        recyclerView = CentralAdapter.createRecycleView((RecyclerView) view.findViewById(R.id.ownedCompList), getContext(), DBConn.tempCompanies, CentralAdapter.OWNED_COMPANY_LIST);
        recyclerView.setVisibility(View.GONE);

        DBConn.setOnAccountUpdateListener(new AccountUpdateListener()
        {
            @Override
            public void onAccountUpdate(UserAccount userAccount)
            {
                updateData();
            }
        });

        //on add company button listener (creates dialog)
        addCompanyButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                createNewCompanyDialog();
            }
        });


        //disables scroll in scrollView when company list is opened
        scrollAccountOverview.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent)
            {
                return enableScroll;
            }
        });

        //company list button listener
        ownedCompButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(recyclerView.getVisibility()==View.GONE)
                {
                    accountFinance.setVisibility(View.GONE);
                    companiesOwnedInfo.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    enableScroll = true;
                }
                else if(recyclerView.getVisibility()==View.VISIBLE)
                {
                    accountFinance.setVisibility(View.VISIBLE);
                    companiesOwnedInfo.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                    enableScroll = false;
                }
            }
        });

        //updates company list when data changes in firebase
        DBConn.registerdCompanyListListener = new ownedCompanyListListener()
        {
            @Override
            public void onOwnedCompanyListRetrieved()
            {
                (recyclerView.getAdapter()).notifyItemRangeChanged(0, DBConn.tempCompanies.size());
            }
        };

    }

    /**
     * creates new company dialog to present options for adding company to account
     */
    private void createNewCompanyDialog()
    {
        //build dialog
        final Dialog d = new Dialog(getContext());
        d.setContentView(R.layout.add_company_dialog);
        d.setTitle("add company");
        d.setCancelable(true);

        //initialize in dialog
        Button cancelButton = d.findViewById(R.id.cancel);
        Button stockListButton = d.findViewById(R.id.toShareList);
        Button startCompany = d.findViewById(R.id.startCompany);
        final EditText companyName = d.findViewById(R.id.companyName);
        final EditText description = d.findViewById(R.id.description);
        final EditText budgetAllocation = d.findViewById(R.id.budgetAllocation);
        TextView accountBudget = d.findViewById(R.id.accountBudget);

        accountBudget.setText(Double.toString(DBConn.account.balance));

        startCompany.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {

                if(companyName.getText().toString().isEmpty()||description.getText().toString().isEmpty()||budgetAllocation.getText().toString().isEmpty())
                {
                    Toast.makeText(view.getContext(), "empty fields", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(Double.parseDouble(budgetAllocation.getText().toString())>DBConn.account.getBalance())
                {
                    Toast.makeText(view.getContext(), "not enough funds", Toast.LENGTH_SHORT).show();
                    return;
                }

                Company company = new Company(companyName.getText().toString(), description.getText().toString(), Double.parseDouble(budgetAllocation.getText().toString()));

                DatabaseReference companyRef = DBConn.pushToDB(company, "companies");
                ArrayList<Share> shares = new ArrayList<>();
                shares.add(new Share(DBConn.account.accountId, 1, 0, companyRef.getKey()));
                company.setOwners(shares);
                DBConn.account.balance -= Double.parseDouble(budgetAllocation.getText().toString());
                DBConn.updateCompanyToDB(company);
                DBConn.account.getCompaniesKeys().add(companyRef.getKey());
                DBConn.updateAccountToDB();
                d.cancel();
            }
        });

        stockListButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(view.getContext(), StockList.class);
                startActivity(intent);
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                d.cancel();
            }
        });

        //set size of dialog
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(d.getWindow().getAttributes());
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        d.getWindow().setAttributes(layoutParams);

        d.show();
    }

    /**
     * updates account data on views
     */
    private void updateData()
    {
        companyCount.setText(Integer.toString(DBConn.account.getCompaniesKeys().size()));
        investedMoney.setText(Double.toString(DBConn.account.investedMoney));
        lastDailyPnL.setText(Double.toString(DBConn.account.lastDailyPnL));
        lastMonthPnL.setText(Double.toString(DBConn.account.lastMonthPnL));
    }

}
