package com.sensor.common.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.IOException;

/**
 * Created by tianyi on 01/08/2017.
 */
public class JodaDatetimeSerializer {
    private static final DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd");

    public JodaDatetimeSerializer() {
    }

    public void serialize(DateTime var1, JsonGenerator var2, SerializerProvider var3) throws IOException, JsonProcessingException {
        var2.writeString(var1.toString(formatter));
    }
}
