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
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.FadeTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;
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
    private final ResourceBundle mBundle = SystemHelper.getBundle(AppForm.class, "Bundle");
    private Font mDefaultFont;
    private ListView<Profile> mListView;
    private final ProfileManager mProfileManager = ProfileManager.getInstance();
    private ArrayList<Profile> mProfiles;
    private final RunStateManager mRunStateManager = RunStateManager.getInstance();
    private final StatusPanel mStatusPanel = new StatusPanel();
    private final Options mOptions = Options.getInstance();
    private final Log mLog = new Log();

    public AppForm() {
        mLog.setUseTimestamps(false);

        createUI();
        initListeners();
        postInit();
        mRunStateManager.setRunState(RunState.STARTABLE);
        mListView.requestFocus();
    }

    public void profileEdit(Profile profile) {
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

    }

    private Stage getStage() {
        return (Stage) getScene().getWindow();
    }

    private void initListeners() {
        mListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            mRunStateManager.setProfile(newValue);
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

    private void profileRun(Profile profile) {
        mStatusPanel.clear();
        String runDesc = String.format("%s '%s' (%s)", Dict.RUN.toString(), profile.getName(), profile.getDescription());
        mStatusPanel.out(runDesc);
        mStatusPanel.out(String.join(" ", profile.getCommand()));

        if (profile.isValid()) {
            String title = Dict.RUN.toString();
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

    class ProfileListCell extends ListCell<Profile> {

        private static final int ICON_SIZE_PROFILE = 24;
        private Action mCloneAction;
        private Action mEditAction;

        private final Label mDescLabel = new Label();
        private final Duration mDuration = Duration.millis(200);
        private final FadeTransition mFadeInTransition = new FadeTransition();
        private final FadeTransition mFadeOutTransition = new FadeTransition();
        private final Label mLastLabel = new Label();
        private final Label mNameLabel = new Label();
        private final SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat();
        private final StackPane mStackPane = new StackPane();
        private Action mRemoveAction;
        private Action mRunAction;

        public ProfileListCell() {
            mFadeInTransition.setDuration(mDuration);
            mFadeInTransition.setFromValue(0);
            mFadeInTransition.setToValue(1);

            mFadeOutTransition.setDuration(mDuration);
            mFadeOutTransition.setFromValue(1);
            mFadeOutTransition.setToValue(0);

            createUI();
            updateNightMode();

            mOptions.nightModeProperty().addListener((observable, oldValue, newValue) -> {
                updateNightMode();
            });
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

            setGraphic(mStackPane);
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

            mRunAction = new Action(Dict.RUN.toString(), actionEvent -> {
                mFadeOutTransition.playFromStart();
                profileRun(getSelectedProfile());
                mListView.requestFocus();
            });

            mEditAction = new Action(Dict.EDIT.toString(), actionEvent -> {
                mFadeOutTransition.playFromStart();
                profileEdit(getSelectedProfile());
                mListView.requestFocus();
            });

            mCloneAction = new Action(Dict.CLONE.toString(), actionEvent -> {
                mFadeOutTransition.playFromStart();
                profileClone();
                mListView.requestFocus();
            });

            mRemoveAction = new Action(Dict.REMOVE.toString(), actionEvent -> {
                mFadeOutTransition.playFromStart();
                profileRemove(getSelectedProfile());
                mListView.requestFocus();
            });

            var mainBox = new VBox(mNameLabel, mDescLabel, mLastLabel);
            mainBox.setAlignment(Pos.CENTER_LEFT);

            Collection<? extends Action> actions = Arrays.asList(
                    mRunAction,
                    mEditAction,
                    mCloneAction,
                    mRemoveAction
            );

            var toolBar = ActionUtils.createToolBar(actions, ActionUtils.ActionTextBehavior.HIDE);
            toolBar.setBackground(Background.EMPTY);
            toolBar.setVisible(false);
            toolBar.setMaxWidth(4 * ICON_SIZE_PROFILE * 1.84);
            FxHelper.slimToolBar(toolBar);
            FxHelper.undecorateButtons(toolBar.getItems().stream());
            FxHelper.adjustButtonWidth(toolBar.getItems().stream(), ICON_SIZE_PROFILE * 1.8);

            mFadeInTransition.setNode(toolBar);
            mFadeOutTransition.setNode(toolBar);
            mStackPane.getChildren().setAll(mainBox, toolBar);
            StackPane.setAlignment(toolBar, Pos.CENTER_RIGHT);

            mStackPane.setOnMouseEntered(mouseEvent -> {
                if (!toolBar.isVisible()) {
                    toolBar.setVisible(true);
                }

                selectListItem();
                mRunStateManager.setProfile(getSelectedProfile());
                mFadeInTransition.playFromStart();
            });

            mStackPane.setOnMouseExited(mouseEvent -> {
                mFadeOutTransition.playFromStart();
            });
        }

        private Profile getSelectedProfile() {
            return mListView.getSelectionModel().getSelectedItem();
        }

        private void profileClone() {
            Profile p = getSelectedProfile().clone();
            p.setName(null);
            profileEdit(p);
        }

        private void selectListItem() {
            mListView.getSelectionModel().select(this.getIndex());
            mListView.requestFocus();
        }

        private void updateNightMode() {
            MaterialIcon.setDefaultColor(mOptions.isNightMode() ? Color.LIGHTGRAY : Color.BLACK);

            mRunAction.setGraphic(MaterialIcon._Av.PLAY_ARROW.getImageView(ICON_SIZE_PROFILE));
            mEditAction.setGraphic(MaterialIcon._Content.CREATE.getImageView(ICON_SIZE_PROFILE));
            mCloneAction.setGraphic(MaterialIcon._Content.CONTENT_COPY.getImageView(ICON_SIZE_PROFILE));
            mRemoveAction.setGraphic(MaterialIcon._Content.REMOVE.getImageView(ICON_SIZE_PROFILE));
        }
    }

}
