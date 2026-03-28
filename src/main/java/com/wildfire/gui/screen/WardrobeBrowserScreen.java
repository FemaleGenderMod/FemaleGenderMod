package com.wildfire.gui.screen;

import com.wildfire.gui.GuiUtils;
import com.wildfire.gui.WildfireButton;
import com.wildfire.main.Gender;
import com.wildfire.main.entitydata.PlayerConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.Calendar;
import java.util.Objects;
import java.util.UUID;

@OnlyIn(Dist.CLIENT)
public class WardrobeBrowserScreen extends BaseWildfireScreen {
    private static final ResourceLocation BACKGROUND_FEMALE = ResourceLocation.fromNamespaceAndPath("wildfire_gender", "textures/gui/wardrobe_bg2.png");
    private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath("wildfire_gender", "textures/gui/wardrobe_bg3.png");
    private static final ResourceLocation TXTR_RIBBON = ResourceLocation.fromNamespaceAndPath("wildfire_gender", "textures/bc_ribbon.png");
    private static final UUID CREATOR_UUID = UUID.fromString("23b6feed-2dfe-4f2e-9429-863fd4adb946");
    private static final boolean isBreastCancerAwarenessMonth = Calendar.getInstance().get(Calendar.MONTH) == Calendar.OCTOBER; // Calendar.OCTOBER is 9

    public WardrobeBrowserScreen(Screen parent, UUID uuid) {
        super(Component.translatable("wildfire_gender.wardrobe.title"), parent, uuid);
    }

    @Override
    protected void init() {
        super.init();
        int y = this.height / 2;
        PlayerConfig plr = Objects.requireNonNull(this.getPlayer(), "getPlayer()");

        // Botón para cambiar el género
        this.addRenderableWidget(new WildfireButton(this.width / 2 - 42, y - 52, 158, 20, this.getGenderLabel(plr.getGender()), (button) -> {
            Gender nextGender = switch (plr.getGender()) {
                case MALE -> Gender.FEMALE;
                case FEMALE -> Gender.OTHER;
                case OTHER -> Gender.MALE;
                default -> throw new MatchException(null, null);
            };

            if (plr.updateGender(nextGender)) {
                button.setMessage(this.getGenderLabel(nextGender));
                PlayerConfig.saveGenderInfo(plr);
                // rebuildWidgets() no existe directo en 1.21.1, usamos init() o clearWidgets()
                this.clearWidgets();
                this.init();
            }
        }));

        // Botón de apariencia (Solo si puede tener pechos)
        if (plr.getGender().canHaveBreasts()) {
            this.addRenderableWidget(new WildfireButton(this.width / 2 - 42, y - 32, 158, 20, Component.translatable("wildfire_gender.appearance_settings.title").append("..."), (button) -> {
                Minecraft.getInstance().setScreen(new WildfireBreastCustomizationScreen(this, this.playerUUID));
            }));
        }

        // Botón de opciones extra (depende del género para su posición Y)
        this.addRenderableWidget(new WildfireButton(this.width / 2 - 42, y - (plr.getGender().canHaveBreasts() ? 12 : 32), 158, 20, Component.translatable("wildfire_gender.char_settings.title").append("..."), (button) -> {
            Minecraft.getInstance().setScreen(new WildfireCharacterSettingsScreen(this, this.playerUUID));
        }));

        // Botón de cerrar "X"
        this.addRenderableWidget(new WildfireButton(this.width / 2 + 111, y - 63, 9, 9, Component.literal("X"), (button) -> {
            Minecraft.getInstance().setScreen(this.parent);
        }));
    }

    private Component getGenderLabel(Gender gender) {
        return Component.translatable("wildfire_gender.label.gender").append(" - ").append(gender.getDisplayName());
    }

    @Override
    public void renderBackground(GuiGraphics ctx, int mouseX, int mouseY, float delta) {
        super.renderBackground(ctx, mouseX, mouseY, delta);
        PlayerConfig plr = this.getPlayer();
        if (plr != null) {
            ResourceLocation backgroundTexture = plr.getGender().canHaveBreasts() ? BACKGROUND_FEMALE : BACKGROUND;
            ctx.blit(backgroundTexture, (this.width - 248) / 2, (this.height - 134) / 2, 0, 0, 248, 156);

            if (this.minecraft != null && this.minecraft.level != null) {
                int xP = this.width / 2 - 82;
                int yP = this.height / 2 + 40;
                Player ent = this.minecraft.level.getPlayerByUUID(this.playerUUID);

                if (ent != null) {
                    ctx.enableScissor(xP - 35, yP - 93, xP + 35, yP + 6);
                    GuiUtils.drawEntityOnScreen(ctx, xP, yP, 45, (float) (xP - mouseX), (float) (yP - 76 - mouseY), ent);
                    ctx.disableScissor();
                }
            }
        }
    }

    @Override
    public void render(GuiGraphics ctx, int mouseX, int mouseY, float delta) {
        // En 1.21.1, renderBackground se debe llamar manualmente dentro de render si lo sobreescribes
        this.renderBackground(ctx, mouseX, mouseY, delta);

        int x = this.width / 2;
        int y = this.height / 2;

        ctx.drawString(this.font, this.title, x - 118, y - 62, 4473924, false);

        if (this.minecraft != null && this.minecraft.player != null) {
            boolean withCreator = this.minecraft.player.connection.getOnlinePlayers().stream().anyMatch((playerInfo) -> playerInfo.getProfile().getId().equals(CREATOR_UUID));
            if (withCreator) {
                int creatorY = y + 65;
                if (isBreastCancerAwarenessMonth) {
                    creatorY += 30;
                }
                GuiUtils.drawCenteredText(ctx, this.font, Component.translatable("wildfire_gender.label.with_creator"), this.width / 2, creatorY, 16711935);
            }
        }

        if (isBreastCancerAwarenessMonth) {
            int bcaY = y - 45;
            ctx.fill(x - 159, bcaY + 106, x + 159, bcaY + 136, 1426063360);
            ctx.drawString(this.font, Component.translatable("wildfire_gender.cancer_awareness.title").withStyle(ChatFormatting.BOLD, ChatFormatting.ITALIC), this.width / 2 - 148, bcaY + 117, 16777215);
            ctx.blit(TXTR_RIBBON, x + 130, bcaY + 109, 26, 26, 0.0F, 0.0F, 20, 20, 20, 20);
        }

        // Importante: llama al super para que se dibujen los botones
        super.render(ctx, mouseX, mouseY, delta);
    }
}