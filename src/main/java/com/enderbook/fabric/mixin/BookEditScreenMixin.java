package com.enderbook.fabric.mixin;

import com.enderbook.fabric.EnderbookMod;

import net.minecraft.client.gui.screen.ingame.BookEditScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * {@link BookEditScreen} mixin so we can intercept user book signing.
 */
@Mixin(BookEditScreen.class)
public class BookEditScreenMixin {

    @Shadow
    private String title;

    @Inject(
        method = "keyPressedSignMode",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screen/ingame/BookEditScreen;finalizeBook(Z)V"
        ),
        cancellable = true
    )
    public void finalizeBook(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> info) {
        if ("Enderbook".equals(title)) {
            // We don't want it to close the screen so we'll tell it to return early after this call
            info.setReturnValue(true);
            info.cancel();

            if (!EnderbookMod.loggedIn()) {
                // Open our API Key screen prompt
                EnderbookMod.openAPIKeyScreen((BookEditScreen)(Object)this);
            }
        }
    }
}
