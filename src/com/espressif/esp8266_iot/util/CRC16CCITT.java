/*************************************************************************
 *  Compilation:  javac CRC16CCITT.java
 *  Execution:    java CRC16CCITT s
 *  Dependencies:
 *
 *  Reads in a sequence of bytes and prints out its 16 bit
 *  Cylcic Redundancy Check (CRC-CCIIT 0xFFFF).
 *
 *  1 + x + x^5 + x^12 + x^16 is irreducible polynomial.
 *
 *  % java CRC16-CCITT 123456789
 *  CRC16-CCITT = 29b1
 *
 *************************************************************************/
package com.espressif.esp8266_iot.util;

import android.util.Log;

import android.util.Log;

public class CRC16CCITT {

    static final String TAG = "esp8266";

    // ***************************************************************************
    static public int CRC16(byte buf[], int len) {
        int crc = 0xFFFF;

        for (int pos = 0; pos < len; pos++) {
            crc ^= buf[pos] & 0xff; 				// XOR byte into least sig. byte of crc

            for (int i = 8; i != 0; i--) { 	// Loop over each bit
                if ((crc & 0x0001) != 0) { 		// If the LSB is set
                    crc >>= 1; 									// Shift right and XOR 0xA001
                    crc ^= 0xA001;
                } else { 											// Else LSB is not set
                    crc >>= 1; 									// Just shift right
                }
            }
        }

        return crc;
    }

}