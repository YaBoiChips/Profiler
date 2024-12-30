package yaboichips.profilerforge;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.logging.LogUtils;
import net.minecraft.client.*;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.OptionsScreen;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(ProfilerForge.MODID)
public class ProfilerForge {

    // Define mod id in a common place for everything to reference
    public static final String MODID = "profilerforge";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static final File PROFILER_DIR = new File(System.getenv("APPDATA") + "/.profiler");
    public static final File PROFILE_FILE = new File(PROFILER_DIR, "profile.json");


    public ProfilerForge() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::onClientSetup);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void onClientSetup(final FMLClientSetupEvent event) {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            MinecraftForge.EVENT_BUS.addListener(this::doScreenInit);
        }
    }

    @SubscribeEvent
    public void doScreenInit(ScreenEvent event) {
        if (event.getScreen() instanceof OptionsScreen optionsScreen) {
            addButtons(optionsScreen, Minecraft.getInstance().options);
        }
    }

    private void addButtons(OptionsScreen optionsScreen, Options gameOptions) {
        if (!PROFILER_DIR.exists()) {
            PROFILER_DIR.mkdir();
        }

        int buttonX = (optionsScreen.width / Minecraft.getInstance().getWindow().getGuiScaledWidth());
        int buttonY = (optionsScreen.height / Minecraft.getInstance().getWindow().getGuiScaledHeight());
        // First Button: Load Settings from profile.json
        Button loadSettingsButton = new Button(
                buttonX, buttonY, 20, 20,
                new TextComponent("↓"),
                button -> {
                    if (PROFILE_FILE.exists()) {
                        // Load settings from profile.json
                        try (FileReader reader = new FileReader(PROFILE_FILE)) {
                            SettingsProfile profile = GSON.fromJson(reader, SettingsProfile.class);
                            applySettings(profile);
                            Minecraft.getInstance().getToasts().addToast(new SystemToast(SystemToast.SystemToastIds.WORLD_BACKUP,
                                    new TextComponent("Settings Loaded"),
                                    new TextComponent("Your settings have been loaded from profiler.json.")
                            ));
                            System.out.println("Settings loaded from profile.json");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Minecraft.getInstance().getToasts().addToast(new SystemToast(SystemToast.SystemToastIds.WORLD_BACKUP,
                                new TextComponent("File Not Found"),
                                new TextComponent("profiler.json was not found on your computer, try saving first!")
                        ));
                    }
                    optionsScreen.onClose();
                }
        );

        // Second Button: Save Settings to profile.json
        Button saveSettingsButton = new Button(
                optionsScreen.width - 21, buttonY, 20, 20,
                // Button height
                new TextComponent("↑"), // Button text
                button -> {
                    // Save current settings to profile.json
                    SettingsProfile profile = new SettingsProfile(
                            (int) gameOptions.fov,
                            gameOptions.enableVsync,
                            gameOptions.guiScale,
                            gameOptions.gamma,
                            gameOptions.particles.getId(),
                            gameOptions.framerateLimit,
                            gameOptions.bobView,
                            getCloudInt(gameOptions.renderClouds),
                            gameOptions.entityShadows,
                            gameOptions.entityDistanceScaling,
                            gameOptions.fovEffectScale,
                            gameOptions.showAutosaveIndicator,
                            gameOptions.graphicsMode.getId(),
                            gameOptions.renderDistance,
                            gameOptions.fullscreen,
                            gameOptions.sensitivity,
                            gameOptions.mouseWheelSensitivity,
                            gameOptions.touchscreen,
                            gameOptions.invertYMouse,
                            gameOptions.discreteMouseScroll,
                            gameOptions.rawMouseInput,
                            gameOptions.toggleCrouch,
                            gameOptions.toggleSprint,
                            gameOptions.autoJump,
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
                            gameOptions.showSubtitles,
                            gameOptions.keyUp.getKey().getValue(),
                            gameOptions.keyLeft.getKey().getValue(),
                            gameOptions.keyDown.getKey().getValue(),
                            gameOptions.keyRight.getKey().getValue(),
                            gameOptions.keyJump.getKey().getValue(),
                            gameOptions.keyShift.getKey().getValue(),
                            gameOptions.keySprint.getKey().getValue(),
                            gameOptions.keyInventory.getKey().getValue(),
                            gameOptions.keySwapOffhand.getKey().getValue(),
                            gameOptions.keyDrop.getKey().getValue(),
                            gameOptions.keyUse.getKey().getValue(),
                            gameOptions.keyAttack.getKey().getValue(),
                            gameOptions.keyPickItem.getKey().getValue(),
                            gameOptions.keyChat.getKey().getValue(),
                            gameOptions.keyPlayerList.getKey().getValue(),
                            gameOptions.keyCommand.getKey().getValue(),
                            gameOptions.keyScreenshot.getKey().getValue(),
                            gameOptions.keyTogglePerspective.getKey().getValue(),
                            gameOptions.keySmoothCamera.getKey().getValue(),
                            gameOptions.keyAdvancements.getKey().getValue()
                    );
                    Minecraft.getInstance().getToasts().addToast(new SystemToast(SystemToast.SystemToastIds.WORLD_BACKUP,
                            new TextComponent("Settings Saved"),
                            new TextComponent("Your settings have been saved.")
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
        gameOptions.fov = (profile.fov);
        gameOptions.showSubtitles = (profile.subtitles);

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


        gameOptions.sensitivity = (profile.mouseSensitivity);
        gameOptions.mouseWheelSensitivity = (profile.scrollSensitivity);
        gameOptions.touchscreen = (profile.touchscreen);
        gameOptions.invertYMouse = (profile.invertMouse);
        gameOptions.discreteMouseScroll = (profile.discreteMouse);
        gameOptions.rawMouseInput = (profile.rawMouseInput);

        gameOptions.toggleCrouch = (profile.toggleCrouch);
        gameOptions.toggleSprint = (profile.toggleSprint);
        gameOptions.autoJump = (profile.autoJump);


        //volume
        gameOptions.sourceVolumes.put(SoundSource.MASTER, (float) profile.masterVolume);
        gameOptions.sourceVolumes.put(SoundSource.MUSIC, (float) profile.musicVolume);
        gameOptions.sourceVolumes.put(SoundSource.RECORDS, (float) profile.recordVolume);
        gameOptions.sourceVolumes.put(SoundSource.WEATHER, (float) profile.weatherVolume);
        gameOptions.sourceVolumes.put(SoundSource.BLOCKS, (float) profile.blockVolume);
        gameOptions.sourceVolumes.put(SoundSource.HOSTILE, (float) profile.hostileVolume);
        gameOptions.sourceVolumes.put(SoundSource.NEUTRAL, (float) profile.neutralVolume);
        gameOptions.sourceVolumes.put(SoundSource.PLAYERS, (float) profile.playerVolume);
        gameOptions.sourceVolumes.put(SoundSource.AMBIENT, (float) profile.ambientVolume);
        gameOptions.sourceVolumes.put(SoundSource.VOICE, (float) profile.voiceVolume);

        //video settings
        gameOptions.graphicsMode = (GraphicsStatus.byId(profile.graphicsMode));
        gameOptions.enableVsync = (profile.vsync);
        gameOptions.guiScale = (profile.guiScale);
        gameOptions.renderDistance = (profile.viewDistance);
        gameOptions.fullscreen = (profile.fullscreen);
        gameOptions.gamma = (profile.brightness);
        gameOptions.particles = (ParticleStatus.byId(profile.particles));
        gameOptions.framerateLimit = (profile.framerateLimit);
        gameOptions.bobView = (profile.bobView);
        gameOptions.renderClouds = (getCloudByID(profile.cloudStatus));
        gameOptions.entityShadows = (profile.entityShadows);
        gameOptions.entityDistanceScaling = (float) profile.entityDistance;
        gameOptions.fovEffectScale = (float) profile.fovEffect;
        gameOptions.showAutosaveIndicator = (profile.autosave);
        Minecraft.getInstance().resizeDisplay();
    }

    public static CloudStatus getCloudByID(int id) {
        switch (id) {
            case 0 -> {
                return CloudStatus.OFF;
            }
            case 1 -> {
                return CloudStatus.FAST;
            }
            default -> {
                return CloudStatus.FANCY;
            }
        }
    }

    public int getCloudInt(CloudStatus status) {
        switch (status) {
            case OFF -> {
                return 0;
            }
            case FAST -> {
                return 1;
            }
            default -> {
                return 2;
            }
        }
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
