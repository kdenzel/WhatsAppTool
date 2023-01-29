/*
 * The MIT License
 *
 * Copyright 2023 Kai Denzel.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package de.kswmd.whatsapptool.contacts;

import de.kswmd.whatsapptool.cli.Console;
import de.kswmd.whatsapptool.utils.PathResolver;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * The source is a XML-File
 *
 * @author Kai Denzel
 */
public final class MessageFileDatabase implements MessageDatabase {

    private static final Logger LOGGER = LogManager.getLogger();

    public static final String EXAMPLE_NAME = "notifications.example.xml";

    private final File xmlFile;
    private final DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
    private DocumentBuilder builder;
    private final XPath xPath = XPathFactory.newInstance().newXPath();

    private MessageFileDatabase(String filePath) throws SAXException, IOException, ParserConfigurationException {
        createExampleFile(PathResolver.getConfigDir().toString());
        this.xmlFile = new File(filePath);
        validateXMLSchema(xmlFile);
        builder = builderFactory.newDocumentBuilder();
    }

    public static MessageFileDatabase create(String filePath) throws SAXException, IOException, ParserConfigurationException {
        return new MessageFileDatabase(filePath);
    }

    public synchronized boolean createExampleFile(String pathToFolder) {
        URL inputUrl = getClass().getResource("/" + EXAMPLE_NAME);
        File dest = new File(pathToFolder + "/" + EXAMPLE_NAME);
        try {
            FileUtils.copyURLToFile(inputUrl, dest);
            return true;
        } catch (IOException ex) {
        }
        return false;
    }

    public synchronized void validateXMLSchema(File f) throws SAXException, IOException {
        SchemaFactory factory
                = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = factory.newSchema(getClass().getResource("/notifications.xsd"));
        Validator validator = schema.newValidator();
        validator.validate(new StreamSource(f));

    }

    public synchronized void validateXMLSchema(String xmlPath) throws SAXException, IOException {
        validateXMLSchema(new File(xmlPath));
    }

    @Override
    public List<Entity> getEntities() {
        List<Entity> entities = new ArrayList<>();
        String cronExpression = null;
        String identifier = null;
        int messageIndex = 0;
        try (FileInputStream fileIS = new FileInputStream(this.xmlFile)) {
            Document xmlDocument = builder.parse(fileIS);
            NodeList entityNodes = (NodeList) xPath.compile("/contacts/Entity").evaluate(xmlDocument, XPathConstants.NODESET);
            for (int i = 0; i < entityNodes.getLength(); i++) {
                Entity entity = new Entity();
                Node entityNode = entityNodes.item(i);
                String id = (String) xPath.compile("./identifier").evaluate(entityNode, XPathConstants.STRING);
                entity.setIdentifier(id);
                identifier = id;
                NodeList messageNodes = (NodeList) xPath.compile("./message").evaluate(entityNode, XPathConstants.NODESET);
                for (int j = 0; j < messageNodes.getLength(); j++) {
                    messageIndex = j + 1;
                    Node messageNode = messageNodes.item(i);
                    Message message = new Message();
                    cronExpression = (String) xPath.compile("./cronExpression").evaluate(messageNode, XPathConstants.STRING);
                    message.setCronExpression(cronExpression);
                    message.setContent((String) xPath.compile("./content").evaluate(messageNode, XPathConstants.STRING));
                    entity.addMessage(message);
                }
                entities.add(entity);
            }
        } catch (FileNotFoundException ex) {
            Console.writeLine("The file " + xmlFile.getAbsolutePath() + " was not found.");
            LOGGER.debug("No such file...", ex);
        } catch (SAXException ex) {
            Console.writeLine("Something unexpected happened.");
            LOGGER.debug("SAXException...", ex);
        } catch (IOException ex) {
            Console.writeLine("Something unexpected happened.");
            LOGGER.debug("IOException...", ex);
        } catch (XPathExpressionException ex) {
            Console.writeLine("Something unexpected happened.");
            LOGGER.debug("No valid xpath expression...", ex);
        } catch (ParseException ex) {
            Console.writeLine("The chron expression '" + cronExpression + "' in message number " + messageIndex + " with identifier " + identifier + "was invalid.");
            LOGGER.debug("Cronexpresion invalid...", ex);
        }
        return entities;
    }

}
