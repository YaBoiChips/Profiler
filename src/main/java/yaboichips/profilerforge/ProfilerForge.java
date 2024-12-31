package yaboichips.profilerforge;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.GameSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.OptionsScreen;
import net.minecraft.client.gui.toasts.SystemToast;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.settings.CloudOption;
import net.minecraft.client.settings.GraphicsFanciness;
import net.minecraft.client.settings.ParticleStatus;
import net.minecraft.client.util.InputMappings;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

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
    public void doScreenInit(GuiScreenEvent.InitGuiEvent.Post event) {
        if (event.getGui() instanceof OptionsScreen) {
            addButtons((OptionsScreen) event.getGui(), Minecraft.getInstance().options, event);
        }
    }

    private void addButtons(OptionsScreen optionsScreen, GameSettings gameOptions, GuiScreenEvent.InitGuiEvent.Post event) {
        if (!PROFILER_DIR.exists()) {
            PROFILER_DIR.mkdir();
        }

        int buttonX = (optionsScreen.width / Minecraft.getInstance().getWindow().getGuiScaledWidth());
        int buttonY = (optionsScreen.height / Minecraft.getInstance().getWindow().getGuiScaledHeight());
        // First Button: Load Settings from profile.json
        Button loadSettingsButton = new Button(
                buttonX, buttonY, 20, 20,
                new StringTextComponent("↓"),
                button -> {
                    if (PROFILE_FILE.exists()) {
                        // Load settings from profile.json
                        try (FileReader reader = new FileReader(PROFILE_FILE)) {
                            SettingsProfile profile = GSON.fromJson(reader, SettingsProfile.class);
                            applySettings(profile);
                            Minecraft.getInstance().getToasts().addToast(new SystemToast(SystemToast.Type.WORLD_BACKUP,
                                    new StringTextComponent("Settings Loaded"),
                                    new StringTextComponent("Your settings have been loaded from profiler.json.")
                            ));
                            System.out.println("Settings loaded from profile.json");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Minecraft.getInstance().getToasts().addToast(new SystemToast(SystemToast.Type.WORLD_BACKUP,
                                new StringTextComponent("File Not Found"),
                                new StringTextComponent("profiler.json was not found on your computer, try saving first!")
                        ));
                    }
                    optionsScreen.onClose();
                }
        ) {
            @Override
            public void renderToolTip(MatrixStack matrixStack, int mouseX, int mouseY) {
                if (this.isHovered()) {
                    optionsScreen.renderTooltip(matrixStack, new StringTextComponent("Get Saved Settings"), mouseX, mouseY);
                }
            }
        };

        // Second Button: Save Settings to profile.json
        Button saveSettingsButton = new Button(
                optionsScreen.width - 21, buttonY, 20, 20,
                // Button height
                new StringTextComponent("↑"), // Button text
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
                            gameOptions.sourceVolumes.get(SoundCategory.MASTER),
                            gameOptions.sourceVolumes.get(SoundCategory.MUSIC),
                            gameOptions.sourceVolumes.get(SoundCategory.RECORDS),
                            gameOptions.sourceVolumes.get(SoundCategory.WEATHER),
                            gameOptions.sourceVolumes.get(SoundCategory.BLOCKS),
                            gameOptions.sourceVolumes.get(SoundCategory.HOSTILE),
                            gameOptions.sourceVolumes.get(SoundCategory.NEUTRAL),
                            gameOptions.sourceVolumes.get(SoundCategory.PLAYERS),
                            gameOptions.sourceVolumes.get(SoundCategory.AMBIENT),
                            gameOptions.sourceVolumes.get(SoundCategory.VOICE),
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
                    Minecraft.getInstance().getToasts().addToast(new SystemToast(SystemToast.Type.WORLD_BACKUP,
                            new StringTextComponent("Settings Saved"),
                            new StringTextComponent("Your settings have been saved.")
                    ));
                    try (FileWriter writer = new FileWriter(PROFILE_FILE)) {
                        GSON.toJson(profile, writer);
                        System.out.println("Settings saved to profile.json");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    optionsScreen.onClose();

                }
        ) {
            @Override
            public void renderToolTip(MatrixStack matrixStack, int mouseX, int mouseY) {
                if (this.isHovered()) {
                    optionsScreen.renderTooltip(matrixStack, new StringTextComponent("Save Settings"), mouseX, mouseY);
                }
            }
        };
        // Add both buttons to the options screen
        event.addWidget(loadSettingsButton);
        event.addWidget(saveSettingsButton);
    }

    // Method to apply settings to the game options
    private void applySettings(SettingsProfile profile) {

        GameSettings gameOptions = Minecraft.getInstance().options;
        //other settings
        gameOptions.fov = (profile.fov);
        gameOptions.showSubtitles = (profile.subtitles);

        //controls
// Assuming profile has integer fields for key bindings
        gameOptions.keyUp.setKey(InputMappings.getKey(profile.keyUp, profile.keyUp));
        gameOptions.keyLeft.setKey(InputMappings.getKey(profile.keyLeft, profile.keyLeft));
        gameOptions.keyDown.setKey(InputMappings.getKey(profile.keyDown, profile.keyDown));
        gameOptions.keyRight.setKey(InputMappings.getKey(profile.keyRight, profile.keyRight));
        gameOptions.keyJump.setKey(InputMappings.getKey(profile.keyJump, profile.keyJump));
        gameOptions.keyShift.setKey(InputMappings.getKey(profile.keySneak, profile.keySneak));
        gameOptions.keySprint.setKey(InputMappings.getKey(profile.keySprint, profile.keySprint));
        gameOptions.keyInventory.setKey(InputMappings.getKey(profile.keyInventory, profile.keyInventory));
        gameOptions.keySwapOffhand.setKey(InputMappings.getKey(profile.keySwapHands, profile.keySwapHands));
        gameOptions.keyDrop.setKey(InputMappings.getKey(profile.keyDrop, profile.keyDrop));
        gameOptions.keyChat.setKey(InputMappings.getKey(profile.keyChat, profile.keyChat));
        gameOptions.keyPlayerList.setKey(InputMappings.getKey(profile.keyPlayerList, profile.keyPlayerList));
        gameOptions.keyCommand.setKey(InputMappings.getKey(profile.keyCommand, profile.keyCommand));
        gameOptions.keyScreenshot.setKey(InputMappings.getKey(profile.keyScreenshot, profile.keyScreenshot));
        gameOptions.keyTogglePerspective.setKey(InputMappings.getKey(profile.keyTogglePerspective, profile.keyTogglePerspective));
        gameOptions.keySmoothCamera.setKey(InputMappings.getKey(profile.keySmoothCamera, profile.keySmoothCamera));
        gameOptions.keyAdvancements.setKey(InputMappings.getKey(profile.keyAdvancements, profile.keyAdvancements));


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
        gameOptions.sourceVolumes.put(SoundCategory.MASTER, (float) profile.masterVolume);
        gameOptions.sourceVolumes.put(SoundCategory.MUSIC, (float) profile.musicVolume);
        gameOptions.sourceVolumes.put(SoundCategory.RECORDS, (float) profile.recordVolume);
        gameOptions.sourceVolumes.put(SoundCategory.WEATHER, (float) profile.weatherVolume);
        gameOptions.sourceVolumes.put(SoundCategory.BLOCKS, (float) profile.blockVolume);
        gameOptions.sourceVolumes.put(SoundCategory.HOSTILE, (float) profile.hostileVolume);
        gameOptions.sourceVolumes.put(SoundCategory.NEUTRAL, (float) profile.neutralVolume);
        gameOptions.sourceVolumes.put(SoundCategory.PLAYERS, (float) profile.playerVolume);
        gameOptions.sourceVolumes.put(SoundCategory.AMBIENT, (float) profile.ambientVolume);
        gameOptions.sourceVolumes.put(SoundCategory.VOICE, (float) profile.voiceVolume);

        //video settings
        gameOptions.graphicsMode = (GraphicsFanciness.byId(profile.graphicsMode));
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
        Minecraft.getInstance().resizeDisplay();
    }

    public static CloudOption getCloudByID(int id) {
        switch (id) {
            case 0: {
                return CloudOption.OFF;
            }
            case 1: {
                return CloudOption.FAST;
            }
            default: {
                return CloudOption.FANCY;
            }
        }
    }

    public int getCloudInt(CloudOption status) {
        switch (status) {
            case OFF: {
                return 0;
            }
            case FAST: {
                return 1;
            }
            default: {
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
                               boolean entityShadows, double entityDistance, double fovEffect, int graphicsMode, int viewDistance, boolean fullscreen,
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
