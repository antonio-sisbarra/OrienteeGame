package com.sisbarra.orienteegame;

import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Antonio Sisbarra on 16/06/2017.
 * Sotto classe che mi serve per operare con il database dei target
 */

class DataBaseHelper extends SQLiteOpenHelper {

    static String LAT_COLUMN = "lat";
    static String LONG_COLUMN = "long";
    private static String ID_COLUMN = "_id";

    //The Android's default system path of my application database.
    private static String DB_PATH;
    private static String DB_NAME = "orienteegame_database.sqlite";
    private final Context myContext;
    private SQLiteDatabase myDataBase;

    /**
     * Constructor
     * Si tiene un riferimento al Context per accedere alle risorse
     * @param context
     */
    public DataBaseHelper(Context context) throws PackageManager.NameNotFoundException {
        super(context, DB_NAME, null, 1);

        DB_PATH = context.getPackageManager().getPackageInfo(
                context.getPackageName(), 0).applicationInfo.dataDir + "/databases/";
        this.myContext = context;
    }

    /**
     * Crea un db vuoto nel sistema, e lo riscrive con il mio db
     * */
    void createDataBase() throws IOException {

        boolean dbExist = checkDataBase();

        if(!dbExist){
            //Con questo metodo un db vuoto sarà creato nel path di default del sistema
            //dell'app perciò posso sovrascrivere quel db con il mio.
            this.getWritableDatabase();

            try {
                copyDataBase();
            }
            catch (IOException e) {
                throw new Error(myContext.getString(R.string.error_copy_db));
            }
        }
    }

    /**
     * Controlla se il db esiste per evitare ogni volta la ricopiatura all'apertura dell'app.
     * @return true se esiste, false altrimenti
     */
    private boolean checkDataBase(){

        SQLiteDatabase checkDB = null;

        try{
            String myPath = DB_PATH + DB_NAME;
            checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);
        }
        catch(SQLiteException e){
            //db non esiste ancora.
        }

        if(checkDB != null){
            checkDB.close();
            return true;
        }

        return false;
    }

    /**
     * Copia il db dagli asset al db vuoto appena creato nella
     * cartella di sistema, dalla quale si può accedere.
     * Fatto trasferendo bytestream
     * */
    private void copyDataBase() throws IOException {

        //Apre il db locale come inputstream
        InputStream myInput = myContext.getAssets().open(DB_NAME);

        //Path al db vuoto appena creato
        String outFileName = DB_PATH + DB_NAME;

        //Apre il db vuoto come outputstream
        OutputStream myOutput = new FileOutputStream(outFileName);

        //Trasferisce i bytes
        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer)) > 0)
            myOutput.write(buffer, 0, length);

        //Chiude gli stream
        myOutput.flush();
        myOutput.close();
        myInput.close();
    }

    //Apre il db
    void openDataBase() throws SQLException {
        String myPath = DB_PATH + DB_NAME;
        myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);
    }

    /**
     * Chiude ogni oggetto db
     */
    @Override
    public synchronized void close() {
        if(myDataBase != null)
            myDataBase.close();

        super.close();
    }

    /**
     * Non faccio nulla qui, perché il DB già esiste
     */
    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    /**
     * TODO: Non faccio nulla qui
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    Cursor getAllTargetCursor(){

        return myDataBase.query(true, myContext.getString(R.string.name_table_targets),
                new String[]{ID_COLUMN, LAT_COLUMN, LONG_COLUMN}, null, null, null, null, null, null);
    }

    // TODO: Add your public helper methods to access and get content from the database.
    // TODO: You could return cursors by doing "return myDataBase.query(....)" so it'd be easy
    // TODO: to you to create adapters for your views.
}
