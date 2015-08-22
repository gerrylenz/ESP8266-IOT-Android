package com.espressif.esp8266_iot.DataStore;

/**
 * Created by gerry on 22.07.2015.
 */
public class DataStore_Name {

    //private variables
    int _id;
    String _name;

    // Empty constructor
    public DataStore_Name() {

    }

    public DataStore_Name(int id) {
        this._id = id;
    }

    public DataStore_Name(int id, String name) {
        this._id = id;
        this._name = name;
    }

    // getting ID
    public int getID() {
        return this._id;
    }

    // setting id
    public void setID(int Value) {
        this._id = Value;
    }

    // getting name
    public String getName() {
        return this._name;
    }

    // setting name
    public void setName(String Value) {
        this._name = Value;
    }

}