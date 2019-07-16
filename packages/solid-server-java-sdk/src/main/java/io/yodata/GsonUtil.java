/*
 * Copyright 2018 YoData, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.yodata;

import com.google.gson.*;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class GsonUtil {

    private static final Gson instance = build();
    private static final Gson instancePretty = buildPretty();

    private static GsonBuilder buildImpl() {
        return new GsonBuilder().disableHtmlEscaping();
    }

    public static Gson buildPretty() {
        return buildImpl().setPrettyPrinting().create();
    }

    public static Gson build() {
        return buildImpl().create();
    }

    public static JsonArray asArray(List<JsonElement> elements) {
        JsonArray a = new JsonArray();
        elements.forEach(a::add);
        return a;
    }

    public static JsonArray asArrayObj(List<? extends Object> elements) {
        return asArray(elements.stream().map(e -> get().toJsonTree(e)).collect(Collectors.toList()));
    }

    public static JsonArray asArray(String... elements) {
        return asArray(Arrays.stream(elements).map(JsonPrimitive::new).collect(Collectors.toList()));
    }

    public static JsonArray asArray(Collection<String> elements) {
        JsonArray a = new JsonArray();
        elements.forEach(a::add);
        return a;
    }

    public static JsonArray asStringArray(Collection<? extends Object> elements) {
        return asArray(elements.stream().map(Object::toString).collect(Collectors.toList()));
    }

    public static <T> List<T> asList(JsonArray a, Class<T> c) {
        List<T> l = new ArrayList<>();
        a.forEach(v -> l.add(GsonUtil.get().fromJson(v, c)));
        return l;
    }

    public static <T> List<T> asList(JsonObject obj, String member, Class<T> c) {
        return asList(getArray(obj, member), c);
    }

    public static JsonObject makeObj(Object o) {
        return instance.toJsonTree(o).getAsJsonObject();
    }

    public static JsonObject makeObj(String key, Object value) {
        return makeObj(key, instance.toJsonTree(value));
    }

    public static JsonObject makeObj(String key, JsonElement el) {
        JsonObject obj = new JsonObject();
        obj.add(key, el);
        return obj;
    }

    public static JsonObject makeObj(Consumer<JsonObject> consumer) {
        JsonObject obj = new JsonObject();
        consumer.accept(obj);
        return obj;
    }

    public static Gson get() {
        return instance;
    }

    public static Gson getPretty() {
        return instancePretty;
    }

    public static String getPrettyForLog(Object o) {
        return System.lineSeparator() + getPretty().toJson(o);
    }

    public static JsonElement parse(String s) {
        try {
            return instance.fromJson(s, JsonElement.class);
        } catch (JsonSyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static JsonElement parse(byte[] s) {
        return parse(new String(s, StandardCharsets.UTF_8));
    }

    public static JsonElement parseBase64(String s) {
        return parse(new String(Base64.getDecoder().decode(s), StandardCharsets.UTF_8));
    }

    public static JsonObject parseObj(byte[] b) {
        try {
            return parse(b).getAsJsonObject();
        } catch (IllegalStateException e) {
            throw new IllegalArgumentException("Not an object");
        }
    }

    public static JsonObject parseObj(String s) {
        try {
            return parse(s).getAsJsonObject();
        } catch (IllegalStateException e) {
            throw new IllegalArgumentException("Not an object");
        }
    }

    public static JsonObject parseObj(InputStream s) {
        return parse(s, JsonObject.class);
    }

    public static Optional<JsonObject> tryParseObj(String s) {
        try {
            return Optional.ofNullable(parseObj(s));
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

    public static <T> T parse(byte[] data, Class<T> type) {
        return parse(new String(data, StandardCharsets.UTF_8), type);
    }

    public static JsonElement parseEl(InputStream s) {
        return parse(s, JsonElement.class);
    }

    public static <T> T parse(InputStream is, Class<T> type) {
        try {
            T v = parse(IOUtils.toString(is, StandardCharsets.UTF_8), type);
            is.close();
            return v;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static <T> T parse(InputStream is, Type type) {
        try {
            return get().fromJson(IOUtils.toString(is, StandardCharsets.UTF_8), type);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static <T> T parse(String data, Class<T> type) {
        return get().fromJson(data, type);
    }

    public static JsonArray getArray(JsonObject obj, String member) {
        return findArray(obj, member).orElseThrow(() -> new IllegalArgumentException("Not an array"));
    }

    public static JsonObject getObj(JsonObject obj, String member) {
        return findObj(obj, member).orElseThrow(() -> new IllegalArgumentException("No object for member " + member));
    }

    public static Optional<String> findString(JsonObject o, String key) {
        return findPrimitive(o, key).map(JsonPrimitive::getAsString);
    }

    public static String getStringOrNull(JsonObject o, String key) {
        JsonElement el = o.get(key);
        if (el != null && el.isJsonPrimitive()) {
            return el.getAsString();
        } else {
            return null;
        }
    }

    public static String getStringOrThrow(JsonObject obj, String member) {
        if (!obj.has(member)) {
            throw new IllegalArgumentException(member + " key is missing");
        }

        return obj.get(member).getAsString();
    }

    public static Optional<JsonElement> findElement(JsonObject o, String key) {
        return Optional.ofNullable(o.get(key));
    }

    public static List<String> findArrayOrString(JsonObject o, String key) {
        List<String> values = new ArrayList<>();
        Optional<JsonElement> elOpt = findElement(o, key);
        if (elOpt.isPresent()) {
            JsonElement el = elOpt.get();
            if (el.isJsonArray()) {
                values.addAll(asList(el.getAsJsonArray(), String.class));
            }
            if (el.isJsonPrimitive()) {
                values.add(el.getAsJsonPrimitive().getAsString());
            }
        }
        return values;
    }

    public static Optional<JsonPrimitive> findPrimitive(JsonObject o, String key) {
        return findElement(o, key).map(el -> el.isJsonPrimitive() ? el.getAsJsonPrimitive() : null);
    }

    public static JsonPrimitive getPrimitive(JsonObject o, String key) {
        return findPrimitive(o, key).orElseThrow(() -> new IllegalArgumentException("No primitive value for key " + key));
    }

    public static Optional<Long> findLong(JsonObject o, String key) {
        return findPrimitive(o, key).map(JsonPrimitive::getAsLong);
    }

    public static long getLong(JsonObject o, String key) {
        return findLong(o, key).orElseThrow(() -> new IllegalArgumentException("No numeric value for key " + key));
    }

    public static Optional<JsonObject> findObj(JsonObject o, String key) {
        if (!o.has(key)) {
            return Optional.empty();
        }

        return Optional.ofNullable(o.getAsJsonObject(key));
    }

    public static Optional<JsonArray> findArray(JsonObject o, String key) {
        return findElement(o, key).filter(JsonElement::isJsonArray).map(JsonElement::getAsJsonArray);
    }

    public static String toJson(Object o) {
        return get().toJson(o);
    }

    public static byte[] toJsonBytes(Object o) {
        return toJson(o).getBytes(StandardCharsets.UTF_8);
    }

}
