/*
 * Copyright 2021 Patrik Karlström.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.trixon.cric;

import se.trixon.cric.ui.AppForm;
import se.trixon.cric.ui.OptionsPanel;
import de.jangassen.MenuToolkit;
import java.util.Arrays;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ToolBar;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.apache.commons.lang3.SystemUtils;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionUtils;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.PomInfo;
import se.trixon.almond.util.SystemHelper;
import se.trixon.almond.util.SystemHelperFx;
import se.trixon.almond.util.fx.AboutModel;
import se.trixon.almond.util.fx.AlmondFx;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.dialogs.about.AboutPane;
import se.trixon.almond.util.icons.material.MaterialIcon;

/**
 * JavaFX App
 */
public class App extends Application {

    public static final String APP_TITLE = "CRIC";
    private static final int ICON_SIZE_TOOLBAR = 32;
    private Action mAboutAction;

    private final AlmondFx mAlmondFX = AlmondFx.getInstance();
    private AppForm mAppForm;
    private Action mOptionsAction;
    private BorderPane mRoot;
    private final RunStateManager mRunStateManager = RunStateManager.getInstance();
    private Stage mStage;

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) {
        mStage = stage;
        stage.getIcons().add(new Image(App.class.getResourceAsStream("about_logo.png")));
        mAlmondFX.addStageWatcher(stage, App.class);
        createUI();
        if (SystemUtils.IS_OS_MAC) {
            initMac();
        }
        mStage.setTitle(APP_TITLE);
        mStage.show();

        initAccelerators();
    }

    private void createUI() {
        var addAction = new Action(Dict.ADD.toString(), actionEvent -> {
            mAppForm.profileEdit(null);
        });
        addAction.setGraphic(MaterialIcon._Content.ADD.getImageView(ICON_SIZE_TOOLBAR));
        FxHelper.setTooltip(addAction, new KeyCodeCombination(KeyCode.N, KeyCombination.SHORTCUT_DOWN));
        addAction.disabledProperty().bind(mRunStateManager.runningProperty());

        mOptionsAction = new Action(Dict.OPTIONS.toString(), actionEvent -> {
            displayOptions();
        });
        mOptionsAction.setGraphic(MaterialIcon._Action.SETTINGS.getImageView(ICON_SIZE_TOOLBAR));
        FxHelper.setTooltip(mOptionsAction, new KeyCodeCombination(KeyCode.COMMA, KeyCombination.SHORTCUT_DOWN));

        var helpAction = new Action(Dict.HELP.toString(), actionEvent -> {
            displayHelp();
        });
        helpAction.setGraphic(MaterialIcon._Action.HELP_OUTLINE.getImageView(ICON_SIZE_TOOLBAR));
        FxHelper.setTooltip(helpAction, new KeyCodeCombination(KeyCode.F1, KeyCombination.SHORTCUT_ANY));

        //about
        PomInfo pomInfo = new PomInfo(App.class, "se.trixon", "cric");
        AboutModel aboutModel = new AboutModel(SystemHelper.getBundle(App.class, "about"), SystemHelperFx.getResourceAsImageView(App.class, "about_logo.png"));
        aboutModel.setAppVersion(pomInfo.getVersion());
        mAboutAction = AboutPane.getAction(mStage, aboutModel);
        mAboutAction.setGraphic(MaterialIcon._Action.INFO_OUTLINE.getImageView(ICON_SIZE_TOOLBAR));

        var actions = Arrays.asList(
                addAction,
                mOptionsAction,
                ActionUtils.ACTION_SPAN,
                mAboutAction,
                helpAction
        );

        ToolBar toolBar = ActionUtils.createToolBar(actions, ActionUtils.ActionTextBehavior.SHOW);
        FxHelper.undecorateButtons(toolBar.getItems().stream());
        FxHelper.slimToolBar(toolBar);

        mRoot = new BorderPane();
        mRoot.setTop(toolBar);
        mRoot.setCenter(mAppForm = new AppForm());
        var scene = new Scene(mRoot);

        mStage.setScene(scene);
    }

    private void displayHelp() {
        SystemHelper.desktopBrowse("https://trixon.se/projects/cric/documentation/");
    }

    private void displayOptions() {
        var alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.initOwner(mStage);
        alert.setTitle(Dict.OPTIONS.toString());
        alert.setGraphic(null);
        alert.setHeaderText(null);
        alert.setResizable(true);

        var optionsPanel = new OptionsPanel();
        optionsPanel.load();
        var dialogPane = alert.getDialogPane();
        dialogPane.setContent(optionsPanel);

        var result = FxHelper.showAndWait(alert, mStage);
        if (result.get() == ButtonType.OK) {
            optionsPanel.save();
        }
    }

    private void initAccelerators() {
        var accelerators = mStage.getScene().getAccelerators();

        accelerators.put(new KeyCodeCombination(KeyCode.Q, KeyCombination.SHORTCUT_DOWN), () -> {
            mStage.fireEvent(new WindowEvent(mStage, WindowEvent.WINDOW_CLOSE_REQUEST));
        });

        accelerators.put(new KeyCodeCombination(KeyCode.N, KeyCombination.SHORTCUT_DOWN), () -> {
            mAppForm.profileEdit(null);
        });

        if (!SystemUtils.IS_OS_MAC) {
            accelerators.put(new KeyCodeCombination(KeyCode.COMMA, KeyCombination.SHORTCUT_DOWN), () -> {
                displayOptions();
            });

            accelerators.put(new KeyCodeCombination(KeyCode.F1, KeyCombination.SHORTCUT_ANY), () -> {
                displayHelp();
            });
        }
    }

    private void initMac() {
        var menuToolkit = MenuToolkit.toolkit();
        var applicationMenu = menuToolkit.createDefaultApplicationMenu(APP_TITLE);
        menuToolkit.setApplicationMenu(applicationMenu);

        applicationMenu.getItems().remove(0);
        var aboutMenuItem = new MenuItem(String.format(Dict.ABOUT_S.toString(), APP_TITLE));
        aboutMenuItem.setOnAction(mAboutAction);

        var settingsMenuItem = new MenuItem(Dict.PREFERENCES.toString());
        settingsMenuItem.setOnAction(mOptionsAction);
        settingsMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.COMMA, KeyCombination.SHORTCUT_DOWN));

        applicationMenu.getItems().add(0, aboutMenuItem);
        applicationMenu.getItems().add(2, settingsMenuItem);

        int cnt = applicationMenu.getItems().size();
        applicationMenu.getItems().get(cnt - 1).setText(String.format("%s %s", Dict.QUIT.toString(), APP_TITLE));
    }

}
