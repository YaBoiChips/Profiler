package yaboichips.profilerforge;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.logging.LogUtils;
import net.minecraft.client.*;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.screens.OptionsScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
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
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File PROFILER_DIR = new File(System.getenv("APPDATA") + "/.profiler");
    private static final File PROFILE_FILE = new File(PROFILER_DIR, "profile.json");


    public ProfilerForge() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::onClientSetup);

        modEventBus.addListener(this::commonSetup);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void onClientSetup(final FMLClientSetupEvent event) {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            MinecraftForge.EVENT_BUS.addListener(this::doScreenInit);
        }
    }
    @SubscribeEvent
    public void doScreenInit(ScreenEvent.Init.Pre event){
        if (event.getScreen() instanceof OptionsScreen optionsScreen){
            addButtons(optionsScreen, Minecraft.getInstance().options);

        }
    }

    private void addButtons(OptionsScreen optionsScreen, Options gameOptions) {
        if (!PROFILER_DIR.exists()) {
            PROFILER_DIR.mkdir();
        }

        int buttonX = (optionsScreen.width / Minecraft.getInstance().getWindow().getGuiScaledWidth());
        int buttonY = (optionsScreen.height / Minecraft.getInstance().getWindow().getGuiScaledHeight());

        GridLayout gridLayout = new GridLayout();
        gridLayout.defaultCellSetting().paddingHorizontal(4).paddingBottom(4).alignHorizontallyCenter();
        GridLayout.RowHelper rowHelper = gridLayout.createRowHelper(2);
        // First Button: Load Settings from profile.json
        Button loadSettingsButton = Button.builder(
                Component.literal("↓"), // Button text
                button -> {
                    if (PROFILE_FILE.exists()) {
                        // Load settings from profile.json
                        try (FileReader reader = new FileReader(PROFILE_FILE)) {
                            SettingsProfile profile = GSON.fromJson(reader, SettingsProfile.class);
                            applySettings(profile);
                            Minecraft.getInstance().getToasts().addToast(new SystemToast(SystemToast.SystemToastId.WORLD_BACKUP,
                                    Component.literal("Settings Loaded"),
                                    Component.literal("Your settings have been loaded from profiler.json.")
                            ));
                            System.out.println("Settings loaded from profile.json");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Minecraft.getInstance().getToasts().addToast(new SystemToast(SystemToast.SystemToastId.WORLD_BACKUP,
                                Component.literal("File Not Found"),
                                Component.literal("profiler.json was not found on your computer, try saving first!")
                        ));
                    }
                    optionsScreen.onClose();
                }
        ).size(20, 20).pos(buttonX, buttonY).tooltip(Tooltip.create(Component.literal("Retrieve Minecraft Settings"))).build();

        // Second Button: Save Settings to profile.json
        Button saveSettingsButton = Button.builder(
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
                            gameOptions.glintSpeed().get(),
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
                            gameOptions.operatorItemsTab().get(),
                            gameOptions.getSoundSourceVolume(SoundSource.MASTER),
                            gameOptions.getSoundSourceVolume(SoundSource.MUSIC),
                            gameOptions.getSoundSourceVolume(SoundSource.RECORDS),
                            gameOptions.getSoundSourceVolume(SoundSource.WEATHER),
                            gameOptions.getSoundSourceVolume(SoundSource.BLOCKS),
                            gameOptions.getSoundSourceVolume(SoundSource.HOSTILE),
                            gameOptions.getSoundSourceVolume(SoundSource.NEUTRAL),
                            gameOptions.getSoundSourceVolume(SoundSource.PLAYERS),
                            gameOptions.getSoundSourceVolume(SoundSource.AMBIENT),
                            gameOptions.getSoundSourceVolume(SoundSource.VOICE),
                            gameOptions.showSubtitles().get(),
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
                    Minecraft.getInstance().getToasts().addToast(new SystemToast(SystemToast.SystemToastId.WORLD_BACKUP,
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
        ).size(20, 20).pos(optionsScreen.width - 21, buttonY).tooltip(Tooltip.create(Component.literal("Set New Default Settings"))).build();

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
        gameOptions.operatorItemsTab().set(profile.opTab);


        //volume
        gameOptions.soundSourceVolumes.get(SoundSource.MASTER).set(profile.masterVolume);
        gameOptions.soundSourceVolumes.get(SoundSource.MUSIC).set(profile.musicVolume);
        gameOptions.soundSourceVolumes.get(SoundSource.RECORDS).set(profile.recordVolume);
        gameOptions.soundSourceVolumes.get(SoundSource.WEATHER).set(profile.weatherVolume);
        gameOptions.soundSourceVolumes.get(SoundSource.BLOCKS).set(profile.blockVolume);
        gameOptions.soundSourceVolumes.get(SoundSource.HOSTILE).set(profile.hostileVolume);
        gameOptions.soundSourceVolumes.get(SoundSource.NEUTRAL).set(profile.neutralVolume);
        gameOptions.soundSourceVolumes.get(SoundSource.PLAYERS).set(profile.playerVolume);
        gameOptions.soundSourceVolumes.get(SoundSource.AMBIENT).set(profile.ambientVolume);
        gameOptions.soundSourceVolumes.get(SoundSource.VOICE).set(profile.voiceVolume);

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
        gameOptions.glintSpeed().set(profile.glintSpeed);
        gameOptions.showAutosaveIndicator().set(profile.autosave);
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
        boolean opTab;
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
        double glintSpeed;
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
                               boolean entityShadows, double entityDistance, double fovEffect, double glintSpeed, boolean autosave, int graphicsMode, int viewDistance, boolean fullscreen,
                               double mouseSensitivity, double scrollSensitivity, boolean touchscreen, boolean invertMouse, boolean discreteMouse, boolean rawMouseInput,
                               boolean toggleCrouch, boolean toggleSprint, boolean autoJump, boolean opTab,
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
            this.glintSpeed = glintSpeed;
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
            this.opTab = opTab;
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
    private void clientSetup(final FMLClientSetupEvent event) {
    }
    private void commonSetup(final FMLCommonSetupEvent event) {
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
        }
    }
}
