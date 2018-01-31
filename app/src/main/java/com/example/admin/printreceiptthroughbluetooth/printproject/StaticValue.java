package com.example.admin.printreceiptthroughbluetooth.printproject;

import com.example.admin.printreceiptthroughbluetooth.model.SalesModel;

import java.util.ArrayList;



/**
 * Created by sunder on 29.12.2015.
 */
public class StaticValue {
    public static boolean  isPrinterConnected=false;
    public static ArrayList<SalesModel> arrayListSalesModel = new ArrayList<SalesModel>();
    public static final String CURRENCY = "EUR";
    public static final double VAT = 10.00;
    public static final String VAT_REGISTRATION_NUMBER ="8877BD9877";
    public static final String BRANCH_ADDRESS ="70188, Stuttgart, Germany";
}
