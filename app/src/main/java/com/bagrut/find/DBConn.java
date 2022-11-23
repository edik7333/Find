package com.bagrut.find;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;

/**
 * a static class that deals with all the connections with firebase
 */
@SuppressWarnings("SpellCheckingInspection")
public class DBConn
{
    /**
     * broadcast reciver for network status
     */
    public static NoSignalReceiver noSignalReceiver = new NoSignalReceiver();

    /**
     * firebase database
     */
    public static FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

    /**
     * user account data
     */
    public static UserAccount account = new UserAccount();
    /**
     * user account refarence
     */
    public static DatabaseReference accountsRef;
    /**
     * account snapshot
     */
    public static DataSnapshot accountSnapshot = null;
    /**
     * account listener
     */
    public static ValueEventListener accountListener;
    /**
     * registered listeners for account updates
     */
    private static ArrayList<AccountUpdateListener> registeredUpdateAccountListeners = new ArrayList<>();

    /**
     * firebase authentication
     */
    public static FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    /**
     * temporary data for companies
     */
    public static ArrayList<Company> tempCompanies = new ArrayList<>();
    /**
     * registered owned company listeners
     */
    public static ownedCompanyListListener registerdCompanyListListener;
    /**
     * registered company listeners
     */
    private static ArrayList<getCompanyListener> CompanyListeners = new ArrayList<>();

    /**
     * temporary ordered products data
     */
    public static ArrayList<ProductSale> tempOrderedProducts = new ArrayList<>();

    /**
     * firestore for picture access
     */
    public static FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();

