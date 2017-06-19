package com.sisbarra.orienteegame;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

//Codice dell'activity principale che contiene le tre sezioni della app
public class MainActivity extends AppCompatActivity {

    //Numero di tab nell'app
    private static final int mNTabs = 3;
    //Nome delle preferences
    public static String PREFERENCE_FILENAME = null;

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

        //TODO: DEVO INIZIALIZZARE QUI IL DB? (MAGARI IN UN THREAD SEPARATO)

        //TODO: Verifica dei permessi necessari (comprese API Google)

        //TODO: Verifica della connessione a internet e gps

    }

    @Override
    protected void onStart() {
        //Verifica dell'username e se siamo al primo utilizzo del gioco -> Dialog
        getUserInfo();

        super.onStart();
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
     * Un {@link FragmentPagerAdapter} che restituisce il fragment corrispondente a
     * una sezione.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

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
                case 2: return HistoryFragment.newInstance("Ue", "ue");
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
    }
}
