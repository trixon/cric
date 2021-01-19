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

    private static final Logger LOGGER = Logger.getLogger(Profile.class.getName());

    @SerializedName("bindServices")
    private boolean mBindServices;
    private transient final ResourceBundle mBundle = SystemHelper.getBundle(Profile.class, "Bundle");
    @SerializedName("compress")
    private int mCompress;
    @SerializedName("description")
    private String mDescription;
    transient private boolean mDryRun;
    @SerializedName("endian")
    private int mEndian;
    @SerializedName("ignoreSigning")
    private boolean mIgnoreSigning;
    @SerializedName("jlink")
    private File mJlink;
    @SerializedName("last_run")
    private long mLastRun;
    @SerializedName("name")
    private String mName;
    @SerializedName("noHeaders")
    private boolean mNoHeaders;
    @SerializedName("noManPages")
    private boolean mNoManPages;
    @SerializedName("output")
    private File mOutput;
    @SerializedName("source")
    private File mSourceDir;
    @SerializedName("stripDebug")
    private boolean mStripDebug;
    private transient StringBuilder mValidationErrorBuilder = new StringBuilder();

    public Profile() {
    }

    @Override
    public Profile clone() {
        try {
            return (Profile) super.clone();
        } catch (CloneNotSupportedException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            return null;
        }
    }

    @Override
    public int compareTo(Profile o) {
        return mName.compareTo(o.getName());
    }

    public int getCompress() {
        return mCompress;
    }

    public String getDescription() {
        return StringUtils.defaultString(mDescription);
    }

    public String getDestDirAsString() {
        return mOutput == null ? "" : mOutput.getPath();
    }

    public int getEndian() {
        return mEndian;
    }

    public File getJlink() {
        return mJlink;
    }

    public long getLastRun() {
        return mLastRun;
    }

    public String getName() {
        return mName;
    }

    public File getOutput() {
        return mOutput;
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

    public boolean isBindServices() {
        return mBindServices;
    }

    public boolean isDryRun() {
        return mDryRun;
    }

    public boolean isIgnoreSigning() {
        return mIgnoreSigning;
    }

    public boolean isNoHeaders() {
        return mNoHeaders;
    }

    public boolean isNoManPages() {
        return mNoManPages;
    }

    public boolean isStripDebug() {
        return mStripDebug;
    }

    public boolean isValid() {
        mValidationErrorBuilder = new StringBuilder();

        if (mSourceDir == null || !mSourceDir.isDirectory()) {
            addValidationError(String.format(mBundle.getString("invalid_source_dir"), mSourceDir));
        }

        if (mOutput == null || !mOutput.isDirectory()) {
            addValidationError(String.format(mBundle.getString("invalid_dest_dir"), mOutput));
        }

        return mValidationErrorBuilder.length() == 0;
    }

    public void setBindServices(boolean bindServices) {
        mBindServices = bindServices;
    }

    public void setCompress(int compress) {
        mCompress = compress;
    }

    public void setDescription(String description) {
        mDescription = description;
    }

    public void setDryRun(boolean dryRun) {
        mDryRun = dryRun;
    }

    public void setEndian(int endian) {
        mEndian = endian;
    }

    public void setIgnoreSigning(boolean ignoreSigning) {
        mIgnoreSigning = ignoreSigning;
    }

    public void setJlink(File jlink) {
        mJlink = jlink;
    }

    public void setLastRun(long lastRun) {
        mLastRun = lastRun;
    }

    public void setName(String name) {
        mName = name;
    }

    public void setNoHeaders(boolean noHeaders) {
        mNoHeaders = noHeaders;
    }

    public void setNoManPages(boolean noManPages) {
        mNoManPages = noManPages;
    }

    public void setOutput(File dest) {
        mOutput = dest;
    }

    public void setSourceDir(File source) {
        mSourceDir = source;
    }

    public void setStripDebug(boolean stripDebug) {
        mStripDebug = stripDebug;
    }

    @Override
    public String toString() {
        return mName;
    }

    private void addValidationError(String string) {
        mValidationErrorBuilder.append(string).append("\n");
    }

}
