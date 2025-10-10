package com.waiterpos.ui.waiter;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.waiterpos.R;
import com.waiterpos.databinding.ActivityWaiterBinding;
import com.google.android.material.navigation.NavigationView;

public class WaiterActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityWaiterBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityWaiterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarWaiter.toolbar);
        /*binding.appBarWaiter.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null)
                        .setAnchorView(R.id.fab).show();
            }
        });*/
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(R.id.nav_menu, R.id.nav_orders, R.id.nav_register).setOpenableLayout(drawer).build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_waiter);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra("HEADER_TEXT")) {
                setNavigationHeaderText(intent.getStringExtra("HEADER_TEXT"));
            }
            if (intent.hasExtra("ORDER_ID")) {
                Bundle bundle = new Bundle();
                bundle.putString("ORDER_ID", intent.getStringExtra("ORDER_ID"));
                navController.navigate(R.id.nav_register, bundle);
            } else if (intent.hasExtra("TABLES_COUNT")) {
                Bundle bundle = new Bundle();
                bundle.putInt("TABLES_KEY", intent.getIntExtra("TABLES_COUNT", 0));
                navController.navigate(R.id.nav_menu, bundle);
            }
        }
    }

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.waiter, menu);
        return true;
    }*/

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_waiter);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration) || super.onSupportNavigateUp();
    }

    private void setNavigationHeaderText(String text) {
        TextView headerTextView = headerTextView();
        headerTextView.setText(text);
    }

    public String getNavigationHeaderText() {
        TextView headerTextView = headerTextView();
        return headerTextView.getText().toString();
    }

    private TextView headerTextView() {
        NavigationView navigationView = binding.navView;
        View headerView = navigationView.getHeaderView(0);
        return headerView.findViewById(R.id.nav_header_title);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}