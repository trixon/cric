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
package se.trixon.cric.ui;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.apache.commons.io.FileUtils;
import org.controlsfx.dialog.ExceptionDialog;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.Log;
import se.trixon.almond.util.ProcessLogThread;
import se.trixon.almond.util.SystemHelper;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.cric.Options;
import se.trixon.cric.Profile;
import se.trixon.cric.RunState;
import se.trixon.cric.RunStateManager;

/**
 *
 * @author Patrik Karlström
 */
public class AppForm extends BorderPane {

    private static final Logger LOGGER = Logger.getLogger(AppForm.class.getName());
    private Font mDefaultFont;
    private final Log mLog = new Log();
    private final Options mOptions = Options.getInstance();
    private final ProfileListEditor mProfileListEditor = new ProfileListEditor();
    private BorderPane mRightBorderPane = new BorderPane();
    private final RunStateManager mRunStateManager = RunStateManager.getInstance();
    private final StatusPanel mStatusPanel = new StatusPanel();

    public AppForm() {
        mLog.setUseTimestamps(false);
        createUI();
        initListeners();
        mRunStateManager.setRunState(RunState.STARTABLE);
    }

    public void setToolBar(ToolBar toolBar) {
        mRightBorderPane.setTop(toolBar);
    }

    public void updateNightMode() {
        mProfileListEditor.getEditableList().refreshIcons();
    }

    private void createUI() {
        mDefaultFont = Font.getDefault();

        mProfileListEditor.getEditableList().disableProperty().bind(mRunStateManager.runningProperty());
        setLeft(mProfileListEditor.getEditableList());
        mRightBorderPane.setCenter(mStatusPanel);
        setCenter(mRightBorderPane);
        mLog.setOut(s -> {
            System.out.println(s);
            mStatusPanel.out(s);
        });

        mLog.setErr(s -> {
            System.err.println(s);
            mStatusPanel.err(s);
        });

        mLog.out(SystemHelper.getSystemInfo());
    }

    private void initListeners() {
        mProfileListEditor.getListView().getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            mRunStateManager.setProfile(newValue);
        });

        mOptions.nightModeProperty().addListener((observable, oldValue, newValue) -> {
            updateNightMode();
        });

        mProfileListEditor.setProfileStartListener(profile -> {
            profileStart(profile);
        });
    }

    private void profileStart(Profile profile) {
        mStatusPanel.clear();
        String runDesc = String.format("%s '%s' (%s)", Dict.RUN.toString(), profile.getName(), profile.getDescription());
        mStatusPanel.out(runDesc);
        mStatusPanel.out(String.join(" \\\n    ", profile.getCommand()));

        if (profile.isValid()) {
            String title = Dict.RUN.toString();
            if (profile.getOutput().exists()) {
                runDesc += String.format("?\n\n'%s' will be replaced.", profile.getOutput().getPath());
            }
            var alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.initOwner(getScene().getWindow());
            alert.setTitle(title);
            alert.setHeaderText(runDesc);
            ((Button) alert.getDialogPane().lookupButton(ButtonType.OK)).setText(Dict.RUN.toString());

            var result = FxHelper.showAndWait(alert, (Stage) getScene().getWindow());
            if (result.get() == ButtonType.OK) {
                if (requestDirectoryRemoval(profile)) {
                    mRunStateManager.setRunState(RunState.CANCELABLE);
                    mLog.out("");
                    new Thread(() -> {
                        var processBuilder = new ProcessBuilder(profile.getCommand()).inheritIO();
                        processBuilder.redirectOutput(ProcessBuilder.Redirect.PIPE);
                        try {
                            var process = processBuilder.start();
                            new ProcessLogThread(process.getInputStream(), 0, mLog).start();
                            new ProcessLogThread(process.getErrorStream(), -1, mLog).start();

                            process.waitFor();
                            profile.setLastRun(System.currentTimeMillis());
//                            profilesSave();
//                            populateProfiles(profile);
                        } catch (IOException | InterruptedException ex) {
                            mLog.timedErr(ex.getMessage());
                        }
                        mLog.timedOut("Done.");
                        mRunStateManager.setRunState(RunState.STARTABLE);
                    }).start();
                }
            }
        } else {
            mStatusPanel.out(profile.getValidationError());
            mStatusPanel.out(Dict.ABORTING.toString());
        }
    }

    private boolean requestDirectoryRemoval(Profile profile) {
        if (profile.getOutput().exists()) {
            var alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.initOwner(getScene().getWindow());
            boolean directory = profile.getOutput().isDirectory();
            String title = directory ? Dict.Dialog.TITLE_REMOVE_DIR.toString() : Dict.Dialog.TITLE_REMOVE_FILE.toString();
            alert.setTitle(title + "?");
            String message = String.format(directory ? Dict.Dialog.MESSAGE_REMOVE_DIR.toString() : Dict.Dialog.MESSAGE_REMOVE_FILE.toString(), profile.getOutputAsString());
            alert.setHeaderText(message);

            ((Button) alert.getDialogPane().lookupButton(ButtonType.OK)).setText(title);

            var result = FxHelper.showAndWait(alert, (Stage) getScene().getWindow());
            if (result.get() == ButtonType.OK) {
                try {
                    FileUtils.forceDelete(profile.getOutput());
                    return true;
                } catch (IOException ex) {
                    Logger.getLogger(AppForm.class.getName()).log(Level.SEVERE, null, ex);
                    ExceptionDialog exceptionDialog = new ExceptionDialog(ex);
                    FxHelper.showAndWait(exceptionDialog, (Stage) getScene().getWindow());
                    return false;
                }
            } else {
                return false;
            }
        } else {
            return true;
        }
    }
}
