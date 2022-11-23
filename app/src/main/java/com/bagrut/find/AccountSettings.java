package com.bagrut.find;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.Timer;
import java.util.TimerTask;

/**
 * class for accountSettings fragment
 */
public class AccountSettings extends Fragment
{
    /**
     * button that delets account from firebase
     */
    private Button deleteAccountButton;
    /**
     * button displays personal info of account
     */
    private Button showAccountButton;
    /**
     * shows a transferFundsButton
     */
    private Button showFundsButton;
    /**
     * displays a dialog to transfer funds to account
     */
    private Button transferFundsButton;
    /**
     * updates changed personal account info to firebase
     */
    private Button updateAccountButton;
    /**
     * "container" to show account personal info
     */
    private ConstraintLayout accountDetails;
    /**
     * "container" to show transfer funds button
     */
    private ConstraintLayout funds;
    /**
     * first name field
     */
    private EditText fname;
    /**
     * last name field
     */
    private EditText lname;
    /**
     * gender radio buttons
     */
    private RadioGroup gender;
    /**
     * email field
     */
    private EditText email;
    /**
     * phone field
     */
    private EditText phone;
    /**
     * loading progress for account info update
     */
    private ProgressBar updateAccountProgress;
    /**
     * handles message when funds transactions is complete
     */
    private Handler handler;

    /**
     * constructor
     */
    public AccountSettings()
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
        return inflater.inflate(R.layout.fragment_account_settings, container, false);
    }

    /**
     * initializes fragment views
     * @param view
     * @param savedInstanceState
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        //initialization
        deleteAccountButton = view.findViewById(R.id.delete_account);
        showAccountButton = view.findViewById(R.id.show_account);
        showFundsButton = view.findViewById(R.id.show_funds);
        transferFundsButton = view.findViewById(R.id.transfer_funds_button);
        updateAccountButton = view.findViewById(R.id.update_account_button);
        accountDetails = view.findViewById(R.id.account_details);
        funds = view.findViewById(R.id.funds_details);
        fname = view.findViewById(R.id.fname);
        lname = view.findViewById(R.id.lname);
        gender = view.findViewById(R.id.gender);
        email = view.findViewById(R.id.email);
        phone = view.findViewById(R.id.phone);
        updateAccountProgress = view.findViewById(R.id.update_account_progress);
        updateAccountProgress.setVisibility(View.INVISIBLE);

        //update data
        updateAccount();
        DBConn.setOnAccountUpdateListener(new AccountUpdateListener()
        {
            @Override
            public void onAccountUpdate(UserAccount userAccount)
            {
                updateAccount();
            }
        });

        updateAccountButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(fname.getText().toString().isEmpty()||lname.getText().toString().isEmpty()||email.getText().toString().isEmpty()||phone.getText().toString().isEmpty())
                    return;

                DBConn.account.setFname(fname.getText().toString());
                DBConn.account.setLname(lname.getText().toString());
                DBConn.account.setEmail(email.getText().toString());
                DBConn.account.setPhone(phone.getText().toString());

                view.setClickable(false);
                updateAccountProgress.setVisibility(View.VISIBLE);

                switch (gender.getCheckedRadioButtonId())
                {
                    case R.id.male:
                        DBConn.account.setGender("male");
                        break;
                    case R.id.female:
                        DBConn.account.setGender("female");
                        break;
                    case R.id.other:
                        DBConn.account.setGender("other");
                        break;
                }

                DBConn.accountsRef.setValue(DBConn.account).addOnCompleteListener(new OnCompleteListener<Void>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if(task.isSuccessful())
                        {
                            Toast.makeText(getContext(), "account updated", Toast.LENGTH_LONG).show();
                        }
                        else
                        {
                            Toast.makeText(getContext(), "couldn't update account", Toast.LENGTH_LONG).show();
                        }
                        updateAccountButton.setClickable(true);
                        updateAccountProgress.setVisibility(View.INVISIBLE);
                    }
                });
            }
        });

        //creates transfer fund dialog on click
        transferFundsButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                final Dialog d = new Dialog(getContext());
                d.setContentView(R.layout.transfer_funds_dialog);
                d.setTitle("transfer funds");
                d.setCancelable(true);

                final EditText transactionAmount = d.findViewById(R.id.transaction_amount);
                Button submit = d.findViewById(R.id.submit);
                final ProgressBar loading = d.findViewById(R.id.loading);
                loading.setVisibility(View.INVISIBLE);
                submit.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        loading.setVisibility(View.VISIBLE);
                        new Timer().schedule(new TimerTask()
                        {
                            @Override
                            public void run()
                            {
                                DBConn.account.balance+=Double.parseDouble(transactionAmount.getText().toString());
                                DBConn.updateAccountToDB();
                                Message message = new Message();
                                Bundle bundle = new Bundle();
                                bundle.putString("amount", transactionAmount.getText().toString());
                                message.setData(bundle);
                                handler.sendMessage(message);
                                d.cancel();
                            }
                        }, 5000);

                    }
                });
                d.show();
            }
        });

        accountDetails.setVisibility(View.GONE);
        funds.setVisibility(View.GONE);

        final AlertDialog.Builder builder = new AlertDialog.Builder(this.getParentFragment().getContext());

        //delete account
        deleteAccountButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(final View view)
            {
                //creates alert dialog for exiting the app
                builder.setMessage("Are you sure you want to delete your account?");
                builder.setTitle("Delete account");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i)
                    {
                        view.setClickable(false);
                        DBConn.firebaseAuth.getCurrentUser().delete();
                        DBConn.logout();
                        DBConn.accountSnapshot.getRef().removeValue();
                        Toast.makeText(((Activity)view.getContext()), "account deleted", Toast.LENGTH_LONG).show();
                        ((Activity)view.getContext()).finish();
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
        });


        showAccountButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(accountDetails.getVisibility()==View.GONE)
                {
                    accountDetails.setVisibility(View.VISIBLE);
                }
                else
                {
                    accountDetails.setVisibility(View.GONE);
                }
            }
        });

        showFundsButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(funds.getVisibility()==View.GONE)
                {
                    funds.setVisibility(View.VISIBLE);
                }
                else
                {
                    funds.setVisibility(View.GONE);
                }
            }
        });

        handler = new Handler(Looper.getMainLooper())
        {
            @Override
            public void handleMessage(@NonNull Message msg)
            {
                super.handleMessage(msg);
                Toast.makeText(getContext(), "transaction of "+msg.getData().get("amount")+"$ complete", Toast.LENGTH_LONG).show();
            }
        };
    }

    /**
     * updates account data to views
     */
    private void updateAccount()
    {
        try
        {
            fname.setText(DBConn.account.fname);
            lname.setText(DBConn.account.lname);
            switch (DBConn.account.gender)
            {
                case "male":
                    gender.check(R.id.male);
                    break;
                case "female":
                    gender.check(R.id.female);
                    break;
                case "other":
                    gender.check(R.id.other);
                    break;
            }
            email.setText(DBConn.account.email);
            phone.setText(DBConn.account.phone);
        }
        catch (Exception ignored)
        {

        }
    }
}