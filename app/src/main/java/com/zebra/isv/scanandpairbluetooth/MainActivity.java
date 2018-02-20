/**
 * Application: Scan and Pair Bluetooth
 * Version: 1.1
 * Description: Crates a Bluetooth connection and pairs a Zebra Bluetooth enabled printer when a Bluetooth MAC Address is scanned
 * Last updated: 7/31/15
 * Updated by: Benjamin Wai, Zebra ISV Team
 */


package com.zebra.isv.scanandpairbluetooth;

/**
 * Imports packages from Android, Zebra LinkOS SDK, and Java libraries.
 * To import the Zebra SDK libraries, the ZSDK_ANDROID_API.jar must be added to the project folder
 *      and module dependency must be added via File -> Project Structure -> app -> Dependencies
 */

        import android.app.Activity;
        import android.app.AlertDialog;

        import android.bluetooth.BluetoothAdapter;
        import android.bluetooth.BluetoothClass;
        import android.bluetooth.BluetoothDevice;
        import android.content.BroadcastReceiver;
        import android.content.Context;
        import android.content.DialogInterface;
        import android.content.Intent;
        import android.content.IntentFilter;
        import android.os.Bundle;
        import android.view.Gravity;
        import android.view.Menu;
        import android.view.MenuItem;
        import android.view.View;
        import android.widget.AdapterView;
        import android.widget.ArrayAdapter;
        import android.widget.Button;
        import android.widget.EditText;
        import android.widget.ListView;
        import android.widget.ProgressBar;
        import android.widget.Spinner;
        import android.widget.Toast;
        import com.zebra.sdk.comm.BluetoothConnection;
        import com.zebra.sdk.comm.Connection;
        import com.zebra.sdk.comm.ConnectionException;
        import com.zebra.sdk.printer.SGD;
        import com.zebra.sdk.printer.ZebraPrinter;
        import com.zebra.sdk.printer.ZebraPrinterFactory;

        import java.util.ArrayList;
        import java.util.Arrays;
        import java.util.List;
        import java.util.Set;



public class MainActivity extends Activity{

    public static boolean debug =false;
    final Context context = this;
    public static String PRINTER_NAME ="ZEBRAMZ320";

    private AlertDialog successDialog;

    //Initialize Global Variables

    private BluetoothDeviceArrayAdapter adapter;
    private BroadcastReceiver broadcastReceiver;
    private ListView listview;
    private String bluetoothAddr;
    private String macAddress;
    private PrintUtils mPrintUtils;
    private Spinner formatSpinner ;

    /**
     * onCreate() contains several processes that begin once the application is started
     * Contains EventListener/ Event procedures
     *
     * @param savedInstanceState
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //  Initialize the list view and its adapter
        listview = (ListView) findViewById(R.id.lvPairedDevices);
        adapter = new BluetoothDeviceArrayAdapter(context,getPairedPrinters());
        listview.setAdapter(adapter);

        formatSpinner = (Spinner) findViewById(R.id.formatSpinner);

        //  Print a configuration label when a Bluetooth printer is clicked
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
                final BluetoothDevice item = (BluetoothDevice) parent.getItemAtPosition(position);
                if (item != null && item.getAddress() != null && isBluetoothPrinter(item)) {

                    mPrintUtils = PrintUtils.getInstance();
                    bluetoothAddr = item.getAddress();
                    if(debug)
                    {
                        createAlert(context, "Print", "Are you sure you want to print a test page?",true);
                    }
                    else
                    {
                        mPrintUtils.setPrinter(bluetoothAddr);
                        mPrintUtils.printNavigator(((GenericTable) formatSpinner.getSelectedItem()).getKod());
                        mPrintUtils.freePriner();
                    }
                }
            }
        });

        fillFormatSpinner();

        //  Create a BroadcastReciever to refresh the ListView when device is paired/unpaired
        broadcastReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                refreshList();
            }
        };

        //  Registers Bluetooth devices to ListView once paired
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(broadcastReceiver, filter);
    }

    // Inflate the menu; this adds items to the action bar if it is present.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    private void fillFormatSpinner()
    {
        List<GenericTable> printFormats = new ArrayList<>();
        for(PrintFormats it:PrintFormats.values())
        {
            printFormats.add(new GenericTable(it.name(),it.toString()));
        }

        AdapterSpinnerGeneric formatAdapter = new AdapterSpinnerGeneric(this, printFormats);
        formatAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        formatSpinner.setAdapter(formatAdapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.menu_refresh:
                refreshList();
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onDestroy() {
        if (broadcastReceiver != null)
            unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //stopForegroundDispatch();
    }

    private void showAlreadyPaired(final String serialName) {
        displayToast(String.format("%s is already paired", serialName));
    }

    private void refreshList() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.clear();
                adapter.addAll(getPairedPrinters());
                adapter.notifyDataSetChanged();
            }
        });
    }

    /**
     * Checks to see if the given printer is currently paired to the Android device via bluetooth.
     *
     * @param address
     * @return true if the printer is paired
     */

