package com.bagrut.find;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

public class ProductActivity extends AppCompatActivity
{
    /**
     * product reviews list
     */
    private RecyclerView reviews;
    /**
     * product data
     */
    public static  Product product;
    /**
     * displays price
     */
    private TextView price;
    /**
     * displays product name
     */
    private TextView prodName;
    /**
     * displays company name
     */
    private TextView compName;
    /**
     * displays description
     */
    private TextView description;
    /**
     * product image
     */
    private ImageView prodImage;
    /**
     * displays units in stock
     */
    private TextView inStock;
    /**
     * buys product
     */
    private Button buy;
    /**
     * display write review dialog
     */
    private Button comment;
    /**
     * store total price for transaction
     */
    private double totalPriceD = 0;
    /**
     * did the user visited the product already
     */
    private boolean firstVisit = true;

    /**
     * initialization
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product);

        //initialization
        price = findViewById(R.id.price);
        prodName = findViewById(R.id.prodName);
        compName = findViewById(R.id.sellerName);
        description = findViewById(R.id.prodDescription);
        prodImage = findViewById(R.id.prodImage);
        inStock = findViewById(R.id.inStock);
        buy = findViewById(R.id.buy_button);
        comment = findViewById(R.id.comment);

        //retrieves product data from firebase by using product id
        Intent intent = getIntent();
        DBConn.startProductUpdateById(intent.getExtras().getString("id"), new getProductListener()
        {
            @Override
            public void onProductRetrieved(ArrayList<Product> products)
            {
                product = products.get(0);
                if(product == null)
                    return;

                price.setText(product.getPrice()+"$");
                prodName.setText(product.getName());
                compName.setText(product.getSellerName());
                description.setText(product.getDescription());
                inStock.setText(Integer.toString(product.getInStock()));
                DBConn.getProductImageById(product.key, prodImage);

                if(firstVisit)
                {
                    firstVisit = false;
                    boolean neverVisited = true;
                    for(String productId:DBConn.account.getVisitedProducts())
                    {
                        if(product.key.equals(productId))
                        {
                            neverVisited = false;
                        }
                    }
                    if(neverVisited)
                    {
                        product.setDailyViews(product.getDailyViews()+1);
                        product.setTotalViews(product.getDailyViews()+1);
                        DBConn.account.getVisitedProducts().add(product.key);
                        DBConn.updateAccountToDB();
                    }

                    DBConn.updateProductToDB(product);
                }
                reviews = CentralAdapter.createRecycleView((RecyclerView)findViewById(R.id.reviews), price.getContext(), product.getReviews(), CentralAdapter.REVIEW_LIST);
            }
        });

        buy.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                //build dialog
                final Dialog d = new Dialog(view.getContext());
                d.setContentView(R.layout.buy_product_dialog);
                d.setTitle("buy product");
                d.setCancelable(true);

                final EditText amount = d.findViewById(R.id.amount);
                final TextView totalPrice = d.findViewById(R.id.total_price);
                final TextView accountBalance = d.findViewById(R.id.account_balance);
                final TextView warning = d.findViewById(R.id.warning);
                Button purchase = d.findViewById(R.id.purchase);

                accountBalance.setText(Double.toString(DBConn.account.getBalance())+"$");
                amount.addTextChangedListener(new TextWatcher()
                {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2)
                    {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2)
                    {
                        if(charSequence.toString().isEmpty())
                        {
                            totalPrice.setText("0$");
                            return;
                        }
                        totalPriceD = Integer.parseInt(charSequence.toString())*product.getPrice();
                        totalPrice.setText(totalPriceD+"$");
                    }

                    @Override
                    public void afterTextChanged(Editable editable)
                    {

                    }
                });
                purchase.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        if(amount.getText().toString().isEmpty()||Integer.parseInt(amount.getText().toString())<1)
                        {
                            warning.setText("\"amount\" field cannot be empty or 0");
                            return;
                        }
                        if(totalPriceD>DBConn.account.getBalance())
                        {
                            warning.setText("not enough funds in account");
                            return;
                        }
                        if(Integer.parseInt(amount.getText().toString())>product.getInStock())
                        {
                            warning.setText("not enough product in stock");
                            return;
                        }

                        ProductSale sale = new ProductSale(DBConn.account.accountId, product.getSellerName(), product.getSellerId(), product.getName(), product.key, Integer.parseInt(amount.getText().toString()), product.getPrice());
                        byte[] array = new byte[7]; // length is bounded by 7
                        new Random().nextBytes(array);
                        sale.key = new String(array, Charset.forName("UTF-8"));
                        DBConn.account.getInCart().add(sale);
                        DBConn.updateAccountToDB();
                        d.cancel();
                        Toast.makeText(view.getContext(), "added to cart", Toast.LENGTH_LONG).show();
                    }
                });
                DBConn.setOnAccountUpdateListener(new AccountUpdateListener()
                {
                    @Override
                    public void onAccountUpdate(UserAccount userAccount)
                    {
                        accountBalance.setText(Double.toString(DBConn.account.getBalance())+"$");
                    }
                });
                d.show();
            }
        });

        comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Dialog d = new Dialog(view.getContext());
                d.setContentView(R.layout.commen_dialog);
                d.setTitle("review");
                d.setCancelable(true);

                final EditText comment = d.findViewById(R.id.comment);
                Button postComment = d.findViewById(R.id.post_comment);
                postComment.setOnClickListener(new View.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onClick(View view) {
                        if(!comment.getText().toString().isEmpty())
                        {
                            Review review = new Review(comment.getText().toString(), DBConn.account.accountId, DBConn.account.fname, LocalDateTime.now());

                            byte[]bytes = new byte[8];
                            new Random().nextBytes(bytes);
                            review.key = new String(bytes, Charset.forName("UTF-8"));
                            product.getReviews().add(review);
                            DBConn.updateProductToDB(product);
                            d.cancel();
                        }
                        else
                        {

                        }

                    }
                });

                d.show();
            }
        });
    }

    /**
     * back to market list
     * @param view
     */
    public void backToList(View view)
    {
        finish();
    }
}