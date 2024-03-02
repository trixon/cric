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
package se.trixon.cric.core;

import java.io.IOException;
import java.util.HashMap;
import java.util.ResourceBundle;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.windows.InputOutput;

/**
 *
 * @author Patrik Karlström <patrik@trixon.se>
 */
public class ExecutorManager {

    private final ResourceBundle mBundle = NbBundle.getBundle(ExecutorManager.class);
    private final HashMap<String, Executor> mExecutors = new HashMap<>();
    private InputOutput mInputOutput;

    public static ExecutorManager getInstance() {
        return Holder.INSTANCE;
    }

    private ExecutorManager() {
    }

    public HashMap<String, Executor> getExecutors() {
        return mExecutors;
    }

    public void requestStart(Task task) {
        if (mInputOutput != null) {
            try {
                mInputOutput.getOut().reset();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        var executor = new Executor(task);
        mExecutors.put(task.getId(), executor);
        executor.run();
    }

    private static class Holder {

        private static final ExecutorManager INSTANCE = new ExecutorManager();
    }

}
