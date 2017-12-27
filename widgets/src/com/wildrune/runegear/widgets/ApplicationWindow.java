package com.wildrune.runegear.widgets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.wildrune.runegear.widgets.util.WindowWorker;

/**
 * @author Mark van der Wal
 * @web www.markvanderwal.nl
 * @since 06/08/17
 */
public class ApplicationWindow extends Table {
    private final Stage applicationStage;
    private final WindowWorker windowWorker;
    private boolean isMovable = true;
    private boolean isResizable = true;
    private boolean drawTitleBar = true;
    private Label titleLabel;
    private Table titleTable;
    private ImageButton maximizeButton;

    private enum DragDirection{LEFT, RIGHT, UP, DOWN, NONE};
    private DragDirection dragDirection = DragDirection.NONE;

    public ApplicationWindow(String title, Skin skin, WindowWorker windowWorker) {
        if (title == null) throw new IllegalArgumentException("title cannot be null.");
        applicationStage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(applicationStage);
        Window.WindowStyle style = skin.get(Window.WindowStyle.class);
        this.windowWorker = windowWorker;

        // configure titlebar
        layoutTitlebar(title, skin);

        // configure application window
        setSkin(skin);
        setTouchable(Touchable.enabled);
        setClip(true);
        setStyle(style);
        setFillParent(true);
        applicationStage.addActor(this);

        // configure application window moving and resizing
        applicationStage.addListener(new InputListener() {
            @Override
            public boolean mouseMoved(InputEvent event, float x, float y) {
                if(windowWorker.getWindowState() == WindowWorker.WindowState.MAXIMIZED) return true;
                float width = getWidth(), height = getHeight();
                float padLeft = getPadLeft(), padBottom = getPadBottom(), padRight = getPadRight();

                if (isResizable) {
                    if (x <= padLeft) {
                        dragDirection = DragDirection.LEFT;
                        Gdx.graphics.setSystemCursor(Cursor.SystemCursor.HorizontalResize);
                    } else if(x >= width - padRight) {
                        dragDirection = DragDirection.RIGHT;
                        Gdx.graphics.setSystemCursor(Cursor.SystemCursor.HorizontalResize);
                    } else if (y <= padBottom) {
                        dragDirection = DragDirection.DOWN;
                        Gdx.graphics.setSystemCursor(Cursor.SystemCursor.VerticalResize);
                    } else if( y >= height - padBottom) {
                        dragDirection = DragDirection.UP;
                        Gdx.graphics.setSystemCursor(Cursor.SystemCursor.VerticalResize);
                    } else {
                        dragDirection = DragDirection.NONE;
                        Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Arrow);
                    }

                    return false;
                }

                return true;
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Arrow);
            }
        });

        DragListener dragWindowListener = new DragListener() {
            private int startX;
            private int startY;

            @Override
            public void dragStart(InputEvent event, float x, float y, int pointer) {
                startX = (int) x;
                startY = (int) y;
            }

            @Override
            public void drag(InputEvent event, float x, float y, int pointer) {
                if (!isMovable) return;
                int deltaX = (int) x - startX;
                int deltaY = startY - (int) y;

                if (windowWorker.getWindowState() == WindowWorker.WindowState.MAXIMIZED) {
                    int mouseDesktopX = (int) (windowWorker.getPositionX() + x);
                    int oldWindowY = windowWorker.getPositionY();
                    float windowWidthFactor = (float) Math.min(x / Gdx.graphics.getWidth(), 0.8);

                    windowWorker.restore();
                    maximizeButton.setStyle(skin.get("maximize", ImageButton.ImageButtonStyle.class));

                    windowWorker.setPosition((int) (mouseDesktopX - Gdx.graphics.getWidth() * windowWidthFactor) + deltaX,
                            oldWindowY + deltaY);
                } else if(windowWorker.getWindowState() == WindowWorker.WindowState.RESTORED) {
                    windowWorker.setPosition(windowWorker.getPositionX() + deltaX,
                            windowWorker.getPositionY() + deltaY);
                }
            }
        };

        DragListener dragResizeListneer = new DragListener(){
            private int resizeX, resizeY;
            private int dragX;

            @Override
            public void dragStart(InputEvent event, float x, float y, int pointer) {
                resizeX = (int) x;
                resizeY = (int) y;
                dragX = resizeX;
            }

            @Override
            public void drag(InputEvent event, float x, float y, int pointer) {
                if (!isResizable || windowWorker.getWindowState() == WindowWorker.WindowState.MAXIMIZED) return;

                switch(dragDirection) {
                    case LEFT:
                        int translateX = (int) x - resizeX;

                        Gdx.graphics.setWindowedMode(Gdx.graphics.getWidth() - translateX, Gdx.graphics.getHeight());
                        windowWorker.setPosition(windowWorker.getPositionX() - dragX + (int) x, windowWorker.getPositionY());
                        break;
                    case RIGHT:
                        translateX = (int) x - resizeX;

                        Gdx.graphics.setWindowedMode(Gdx.graphics.getWidth() + translateX, Gdx.graphics.getHeight());
                        resizeX = (int) x;
                        break;
                    case UP:
                        int amount = resizeY - (int) y;

                        resizeY = (int) y;


                        Gdx.graphics.setWindowedMode(Gdx.graphics.getWidth(), Gdx.graphics.getHeight() - amount);
                        windowWorker.setPosition(windowWorker.getPositionX(), windowWorker.getPositionY() + amount);
                        break;
                    case DOWN:
                        int oldHeight = Gdx.graphics.getHeight();
                        int translateY = resizeY - (int) y;

                        Gdx.graphics.setWindowedMode(Gdx.graphics.getWidth(), Gdx.graphics.getHeight() + translateY);
                        resizeY = (int) y - oldHeight + Gdx.graphics.getHeight();
                        break;
                }
            }
        };

        dragResizeListneer.setTapSquareSize(0.0f);
        dragWindowListener.setTapSquareSize(0.0f);
        addListener(dragResizeListneer);
        titleTable.addListener(dragWindowListener);
    }

    private void layoutTitlebar(String title, Skin skin) {
        Window.WindowStyle style = skin.get(Window.WindowStyle.class);
        titleLabel = new Label(title, new Label.LabelStyle(style.titleFont, style.titleFontColor));
        titleLabel.setEllipsis(true);
        titleTable = new Table() {
            public void draw(Batch batch, float parentAlpha) {
                if (drawTitleBar) super.draw(batch, parentAlpha);
            }
        };
        ImageButton minimizeButton = new ImageButton(skin, "minimize");
        maximizeButton = new ImageButton(skin, "maximize");
        ImageButton exitButton = new ImageButton(skin, "close");

        titleTable.add(new Image(skin, "app_icon_32"))
                .maxHeight(20)
                .maxWidth(20).expandY().spaceRight(6);
        titleTable.add(titleLabel).expand().fillX().minWidth(0);
        titleTable.add(minimizeButton).top();
        titleTable.add(maximizeButton).top();
        titleTable.add(exitButton).top();
        addActor(titleTable);

        exitButton.addListener(new EventStoppingClickListener((event, x, y) -> Gdx.app.exit()));
        minimizeButton.addListener(new EventStoppingClickListener((event, x, y) -> windowWorker.minimize()));
        maximizeButton.addListener(new EventStoppingClickListener((event, x, y) -> {
            if (windowWorker.getWindowState() == WindowWorker.WindowState.MAXIMIZED) {
                windowWorker.restore();
                maximizeButton.setStyle(skin.get("maximize", ImageButton.ImageButtonStyle.class));
            } else {
                windowWorker.maximize();
                maximizeButton.setStyle(skin.get("maximize_restore", ImageButton.ImageButtonStyle.class));
            }
        }));
    }

    public void draw(Batch batch, float parentAlpha) {
        Stage stage = applicationStage;
        if (stage.getKeyboardFocus() == null) stage.setKeyboardFocus(this);
        super.draw(batch, parentAlpha);
    }

    protected void drawBackground(Batch batch, float parentAlpha, float x, float y) {
        super.drawBackground(batch, parentAlpha, x, y);

        // Manually draw the title table before clipping is done.
        titleTable.getColor().a = getColor().a;
        float padTop = getPadTop(), padLeft = getPadLeft();
        titleTable.setSize(getWidth() - padLeft - getPadRight(), padTop);
        titleTable.setPosition(padLeft, getHeight() - padTop);
        drawTitleBar = true;
        titleTable.draw(batch, parentAlpha);
        drawTitleBar = false; // Avoid drawing the title table again in drawChildren.
    }

    private void setStyle(Window.WindowStyle style) {
        if (style == null) throw new IllegalArgumentException("style cannot be null.");
        setBackground(style.background);

        if (titleLabel != null) {
            titleLabel.setStyle(new Label.LabelStyle(style.titleFont, style.titleFontColor));
        }
        invalidateHierarchy();
    }

    public void resize(int width, int height) {
        applicationStage.getViewport().update(width, height, true);
    }

    public void tick() {
        Color background = getSkin().getColor("background");
        Gdx.gl.glClear(GL30.GL_COLOR_BUFFER_BIT);
        Gdx.gl.glClearColor(background.r, background.g, background.b, background.a);

        applicationStage.act(Gdx.graphics.getDeltaTime());

        applicationStage.draw();
    }

    public void dispose() {
        applicationStage.dispose();
    }

    public float getPrefWidth() {
        return Math.max(super.getPrefWidth(), titleLabel.getPrefWidth() + getPadLeft() + getPadRight());
    }

    public void setTitle(String title) {
        titleLabel.setText(title);
    }

    public void setMovable(boolean isMovable) {
        this.isMovable = isMovable;
    }

    public void setResizable(boolean isResizable) {
        this.isResizable = isResizable;
    }

    /**
     * This prevents parent widgets to also handle events if they have registered normal listeners
     */
    private class EventStoppingClickListener extends ClickListener {

        OnTitleBarButtonHandler consumer;

        EventStoppingClickListener(OnTitleBarButtonHandler consumer) {
            this.consumer = consumer;
        }

        @Override
        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            event.stop();
            return super.touchDown(event, x, y, pointer, button);
        }

        @Override
        public boolean mouseMoved(InputEvent event, float x, float y) {
            event.stop();
            return super.mouseMoved(event, x, y);
        }

        @Override
        public void clicked(InputEvent event, float x, float y) {
            if (consumer != null) {
                consumer.apply(event, x, y);
            }
        }
    }

    private interface OnTitleBarButtonHandler {

        void apply(InputEvent event, float x, float y);
    }
}
