package prjgui;

import com.fazecast.jSerialComm.*;



public class SerialNetw {

    static void initSerialNetw() {
    	int  k;

        n_devices = 0;
        name_connect = null;
		// @SuppressWarnings("unchecked")
		SerialPort comPort[] = SerialPort.getCommPorts();
		for (k = 0; k < comPort.length; k++) {
            ser_device[n_devices++] = comPort[k].getSystemPortName();
            if (n_devices >= NDEV_MAX) {
            	break;
            }
		}
    }

    static int getNDev() {
        return n_devices;
    }
    
    static String getDevName(int k) {
        if (k < n_devices) {
            return ser_device[k];
        }
        else {
            return "fail";
        }
    }
    
    static String getConName() {
        return name_connect;
    }

    // 0 = device does not exist, 1 = device exists, 2 = device connected
    static int getDevStat(String d_name) {
        int k, retval = 0;

        for (k = 0; k < n_devices; k++) {
            if (d_name.equals(ser_device[k])) {
                retval = 1;
                if (d_name.equals(name_connect)) {
                    retval = 2;
                }
            }
        }
        return retval;
    }


    void spConnect(String dev_name) {
        try {
        	S_Port = SerialPort.getCommPort(dev_name);
        	S_Port.setComPortParameters(115200, 8, SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY, false);
        	if (S_Port.openPort() == false) {
        		System.out.printf("*** error open %s\n", dev_name);
        	}
            name_connect = dev_name;
            is_connected = true;
        } catch (Exception ex) {
        	System.out.println(ex.toString());
        }
    }

    static void spDisconn(String dev_name) {
    	S_Port.closePort();
        name_connect = null;
        is_connected = false;
    }

    static void SendString(String tstr) {
        if (is_connected == true) {
            S_Port.writeBytes(tstr.getBytes(), tstr.length());
        }
    }


    static String ReadString() {
    	String  recv_str;
        byte[] c_arr;
        int  txsize;

        recv_str = null;
        if (is_connected == true) {
	        try {
	            if ((txsize = S_Port.bytesAvailable()) > 0) {
	            	c_arr = new byte[txsize];
	            	S_Port.readBytes(c_arr, txsize);
	            	recv_str = new String(c_arr);
		            // System.out.printf("ReadString(): [%s] %d\n", recv_str, txsize);
	            }
	        } catch (Exception ex) {
	        	System.out.println(ex.toString());
	        }
        }
        return recv_str;
    }


    static final int NDEV_MAX = 10;
    static private int n_devices;
    static private String[] ser_device = new String[NDEV_MAX];
    static private SerialPort S_Port;
    static private String name_connect;
    static private boolean is_connected = false;
}