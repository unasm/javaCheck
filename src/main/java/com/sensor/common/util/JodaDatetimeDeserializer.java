package com.sensor.common.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.IOException;

/**
 * Created by tianyi on 01/08/2017.
 */
public class JodaDatetimeDeserializer {
    private static final DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd");

    public JodaDatetimeDeserializer() {
    }

    public DateTime deserialize(JsonParser parser, DeserializationContext var2) throws IOException, JsonProcessingException {
        return formatter.parseDateTime(parser.getText());
    }
}
