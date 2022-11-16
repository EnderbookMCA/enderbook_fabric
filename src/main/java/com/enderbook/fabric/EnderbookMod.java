package com.enderbook.fabric;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Fabric entrypoint class.
 */
public class EnderbookMod implements ClientModInitializer {

    static final Logger LOGGER = LoggerFactory.getLogger(EnderbookMod.class);

    @Override
    public void onInitializeClient() {
        LOGGER.debug("Reached Enderbook entrypoint!");
    }
}
