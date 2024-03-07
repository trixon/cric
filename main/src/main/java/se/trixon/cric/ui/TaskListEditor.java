/*
 * Copyright 2023 Patrik Karlström <patrik@trixon.se>.
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

import java.awt.Dimension;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;
import javafx.application.Platform;
import javafx.scene.Scene;
import javax.swing.SwingUtilities;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import se.trixon.almond.nbp.fx.FxDialogPanel;
import se.trixon.almond.nbp.fx.NbEditableList;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.control.editable_list.EditableList;
import se.trixon.almond.util.swing.SwingHelper;
import se.trixon.cric.core.ExecutorManager;
import se.trixon.cric.core.StorageManager;
import static se.trixon.cric.core.StorageManager.GSON;
import se.trixon.cric.core.Task;
import se.trixon.cric.core.TaskManager;

/**
 *
 * @author Patrik Karlström <patrik@trixon.se>
 */
public class TaskListEditor {

    private EditableList<Task> mEditableList;
    private final ExecutorManager mExecutorManager = ExecutorManager.getInstance();
    private final Dimension mPreferredSize = SwingHelper.getUIScaledDim(740, 480);
    private final TaskEditor mTaskEditor;
    private final Scene mTaskEditorScene;
    private final TaskManager mTaskManager = TaskManager.getInstance();

    public TaskListEditor() {
        init();

        var insets = FxHelper.getUIScaledInsets(8, 8, 0, 8);

        mTaskEditor = new TaskEditor();
        mTaskEditor.setPadding(insets);
        mTaskEditorScene = new Scene(mTaskEditor);
    }

    public EditableList<Task> getEditableList() {
        return mEditableList;
    }

    void editTask(String title, Task task) {
        var dialogPanel = new FxDialogPanel() {
            @Override
            protected void fxConstructor() {
                setScene(mTaskEditorScene);
            }
        };

        dialogPanel.setPreferredSize(mPreferredSize);
        dialogPanel.initFx();
        var d = new DialogDescriptor(dialogPanel, Objects.toString(title, Dict.EDIT.toString()));
        d.setValid(false);
        dialogPanel.setNotifyDescriptor(d);
        mTaskEditor.load(task, d);

        SwingUtilities.invokeLater(() -> {
            if (DialogDescriptor.OK_OPTION == DialogDisplayer.getDefault().notify(d)) {
                Platform.runLater(() -> {
                    var editedItem = mTaskEditor.save();
                    postEdit(mTaskManager.getById(editedItem.getId()));
                });
            }
        });
    }

    private void init() {
        mEditableList = new NbEditableList.Builder<Task>()
                .setIconSize(FxHelper.getUIScaled(22))
                .setItemSingular(Dict.TASK.toString())
                .setItemPlural(Dict.TASKS.toString())
                .setItemsProperty(mTaskManager.itemsProperty())
                .setOnEdit((title, task) -> {
                    editTask(title, task);
                })
                .setOnRemoveAll(() -> {
                    mTaskManager.getIdToItem().clear();
                    StorageManager.save();
                })
                .setOnRemove(t -> {
                    mTaskManager.getIdToItem().remove(t.getId());
                    StorageManager.save();
                })
                .setOnClone(t -> {
                    var original = t;
                    var json = GSON.toJson(original);
                    var clone = GSON.fromJson(json, original.getClass());
                    var uuid = UUID.randomUUID().toString();
                    clone.setId(uuid);
                    clone.setLastRun(0);
                    clone.setName("%s %s".formatted(clone.getName(), LocalDate.now().toString()));
                    mTaskManager.getIdToItem().put(clone.getId(), clone);

                    StorageManager.save();

                    return mTaskManager.getById(uuid);
                })
                .setOnStart(task -> {
                    mExecutorManager.requestStart(task);
                })
                .build();

        mEditableList.getListView().setCellFactory(listView -> new TaskListCell(this));
    }

    private void postEdit(Task task) {
        mEditableList.postEdit(task);
    }

}
