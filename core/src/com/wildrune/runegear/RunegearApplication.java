package com.wildrune.runegear;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.wildrune.runegear.widgets.ApplicationWindow;
import com.wildrune.runegear.widgets.util.WindowWorker;

public class RunegearApplication extends ApplicationAdapter {

    static final String TITLE = "Runegear 1.x";

    private ApplicationWindow applicationWindow;
    private WindowWorker windowWorker;

    RunegearApplication(WindowWorker windowWorker){
        this.windowWorker = windowWorker;
    }

    @Override
    public void create() {
        Skin editorSkin = new Skin(Gdx.files.internal("assets/skin/skin.json"));
        applicationWindow = new ApplicationWindow(TITLE, editorSkin, windowWorker);
        applicationWindow.setResizable(true);
        applicationWindow.setMovable(true);
//        applicationWindow.setDebug(true);

        final TextField field = new TextField("", editorSkin);

        TextButton button = new TextButton("Change title", editorSkin);
        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                applicationWindow.setTitle(field.getText());
            }

        });

        applicationWindow.add(button).spaceRight(10);
        applicationWindow.add(field);
    }

    @Override
    public void render() {
        applicationWindow.tick();
    }

    @Override
    public void resize(int width, int height) {
        applicationWindow.resize(width, height);
    }

    @Override
    public void dispose() {
        applicationWindow.dispose();
    }
}
