package com.sensor.queryengine.rewriter;

import com.sensor.common.request.Field;
import com.sensor.db.bean.PropertyBean;
import com.sensor.queryengine.QueryContext;
import com.sensor.queryengine.UserRequest;
import com.sensor.queryengine.error.ErrorCode;
import com.sensor.queryengine.error.PropertyNotExistsException;
import com.sensor.queryengine.response.QueryResponse;
import com.sensor.service.MetaDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by tianyi on 05/09/2017.
 */
public class UserRewriter extends Rewriter{
    private static final Logger logger = LoggerFactory.getLogger(UserRewriter.class);
    public UserRewriter() {
    }

    public void rewriteRequest(QueryContext context) throws Exception {
        UserRequest request = (UserRequest)context.getQueryRequest();
        request.setFilter(rewriterService.rewriteElementFilter(request.getFilter()));
        List<String> sliceByValues = request.getSliceByValues();
        logger.info("UserRewriter_rewriteRequest : {}", sliceByValues);
        int cnt = 0;
        //if (sliceByValues != null) {
        if (sliceByValues != null && sliceByValues.size() > 0) {
            //for(Iterator var5 = sliceByValues.iterator(); var5.hasNext(); ++var4) {
            for (String value : sliceByValues) {
                //String var6 = (String)var5.next();
                value = this.getSliceByValue(value, request.getByFields().get(cnt));
                sliceByValues.set(cnt, value);
                cnt++;
            }
        }

    }

    public QueryResponse rewriteResponse(QueryContext context) throws Exception {
        return context.getQueryResponse();
    }

    public String getSliceByValue(String var1, String var2) throws Exception {
        if (var1 != null && !var1.equals("$ALL")) {
            if(var2 != null && !var2.isEmpty()) {
                Field var3 = Field.of(var2);
                PropertyBean var4 = MetaDataService.currentProject().getPropertyByField(var3);
                if(var4 == null) {
                    throw new PropertyNotExistsException(var3.getFieldExpression(), ErrorCode.PROPERTY_NOT_EXISTS);
                } else if(!var4.hasDict()) {
                    return var1;
                } else {
                    String var5 = rewriterService.getDimensionValue(var4, true, var1);
                    return var5 != null?var5:var1;
                }
            } else {
                return var1;
            }
        } else {
            return var1;
        }
    }
}
