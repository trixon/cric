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

import java.util.HashSet;
import javafx.geometry.Insets;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

/**
 *
 * @author Patrik Karlström
 */
public class SummaryDetails extends TextFlow {

    private final Text mJLink = new Text();
    private final Text mJLinkHeader = new Text("\n\njlink\n");
    private final Text mOptions = new Text();
    private final Text mOutput = new Text();
    private final Text mOutputHeader = new Text("\noutput\n");

    public SummaryDetails() {
        setVisible(false);
        setPadding(new Insets(8));
    }

    public SummaryDetails(Profile profile) {
        this();
        load(profile);
    }

    void load(Profile profile) {
        setVisible(profile != null);
        if (profile == null) {
            return;
        }

        var sb = new StringBuilder();
        sb.append(getBallotBox(profile.isBindServices())).append("bind-services").append(", ");
        sb.append(getBallotBox(profile.isIgnoreSigning())).append("ignore-signing-information").append(", ");
        sb.append(getBallotBox(profile.isNoHeaders())).append("no-header-files").append(", ");
        sb.append(getBallotBox(profile.isNoManPages())).append("no-man-pages").append(", ");
        sb.append(getBallotBox(profile.isStripDebug())).append("strip-debug");

        mOptions.setText(sb.toString());
        mJLink.setText(profile.getJlink().getPath());
        mOutput.setText(profile.getOutput().getPath());

        getChildren().setAll(
                mOptions,
                mJLinkHeader,
                mJLink,
                mOutputHeader,
                mOutput
        );

        HashSet<Text> headerTexts = new HashSet<>();
        headerTexts.add(mJLinkHeader);
        headerTexts.add(mOutputHeader);

        for (var modulePath : profile.getModulePaths()) {
            Text path = new Text("\n" + modulePath.getDirectory().getPath());
            headerTexts.add(path);
            Text modules = new Text("\n" + String.join(", ", modulePath.getSelectedModules()));

            getChildren().addAll(path, modules);
        }

        var defaultFont = Font.getDefault();
        double fontSize = defaultFont.getSize() * 1.4;

        getChildren().stream().filter((node) -> (node instanceof Text)).forEachOrdered((node) -> {
            ((Text) node).setFont(Font.font(fontSize));
        });

        var headerFont = Font.font(defaultFont.getName(), FontWeight.EXTRA_BOLD, fontSize);

        for (Text text : headerTexts) {
            text.setFill(Color.RED);
            text.setFont(headerFont);
        }
    }

    private String getBallotBox(boolean value) {
        return value ? "☑ " : "☐ ";
    }
}
