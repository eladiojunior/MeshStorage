package br.com.devd2.meshstorageclient.helper;

import br.com.devd2.meshstorage.helper.FileBase64Util;

import java.io.File;
import java.io.FileOutputStream;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;

public class UtilClient {

    /**
     * Recupera a Url padrão de acesso ao Websocket
     * @return Url do Websocket do server.
     */
    public static String getUrlWebsocketServer() {
        return "ws://localhost:8181/server-storage-websocket";
    }

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
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                if (iface.isLoopback() || !iface.isUp()) continue;
                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (addr instanceof Inet4Address && !addr.isLoopbackAddress()) {
                        return addr.getHostAddress();
                    }
                }
            }
            return "IP não encontrado";
        } catch (SocketException e) {
            return "Erro ao recuperar IP";
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

    /**
     * Responsável por gerar um HASH de identificação do client, de forma única.
     * @param serverName - Nome do Servidor de Storage
     * @param storageName - Nome do Storage no Client.
     * @return String com o HASH de identificação.
     */
    public static String generateHashIdClient(String serverName, String storageName) {
        String input = serverName + storageName;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte hashByte : hashBytes) {
                hexString.append(String.format("%02x", hashByte));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Erro ao gerar hash: algorítmo não encontrado.", e);
        }
    }

    /**
     * Montar a estrutura de armazenamento no padrão ano\mes\dia\nome_arquivo.extensao.
     * @param nomeArquivo - Nome do arquivo para armazenamento.
     * @return String no padrão de estrutura do Client.
     */
    public static String mountPathStorage(String nomeArquivo) {
        String ano = nomeArquivo.substring(0, 4);
        String mes = nomeArquivo.substring(4, 6);
        String dia = nomeArquivo.substring(6, 8);
        String dataPath = ano + File.separator + mes + File.separator + dia;
        return Paths.get(dataPath, nomeArquivo).toString();
    }

    /**
     * Responsável pela verificação da estrutura do caminho de armazenamento e
     * caso não exista criar a estruutra de pastas.
     * @param pathFileStorage - Path de armazemento do arquivo físico.
     */
    public static void checkAndCreatePathStorage(String pathFileStorage) {
        File file = new File(pathFileStorage);
        if (!file.getParentFile().exists() && !file.getParentFile().mkdirs())
            throw new RuntimeException("Erro ao criar o estrutura de pastas no storage: " + file.getAbsolutePath());
    }

}