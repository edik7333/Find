package com.bagrut.find;

import java.time.LocalDateTime;

/**
 * holds all data about a specific product sale(when product is in cart or bought)
 */
public class ProductSale extends DBObject
{
    public String saleId;
    public String clientId;
    public String sellerName;
    public String sellerID;
    public String productName;
    public String productID;
    public int amount;
    public double pricePerUnit;
    public LocalDateTime transactionTime;
    public LocalDateTime estimatedArrivalTime;

    /**
     * constructor
     */
    public ProductSale()
    {
    }

    /**
     * constructor
     * @param clientId client id
     * @param sellerName seller name
     * @param sellerID seller id
     * @param productName product name
     * @param productID product id
     * @param amount amount of bought units
     * @param pricePerUnit price per unit
     */
    public ProductSale(String clientId, String sellerName, String sellerID, String productName, String productID, int amount, double pricePerUnit)
    {
        this.clientId = clientId;
        this.sellerName = sellerName;
        this.sellerID = sellerID;
        this.productName = productName;
        this.productID = productID;
        this.amount = amount;
        this.pricePerUnit = pricePerUnit;
    }

    public String getSaleId()
    {
        return saleId;
    }

    public void setSaleId(String saleId)
    {
        this.saleId = saleId;
    }

    public String getClientId()
    {
        return clientId;
    }

    public void setClientId(String client)
    {
        this.clientId = client;
    }

    public String getSellerName()
    {
        return sellerName;
    }

    public void setSellerName(String sellerName)
    {
        this.sellerName = sellerName;
    }

    public String getSellerID()
    {
        return sellerID;
    }

    public void setSellerID(String sellerID)
    {
        this.sellerID = sellerID;
    }

    public String getProductName()
    {
        return productName;
    }

    public void setProductName(String productName)
    {
        this.productName = productName;
    }

    public String getProductID()
    {
        return productID;
    }

    public void setProductID(String productID)
    {
        this.productID = productID;
    }

    public int getAmount()
    {
        return amount;
    }

    public void setAmount(int amount)
    {
        this.amount = amount;
    }

    public double getPricePerUnit()
    {
        return pricePerUnit;
    }

    public void setPricePerUnit(double pricePerUnit)
    {
        this.pricePerUnit = pricePerUnit;
    }

}
