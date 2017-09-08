package com.sensor.controller;

import com.google.common.io.CountingOutputStream;
import com.sensor.queryengine.QueryEngineUtil;
import com.sensor.queryengine.QueryRequest;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

/**
 *
 * 数据传输的控制，通过New 线程，并且通过pip传输内容
 * Created by tianyi on 01/08/2017.
 */
public class AbstractController {
    private static final Logger logger = LoggerFactory.getLogger(AbstractController.class);
    public AbstractController() {}

    //public void getUserList(QueryRequest request, HttpServletResponse response, final String format, boolean isAllPage) {
    public void getUserList(QueryRequest request, HttpServletResponse response, final String format, boolean isAllPage) {
        //public void getUserList(QueryRequest request, HttpServletResponse response, final String format, boolean isAllPage) {
        //final PrintWriter printerWriter = null;
        if (request.getLimit() == null && !isAllPage) {
            request.setLimit(1000L);
        }
        CountingOutputStream cos = null;
        PrintWriter tmpWriter = null;
        ServletOutputStream outputStream = null;
        try {
            outputStream = response.getOutputStream();
            cos = new CountingOutputStream(outputStream);
            tmpWriter = new PrintWriter(new OutputStreamWriter(cos, "UTF-8"));
        } catch (IOException ex) {
            logger.warn("init_writer_error : {}",ex);
            return ;
        }

        final PrintWriter  printWriter = tmpWriter;
        try {
            if (StringUtils.isNotBlank(format) && format.toLowerCase().equals("csv")) {
                response.setContentType("text/csv; charset=UTF-8");
            } else {
                response.setContentType("application/json");
            }
            response.setContentType("UTF-8");

            final PipedInputStream pipedInputStream = new PipedInputStream(10240);
            PipedOutputStream pipedOutputStream = new PipedOutputStream(pipedInputStream);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(pipedOutputStream);

            Thread thread = new Thread() {
                public void run() {
                    super.run();
                    try {
                        printWriter.write(IOUtils.toString(pipedInputStream, "UTF-8"));
                    } catch (Exception ex) {
                        System.out.println("failed to execute");
                        AbstractController.logger.warn("exception_Thread {}, format : {}", ex, format);
                    }
                }
            };
            thread.start();
            QueryEngineUtil.queryUserList(request, outputStreamWriter, request.getUseCache());
            outputStreamWriter.close();
            thread.join();

            outputStream.flush();
            logger.info("get_user_list csv output {} bytes. format : {} ", cos.getCount(), format);
            printWriter.close();
            response.setStatus(200);
            //outputStream.close();
        } catch (Exception ex) {
            logger.warn("get segmentation user list csv failed", ex);
            response.setStatus(500);
        } finally {
            printWriter.close();
            tmpWriter.close();
        }
    }
}
