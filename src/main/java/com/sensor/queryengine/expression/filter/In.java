package com.sensor.queryengine.expression.filter;

import com.sensor.queryengine.expression.AbstractColumn;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collection;

/**
 * sql 中in 的表达式
 * Created by tianyi on 21/08/2017.
 */
public class In extends AbstractFilter {
    private Collection values;
    private boolean reverse;

    public In(AbstractColumn idColumn, Collection idList, boolean isReverse) {
        super(idColumn);
        this.values = idList;
        // 是in 还是not in
        this.reverse = isReverse;
    }

    public String constructSql() throws Exception {
        String columnId = this.column.getId();
        ArrayList<String> idList = new ArrayList<>();

        String tmpStr;
        //for(Iterator iterator = this.values.iterator(); var3.hasNext();) {
        for (Object value : this.values) {
            //Object var4 = var3.next();
            if(value instanceof Number) {
                tmpStr = value.toString();
            } else {
                tmpStr = String.format("\'%s\'", value);
            }
            idList.add(tmpStr);
        }

        return String.format("%s %s %s", columnId, this.reverse ? "NOT IN" : "IN", "(" + StringUtils.join(idList, ',') + ")");
    }
}
