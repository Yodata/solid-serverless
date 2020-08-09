package io.yodata.ldp.solid.server.aws;

import io.yodata.ldp.solid.server.config.Config;
import io.yodata.ldp.solid.server.config.EnvConfig;
import io.yodata.ldp.solid.server.config.LocalFileConfig;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class Configs {

    private static final Logger log = LoggerFactory.getLogger(Configs.class);

    public static final String DOM_BASE = "reflex.domain.base";

    private static Config cfg;

    public static Config get() {
        if (Objects.isNull(cfg)) {
            Config cfgNew = new EnvConfig();

            String env = cfgNew.find("solid.env").orElse("prod");
            log.info("Environment: {}", env);
            if (StringUtils.equals("dev", env)) {
                log.info("Loading local file config");
                cfgNew = new LocalFileConfig(cfgNew);
            }

            cfg = new AmazonS3Config(cfgNew);
        }

        return cfg;
    }

}
