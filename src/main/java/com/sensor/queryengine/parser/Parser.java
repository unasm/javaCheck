package com.sensor.queryengine.parser;

import com.sensor.common.DataType;
import com.sensor.common.request.Field;
import com.sensor.common.util.DateUnit;
import com.sensor.db.bean.PropertyBean;
import com.sensor.queryengine.QueryContext;
import com.sensor.queryengine.RequestElementCondition;
import com.sensor.queryengine.RequestElementFilter;
import com.sensor.queryengine.error.*;
import com.sensor.service.MetaDataService;
import org.apache.commons.collections.CollectionUtils;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by tianyi on 21/08/2017.
 */
public abstract class Parser {
    public static final MetaDataService metaDataService = MetaDataService.getInstance();

    public Parser() {
    }

    public abstract void parser(QueryContext var1) throws Exception;

    public static List<String> parseFieldsFromFilter(RequestElementFilter var0) throws ParameterException, SQLException {
        if(var0 != null && CollectionUtils.isNotEmpty(var0.getConditions())) {
            Iterator var1 = var0.getConditions().iterator();

            label166:
            while(true) {
                RequestElementCondition var2;
                do {
                    if(!var1.hasNext()) {
                        break label166;
                    }

                    var2 = (RequestElementCondition)var1.next();
                } while(var2 == null);

                if(var2.getParams() == null) {
                    var2.setParams(Collections.emptyList());
                }

                Field var3 = Field.of(var2.getField());
                PropertyBean var4 = MetaDataService.currentProject().getPropertyByField(var3);
                if(var4.getDataType() == DataType.NUMBER.getIndex()) {
                    for(int var5 = 0; var5 < var2.getParams().size(); ++var5) {
                        try {
                            Double.valueOf(var2.getParams().get(var5).toString());
                        } catch (Exception var11) {
                            throw new DataTypeInvalidException("number", "string", ErrorCode.PARAMETER_FORMAT_ERROR);
                        }
                    }
                }

                String var13 = var2.getFunction().toLowerCase();
                byte var7 = -1;
                switch(var13.hashCode()) {
                    case -1946536287:
                        if(var13.equals("notcontain")) {
                            var7 = 13;
                        }
                        break;
                    case -1847027023:
                        if(var13.equals("absolutebetween")) {
                            var7 = 1;
                        }
                        break;
                    case -1710951492:
                        if(var13.equals("relativebetween")) {
                            var7 = 7;
                        }
                        break;
                    case -1232567915:
                        if(var13.equals("relative_between")) {
                            var7 = 8;
                        }
                        break;
                    case -1179132488:
                        if(var13.equals("istrue")) {
                            var7 = 21;
                        }
                        break;
                    case -1155942080:
                        if(var13.equals("absolute_between")) {
                            var7 = 2;
                        }
                        break;
                    case -1039680337:
                        if(var13.equals("notset")) {
                            var7 = 24;
                        }
                        break;
                    case -955488651:
                        if(var13.equals("notinclude")) {
                            var7 = 17;
                        }
                        break;
                    case -314807481:
                        if(var13.equals("absolute_before")) {
                            var7 = 15;
                        }
                        break;
                    case -216634360:
                        if(var13.equals("between")) {
                            var7 = 0;
                        }
                        break;
                    case -178731950:
                        if(var13.equals("relative_before")) {
                            var7 = 4;
                        }
                        break;
                    case 3365:
                        if(var13.equals("in")) {
                            var7 = 18;
                        }
                        break;
                    case 3318169:
                        if(var13.equals("less")) {
                            var7 = 9;
                        }
                        break;
                    case 96757556:
                        if(var13.equals("equal")) {
                            var7 = 19;
                        }
                        break;
                    case 100509432:
                        if(var13.equals("isset")) {
                            var7 = 23;
                        }
                        break;
                    case 108603145:
                        if(var13.equals("rlike")) {
                            var7 = 12;
                        }
                        break;
                    case 283601914:
                        if(var13.equals("greater")) {
                            var7 = 10;
                        }
                        break;
                    case 426584382:
                        if(var13.equals("relative_within")) {
                            var7 = 6;
                        }
                        break;
                    case 951526612:
                        if(var13.equals("contain")) {
                            var7 = 11;
                        }
                        break;
                    case 1191309643:
                        if(var13.equals("relativebefore")) {
                            var7 = 3;
                        }
                        break;
                    case 1464014774:
                        if(var13.equals("absolutebefore")) {
                            var7 = 14;
                        }
                        break;
                    case 1582008385:
                        if(var13.equals("notequal")) {
                            var7 = 20;
                        }
                        break;
                    case 1796625975:
                        if(var13.equals("relativewithin")) {
                            var7 = 5;
                        }
                        break;
                    case 1942574248:
                        if(var13.equals("include")) {
                            var7 = 16;
                        }
                        break;
                    case 2088154681:
                        if(var13.equals("isfalse")) {
                            var7 = 22;
                        }
                }
                switch(var7) {
                    case 0:
                    case 1:
                    case 2:
                        if(var2.getParams().size() != 2) {
                            throw new FilterParameterException(var2.getFunction(), 2, ErrorCode.FILTER_PARAMETER_NUM_INVALID);
                        }
                        break;
                    case 3:
                    case 4:
                    case 5:
                    case 6:
                        if(var2.getParams().size() != 2) {
                            throw new FilterParameterException(var2.getFunction(), 2, ErrorCode.FILTER_PARAMETER_NUM_INVALID);
                        }

                        try {
                            Integer.parseInt(var2.getParams().get(0).toString());
                            DateUnit.valueOf(var2.getParams().get(1).toString().toUpperCase());
                        } catch (Exception var10) {
                            throw new FilterParameterException(var2.getFunction(), 2, ErrorCode.FILTER_PARAMETER_NUM_INVALID);
                        }

                        var2.setAbsoluteTimeByRelativeTime(new Date());
                        break;
                    case 7:
                    case 8:
                        if(var2.getParams().size() != 3) {
                            throw new FilterParameterException(var2.getFunction(), 3, ErrorCode.FILTER_PARAMETER_NUM_INVALID);
                        }

                        try {
                            Integer.parseInt(var2.getParams().get(0).toString());
                            Integer.parseInt(var2.getParams().get(1).toString());
                            DateUnit.valueOf(var2.getParams().get(2).toString().toUpperCase());
                        } catch (Exception var9) {
                            throw new FilterParameterException(var2.getFunction(), 3, ErrorCode.FILTER_PARAMETER_NUM_INVALID);
                        }

                        var2.setAbsoluteTimeByRelativeTime(new Date());
                        break;
                    case 9:
                    case 10:
                    case 11:
                    case 12:
                    case 13:
                    case 14:
                    case 15:
                        if(var2.getParams().size() != 1) {
                            throw new FilterParameterException(var2.getFunction(), 1, ErrorCode.FILTER_PARAMETER_NUM_INVALID);
                        }
                        break;
                    case 16:
                    case 17:
                    case 18:
                    case 19:
                    case 20:
                        if(var2.getParams().size() < 1) {
                            throw new FilterParameterException(var2.getFunction(), " at least 1", ErrorCode.FILTER_PARAMETER_NUM_INVALID);
                        }
                        break;
                    case 21:
                    case 22:
                    case 23:
                    case 24:
                        if(var2.getParams().size() != 0) {
                            throw new FilterParameterException(var2.getFunction(), 0, ErrorCode.FILTER_PARAMETER_NUM_INVALID);
                        }
                }
            }
        }

        ArrayList var12 = new ArrayList();
        if(var0 != null && CollectionUtils.isNotEmpty(var0.getConditions())) {
            var12.addAll((Collection)var0.getConditions().stream().map(RequestElementCondition::getField).collect(Collectors.toList()));
        }

        return var12;
    }

