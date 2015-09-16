/**
 * Created by gerry on 22.07.2015.
 * http://www.androidhive.info/2011/11/android-sqlite-database-tutorial/
 */
package com.espressif.esp8266_iot.DataStore;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHandler_Data extends SQLiteOpenHelper {

    static final String TAG = "DatabaseHandler_Data";

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "db_Data";

    // DataStores table name
    private static final String TABLE_DataStoreS = "tbl_Data";

    // DataStores Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_DEVID = "devid";
    private static final String KEY_ADDRESS = "address";
    private static final String KEY_FUNC = "function";
    private static final String KEY_STATUS = "status";
    private static final String KEY_ACTIV = "activ";

    public DatabaseHandler_Data(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "Database onCreate");

        String CREATE_DataStoreS_TABLE = "CREATE TABLE " + TABLE_DataStoreS + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_DEVID + " TEXT,"
                + KEY_ADDRESS + " TEXT,"
                + KEY_FUNC + " TEXT,"
                + KEY_STATUS + " TEXT,"
                + KEY_ACTIV + " INTEGER"
                + ")";
        db.execSQL(CREATE_DataStoreS_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DataStoreS);

        // Create tables again
        onCreate(db);
    }

    /**
     * All CRUD(Create, Read, Update, Delete) Operations
     */

    public void dropTable() {
        SQLiteDatabase db = this.getWritableDatabase();
        String sql = "drop table " + TABLE_DataStoreS;
            db.execSQL(sql);
    }

    // Adding new Data
    public void addDataStore(DataStore_Data Data) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_ID, Data.getID()); // Data ID
        values.put(KEY_DEVID, Data.getDevID()); // Device ID
        values.put(KEY_ADDRESS, Data.getAddress()); // Data Address
        values.put(KEY_FUNC, Data.getFunction()); // Data Function
        values.put(KEY_STATUS, Data.getStatus()); // Data Status
        values.put(KEY_ACTIV, Data.getisActive()); // Data Active

        // Inserting Row
        db.insert(TABLE_DataStoreS, null, values);
        try {
            db.insertOrThrow(TABLE_DataStoreS, null, values);
        } catch (SQLiteConstraintException e) {
            Log.e(TAG, "Database insert error try update..");
            db.update(TABLE_DataStoreS, values, KEY_ID + " = ?", new String[]{String.valueOf(Data.getID())});
            Log.d(TAG, "Success");
        }
        db.close(); // Closing database connection
    }

    // Getting single DataStore_Data
    public DataStore_Data getData(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor;
        cursor = db.query(TABLE_DataStoreS, new String[]{KEY_ID, KEY_DEVID, KEY_ADDRESS, KEY_FUNC, KEY_STATUS, KEY_ACTIV}, KEY_ID + "=?", new String[]{String.valueOf(id)}, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        DataStore_Data datastore = new DataStore_Data( cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getInt(5) );
        // return DataStore_Data
        return datastore;
    }

    // Getting All DataStores
    public List<DataStore_Data> getAllDataStores() {
        List<DataStore_Data> dataStoreDataList = new ArrayList<DataStore_Data>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_DataStoreS;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                DataStore_Data DataStore_Data = new DataStore_Data();
                DataStore_Data.setID(cursor.getInt(0));
                DataStore_Data.setAddress(cursor.getString(1));
                DataStore_Data.setAddress(cursor.getString(2));
                DataStore_Data.setFunktion(cursor.getString(3));
                DataStore_Data.setStatus(cursor.getString(4));
                DataStore_Data.setisActive(cursor.getInt(5));
                // Adding DataStore_Data to list
                dataStoreDataList.add(DataStore_Data);
            } while (cursor.moveToNext());
        }

        // return DataStore_Data list
        return dataStoreDataList;
    }

    // Updating single Data
    public int updateDataStore(DataStore_Data Data) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_ID, Data.getID()); // Data ID
        values.put(KEY_DEVID, Data.getDevID()); // Data ID
        values.put(KEY_ADDRESS, Data.getAddress()); // Data Name
        values.put(KEY_FUNC, Data.getFunction()); // Data Function
        values.put(KEY_STATUS, Data.getStatus()); // Data Active
        values.put(KEY_ACTIV, Data.getisActive()); // Data Active

        // updating row
        return db.update(TABLE_DataStoreS, values, KEY_ID + " = ?",
                new String[]{String.valueOf(Data.getID())});
    }

    // Deleting single DataStore_Data
    public void deleteDataStore(DataStore_Data Data) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_DataStoreS, KEY_ID + " = ?", new String[]{String.valueOf(Data.getID())});
        db.close();
    }


    // Getting DataStores Count
    public int getDataStoresCount() {
        String countQuery = "SELECT  * FROM " + TABLE_DataStoreS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        cursor.close();

        // return count
        return cursor.getCount();
    }

    public boolean IsDataInDB(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor;
        cursor = db.query(TABLE_DataStoreS, new String[]{KEY_ID}, KEY_ID + "=?", new String[]{String.valueOf(id)}, null, null, null, null);

        if(cursor.getCount() <= 0){
            cursor.close();
            return false;
        }
        cursor.close();
        return true;
    }

}