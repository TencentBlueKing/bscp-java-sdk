package com.tencent.bscp.helper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.protobuf.ProtobufFactory;

import java.io.IOException;

public class JsonHelper {
    private static final ThreadLocal<ObjectMapper> objectMapperThreadLocal =
            ThreadLocal.withInitial(() -> new ObjectMapper().disable(
                DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));

    public static String serialize(Object object) throws JsonProcessingException {
        ObjectMapper objectMapper = objectMapperThreadLocal.get();
        return objectMapper.writeValueAsString(object);
    }

    public static byte[] serializeByte(Object object) throws JsonProcessingException {
        ObjectMapper objectMapper = objectMapperThreadLocal.get();
        return objectMapper.writeValueAsBytes(object);
    }

    public static <T> T deserialize(String json, Class<T> valueType) throws JsonProcessingException {
        ObjectMapper objectMapper = objectMapperThreadLocal.get();
        return objectMapper.readValue(json, valueType);
    }

    public static <T> T deserialize(byte[] json, Class<T> valueType) throws IOException {
        ObjectMapper objectMapper = objectMapperThreadLocal.get();
        return objectMapper.readValue(json, valueType);
    }
}
