package com.example.admin.printreceiptthroughbluetooth.printproject;

import android.app.Notification;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.printservice.CustomPrinterIconCallback;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.graphics.BitmapCompat;
import android.util.Log;
import android.util.Printer;
import android.widget.ImageView;

import com.example.admin.printreceiptthroughbluetooth.R;
import com.example.admin.printreceiptthroughbluetooth.model.SalesModel;
import com.example.admin.printreceiptthroughbluetooth.utility.Utility;


import java.io.ByteArrayOutputStream;

import static android.graphics.Bitmap.*;


/**
 * This class is responsible to generate a static sales receipt and to print that receipt
 */
public class PrintReceipt {


    public static boolean printBillFromOrder(Context context) {
        if (BluetoothPrinterActivity.BLUETOOTH_PRINTER.IsNoConnection()) {
            return false;
        }

		/*double totalBill=0.00, netBill=0.00, totalVat=0.00;*/

        //LF = Line feed
        BluetoothPrinterActivity.BLUETOOTH_PRINTER.Begin();
        BluetoothPrinterActivity.BLUETOOTH_PRINTER.LF();
        /*BluetoothPrinterActivity.BLUETOOTH_PRINTER.LF();*/
        BluetoothPrinterActivity.BLUETOOTH_PRINTER.SetAlignMode((byte) 0);//CENTER
        BluetoothPrinterActivity.BLUETOOTH_PRINTER.SetLineSpacing((byte) 50);    //30 * 0.125mm
        //BluetoothPrinterActivity.BLUETOOTH_PRINTER.SetFontEnlarge((byte) 0x00);//normal
        BluetoothPrinterActivity.BLUETOOTH_PRINTER.BT_Write("Company Name");
        //Drawable myDrawable = context.getDrawable(R.drawable.ic_launcher_background);
        //	Bitmap anImage      = ((BitmapDrawable) myDrawable).getBitmap();
		/*Bitmap bitmap;
		bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher_background);

*/

    //    Bitmap bitmap1 = Bitmap.createBitmap(60, 60, ((BitmapDrawable) drawable).getBitmap().getConfig());
  //      BluetoothPrinterActivity.BLUETOOTH_PRINTER.printImage(bitmap1);
        //Bitmap bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher_background);
        //Notification.Builder.setLargeIcon(bmp);
		/*	try {
			Bitmap bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher_background);
			if(bmp!=null){
				byte[] command = Utils.decodeBitmap(bmp);
			//	printText(command);
				BluetoothPrinterActivity.BLUETOOTH_PRINTER.printImage(bmp);
			}else{
				Log.e("Print Photo error", "the file isn't exists");
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.e("PrintTools", "the file isn't exists");
		}*/

	/*	BluetoothPrinterActivity.BLUETOOTH_PRINTER.LF();
		BluetoothPrinterActivity.BLUETOOTH_PRINTER.SetAlignMode((byte) 1);
		BluetoothPrinterActivity.BLUETOOTH_PRINTER.SetLineSpacing((byte) 30);
		BluetoothPrinterActivity.BLUETOOTH_PRINTER.SetFontEnlarge((byte) 0x00);*/

        //BT_Write() method will initiate the printer to start printing.
       /* try {
            Bitmap bmp = BitmapFactory.decodeResource(context.getResources(),
                    R.drawable.face);
            if(bmp!=null){
                byte[] command = Utils.decodeBitmap(bmp);
                printText(command);
            }else{
                Log.e("Print Photo error", "the file isn't exists");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("PrintTools", "the file isn't exists");
        }*/
        BluetoothPrinterActivity.BLUETOOTH_PRINTER.printImage(((BitmapDrawable) context.getResources().getDrawable(R.drawable.face)).getBitmap());
      // BluetoothPrinterActivity.BLUETOOTH_PRINTER.BT_Write(((BitmapDrawable) context.getResources().getDrawable(R.drawable.face)).getBitmap(),60,60,false);
        BluetoothPrinterActivity.BLUETOOTH_PRINTER.BT_Write(
                "\nBranch Name: " + "Stuttgart Branch" +
                        "\nOrder No: " + "1245784256454" +
                        "\nBill No: " + "554741254854" +
                        "\nTrn. Date:" + "29/12/2015" +
                        "\nSalesman:" + "Mr. Salesman" +
                        "\nSalesman:" + "Mr. Salesman");
		
		/*BluetoothPrinterActivity.BLUETOOTH_PRINTER.LF();
		BluetoothPrinterActivity.BLUETOOTH_PRINTER.BT_Write(context.getResources().getString(R.string.print_line));
		BluetoothPrinterActivity.BLUETOOTH_PRINTER.LF();
		
		BluetoothPrinterActivity.BLUETOOTH_PRINTER.SetAlignMode((byte) 0);//LEFT
		BluetoothPrinterActivity.BLUETOOTH_PRINTER.SetLineSpacing((byte) 30);	//50 * 0.125mm
		BluetoothPrinterActivity.BLUETOOTH_PRINTER.SetFontEnlarge((byte) 0x00);//normal font*/

        //static sales record are generated
		/*SalesModel.generatedMoneyReceipt();*/

		/*for(int i=0;i<StaticValue.arrayListSalesModel.size();i++){
			SalesModel salesModel = StaticValue.arrayListSalesModel.get(i);
			BluetoothPrinterActivity.BLUETOOTH_PRINTER.BT_Write(salesModel.getProductShortName());
			BluetoothPrinterActivity.BLUETOOTH_PRINTER.LF();
			BluetoothPrinterActivity.BLUETOOTH_PRINTER.BT_Write(" " + salesModel.getSalesAmount() + "x" + salesModel.getUnitSalesCost() +
					"=" + Utility.doubleFormatter(salesModel.getSalesAmount() * salesModel.getUnitSalesCost()) + "" + StaticValue.CURRENCY);
			BluetoothPrinterActivity.BLUETOOTH_PRINTER.LF();
			
			totalBill=totalBill + (salesModel.getUnitSalesCost() * salesModel.getSalesAmount());
		}*/
		
		/*BluetoothPrinterActivity.BLUETOOTH_PRINTER.LF();
		BluetoothPrinterActivity.BLUETOOTH_PRINTER.BT_Write(context.getResources().getString(R.string.print_line));
		
		
		BluetoothPrinterActivity.BLUETOOTH_PRINTER.SetAlignMode((byte) 2);//RIGHT
		BluetoothPrinterActivity.BLUETOOTH_PRINTER.SetLineSpacing((byte) 30);	//50 * 0.125mm
		BluetoothPrinterActivity.BLUETOOTH_PRINTER.SetFontEnlarge((byte)0x00);//normal font
		
		totalVat=Double.parseDouble(Utility.doubleFormatter(totalBill*(StaticValue.VAT/100)));
		netBill=totalBill+totalVat;
		
		BluetoothPrinterActivity.BLUETOOTH_PRINTER.LF();
		BluetoothPrinterActivity.BLUETOOTH_PRINTER.BT_Write("Total Bill:" + Utility.doubleFormatter(totalBill) + "" + StaticValue.CURRENCY);
		
		BluetoothPrinterActivity.BLUETOOTH_PRINTER.LF();
		BluetoothPrinterActivity.BLUETOOTH_PRINTER.BT_Write(Double.toString(StaticValue.VAT) + "% VAT:" + Utility.doubleFormatter(totalVat) + "" +
				StaticValue.CURRENCY);
		
		BluetoothPrinterActivity.BLUETOOTH_PRINTER.LF();
		BluetoothPrinterActivity.BLUETOOTH_PRINTER.SetAlignMode((byte) 1);//center
		BluetoothPrinterActivity.BLUETOOTH_PRINTER.BT_Write(context.getResources().getString(R.string.print_line));*/

		
/*		BluetoothPrinterActivity.BLUETOOTH_PRINTER.LF();
		BluetoothPrinterActivity.BLUETOOTH_PRINTER.SetLineSpacing((byte) 30);
		BluetoothPrinterActivity.BLUETOOTH_PRINTER.SetAlignMode((byte) 2);//Right
		BluetoothPrinterActivity.BLUETOOTH_PRINTER.SetFontEnlarge((byte) 0x9);
		BluetoothPrinterActivity.BLUETOOTH_PRINTER.BT_Write("Net Bill:" + Utility.doubleFormatter(netBill) + "" + StaticValue.CURRENCY);
		
		BluetoothPrinterActivity.BLUETOOTH_PRINTER.LF();
		BluetoothPrinterActivity.BLUETOOTH_PRINTER.SetAlignMode((byte) 1);//center
		BluetoothPrinterActivity.BLUETOOTH_PRINTER.SetFontEnlarge((byte) 0x00);//normal font
		BluetoothPrinterActivity.BLUETOOTH_PRINTER.BT_Write(context.getResources().getString(R.string.print_line));
		
		BluetoothPrinterActivity.BLUETOOTH_PRINTER.LF();
		BluetoothPrinterActivity.BLUETOOTH_PRINTER.SetAlignMode((byte) 0);//left
		BluetoothPrinterActivity.BLUETOOTH_PRINTER.BT_Write("VAT Reg. No:" + StaticValue.VAT_REGISTRATION_NUMBER);
		
		BluetoothPrinterActivity.BLUETOOTH_PRINTER.LF();
		BluetoothPrinterActivity.BLUETOOTH_PRINTER.LF();
		BluetoothPrinterActivity.BLUETOOTH_PRINTER.LF();
		BluetoothPrinterActivity.BLUETOOTH_PRINTER.SetAlignMode((byte) 0);//left
		BluetoothPrinterActivity.BLUETOOTH_PRINTER.BT_Write(StaticValue.BRANCH_ADDRESS);
		
		BluetoothPrinterActivity.BLUETOOTH_PRINTER.LF();
		BluetoothPrinterActivity.BLUETOOTH_PRINTER.LF();
		BluetoothPrinterActivity.BLUETOOTH_PRINTER.LF();
		BluetoothPrinterActivity.BLUETOOTH_PRINTER.LF();
		BluetoothPrinterActivity.BLUETOOTH_PRINTER.SetAlignMode((byte)1);//Center
		BluetoothPrinterActivity.BLUETOOTH_PRINTER.BT_Write("\n\nThank You\nPOWERED By SIAS ERP");


		BluetoothPrinterActivity.BLUETOOTH_PRINTER.LF();
		BluetoothPrinterActivity.BLUETOOTH_PRINTER.LF();
		BluetoothPrinterActivity.BLUETOOTH_PRINTER.LF();
		BluetoothPrinterActivity.BLUETOOTH_PRINTER.LF();*/
        return true;
    }
}
