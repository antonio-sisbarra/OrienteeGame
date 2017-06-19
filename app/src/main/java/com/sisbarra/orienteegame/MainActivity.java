package com.sisbarra.orienteegame;

import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.SQLException;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

import static com.sisbarra.orienteegame.R.style.AppAlertTheme;

//Codice dell'activity principale che contiene le tre sezioni della app
public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks {

    //Numero di tab nell'app
    private static final int mNTabs = 3;
    //Nome delle preferences
    public static String PREFERENCE_FILENAME = null;
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

    //Riferimenti per il caricamento del DB
    private ListView mLstTargets;
    private TargetsListCursorAdapter mAdapter;
    private DataBaseHelper mHelper;

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

    }

    @Override
    protected void onStart() {
        //Verifica dell'username e se siamo al primo utilizzo del gioco -> Dialog
        getUserInfo();

        super.onStart();
    }

    @Override
    protected void onPostResume() {
        //Verifica della connessione a internet e gps
        verifyInternetGps();

        //Registro il listener della posizione
        MyLocation.LocationResult locationResult = new MyLocation.LocationResult(){
            @Override
            public void gotLocation(Location location){
                //Got the location!
                //TODO: IMPLEMENTAZIONE
            }
        };
        MyLocation myLocation = new MyLocation(mLocationManager, mGpsEnabled, mNetworkEnabled, this);
        myLocation.getLocation(this, locationResult);



        super.onPostResume();
    }

    /**
     * Called when a fragment is attached to the activity.
     * <p>
     * <p>This is called after the attached fragment's <code>onAttach</code> and before
     * the attached fragment's <code>onCreate</code> if the fragment has not yet had a previous
     * call to <code>onCreate</code>.</p>
     *
     * @param fragment
     */
    @Override
    public void onAttachFragment(Fragment fragment) {
        if(fragment instanceof HistoryFragment)
            //Inizializza il DB
            loadDb();
        super.onAttachFragment(fragment);
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

    //Inizializza il DB
    private void loadDb(){
        //Inizializza il loader dei target-> chiama onCreateLoader
        getSupportLoaderManager().initLoader(0, null, this);
        //TODO: ALTRI LOADER
    }

    //Verifica se si è connessi a internet e al gps
    private void verifyInternetGps(){
        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

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

    }

    //Setta tutto il layout relativo alle tab
    private void setTabLayout(){
        // Crea l'adapter che restituirà un fragment per ognuno delle tre
        // sezioni primarie dell'activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Setta il viewpager con l'adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
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
     * Instantiate and return a new Loader for the given ID.
     *
     * @param id   The ID whose loader is to be created.
     * @param args Any arguments supplied by the caller.
     * @return Return a new Loader instance that is ready to start loading.
     */
    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        switch (id) {
            //Loader dei target
            case 0: return new android.support.v4.content.CursorLoader(this, null, null, null, null, null) {
                @Override
                //In Background carico il db dei target e il cursor relativo
                public Cursor loadInBackground() {
                    try {
                        mHelper = new DataBaseHelper(getContext());
                        mHelper.createDataBase();
                    } catch (Exception e) {
                        throw new Error(getString(R.string.error_create_db));
                    }
                    try {
                        mHelper.openDataBase();
                    } catch (SQLException sqle) {
                        throw sqle;
                    }

                    return mHelper.getAllTargetCursor();
                }
            };
            default: return null;
        }
    }

    /**
     * Called when a previously created loader has finished its load.  Note
     * that normally an application is <em>not</em> allowed to commit fragment
     * transactions while in this call, since it can happen after an
     * activity's state is saved.  See {@link FragmentManager#beginTransaction()
     * FragmentManager.openTransaction()} for further discussion on this.
     * <p>
     * <p>This function is guaranteed to be called prior to the release of
     * the last data that was supplied for this Loader.  At this point
     * you should remove all use of the old data (since it will be released
     * soon), but should not do your own release of the data since its Loader
     * owns it and will take care of that.  The Loader will take care of
     * management of its data so you don't have to.  In particular:
     * <p>
     * <ul>
     * <li> <p>The Loader will monitor for changes to the data, and report
     * them to you through new calls here.  You should not monitor the
     * data yourself.  For example, if the data is a {@link Cursor}
     * and you place it in a {@link CursorAdapter}, use
     * the { CursorAdapter#CursorAdapter(Context, * Cursor, int)} constructor <em>without</em> passing
     * in either {@link CursorAdapter#FLAG_AUTO_REQUERY}
     * or {@link CursorAdapter#FLAG_REGISTER_CONTENT_OBSERVER}
     * (that is, use 0 for the flags argument).  This prevents the CursorAdapter
     * from doing its own observing of the Cursor, which is not needed since
     * when a change happens you will get a new Cursor throw another call
     * here.
     * <li> The Loader will release the data once it knows the application
     * is no longer using it.  For example, if the data is
     * a {@link Cursor} from a {@link CursorLoader},
     * you should not call close() on it yourself.  If the Cursor is being placed in a
     * {@link CursorAdapter}, you should use the
     * {@link CursorAdapter#swapCursor(Cursor)}
     * method so that the old Cursor is not closed.
     * </ul>
     *
     * @param loader The Loader that has finished.
     * @param data   The data generated by the Loader.
     */
    @Override
    public void onLoadFinished(Loader loader, Object data) {
        switch (loader.getId()) {
            //Caso dei target
            case 0:
                Cursor cursor = (Cursor) data;
                cursor.moveToFirst();
                mAdapter = new TargetsListCursorAdapter(this, cursor, 0);
                StartGameFragment frag = (StartGameFragment) mSectionsPagerAdapter.getRegisteredFragment(0);
                mLstTargets = frag.getListTargets();
                mLstTargets.setAdapter(mAdapter);
                //TODO: CAMBIARE TEXT DELLA SECONDA CARD (PRESENTAZIONE OBIETTIVI)
                break;
            default: return;

        }
    }

    /**
     * Called when a previously created loader is being reset, and thus
     * making its data unavailable.  The application should at this point
     * remove any references it has to the Loader's data.
     *
     * @param loader The Loader that is being reset.
     */
    @Override
    public void onLoaderReset(Loader loader) {

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
                //TODO: DARE RIFERIMENTO AL DB AI FRAGMENT
                case 0: return StartGameFragment.newInstance();
                case 1: return OthersFragment.newInstance("pippo", "cacca");
                case 2: return HistoryFragment.newInstance("Ue");
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
