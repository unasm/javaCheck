package com.sensor.queryengine;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tianyi on 01/08/2017.
 */
public class ParseResult {
    private List<String> allEventNames = new ArrayList();
    private List<String> allFields = new ArrayList();

    public ParseResult() {
    }

    public List<String> getAllEventNames() {
        return this.allEventNames;
    }

    public void setAllEventNames(List<String> var1) {
        this.allEventNames = var1;
    }

    public List<String> getAllFields() {
        return this.allFields;
    }

    public void setAllFields(List<String> var1) {
        this.allFields = var1;
    }
}
