package core;

import static com.badlogic.gdx.graphics.GL20.GL_COLOR_BUFFER_BIT;

import static core.utils.logging.LoggerConfig.initBaseLogger;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.ai.pfa.GraphPath;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScalingViewport;

import contrib.entities.EntityFactory;
import contrib.systems.MultiplayerSynchronizationSystem;
import contrib.utils.multiplayer.manager.IMultiplayerClientManagerObserver;
import contrib.utils.multiplayer.manager.IMultiplayerServerManagerObserver;
import contrib.utils.multiplayer.manager.MultiplayerClientManager;
import contrib.utils.multiplayer.manager.MultiplayerServerManager;

import core.components.PositionComponent;
import core.components.UIComponent;
import core.configuration.Configuration;
import core.hud.UITools;
import core.level.Tile;
import core.level.elements.ILevel;
import core.level.generator.postGeneration.WallGenerator;
import core.level.generator.randomwalk.RandomWalkGenerator;
import core.level.utils.Coordinate;
import core.level.utils.LevelElement;
import core.level.utils.LevelSize;
import core.systems.CameraSystem;
import core.systems.DrawSystem;
import core.systems.HudSystem;
import core.systems.LevelSystem;
import core.systems.PlayerSystem;
import core.systems.VelocitySystem;
import core.utils.Constants;
import core.utils.DelayedSet;
import core.utils.IVoidFunction;
import core.utils.Point;
import core.utils.components.MissingComponentException;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Game extends ScreenAdapter
        implements IMultiplayerClientManagerObserver, IMultiplayerServerManagerObserver {

    /* Used for singleton. */
    private static Game INSTANCE;

    /**
     * A Map with each {@link System} in the game.
     *
     * <p>The Key-Value is the Class of the system
     */
    private static final Map<Class<? extends System>, System> SYSTEMS = new LinkedHashMap<>();
    /** All entities that are currently active in the dungeon */
    private static final DelayedSet<Entity> ENTITIES = new DelayedSet<>();

    private static final Logger LOGGER = Logger.getLogger("Game");
    /**
     * The width of the game window in pixels.
     *
     * <p>Manipulating this value will only result in changes before {@link Dungeon#run} was
     * executed.
     */
    private static int WINDOW_WIDTH = 1280;
    /** Part of the pre-run configuration. The height of the game window in pixels. */
    private static int WINDOW_HEIGHT = 720;

    /** Currently used level-size configuration for generating new level */
    private static LevelSize LEVELSIZE = LevelSize.LARGE;
    /**
     * Part of the pre-run configuration.
     * This function will be called at each frame.
     * <p> Use this, if you want to execute some logic outside of a system.</p>
     * <p> Will not replace {@link #onFrame )</p>
     */
    private static IVoidFunction userOnFrame = () -> {};
    /**
     * Part of the pre-run configuration. This function will be called after a level was loaded.
     *
     * <p>Use this, if you want to execute some logic after a level was loaded. For example spawning
     * some Monsters.
     *
     * <p>Will not replace {@link #onLevelLoad}
     */
    private static IVoidFunction userOnLevelLoad = () -> {};

    private static Entity hero;

    private static Stage stage;
    /**
     * Sets {@link #currentLevel} to the new level and removes all entities.
     *
     * <p>Will re-add the hero if he exists.
     */
    private final IVoidFunction onLevelLoad =
            () -> {
                removeAllEntities();
                try {
                    hero().ifPresent(this::placeOnLevelStart);
                } catch (MissingComponentException e) {
                    LOGGER.warning(e.getMessage());
                }
                hero().ifPresent(Game::addEntity);
                userOnLevelLoad.execute();
            };

    private boolean doSetup = true;
    private boolean uiDebugFlag = false;

    private static Optional<GameMode> currentGameMode;
    private static MultiplayerClientManager clientManager;
    private static MultiplayerServerManager serverManager;

    // for singleton
    private Game() {}

    /**
     * @return the currently loaded level
     */
    public static ILevel currentLevel() {
        return LevelSystem.level();
    }

    /**
     * @return a copy of the map that stores all registered {@link System} in the game.
     */
    public static Map<Class<? extends System>, System> systems() {
        return new LinkedHashMap<>(SYSTEMS);
    }

    /** Remove all registered systems from the game. */
    public static void removeAllSystems() {
        SYSTEMS.clear();
    }

    /**
     * The currently set level-Size.
     *
     * <p>This value is used for the generation of the next level.
     *
     * <p>The currently active level can have a different size.
     *
     * @return currently set level-Size.
     */
    public static LevelSize levelSize() {
        return LEVELSIZE;
    }

    /**
     * Set the {@link LevelSize} of the next level.
     *
     * @param levelSize Size of the next level.
     */
    public static void levelSize(LevelSize levelSize) {
        LevelSystem.levelSize(levelSize);
    }

    /**
     * Set the function that will be executed at each frame.
     * <p> Use this, if you want to execute some logic outside of a system.</p>
     * <p> Will not replace {@link #onFrame )</p>
     *
     * @param userFrame function that will be called at each frame.
     * @see IVoidFunction
     */
    public static void userOnFrame(IVoidFunction userFrame) {
        Game.userOnFrame = userFrame;
    }

    /**
     * Set the function that will be executed after a new level was loaded.
     *
     * <p>Use this, if you want to execute some logic after a level was loaded. For example spawning
     * some Monsters.
     *
     * @param userOnLevelLoad the function that will be executed after a new level was loaded
     * @see IVoidFunction
     *     <p>Will not replace {@link #onLevelLoad}
     */
    public static void userOnLevelLoad(IVoidFunction userOnLevelLoad) {
        Game.userOnLevelLoad = userOnLevelLoad;
    }

    /**
     * In the next frame, each system will be informed that the given entity has changes in its
     * Component Collection.
     *
     * @param entity the entity that has changes in its Component Collection
     */
    public static void informAboutChanges(Entity entity) {
        //        LOGGER.info("Entity: " + entity + " informed the Game about component changes.");
    }

    /**
     * The given entity will be added to the game on the next frame.
     *
     * @param entity the entity to add
     * @see DelayedSet
     */
    public static void addEntity(Entity entity) {
        ENTITIES.add(entity);
        LOGGER.info("Entity: " + entity + " will be added to the Game.");
    }

    /**
     * The given entity will be removed from the game on the next frame.
     *
     * @param entity the entity to remove
     * @see DelayedSet
     */
    public static void removeEntity(Entity entity) {
        ENTITIES.remove(entity);
        LOGGER.info("Entity: " + entity + " will be removed from the Game.");
    }

    /**
     * Use this stream if you want to iterate over all currently active entities.
     *
     * @return a stream of all entities currently in the game
     */
    public static Stream<Entity> entityStream() {
        return ENTITIES.stream();
    }

    /**
     * Use this stream if you want to iterate over all currently active GLOBAL entities.
     *
     * @return a stream of all GLOBAL entities.
     */
    public static Stream<Entity> entityStreamGlobal() {
        return clientManager.entityStream();
    }

    /**
     * @return the player character, can be null if not initialized
     * @see Optional
     */
    public static Optional<Entity> hero() {
        return Optional.ofNullable(hero);
    }

    /**
     * Set the reference of the playable character.
     *
     * <p>Be careful: the old hero will not be removed from the game.
     *
     * @param hero the new reference of the hero
     */
    public static void hero(Entity hero) {
        Game.hero = hero;
    }

    /**
     * Load the configuration from the given path. If the configuration has already been loaded, the
     * cached version will be used.
     *
     * @param pathAsString the path to the config file as a string
     * @param klass the class where the ConfigKey fields are located
     * @throws IOException if the file could not be read
     */
    public static void loadConfig(String pathAsString, Class<?>... klass) throws IOException {
        Configuration.loadAndGetConfiguration(pathAsString, klass);
    }

    /** Forces all systems to run. */
    public void resumeSystems() {
        if (SYSTEMS != null) {
            SYSTEMS.forEach(
                    (klass, system) -> {
                        system.run();
                    });
        }
    }

    /** Forces all systems to stop. */
    public void stopSystems() {
        if (SYSTEMS != null) {
            SYSTEMS.forEach(
                    (klass, system) -> {
                        system.stop();
                    });
        }
    }

    /**
     * Has to be called whenever an entity is moved, so that it can be synchronized with global
     * state.
     *
     * @param entityGlobalID Global ID of the entity.
     * @param newPosition New position of the entity.
     * @param xVelocity X velocity while moving.
     * @param yVelocity Y velocity while moving.
     */
    public static void sendMovementUpdate(
            final int entityGlobalID,
            final Point newPosition,
            final float xVelocity,
            final float yVelocity) {
        clientManager.sendMovementUpdate(entityGlobalID, newPosition, xVelocity, yVelocity);
    }

    /** Has to be called to play in single player mode. */
    public void runSinglePlayer() {
        currentGameMode = Optional.of(GameMode.SinglePlayer);
        resumeSystems();
    }

    /**
     * Hosting multiplayer session with the current local game state (level, entities), so that
     * other clients can join.
     */
    public void openToLan() {
        boolean isFailed = true;
        String errorMessage = "Something failed";
        currentGameMode = Optional.of(GameMode.Multiplayer);
        resumeSystems();
        if (doSetup) onSetup();
        try {
            if (hero == null) {
                hero(EntityFactory.newHero());
            }
            if (currentLevel() == null) {
                currentLevel(LevelSize.LARGE);
            }
            //            // Check whether which random port is not already in use and listen to
            // this on
            //            // serverside
            //            // it's unlikely that no port is free but to not run into infinite loop,
            // limit tries.
            int generatePortTriesMaxCount = 20;
            int generatePortTriesCount = 0;
            boolean isServerStarted = false;
            int serverPort = 12345;
//            int serverPort;
//            do {
//                // Create random 5 digit port
//                serverPort = ThreadLocalRandom.current().nextInt(10000, 65535 + 1);
//                isServerStarted = serverManager.start(serverPort);
//            } while ((generatePortTriesCount < generatePortTriesMaxCount) && !isServerStarted);

            isServerStarted = serverManager.start(serverPort);

            if (isServerStarted) {
                if (!clientManager.connect("127.0.0.1", serverPort)) {
                    errorMessage = "Server started but client failed to connect.";
                } else {
                    isFailed = false;
                    /* Need to synchronize toAdd and toRemove into current entities. */
                    updateSystems();
                    clientManager.loadMap(
                            currentLevel(), entityStream().collect(Collectors.toSet()), hero);
                }
            } else {
                errorMessage = "Server can not be started.";
            }
        } catch (Exception ex) {
            final String message = "Multiplayer session failed to start.\n" + ex.getMessage();
        }

        if (isFailed) {
            LOGGER.severe(errorMessage);
            Entity entity = UITools.generateNewTextDialog(errorMessage, "Ok", "Error");
            entity.fetch(UIComponent.class).ifPresent(y -> y.dialog().setVisible(true));
            stopSystems();
        }
    }

    /**
     * Join existing multiplayer session.
     *
     * @param hostAddress Address of host device.
     * @param port Port that offers communication.
     */
    public void joinMultiplayerSession(final String hostAddress, final Integer port) {
        currentGameMode = Optional.of(GameMode.Multiplayer);
        resumeSystems();
        if (doSetup) onSetup();
        try {
            if (hero == null) {
                hero(EntityFactory.newHero());
            }
            clientManager.joinSession(hostAddress, port, hero);
        } catch (Exception ex) {
            final String message = "Multiplayer session failed to join.";
            LOGGER.warning(String.format("%s\n%s", message, ex.getMessage()));
            Entity entity = UITools.generateNewTextDialog(message, "Ok", "Error on join.");
            entity.fetch(UIComponent.class).ifPresent(y -> y.dialog().setVisible(true));
            stopSystems();
        }
    }

    @Override
    public void onMultiplayerSessionJoined(
            final boolean isSucceed,
            final int heroGlobalID,
            final ILevel level,
            final Point initialHeroPosition) {
        if (isSucceed) {
            hero().get().globalID(heroGlobalID);
            PositionComponent heroPositionComponent =
                    Game.hero().get().fetch(PositionComponent.class).orElseThrow();
            heroPositionComponent.position(initialHeroPosition);

            try {
                currentLevel(level);
                updateSystems();
            } catch (Exception ex) {
                final String message = "Session successfully joined but level can not be set.";
                LOGGER.warning(String.format("%s\n%s", message, ex.getMessage()));
                //                Entity entity = UITools.generateNewTextDialog(message, "Ok",
                // "Process failure");
                //                entity.fetch(UIComponent.class).ifPresent(y ->
                // y.dialog().setVisible(true));
                stopSystems();
            }
        } else {
            final String message = "Cannot join multiplayer session";
            LOGGER.warning(message);
            //            Entity entity = UITools.generateNewTextDialog(message, "Ok", "Connection
            // failed.");
            //            entity.fetch(UIComponent.class).ifPresent(y ->
            // y.dialog().setVisible(true));
            stopSystems();
        }
    }

    @Override
    public void onMapLoad(ILevel level) {
        if (level == null) {
            final String message = "Level failed to load. Is null.";
            LOGGER.warning(message);
            //            Entity entity = UITools.generateNewTextDialog(message, "Ok", "No map
            // loaded.");
            //            entity.fetch(UIComponent.class).ifPresent(y ->
            // y.dialog().setVisible(true));
            stopSystems();
            return;
        }

        /* Only set received level for Not-Hosts, because host has set the level. */
        if (!serverManager.isHost(clientManager.clientID())) {
            currentLevel(level);
            updateSystems();
        }
    }

    @Override
    public void onChangeMapRequest() {
        if (serverManager.isHost(clientManager.clientID())) {
            currentLevel(LEVELSIZE);
            // Needed to synchronize toAdd and toRemove entities into currentEntities
            updateSystems();
            clientManager.loadMap(
                    currentLevel(), ENTITIES.stream().collect(Collectors.toSet()), hero);
        }
    }

    @Override
    public void onMultiplayerSessionConnectionLost() {
        final String message = "Disconnected from multiplayer session.";
        LOGGER.info(message);
        //        Entity entity = UITools.generateNewTextDialog(message, "Ok", "Connection lost.");
        //        entity.fetch(UIComponent.class).ifPresent(y -> y.dialog().setVisible(true));
        stopSystems();
    }

    @Override
    public void dispose() {
        clientManager.disconnect();
        serverManager.stop();
    }

    /**
     * Add a {@link System} to the game.
     *
     * <p>If a System is added to the game, the {@link System#execute} method will be called every
     * frame.
     *
     * <p>Additionally, the system will be informed about all new, changed, and removed entities via
     * {@link System#showEntity} or {@link System#removeEntity}.
     *
     * <p>The game can only store one system of each system type.
     *
     * @param system the System to add
     * @return an optional that contains the previous existing system of the given system class, if
     *     one exists
     * @see System
     * @see Optional
     */
    public static Optional<System> addSystem(System system) {
        System currentSystem = SYSTEMS.get(system.getClass());
        SYSTEMS.put(system.getClass(), system);
        LOGGER.info("A new " + system.getClass().getName() + " was added to the game");
        return Optional.ofNullable(currentSystem);
    }

    /**
     * Remove the stored system of the given class from the game.
     *
     * @param system the class of the system to remove
     */
    public static void removeSystem(Class<? extends System> system) {
        SYSTEMS.remove(system);
    }

    /**
     * Remove all entities from the game immediately.
     *
     * <p>This will also remove all entities from each system.
     */
    public static void removeAllEntities() {
        SYSTEMS.values().forEach(System::clearEntities);
        ENTITIES.clear();
        LOGGER.info("All entities will be removed from the game.");
    }

    public static Optional<Stage> stage() {
        return Optional.ofNullable(stage);
    }

    private static void updateStage(Stage x) {
        x.act(Gdx.graphics.getDeltaTime());
        x.draw();
    }

    /**
     * Get the tile at the given point in the level
     *
     * <p>{@link Point#toCoordinate} will be used, to convert the point into a coordinate.
     *
     * @param p Point from where to get the tile
     * @return the tile at the given point.
     */
    public static Tile tileAT(Point p) {
        return currentLevel().tileAt(p);
    }

    /**
     * Get the tile at the given coordinate in the level
     *
     * @param c Coordinate from where to get the tile
     * @return the tile at the given coordinate.
     */
    public static Tile tileAT(Coordinate c) {
        return currentLevel().tileAt(c);
    }

    /**
     * @return a random Tile in the Level
     */
    public static Tile randomTile() {
        return currentLevel().randomTile();
    }

    /**
     * Get the end tile.
     *
     * @return The end tile.
     */
    public static Tile endTile() {
        return currentLevel().endTile();
    }

    /**
     * Get the start tile.
     *
     * @return The start tile.
     */
    public static Tile startTile() {
        return currentLevel().startTile();
    }

    /**
     * Returns the tile the given entity is standing on.
     *
     * @param entity entity to check for.
     * @return tile at the coordinate of the entity
     */
    public static Tile tileAtEntity(Entity entity) {
        return currentLevel().tileAtEntity(entity);
    }

    /**
     * Get a random Tile
     *
     * @param elementType Type of the Tile
     * @return A random Tile of the given Type
     */
    public static Tile randomTile(LevelElement elementType) {
        return currentLevel().randomTile(elementType);
    }

    /**
     * Get the position of a random Tile as Point
     *
     * @return Position of the Tile as Point
     */
    public static Point randomTilePoint() {
        return currentLevel().randomTilePoint();
    }

    /**
     * Get the position of a random Tile as Point
     *
     * @param elementTyp Type of the Tile
     * @return Position of the Tile as Point
     */
    public static Point randomTilePoint(LevelElement elementTyp) {
        return currentLevel().randomTilePoint(elementTyp);
    }

    /**
     * Starts the indexed A* pathfinding algorithm a returns a path
     *
     * <p>Throws an IllegalArgumentException if start or end is non-accessible.
     *
     * @param start Start tile
     * @param end End tile
     * @return Generated path
     */
    public static GraphPath<Tile> findPath(Tile start, Tile end) {
        return currentLevel().findPath(start, end);
    }

    /**
     * Get the Position of the given entity in the level.
     *
     * @param entity Entity to get the current position from (needs a {@link PositionComponent}
     * @return Position of the given entity.
     */
    public static Point positionOf(Entity entity) {
        return currentLevel().positionOf(entity);
    }

    /**
     * Set the current level.
     *
     * <p>This method is for testing and debugging purposes.
     *
     * @param level New level
     */
    public static void currentLevel(ILevel level) {
        LevelSystem levelSystem = (LevelSystem) SYSTEMS.get(LevelSystem.class);
        if (levelSystem != null) levelSystem.loadLevel(level);
        else LOGGER.warning("Can not set Level because levelSystem is null.");
    }

    /**
     * Set the current level.
     *
     * @param levelSize predefined level size of the level to be generated.
     */
    public static void currentLevel(LevelSize levelSize) {
        LevelSystem levelSystem = (LevelSystem) SYSTEMS.get(LevelSystem.class);
        if (levelSystem != null) levelSystem.loadLevel(levelSize);
        else LOGGER.warning("Can not set Level because levelSystem is null.");
    }

    private static void setupStage() {
        stage =
                new Stage(
                        new ScalingViewport(Scaling.stretch, WINDOW_WIDTH, WINDOW_HEIGHT),
                        new SpriteBatch());
        Gdx.input.setInputProcessor(stage);
    }

    /**
     * Main game loop.
     *
     * <p>Redraws the dungeon, updates the entity sets, and triggers the execution of the systems.
     * Will call {@link #onFrame}.
     *
     * @param delta the time since the last loop
     */
    @Override
    public void render(float delta) {
        if (doSetup) onSetup();
        DrawSystem.batch().setProjectionMatrix(CameraSystem.camera().combined);
        onFrame();
        clearScreen();
        updateSystems();
        SYSTEMS.values().stream().filter(System::isRunning).forEach(System::execute);
        CameraSystem.camera().update();
        // stage logic
        Game.stage().ifPresent(Game::updateStage);
    }

    /**
     * Called once at the beginning of the game.
     *
     * <p>Will perform some setup.
     */
    private void onSetup() {
        doSetup = false;
        CameraSystem.camera().zoom = Constants.DEFAULT_ZOOM_FACTOR;
        initBaseLogger();
        createSystems();
        clientManager = new MultiplayerClientManager(this);
        serverManager = new MultiplayerServerManager(this);
        setupStage();
    }

    /**
     * Called at the beginning of each frame, before the entities are updated and the systems are
     * executed.
     *
     * <p>This is the place to add basic logic that isn't part of any system.
     */
    private void onFrame() {
        debugKeys();
        fullscreenKey();
        userOnFrame.execute();
    }

    /** Just for debugging, remove later. */
    private void debugKeys() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.P)) {
            // Text Dialogue (output of information texts)
            newPauseMenu();
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            // toggle UI "debug rendering"
            stage().ifPresent(x -> x.setDebugAll(uiDebugFlag = !uiDebugFlag));
        }
    }

    private void fullscreenKey() {
        if (Gdx.input.isKeyJustPressed(
                core.configuration.KeyboardConfig.TOGGLE_FULLSCREEN.value())) {
            if (!Gdx.graphics.isFullscreen()) {
                Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
            } else {
                Gdx.graphics.setWindowedMode(WINDOW_WIDTH, WINDOW_HEIGHT);
            }
        }
    }

    private Entity newPauseMenu() {
        Entity entity = UITools.generateNewTextDialog("Pause", "Continue", "Pausemenu");
        entity.fetch(UIComponent.class).ifPresent(y -> y.dialog().setVisible(true));
        return entity;
    }

    /** Will update the entity sets of each system and {@link Game#ENTITIES}. */
    private static void updateSystems() {
        for (System system : SYSTEMS.values()) {
            ENTITIES.foreachEntityInAddSet(system::showEntity);
            ENTITIES.foreachEntityInRemoveSet(system::removeEntity);
        }
        ENTITIES.update();
    }

    public static Game getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Game();
        }

        return INSTANCE;
    }

    /**
     * Set the position of the given entity to the position of the level-start.
     *
     * <p>A {@link PositionComponent} is needed.
     *
     * @param entity entity to set on the start of the level, normally this is the hero.
     */
    private void placeOnLevelStart(Entity entity) {
        ENTITIES.add(entity);
        PositionComponent pc =
                entity.fetch(PositionComponent.class)
                        .orElseThrow(
                                () ->
                                        MissingComponentException.build(
                                                entity, PositionComponent.class));
        pc.position(startTile());
    }

    /**
     * Clear the screen. Removes all.
     *
     * <p>Needs to be called before redraw something.
     */
    private void clearScreen() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL_COLOR_BUFFER_BIT);
    }

    /** Create the systems. */
    private void createSystems() {
        addSystem(new CameraSystem());
        addSystem(
                new LevelSystem(
                        DrawSystem.painter(),
                        new WallGenerator(new RandomWalkGenerator()),
                        onLevelLoad));
        addSystem(new DrawSystem());
        addSystem(new VelocitySystem());
        addSystem(new PlayerSystem());
        addSystem(new HudSystem());
        addSystem(new MultiplayerSynchronizationSystem());
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        stage().ifPresent(x -> x.getViewport().update(width, height, true));
    }

    public static boolean isMultiplayerMode() {
        return currentGameMode.isPresent() ? currentGameMode.get() != GameMode.SinglePlayer : false;
    }

    public static void handleHeroOnEndTile() {
        LevelSystem levelSystem = (LevelSystem) SYSTEMS.get(LevelSystem.class);

        if (currentGameMode.isPresent()) {
            if (currentGameMode.get() == GameMode.SinglePlayer) {
                levelSystem.loadLevel(LEVELSIZE);
            } else {
                if (clientManager.isConnectedToSession()) {
                    if (serverManager.isHost(clientManager.clientID())) {
                        // First create new leve locally and then force server to host new map for
                        // all.
                        levelSystem.loadLevel(LevelSize.SMALL);
                        updateSystems();
                        clientManager.loadMap(
                                currentLevel(), entityStream().collect(Collectors.toSet()), hero);
                    } else {
                        // Only host is allowed to load map, so force host to generate new map
                        clientManager.requestNewLevel();
                    }
                } else {
                    LOGGER.severe(
                            "Entity on end tile and Multiplayer mode set but disconnected from session.");
                }
            }
        } else {
            LOGGER.severe(
                    "Entity on end tile but game mode is not set to determine needed action.");
        }
    }
}
