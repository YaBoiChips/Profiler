package yaboichips.profiler.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.*;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.OptionsScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import org.spongepowered.include.com.google.gson.Gson;
import org.spongepowered.include.com.google.gson.GsonBuilder;

import java.awt.*;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ProfilerClient implements ClientModInitializer {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File PROFILER_DIR = new File(System.getenv("APPDATA") + "/.profiler");
    private static final File PROFILE_FILE = new File(PROFILER_DIR, "profile.json");

    @Override
    public void onInitializeClient() {
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof OptionsScreen optionsScreen) {
                addButtons(optionsScreen, client.options);
            }
        });
    }

    // Method to add the FOV button to the options screen
    private void addButtons(OptionsScreen optionsScreen, Options gameOptions) {
        if (!PROFILER_DIR.exists()) {
            PROFILER_DIR.mkdir();
        }

        int buttonX = (optionsScreen.width / Minecraft.getInstance().getWindow().getGuiScaledWidth());
        int buttonY = (optionsScreen.height / Minecraft.getInstance().getWindow().getGuiScaledHeight());

        // First Button: Load Settings from profile.json
        Button loadSettingsButton = new Button(
                buttonX, buttonY, 20, 20,
                Component.literal("↓"), // Button text
                button -> {
                    if (PROFILE_FILE.exists()) {
                        // Load settings from profile.json
                        try (FileReader reader = new FileReader(PROFILE_FILE)) {
                            SettingsProfile profile = GSON.fromJson(reader, SettingsProfile.class);
                            applySettings(profile);
                            Minecraft.getInstance().getToasts().addToast(new SystemToast(SystemToast.SystemToastIds.WORLD_BACKUP,
                                    Component.literal("Settings Loaded"),
                                    Component.literal("Your settings have been loaded from profiler.json.")
                            ));
                            System.out.println("Settings loaded from profile.json");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Minecraft.getInstance().getToasts().addToast(new SystemToast(SystemToast.SystemToastIds.WORLD_BACKUP,
                                Component.literal("File Not Found"),
                                Component.literal("profiler.json was not found on your computer, try saving first!")
                        ));
                    }
                    optionsScreen.onClose();
                }
        );

        // Second Button: Save Settings to profile.json
        Button saveSettingsButton = new Button(
                optionsScreen.width - 21, buttonY, 20,20,
                // Button height
                Component.literal("↑"), // Button text
                button -> {
                    // Save current settings to profile.json
                    SettingsProfile profile = new SettingsProfile(
                            gameOptions.fov().get(),
                            gameOptions.enableVsync().get(),
                            gameOptions.guiScale().get(),
                            gameOptions.gamma().get(),
                            gameOptions.particles().get().getId(),
                            gameOptions.framerateLimit().get(),
                            gameOptions.bobView().get(),
                            gameOptions.cloudStatus().get().getId(),
                            gameOptions.entityShadows().get(),
                            gameOptions.entityDistanceScaling().get(),
                            gameOptions.fovEffectScale().get(),
                            gameOptions.showAutosaveIndicator().get(),
                            gameOptions.graphicsMode().get().getId(),
                            gameOptions.renderDistance().get(),
                            gameOptions.fullscreen().get(),
                            gameOptions.sensitivity().get(),
                            gameOptions.mouseWheelSensitivity().get(),
                            gameOptions.touchscreen().get(),
                            gameOptions.invertYMouse().get(),
                            gameOptions.discreteMouseScroll().get(),
                            gameOptions.rawMouseInput().get(),
                            gameOptions.toggleCrouch().get(),
                            gameOptions.toggleSprint().get(),
                            gameOptions.autoJump().get(),
                            gameOptions.sourceVolumes.get(SoundSource.MASTER),
                            gameOptions.sourceVolumes.get(SoundSource.MUSIC),
                            gameOptions.sourceVolumes.get(SoundSource.RECORDS),
                            gameOptions.sourceVolumes.get(SoundSource.WEATHER),
                            gameOptions.sourceVolumes.get(SoundSource.BLOCKS),
                            gameOptions.sourceVolumes.get(SoundSource.HOSTILE),
                            gameOptions.sourceVolumes.get(SoundSource.NEUTRAL),
                            gameOptions.sourceVolumes.get(SoundSource.PLAYERS),
                            gameOptions.sourceVolumes.get(SoundSource.AMBIENT),
                            gameOptions.sourceVolumes.get(SoundSource.VOICE),
                            gameOptions.showSubtitles().get(),
                            gameOptions.keyUp.key.getValue(),
                            gameOptions.keyLeft.key.getValue(),
                            gameOptions.keyDown.key.getValue(),
                            gameOptions.keyRight.key.getValue(),
                            gameOptions.keyJump.key.getValue(),
                            gameOptions.keyShift.key.getValue(),
                            gameOptions.keySprint.key.getValue(),
                            gameOptions.keyInventory.key.getValue(),
                            gameOptions.keySwapOffhand.key.getValue(),
                            gameOptions.keyDrop.key.getValue(),
                            gameOptions.keyUse.key.getValue(),
                            gameOptions.keyAttack.key.getValue(),
                            gameOptions.keyPickItem.key.getValue(),
                            gameOptions.keyChat.key.getValue(),
                            gameOptions.keyPlayerList.key.getValue(),
                            gameOptions.keyCommand.key.getValue(),
                            gameOptions.keyScreenshot.key.getValue(),
                            gameOptions.keyTogglePerspective.key.getValue(),
                            gameOptions.keySmoothCamera.key.getValue(),
                            gameOptions.keyAdvancements.key.getValue()
                    );
                    Minecraft.getInstance().getToasts().addToast(new SystemToast(SystemToast.SystemToastIds.WORLD_BACKUP,
                            Component.literal("Settings Saved"),
                            Component.literal("Your settings have been saved.")
                    ));
                    try (FileWriter writer = new FileWriter(PROFILE_FILE)) {
                        GSON.toJson(profile, writer);
                        System.out.println("Settings saved to profile.json");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    optionsScreen.onClose();

                }
        );
        // Add both buttons to the options screen
        optionsScreen.addRenderableWidget(loadSettingsButton);
        optionsScreen.addRenderableWidget(saveSettingsButton);
    }

    // Method to apply settings to the game options
    private void applySettings(SettingsProfile profile) {

        Options gameOptions = Minecraft.getInstance().options;
        //other settings
        gameOptions.fov().set(profile.fov);
        gameOptions.showSubtitles().set(profile.subtitles);

        //controls
// Assuming profile has integer fields for key bindings
        gameOptions.keyUp.setKey(InputConstants.getKey(profile.keyUp, profile.keyUp));
        gameOptions.keyLeft.setKey(InputConstants.getKey(profile.keyLeft, profile.keyLeft));
        gameOptions.keyDown.setKey(InputConstants.getKey(profile.keyDown, profile.keyDown));
        gameOptions.keyRight.setKey(InputConstants.getKey(profile.keyRight, profile.keyRight));
        gameOptions.keyJump.setKey(InputConstants.getKey(profile.keyJump, profile.keyJump));
        gameOptions.keyShift.setKey(InputConstants.getKey(profile.keySneak, profile.keySneak));
        gameOptions.keySprint.setKey(InputConstants.getKey(profile.keySprint, profile.keySprint));
        gameOptions.keyInventory.setKey(InputConstants.getKey(profile.keyInventory, profile.keyInventory));
        gameOptions.keySwapOffhand.setKey(InputConstants.getKey(profile.keySwapHands, profile.keySwapHands));
        gameOptions.keyDrop.setKey(InputConstants.getKey(profile.keyDrop, profile.keyDrop));
        gameOptions.keyChat.setKey(InputConstants.getKey(profile.keyChat, profile.keyChat));
        gameOptions.keyPlayerList.setKey(InputConstants.getKey(profile.keyPlayerList, profile.keyPlayerList));
        gameOptions.keyCommand.setKey(InputConstants.getKey(profile.keyCommand, profile.keyCommand));
        gameOptions.keyScreenshot.setKey(InputConstants.getKey(profile.keyScreenshot, profile.keyScreenshot));
        gameOptions.keyTogglePerspective.setKey(InputConstants.getKey(profile.keyTogglePerspective, profile.keyTogglePerspective));
        gameOptions.keySmoothCamera.setKey(InputConstants.getKey(profile.keySmoothCamera, profile.keySmoothCamera));
        gameOptions.keyAdvancements.setKey(InputConstants.getKey(profile.keyAdvancements, profile.keyAdvancements));


        gameOptions.sensitivity().set(profile.mouseSensitivity);
        gameOptions.mouseWheelSensitivity().set(profile.scrollSensitivity);
        gameOptions.touchscreen().set(profile.touchscreen);
        gameOptions.invertYMouse().set(profile.invertMouse);
        gameOptions.discreteMouseScroll().set(profile.discreteMouse);
        gameOptions.rawMouseInput().set(profile.rawMouseInput);

        gameOptions.toggleCrouch().set(profile.toggleCrouch);
        gameOptions.toggleSprint().set(profile.toggleSprint);
        gameOptions.autoJump().set(profile.autoJump);


        //volume
        gameOptions.sourceVolumes.put(SoundSource.MASTER, (float)profile.masterVolume);
        gameOptions.sourceVolumes.put(SoundSource.MUSIC, (float)profile.musicVolume);
        gameOptions.sourceVolumes.put(SoundSource.RECORDS, (float)profile.recordVolume);
        gameOptions.sourceVolumes.put(SoundSource.WEATHER, (float)profile.weatherVolume);
        gameOptions.sourceVolumes.put(SoundSource.BLOCKS, (float)profile.blockVolume);
        gameOptions.sourceVolumes.put(SoundSource.HOSTILE, (float)profile.hostileVolume);
        gameOptions.sourceVolumes.put(SoundSource.NEUTRAL, (float)profile.neutralVolume);
        gameOptions.sourceVolumes.put(SoundSource.PLAYERS, (float)profile.playerVolume);
        gameOptions.sourceVolumes.put(SoundSource.AMBIENT, (float)profile.ambientVolume);
        gameOptions.sourceVolumes.put(SoundSource.VOICE, (float)profile.voiceVolume);

        //video settings
        gameOptions.graphicsMode().set(GraphicsStatus.byId(profile.graphicsMode));
        gameOptions.enableVsync().set(profile.vsync);
        gameOptions.guiScale().set(profile.guiScale);
        gameOptions.renderDistance().set(profile.viewDistance);
        gameOptions.fullscreen().set(profile.fullscreen);
        gameOptions.gamma().set(profile.brightness);
        gameOptions.particles().set(ParticleStatus.byId(profile.particles));
        gameOptions.framerateLimit().set(profile.framerateLimit);
        gameOptions.bobView().set(profile.bobView);
        gameOptions.cloudStatus().set(getCloudByID(profile.cloudStatus));
        gameOptions.entityShadows().set(profile.entityShadows);
        gameOptions.entityDistanceScaling().set(profile.entityDistance);
        gameOptions.fovEffectScale().set(profile.fovEffect);
        gameOptions.showAutosaveIndicator().set(profile.autosave);
        Minecraft.getInstance().resizeDisplay();
    }

    public static CloudStatus getCloudByID(int id) {
        for (CloudStatus e : CloudStatus.values()) {
            if (e.getId() == id) {
                return e;
            }
        }
        return CloudStatus.FANCY;
    }

    // Inner class to represent the player's settings in JSON
    private static class SettingsProfile {
        boolean subtitles;
        double musicVolume;
        double recordVolume;
        double weatherVolume;
        double blockVolume;
        double hostileVolume;
        double neutralVolume;
        double playerVolume;
        double ambientVolume;
        double voiceVolume;
        boolean toggleCrouch;
        boolean toggleSprint;
        boolean autoJump;
        boolean touchscreen;
        boolean invertMouse;
        boolean discreteMouse;
        boolean rawMouseInput;
        double scrollSensitivity;
        double mouseSensitivity;
        boolean bobView;
        int cloudStatus;
        boolean entityShadows;
        double entityDistance;
        double fovEffect;
        boolean autosave;
        int framerateLimit;
        int particles;
        double brightness;
        int fov;
        boolean vsync;
        int guiScale;
        double masterVolume;
        int graphicsMode;
        int viewDistance;
        boolean fullscreen;
        public int keyUp;
        public int keyLeft;
        public int keyDown;
        public int keyRight;
        public int keyJump;
        public int keySneak;
        public int keySprint;
        public int keyInventory;
        public int keySwapHands;
        public int keyDrop;
        public int keyUse;
        public int keyAttack;
        public int keyPickItem;
        public int keyChat;
        public int keyPlayerList;
        public int keyCommand;
        public int keyScreenshot;
        public int keyTogglePerspective;
        public int keySmoothCamera;
        public int keyAdvancements;

        public SettingsProfile(int fov, boolean vsync, int guiScale, double gamma, int particles, int framerateLimit, boolean bobView, int cloudStatus,
                               boolean entityShadows, double entityDistance, double fovEffect, boolean autosave, int graphicsMode, int viewDistance, boolean fullscreen,
                               double mouseSensitivity, double scrollSensitivity, boolean touchscreen, boolean invertMouse, boolean discreteMouse, boolean rawMouseInput,
                               boolean toggleCrouch, boolean toggleSprint, boolean autoJump,
                               double masterVolume, double musicVolume, double recordVolume, double weatherVolume, double blockVolume, double hostileVolume, double neutralVolume, double playerVolume, double ambientVolume, double voiceVolume, boolean subtitles,
                               int keyUp, int keyLeft, int keyDown, int keyRight, int keyJump,
                               int keySneak, int keySprint, int keyInventory, int keySwapHands,
                               int keyDrop, int keyUse, int keyAttack, int keyPickItem,
                               int keyChat, int keyPlayerList, int keyCommand,
                               int keyScreenshot, int keyTogglePerspective, int keySmoothCamera,
                               int keyAdvancements) {
            this.fov = fov;
            this.vsync = vsync;
            this.guiScale = guiScale;
            this.brightness = gamma;
            this.particles = particles;
            this.framerateLimit = framerateLimit;
            this.bobView = bobView;
            this.cloudStatus = cloudStatus;
            this.entityShadows = entityShadows;
            this.entityDistance = entityDistance;
            this.fovEffect = fovEffect;
            this.autosave = autosave;
            this.masterVolume = masterVolume;
            this.graphicsMode = graphicsMode;
            this.viewDistance = viewDistance;
            this.fullscreen = fullscreen;
            this.mouseSensitivity = mouseSensitivity;
            this.scrollSensitivity = scrollSensitivity;
            this.touchscreen = touchscreen;
            this.invertMouse = invertMouse;
            this.discreteMouse = discreteMouse;
            this.rawMouseInput = rawMouseInput;
            this.toggleCrouch = toggleCrouch;
            this.toggleSprint = toggleSprint;
            this.autoJump = autoJump;
            this.musicVolume = musicVolume;
            this.recordVolume = recordVolume;
            this.weatherVolume = weatherVolume;
            this.blockVolume = blockVolume;
            this.hostileVolume = hostileVolume;
            this.neutralVolume = neutralVolume;
            this.playerVolume = playerVolume;
            this.ambientVolume = ambientVolume;
            this.voiceVolume = voiceVolume;
            this.subtitles = subtitles;
            this.keyUp = keyUp;
            this.keyLeft = keyLeft;
            this.keyDown = keyDown;
            this.keyRight = keyRight;
            this.keyJump = keyJump;
            this.keySneak = keySneak;
            this.keySprint = keySprint;
            this.keyInventory = keyInventory;
            this.keySwapHands = keySwapHands;
            this.keyDrop = keyDrop;
            this.keyUse = keyUse;
            this.keyAttack = keyAttack;
            this.keyPickItem = keyPickItem;
            this.keyChat = keyChat;
            this.keyPlayerList = keyPlayerList;
            this.keyCommand = keyCommand;
            this.keyScreenshot = keyScreenshot;
            this.keyTogglePerspective = keyTogglePerspective;
            this.keySmoothCamera = keySmoothCamera;
            this.keyAdvancements = keyAdvancements;
        }
    }
}
