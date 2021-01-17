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

import com.google.gson.annotations.SerializedName;
import java.io.File;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;
import se.trixon.almond.util.SystemHelper;

/**
 *
 * @author Patrik Karlström
 */
public class Profile implements Comparable<Profile>, Cloneable {

    private transient final ResourceBundle mBundle = SystemHelper.getBundle(Profile.class, "Bundle");
    @SerializedName("description")
    private String mDescription;
    @SerializedName("destination")
    private File mDestDir;
    transient private boolean mDryRun;
    @SerializedName("last_run")
    private long mLastRun;
    @SerializedName("name")
    private String mName;
    @SerializedName("source")
    private File mSourceDir;
    private transient StringBuilder mValidationErrorBuilder = new StringBuilder();

    public Profile() {
    }

    @Override
    public Profile clone() {
        try {
            return (Profile) super.clone();
        } catch (CloneNotSupportedException ex) {
            Logger.getLogger(Profile.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    @Override
    public int compareTo(Profile o) {
        return mName.compareTo(o.getName());
    }

    public String getDescription() {
        return StringUtils.defaultString(mDescription);
    }

    public File getDestDir() {
        return mDestDir;
    }

    public String getDestDirAsString() {
        return mDestDir == null ? "" : mDestDir.getPath();
    }

    public long getLastRun() {
        return mLastRun;
    }

    public String getName() {
        return mName;
    }

    public File getSourceDir() {
        return mSourceDir;
    }

    public String getSourceDirAsString() {
        return mSourceDir == null ? "" : mSourceDir.getPath();
    }

    public String getValidationError() {
        return mValidationErrorBuilder.toString();
    }

    public boolean isDryRun() {
        return mDryRun;
    }

    public boolean isValid() {
        mValidationErrorBuilder = new StringBuilder();

        if (mSourceDir == null || !mSourceDir.isDirectory()) {
            addValidationError(String.format(mBundle.getString("invalid_source_dir"), mSourceDir));
        }

        if (mDestDir == null || !mDestDir.isDirectory()) {
            addValidationError(String.format(mBundle.getString("invalid_dest_dir"), mDestDir));
        }

        return mValidationErrorBuilder.length() == 0;
    }

    public void setDescription(String description) {
        mDescription = description;
    }

    public void setDestDir(File dest) {
        mDestDir = dest;
    }

    public void setDryRun(boolean dryRun) {
        mDryRun = dryRun;
    }

    public void setLastRun(long lastRun) {
        this.mLastRun = lastRun;
    }

    public void setName(String name) {
        mName = name;
    }

    public void setSourceDir(File source) {
        mSourceDir = source;
    }

    @Override
    public String toString() {
        return mName;
    }

    private void addValidationError(String string) {
        mValidationErrorBuilder.append(string).append("\n");
    }

}
