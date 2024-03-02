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

import java.text.SimpleDateFormat;
import java.util.Date;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import se.trixon.cric.Profile;

/**
 *
 * @author Patrik Karlström <patrik@trixon.se>
 */
class ProfileListCell extends ListCell<Profile> {

    private Font mDefaultFont = Font.getDefault();
    private final Label mDescLabel = new Label();
    private final Label mLastLabel = new Label();
    private VBox mMainBox;
    private final Label mNameLabel = new Label();
    private final SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat();

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
