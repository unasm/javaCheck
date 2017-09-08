package com.sensor.common.config;

import com.sensor.db.OLAPEngineType;

/**
 * Created by tianyi on 31/08/2017.
 */
public class OLAPEngineConfigInfo {
    private OLAPEngineType engineType;
    private VerticaConfigInfo verticaConfigInfo;
    private ImpalaConfigInfo impalaConfigInfo;

    public OLAPEngineConfigInfo() {
    }

    public VerticaConfigInfo getVerticaConfigInfo() {
        return this.verticaConfigInfo;
    }

    public void setVerticaConfigInfo(VerticaConfigInfo configInfo) {
        this.verticaConfigInfo = configInfo;
    }

    public ImpalaConfigInfo getImpalaConfigInfo() {
        return this.impalaConfigInfo;
    }

    public void setImpalaConfigInfo(ImpalaConfigInfo configInfo) {
        this.impalaConfigInfo = configInfo;
    }

    public OLAPEngineType getEngineType() {
        return this.engineType;
    }

    public void setEngineType(OLAPEngineType type) {
        this.engineType = type;
    }
}
