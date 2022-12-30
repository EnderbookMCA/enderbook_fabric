package com.enderbook.fabric;

import com.enderbook.fabric.gui.APIKeyScreen;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.BookEditScreen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Fabric entrypoint class, also holds more centralised functions for the rest of the mod to use.
 * See:
 *  - {@link EnderbookMod#loggedIn()}
 *  - {@link EnderbookMod#openAPIKeyScreen(BookEditScreen)}
 *  - {@link EnderbookMod#openAPIKeyScreen()}
 */
public class EnderbookMod implements ClientModInitializer {

    static final Logger LOGGER = LoggerFactory.getLogger(EnderbookMod.class);

    /**
     * @return Are we currently logged in to Enderbook?
     */
    public static boolean loggedIn() {
        return false; // TODO
    }

    /**
     * Opens the {@link APIKeyScreen}.
     * @param previous The previous {@link BookEditScreen}.
     */
    public static void openAPIKeyScreen(BookEditScreen previous) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null) {
            LOGGER.debug("Opening API Key screen...");
            client.setScreen(new APIKeyScreen(previous));
        }
    }

    /**
     * Opens the {@link APIKeyScreen}.
     */
    public static void openAPIKeyScreen() {
        openAPIKeyScreen(null);
    }

    /* ------------------------------ Fabric ------------------------------ */

    @Override
    public void onInitializeClient() {
        LOGGER.debug("Reached Enderbook entrypoint!");
    }
}
