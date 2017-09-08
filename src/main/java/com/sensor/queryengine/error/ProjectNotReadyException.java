package com.sensor.queryengine.error;

/**
 * Created by tianyi on 21/08/2017.
 */
public class ProjectNotReadyException  extends ParameterException{
    public ProjectNotReadyException() {
        super("project is not ready", ErrorCode.PROJECT_NOT_READY);
    }
}
