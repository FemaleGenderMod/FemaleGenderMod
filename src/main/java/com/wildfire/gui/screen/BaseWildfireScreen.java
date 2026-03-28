package com.wildfire.gui.screen;

import com.wildfire.main.WildfireGender;
import com.wildfire.main.entitydata.PlayerConfig;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@OnlyIn(Dist.CLIENT)
public abstract class BaseWildfireScreen extends Screen {
    protected final UUID playerUUID;
    protected final Screen parent;

    protected BaseWildfireScreen(Component title, Screen parent, UUID uuid) {
        super(title);
        this.parent = parent;
        this.playerUUID = uuid;
    }

    public @Nullable PlayerConfig getPlayer() {
        return WildfireGender.getPlayerById(this.playerUUID);
    }

    @Override
    public boolean isPauseScreen() {
        // Esto evita que el juego se pause al abrir el menú en un mundo para un jugador
        return false;
    }
}