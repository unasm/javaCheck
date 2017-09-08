package com.sensor.controller;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.sensor.common.Constants;
import com.sensor.queryengine.UserRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.ArrayList;

/**
 * Created by tianyi on 08/08/2017.
 */

@Controller
@RequestMapping("/api/")
public class InfoController extends AbstractController {
    private static final Logger logger = LoggerFactory.getLogger(InfoController.class);
    //private static final Logger log = Logger.getLogger(InfoController.class.getName());

    public InfoController (){

    }

    /**
     * 实现json 转换为 csv的 功能
     *
     * 但是不具有通用性
     *
     * @param inputStream
     * @param writer
     * @throws IOException
     */
    private void processUserStream(InputStream inputStream, Writer writer) throws  IOException{
        JsonParser json = Constants.DEFAULT_OBJECT_MAPPER.getFactory().createParser(inputStream);
        ArrayList arr = new ArrayList();
        while (true) {
            label:
            while(json.nextToken() != JsonToken.END_OBJECT) {
                String name = json.getCurrentName();
                if ("column_name".equals(name)) {
                    json.nextToken();
                    while(json.nextToken() != JsonToken.END_ARRAY) {
                        arr.add(json.getText());
                    }
                    System.out.println("end of while");
                }
            }
            return;
        }
    }

    @RequestMapping(
            value = {"/user"}
            //method = {RequestMethod.POST}
    )
    //@ResponseBody
    //public Map<Object, Object> getUserList(@RequestParam(value = "format", required = false)String format, @RequestBody UserRequest request, HttpServletResponse response) {
    public void getUserList(
            @RequestParam(value = "format", required = false) String format,
            @RequestBody UserRequest request,
            HttpServletResponse response) throws ServletException, IOException {
    //public Map<Object, Object> getUserList(@RequestParam(value = "format", required = false)String format) {
        logger.info("infoController_format : {}", format);
        this.getUserList(request, response, format, request.isAllPage());
    }
}
