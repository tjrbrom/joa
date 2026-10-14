package com.x.hhqs.joa.alarm.utils;

import com.x.hhqs.joa.alarm.dto.XAlarmVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.util.StreamReaderDelegate;
import java.io.StringReader;

public class NotificationParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationParser.class);

    private static final NotificationParser INSTANCE = new NotificationParser();

    private static NotificationParser test;

    public static void setInstanceForTest(NotificationParser instance) {
        test = instance;
    }

    private NotificationParser() {}

    public static NotificationParser getInstance() {
        if (test != null) {
            return test;
        }
        return INSTANCE;
    }
    
    private static XMLInputFactory xif = XMLInputFactory.newFactory();

    public XAlarmVO parseDeviceAlarm(String notification) {
        try {
            StringReader reader = new StringReader(notification);
            XMLStreamReader xsr = xif.createXMLStreamReader(reader);
            xsr = new XsiTypeReader(xsr);

            JAXBContext jaxbContext = JAXBContext.newInstance(DeviceNotification.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            DeviceNotification deviceNotification = (DeviceNotification) unmarshaller.unmarshal(xsr);
            return deviceNotification.getXAlarmVO();
        } catch (Exception exception) {
            LOGGER.error("Exception occurred while parsing device Alarm ", exception);
            return null;
        }
    }

    private static class XsiTypeReader extends StreamReaderDelegate {
        public XsiTypeReader(XMLStreamReader reader) {
            super(reader);
        }

        @Override
        public String getNamespaceURI() {
            return "";
        }

        @Override
        public String getAttributeNamespace(int index) {
            return "";
        }
    }
}