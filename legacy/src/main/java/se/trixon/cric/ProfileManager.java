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

import java.util.ArrayList;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Patrik Karlström
 */
public class ProfileManager {

    private final ObjectProperty<ObservableMap<String, Profile>> mIdToItemProperty = new SimpleObjectProperty<>();
    private final ObjectProperty<ObservableList<Profile>> mItemsProperty = new SimpleObjectProperty<>();

    public static ProfileManager getInstance() {
        return Holder.INSTANCE;
    }

    private ProfileManager() {
        mItemsProperty.setValue(FXCollections.observableArrayList());
        mIdToItemProperty.setValue(FXCollections.observableHashMap());

        mIdToItemProperty.get().addListener((MapChangeListener.Change<? extends String, ? extends Profile> change) -> {
            var values = new ArrayList<Profile>(getIdToItem().values());
            values.sort((o1, o2) -> o1.getName().compareToIgnoreCase(o2.getName()));
            getItems().setAll(values);
        });
    }

    public Profile getById(String id) {
        return getIdToItem().get(id);
    }

    public Profile getByName(String name) {
        for (var task : getItems()) {
            if (task.getName().equalsIgnoreCase(name)) {
                return task;
            }
        }

        System.out.println("TASK NOT FOUND: " + name);

        return null;
    }

    public final ObservableMap<String, Profile> getIdToItem() {
        return mIdToItemProperty.get();
    }

    public final ObservableList<Profile> getItems() {
        return mItemsProperty.get();
    }

//    public Profile getProfile(String name) {
//        for (Profile profile : mProfilesHolder.getProfiles()) {
//            if (profile.getName().equalsIgnoreCase(name)) {
//                return profile;
//            }
//        }
//
//        return null;
//    }
//
//    public ArrayList<Profile> getProfiles() {
//        if (mProfilesHolder == null) {
//            try {
//                load();
//            } catch (IOException ex) {
//                mProfilesHolder = new ProfilesHolder();
//                Logger.getLogger(ProfileManager.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        }
//
//        return mProfilesHolder.getProfiles();
//    }
//    public int getVersion() {
//        return mProfilesHolder.getFileFormatVersion();
//    }
//
//    public boolean hasProfiles() {
//        return !mProfilesHolder.getProfiles().isEmpty();
//    }
//    public boolean isValid(String oldName, String newName) {
//        if (StringUtils.isBlank(newName)) {
//            return false;
//        }
//
//        Profile profileByName = getProfile(newName.trim());
//        return profileByName == null || profileByName == getProfile(oldName);
//    }
    public boolean isValid(String oldName, String newName) {
        if (StringUtils.isBlank(newName)) {
            return false;
        }

        var profileByName = getByName(newName.trim());

        return profileByName == null || profileByName == getByName(oldName);
    }

    public ObjectProperty<ObservableList<Profile>> itemsProperty() {
        return mItemsProperty;
    }

//    public void load() throws IOException {
//        if (mProfileFile.exists()) {
//            mProfilesHolder = ProfilesHolder.open(mProfileFile);
//        } else {
//            mProfilesHolder = new ProfilesHolder();
//        }
//    }
//
//    public void save() throws IOException {
//        mProfilesHolder.save(mProfileFile);
//    }
    private static class Holder {

        private static final ProfileManager INSTANCE = new ProfileManager();
    }
}
