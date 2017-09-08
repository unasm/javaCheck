package com.sensor.queryengine;

import com.sensor.common.util.DateUnit;
import com.sensor.common.DateTimeUtil;
import com.sensor.common.DateFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.*;

/**
 * Created by tianyi on 01/08/2017.
 */
public class RequestElementCondition  implements Serializable {
    private static final Logger logger = LoggerFactory.getLogger(RequestElementCondition.class);
    private String field;
    private String function;
    private List<Object> params = new ArrayList<>();

    public RequestElementCondition() {
    }

    public String getField() {
        return this.field;
    }

    public void setField(String var1) {
        this.field = var1;
    }

    public String getFunction() {
        return this.function;
    }

    public void setFunction(String func) {
        this.function = func;
    }

    public List<Object> getParams() {
        return this.params;
    }

    public void setParams(List<Object> params) {
        this.params = params;
    }

    public void setAbsoluteTimeByRelativeTime(Date var1) {
        logger.info("step_info_setAbsoluteTimeByRelativeTime {}", var1);
        String var2 = this.function;
        byte var3 = -1;
        switch(var2.hashCode()) {
            case -1710951492:
                if(var2.equals("relativebetween")) {
                    var3 = 4;
                }
                break;
            case -1232567915:
                if(var2.equals("relative_between")) {
                    var3 = 5;
                }
                break;
            case -178731950:
                if(var2.equals("relative_before")) {
                    var3 = 0;
                }
                break;
            case 426584382:
                if(var2.equals("relative_within")) {
                    var3 = 2;
                }
                break;
            case 1191309643:
                if(var2.equals("relativebefore")) {
                    var3 = 1;
                }
                break;
            case 1796625975:
                if(var2.equals("relativewithin")) {
                    var3 = 3;
                }
        }

        int var4;
        DateUnit var5;
        Date var7;
        Date var8;
        switch(var3) {
            case 0:
            case 1:
                var4 = Integer.parseInt(this.getParams().get(0).toString());
                var5 = DateUnit.valueOf(this.getParams().get(1).toString().toUpperCase());
                Date var6 = DateTimeUtil.nextDateUnit(DateTimeUtil.truncate(var1, DateUnit.DAY), -var4 + 1, var5);
                this.setFunction("absolute_before");
                // @todo 暂时注释掉，用的时候再理解具体使用
                //this.setParams(Collections.singletonList(DateFormat.SHORT_DAY_FORMAT.format(var6)));
                break;
            case 2:
            case 3:
                var4 = Integer.parseInt(this.getParams().get(0).toString());
                var5 = DateUnit.valueOf(this.getParams().get(1).toString().toUpperCase());
                var7 = DateTimeUtil.nextDateUnit(DateTimeUtil.truncate(var1, DateUnit.DAY), -var4 + 1, var5);
                var8 = DateTimeUtil.truncate(var1, DateUnit.DAY);
                this.setFunction("absolute_between");
                this.setParams(Arrays.asList(new Object[]{DateFormat.SHORT_DAY_FORMAT.format(var7), DateFormat.SHORT_DAY_FORMAT.format(var8)}));
                break;
            case 4:
            case 5:
                int var9 = Integer.parseInt(this.getParams().get(0).toString());
                int var10 = Integer.parseInt(this.getParams().get(1).toString());
                DateUnit var11 = DateUnit.valueOf(this.getParams().get(2).toString().toUpperCase());
                var7 = DateTimeUtil.nextDateUnit(DateTimeUtil.truncate(var1, DateUnit.DAY), -var9 + 1, var11);
                var8 = DateTimeUtil.nextDateUnit(DateTimeUtil.truncate(var1, DateUnit.DAY), -var10, var11);
                this.setFunction("absolute_between");
                this.setParams(Arrays.asList(new Object[]{DateFormat.SHORT_DAY_FORMAT.format(var7), DateFormat.SHORT_DAY_FORMAT.format(var8)}));
        }

    }

    public String toString() {
        return (new ToStringBuilder(this)).append("field", this.field).append("function", this.function).append("params", this.params).toString();
    }
}