    private boolean isPrinterPaired(String address) {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        for (BluetoothDevice device : pairedDevices) {
            if (device.getAddress().replaceAll("[\\p{P}\\p{S}]", "").equalsIgnoreCase(address)) {
                showAlreadyPaired(address);
                return true;
            }
        }
        return false;
    }

    /**
     * Returns a list of all the printers currently paired to the Android device via bluetooth.
     *
     * @return a list of all the printers currently paired to the Android device via bluetooth.
     */

    private ArrayList<BluetoothDevice> getPairedPrinters() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        ArrayList<BluetoothDevice> pairedDevicesList = new ArrayList<BluetoothDevice>();
        for (BluetoothDevice device : pairedDevices) {
            if (isBluetoothPrinter(device))
                pairedDevicesList.add(device);
        }
        return pairedDevicesList;
    }

    /**
     * Determines if the given bluetooth device is a printer
     *
     * @param bluetoothDevice bluetooth device
     * @return true if the bluetooth device is a printer
     */

    private boolean isBluetoothPrinter(BluetoothDevice bluetoothDevice) {
        return bluetoothDevice.getBluetoothClass().getMajorDeviceClass() == BluetoothClass.Device.Major.IMAGING
                || bluetoothDevice.getBluetoothClass().getMajorDeviceClass() == BluetoothClass.Device.Major.UNCATEGORIZED;
    }

    /**
     * findPrinterStatus() contains processes that check the connected printer's current status for two common error states, isHeadOpen and isPaperOut,
     *      and returns a boolean
     *
     * @param conn Established connection. Can be either BluetoothConnection or TcpConnection
     * @return True if no error is found. False if an error is found.
     */

    private boolean findPrinterStatus(final Connection conn){
        try {
            if (ZebraPrinterFactory.getInstance(conn).getCurrentStatus().isHeadOpen) {
                displayToast("ERROR: Printer Head is Open");
                return false;
            }

            else if (ZebraPrinterFactory.getInstance(conn).getCurrentStatus().isPaperOut) {
                displayToast("ERROR: No Media Detected");
                return false;
            }
        } catch (Exception e){
            e.printStackTrace();
        }

        return true; // Returns if neither of the above error states is found
    }

    /**
     * displayToast creates a Toast pop up that appears in the center of the screen containing the
     * String message parameter
     * @param message String
     */

    private void displayToast(final String message){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast toast;
                toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                toast.show();
            }
        });
    }

    /**
     * Creates an Alert Dialog
     * @param context
     */

    private void createAlert(Context context, String title, String message, final boolean print){

        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        new Thread(new Runnable() {
                            public void run() {
                                try {
                                    if (print) {
                                        BluetoothConnection conn = new BluetoothConnection(bluetoothAddr);
                                        connectAndPrint(conn);
                                    }
                                    else
                                    {
                                        BluetoothConnection conn = new BluetoothConnection(macAddress);
                                        if (!isPrinterPaired(macAddress))
                                            connectDevice(conn);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        ).start();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing
                    }
                })
                .show();
    }

    /**
     * Opens a connection and prints a configuration label
     * @param conn
     */


    private void connectAndPrint (Connection conn,String _params){
        try {
            // Instantiate connection for given Bluetooth&reg; MAC Address.

            conn.open();

            // Open the connection - physical connection is established here.
            ZebraPrinter zPrinterIns = ZebraPrinterFactory.getInstance(conn);
            zPrinterIns.sendCommand("! U1 setvar \"device.languages\" \"line_print\"\r\n");
            Thread.sleep(500);

            zPrinterIns.sendCommand(_params);
            zPrinterIns.sendCommand("FORM");
            zPrinterIns.sendCommand("PRINT");

            Thread.sleep(500);
            conn.close();

        } catch (Exception e) {
            // Handle communications error here.
            e.printStackTrace();
        }
    }
    private void connectAndPrint (Connection conn){
        try {
            // Instantiate connection for given Bluetooth&reg; MAC Address.

            conn.open();

            // Open the connection - physical connection is established here.
            ZebraPrinter zPrinterIns = ZebraPrinterFactory.getInstance(conn);
            zPrinterIns.sendCommand("! U1 setvar \"device.languages\" \"line_print\"\r\n");
            Thread.sleep(500);

            // Send the data to printer as a byte array.

//            zPrinterIns.sendCommand("! 0 300 200 210 1");
//            zPrinterIns.sendCommand("TEXT 7 0 0 0 =======================================");
//            zPrinterIns.sendCommand("TEXT 7 0 0 10 ============= CPCL Command =============");
//            zPrinterIns.sendCommand("TEXT 7 0 0 30 =======================================");
//            zPrinterIns.sendCommand("TEXT 4 0 30 50 Hello World1");
//            zPrinterIns.sendCommand("TEXT 4 0 30 50 Hello World2");
//            zPrinterIns.sendCommand("TEXT 4 0 40 50 Hello World3");
//            zPrinterIns.sendCommand("TEXT 4 10 40 50 Hello World4");

//            zPrinterIns.sendCommand("! 0.3937 300 300 3 1");
//            zPrinterIns.sendCommand("IN-INCHES");
//            zPrinterIns.sendCommand("! 0 400 400 2.54 1");
//            zPrinterIns.sendCommand("IN-CENTIMETERS");
//
//            zPrinterIns.sendCommand("T 4 0 0 0 inch 0");
//            zPrinterIns.sendCommand("IN-MILLIMETERS");
//            zPrinterIns.sendCommand("T 4 0 0 5 inch 1");
//            zPrinterIns.sendCommand("T 4 0 0 10 inch 2");
//            zPrinterIns.sendCommand("T FG 3 120 250 Extra Fancy Ketchup");
//            zPrinterIns.sendCommand("T FG 3 180 250 Large Size Extra Fancy Ketchup");


            zPrinterIns.sendCommand("! 0 200 200 600 1");
            zPrinterIns.sendCommand("ENCODING GB18030");
            zPrinterIns.sendCommand("TEXT GBUNSG24.CPF 0 20 30 Font: GBUNSG24 ‚t‚u");
            zPrinterIns.sendCommand("ENCODING ASCII");
            zPrinterIns.sendCommand("TEXT 7 0 20 80 Font 7, Size 0");
            zPrinterIns.sendCommand("TEXT 6 0 20 200 Font 6, Size 0");
            zPrinterIns.sendCommand("TEXT 5 0 20 320 Font 5, Size 0");

//            zPrinterIns.sendCommand("IN-DOTS");
//            zPrinterIns.sendCommand("T 4 0 0 48 1 mm = 1 dots");
//            zPrinterIns.sendCommand("T 4 0 0 96 1 mm = 2 dots");
//            zPrinterIns.sendCommand("T 4 0 0 144 1 mm = 3 dots");
//            zPrinterIns.sendCommand("T 4 0 0 196 1 mm = 4 dots");
//            zPrinterIns.sendCommand("B 128 1 1 250 16 112 UNITS");
//            zPrinterIns.sendCommand("T 4 0 48 350 UNITS");

            zPrinterIns.sendCommand("FORM");
            zPrinterIns.sendCommand("PRINT");


            // Make sure the data got to the printer before closing the connection
            Thread.sleep(500);

            // Close the connection to release resources.
            conn.close();

        } catch (Exception e) {
            // Handle communications error here.
            e.printStackTrace();
        }
    }

    /**
     * Opens a connection to the printer and pulls information
     * @param conn
     */
    private void connectDevice (Connection conn){
        try{
            conn.open();
            Thread.sleep(500);
            conn.close();

        } catch (ConnectionException e) {
            displayToast("ERROR: Unable to connect to Printer");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }





}
