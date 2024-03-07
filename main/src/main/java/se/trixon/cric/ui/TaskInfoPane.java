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
package se.trixon.cric.ui;

import java.util.HashSet;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import se.trixon.almond.nbp.NbHelper;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.cric.core.Task;

/**
 *
 * @author Patrik Karlström
 */
public class TaskInfoPane extends VBox {

    private final Label mDescLabel = new Label();
    private final Label mNameLabel = new Label();
    private final SummaryDetails mSummaryDetails = new SummaryDetails();

    public TaskInfoPane() {
        setAlignment(Pos.TOP_LEFT);
        setPadding(new Insets(FxHelper.getUIScaled(8)));
        getChildren().setAll(
                mNameLabel,
                mDescLabel,
                mSummaryDetails
        );

        var fontFamily = Font.getDefault().getFamily();
        var fontSize = Font.getDefault().getSize();

        mNameLabel.setFont(Font.font(fontFamily, FontWeight.BOLD, fontSize * 1.6));
        mDescLabel.setFont(Font.font(fontFamily, FontWeight.NORMAL, fontSize * 1.3));
    }

    void load(Task task) {
        mNameLabel.setText(task.getName());
        mDescLabel.setText(task.getDescription());

        mSummaryDetails.load(task);
    }

    public class SummaryDetails extends TextFlow {

        private final Text mJLinkHeaderText = new Text("\n\njlink\n");
        private final Text mJLinkText = new Text();
        private final Text mLauncherHeaderText = new Text("\nlauncher\n");
        private final Text mLauncherText = new Text();
        private final Text mOptionsBallotsText = new Text();
        private final Text mOutputHeaderText = new Text("\noutput\n");
        private final Text mOutputText = new Text();

        public SummaryDetails() {
        }

        void load(Task task) {
            var sb = new StringBuilder("\n");
            var separator = ", ";
            sb.append(getBallotBox(task.isBindServices())).append(" bind-services").append(separator);
            sb.append(getBallotBox(task.isIgnoreSigning())).append(" ignore-signing-information").append(separator);
            sb.append(getBallotBox(task.isNoHeaders())).append(" no-header-files").append(separator);
            sb.append(getBallotBox(task.isNoManPages())).append(" no-man-pages").append(separator);
            sb.append(getBallotBox(task.isStripDebug())).append(" strip-debug");

            mOptionsBallotsText.setText(sb.toString());
            mJLinkText.setText(task.getJlinkString());
            mOutputText.setText(task.getOutput().getPath());
            mLauncherText.setText(task.getLauncher());

            getChildren().setAll(
                    mOptionsBallotsText,
                    mJLinkHeaderText,
                    mJLinkText,
                    mLauncherHeaderText,
                    mLauncherText,
                    mOutputHeaderText,
                    mOutputText
            );

            var headerTexts = new HashSet<Text>();
            headerTexts.add(mJLinkHeaderText);
            headerTexts.add(mOutputHeaderText);
            headerTexts.add(mLauncherHeaderText);

            var bodyTexts = new HashSet<Text>();
            bodyTexts.add(mOptionsBallotsText);
            bodyTexts.add(mJLinkText);
            bodyTexts.add(mOutputText);
            bodyTexts.add(mLauncherText);

            for (var modulePath : task.getModulePaths()) {
                var path = new Text("\n" + modulePath.getDirectory().getPath());
                headerTexts.add(path);
                var modules = new Text("\n" + String.join(separator, modulePath.getSelectedModules()));
                bodyTexts.add(modules);

                getChildren().addAll(path, modules);
            }

            var defaultFont = Font.getDefault();
            var fontSize = defaultFont.getSize() * 1.2;

            getChildren().stream()
                    .filter(node -> node instanceof Text)
                    .map(node -> (Text) node)
                    .forEachOrdered(text -> {
                        text.setFont(Font.font(fontSize));
                    });

            var headerFont = Font.font(defaultFont.getName(), FontWeight.EXTRA_BOLD, fontSize);

            for (var text : headerTexts) {
                text.setFill(Color.RED);
                text.setFont(headerFont);
            }

            for (var text : bodyTexts) {
                text.setFill(NbHelper.isNightMode() ? Color.web("#ebebeb") : Color.BLACK);
            }
        }

        private char getBallotBox(boolean checked) {
            return checked ? '◉' : '○';
        }
    }
}
