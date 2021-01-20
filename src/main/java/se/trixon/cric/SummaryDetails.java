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

import java.util.Locale;
import java.util.ResourceBundle;
import javafx.geometry.Insets;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.SystemHelper;

/**
 *
 * @author Patrik Karlström
 */
public class SummaryDetails extends TextFlow {

    private final Text mBasedOn = new Text();
    private final ResourceBundle mBundle = SystemHelper.getBundle(SummaryDetails.class, "Bundle");
    private final Text mCase = new Text();
    private final Text mDest = new Text("\n");
    private final Text mFilesFrom = new Text("mBundle.getString(files_from)");
    private final Text mOperation = new Text();
    private final Text mOptions = new Text();
    private final Text mSource = new Text("\n");
    private final Text mTo = new Text(String.format(" %s\n", Dict.TO.toString().toLowerCase(Locale.getDefault())));

    public SummaryDetails() {
        mOperation.setFill(Color.RED);
        mSource.setFill(Color.RED);
        mDest.setFill(Color.RED);

        getChildren().setAll(
                mOperation,
                mFilesFrom,
                mSource,
                mTo,
                mDest,
                mOptions,
                mBasedOn,
                mCase
        );

        final int fontSize = 16;
        getChildren().stream().filter((node) -> (node instanceof Text)).forEachOrdered((node) -> {
            ((Text) node).setFont(Font.font(fontSize));
        });
        setVisible(false);

        Font defaultFont = Font.getDefault();
        mOperation.setFont(Font.font(defaultFont.getName(), FontWeight.EXTRA_BOLD, fontSize));

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

//        mOperation.setText(profile.getCommand().toString());
//        mSource.setText(String.format("%s%s%s",
//                profile.getSourceDirAsString(),
//                File.separator,
//                profile.getFilePattern())
//        );
//
//        mDest.setText(String.format("%s%s%s\n",
//                profile.getDestDirAsString(),
//                File.separator,
//                profile.getDatePattern())
//        );
//
//        mBasedOn.setText(String.format("%s = '%s'\n",
//                Dict.DATE_SOURCE.toString(),
//                profile.getDateSource().toString()
//        ));
//
//        var sb = new StringBuilder();
//        sb.append(getBallotBox(profile.isFollowLinks())).append(Dict.FOLLOW_LINKS.toString()).append(", ");
//        sb.append(getBallotBox(profile.isRecursive())).append(Dict.RECURSIVE.toString()).append(", ");
//        sb.append(getBallotBox(profile.isReplaceExisting())).append(Dict.REPLACE.toString()).append(". ");
//        mOptions.setText(sb.toString());
//
//        String caseText = String.format("%s %s, %s %s",
//                Dict.BASENAME.toString(),
//                profile.getCaseBase(),
//                Dict.EXTENSION.toString(),
//                profile.getCaseExt()
//        );
//
//        mCase.setText(caseText);
    }

    private String getBallotBox(boolean value) {
        return value ? "☑ " : "☐ ";
    }
}