    /**
     * a private method that updates account data from firebase data snepshot (used by startAccountOnChangeUpdate())
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private static void accountUpdate()
    {
        accountsRef = accountSnapshot.getRef();

        if (accountSnapshot.getValue() == null)
        {
            return;
        }

        account = accountSnapshot.getValue(UserAccount.class);

        for (String ownedCompany : account.getCompaniesKeys())
        {
            firebaseDatabase.getReference("companies").child(ownedCompany).get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>()
            {
                @Override
                public void onSuccess(DataSnapshot dataSnapshot)
                {
                    Company company = dataSnapshot.getValue(Company.class);
                    for (Company c : tempCompanies)
                    {
                        if (c.key.equals(company.key))
                        {
                            c = company;
                            return;
                        }
                    }
                    tempCompanies.add(company);
                    registerdCompanyListListener.onOwnedCompanyListRetrieved();
                }
            });
        }
    }

    /**
     * sends an email with reset pass link if the email is in database
     * @param emailAddress the email address to send reset link
     * @param conf email sent conformetion listener
     */
    public static void resetPass(String emailAddress, final getPassResetConf conf)
    {
        firebaseAuth.sendPasswordResetEmail(emailAddress)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            conf.onPasswordReset();
                        }
                    }
                });
    }

    /**
     * uploads image of product
     * @param productId product id of image
     * @param image the image to upload
     */
    public static void uploadProductImage(String productId, Uri image)
    {
        if(image==null)
            return;
        StorageMetadata metadata = new StorageMetadata.Builder().setCustomMetadata("productId", productId).build();
        DBConn.firebaseStorage.getReference("products").child(productId).putFile(image, metadata);
    }

    /**
     * retrieves image of product by product id and sets it to image view
     * @param productId  product id of image
     * @param imageView the image view to display picture on
     */
    public static void getProductImageById(String productId, final ImageView imageView)
    {
        try
        {
            StorageReference reference = firebaseStorage.getReference().child("products").child(productId);

            GlideApp.with(imageView.getContext()).load(reference).into(imageView);
        }catch (Exception e)
        {
            e.printStackTrace();
            imageView.setImageResource(R.drawable.background_in_company_activity);
        }

    }

    /**
     * - retrieves once account data from firebase <br>
     * - stores db snapshot of account in accountSnapshot static var<br>
     * notice: this method can only retrieve data if user is logged in<br>
     *
     * @return true - if successful data retrieval <br> false - if failed data retrieval
     */
    public static Task firstAccountUpdate()
    {
        if (firebaseAuth.getCurrentUser() == null)
            return null;

        DatabaseReference ref = DBConn.firebaseDatabase.getReference("userAccounts").orderByChild("accountId").equalTo(DBConn.firebaseAuth.getCurrentUser().getUid()).getRef();

        return ref.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>()
        {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task)
            {
                if(task.isComplete()&&task.isSuccessful())
                {
                    //get registered account snapshot
                    for (DataSnapshot s : task.getResult().getChildren())
                    {
                        if (s.child("accountId").getValue().equals(DBConn.firebaseAuth.getCurrentUser().getUid()))
                        {
                            accountSnapshot = s;
                            break;
                        }
                    }

                    if (accountSnapshot != null)
                    {
                        accountsRef = accountSnapshot.getRef();
                    }
                    else
                    {
                        account = null;
                    }

                    //update listeners
                    for (AccountUpdateListener aListener : registeredUpdateAccountListeners)
                    {
                        aListener.onAccountUpdate(account);
                    }
                    account = new UserAccount();
                }
                else
                {
                    Log.w("login", task.getException());
                    registeredUpdateAccountListeners.clear();
                }
            }
        });
    }

    /**
     * - creates on change listener and retrieves account data from firebase and stores it in account static var <br>
     * - stores db snapshot of account in accountSnapshot static var<br>
     * notice: this method can only retrieve data if user is logged in<br>
     *
     * @return true - if successful data retrieval <br>
     * false - if failed data retrieval
     */
    public static ValueEventListener startAccountOnChangeUpdate()
    {
        if (accountsRef != null && firebaseAuth.getCurrentUser() == null)
            return null;

        if (accountListener != null)
        {
            accountsRef.removeEventListener(accountListener);
        }

        accountListener = accountsRef.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {

                if (snapshot != null)
                {
                    accountSnapshot = snapshot;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    {
                        accountUpdate();
                    }
                }
                else
                {
                    account = null;
                }

                //update listeners
                for (AccountUpdateListener aListener : registeredUpdateAccountListeners)
                {
                    if (aListener == null)
                        continue;
                    aListener.onAccountUpdate(account);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {
                Log.w("DBConn", error.getMessage());
            }
        });
        return accountListener;
    }

    /**
     * retrieves product by productId from firebase and sends it to a listener when task complite
     *
     * @param productId the id of the product to retrieve
     * @param listener  the listener who will get the product when task complite
     */
    public static void requestProductById(final String productId, final getProductListener listener)
    {
        if (productId == null || firebaseAuth.getCurrentUser() == null)
            return;
        firebaseDatabase.getReference("products").child(productId).get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>()
        {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot)
            {
                ArrayList<Product> products = new ArrayList<>();
                Product product = dataSnapshot.getValue(Product.class);
                products.add(product);
                listener.onProductRetrieved(products);
            }
        });
    }

    /**
     * retrieves products by productId from firebase and sends it to a listener when task complite
     *
     * @param productsId the ids of the products to retrieve
     * @param listener  the listener who will get the product when task complite
     */
    public static void requestProductsById(final ArrayList<String> productsId, final getProductListener listener)
    {
        if (productsId == null || firebaseAuth.getCurrentUser() == null)
            return;
        final ArrayList<Product> products = new ArrayList<>();
        int count = 0;
        for(String productId : productsId)
        {
            count++;
            if(productId==null)
                continue;
            int finalCount = count;
            firebaseDatabase.getReference("products").child(productId).get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>()
            {
                @Override
                public void onSuccess(DataSnapshot dataSnapshot)
                {
                    Product product = dataSnapshot.getValue(Product.class);
                    products.add(product);
                    if(finalCount >=productsId.size())
                        listener.onProductRetrieved(products);
                }
            });
        }

    }

    /**
     * retrieves product by productId from firebase when updated and sends it to a listener when task complite
     *
     * @param productId the id of the product to retrieve
     * @param listener  the listener who will get the product when task complite
     */
    public static void startProductUpdateById(final String productId, final getProductListener listener)
    {
        if (productId == null || firebaseAuth.getCurrentUser() == null)
            return;

        firebaseDatabase.getReference("products").child(productId).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                ArrayList<Product> products = new ArrayList<>();
                Product product = snapshot.getValue(Product.class);
                products.add(product);
                listener.onProductRetrieved(products);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {

            }
        });
    }

    /**
     * retrieves 5 most popular products from DB starting from product at place x from the top and sends it to a listener when task complite
     *
     * @param maxPopularPlace the place of the most popular product to start from
     * @param listener        the listener who will get the product when task complite
     */
    public static void requestMostPopularProducts(int maxPopularPlace, final getProductListener listener)
    {
        if (maxPopularPlace < 0)
            return;

        firebaseDatabase.getReference("products").orderByPriority().addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                ArrayList<Product> products = new ArrayList<>();
                for (DataSnapshot productSnapshot : snapshot.getChildren())
                {
                    Product product = productSnapshot.getValue(Product.class);
                    products.add(product);
                }

                listener.onProductRetrieved(products);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {

            }
        });
    }

    /**
     * retrieves 5 most popular products from DB starting from product at place x from the top and sends it to a listener when task complite
     *
     * @param maxPopularPlace the place of the most popular product to start from
     * @param listener        the listener who will get the product when task complite
     */
    public static void requestMostPopularShares(int maxPopularPlace, final getShareListener listener)
    {
        if (maxPopularPlace < 0)
            return;

        firebaseDatabase.getReference("shares").orderByPriority().limitToFirst(5).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                ArrayList<Share> shares = new ArrayList<>();
                for (DataSnapshot shareSnapshot : snapshot.getChildren())
                {
                    Share share = shareSnapshot.getValue(Share.class);
                    shares.add(share);
                }

                listener.onShareRetrieved(shares);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {

            }
        });
    }

    /**
     * retrieves company by companyId from firebase and sends it to a listener when task complite
     *
     * @param companyId the id of the company to retrieve
     * @param listener  the listener who will get the company when task complite
     */
    public static void requestCompanyById(String companyId, final ArrayList<Product> companyProducts, final getCompanyListener listener)
    {
        if (companyId == null || firebaseAuth.getCurrentUser() == null)
            return;

        firebaseDatabase.getReference("companies").child(companyId).get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>()
        {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot)
            {
                fatchCompanyFromDB(dataSnapshot, companyProducts, new getCompanyListener()
                {
                    @Override
                    public void onCompanyRetrieved(Company company)
                    {
                        listener.onCompanyRetrieved(company);
                        for (getCompanyListener listener1 : CompanyListeners)
                        {
                            listener1.onCompanyRetrieved(company);
                        }
                    }

                    @Override
                    public void onCompanyRetrievedAndProducts(Company company, ArrayList<Product> companyProductss)
                    {

                    }
                });
            }
        });
    }

    /**
     * retrieves company by companyId when updated from firebase and sends it to a listener when task complite
     *
     * @param companyId the id of the company to retrieve
     * @param listener  the listener who will get the company when task complite
     */
    public static void startCompanyUpdateById(String companyId, final ArrayList<Product> companyProducts, final getCompanyListener listener)
    {
        if (companyId == null || firebaseAuth.getCurrentUser() == null)
            return;


        firebaseDatabase.getReference("companies").child(companyId).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                final Company company = snapshot.getValue(Company.class);
                if (!company.getProductsKeys().isEmpty())
                {
                    companyProducts.clear();
                    requestProductsById( company.getProductsKeys(), new getProductListener()
                    {
                        @Override
                        public void onProductRetrieved(ArrayList<Product> products)
                        {
                            for(Product p:products)
                            {
                                companyProducts.add(p);
                            }
                            try
                            {
                                for (getCompanyListener listener1 : CompanyListeners)
                                {
                                    listener1.onCompanyRetrieved(company);
                                    listener1.onCompanyRetrievedAndProducts(company, companyProducts);
                                }
                            }catch (ConcurrentModificationException e)
                            {
                                for (getCompanyListener listener1 : CompanyListeners)
                                {
                                    listener1.onCompanyRetrieved(company);
                                    listener1.onCompanyRetrievedAndProducts(company, companyProducts);
                                }
                            }

                            listener.onCompanyRetrieved(company);
                        }
                    });
                }
                else
                {
                    try
                    {
                        for (getCompanyListener listener1 : CompanyListeners)
                        {
                            listener1.onCompanyRetrieved(company);
                            listener1.onCompanyRetrievedAndProducts(company, companyProducts);
                        }
                    }catch (ConcurrentModificationException e)
                    {
                        for (getCompanyListener listener1 : CompanyListeners)
                        {
                            listener1.onCompanyRetrieved(company);
                            listener1.onCompanyRetrievedAndProducts(company, companyProducts);
                        }
                    }
                    listener.onCompanyRetrieved(company);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {

            }
        });
    }

    /**
     * retrieves productSales by productSaleId from firebase and sends it to a listener when task complite
     *
     * @param productSaleId the id of the productSale to retrieve
     * @param listener      the listener who will get the productSale when task complite
     */
    public static void requestProductSaleById(final String productSaleId, final getProductSaleListener listener)
    {
        if (productSaleId == null || firebaseAuth.getCurrentUser() == null)
            return;

        firebaseDatabase.getReference("ordered").child(productSaleId).get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>()
        {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot)
            {
                ProductSale sale = new ProductSale(DBConn.account.accountId, dataSnapshot.child("sellerName").getValue().toString(), dataSnapshot.child("sellerID").getValue().toString(), dataSnapshot.child("productName").getValue().toString(), dataSnapshot.getKey(), Integer.parseInt(dataSnapshot.child("amount").getValue().toString()), Double.parseDouble(dataSnapshot.child("pricePerUnit").getValue().toString()));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                {
                    sale.transactionTime = LocalDateTime.of(Integer.parseInt(dataSnapshot.child("transactionTime").child("year").getValue().toString()), Integer.parseInt(dataSnapshot.child("transactionTime").child("monthValue").getValue().toString()), Integer.parseInt(dataSnapshot.child("transactionTime").child("dayOfMonth").getValue().toString()), Integer.parseInt(dataSnapshot.child("transactionTime").child("hour").getValue().toString()), Integer.parseInt(dataSnapshot.child("transactionTime").child("minute").getValue().toString()));
                }
                sale.key = dataSnapshot.getKey();
                ArrayList<ProductSale> sales = new ArrayList<>();
                sales.add(sale);
                listener.onProductSaleRetrieved(sales);
            }
        });
    }

    /**
     * retrieves shares by company id from firebase and sends it to a listener when task complite
     *
     * @param companyId     the id of the companies shares to retrieve
     * @param listener      the listener who will get the productSale when task complite
     */
    public static void requestShareByCompanyId(final String companyId, final getShareListener listener)
    {
        firebaseDatabase.getReference("shares").get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>()
        {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    firebaseDatabase.getReference("shares").orderByChild("companyId").equalTo(companyId).getRef().get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>()
                    {
                        @Override
                        public void onSuccess(DataSnapshot dataSnapshot)
                        {
                            DataSnapshot shareSnapshot = null;
                            for (DataSnapshot s : dataSnapshot.getChildren())
                            {
                                if (s.child("companyId").getValue().equals(companyId))
                                {
                                    shareSnapshot = s;
                                    break;
                                }
                            }
                            ArrayList<Share> shares = new ArrayList<>();
                            if(shareSnapshot!=null)
                            {
                                shares.add(shareSnapshot.getValue(Share.class));
                            }
                            listener.onShareRetrieved(shares);

                        }
                    });
                }
                else
                    listener.onShareRetrieved(new ArrayList<Share>());
            }
        });

    }

    /**
     * update shares to firebase DB
     * @param share the share to update to DB
     */
    public static void updateShare(final Share share)
    {
        firebaseDatabase.getReference("shares").orderByChild("companyId").equalTo(share.getCompanyId()).getRef().get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>()
        {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot)
            {
                DataSnapshot shareSnapshot = null;
                for (DataSnapshot s : dataSnapshot.getChildren())
                {
                    if (s.child("companyId").getValue().equals(share.getCompanyId()))
                    {
                        shareSnapshot = s;
                        break;
                    }
                }
                if (shareSnapshot != null)
                {
                    shareSnapshot.getRef().setValue(share);
                }
            }
        });
    }

    /**
     * retrieves all productSales by clientId from firebase and sends it to a listener when task complite
     *
     * @param clientId the id of the client of productSale to retrieve
     * @param listener the listener who will get the productSale when task complite
     */
    public static void requestProductSaleByClientId(final String clientId, final getProductSaleListener listener)
    {
        if (clientId == null || firebaseAuth.getCurrentUser() == null)
            return;

        firebaseDatabase.getReference("ordered").orderByChild("clientId").equalTo(clientId).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                tempOrderedProducts = new ArrayList<>();
                for (DataSnapshot order : snapshot.getChildren())
                {
                    if (order.child("clientId").getValue().toString().equals(clientId))
                    {
                        ProductSale sale = new ProductSale(DBConn.account.accountId, order.child("sellerName").getValue().toString(), order.child("sellerID").getValue().toString(), order.child("productName").getValue().toString(), order.child("productID").getValue().toString(), Integer.parseInt(order.child("amount").getValue().toString()), Double.parseDouble(order.child("pricePerUnit").getValue().toString()));
                        sale.key = order.getKey();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                        {
                            sale.transactionTime = LocalDateTime.of(Integer.parseInt(order.child("transactionTime").child("year").getValue().toString()), Integer.parseInt(order.child("transactionTime").child("monthValue").getValue().toString()), Integer.parseInt(order.child("transactionTime").child("dayOfMonth").getValue().toString()), Integer.parseInt(order.child("transactionTime").child("hour").getValue().toString()), Integer.parseInt(order.child("transactionTime").child("minute").getValue().toString()));
                        }
                        tempOrderedProducts.add(sale);

                    }
                }
                listener.onProductSaleRetrieved(tempOrderedProducts);
                //update listeners
                for (AccountUpdateListener aListener : registeredUpdateAccountListeners)
                {
                    if (aListener == null)
                        continue;
                    aListener.onAccountUpdate(account);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {

            }
        });
    }

    /**
     * creates new company object from snapshotdata and returns it (used in requestCompanyById)
     *
     * @param dataSnapshot datasnapshot of a company in firebase
     * @return a new Company object
     *
     */
    private static void fatchCompanyFromDB(DataSnapshot dataSnapshot, final ArrayList<Product> companyProducts, final getCompanyListener listener)
    {
        final Company company = dataSnapshot.getValue(Company.class);
        if(company.getProductsKeys()!=null)
            company.getProductsKeys().clear();
        //getProducts
        for (DataSnapshot key : dataSnapshot.child("productsKeys").getChildren())
        {
            String keyString = key.getValue().toString();
            company.getProductsKeys().add(keyString);

            firebaseDatabase.getReference("products").child(keyString).get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>()
            {
                @Override
                public void onSuccess(DataSnapshot dataSnapshot)
                {
                    Product p = dataSnapshot.getValue(Product.class);
                    companyProducts.add(p);
                }
            });
        }
        listener.onCompanyRetrieved(company);
    }

    /**
     * uploads DBobject to specified path in firebase in a form of json
     *
     * @param object DBobject to upload
     * @param path   a path in firebase to upload to dirs seperated by '/', example: child1/child2
     */
    public static DatabaseReference pushToDB(DBObject object, String path)
    {
        String[] directories = path.split("/");
        DatabaseReference reference = DBConn.firebaseDatabase.getReference(directories[0]);
        if (directories.length > 1)
            for (int i = 1; i < directories.length; i++)
            {
                reference = reference.child(directories[i]);
            }
        reference = reference.push();
        object.key = reference.getKey();
        reference.setValue(object);

        return reference;
    }

    /**
     * uploads DBobject to specified path in firebase in a form of json
     *
     * @param object    DBobject to upload
     * @param reference the reference to push object to
     * @return
     */
    public static DatabaseReference pushToDB(DBObject object, DatabaseReference reference)
    {
        reference = reference.push();
        object.key = reference.getKey();
        reference.setValue(object);

        return reference;
    }

    /**
     * updates account to firebase(cannot remove values)
     */
    public static void updateAccountToDB()
    {
        firebaseDatabase.getReference("userAccounts").child(account.key).setValue(account);
    }

    /**
     * remove a product from cart
     * @param productSaleId the product sale to remove from cart
     */
    public static void removeInCart(final String productSaleId)
    {
        accountsRef.child("inCart").orderByChild("key").equalTo(productSaleId).get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>()
        {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot)
            {
                for (DataSnapshot d : dataSnapshot.getChildren())
                {
                    if (d.child("key").getValue().toString().equals(productSaleId))
                        d.getRef().removeValue();
                }
            }
        });
    }

    /**
     * remove product from favorites
     * @param productId the product to remove
     */
    public static void removeFav(final String productId)
    {
        accountsRef.child("favorite").orderByValue().equalTo(productId).get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>()
        {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot)
            {
                for (DataSnapshot d : dataSnapshot.getChildren())
                {
                    if (d.getValue().toString().equals(productId))
                        d.getRef().removeValue();
                }
            }
        });
    }

    /**
     * removes an order
     * @param productSaleId the order to remove
     */
    public static void removeOrder(String productSaleId)
    {
        firebaseDatabase.getReference("ordered").child(productSaleId).removeValue();
    }

    /**
     * removes a product from DB
     * @param productId the product to remove
     */
    public static void removeProduct(final String productId)
    {
        requestProductById(productId, new getProductListener()
        {
            @Override
            public void onProductRetrieved(ArrayList<Product> products)
            {
                if (products.get(0) == null)
                    return;
                firebaseDatabase.getReference("companies").child(products.get(0).getSellerId()).child("productsKeys").get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>()
                {
                    @Override
                    public void onSuccess(DataSnapshot dataSnapshot)
                    {
                        for (DataSnapshot productKey : dataSnapshot.getChildren())
                        {
                            if (productKey.getValue().toString().equals(productId))
                                productKey.getRef().removeValue();
                        }
                        firebaseDatabase.getReference("products").child(productId).removeValue();
                    }
                });
            }
        });

    }

    /**
     * gets Company object and updates it to firebase
     *
     * @param company a Company object
     */
    public static void updateCompanyToDB(Company company)
    {
        firebaseDatabase.getReference("companies").child(company.key).setValue(company);
    }

    /**
     * updates product to firebase
     * @param product the product to update
     */
    public static void updateProductToDB(Product product)
    {
        firebaseDatabase.getReference("products").child(product.key).setValue(product);
    }
    /**
     * updates product sale to firebase
     * @param productSale the product sale to update
     */
    public static void updateProductSaleToDB(ProductSale productSale)
    {
        firebaseDatabase.getReference("ordered").child(productSale.key).setValue(productSale);
    }

    /**
     * updates DBobject to specified path in firebase in a form of json
     *
     * @param updatedValue DBobject to update
     * @param path         a path in firebase to upload to dirs seperated by '/', example: child1/child2
     * @return
     */
    public static void updateToDB(Object updatedValue, String path)
    {
        String[] directories = path.split("/");
        DatabaseReference reference = DBConn.firebaseDatabase.getReference(directories[0]);
        if (directories.length > 1)
            for (int i = 1; i < directories.length; i++)
            {
                reference = reference.child(directories[i]);
            }
        reference.setValue(updatedValue);
    }

    /**
     * loggs out of the account and deletes temp data of account in the app
     */
    public static void logout()
    {
        DBConn.removeAllOnAccountUpdateListeners();
        DBConn.firebaseAuth.signOut();
        DBConn.account = new UserAccount();
        tempCompanies = new ArrayList<>();
        NoSignalReceiver.removeAllConnStatusListeners();
    }

    /**
     * adds a listener to a list that runs when account data is retrived
     *
     * @param listener interface with onAccountUpdate
     */
    public static void setOnAccountUpdateListener(AccountUpdateListener listener)
    {
        registeredUpdateAccountListeners.add(listener);
    }

    /**
     * removes a listener from a list that runs when account data is retrived
     *
     * @param listener interface with onAccountUpdate
     */
    public static void removeOnAccountUpdateListener(AccountUpdateListener listener)
    {
        registeredUpdateAccountListeners.remove(listener);
    }

    /**
     * removes all listener in a list that runs when account data is retrived
     */
    public static void removeAllOnAccountUpdateListeners()
    {
        registeredUpdateAccountListeners = new ArrayList<>();
    }

    /**
     * registers a new company listener to recive data updates
     * @param listener the listener to register
     */
    public static void setOnCompaniesListener(getCompanyListener listener)
    {
        CompanyListeners.add(listener);
    }

    /**
     * removes a company listener from reciveing data updates
     * @param listener the listener to remove
     */
    public static void removeOnCompanyListener(getCompanyListener listener)
    {
        CompanyListeners.remove(listener);
    }

    /**
     * removes all company data update listeners
     */
    public static void removeAllOnCompaniesListener()
    {
        CompanyListeners = new ArrayList<>();
    }

    /**
     * - extends from BroadcastReceiver class <br>
     * - set visibility of no_conn_icon and no_conn_text text views according to network status
     */
    public static class NoSignalReceiver extends BroadcastReceiver
    {
        /**
         * array list of all listeners that require status update about internet signal
         */
        private static ArrayList<onConnStatusListener> statusListeners = new ArrayList<>();

        /**
         * recieves network status change
         * @param context
         * @param intent
         */
        @Override
        public void onReceive(Context context, Intent intent)
        {
            ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            for (onConnStatusListener listener : statusListeners)
            {
                listener.onConnectionStatusChanged(connMgr);
            }

        }

        /**
         * registers new connection status listener
         * @param listener the listener to register
         */
        public static void addConnStatusListener(onConnStatusListener listener)
        {
            statusListeners.add(listener);
        }

        /**
         * unregisters all listeners
         */
        public static void removeAllConnStatusListeners()
        {
            statusListeners = new ArrayList<>();
        }

        /**
         * sets visability to no connection views according to the connection status and displays dialog
         * @param activity activity of account main view
         * @param connMgr the connection manager that shows connection status
         */
        public static void handleViewOnConnStatusChange(final Activity activity, ConnectivityManager connMgr)
        {
            if (AccountActivity.noConnText != null && AccountActivity.noConnIcon != null)
            {
                if (connMgr.getActiveNetworkInfo() == null)
                {
                    AccountActivity.noConnIcon.setVisibility(View.VISIBLE);
                    AccountActivity.noConnText.setVisibility(View.VISIBLE);

                    AlertDialog.Builder builder = new AlertDialog.Builder(AccountActivity.noConnIcon.getContext());
                    builder.setMessage("connection lost, please try to login again  ");
                    builder.setTitle("Connection lost");
                    builder.setPositiveButton("logout", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i)
                        {
                            DBConn.logout();
                            activity.finish();
                        }
                    });


                    builder.show();
                }
                else
                {
                    if (connMgr.getActiveNetworkInfo().isConnected())
                    {
                        AccountActivity.noConnIcon.setVisibility(View.GONE);
                        AccountActivity.noConnText.setVisibility(View.GONE);
                    }
                    else
                    {
                        AccountActivity.noConnIcon.setVisibility(View.VISIBLE);
                        AccountActivity.noConnText.setVisibility(View.VISIBLE);

                        AlertDialog.Builder builder = new AlertDialog.Builder(AccountActivity.noConnIcon.getContext());
                        builder.setMessage("connection lost, please try to login again ");
                        builder.setTitle("Connection lost");
                        builder.setPositiveButton("logout", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i)
                            {
                                DBConn.logout();
                                activity.finish();
                            }
                        });

                        builder.show();
                    }
                }

            }
        }
    }

}

/**
 * receiving account update
 */
interface AccountUpdateListener
{
    void onAccountUpdate(UserAccount userAccount);
}

/**
 * receiving product update
 */
interface getProductListener
{
    void onProductRetrieved(ArrayList<Product> products);
}

/**
 * receiving company update
 */
interface getCompanyListener
{
    void onCompanyRetrieved(Company company);
    void onCompanyRetrievedAndProducts(Company company, ArrayList<Product> companyProductss);
}

/**
 * receiving product sale update
 */
interface getProductSaleListener
{
    void onProductSaleRetrieved(ArrayList<ProductSale> productSales);
}

/**
 * receiving owned companies update
 */
interface ownedCompanyListListener
{
    void onOwnedCompanyListRetrieved();
}

/**
 * receiving connection status update
 */
interface onConnStatusListener
{
    void onConnectionStatusChanged(ConnectivityManager connMgr);
}

/**
 * receiving share update
 */
interface getShareListener
{
    void onShareRetrieved(ArrayList<Share> shares);
}

/**
 * receiving password reset conformation
 */
interface getPassResetConf
{
    void onPasswordReset();
}