    protected void checkPropertyExists(List<String> var1) throws SQLException, PropertyNotExistsException, EventNotExistsException {
        Iterator var2 = var1.iterator();

        while(var2.hasNext()) {
            String var3 = (String)var2.next();
            Field var4 = Field.of(var3);
            if(MetaDataService.currentProject().getPropertyByField(var4) == null) {
                throw new PropertyNotExistsException(var4.getFieldExpression(), ErrorCode.PROPERTY_NOT_EXISTS);
            }

            if(var4.isEvent()) {
                Integer var5 = MetaDataService.currentProject().getEventIdByName(var4.getEventName());
                if(var5 == null) {
                    throw new EventNotExistsException(var4.getEventName(), ErrorCode.EVENT_NOT_EXISTS);
                }

                HashSet var6 = new HashSet();
                if(var5.intValue() != -1) {
                    var6.addAll((Collection)MetaDataService.getInstance().getPropertiesByEventId(var5).stream().map(PropertyBean::getName).collect(Collectors.toList()));
                    if(!var6.contains(var4.getName())) {
                        throw new PropertyNotExistsException(var4.getFieldExpression(), ErrorCode.PROPERTY_NOT_EXISTS);
                    }
                }
            }
        }
    }

    protected static void checkFieldEventNameValid(List<String> var0, List<String> var1, RequestElementFilter var2) throws ParameterException {
        checkFieldEventNameValid(var0, var1, var2, (RequestElementFilter)null);
    }

    protected static void checkFieldEventNameValid(List<String> var0, List<String> var1, RequestElementFilter var2, RequestElementFilter var3) throws ParameterException {
        HashSet<String> var4 = new HashSet<>(var0);
        var4.add("$Anything");
        Iterator var5;
        Field var7;
        if(CollectionUtils.isNotEmpty(var1)) {
            var5 = var1.iterator();

            while(var5.hasNext()) {
                String var6 = (String)var5.next();
                var7 = Field.of(var6);
                if(var7.isEvent() && !var4.contains(var7.getEventName())) {
                    throw new ParameterException("parameter is invalid", ErrorCode.PARAMETER_INVALID);
                }
            }
        }

        RequestElementCondition var8;
        if(var2 != null && CollectionUtils.isNotEmpty(var2.getConditions())) {
            var5 = var2.getConditions().iterator();

            while(var5.hasNext()) {
                var8 = (RequestElementCondition)var5.next();
                var7 = Field.of(var8.getField());
                if(var7.isEvent() && !var4.contains(var7.getEventName())) {
                    throw new ParameterException("parameter is invalid", ErrorCode.PARAMETER_INVALID);
                }
            }
        }

        if(var3 != null && CollectionUtils.isNotEmpty(var3.getConditions())) {
            var5 = var3.getConditions().iterator();

            while(var5.hasNext()) {
                var8 = (RequestElementCondition)var5.next();
                var7 = Field.of(var8.getField());
                if(var7.isEvent() && !var4.contains(var7.getEventName())) {
                    throw new ParameterException("parameter is invalid", ErrorCode.PARAMETER_INVALID);
                }
            }
        }
    }
}
