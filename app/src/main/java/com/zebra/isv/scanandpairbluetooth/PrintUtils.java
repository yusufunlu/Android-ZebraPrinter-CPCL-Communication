package com.zebra.isv.scanandpairbluetooth;

import com.zebra.sdk.comm.BluetoothConnection;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.printer.ZebraPrinter;
import com.zebra.sdk.printer.ZebraPrinterFactory;
import com.zebra.sdk.printer.ZebraPrinterLanguageUnknownException;

/**
 * Created by DTYUNLU on 14.02.2018.
 */

public class PrintUtils {

    public static PrintUtils mPrintUtils;
    private int pageWidth, leftMargin, rightMargin;
    private String bluetoothAddr;
    private BluetoothConnection conn;
    ZebraPrinter mPrinter ;

    public static synchronized PrintUtils getInstance()
    {
        if (mPrintUtils == null)
        {
            mPrintUtils = new PrintUtils();
        }
        return mPrintUtils;
    }

    public PrintUtils() {
        pageWidth=500;
        leftMargin = 20;
        rightMargin = 20;
    }

    public void setPrinter( String _bluetoothAddr)
    {
        bluetoothAddr = _bluetoothAddr;
        conn = new BluetoothConnection(bluetoothAddr);

        if(!conn.isConnected())
        {
            try {
                conn.open();
                mPrinter = ZebraPrinterFactory.getInstance(conn);
                Thread.sleep(500);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void freePriner()
    {
        if(conn.isConnected())
        {
            try {
                Thread.sleep(500);
                conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void printNavigator(String _toNav)
    {
        if(_toNav.equals(PrintFormats.NORMAL.name()))
        {
            printNormal();
        }
        else if(_toNav.equals(PrintFormats.CENTERED_NORMAL.name()))
        {
            printNormalCentered();
        }
        else if(_toNav.equals(PrintFormats.BOLD.name()))
        {
            printBold();
        }
        else if(_toNav.equals(PrintFormats.CENTERED_BOLD.name()))
        {
            printCenteredBold();
        }
        else if(_toNav.equals(PrintFormats.BARCODE.name()))
        {
            printBarcode();
        }
        else if(_toNav.equals(PrintFormats.RIGHT_BIG.name()))
        {
            printRightHuge();
        }
    }

    public void printNormal()
    {
        try {
            mPrinter.sendCommand("! 0 300 200 210 1");
            mPrinter.sendCommand("TEXT 7 0 0 0 =======================================");
            mPrinter.sendCommand("TEXT 7 0 0 10 ============= CPCL Command =============");
            mPrinter.sendCommand("TEXT 7 0 0 30 =======================================");

            mPrinter.sendCommand("FORM");
            mPrinter.sendCommand("PRINT");

            // Make sure the data got to the printer before closing the connection
            Thread.sleep(500);

        } catch (Exception e) {
            // Handle communications error here.
            e.printStackTrace();
        }
    }

    public void printNormalCentered()
    {
        try {
            mPrinter.sendCommand("! 0 200 200 210 3");
            mPrinter.sendCommand("CENTER");
            mPrinter.sendCommand("TEXT 4 0 0 50 TESTING 001");

            mPrinter.sendCommand("FORM");
            mPrinter.sendCommand("PRINT");

            // Make sure the data got to the printer before closing the connection
            Thread.sleep(500);
        } catch (Exception e) {
            // Handle communications error here.
            e.printStackTrace();
        }


    }

    public void printCenteredBold()
    {
        try {
            mPrinter.sendCommand("! U1 SETBOLD 2");
            mPrinter.sendCommand("printCenteredBold ! U1 SETBOLD 0");
            mPrinter.sendCommand("but this text is normal.");

            mPrinter.sendCommand("FORM");
            mPrinter.sendCommand("PRINT");
            // Make sure the data got to the printer before closing the connection
            Thread.sleep(500);

        } catch (Exception e) {
            // Handle communications error here.
            e.printStackTrace();
        }
    }

    public void printBold() {

        try {
            mPrinter.sendCommand("! U1 SETBOLD 2");
            mPrinter.sendCommand("This text is in bold ! U1 SETBOLD 0");
            mPrinter.sendCommand("but this text is normal.");

            mPrinter.sendCommand("FORM");
            mPrinter.sendCommand("PRINT");
            // Make sure the data got to the printer before closing the connection
            Thread.sleep(500);
        } catch (Exception e) {
            // Handle communications error here.
            e.printStackTrace();
        }

    }

    public void printBarcode()
    {
        try
        {
        mPrinter.sendCommand("! 0 300 300 410 1");
        mPrinter.sendCommand("CENTER");
        mPrinter.sendCommand("BARCODE EAN8 1 1 100 0 30 22369112");
        mPrinter.sendCommand("PRINT");

        } catch (Exception e) {
            // Handle communications error here.
            e.printStackTrace();
        }
    }

    public void printRightHuge()
    {

    }
}
