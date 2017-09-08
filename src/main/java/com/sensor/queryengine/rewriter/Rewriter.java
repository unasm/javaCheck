package com.sensor.queryengine.rewriter;

import com.google.gson.reflect.TypeToken;
import com.sensor.common.RedisClient;
import com.sensor.common.request.RequestElementEventWithFilter;
import com.sensor.db.bean.FunnelBean;
import com.sensor.db.dao.FunnelDao;
import com.sensor.queryengine.Constants;
import com.sensor.queryengine.QueryContext;
import com.sensor.queryengine.error.ErrorCode;
import com.sensor.queryengine.error.EventNotExistsException;
import com.sensor.queryengine.error.FunnelNotExistsException;
import com.sensor.queryengine.parser.result.FunnelParseResult;
import com.sensor.queryengine.request.FunnelRequest;
import com.sensor.queryengine.response.QueryResponse;
import com.sensor.service.MetaDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by tianyi on 21/08/2017.
 */
public abstract  class Rewriter {
    public static final MetaDataService metaDataService = MetaDataService.getInstance();
    public static final RewriterService rewriterService = RewriterService.getInstance();
    private static final FunnelDao funnelDao = new FunnelDao();
    public static RedisClient redisClient = new RedisClient(3);
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public Rewriter() {
    }

    public abstract void rewriteRequest(QueryContext var1) throws Exception;

    public abstract QueryResponse rewriteResponse(QueryContext context) throws Exception;

    protected void rewriteFunnelDefine(QueryContext context) throws Exception {
        FunnelRequest request = (FunnelRequest)context.getQueryRequest();
        FunnelBean funnelRow = funnelDao.getFunnel(request.getFunnelId());
        if (null != funnelRow && funnelRow.getProjectId() == metaDataService.getCurrentProjectId()) {
            List<RequestElementEventWithFilter> steps = Constants.GSON.fromJson(funnelRow.getSteps(), (new TypeToken() {}).getType());
            //Iterator var5 = var4.iterator();

            //while(var5.hasNext()) {
            for (RequestElementEventWithFilter filter : steps) {
                //RequestElementEventWithFilter filter = (RequestElementEventWithFilter)var5.next();
                //RequestElementEventWithFilter filter = (RequestElementEventWithFilter)row;
                if (!filter.getEventName().equals("$Anything") && MetaDataService.currentProject().getEventByName(filter.getEventName()) == null) {
                    throw new EventNotExistsException(filter.getEventName(), ErrorCode.EVENT_NOT_EXISTS);
                }

                filter.setFilter(rewriterService.rewriteElementFilter(filter.getFilter()));
            }

            FunnelParseResult parseResult = new FunnelParseResult();
            parseResult.setSteps(steps);
            parseResult.setMaxConvertTime(funnelRow.getMaxConvertTime());
            context.setParseResult(parseResult);
        } else {
            throw new FunnelNotExistsException(request.getFunnelId(), ErrorCode.FUNNEL_NOT_EXISTS);
        }
    }
}
