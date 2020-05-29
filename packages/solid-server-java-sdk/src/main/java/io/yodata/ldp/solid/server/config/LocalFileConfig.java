package io.yodata.ldp.solid.server.config;

import com.google.gson.JsonObject;
import io.yodata.GsonUtil;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class LocalFileConfig implements Config {

    private static final Logger log = LoggerFactory.getLogger(LocalFileConfig.class);

    private Config parent;

    private boolean loaded = false;
    private JsonObject cfg = new JsonObject();

    public LocalFileConfig(Config parent) {
        this.parent = parent;
    }

    public LocalFileConfig() {
        this(new EnvConfig());
    }

    private synchronized void init() {
        if (loaded) {
            return;
        }

        log.info("Loading local config");
        log.info("Current working directory: {}", new File(".").getAbsoluteFile().toString());
        try {
            cfg = GsonUtil.parseObj(FileUtils.readFileToString(new File("solid-serverless.json"), StandardCharsets.UTF_8));
        } catch (IOException e) {
            log.warn("Unable to load local config: {}", e.getMessage());
        }

        loaded = true;
    }

    @Override
    public Optional<String> find(String name) {
        init();

        List<String> keys = new ArrayList<>(Arrays.asList(name.split("\\.")));
        log.info("Keys: {}", GsonUtil.toJson(keys));
        String last = keys.get(keys.size() - 1);
        keys.remove(keys.size() - 1);
        JsonObject c = cfg;
        for (String k : keys) {
            Optional<JsonObject> cNew = GsonUtil.findObj(c, k);
            if (cNew.isPresent()) c = cNew.get();
        }

        Optional<String> value = GsonUtil.findString(c, last);
        if (value.isPresent()) {
            return value;
        }

        return parent.find(name);
    }

}
