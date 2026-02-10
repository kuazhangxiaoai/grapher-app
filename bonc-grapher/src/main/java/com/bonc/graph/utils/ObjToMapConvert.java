package com.bonc.graph.utils;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

public class ObjToMapConvert {
    public static Map<String, Object> objToMap(Object obj) {
        try {
            return new ObjectMapper().convertValue(obj, HashMap.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}