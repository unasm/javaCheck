package com.sensor.queryengine.parser;

import com.sensor.queryengine.QueryContext;
import com.sensor.queryengine.UserRequest;
import com.sensor.queryengine.parser.result.UserParseResult;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;

/**
 * Created by tianyi on 23/08/2017.
 */
public class UserParser  extends  Parser {
    public void parser(QueryContext context) throws Exception {
        UserRequest request = (UserRequest)context.getQueryRequest();
        ArrayList<String> fields = new ArrayList<>();
        fields.addAll(parseFieldsFromFilter(request.getFilter()));
        if (CollectionUtils.isNotEmpty(request.getByFields())) {
            fields.addAll(request.getByFields());
        }

        if (StringUtils.isNotEmpty(request.getxAxisField())) {
            fields.add(request.getxAxisField());
        }

        this.checkPropertyExists(fields);
        UserParseResult parser = new UserParseResult();
        parser.setAllFields(fields);
        context.setParseResult(parser);
    }
}
