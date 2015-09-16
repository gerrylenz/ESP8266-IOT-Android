package com.espressif.esp8266_iot.DataStore;

/**
 * Created by gerry on 22.07.2015.
 */
public class DataStore_Data {

    //private variables
    int _id;
    String _address;
    String _devid;
    String _function;
    String _status;
    Integer _isActive;

    // Empty constructor
    public DataStore_Data() {

    }

    public DataStore_Data(int id) {
        this._id = id;
    }

    public DataStore_Data(int id, String address, String _function, String _status, Integer _isActive) {
        this._id = id;
        this._address = address;
        this._function = _function;
        this._status = _status;
        this._isActive = _isActive;
    }

    public DataStore_Data(int id, String devid, String address, String _function, String _status, Integer _isActive) {
        this._id = id;
        this._devid = devid;
        this._address = address;
        this._function = _function;
        this._status = _status;
        this._isActive = _isActive;
    }

    //****************************************************************
    // getting ID
    public int getID() { return this._id; }
    //****************************************************************
    // setting id
    public void setID(int Value) { this._id = Value; }
    //****************************************************************
    // getting Device ID
    public String getDevID() { return this._devid; }
    //****************************************************************
    // setting Device id
    public void setDevID(String Value) { this._devid = Value; }
    //****************************************************************
    // getting address
    public String getAddress() {
        return this._address;
    }
    //****************************************************************
    // setting address
    public void setAddress(String Value) {
        this._address = Value;
    }
    //****************************************************************
    // getting funtion
    public String getFunction() {
        return this._function;
    }
    //****************************************************************
    // setting funtion
    public void setFunktion(String Value) {
        this._function = Value;
    }
    //****************************************************************
    // getting status
    public String getStatus() { return this._status; }
    //****************************************************************
    // setting status
    public void setStatus(String Value) {
        this._status = Value;
    }
    //****************************************************************
    // getting active
    public Integer getisActive() {
        return this._isActive;
    }
    //****************************************************************
    // setting active
    public void setisActive(Integer Value) {
        this._isActive = Value;
    }
}