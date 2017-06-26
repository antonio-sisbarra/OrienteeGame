package com.sisbarra.orienteegame;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.SQLException;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import static com.sisbarra.orienteegame.R.style.AppAlertTheme;

//Codice dell'activity principale che contiene le tre sezioni della app
public class MainActivity extends AppCompatActivity {

    //Costante per la mindistance per listener della posizione
    public static final int MINDISTANCE = 5;
    //Numero di tab nell'app
    private static final int mNTabs = 3;
    //Nome delle preferences
    public static String PREFERENCE_FILENAME = null;
    //Costante per il numero di pagine massimo in memoria
    static int MAXPAGESINMEMORY = 2;
    //Riferimento al location manager di sistema
    private LocationManager mLocationManager;
    /**
     * Il {@link android.support.v4.view.PagerAdapter} che fornirà
     * i fragments per ognuna delle sezioni. Uso una sottoclasse di
     * {@link FragmentPagerAdapter}, che mantiene ogni fragment
     * caricato in memoria.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;
    /**
     * Il {@link ViewPager} che contiene i contenuti.
     */
    private ViewPager mViewPager;
    private Toolbar mToolbar;
    private TabLayout mTabLayout;
    private SharedPreferences gameSettings;
    //Flag per capire se GPS e Netw sono attivi
    private boolean mGpsEnabled;
    private boolean mNetworkEnabled;
    private boolean mInternetEnabled;
    //Riferimenti per il caricamento del DB
    private ListView mLstTargets;
    private TargetsListCursorAdapter mAdapter;
    private DataBaseHelper mHelper;
    //Listener della posizione
    private MyLocation.LocationResult mLocationResult;
    //Oggetto che si interfaccia per la posizione
    private MyLocation mMyLocation;
    //Ultima location ricevuta
    private Location mLastLocation;
    //Flag per vedere se ho preso già una location
    private boolean mHaveLoc = false;
    /* Vettore di id di icona per le sezioni */
    private int[] mTabIcons = {
            android.R.drawable.ic_menu_compass,
            android.R.drawable.ic_menu_mapmode,
            android.R.drawable.ic_menu_myplaces
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        setTabLayout();

        loadDB();

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    /**
     * Dispatch onPause() to fragments.
     */
    @Override
    protected void onPause() {
        //Rimuovo il listener della posizione
        if(mMyLocation!=null)
            mMyLocation.removeUpdates();

        super.onPause();
    }

    @Override
    protected void onPostResume() {
        //Verifica dell'username e se siamo al primo utilizzo del gioco -> Dialog
        getUserInfo();

        //Verifica della connessione a internet e gps
        verifyInternetGps();

        //Registro il listener della posizione
        mLocationResult = new MyLocation.LocationResult(){
            @Override
            public void gotLocation(Location location){
                //Got the location!
                //Prendo riferimento al fragment e gli spedisco la location
                final StartGameFragment frag = (StartGameFragment)
                        mSectionsPagerAdapter.getRegisteredFragment(0);
                if(frag!=null){
                    if(location!=null) {
                        //Aggiorno la lastlocation(utile per l'activity gioco)
                        if(mLastLocation == null) mLastLocation = new Location(location);
                        else mLastLocation.set(location);

                        frag.updatePos(location);

                        //Se è la prima volta e ho un adapter pronto
                        if(!mHaveLoc && frag.isThereAdapter()){
                            mHaveLoc = true;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    frag.setTargetText(getString(R.string.header_target_text));
                                    mMyLocation.setDistanceForUpdates(getApplicationContext(),
                                            mLocationResult, MINDISTANCE);
                                    findViewById(R.id.loadingTargetPanel).setVisibility(View.GONE);
                                }
                            });
                        }
                    }
                }
            }
        };
        mMyLocation = new MyLocation(mLocationManager, this);
        mMyLocation.getLocation(this, mLocationResult);

        super.onPostResume();
    }

    /**
     * Callback for the result from requesting permissions. This method
     * is invoked for every call on {@link #requestPermissions(String[], int)}.
     * <p>
     * <strong>Note:</strong> It is possible that the permissions request interaction
     * with the user is interrupted. In this case you will receive empty permissions
     * and results arrays which should be treated as a cancellation.
     * </p>
     *
     * @param requestCode  The request code passed in {@link #requestPermissions(String[], int)}.
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either {@link PackageManager#PERMISSION_GRANTED}
     *                     or {@link PackageManager#PERMISSION_DENIED}. Never null.
     * @see #requestPermissions(String[], int)
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 0: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    finish();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }


    //Carica il DB in background
    private void loadDB(){
        new Thread(new Runnable() {
            public void run() {
                try {
                    mHelper = new DataBaseHelper(getApplicationContext());
                    mHelper.createDataBase();
                } catch (Exception e) {
                    throw new Error(getString(R.string.error_create_db));
                }
                try {
                    mHelper.openDataBase();
                } catch (SQLException sqle) {
                    throw sqle;
                }
            }
        }).start();
    }


    //Verifica se si è connessi a internet e al gps
    private void verifyInternetGps(){
        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        mInternetEnabled = isInternetAvailable();

        try {
            mGpsEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch(Exception ex) {}

        try {
            mNetworkEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch(Exception ex) {}

        if(!mNetworkEnabled && !mGpsEnabled) {
            // notify user
            AlertDialog.Builder dialog = new AlertDialog.Builder(this, AppAlertTheme);
            dialog.setMessage(this.getResources().getString(R.string.gps_network_not_enabled));
            dialog.setPositiveButton(this.getResources().getString(R.string.open_location_settings),
                    new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(myIntent);
                }
            });
            dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    finish();
                }
            });
            dialog.show();
        }

        if(!mInternetEnabled){
            // notify user
            AlertDialog.Builder dialog = new AlertDialog.Builder(this, AppAlertTheme);
            dialog.setMessage(this.getResources().getString(R.string.connection_not_enabled));
            dialog.setPositiveButton(this.getResources().getString(R.string.open_connection_settings),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                            Intent myIntent = new Intent(Settings.ACTION_SETTINGS);
                            startActivity(myIntent);
                        }
                    });
            dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    finish();
                }
            });
            dialog.show();
        }

    }

    //Verifica se si è connessi a internet
    public boolean isInternetAvailable() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    //Setta tutto il layout relativo alle tab
    private void setTabLayout(){
        // Crea l'adapter che restituirà un fragment per ognuno delle tre
        // sezioni primarie dell'activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Setta il viewpager con l'adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setOffscreenPageLimit(MAXPAGESINMEMORY);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        mTabLayout = (TabLayout) findViewById(R.id.tabs);
        mTabLayout.setupWithViewPager(mViewPager);

        //Setta le icone con testo per le tab (sono custom)
        setupTabIcons();
    }

    //Verifica se l'utente è la prima volta che gioca, e in caso si provvede a prendere l'username
    private void getUserInfo(){
        PREFERENCE_FILENAME = getString(R.string.filename_pref);
        gameSettings = getSharedPreferences(PREFERENCE_FILENAME, Context.MODE_PRIVATE);
        Boolean first = gameSettings.getBoolean(getString(R.string.first_time_pref), true);
        if(first)
            //Mostra un Dialog per prendere l'user
            showCreateUserDialog();
    }

    //Mostra un dialog per prendere l'user
    private void showCreateUserDialog(){
        FragmentManager fm = getSupportFragmentManager();
        CreateUserDialogFragment editNameDialogFragment =
                CreateUserDialogFragment.newInstance(getString(R.string.create_user_title_dialog));
        editNameDialogFragment.show(fm, getString(R.string.create_user_title_dialog));
    }

    //Setta le icone e il testo per le tab
    private void setupTabIcons() {

        TextView tabOne = (TextView) LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
        tabOne.setText(R.string.tab_one);
        tabOne.setCompoundDrawablesWithIntrinsicBounds(0, mTabIcons[0], 0, 0);
        mTabLayout.getTabAt(0).setCustomView(tabOne);

        TextView tabTwo = (TextView) LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
        tabTwo.setText(R.string.tab_two);
        tabTwo.setCompoundDrawablesWithIntrinsicBounds(0, mTabIcons[1], 0, 0);
        mTabLayout.getTabAt(1).setCustomView(tabTwo);

        TextView tabThree = (TextView) LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
        tabThree.setText(R.string.tab_three);
        tabThree.setCompoundDrawablesWithIntrinsicBounds(0, mTabIcons[2], 0, 0);
        mTabLayout.getTabAt(2).setCustomView(tabThree);
    }

    //Restituisce l'helper al DB (utile per i fragment)
    DataBaseHelper getHelper(){
        return mHelper;
    }

    //Restituisce un riferimento alla MyLocation (utile per activity di gioco)
    Location getLastKnownLocation(){
        return mLastLocation;
    }

    @Override
    //TODO: DA IMPLEMENTARE ANCORA
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    //TODO: DA IMPLEMENTARE ANCORA
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Un {@link FragmentPagerAdapter} che restituisce il fragment corrispondente a
     * una sezione.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        SparseArray<Fragment> registeredFragments = new SparseArray<Fragment>();

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        //Restituisce il fragment per una data posizione
        public Fragment getItem(int position) {
            //Faccio uno switch sulla position (0 game, 1 others, 2 history
            switch (position){
                case 0: return StartGameFragment.newInstance();
                case 1: return OthersFragment.newInstance();
                case 2: return HistoryFragment.newInstance();
                default: return StartGameFragment.newInstance();
            }
        }

        @Override
        //Restituisce il numero di pagine
        public int getCount() {
            // Show 3 total pages.
            return mNTabs;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment fragment = (Fragment) super.instantiateItem(container, position);
            registeredFragments.put(position, fragment);
            return fragment;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            registeredFragments.remove(position);
            super.destroyItem(container, position, object);
        }

        @Override
        //Restituisce il titolo di una pagina data una posizione
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getResources().getString(R.string.tab_one);
                case 1:
                    return getResources().getString(R.string.tab_two);
                case 2:
                    return getResources().getString(R.string.tab_three);
            }
            return null;
        }

        public Fragment getRegisteredFragment(int position) {
            return registeredFragments.get(position);
        }
    }
}
