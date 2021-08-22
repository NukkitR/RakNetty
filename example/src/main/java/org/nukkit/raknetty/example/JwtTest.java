package org.nukkit.raknetty.example;

import org.nukkit.raknetty.handler.codec.bedrock.SkinData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JwtTest {

    static final Logger LOGGER = LoggerFactory.getLogger(JwtTest.class);

    public static void main(String[] args) {
        LOGGER.debug("now");
        new SkinData();
        LOGGER.debug("1");
        new SkinData();
        LOGGER.debug("2");
        new SkinData();
        LOGGER.debug("3");
    }
}
