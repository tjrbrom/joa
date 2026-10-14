package com.x.hhqs.joa.common.sharedfileservice.impl;

import com.github.sardine.DavResource;
import com.github.sardine.Sardine;
import com.github.sardine.impl.SardineException;
import com.github.sardine.model.ObjectFactory;
import com.x.hhqs.joa.common.sharedfileservice.utils.WebdavFileServiceConstants;
import org.apache.http.util.CharArrayBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.sax.SAXSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WebdavSardineUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebdavSardineUtil.class);

    private static final int DEFAULT_BUFFER_SIZE = 4096;

    private static final JAXBContext JAXB_CONTEXT;

    static {
        try {
            JAXB_CONTEXT = JAXBContext.newInstance(ObjectFactory.class);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T unmarshal(InputStream inputStream) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();) {
            // copy stream content to baos to use multiple times
            byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
            int len;
            while ((len = inputStream.read(buffer)) > -1) {
                baos.write(buffer, 0, len);
            }
            baos.flush();
            
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Unmarshalling DAV response: \n{}", getDAVResponse(baos));
            }
            return doUnmarshal(baos);
        }
    }

    public static boolean exists(Sardine sardine, String url) throws IOException {
        try {
            String requestUrl = WebdavSardineUtil.encodeURL(url);
            sardine.list(requestUrl);
            return true;
        } catch(SardineException e){
            if ("status code: 404, reason phrase: Unexpected response (404 Not Found)".equals(e.getMessage())) {
                return false;
            }
            throw e;
        }
    }

    public static List<DavResource> list(Sardine sardine, String url) throws IOException {
        String requestUrl = WebdavSardineUtil.encodeURL(url);
        return sardine.list(requestUrl);
    }

    public static InputStream get(Sardine sardine, String url) throws IOException {
        String requestUrl = WebdavSardineUtil.encodeURL(url);
        return sardine.get(requestUrl);
    }

    public static void put(Sardine sardine, String url, InputStream inputStream) throws IOException {
        String requestUrl = WebdavSardineUtil.encodeURL(url);
        sardine.put(requestUrl, inputStream);
    }

    public static void createDirectory(Sardine sardine, String url) throws IOException {
        String requestUrl = WebdavSardineUtil.encodeURL(url);
        sardine.createDirectory(requestUrl);
    }

    public static String encodeURL(String url) throws UnsupportedEncodingException {
        if (url == null) {
            throw new IllegalArgumentException("URL cannot be null");
        }
        // url must be a full url, e.g: https://ip:port/webdav/x-core-ac/
        // because sardine.exists also requires a full url to work correctly
        Pattern pattern = Pattern.compile("^http(s)?://.+$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(url);
        boolean matchFound = matcher.find();
        if (!matchFound) {
            throw new IllegalArgumentException("URL must be a full URL, e.g: https://ip:port/webdav/x-core-ac/, current URL: " + url);
        }
        String encodedUrl = url;
        // As the url="https://ip:port/webdav/x-core-ac/"
        // parts1 will be ["https", "ip:port/webdav/x-core-ac/"]
        // parts2 will be ["ip:port", "webdav", "x-core-ac"]
        List<String> parts1 = new ArrayList<>(Arrays.asList(encodedUrl.split(WebdavFileServiceConstants.PROTOCOL_SEPARATOR)));
        List<String> parts2 = new ArrayList<>((Arrays.asList(parts1.get(1).split(WebdavFileServiceConstants.SLASH))));
        String protocol = parts1.get(0);
        String host = parts2.remove(0);
        encodedUrl = protocol + WebdavFileServiceConstants.PROTOCOL_SEPARATOR + host;
        for (String part : parts2) {
            encodedUrl += WebdavFileServiceConstants.SLASH + URLEncoder.encode(part, "UTF-8").replace("+", "%20");
        }
        if (url.endsWith(WebdavFileServiceConstants.SLASH)) {
            encodedUrl += WebdavFileServiceConstants.SLASH;
        }
        return encodedUrl;
    }

    private static <T> T doUnmarshal(ByteArrayOutputStream baos) throws IOException {
        try (InputStream in = new ByteArrayInputStream(baos.toByteArray());) {
            Unmarshaller unmarshaller = createUnmarshaller();
            XMLReader reader = XMLReaderFactory.createXMLReader();
            try {
                reader.setFeature("http://xml.org/sax/features/external-general-entities", Boolean.FALSE);
            } catch (SAXException e) {
                // NOSONAR: Not all parsers will support this attribute
                LOGGER.trace("Not all parsers will support this attribute", e);
            }
            try {
                reader.setFeature("http://xml.org/sax/features/external-parameter-entities", Boolean.FALSE);
            } catch (SAXException e) {
                // NOSONAR: Not all parsers will support this attribute
                LOGGER.trace("Not all parsers will support this attribute", e);
            }
            try {
                reader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", Boolean.FALSE);
            } catch (SAXException e) {
                // NOSONAR: Not all parsers will support this attribute
                LOGGER.trace("Not all parsers will support this attribute", e);
            }
            try {
                reader.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, Boolean.TRUE);
            } catch (SAXException e) {
                // NOSONAR: Not all parsers will support this attribute
                LOGGER.trace("Not all parsers will support this attribute", e);
            }
            return (T) unmarshaller.unmarshal(new SAXSource(reader, new InputSource(in)));
        } catch (SAXException e) {
            LOGGER.error("Error occurred when unmarshalling DAV response: \n", getDAVResponse(baos), e);

            throw new RuntimeException(e.getMessage(), e);
        } catch (JAXBException e) {
            LOGGER.error("Error occurred when unmarshalling DAV response: \n", getDAVResponse(baos), e);
            
            // Server does not return any valid WebDAV XML that matches our JAXB context
            IOException failure = new IOException("Not a valid DAV response");
            // Backward compatibility
            failure.initCause(e);
            throw failure;
        }
    }

    private static String getDAVResponse(ByteArrayOutputStream baos) throws IOException {
        try (InputStream printInputStream = new ByteArrayInputStream(baos.toByteArray());
                final Reader streamReader = new InputStreamReader(printInputStream, "UTF-8");) {
            final CharArrayBuffer printBuffer = new CharArrayBuffer(DEFAULT_BUFFER_SIZE);
            final char[] tmp = new char[1024];
            int l;
            while ((l = streamReader.read(tmp)) != -1) {
                printBuffer.append(tmp, 0, l);
            }
            return printBuffer.toString();
        }
    }

    private static Unmarshaller createUnmarshaller() throws IOException {
        try {
            return JAXB_CONTEXT.createUnmarshaller();
        } catch (JAXBException e) {
            throw new IOException(e.getMessage(), e);
        }
    }
}