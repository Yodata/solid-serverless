package io.yodata.ldp.solid.server.config;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.function.Function;

public class Configs {

    private static final Logger log = LoggerFactory.getLogger(Configs.class);

    public static final String DOM_BASE = "reflex.domain.base";

    private static Config setCfg;
    private static Config internalCfg;

    public static void set(Function<Config, Config> f) {
        setCfg = f.apply(get());
    }

    public static Config get() {
        if (Objects.isNull(internalCfg)) {
            internalCfg = new EnvConfig();

            String env = internalCfg.find("solid.env").orElse("prod");
            if (StringUtils.equals("dev", env)) {
                log.info("Loading local file config");
                internalCfg = new LocalFileConfig(internalCfg);
            }
        }

        if (Objects.isNull(setCfg)) {
            return internalCfg;
        } else {
            return setCfg;
        }
    }

}
