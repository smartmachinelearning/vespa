// Copyright 2017 Yahoo Holdings. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
package com.yahoo.config.application;

import com.yahoo.config.provision.Environment;
import com.yahoo.config.provision.RegionName;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Test;
import org.w3c.dom.Document;

import javax.xml.transform.TransformerException;
import java.io.StringReader;

/**
 * @author bratseth
 */
public class MultiOverrideProcessorTest {

    static {
        XMLUnit.setIgnoreWhitespace(true);
    }

    private static final String input =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<services version=\"1.0\" xmlns:deploy=\"vespa\">\n" +
            "    <container id='qrserver' version='1.0'>\n" +
            "        <component id=\"comp-B\" class=\"com.yahoo.ls.MyComponent\" bundle=\"lsbe-hv\">\n" +
            "            <config name=\"ls.config.resource-pool\">\n" +
            "                <resource>\n" +
            "                    <item>\n" +
            "                        <id>comp-B-item-0</id>\n" +
            "                        <type></type>\n" +
            "                    </item>\n" +
            "                    <item deploy:environment=\"dev perf test staging prod\" deploy:region=\"us-west-1 us-east-3\">\n" +
            "                        <id>comp-B-item-1</id>\n" +
            "                        <type></type>\n" +
            "                    </item>\n" +
            "                    <item>\n" +
            "                        <id>comp-B-item-2</id>\n" +
            "                        <type></type>\n" +
            "                    </item>\n" +
            "                </resource>\n" +
            "            </config>\n" +
            "        </component>\n" +
            "        <nodes deploy:environment=\"dev\" count=\"1\"/>\n" +
            "    </container>\n" +
            "</services>\n";

    @Test
    public void testParsingDev() throws TransformerException {
        String expected =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<services version=\"1.0\" xmlns:deploy=\"vespa\">\n" +
                "    <container id='qrserver' version='1.0'>\n" +
                "        <component id=\"comp-B\" class=\"com.yahoo.ls.MyComponent\" bundle=\"lsbe-hv\">\n" +
                "            <config name=\"ls.config.resource-pool\">\n" +
                "                <resource>\n" +
                "                    <item>\n" +
                "                        <id>comp-B-item-0</id>\n" +
                "                        <type></type>\n" +
                "                    </item>\n" +
                "                    <item>\n" +
                "                        <id>comp-B-item-1</id>\n" +
                "                        <type></type>\n" +
                "                    </item>\n" +
                "                    <item>\n" +
                "                        <id>comp-B-item-2</id>\n" +
                "                        <type></type>\n" +
                "                    </item>\n" +
                "                </resource>\n" +
                "            </config>\n" +
                "        </component>\n" +
                "        <nodes count=\"1\"/>\n" +
                "    </container>\n" +
                "</services>";
        assertOverride(Environment.dev, RegionName.defaultName(), expected);
    }

    private void assertOverride(Environment environment, RegionName region, String expected) throws TransformerException {
        Document inputDoc = Xml.getDocument(new StringReader(input));
        Document newDoc = new OverrideProcessor(environment, region).process(inputDoc);
        TestBase.assertDocument(expected, newDoc);
    }

}
