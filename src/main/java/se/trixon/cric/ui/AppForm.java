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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.binding.BooleanBinding;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import org.apache.commons.io.FileUtils;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionUtils;
import org.controlsfx.dialog.ExceptionDialog;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.Log;
import se.trixon.almond.util.ProcessLogThread;
import se.trixon.almond.util.SystemHelper;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.icons.material.MaterialIcon;
import static se.trixon.cric.App.ICON_SIZE_TOOLBAR;
import se.trixon.cric.Options;
import se.trixon.cric.Profile;
import se.trixon.cric.ProfileManager;
import se.trixon.cric.RunState;
import se.trixon.cric.RunStateManager;

/**
 *
 * @author Patrik Karlström
 */
public class AppForm extends BorderPane {

    private static final Logger LOGGER = Logger.getLogger(AppForm.class.getName());
    private Action mAddAction;
    private final ResourceBundle mBundle = SystemHelper.getBundle(AppForm.class, "Bundle");
    private Action mCloneAction;
    private Font mDefaultFont;
    private Action mEditAction;
    private ListView<Profile> mListView;
    private final Log mLog = new Log();
    private final Options mOptions = Options.getInstance();
    private final ProfileManager mProfileManager = ProfileManager.getInstance();
    private ArrayList<Profile> mProfiles;
    private Action mRemoveAction;
    private Action mRunAction;
    private final RunStateManager mRunStateManager = RunStateManager.getInstance();
    private final StatusPanel mStatusPanel = new StatusPanel();

    public AppForm() {
        mLog.setUseTimestamps(false);

        createUI();
        initListeners();
        postInit();
        mRunStateManager.setRunState(RunState.STARTABLE);
        mListView.requestFocus();

        if (mProfileManager.hasProfiles()) {
            mListView.getSelectionModel().selectFirst();
        }
    }

    public ArrayList<Action> getToolBarActions() {
        var actions = new ArrayList<>(Arrays.asList(
                mAddAction,
                mEditAction,
                mCloneAction,
                mRemoveAction,
                ActionUtils.ACTION_SEPARATOR,
                mRunAction,
                ActionUtils.ACTION_SEPARATOR
        ));

        return actions;
    }

    public void initAccelerators() {
        var accelerators = getScene().getAccelerators();

        accelerators.put(new KeyCodeCombination(KeyCode.N, KeyCombination.SHORTCUT_DOWN), () -> {
            profileEdit(null);
        });

        accelerators.put(new KeyCodeCombination(KeyCode.R, KeyCombination.SHORTCUT_DOWN), () -> {
            if (getSelectedProfile() != null) {
                profileRun(getSelectedProfile());
            }
        });

        accelerators.put(new KeyCodeCombination(KeyCode.E, KeyCombination.SHORTCUT_DOWN), () -> {
            if (getSelectedProfile() != null) {
                profileEdit(getSelectedProfile());
            }
        });

        accelerators.put(new KeyCodeCombination(KeyCode.C, KeyCombination.SHORTCUT_DOWN), () -> {
            if (getSelectedProfile() != null) {
                profileClone(getSelectedProfile());
            }
        });

        accelerators.put(new KeyCodeCombination(KeyCode.DELETE), () -> {
            if (getSelectedProfile() != null) {
                profileRemove(getSelectedProfile());
            }
        });
    }

