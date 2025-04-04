package br.com.devd2.meshstorage.helper;

import com.google.gson.Gson;

public class JsonUtil {

    private static final Gson gson = new Gson();

    public static String toJson(Object obj) {
        try {
            return gson.toJson(obj, obj.getClass());
        } catch (Exception e) {
            throw new RuntimeException("Erro ao serializar objeto para JSON", e);
        }
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return gson.fromJson(json, clazz);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao deserializar JSON para objeto", e);
        }
    }

}
