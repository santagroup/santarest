package com.santarest;

import com.google.gson.reflect.TypeToken;
import com.santarest.http.Header;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class CollectionTypes {
    public final static java.lang.reflect.Type TYPE_MAP_WITH_STRING_KEYS = new TypeToken<Map<String, ?>>() {
    }.getType();
    public final static java.lang.reflect.Type TYPE_MAP_WITH_STRINGS = new TypeToken<Map<String, String>>() {
    }.getType();
    public final static java.lang.reflect.Type TYPE_LIST_WITH_HEADER = new TypeToken<List<Header>>() {
    }.getType();
    public final static java.lang.reflect.Type TYPE_COLLECTION_WITH_HEADER = new TypeToken<Collection<Header>>() {
    }.getType();
}
