package org.wangyang.gc;

import java.util.HashMap;
import java.util.Map;

public class StaticMap {
    private static final Map<Integer, String> MAP = new HashMap<>();

    public static void main(String[] args) {
        StringBuilder builder = new StringBuilder(String.valueOf(System.currentTimeMillis()));
        for (int i = 0; i < 10000000; i++) {
            builder.append("a");
            MAP.put(i, builder.toString());
        }
    }
}
