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
import java.time.LocalDate;
import java.util.ResourceBundle;
import java.util.UUID;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.stage.Stage;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.SystemHelper;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.control.editable_list.EditableList;
import se.trixon.cric.App;
import se.trixon.cric.Profile;
import se.trixon.cric.ProfileManager;
import se.trixon.cric.StorageManager;
import static se.trixon.cric.StorageManager.GSON;

/**
 *
 * @author Patrik Karlström <patrik@trixon.se>
 */
public class ProfileListEditor {

    private final ResourceBundle mBundle = SystemHelper.getBundle(AppForm.class, "Bundle");
    private Font mDefaultFont = Font.getDefault();
    private EditableList<Profile> mEditableList;
    private final ProfileManager mProfileManager = ProfileManager.getInstance();
    private final StorageManager mStorageManager = StorageManager.getInstance();
    private ProfileStartListener mProfileStartListener;

    public ProfileListEditor() {
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

    public void setProfileStartListener(ProfileStartListener profileStartListener) {
        mProfileStartListener = profileStartListener;
    }

    private Stage getStage() {
        return (Stage) mEditableList.getScene().getWindow();
    }

    private void init() {
        mEditableList = new EditableList.Builder<Profile>()
                .setIconSize(App.ICON_SIZE_TOOLBAR)
                .setItemSingular(Dict.PROFILE.toString())
                .setItemPlural(Dict.PROFILES.toString())
                .setItemsProperty(mProfileManager.itemsProperty())
                .setOnEdit((title, profile) -> {
                    profileEdit(title, profile);
                })
                .setOnRemoveAll(() -> {
                    mProfileManager.getIdToItem().clear();
                    StorageManager.save();
                })
                .setOnRemove(t -> {
                    mProfileManager.getIdToItem().remove(t.getId());
                    StorageManager.save();
                })
                .setOnClone(t -> {
                    var original = t;
                    var json = GSON.toJson(original);
                    var clone = GSON.fromJson(json, original.getClass());
                    var uuid = UUID.randomUUID().toString();
                    clone.setId(uuid);
                    clone.setLastRun(0);
                    clone.setName("%s %s".formatted(clone.getName(), LocalDate.now().toString()));
                    mProfileManager.getIdToItem().put(clone.getId(), clone);

                    StorageManager.save();

                    return mProfileManager.getById(uuid);
                })
                .setOnStart(t -> {
                    mProfileStartListener.onStart(t);
                })
                .build();

        mEditableList.getListView().setCellFactory(listView -> new ProfileListCell());
    }

    private void postEdit(Profile profile) {
        mEditableList.postEdit(profile);
    }

    private void postInit() {
        try {
            mStorageManager.load();
        } catch (IOException ex) {
//            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    private void profileEdit(String title, Profile profile) {
        var alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.initOwner(getStage());
        alert.setResizable(true);
        alert.setTitle(title);
        alert.setGraphic(null);
        alert.setHeaderText(null);

        var dialogPane = alert.getDialogPane();
        var profileEditor = new ProfileEditor(profile);
        var button = (Button) dialogPane.lookupButton(ButtonType.OK);
        button.setText(Dict.SAVE.toString());

        dialogPane.setContent(profileEditor);
        profileEditor.setOkButton(button);
        FxHelper.removeSceneInitFlicker(dialogPane);

        var result = FxHelper.showAndWait(alert, getStage());
        if (result.get() == ButtonType.OK) {
            var editedItem = profileEditor.save();
            postEdit(mProfileManager.getById(editedItem.getId()));
        }
    }

    public interface ProfileStartListener {

        void onStart(Profile profile);
    }
}
