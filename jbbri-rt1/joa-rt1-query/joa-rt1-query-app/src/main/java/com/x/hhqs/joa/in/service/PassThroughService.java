package com.x.hhqs.joa.in.service;

import com.x.hhqs.joa.in.exception.Rt1QueryException;
import com.x.hhqs.joa.in.vo.passThrough.ResultTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class PassThroughService {

    @Value("${passthrough.file-path}")
    private String configPath;

    private static final Logger LOG = LoggerFactory.getLogger(PassThroughService.class);

    public ResultTemplate generateResultTemplate(String commandName) throws Rt1QueryException {
        try {
            File file = new File(configPath, commandName);
            String xml = Files.readString(Path.of(file.toURI()));

            JAXBContext context = JAXBContext.newInstance(ResultTemplate.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            StringReader reader = new StringReader(xml);
            return (ResultTemplate) unmarshaller.unmarshal(reader);
        } catch (Exception e) {
            throw new Rt1QueryException("generateResultTemplate " + commandName + "error");
        }
    }


}
