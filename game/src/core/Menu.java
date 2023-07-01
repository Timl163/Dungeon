package core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import core.utils.Constants;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Optional;

public class Menu extends ScreenAdapter {

    private final Dungeon parent;
    private final Stage stage = new Stage(new ScreenViewport());
    private final Skin skin = new Skin(Gdx.files.internal(Constants.SKIN_FOR_DIALOG));
    private final Table table = new Table();
    private final TextButton buttonSinglePlayer = new TextButton("SinglePlayer", skin);
    private final TextButton buttonMultiPlayer = new TextButton("MultiPlayer", skin);
    private final TextButton buttonStartSession = new TextButton("Start session", skin);
    private final TextButton buttonJoinSession = new TextButton("Join session", skin);
    private final TextButton buttonExit = new TextButton("Exit", skin);
    private final TextField inputHostIpPort = new TextField("", skin);
    private final TextButton buttonJoin = new TextButton("Connect", skin);
    private final Label textInvalidAddress = new Label("", skin);
    private final ArrayList<IMenuScreenObserver> observers = new ArrayList<>();

    private enum MenuType {
        GameModeChoice,
        MultiplayerStartOrJoinSession,
        MultiplayerJoinSession
    }

    public Menu(Dungeon dungeon) {
        this.parent = dungeon;
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);

        table.setFillParent(true);
        table.setDebug(true);
        stage.addActor(table);

        setActiveMenu(MenuType.GameModeChoice);
        registerListeners();
    }

    @Override
    public void render(float delta) {
        // clear the screen ready for next set of images to be drawn
        Gdx.gl.glClearColor(0f, 0f, 0f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // tell our stage to do actions and draw itself
        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();

        // temp debug stuff
        //parent.changeScreen(Box2DTutorial.APPLICATION);
    }

    @Override
    public void resize(int width, int height) {
        // change the stage's viewport when teh screen size is changed
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        // dispose of assets when not needed anymore
        stage.dispose();
    }

    public void addListener(IMenuScreenObserver observer) {
        observers.add(observer);
    }

    public void removeListener(IMenuScreenObserver observer) {
        observers.remove(observer);
    }

    private void registerListeners() {
        buttonExit.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Gdx.app.exit();
            }
        });

        buttonSinglePlayer.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                observers.forEach((IMenuScreenObserver observer) -> observer.onSinglePlayerModeChosen());
            }
        });

        buttonMultiPlayer.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                setActiveMenu(MenuType.MultiplayerStartOrJoinSession);
            }
        });

        buttonStartSession.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                observers.forEach((IMenuScreenObserver observer) -> observer.onMultiPlayerHostModeChosen());
            }
        });

        buttonJoinSession.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                setActiveMenu(MenuType.MultiplayerJoinSession);
            }
        });

        buttonJoin.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                String[] temp = inputHostIpPort.getText().split(":");
                try (Socket socket = new Socket()) {
                    String address = temp[0];
                    int port = Integer.parseInt(temp[1]);
                    socket.connect(new InetSocketAddress(address, port), 1000);
                    socket.close();
                    observers.forEach((IMenuScreenObserver observer) -> observer.onMultiPlayerClientModeChosen(address, port));
                } catch (Exception e) {
                    textInvalidAddress.setVisible(true);
                }
            }
        });
    }

    private void setActiveMenu(MenuType menuType) {
        table.clear();
        switch (menuType) {
            case GameModeChoice -> {
                table.add(buttonSinglePlayer).fillX().uniformX();
                table.row().pad(10, 0, 10, 0);
                table.row();
                table.add(buttonMultiPlayer).fillX().uniformX();
                table.row().pad(10, 0, 10, 0);
                table.row();
                table.add(buttonExit).fillX().uniformX();
            }
            case MultiplayerStartOrJoinSession -> {
                table.add(buttonStartSession).fillX().uniformX();
                table.row().pad(10, 0, 10, 0);
                table.row();
                table.add(buttonJoinSession).fillX().uniformX();
                table.row().pad(10, 0, 10, 0);
                table.row();
                table.add(buttonExit).fillX().uniformX();
            }
            case MultiplayerJoinSession -> {
                table.add(inputHostIpPort).fillX().uniformX();
                table.row().pad(10, 0, 10, 0);
                table.row();
                table.add(buttonJoin).fillX().uniformX();
                table.row().pad(10, 0, 10, 0);
                table.row();
                table.add(buttonExit).fillX().uniformX();
            }
            default -> throw new RuntimeException("Invalid menu type");
        }
    }
}
