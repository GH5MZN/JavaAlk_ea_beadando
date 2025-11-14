package com.example.java_beadando_ea_xndivegh5mzn.service;

import com.example.java_beadando_ea_xndivegh5mzn.dto.ExchangeRate;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class MNBSoapService {

    private static final String MNB_SOAP_URL = "https://www.mnb.hu/arfolyamok.asmx";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public List<ExchangeRate> getExchangeRates(String currency, LocalDate startDate, LocalDate endDate) {
        try {
            String soapRequest = createSoapRequest(currency, startDate, endDate);
            String soapResponse = sendSoapRequest(soapRequest);
            return parseExchangeRates(soapResponse, currency);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private String createSoapRequest(String currency, LocalDate startDate, LocalDate endDate) {
        String startDateStr = startDate.format(DATE_FORMAT);
        String endDateStr = endDate.format(DATE_FORMAT);

        return "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
                "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" " +
                "xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
                "  <soap:Body>\n" +
                "    <GetExchangeRates xmlns=\"http://www.mnb.hu/webservices/\">\n" +
                "      <startDate>" + startDateStr + "</startDate>\n" +
                "      <endDate>" + endDateStr + "</endDate>\n" +
                "      <currencyNames>" + currency + "</currencyNames>\n" +
                "    </GetExchangeRates>\n" +
                "  </soap:Body>\n" +
                "</soap:Envelope>";
    }

    private String sendSoapRequest(String soapRequest) throws Exception {
        URL url = new URL(MNB_SOAP_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
        connection.setRequestProperty("SOAPAction", "http://www.mnb.hu/webservices/GetExchangeRates");
        connection.setDoOutput(true);

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = soapRequest.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        // Read response
        StringBuilder response = new StringBuilder();
        try (java.io.BufferedReader br = new java.io.BufferedReader(
                new java.io.InputStreamReader(connection.getInputStream(), "utf-8"))) {
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
        }

        return response.toString();
    }

    private List<ExchangeRate> parseExchangeRates(String soapResponse, String currency) {
        List<ExchangeRate> rates = new ArrayList<>();

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            // Extract the XML content from SOAP response
            String xmlContent = extractXmlContent(soapResponse);
            if (xmlContent == null || xmlContent.isEmpty()) {
                return rates;
            }

            Document doc = builder.parse(new ByteArrayInputStream(xmlContent.getBytes()));

            NodeList dayNodes = doc.getElementsByTagName("Day");
            for (int i = 0; i < dayNodes.getLength(); i++) {
                Node dayNode = dayNodes.item(i);
                if (dayNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element dayElement = (Element) dayNode;
                    String dateStr = dayElement.getAttribute("date");
                    LocalDate date = LocalDate.parse(dateStr, DATE_FORMAT);

                    NodeList rateNodes = dayElement.getElementsByTagName("Rate");
                    for (int j = 0; j < rateNodes.getLength(); j++) {
                        Element rateElement = (Element) rateNodes.item(j);
                        String curr = rateElement.getAttribute("curr");
                        if (currency.equals(curr)) {
                            String unit = rateElement.getAttribute("unit");
                            String rateValue = rateElement.getTextContent().replace(",", ".");
                            BigDecimal rate = new BigDecimal(rateValue);

                            rates.add(new ExchangeRate(currency, date, rate, unit));
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return rates;
    }

    private String extractXmlContent(String soapResponse) {
        try {
            // Find the XML content within CDATA or direct content
            int startIndex = soapResponse.indexOf("&lt;MNBExchangeRates&gt;");
            if (startIndex == -1) {
                startIndex = soapResponse.indexOf("<MNBExchangeRates>");
            }

            int endIndex = soapResponse.indexOf("&lt;/MNBExchangeRates&gt;");
            if (endIndex == -1) {
                endIndex = soapResponse.indexOf("</MNBExchangeRates>");
            }

            if (startIndex != -1 && endIndex != -1) {
                String xmlContent = soapResponse.substring(startIndex, endIndex + 22); // +22 for closing tag
                // Decode HTML entities
                xmlContent = xmlContent.replace("&lt;", "<")
                                     .replace("&gt;", ">")
                                     .replace("&amp;", "&")
                                     .replace("&quot;", "\"");
                return xmlContent;
            }

            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
