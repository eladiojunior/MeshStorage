package br.com.devd2.meshstorageclient.helper;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class UtilClient {

    /**
     * Recupera o Nome da máquina do servidor.
     * @return String do nome da máquina do servidor.
     */
    public static String getMachineName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "Desconhecido";
        }
    }

    /**
     * Recupera o IP do servidor.
     * @return String do IP do servidor.
     */
    public static String getMachineIp() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            return "Desconhecido";
        }
    }

    /**
     * Recupera o nome e versão do sistema operacional do servidor.
     * @return String com o nome e versão do sistema operacional do servidor.
     */
    public static String getOperatingSystem() {
        return System.getProperty("os.name") + " " +
               System.getProperty("os.version") + " (" +
               System.getProperty("os.arch") + ")";
    }

    /**
     * Verifica se o storage é válido e existente no servidor.
     * @param storageName - Local do armazenamento do storage.
     * @return Boolean se o storage é válido e existente.
     */
    public static boolean isStorageValid(String storageName) {
        File storage = new File(storageName);
        return (storage.exists() && storage.isDirectory());
    }

    /**
     * Recupera o total de espaço do storage definido.
     * @param storageName - Local do armazenamento do storage.
     * @return Long com o total do espaço no storage, em MegaBytes-MB, se -1 o storage não é válido.
     */
    public static long getTotalSpaceStorage(String storageName) {
        long totalSpace = -1;
        if (isStorageValid(storageName)) {
            File storage = new File(storageName);
            totalSpace = storage.getTotalSpace() / (1024 * 1024);
        }
        return totalSpace;
    }

    /**
     * Recupera o espaço livre do storage definido.
     * @param storageName - Local do armazenamento do storage.
     * @return Long com o espaço libre no storage, se -1 o storage não é válido.
     */
    public static long getFreeSpaceStorage(String storageName) {
        long freeSpace = -1;
        if (isStorageValid(storageName)) {
            File storage = new File(storageName);
            freeSpace = storage.getFreeSpace() / (1024 * 1024);
        }
        return freeSpace;
    }

}