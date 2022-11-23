package com.bagrut.find;


import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;

/**
 * central adapter is a class that handles all recyclerView adapters
 * @param <T> the type of data to display on recyclerView
 */
public class CentralAdapter<T> extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{
    /**
     * the items to display
     */
    public ArrayList<T> items;
    /**
     * context for functions that require it
     */
    public Context context;
    /**
     * the type of adapter that is used in a specific instance
     */
    private int adapterType;

    /**
     * product in market list
     */
    public static final int MARKET_PRODUCT_LIST = 0;
    /**
     * review in product review list
     */
    public static final int REVIEW_LIST = 1;
    /**
     * ordered product list
     */
    public static final int ORDERED_PRODUCTS_LIST = 2;
    /**
     * company list owned by the user
     */
    public static final int OWNED_COMPANY_LIST = 3;
    /**
     * product order in cart list
     */
    public static final int CART_LIST = 4;
    /**
     * list of products sold by company
     */
    public static final int PRODUCTS_OF_COMPANY_LIST = 5;
    /**
     * list of stock shares
     */
    public static final int SHARES_LIST = 6;
    /**
     * list of messages in company
     */
    public static final int INBOX_LIST = 7;

    /**
     * a static function that sets up and returns a recyclerview attached to adapter and all its data
     * @param recyclerView the recycle view to setup
     * @param context context
     * @param data the data to bind
     * @param adapterType type of adapter to use, example: CentralAdapter.REVIEW_LIST
     * @param <T> the type of data
     * @return returns a ready recyclerview
     */
    public static <T> RecyclerView createRecycleView(RecyclerView recyclerView, Context context, ArrayList<T> data, int adapterType)
    {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context);
        recyclerView.addItemDecoration(new DividerItemDecoration(context, DividerItemDecoration.VERTICAL));
        recyclerView.setLayoutManager(layoutManager);

        CentralAdapter<T> centralAdapter = new CentralAdapter(data, context, adapterType);
        recyclerView.setAdapter(centralAdapter);

