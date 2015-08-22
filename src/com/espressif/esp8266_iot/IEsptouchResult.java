package com.espressif.esp8266_iot;

import java.net.InetAddress;

public interface IEsptouchResult {
	
	/**
	 * check whether the esp8266_iot task is executed suc
	 * 
	 * @return whether the esp8266_iot task is executed suc
	 */
	boolean isSuc();

	/**
	 * get the device's bssid
	 * 
	 * @return the device's bssid
	 */
	String getBssid();

	/**
	 * check whether the esp8266_iot task is cancelled by user
	 * 
	 * @return whether the esp8266_iot task is cancelled by user
	 */
	boolean isCancelled();

	/**
	 * get the ip address of the device
	 * 
	 * @return the ip device of the device
	 */
	InetAddress getInetAddress();
}
