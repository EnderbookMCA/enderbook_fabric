package com.enderbook.fabric.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.BookEditScreen;
import net.minecraft.client.gui.screen.ingame.BookScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.SelectionManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/**
 * A screen that allows the user to enter their Enderbook API key.
 */
public class APIKeyScreen extends Screen {

    static final Text PROMPT_TEXT = Text.translatable("enderbook.apikey.prompt");
    static final Text LOGIN_TEXT  = Text.translatable("enderbook.apikey.login");

    // Would normally be from BookScreen, BookScreen but both have protected access (and it's nicer to let the compiler
    // inline these values than access them in some other way), if it's changed in the future these will be updated, 
    // hopefully.
    static final int MAX_TEXT_WIDTH  = 114;
    static final int MAX_TEXT_HEIGHT = 128;
    static final int WIDTH           = 192;
    static final int HEIGHT          = 192;

    static final int MAX_API_KEY_LENGTH = 64; // FIXME: Could be moved somewhere else + necessary?

    private final BookEditScreen previous; // Keep track of the previous screen, if necessary

    private String text = ""; // Weird place to put this, I know, just trying to avoid forward references :p
    private String[] lines = null; // So we don't recompute the lines constantly

    private final SelectionManager selection = new SelectionManager(
            () -> text,
            text -> { this.text = text; },
            () -> SelectionManager.getClipboard(client),
            clipboard -> SelectionManager.setClipboard(client, clipboard),
            text -> {
                if (text.length() > MAX_API_KEY_LENGTH) return false; // Valid API key length?
                // Weird unicode characters may cause an issue? Not sure, better safe than sorry though, so check that
                // we don't go over the maximum number of wrapped lines.
                return textRenderer.getWrappedLinesHeight(text, MAX_TEXT_WIDTH) <= MAX_TEXT_HEIGHT;
            }
    );

    private /* final */ ButtonWidget loginButton;
    private /* final */ ButtonWidget cancelButton;

    private int tickCounter = 0;

    /**
     * @param previous The previous {@link BookEditScreen}.
     */
    public APIKeyScreen(BookEditScreen previous) {
        super(Text.of("API Key"));

        this.previous = previous;
    }

    public APIKeyScreen() {
        this(null);
    }

    /* ------------------------------ GUI stuff ------------------------------ */

    @Override
    public void tick() {
        super.tick();
        ++tickCounter;
    }

    @Override
    protected void init() {
        client.keyboard.setRepeatEvents(true);

        // FIXME: Buttons appear to be offset by a small amount?
        loginButton = addDrawableChild(
                new ButtonWidget(width / 2 - 100, WIDTH, HEIGHT / 2, 20, LOGIN_TEXT, button -> login())
        );
        cancelButton = addDrawableChild(
                new ButtonWidget(width / 2 + 2, WIDTH, HEIGHT / 2, 20, ScreenTexts.CANCEL, button -> cancel())
        );

        // TODO: Clipboard API key detection?

        // final String clipboard = SelectionManager.getClipboard(client);
        // if (clipboard != null) { // TODO: API key regex?
        //     text = clipboard;
        // }

        loginButton.active = !text.isEmpty();
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        setFocused(null);

        // Draw book background (taken from the BookEditScreen)
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShaderTexture(0, BookScreen.BOOK_TEXTURE);

        drawTexture(matrices, (width - WIDTH) / 2, 2, 0, 0, WIDTH, HEIGHT);

        // Draw the prompt text
        final int textWidth = textRenderer.getWidth(PROMPT_TEXT);
        if (textWidth > MAX_TEXT_WIDTH) {
            ; // TODO: Might not be necessary, not too sure?
        } else {
            // FIXME: I'm not exactly where this 18.0f comes from, so it would be nice to have something concrete rather
            //        than magic constants :p.
            textRenderer.draw(matrices, PROMPT_TEXT, (width - textWidth) / 2.0f, 18.0f, 0);
        }

        // Draw the inputted text, splitting it into lines first
        if (lines == null) { // Need to work out the lines to draw
            if (!text.isEmpty()) {
                List<String> lines = new ArrayList<>((int)(MAX_TEXT_HEIGHT / 9));
                textRenderer.getTextHandler().wrapLines(
                        text, MAX_TEXT_WIDTH, Style.EMPTY, true,
                        (style, start, end) -> lines.add(text.substring(start, end))
                );
                this.lines = lines.toArray(new String[0]);
            } else {
                lines = new String[0];
            }
        }

        float y = 0.0f;
        for (String line : lines) {
            // FIXME: More magic constants!!!!
            textRenderer.draw(matrices, line, (width - WIDTH) / 2 + 36, y + 32, 0);
            y += 9;
        }

        // Draw the cursor
        if (tickCounter % 12 == 0) {
            final int x = lines.length == 0 ? 0 : textRenderer.getWidth(lines[lines.length - 1]);
            DrawableHelper.fill(matrices, x, (int)y - 1, x + 1, (int)y + 9, 0);
        }

        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        boolean dirty = false; // Has the text changed?

        switch (keyCode) { // https://www.glfw.org/docs/3.3/group__keys.html
            case GLFW.GLFW_KEY_ESCAPE: {
                cancel();
                return true;
            }
            case GLFW.GLFW_KEY_ENTER:
            case GLFW.GLFW_KEY_KP_ENTER: {
                if (!text.isEmpty()) login();
                return true;
            }
            case GLFW.GLFW_KEY_BACKSPACE: {
                selection.delete(-1);
                dirty = true;
                break;
            }
            default: {
                if (Screen.isSelectAll(keyCode)) {
                    selection.selectAll();
                    dirty = true;
                } else if (Screen.isCopy(keyCode)) {
                    selection.copy();
                    dirty = true;
                } else if (Screen.isPaste(keyCode)) {
                    selection.paste();
                    dirty = true;
                } else if (Screen.isCut(keyCode)) {
                    selection.cut();
                    dirty = true;
                } else {
                    // TODO: Moving the cursor
                }

                break;
            }
        }

        if (dirty) {
            loginButton.active = !text.isEmpty(); // Need to update if the login button can be pressed
            lines = null; // Indicate that we need to recalculate the lines
        }

        return dirty; // If the text isn't dirty then we haven't consumed the keypress
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (super.charTyped(chr, modifiers)) return true;
        if (selection.insert(chr)) {
            loginButton.active = !text.isEmpty();
            lines = null;
            return true;
        }
        return false;
    }

    /* ------------------------------ Callbacks ------------------------------ */

    /**
     * Attempts to log in to Enderbook with the provided API key.
     */
    private void login() {
        client.setScreen(null);
        // TODO
    }

    /**
     * Cancels the login and returns to the previous screen.
     */
    private void cancel() {
        client.setScreen(previous);
    }
}
