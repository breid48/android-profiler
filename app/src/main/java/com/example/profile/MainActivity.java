package com.example.profile;

import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.LinkedList;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Build;

import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.provider.Settings;
import android.view.View;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.profile.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;

import android.util.DisplayMetrics;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;

    final List<String[]> listData = new LinkedList<String[]>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);

        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        int check = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
        //System.out.println("Google Play Available: " + check);

        /**
         * Fetch System Meta Data and Populate LL
         */
        getAdvertisingId(this);
        listData.add(new String[] { "dpi", getDisplayDensity() });
        listData.add(new String[] { "hardware_version", getDeviceInfo() });
        listData.add(new String[] { "os_build", getBuildString() });
        listData.add(new String[] { "os_name", getOsName() });
        listData.add(new String[] { "os_version", getOsVersion() });
        listData.add(new String[] { "user_agent", getUserAgent() });
        listData.add(new String[] { "referrer", getReferrerString() });
        listData.add(new String[] { "screen_size", getScreenSizeString() });
        listData.add(new String[] { "android_id", getAndroidID() });

        ArrayAdapter<String []> arrayAdapter = new ArrayAdapter<String []>(this,
                                                                            android.R.layout.simple_list_item_2,
                                                                            android.R.id.text1,
                                                                            listData){
            @Override
            public View getView(int position, View convertView, ViewGroup parent){
                View row = super.getView(position, convertView, parent);

                TextView text1 = (TextView) row.findViewById(android.R.id.text1);
                TextView text2 = (TextView) row.findViewById(android.R.id.text2);

                String[] item = listData.get(position);
                text1.setText(item[0]);
                text2.setText(item[1]);
                return row;
            }
        };

        ListView listView = (ListView) findViewById(R.id.mobile_list);
        listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

                String[] item = listData.get(position);
                String label = item[0];
                String message = item[1];

                Toast.makeText(getApplicationContext(),
                        message,
                        Toast.LENGTH_SHORT).show();

                // Write the Clicked Item to Clipboard
                ClipboardManager clipboard = (ClipboardManager)
                        getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText(label, message);
                clipboard.setPrimaryClip(clip);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    /**
     * Fetch Advertising ID.
     *
     * When `isLimitAdTrackingEnabled()` is true, the advertising ID will default to
     * 00000000-0000-0000-0000-000000000000.
     */
    public void getAdvertisingId(MainActivity activObj) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    AdvertisingIdClient.Info adInfo = AdvertisingIdClient.getAdvertisingIdInfo(getApplicationContext());
                    String myId = adInfo != null ? adInfo.getId() : null;
                    activObj.listData.add(new String[] { "android_advertising", myId });
                } catch (Exception e) {
                    System.out.println("Exception Getting Advertising ID\n\n");
                }
            }
        });
    }

    public String getDisplayDensity() {
        // DPI Buckets
        NavigableMap<Integer, String> buckets = new TreeMap<Integer, String>();

        buckets.put(0, "ldpi");
        buckets.put(141, "mdpi");
        buckets.put(201, "hdpi");
        buckets.put(281, "xhdpi");
        buckets.put(401, "xxhdpi");
        buckets.put(560, "xxxhdpi");

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        float density = metrics.density;
        int densityDpi = metrics.densityDpi;

        System.out.println("Display Density: " + density);
        System.out.println("Display Density DPI: " + densityDpi);

        Map.Entry<Integer, String> entry = buckets.floorEntry(densityDpi);

        String resolution = entry.getValue();

        return resolution;
    }

    public String getMacAddress() {
        /**
         * Deprecated Since Android 6
         */
        String macAddress;

        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wInfo = wifiManager.getConnectionInfo();
        macAddress = wInfo.getMacAddress();

        System.out.println("MAC Address: " + macAddress);

        return macAddress;
    }

    public static String getDeviceInfo(){
        return String.format("%s|%s", Build.BRAND, Build.MODEL);
    }

    public static String getBuildString() {
        StringBuilder sb = new StringBuilder("Build/");
        sb.append(Build.ID);
        return sb.toString();
    }

    public static String getOsName(){
        return "Android";
    }

    public static String getOsVersion(){
        return Build.VERSION.RELEASE;
    }

    public static String getReferrerString(){
        return "utm_source=google-play&utm_medium=organic";
    }

    public static String getScreenSizeString(){
        return "screen_normal";
    }

    public static String getUserAgent(){
        String userAgent = System.getProperty("http.agent");
        return userAgent;
    }

    public static String getGameVersion(){
        return "MANUALLY_POPULATE";
    }


    public String getAndroidID(){
        String android_id = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        System.out.println("ANDROID_ID: " + android_id);
        return android_id;
    }

}