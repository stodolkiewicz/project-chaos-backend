package com.stodo.projectchaos.storage.utils;

import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class FileTextExtractor {

    public static String extractText(byte[] bytes) throws TikaException, IOException, SAXException {
        Tika tika = new Tika();
        tika.setMaxStringLength(2000000); // about 1100 pages
        String text = tika.parseToString(new ByteArrayInputStream(bytes));
        return (text != null) ? text.trim() : "";
    }
}