    private void createUI() {
        mDefaultFont = Font.getDefault();

        mListView = new ListView<>();
        mListView.setCellFactory(listView -> new ProfileListCell());
        mListView.disableProperty().bind(mRunStateManager.runningProperty());
        mListView.setPrefWidth(400);

        var welcomeLabel = new Label(mBundle.getString("welcome"));
        welcomeLabel.setFont(Font.font(mDefaultFont.getName(), FontPosture.ITALIC, mDefaultFont.getSize()));

        mListView.setPlaceholder(welcomeLabel);

        setLeft(mListView);
        setCenter(mStatusPanel);
        mLog.setOut(s -> {
            System.out.println(s);
            mStatusPanel.out(s);
        });

        mLog.setErr(s -> {
            System.err.println(s);
            mStatusPanel.err(s);
        });

        mLog.out(SystemHelper.getSystemInfo());

        mAddAction = new Action(Dict.ADD.toString(), actionEvent -> {
            profileEdit(null);
        });
        FxHelper.setTooltip(mAddAction, new KeyCodeCombination(KeyCode.N, KeyCombination.SHORTCUT_DOWN));
        mAddAction.disabledProperty().bind(mRunStateManager.runningProperty());

        BooleanBinding profileBooleanBinding = mRunStateManager.profileProperty().isNull().or(mRunStateManager.runningProperty());

        mRunAction = new Action(Dict.RUN.toString(), actionEvent -> {
            profileRun(getSelectedProfile());
            mListView.requestFocus();
        });
        FxHelper.setTooltip(mRunAction, new KeyCodeCombination(KeyCode.R, KeyCombination.SHORTCUT_DOWN));
        mRunAction.disabledProperty().bind(profileBooleanBinding);

        mEditAction = new Action(Dict.EDIT.toString(), actionEvent -> {
            profileEdit(getSelectedProfile());
            mListView.requestFocus();
        });
        FxHelper.setTooltip(mEditAction, new KeyCodeCombination(KeyCode.E, KeyCombination.SHORTCUT_DOWN));
        mEditAction.disabledProperty().bind(profileBooleanBinding);

        mCloneAction = new Action(Dict.CLONE.toString(), actionEvent -> {
            profileClone(getSelectedProfile());
            mListView.requestFocus();
        });
        FxHelper.setTooltip(mCloneAction, new KeyCodeCombination(KeyCode.C, KeyCombination.SHORTCUT_DOWN));
        mCloneAction.disabledProperty().bind(profileBooleanBinding);

        mRemoveAction = new Action(Dict.REMOVE.toString(), actionEvent -> {
            profileRemove(getSelectedProfile());
            mListView.requestFocus();
        });
        FxHelper.setTooltip(mRemoveAction, new KeyCodeCombination(KeyCode.DELETE));
        mRemoveAction.disabledProperty().bind(profileBooleanBinding);

        updateNightMode();
    }

    private Profile getSelectedProfile() {
        return mListView.getSelectionModel().getSelectedItem();
    }

    private Stage getStage() {
        return (Stage) getScene().getWindow();
    }

