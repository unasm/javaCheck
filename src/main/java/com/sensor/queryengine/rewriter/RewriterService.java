package com.sensor.queryengine.rewriter;

import com.sensor.common.DataType;
import com.sensor.common.request.Field;
import com.sensor.db.bean.EventBean;
import com.sensor.db.bean.PropertyBean;
import com.sensor.queryengine.RequestElementCondition;
import com.sensor.queryengine.RequestElementFilter;
import com.sensor.queryengine.error.ErrorCode;
import com.sensor.queryengine.error.FilterParameterException;
import com.sensor.queryengine.error.PropertyNotExistsException;
import com.sensor.queryengine.rewriter.store.RedisStore;
import com.sensor.queryengine.util.UserUtil;
import com.sensor.service.MetaDataService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Created by tianyi on 05/09/2017.
 */
public class RewriterService {
    private static final Logger logger = LoggerFactory.getLogger(RewriterService.class);
    private RedisStore redisStore = new RedisStore();
    private static RewriterService rewriterService = null;
    private static final String OS_PROPERTY_NAME = "$model";
    private static final String OS_PROPERTY_FILE = "$model.txt";
    private static final String PREDICTOR_FILE = "predictor.txt";
    private static final String PROPETY_SPLIT = "|";
    private Map<String, String> segmenterMap = new HashMap<>();
    private Map<String, String> segmenterMapRev = new HashMap<>();
    private Map<String, String> modelMap = new HashMap<>();
    private Map<String, String> modelMapRev = new HashMap<>();

    public RewriterService() {
        try {
            this.constructDictMap();
        } catch (IOException var2) {
            logger.debug("construct dict failed", var2);
        }

    }

    public static synchronized RewriterService getInstance() {
        if(null == rewriterService) {
            rewriterService = new RewriterService();
        }

        return rewriterService;
    }

    public void constructDictMap() throws IOException {
        InputStream inputStream = RewriterService.class.getClassLoader().getResourceAsStream("predictor.txt");
        Throwable var2 = null;

        String var3;
        String[] var4;
        String[] var5;
        int var6;
        int var7;
        String var8;
        String[] var9;
        try {
            //var3 = IOUtils.toString(var1);
            //byte[] array = inputStream.read();
            int length = inputStream.available();
            byte[] array = new byte[length];
            int gotLen = inputStream.read(array, 0, length);
            if (gotLen != length) {
                logger.info("rewriteMap_gotLength_error readyLen : {}, gotLen : {}", length, gotLen);
            }
            var3 = new String(array, StandardCharsets.UTF_8); // Or any encoding.
            //var3 = ByteArrayOutputStream.toString(array);
            var4 = var3.split("\n");
            //IOUtils.toString(var1);
            var5 = var4;
            var6 = var4.length;

            for(var7 = 0; var7 < var6; ++var7) {
                var8 = var5[var7];
                var9 = StringUtils.split(var8, "|");
                this.segmenterMap.put(var9[0], var9[1]);
                this.segmenterMapRev.put(var9[1], var9[0]);
            }
        } catch (Throwable var33) {
            var2 = var33;
            throw var33;
        } finally {
            if (inputStream != null) {
                if(var2 != null) {
                    try {
                        inputStream.close();
                    } catch (Throwable var29) {
                        var2.addSuppressed(var29);
                    }
                } else {
                    inputStream.close();
                }
            }

        }

        inputStream = RewriterService.class.getClassLoader().getResourceAsStream("$model.txt");
        var2 = null;

        try {
            int length = inputStream.available();
            byte[] array = new byte[length];
            int gotLen = inputStream.read(array, 0, length);
            if (gotLen != length) {
                logger.info("rewriteMap_model_error readyLen : {}, gotLen : {}", length, gotLen);
            }
            var3 = new String(array, StandardCharsets.UTF_8);
            //var3 = IOUtils.toString(inputStream);

            var4 = var3.split("\n");
            var5 = var4;
            var6 = var4.length;

            for(var7 = 0; var7 < var6; ++var7) {
                var8 = var5[var7];
                var9 = StringUtils.split(var8, "|");
                this.modelMap.put(var9[0], var9[1]);
                this.modelMapRev.put(var9[1], var9[0]);
            }
        } catch (Throwable var31) {
            var2 = var31;
            throw var31;
        } finally {
            if(inputStream != null) {
                if(var2 != null) {
                    try {
                        inputStream.close();
                    } catch (Throwable var30) {
                        var2.addSuppressed(var30);
                    }
                } else {
                    inputStream.close();
                }
            }

        }
    }

