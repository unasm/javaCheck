package com.sensor.queryengine;

/**
 * Created by tianyi on 01/08/2017.
 */
public interface PagingParameter {
    boolean isAllPage();

    void setAllPage(boolean var1);

    Integer getNumPerPage();

    void setNumPerPage(Integer var1);

    Integer getPage();

    void setPage(Integer var1);
}
