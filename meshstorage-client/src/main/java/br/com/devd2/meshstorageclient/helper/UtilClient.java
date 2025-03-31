package br.com.devd2.meshstorageclient.helper;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class UtilClient {

    public static String getMachineName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "Desconhecido";
        }
    }

    public static String getMachineIp() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            return "Desconhecido";
        }
    }

    public static String getOperatingSystem() {
        return System.getProperty("os.name") + " " +
                System.getProperty("os.version") + " (" +
                System.getProperty("os.arch") + ")";
    }

    public static boolean isStorageValid(String storageName) {
        File storage = new File(storageName);
        return (storage.exists() && storage.isDirectory());
    }
}
