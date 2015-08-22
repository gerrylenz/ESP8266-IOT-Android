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

public class DatabaseHandler_Name extends SQLiteOpenHelper {

    static final String TAG = "DatabaseHandler_Name";

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "db_Name";

    // DataStores table name
    private static final String TABLE_DataStoreS = "tbl_Name";

    // DataStores Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";

    public DatabaseHandler_Name(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "Database onCreate");

        String CREATE_DataStoreS_TABLE = "CREATE TABLE " + TABLE_DataStoreS + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_NAME + " TEXT"
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
    public void addDataStore(DataStore_Name Data) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_ID, Data.getID()); // Data ID
        values.put(KEY_NAME, Data.getName()); // Data Name

        // Inserting Row
        try {
            db.insertOrThrow(TABLE_DataStoreS, null, values);
        } catch (Exception e) {
            Log.e(TAG, "Database insert error try update..");
            db.update(TABLE_DataStoreS, values, KEY_ID + " = ?", new String[]{String.valueOf(Data.getID())});
            Log.d(TAG, "Success");
        }

        db.close(); // Closing database connection
    }

    // Getting single DataStore_Name
    public DataStore_Name getData(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor;
        cursor = db.query(TABLE_DataStoreS, new String[]{KEY_ID, KEY_NAME}, KEY_ID + "=?", new String[]{String.valueOf(id)}, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        DataStore_Name datastore = new DataStore_Name(cursor.getInt(0), cursor.getString(1));
        // return DataStore_Name
        return datastore;
    }

    // Getting All DataStores
    public List<DataStore_Name> getAllDataStores() {
        List<DataStore_Name> dataStoreDataList = new ArrayList<DataStore_Name>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_DataStoreS;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                DataStore_Name Data = new DataStore_Name();
                Data.setID(cursor.getInt(0));
                Data.setName(cursor.getString(1));
                // Adding Data to list
                dataStoreDataList.add(Data);
            } while (cursor.moveToNext());
        }

        // return DataStore_Name list
        return dataStoreDataList;
    }

    // Updating single Data
    public int updateDataStore(DataStore_Name Data) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_ID, Data.getID()); // Data ID
        values.put(KEY_NAME, Data.getName()); // Data Name

        // updating row
        return db.update(TABLE_DataStoreS, values, KEY_ID + " = ?",
                new String[]{String.valueOf(Data.getID())});
    }

    // Deleting single Data
    public void deleteDataStore(DataStore_Name Data) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_DataStoreS, KEY_ID + " = ?",
                new String[]{String.valueOf(Data.getID())});
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
        cursor = db.query(TABLE_DataStoreS, new String[]{KEY_ID, KEY_NAME}, KEY_ID + "=?", new String[]{String.valueOf(id)}, null, null, null, null);

        if(cursor.getCount() <= 0){
            cursor.close();
            return false;
        }
        cursor.close();
        return true;
    }


}
