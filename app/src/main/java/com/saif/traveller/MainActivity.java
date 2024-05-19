package com.saif.traveller;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.karumi.dexter.BuildConfig;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    Toolbar toolbar;
    TabLayout tab;
    ViewPager viewPager;
    private List<Route> routeList;
    TextView toolBarTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        toolBarTitle=findViewById(R.id.toolbar_title);
        toolBarTitle.setText("TRAVELLER");

        tab=findViewById(R.id.tab);
        viewPager=findViewById(R.id.viewpager);

        // Load route data from CSV
        routeList = new ArrayList<>();
        loadRoutesData("route_ids.csv", 2); // Assuming index 2 contains source information
        // Set up ViewPager with the adapter
        setupViewPager(viewPager);
        // Connect the TabLayout to the ViewPager
        tab.setupWithViewPager(viewPager);
        //to set the colour of status bar(where time,wifi,battery level,volte LTE  is showing) same as toolbar
        Window window = getWindow();
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.Teal));

    }

    private void setupViewPager(ViewPager viewPager) {
        // Create a FragmentPagerAdapter
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        // Add your fragments to the adapter
        adapter.addFragment(new FindBusFragment(routeList), "Find Bus");
        adapter.addFragment(new FindRouteFragment(routeList), "Find Route");
        adapter.addFragment(new LiveGPSFragment(), "Live GPS"); // Add the initialized LiveGPSFragment

        // Set the adapter on the ViewPager
        viewPager.setAdapter(adapter);
    }



    private void loadRoutesData(String fileName, int sourceDestinationIndex) {
        try {
            CSVReader csvReader = new CSVReader(new InputStreamReader(getAssets().open(fileName)));
            csvReader.readNext();
            String[] nextLine;
//            Set<String> previouslyAddedRouteId=new HashSet<>();

            while ((nextLine = csvReader.readNext()) != null) {

                if (nextLine.length > sourceDestinationIndex) {
                    String routeId = nextLine[0];
                    String sourceOrDestination = nextLine[sourceDestinationIndex].trim();
                    String[] sourceAndDestination = sourceOrDestination.split("\\s+TO\\s+", 2);

                    if (sourceAndDestination.length >= 2) {
                        String source = sourceAndDestination[0].trim();
                        String destination = sourceAndDestination[1].trim();

                        // Debugging statements
//                        Log.d("LoadRoutesData", "Route ID: " + routeId);
//                        Log.d("LoadRoutesData", "Source: " + source);
//                        Log.d("LoadRoutesData", "Destination: " + destination);

                        // Check if route ID has been encountered before...currently not checking
                            routeList.add(new Route(routeId, source, destination));

                    } else {
                        Log.d("LoadRoutesData", "Invalid source-destination format: " + sourceOrDestination);
                    }
                } else {
                    Log.e("LoadRoutesData", "Invalid line: " + Arrays.toString(nextLine));
                }
            }
            csvReader.close();
        } catch (IOException | CsvException e) {
            Log.e("TAG", "Error reading CSV file: " + (e.getMessage() != null ? e.getMessage() : "Unknown error"));
        }
    }

//to set options menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        new MenuInflater(this).inflate(R.menu.opt_menu,menu);

        //to show the icons of menu items
        if(menu instanceof MenuBuilder){
            MenuBuilder m = (MenuBuilder) menu;
            m.setOptionalIconsVisible(true);
        }
        return true;
    }

//when items of options menu clicked then this method trigger
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemID=item.getItemId();

        if(itemID==R.id.about){
//            Toast.makeText(this, "about designers", Toast.LENGTH_SHORT).show();
            Intent intent=new Intent(this, AboutActivity.class);
            startActivity(intent);
        } else if (itemID==R.id.tourism) {
//            Toast.makeText(this, "Tourism places ", Toast.LENGTH_SHORT).show();
            Intent intent=new Intent(this, TourismActivity.class);
            startActivity(intent);
        } else if (itemID==R.id.near_by_place) {
//            Toast.makeText(this, "Places near you", Toast.LENGTH_SHORT).show();
            Intent intent=new Intent(this, NearByPlacesActivity.class);
            startActivity(intent);

        } else if (itemID==R.id.share) {
//            Toast.makeText(this, "Share your app", Toast.LENGTH_SHORT).show();
            // Handle share action
            shareApp();
        } else {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
    private void shareApp() {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, "Check out this amazing app: https://play.google.com/store/apps/details?id=" + getPackageName());
        sendIntent.setType("text/plain");
        startActivity(Intent.createChooser(sendIntent, "Share via"));
    }
}