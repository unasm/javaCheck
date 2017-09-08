package com.sensor.queryengine.common;

import com.sensor.service.MetaDataService;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Created by tianyi on 11/08/2017.
 */
public class ProjectContextManager {
    private static ProjectContextManager instance = new ProjectContextManager();
    private ThreadLocal<Integer> projectContext = new ThreadLocal();
    private ThreadLocal<String> projectNameContext = new ThreadLocal();

    public ProjectContextManager() {
    }

    public static ProjectContextManager getInstance() {
        return instance;
    }

    public Integer getCurrentProjectId() {
        Integer var1 = (Integer)this.projectContext.get();
        return var1 == null ? Integer.valueOf(MetaDataService.getDefaultProjectId()):var1;
    }

    public String getCurrentProjectName() {
        String var1 = (String)this.projectNameContext.get();
        return var1 == null ? "default" : var1;
    }

    public void setCurrentProject(Pair<Integer, String> project) {
        this.projectContext.set(project.getLeft());
        this.projectNameContext.set(project.getRight());
    }
}
