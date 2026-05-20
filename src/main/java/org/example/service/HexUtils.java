package org.example.service;

public class HexUtils {
    public static boolean isHexChar(char c) {
        return (c >= '0' && c <= '9') ||
                (c >= 'A' && c <= 'F') ||
                (c >= 'a' && c <= 'f');
    }
}
