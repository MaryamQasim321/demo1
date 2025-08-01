package org.example.demo1.logging;

public class LogUtils {

    public static String success(String message) {
        return "{\"status\": \"success\", \"message\": \"" + message + "\"}";
    }

    public static String warn(String message) {
        return "{\"status\": \"warn\", \"message\": \"" + message + "\"}";
    }
    public static String error(String message, Exception e) {
        return "{\"status\": \"error\", \"message\": \"" + message + "\", \"exception\": \"" + e.getMessage() + "\"}";
    }
    public static String info(String message) {
        return "{\"status\": \"info\", \"message\": \"" + message + "\"}";
    }
    public static String prefix(String prefix) {
        return "[" + prefix + "] ";
    }
}
