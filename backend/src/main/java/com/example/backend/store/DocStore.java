package com.example.backend.store;


import java.util.concurrent.ConcurrentHashMap;

public class DocStore {
    public static ConcurrentHashMap<String, String> DOC_TEXTS = new ConcurrentHashMap<>();
}