        return recyclerView;
    }

    /**
     * constructor
     * @param items data set to bind with adapter
     * @param adapterType type of adapter to use, example: CentralAdapter.REVIEW_LIST
     */
    public CentralAdapter(ArrayList<T> items, int adapterType)
    {
        this.items = items;
        this.adapterType = adapterType;
    }

    /**
     * constructor with context
     * @param items data set to bind with adapter
     * @param context context
     * @param adapterType type of adapter to use, example: CentralAdapter.REVIEW_LIST
     */
    public CentralAdapter(ArrayList<T> items, Context context, int adapterType)
    {
        this.items = items;
        this.context = context;
        this.adapterType = adapterType;
    }

    /**
     * returns a holder for items
     * @param parent
     * @param viewType
     * @return
     */
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        switch (adapterType)
        {
            case MARKET_PRODUCT_LIST:
                View viewInList = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_in_list, parent, false);
                return new productViewHolder(viewInList, this.context);
            case REVIEW_LIST:
                View ReviewView = LayoutInflater.from(parent.getContext()).inflate(R.layout.review_in_list, parent, false);
                return new ReviewViewHolder(ReviewView);
            case ORDERED_PRODUCTS_LIST:
                View orderedHolder = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_sold_in_list, parent, false);
                return new orderHolder(orderedHolder);
            case OWNED_COMPANY_LIST:
                View companyView = LayoutInflater.from(parent.getContext()).inflate(R.layout.company_in_account_overview, parent, false);
                return new companyHolder(companyView);
            case CART_LIST:
                View cartView = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_in_cart, parent, false);
                return new inCartHolder(cartView);
            case PRODUCTS_OF_COMPANY_LIST:
                View productView = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_in_company, parent, false);
                return new productInCompanyHolder(productView);
            case SHARES_LIST:
                View shareView = LayoutInflater.from(parent.getContext()).inflate(R.layout.share_in_list, parent, false);
                return new shareInListHolder(shareView);
            case INBOX_LIST:
                View inboxView = LayoutInflater.from(parent.getContext()).inflate(R.layout.inbox_item, parent, false);
                return new inboxHolder(inboxView);
        }

        return null;
    }

    /**
     * binds item data to item holder views
     * @param holder
     * @param position
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position)
    {
        T current = items.get(position);

        if(adapterType==MARKET_PRODUCT_LIST && current instanceof Product)
        {
            productViewHolder viewHolder = (productViewHolder)holder;
            Product currentProduct = (Product)current;

            viewHolder.productName.setText(currentProduct.getName());
            viewHolder.company.setText(currentProduct.getSellerName());
            viewHolder.price.setText(Double.toString(currentProduct.getPrice())+"$");
            viewHolder.description.setText(currentProduct.getDescription());
            viewHolder.inStock.setText(Integer.toString(currentProduct.getInStock()));
            viewHolder.productId = currentProduct.key;
            viewHolder.bind(currentProduct.key);

        }
        else if(adapterType== REVIEW_LIST && current instanceof Review)
        {
            ReviewViewHolder reviewViewHolder = (ReviewViewHolder)holder;
            Review currentReview = (Review)current;

            reviewViewHolder.review.setText(currentReview.getReview());
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm dd.MM.yyyy");

            reviewViewHolder.publishDate.setText(currentReview.getPublishedDate());
            reviewViewHolder.reviewerName.setText(currentReview.getReviewerName());
            reviewViewHolder.votes.setText(Integer.toString(currentReview.getVotes()));
            reviewViewHolder.currentReview = currentReview;
            reviewViewHolder.bind();
        }
        else if(adapterType==ORDERED_PRODUCTS_LIST && current instanceof ProductSale)
        {
            orderHolder orderHolder = (orderHolder)holder;
            ProductSale currentProductSale = (ProductSale)current;

            orderHolder.sale = currentProductSale;
            orderHolder.name.setText(currentProductSale.productName);
            orderHolder.seller.setText(currentProductSale.sellerName);
            orderHolder.amount.setText(Integer.toString(currentProductSale.amount));
            orderHolder.pricePerUnit.setText(Double.toString(currentProductSale.pricePerUnit));
            orderHolder.totalPrice.setText(Double.toString(currentProductSale.amount*currentProductSale.pricePerUnit));

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm dd/MM/yy");
            orderHolder.transactionTime.setText(currentProductSale.transactionTime.format(formatter));
        }
        else if(adapterType==OWNED_COMPANY_LIST && current instanceof Company)
        {
            companyHolder companyHolder = (CentralAdapter.companyHolder)holder;
            Company currentCompany = (Company)current;

            companyHolder.company = currentCompany;
            companyHolder.companyName.setText(currentCompany.getName());
            companyHolder.totalAssets.setText(currentCompany.getBudget()+"$");
            if(currentCompany.getProductsKeys()==null)
                currentCompany.setProductsKeys(new ArrayList<String>());
            companyHolder.productCount.setText(currentCompany.getProductsKeys().size()+"");
            companyHolder.description.setText(currentCompany.getDescription());
            try
            {
                companyHolder.own.setText(currentCompany.getShareByOwnerID(DBConn.account.getAccountId()).getSharesAmount()/currentCompany.getTotalShares()*100+"%");
            }catch (NullPointerException ex)
            {
                companyHolder.own.setText("0%");
            }

            companyHolder.dailyPnL.setText(Double.toString(currentCompany.getDailyPnL())+"$");
            double change = currentCompany.getDailyPnL()/currentCompany.getPreviousDailyPnL()*100;
            if(change>100)
            {
                companyHolder.dailyPnLChange.setText("+"+Double.toString(change - 100) + "%");
                companyHolder.upDownIcon.setBackground(companyHolder.upDownIcon.getResources().getDrawable(R.drawable.ic_baseline_arrow_drop_up_24));
            }
            else if(change<100)
            {
                companyHolder.dailyPnLChange.setText("-"+Double.toString(100-change)+"%");
                companyHolder.upDownIcon.setBackground(companyHolder.upDownIcon.getResources().getDrawable(R.drawable.ic_baseline_arrow_drop_down_24));
            }
            else
            {
                companyHolder.dailyPnLChange.setText("0%");
                companyHolder.upDownIcon.setBackground(companyHolder.upDownIcon.getResources().getDrawable(R.drawable.ic_baseline_remove_24));
            }
        }
        else if(adapterType==CART_LIST && current instanceof ProductSale)
        {
            inCartHolder cartHolder = (CentralAdapter.inCartHolder)holder;
            ProductSale currentProductInCart = (ProductSale) current;

            cartHolder.sale = currentProductInCart;
            cartHolder.productName.setText(currentProductInCart.productName);
            cartHolder.companyName.setText(currentProductInCart.sellerName);
            cartHolder.amount.setText(Integer.toString(currentProductInCart.amount));
            cartHolder.pricePerUnit.setText(Double.toString(currentProductInCart.pricePerUnit));
            cartHolder.totalPrice.setText(Double.toString(currentProductInCart.pricePerUnit*currentProductInCart.amount));
            cartHolder.bind();
        }

        else if(adapterType==PRODUCTS_OF_COMPANY_LIST && current instanceof Product)
        {
            productInCompanyHolder productHolder = (CentralAdapter.productInCompanyHolder)holder;
            Product currentProductInCompany = (Product) current;

            productHolder.productName.setText(currentProductInCompany.getName());
            productHolder.productId = currentProductInCompany.key;

            String dailyPnL;
            if(currentProductInCompany.getDailyPnL()>0)
            {
                dailyPnL = "+"+currentProductInCompany.getDailyPnL()+"$";
            }
            else
                dailyPnL = currentProductInCompany.getDailyPnL()+"$";
            productHolder.dailyPnL.setText(dailyPnL);
            productHolder.bind();
        }
        else if(adapterType==SHARES_LIST && current instanceof Share)
        {
            final shareInListHolder shareHolder = (CentralAdapter.shareInListHolder)holder;
            final Share currentShare = (Share) current;

            DBConn.requestCompanyById(currentShare.getCompanyId(), new ArrayList<Product>(), new getCompanyListener()
            {
                @Override
                public void onCompanyRetrieved(Company company)
                {
                    shareHolder.companyName.setText(company.getName());
                    shareHolder.description.setText(company.getDescription());
                    shareHolder.price.setText(currentShare.getPrice()+"$");
                    shareHolder.amount.setText(currentShare.getSharesAmount()+"");
                    shareHolder.company = company;
                    shareHolder.share = currentShare;
                    shareHolder.bind();
                }

                @Override
                public void onCompanyRetrievedAndProducts(Company company, ArrayList<Product> companyProductss)
                {

                }
            });
        }
        else if(adapterType == INBOX_LIST && current instanceof Inbox)
        {
            final inboxHolder inboxHolder = (CentralAdapter.inboxHolder)holder;
            final Inbox currentInbox = (Inbox) current;

            inboxHolder.msg.setText(currentInbox.getMsg());
        }
    }

    /**
     * returns amount of items in list
     * @return
     */
    @Override
    public int getItemCount()
    {
        if(items!=null)
            return items.size();
        else
            return 0;
    }

    /**
     * holder for product
     */
    public static class productViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        /**
         * context
         */
        private Context context;
        /**
         * product name
         */
        public TextView productName;
        /**
         * product id
         */
        public String productId;
        /**
         * displays company name
         */
        public TextView company;
        /**
         * displays price
         */
        public TextView price;
        /**
         * displays description
         */
        public TextView description;
        /**
         * displays amount in stock
         */
        public TextView inStock;
        /**
         * displays image
         */
        public ImageView image;
        /**
         * to product activity
         */
        public Button toProduct;
        /**
         * add to favorites
         */
        public Button favButton;
        /**
         * is favorite
         */
        private boolean isFav;
        /**
         * is it first time in list
         */
        private boolean firstTime = true;


        /**
         * constructor
         * @param itemView
         * @param context
         */
        public productViewHolder(@NonNull final View itemView, final Context context)
        {
            super(itemView);

            //initialization
            this.context = context;
            productName = itemView.findViewById(R.id.name);
            company = itemView.findViewById(R.id.company);
            price = itemView.findViewById(R.id.price);
            description = itemView.findViewById(R.id.description);
            inStock = itemView.findViewById(R.id.in_stock);
            image = itemView.findViewById(R.id.imageView4);
            favButton = itemView.findViewById(R.id.favButton);

            isFav = false;



        }

        /**
         * go to product activity
         * @param view
         */
        @Override
        public void onClick(View view)
        {
            Intent intent = new Intent(context, ProductActivity.class);
            intent.putExtra("id", this.productId);
            context.startActivity(intent);
        }

        /**
         * bind product data to views
         * @param productIdInput the product id
         */
        public void bind(String productIdInput)
        {
            if(!firstTime)
                return;
            this.productId = productIdInput;

            //retrieves favorite products and marks as fav on list
            for(DataSnapshot productIdTemp:DBConn.accountSnapshot.child("favorite").getChildren())
            {
                if(this.productId.equals(productIdTemp.getValue().toString()))
                {
                    favButton.setBackground(itemView.findViewById(R.id.favButton).getResources().getDrawable(R.drawable.ic_baseline_star_24_gold));
                    isFav = true;
                    break;
                }
            }

            //fav button listener adds or removes product from fav
            favButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    if(isFav)
                    {
                        isFav = false;
                        DBConn.account.removeFav(productId);
                        DBConn.removeFav(productId);
                        favButton.setBackground(view.getResources().getDrawable(R.drawable.ic_baseline_star_24));
                        Toast.makeText(context, productName.getText()+" removed from favorite", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        isFav = true;
                        DBConn.account.addFav(productId);
                        DBConn.updateAccountToDB();
                        favButton.setBackground(view.getResources().getDrawable(R.drawable.ic_baseline_star_24_gold));
                        Toast.makeText(context, productName.getText()+" is now in favorite", Toast.LENGTH_SHORT).show();
                    }

                }
            });
            //to product button listener creates intent to product page on click
            toProduct = itemView.findViewById(R.id.toProduct);
            toProduct.setOnClickListener(this);
            firstTime = false;

            //display product image
            DBConn.getProductImageById(productId, image);
        }
    }

    /**
     * holder for review
     */
    public static class ReviewViewHolder extends RecyclerView.ViewHolder
    {
        /**
         * review content
         */
        public TextView review;
        /**
         * reviewer name
         */
        public TextView reviewerName;
        /**
         * published date
         */
        public TextView publishDate;
        /**
         * display votes
         */
        public TextView votes;
        /**
         * upVote
         */
        public Button upVote;
        /**
         * downVote
         */
        public Button downVote;
        /**
         * review data
         */
        public Review currentReview;
        /**
         * can upVote
         */
        public boolean canUpVote;
        /**
         * can downVote
         */
        public boolean canDownVote;

        /**
         * constructor
         */
        public ReviewViewHolder(@NonNull View itemView)
        {
            super(itemView);
            review = itemView.findViewById(R.id.content);
            reviewerName = itemView.findViewById(R.id.reviewerName);
            publishDate = itemView.findViewById(R.id.publishDate);
            votes = itemView.findViewById(R.id.votes);
            upVote = itemView.findViewById(R.id.upVote);
            downVote = itemView.findViewById(R.id.downVote);

        }

        /**
         * bind product data to views
         */
        public void bind()
        {
            canUpVote = true;
            canDownVote = true;

            for(String reviewId:DBConn.account.getUpVoteReviews())
            {
                if(currentReview.key.equals(reviewId))
                {
                    canUpVote = false;
                    upVote.setBackground(upVote.getResources().getDrawable(R.drawable.ic_baseline_keyboard_arrow_up_24_green));
                    break;
                }
            }
            for(String reviewId:DBConn.account.getDownVoteReviews())
            {
                if(currentReview.key.equals(reviewId))
                {
                    canDownVote = false;
                    downVote.setBackground(downVote.getResources().getDrawable(R.drawable.ic_baseline_keyboard_arrow_down_24_red));
                    break;
                }
            }


            upVote.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    if(!(canDownVote))
                    {
                        votes.setText(Integer.parseInt(votes.getText().toString())+1+"");
                        currentReview.setVotes(currentReview.getVotes()+1);
                        DBConn.account.getDownVoteReviews().remove(currentReview.key);
                        DBConn.updateAccountToDB();
                        canDownVote = true;
                    }
                    else if(canUpVote&&canDownVote)
                    {
                        votes.setText(Integer.parseInt(votes.getText().toString())+1+"");
                        currentReview.setVotes(currentReview.getVotes()+1);
                        DBConn.account.getUpVoteReviews().add(currentReview.key);
                        DBConn.updateAccountToDB();
                        canUpVote = false;
                    }
                    DBConn.updateProductToDB(ProductActivity.product);
                }
            });
            downVote.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    if(!(canUpVote))
                    {
                        votes.setText(Integer.parseInt(votes.getText().toString())-1+"");
                        currentReview.setVotes(currentReview.getVotes()-1);
                        DBConn.account.getUpVoteReviews().remove(currentReview.key);
                        DBConn.updateAccountToDB();
                        canUpVote = true;
                    }
                    else if(canUpVote&&canDownVote)
                    {
                        votes.setText(Integer.parseInt(votes.getText().toString())-1+"");
                        currentReview.setVotes(currentReview.getVotes()-1);
                        DBConn.account.getDownVoteReviews().add(currentReview.key);
                        DBConn.updateAccountToDB();
                        canDownVote = false;
                    }
                    DBConn.updateProductToDB(ProductActivity.product);
                }
            });
        }
    }

    /**
     * holder for order
     */
    public static class orderHolder extends RecyclerView.ViewHolder
    {
        ProductSale sale;
        TextView name;
        TextView seller;
        TextView amount;
        TextView pricePerUnit;
        TextView totalPrice;
        TextView transactionTime;
        TextView arrivalTime;
        Button toProductPage;
        Button cancel;

        /**
         * constructor
         */
        public orderHolder(@NonNull View itemView)
        {
            super(itemView);

            name = itemView.findViewById(R.id.product_name);
            seller = itemView.findViewById(R.id.seller);
            amount = itemView.findViewById(R.id.amount);
            pricePerUnit = itemView.findViewById(R.id.price_per_unit);
            totalPrice = itemView.findViewById(R.id.total_price);
            transactionTime = itemView.findViewById(R.id.transactionTime);
            toProductPage = itemView.findViewById(R.id.toProductPage);
            cancel = itemView.findViewById(R.id.cancel);

            toProductPage.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    Intent intent = new Intent(view.getContext(), ProductActivity.class);
                    intent.putExtra("id", sale.productID);
                    ((Activity)view.getContext()).startActivity(intent);
                }
            });
            cancel.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    DBConn.removeOrder(sale.key);
                }
            });
        }

    }

    /**
     * holder for company
     */
    public static class companyHolder extends RecyclerView.ViewHolder
    {
        TextView companyName;
        TextView description;
        TextView own;
        TextView dailyPnL;
        TextView productCount;
        TextView totalAssets;
        TextView dailyPnLChange;
        TextView upDownIcon;
        Button toCompanyPage;
        Company company;

        /**
         * constructor
         */
        public companyHolder(@NonNull View itemView)
        {
            super(itemView);

            companyName = itemView.findViewById(R.id.companyName);
            description = itemView.findViewById(R.id.description);
            own = itemView.findViewById(R.id.own);
            dailyPnL = itemView.findViewById(R.id.dailyPnL);
            productCount = itemView.findViewById(R.id.product_count);
            totalAssets = itemView.findViewById(R.id.total_assets);
            dailyPnLChange = itemView.findViewById(R.id.dailyPnLChange);
            upDownIcon = itemView.findViewById(R.id.upDownIcon);
            toCompanyPage = itemView.findViewById(R.id.toCompanyPage);

            toCompanyPage.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    Intent intent = new Intent(view.getContext(), CompanyActivity.class);
                    intent.putExtra("companyId", company.key);
                    view.getContext().startActivity(intent);
                }
            });
        }
    }

    /**
     * holder for product in cart
     */
    public static class inCartHolder extends RecyclerView.ViewHolder
    {
        ProductSale sale;
        TextView productName;
        TextView companyName;
        TextView pricePerUnit;
        TextView amount;
        TextView totalPrice;
        Button deleteFromCart;

        /**
         * constructor
         */
        public inCartHolder(@NonNull View itemView)
        {
            super(itemView);

            productName = itemView.findViewById(R.id.product_name);
            companyName = itemView.findViewById(R.id.company_name);
            pricePerUnit = itemView.findViewById(R.id.price_per_unit);
            amount = itemView.findViewById(R.id.amount);
            totalPrice = itemView.findViewById(R.id.total_price);
            deleteFromCart = itemView.findViewById(R.id.delete_from_cart);

        }

        /**
         * bind product data to views
         */
        public void bind()
        {
            deleteFromCart.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    DBConn.account.getInCart().remove(sale);
                    DBConn.removeInCart(sale.key);
                }
            });
        }
    }

    /**
     * holder for product in company
     */
    public static class productInCompanyHolder extends RecyclerView.ViewHolder
    {
        /**
         * product id
         */
        public String productId;

        /**
         * product name
         */
        public TextView productName;

        /**
         * daily profit\loss
         */
        public TextView dailyPnL;

        /**
         * "container" for buttons
         */
        public LinearLayout buttons;
        /**
         * showing buttons
         */
        public Button showBarButton;
        /**
         * delete product
         */
        public Button delete;
        /**
         * restock product
         */
        public Button restock;
        /**
         * edit product
         */
        public Button edit;

        /**
         * constructor
         */
        public productInCompanyHolder(@NonNull View itemView)
        {
            super(itemView);

            //initialization
            productName = itemView.findViewById(R.id.product_name);
            dailyPnL = itemView.findViewById(R.id.daily_pnl);
            buttons = itemView.findViewById(R.id.product_buttons);
            showBarButton = itemView.findViewById(R.id.show_bar_button);
            delete = itemView.findViewById(R.id.delete_product);
            restock = itemView.findViewById(R.id.restock_product);
            edit = itemView.findViewById(R.id.edit_product);

            buttons.setVisibility(View.GONE);
            showBarButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    if(buttons.getVisibility()==View.GONE)
                        buttons.setVisibility(View.VISIBLE);
                    else
                        buttons.setVisibility(View.GONE);
                }
            });


        }

        /**
         * bind product data to views
         */
        public void bind()
        {
            delete.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    DBConn.removeProduct(productId);
                }
            });
            restock.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final Dialog d = new Dialog(view.getContext());
                    d.setContentView(R.layout.restock_dialog);
                    d.setTitle("restock");
                    d.setCancelable(true);

                    final EditText amount = d.findViewById(R.id.amount);
                    final Button save = d.findViewById(R.id.save);
                    final TextView error = d.findViewById(R.id.error);
                    error.setVisibility(View.INVISIBLE);

                    save.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if(!amount.getText().toString().isEmpty())
                            {
                                DBConn.requestProductById(productId, new getProductListener() {
                                    @Override
                                    public void onProductRetrieved(ArrayList<Product> products) {
                                        products.get(0).setInStock(Integer.parseInt(amount.getText().toString()));
                                        DBConn.updateProductToDB(products.get(0));
                                        d.cancel();
                                    }
                                });
                            }
                            else
                            {
                                error.setVisibility(View.VISIBLE);
                            }

                        }
                    });
                    d.show();
                }
            });
            edit.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    //build dialog
                    final Dialog d = new Dialog(view.getContext());
                    d.setContentView(R.layout.edit_product_dialog);
                    d.setTitle("edit product");
                    d.setCancelable(true);

                    final EditText companyName = d.findViewById(R.id.product_name);
                    Button editProduct = d.findViewById(R.id.edit);

                    editProduct.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(final View view)
                        {
                            if(companyName.getText().toString().isEmpty())
                                return;

                            DBConn.requestProductById(productId, new getProductListener()
                            {
                                @Override
                                public void onProductRetrieved(ArrayList<Product> products)
                                {
                                    if(products.isEmpty())
                                        return;

                                    products.get(0).setName(companyName.getText().toString());
                                    DBConn.updateProductToDB(products.get(0));
                                    d.cancel();
                                    Toast.makeText(view.getContext(), "product updated", Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    });
                    d.show();
                }
            });
        }

    }

    /**
     * holder for share
     */
    public static class shareInListHolder extends RecyclerView.ViewHolder
    {
        /**
         * company data
         */
        public Company company;
        /**
         * share data
         */
        public Share share;
        /**
         * display company name
         */
        public TextView companyName;
        /**
         * display description
         */
        public TextView description;
        /**
         * display price
         */
        public TextView price;
        /**
         * display amount
         */
        public TextView amount;
        /**
         * buy share
         */
        public Button buy;

        /**
         * constructor
         */
        public shareInListHolder(@NonNull View itemView)
        {
            super(itemView);

            //initialization
            companyName = itemView.findViewById(R.id.company_name);
            description = itemView.findViewById(R.id.description);
            price = itemView.findViewById(R.id.price);
            amount = itemView.findViewById(R.id.shares_amount);
            buy = itemView.findViewById(R.id.buy_button);

        }

        /**
         * bind product data to views
         */
        public void bind()
        {
            buy.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    final Dialog d = new Dialog(view.getContext());
                    d.setContentView(R.layout.buy_stock_dialog);
                    d.setTitle("buy stock");
                    d.setCancelable(true);
                    final TextView amountToBuy = d.findViewById(R.id.amount);
                    Button buy = d.findViewById(R.id.buy_button);
                    buy.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View view)
                        {
                            if(amountToBuy.getText().toString().isEmpty())
                                return;
                            if(Integer.parseInt(amountToBuy.getText().toString())>share.getSharesAmount())
                                return;

                            company.buyShares(share.getPrice(), Integer.parseInt(amountToBuy.getText().toString()), DBConn.account.accountId, new getShareListener()
                            {
                                @Override
                                public void onShareRetrieved(ArrayList<Share> shares)
                                {
                                    Toast.makeText(itemView.getContext(), "shares bought", Toast.LENGTH_LONG).show();
                                    d.cancel();
                                }
                            });
                        }
                    });
                    d.show();
                }
            });
        }
    }

    /**
     * holder for company message
     */
    public static class inboxHolder extends RecyclerView.ViewHolder
    {
        /**
         * message content
         */
        public TextView msg;

        /**
         * constructor
         */
        public inboxHolder(@NonNull View itemView)
        {
            super(itemView);
            msg = itemView.findViewById(R.id.msg);
        }
    }
}
