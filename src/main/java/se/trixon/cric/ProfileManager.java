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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import se.trixon.almond.util.Xlog;

/**
 *
 * @author Patrik Karlström
 */
public class ProfileManager {

    private final File mDirectory;
    private final File mProfileFile;
    private ProfilesHolder mProfilesHolder;

    public static ProfileManager getInstance() {
        return Holder.INSTANCE;
    }

    private ProfileManager() {
        mDirectory = new File(System.getProperty("user.home"), ".config/cric");
        mProfileFile = new File(mDirectory, "profiles.json");

        try {
            FileUtils.forceMkdir(mDirectory);
        } catch (IOException ex) {
            Xlog.timedErr(ex.getLocalizedMessage());
        }
    }

    public Profile getProfile(String name) {
        for (Profile profile : mProfilesHolder.getProfiles()) {
            if (profile.getName().equalsIgnoreCase(name)) {
                return profile;
            }
        }

        return null;
    }

    public ArrayList<Profile> getProfiles() {
        if (mProfilesHolder == null) {
            try {
                load();
            } catch (IOException ex) {
                mProfilesHolder = new ProfilesHolder();
                Logger.getLogger(ProfileManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return mProfilesHolder.getProfiles();
    }

    public int getVersion() {
        return mProfilesHolder.getFileFormatVersion();
    }

    public boolean hasProfiles() {
        return !mProfilesHolder.getProfiles().isEmpty();
    }

    public boolean isValid(String oldName, String newName) {
        if (StringUtils.isBlank(newName)) {
            return false;
        }

        Profile profileByName = getProfile(newName.trim());
        return profileByName == null || profileByName == getProfile(oldName);
    }

    public void load() throws IOException {
        if (mProfileFile.exists()) {
            mProfilesHolder = ProfilesHolder.open(mProfileFile);
        } else {
            mProfilesHolder = new ProfilesHolder();
        }
    }

    public void save() throws IOException {
        mProfilesHolder.save(mProfileFile);
    }

    private static class Holder {

        private static final ProfileManager INSTANCE = new ProfileManager();
    }
}
