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
package se.trixon.cric;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import javafx.collections.ObservableMap;
import org.apache.commons.io.FileUtils;
import se.trixon.almond.util.Xlog;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.gson_adapter.FileAdapter;

/**
 *
 * @author Patrik Karlström <patrik@trixon.se>
 */
public class StorageManager {

    public static final Gson GSON = new GsonBuilder()
            .setVersion(1.0)
            .serializeNulls()
            .setPrettyPrinting()
            .registerTypeAdapter(File.class, new FileAdapter())
            .create();
    private final File mDirectory;
    private final File mProfileFile;
    private final ProfileManager mProfileManager = ProfileManager.getInstance();
    private Storage mStorage = new Storage();

    public static StorageManager getInstance() {
        return Holder.INSTANCE;
    }

    public static void save() {
        try {
            StorageManager.getInstance().saveToFile();
        } catch (IOException ex) {
//            Exceptions.printStackTrace(ex);
        }
    }

    private StorageManager() {
        mDirectory = new File(System.getProperty("user.home"), ".config/cric");
        mProfileFile = new File(mDirectory, "profiles32.json");

        try {
            FileUtils.forceMkdir(mDirectory);
        } catch (IOException ex) {
            Xlog.timedErr(ex.getLocalizedMessage());
        }
    }

    public int getFileFormatVersion() {
        return mStorage.getFileFormatVersion();
    }

    public File getProfileFile() {
        return mProfileFile;
    }

    public ProfileManager getProfileManager() {
        return mProfileManager;
    }

    public void load() throws IOException {
        if (mProfileFile.exists()) {
            mStorage = Storage.open(mProfileFile);

            var taskItems = mProfileManager.getIdToItem();
            taskItems.clear();
            taskItems.putAll(mStorage.getProfiles());
        } else {
            mStorage = new Storage();
        }
    }

    private void saveToFile() throws IOException {
        mStorage.setProfiles(mProfileManager.getIdToItem());
        String json = mStorage.save(mProfileFile);
//        String tag = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
//        FileUtils.writeStringToFile(mTasksBackupFile, String.format("%s=%s\n", tag, json), Charset.defaultCharset(), true);

        try {
            FxHelper.runLater(() -> {
                try {
                    load();
                } catch (IOException ex) {
//                    Exceptions.printStackTrace(ex);
                }
            });
        } catch (IllegalStateException e) {
            //nvm - probably started from console w/o fx
        }
    }

    private static class Holder {

        private static final StorageManager INSTANCE = new StorageManager();
    }

    public class Storage {

        private static final int FILE_FORMAT_VERSION = 1;
        @SerializedName("format_version")
        private int mFileFormatVersion;
        @SerializedName("profiles")
        private final HashMap<String, Profile> mProfiles = new HashMap<>();

        public static Storage open(File file) throws IOException, JsonSyntaxException {
            String json = FileUtils.readFileToString(file, Charset.defaultCharset());
            var storage = GSON.fromJson(json, Storage.class);

            if (storage.mFileFormatVersion != FILE_FORMAT_VERSION) {
                //TODO Handle file format version change
            }

            return storage;
        }

        public int getFileFormatVersion() {
            return mFileFormatVersion;
        }

        public HashMap<String, Profile> getProfiles() {
            return mProfiles;
        }

        void setProfiles(ObservableMap<String, Profile> tasks) {
            mProfiles.clear();
            mProfiles.putAll(tasks);
        }

        public String save(File file) throws IOException {
            mFileFormatVersion = FILE_FORMAT_VERSION;
            var json = GSON.toJson(this);
            FileUtils.writeStringToFile(file, json, Charset.defaultCharset());

            return json;
        }
    }
}