    public RequestElementFilter rewriteElementFilter(RequestElementFilter var1) throws Exception {
        if(var1 != null && !CollectionUtils.isEmpty(var1.getConditions())) {
            Iterator var2 = var1.getConditions().iterator();

            while(true) {
                RequestElementCondition var3;
                PropertyBean var5;
                do {
                    if(!var2.hasNext()) {
                        return var1;
                    }

                    var3 = (RequestElementCondition)var2.next();
                    Field var4 = Field.of(var3.getField());
                    var5 = MetaDataService.currentProject().getPropertyByField(var4);
                    if(var5 == null) {
                        throw new PropertyNotExistsException(var4.getFieldExpression(), ErrorCode.PROPERTY_NOT_EXISTS);
                    }
                } while(!var5.hasDict());

                String var6 = var3.getFunction().toLowerCase();
                if(!var6.equals("equal") && !var6.equals("notequal") && !var6.equals("isset") && !var6.equals("notset") && !var6.equals("isempty") && !var6.equals("isnotempty")) {
                    throw new FilterParameterException(var6, ErrorCode.FILTER_FUNCTION_INVALID);
                }

                // @// TODO: 05/09/2017  以下的代码好像没有意义，没有改变任何返回值
                List var7 = var3.getParams();
                int var8 = 0;

                for(Iterator var9 = var7.iterator(); var9.hasNext(); ++var8) {
                    Object var10 = var9.next();
                    if(!var5.getName().equals("$user_id") && !var5.getName().equals("$id")) {
                        String var12 = this.getDimensionValue(var5, true, String.valueOf(var10));
                        if(var12 != null) {
                            if(var5.getDataType() == DataType.NUMBER.getIndex()) {
                                var7.set(var8, Double.valueOf(var12));
                            } else {
                                var7.set(var8, var12);
                            }
                        }
                    } else {
                        Object var11 = UserUtil.convertDistinctIdToUserId(Collections.singletonList(String.valueOf(var10))).get(0);
                        var7.set(var8, var11);
                    }
                }
            }
        } else {
            return null;
        }
    }

    public String getDimensionValue(PropertyBean var1, String var2) throws Exception {
        return this.getDimensionValue(var1, false, var2);
    }

    public String getDimensionValue(PropertyBean var1, boolean var2, String var3) throws Exception {
        String var4 = null;
        if(var1.isSegmenter()) {
            if(!var2) {
                var4 = this.segmenterMap.get(var3);
            } else {
                var4 = this.segmenterMapRev.get(var3);
            }
        } else if(var1.getName().equals("$model")) {
            if(!var2) {
                var4 = this.modelMap.get(var3);
            } else {
                var4 = this.modelMapRev.get(var3);
            }
        } else {
            Collection var5;
            Iterator var6;
            EventBean var7;
            if(var1.getName().equals("$event_id$session")) {
                var5 = MetaDataService.currentProject().getAllEvents();
                if(!var2) {
                    var6 = var5.iterator();

                    while(var6.hasNext()) {
                        var7 = (EventBean)var6.next();
                        if(var7.getId() == Integer.valueOf(var3)) {
                            var4 = var7.getCname();
                        }
                    }
                } else {
                    var6 = var5.iterator();

                    while(var6.hasNext()) {
                        var7 = (EventBean)var6.next();
                        if(var7.getCname().equals(var3)) {
                            var4 = String.valueOf(var7.getId());
                        }
                    }
                }
            } else if(var1.getName().equals("$event_id")) {
                var5 = MetaDataService.currentProject().getAllEvents();
                if(!var2) {
                    var6 = var5.iterator();

                    while(var6.hasNext()) {
                        var7 = (EventBean)var6.next();
                        if(var7.getId() == Integer.valueOf(var3)) {
                            var4 = var7.getName();
                        }
                    }
                } else {
                    var6 = var5.iterator();

                    while(var6.hasNext()) {
                        var7 = (EventBean)var6.next();
                        if(var7.getName().equals(var3)) {
                            var4 = String.valueOf(var7.getId());
                        }
                    }
                }
            }
        }

        if(StringUtils.isNotEmpty(var4)) {
            return var4;
        } else {
            var4 = this.redisStore.getDimensionDictValue(var1, var2, var3);
            return var4;
        }
    }

    public List<String> getDimensionDictList(PropertyBean var1) {
        if(var1.isSegmenter()) {
            return new ArrayList<>(this.segmenterMap.values());
        } else if(var1.getName().equals("$model")) {
            return new ArrayList<>(this.modelMap.values());
        } else if(var1.getName().equals("$event_id$session")) {
            Collection var2 = MetaDataService.currentProject().getAllEvents();
            ArrayList var3 = new ArrayList();
            Iterator var4 = var2.iterator();

            while(var4.hasNext()) {
                EventBean var5 = (EventBean)var4.next();
                if(var5.isVisible() && !var5.isVirtual()) {
                    var3.add(var5.getCname());
                }
            }

            return var3;
        } else {
            return new ArrayList<>();
        }
    }

    public RedisStore getRedisStore() {
        return this.redisStore;
    }
}