    private void initListeners() {
        mListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            mRunStateManager.setProfile(newValue);
        });

        mOptions.nightModeProperty().addListener((observable, oldValue, newValue) -> {
            updateNightMode();
        });
    }

    private void populateProfiles(Profile profile) {
        FxHelper.runLater(() -> {
            Collections.sort(mProfiles);
            mListView.getItems().setAll(mProfiles);

            if (profile != null) {
                mListView.getSelectionModel().select(profile);
                mListView.scrollTo(profile);
            }
        });
    }

    private void postInit() {
        profilesLoad();
        populateProfiles(null);
    }

    private void profileClone(Profile profile) {
        Profile p = profile.clone();
        p.setName(null);
        profileEdit(p);
    }

    private void profileEdit(Profile profile) {
        var alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.initOwner(getStage());
        alert.setResizable(true);
        String title = Dict.EDIT.toString();
        boolean addNew = false;
        boolean clone = profile != null && profile.getName() == null;

        if (profile == null) {
            title = Dict.ADD.toString();
            addNew = true;
            profile = new Profile();
        } else if (clone) {
            title = Dict.CLONE.toString();
            profile.setLastRun(0);
        }

        alert.setTitle(title);
        alert.setGraphic(null);
        alert.setHeaderText(null);

        var profilePanel = new ProfilePanel(profile);
        var dialogPane = alert.getDialogPane();
        var button = (Button) dialogPane.lookupButton(ButtonType.OK);
        button.setText(Dict.SAVE.toString());

        dialogPane.setContent(profilePanel);
        profilePanel.setOkButton(button);
        FxHelper.removeSceneInitFlicker(dialogPane);

        Optional<ButtonType> result = FxHelper.showAndWait(alert, getStage());
        if (result.get() == ButtonType.OK) {
            profilePanel.save();
            if (addNew || clone) {
                mProfiles.add(profile);
            }

            profilesSave();
            populateProfiles(profile);
        }
    }

    private void profileRemove(Profile profile) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.initOwner(getStage());
        alert.setTitle(Dict.Dialog.TITLE_PROFILE_REMOVE.toString() + "?");
        String message = String.format(Dict.Dialog.MESSAGE_PROFILE_REMOVE.toString(), profile.getName());
        alert.setHeaderText(message);

        ((Button) alert.getDialogPane().lookupButton(ButtonType.OK)).setText(Dict.REMOVE.toString());

        Optional<ButtonType> result = FxHelper.showAndWait(alert, getStage());
        if (result.get() == ButtonType.OK) {
            mProfiles.remove(profile);
            profilesSave();
            populateProfiles(null);
        }
    }

    private void profileRun(Profile profile) {
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
            alert.initOwner(getStage());
            alert.setTitle(title);
            alert.setHeaderText(runDesc);
            ((Button) alert.getDialogPane().lookupButton(ButtonType.OK)).setText(Dict.RUN.toString());

            Optional<ButtonType> result = FxHelper.showAndWait(alert, getStage());
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
                            profilesSave();
                            populateProfiles(profile);
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

    private void profilesLoad() {
        try {
            mProfileManager.load();
            mProfiles = mProfileManager.getProfiles();
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    private void profilesSave() {
        try {
            mProfileManager.save();
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    private boolean requestDirectoryRemoval(Profile profile) {
        if (profile.getOutput().exists()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.initOwner(getStage());
            boolean directory = profile.getOutput().isDirectory();
            String title = directory ? Dict.Dialog.TITLE_REMOVE_DIR.toString() : Dict.Dialog.TITLE_REMOVE_FILE.toString();
            alert.setTitle(title + "?");
            String message = String.format(directory ? Dict.Dialog.MESSAGE_REMOVE_DIR.toString() : Dict.Dialog.MESSAGE_REMOVE_FILE.toString(), profile.getOutputAsString());
            alert.setHeaderText(message);

            ((Button) alert.getDialogPane().lookupButton(ButtonType.OK)).setText(title);

            Optional<ButtonType> result = FxHelper.showAndWait(alert, getStage());
            if (result.get() == ButtonType.OK) {
                try {
                    FileUtils.forceDelete(profile.getOutput());
                    return true;
                } catch (IOException ex) {
                    Logger.getLogger(AppForm.class.getName()).log(Level.SEVERE, null, ex);
                    ExceptionDialog exceptionDialog = new ExceptionDialog(ex);
                    FxHelper.showAndWait(exceptionDialog, getStage());
                    return false;
                }
            } else {
                return false;
            }

        } else {
            return true;
        }
    }

    private void updateNightMode() {
        MaterialIcon.setDefaultColor(mOptions.isNightMode() ? Color.LIGHTGRAY : Color.BLACK);

        mAddAction.setGraphic(MaterialIcon._Content.ADD.getImageView(ICON_SIZE_TOOLBAR));
        mRunAction.setGraphic(MaterialIcon._Av.PLAY_ARROW.getImageView(ICON_SIZE_TOOLBAR));
        mEditAction.setGraphic(MaterialIcon._Content.CREATE.getImageView(ICON_SIZE_TOOLBAR));
        mCloneAction.setGraphic(MaterialIcon._Content.CONTENT_COPY.getImageView(ICON_SIZE_TOOLBAR));
        mRemoveAction.setGraphic(MaterialIcon._Content.REMOVE.getImageView(ICON_SIZE_TOOLBAR));
    }

    class ProfileListCell extends ListCell<Profile> {

        private final Label mDescLabel = new Label();
        private final Label mLastLabel = new Label();
        private final Label mNameLabel = new Label();
        private final SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat();
        private VBox mMainBox;

        public ProfileListCell() {
            createUI();
        }

        @Override
        protected void updateItem(Profile profile, boolean empty) {
            super.updateItem(profile, empty);

            if (profile == null || empty) {
                clearContent();
            } else {
                addContent(profile);
            }
        }

        private void addContent(Profile profile) {
            setText(null);

            mNameLabel.setText(profile.getName());
            mDescLabel.setText(profile.getDescription());
            String lastRun = "-";
            if (profile.getLastRun() != 0) {
                lastRun = mSimpleDateFormat.format(new Date(profile.getLastRun()));
            }
            mLastLabel.setText(lastRun);

            setGraphic(mMainBox);
        }

        private void clearContent() {
            setText(null);
            setGraphic(null);
        }

        private void createUI() {
            String fontFamily = mDefaultFont.getFamily();
            double fontSize = mDefaultFont.getSize();

            mNameLabel.setFont(Font.font(fontFamily, FontWeight.BOLD, fontSize * 1.4));
            mDescLabel.setFont(Font.font(fontFamily, FontWeight.NORMAL, fontSize * 1.2));
            mLastLabel.setFont(Font.font(fontFamily, FontWeight.NORMAL, fontSize * 1.2));

            mMainBox = new VBox(mNameLabel, mDescLabel, mLastLabel);
            mMainBox.setAlignment(Pos.CENTER_LEFT);
        }
    }
}
