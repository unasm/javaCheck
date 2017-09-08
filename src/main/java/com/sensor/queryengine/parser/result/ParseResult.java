package com.sensor.queryengine.parser.result;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tianyi on 23/08/2017.
 */
public class ParseResult {
    private List<String> allEventNames = new ArrayList();
    private List<String> allFields = new ArrayList();

    public ParseResult() {
    }

    public List<String> getAllEventNames() {
        return this.allEventNames;
    }

    public void setAllEventNames(List<String> eventNames) {
        this.allEventNames = eventNames;
    }

    public List<String> getAllFields() {
        return this.allFields;
    }

    public void setAllFields(List<String> fields) {
        this.allFields = fields;
    }
}
