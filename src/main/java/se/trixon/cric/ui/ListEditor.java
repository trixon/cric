/*
 * Copyright 2024 Patrik Karlström <patrik@trixon.se>.
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
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.stage.Stage;
import org.apache.commons.io.FileUtils;
import org.controlsfx.dialog.ExceptionDialog;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.SystemHelper;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.control.editable_list.EditableList;
import se.trixon.cric.App;
import se.trixon.cric.Profile;
import se.trixon.cric.ProfileManager;
import se.trixon.cric.StorageManager;

/**
 *
 * @author Patrik Karlström <patrik@trixon.se>
 */
public class ListEditor {

    private final ResourceBundle mBundle = SystemHelper.getBundle(AppForm.class, "Bundle");
    private Font mDefaultFont = Font.getDefault();

    private EditableList<Profile> mEditableList;
    private final ProfileManager mProfileManager = ProfileManager.getInstance();
    private StorageManager mStorageManager = StorageManager.getInstance();

    public ListEditor() {
        init();
        mEditableList.setPrefWidth(400);
        var welcomeLabel = new Label(mBundle.getString("welcome"));
        welcomeLabel.setFont(Font.font(mDefaultFont.getName(), FontPosture.ITALIC, mDefaultFont.getSize()));

        mEditableList.getListView().setPlaceholder(welcomeLabel);
        postInit();
        mEditableList.getListView().requestFocus();

        if (!mProfileManager.getItems().isEmpty()) {
            mEditableList.getListView().getSelectionModel().selectFirst();
        }

    }

    public EditableList<Profile> getEditableList() {
        return mEditableList;
    }

    public ListView<Profile> getListView() {
        return mEditableList.getListView();
    }

    private Profile getSelectedProfile() {
        return mEditableList.getListView().getSelectionModel().getSelectedItem();
    }

    private Stage getStage() {
        return (Stage) mEditableList.getScene().getWindow();
    }

    private void init() {
        mEditableList = new EditableList.Builder<Profile>()
                .setIconSize(App.ICON_SIZE_TOOLBAR)
                .setItemSingular(Dict.TASK.toString())
                .setItemPlural(Dict.TASKS.toString())
                .setItemsProperty(mProfileManager.itemsProperty())
                .setOnEdit((title, profile) -> {
                    profileEdit(title, profile);
                })
                .setOnRemoveAll(() -> {
//                    mTaskManager.getIdToItem().clear();
//                    StorageManager.save();
                })
                .setOnRemove(t -> {
//                    mTaskManager.getIdToItem().remove(t.getId());
//                    StorageManager.save();
                })
                .setOnClone(t -> {
//                    var original = t;
//                    var json = GSON.toJson(original);
//                    var clone = GSON.fromJson(json, original.getClass());
//                    var uuid = UUID.randomUUID().toString();
//                    clone.setId(uuid);
//                    clone.setLastRun(0);
//                    clone.setName("%s %s".formatted(clone.getName(), LocalDate.now().toString()));
//                    mTaskManager.getIdToItem().put(clone.getId(), clone);
//
//                    StorageManager.save();
//
//                    return mTaskManager.getById(uuid);
                    return null;
                })
                .setOnStart(task -> {
//                    mExecutorManager.requestStart(task);
                })
                .build();

        mEditableList.getListView().setCellFactory(listView -> new ProfileListCell());
    }

    private void populateProfiles(Profile profile) {
        FxHelper.runLater(() -> {
//            Collections.sort(mProfiles);
//            mListView.getItems().setAll(mProfiles);
//
//            if (profile != null) {
//                mListView.getSelectionModel().select(profile);
//                mListView.scrollTo(profile);
//            }
        });
    }

    private void postEdit(Profile profile) {
        mEditableList.postEdit(profile);
    }

    private void postInit() {
        profilesLoad();
        populateProfiles(null);
    }

    private void profileClone(Profile profile) {
        Profile p = profile.clone();
        p.setName(null);
//        profileEdit(p);
    }

    private void profileEdit(String title, Profile profile) {
        var alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.initOwner(getStage());
        alert.setResizable(true);
        boolean addNew = false;
        boolean clone = profile != null && profile.getName() == null;

        if (profile == null) {
//            title = Dict.ADD.toString();
            addNew = true;
            profile = new Profile();
        } else if (clone) {
//            title = Dict.CLONE.toString();
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
            var editedItem = profilePanel.save();
            postEdit(mProfileManager.getById(editedItem.getId()));

//            profilePanel.save();
            if (addNew || clone) {
//                mProfiles.add(profile);
//                mProfileManager.getItems().add(profile);
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
//            mProfiles.remove(profile);
            profilesSave();
            populateProfiles(null);
        }
    }

    /*
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
     */
    private void profilesLoad() {
        try {
            mStorageManager.load();
//            mProfiles = mProfileManager.get;
        } catch (IOException ex) {
//            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    private void profilesSave() {
        StorageManager.save();
//        try {
//        } catch (IOException ex) {
//            LOGGER.log(Level.SEVERE, null, ex);
//        }
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

}
