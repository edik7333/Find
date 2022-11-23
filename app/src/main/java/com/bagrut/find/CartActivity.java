package com.bagrut.find;

import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;

public class CartActivity extends Fragment
{
    /**
     * progress bar for loading in cart products
     */
    private ProgressBar loading;
    /**
     * presents all items in cart
     */
    private RecyclerView cartList;
    /**
     * displays amount of products in cart
     */
    private TextView amount;
    /**
     * displays the total sum to pay for all products in cart
     */
    private TextView totalPrice;
    /**
     * buys all products in cart
     */
    private Button buyAll;
    /**
     * removes all products in cart
     */
    private Button emptyCart;
    /**
     * stores the sum to pay(for calculations)
     */
    private double totalPriceD;

    /**
     * constructor
     */
    public CartActivity()
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
        return inflater.inflate(R.layout.activity_cart, container, false);
    }

    /**
     * initializes views in fragment
     * @param view
     * @param savedInstanceState
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        //initialization
        loading = view.findViewById(R.id.loading);
        cartList = view.findViewById(R.id.cart_list);
        amount = view.findViewById(R.id.item_count);
        totalPrice = view.findViewById(R.id.cost);
        buyAll = view.findViewById(R.id.buy_all);
        emptyCart = view.findViewById(R.id.empty_cart);

        CentralAdapter.createRecycleView(cartList, getContext(), DBConn.account.getInCart(), CentralAdapter.CART_LIST);
        updateData();
        loading.setVisibility(View.GONE);
        DBConn.setOnAccountUpdateListener(new AccountUpdateListener()
        {
            @Override
            public void onAccountUpdate(UserAccount userAccount)
            {
                updateData();
            }
        });

        buyAll.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(final View view)
            {
                if(totalPriceD>DBConn.account.getBalance())
                    return;
                for(final ProductSale ps:DBConn.account.getInCart())
                {
                    DBConn.requestProductById(ps.productID, new getProductListener()
                    {
                        @Override
                        public void onProductRetrieved(ArrayList<Product> products)
                        {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                            {
                                if(products.get(0)!=null)
                                {
                                    switch (products.get(0).sell(ps.amount))
                                    {
                                        case "success":
                                            Toast.makeText(view.getContext(), "transaction complete", Toast.LENGTH_LONG).show();
                                            DBConn.account.getInCart().remove(ps);
                                            DBConn.updateAccountToDB();
                                            break;
                                        case "noFunds":
                                            Toast.makeText(view.getContext(), "not enough funds in account", Toast.LENGTH_LONG).show();
                                            break;
                                        case "noStock":
                                            Toast.makeText(view.getContext(), "not enough product in stock", Toast.LENGTH_LONG).show();
                                    }
                                }
                            }
                        }
                    });
                }
                DBConn.account.getInCart().clear();
                DBConn.updateAccountToDB();
            }
        });

        emptyCart.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                DBConn.account.getInCart().clear();
                cartList.getAdapter().notifyDataSetChanged();
                DBConn.updateAccountToDB();
            }
        });
    }

    /**
     * updates account data to views
     */
    public void updateData()
    {
        cartList.getAdapter().notifyDataSetChanged();
        amount.setText(Integer.toString(DBConn.account.getInCart().size()));
        double sum = 0;
        for(ProductSale ps:DBConn.account.getInCart())
        {
            sum+=ps.amount*ps.pricePerUnit;
        }
        totalPriceD = sum;
        totalPrice.setText(sum+"$");
    }
}