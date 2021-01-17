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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author Patrik Karlström
 */
public class ProfilesHolder {

    private static final int FILE_FORMAT_VERSION = 1;
    private static final Gson GSON = new GsonBuilder()
            .setVersion(1.0)
            .serializeNulls()
            .setPrettyPrinting()
            .create();
    @SerializedName("format_version")
    private int mFileFormatVersion;
    @SerializedName("profiles")
    private final ArrayList<Profile> mProfiles = new ArrayList<>();

    public static ProfilesHolder open(File file) throws IOException, JsonSyntaxException {
        String json = FileUtils.readFileToString(file, Charset.defaultCharset());

        ProfilesHolder profiles = GSON.fromJson(json, ProfilesHolder.class);

        if (profiles.mFileFormatVersion != FILE_FORMAT_VERSION) {
            //TODO Handle file format version change
        }

        return profiles;
    }

    public int getFileFormatVersion() {
        return mFileFormatVersion;
    }

    public ArrayList<Profile> getProfiles() {
        return mProfiles;
    }

    public void save(File file) throws IOException {
        mFileFormatVersion = FILE_FORMAT_VERSION;
        FileUtils.writeStringToFile(file, GSON.toJson(this), Charset.defaultCharset());
    }
}
