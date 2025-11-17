package com.example.javaea_beadando.bank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import soapclient.MNBArfolyamServiceSoap;
import soapclient.MNBArfolyamServiceSoapImpl;

@SpringBootApplication
@Controller
public class BankApplication {
    public static void main(String[] args) {
        SpringApplication.run(BankApplication.class, args);
    }

    @GetMapping("/soap")
    public String soapForm(Model model) {
        model.addAttribute("param", new MessagePrice());
        return "SOAP_from";
    }

    @PostMapping("/soap")
    public String soapResult(@ModelAttribute MessagePrice messagePrice, Model model) throws Exception {
        // Validáció: end date ne legyen nagyobb, mint a mai nap
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalDate endDate = java.time.LocalDate.parse(messagePrice.getEndDate());

        if (endDate.isAfter(today)) {
            messagePrice.setEndDate(today.toString());
        }

        MNBArfolyamServiceSoapImpl impl = new MNBArfolyamServiceSoapImpl();
        MNBArfolyamServiceSoap service = impl.getCustomBindingMNBArfolyamServiceSoap();

        String xmlData = service.getExchangeRates(messagePrice.getStartDate(),
                                                  messagePrice.getEndDate(),
                                                  messagePrice.getCurrency());

        // XML parse-olás
        java.util.List<String> dates = new java.util.ArrayList<>();
        java.util.List<Double> rates = new java.util.ArrayList<>();

        try {
            javax.xml.parsers.DocumentBuilderFactory factory = javax.xml.parsers.DocumentBuilderFactory.newInstance();
            javax.xml.parsers.DocumentBuilder builder = factory.newDocumentBuilder();
            org.w3c.dom.Document doc = builder.parse(new java.io.ByteArrayInputStream(xmlData.getBytes()));

            org.w3c.dom.NodeList dayNodes = doc.getElementsByTagName("Day");
            for (int i = 0; i < dayNodes.getLength(); i++) {
                org.w3c.dom.Element dayElement = (org.w3c.dom.Element) dayNodes.item(i);
                String date = dayElement.getAttribute("date");
                org.w3c.dom.NodeList rateNodes = dayElement.getElementsByTagName("Rate");

                if (rateNodes.getLength() > 0) {
                    String rateText = rateNodes.item(0).getTextContent();
                    double rate = Double.parseDouble(rateText.replace(",", "."));
                    dates.add(date);
                    rates.add(rate);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        model.addAttribute("currency", messagePrice.getCurrency());
        model.addAttribute("startDate", messagePrice.getStartDate());
        model.addAttribute("endDate", messagePrice.getEndDate());
        model.addAttribute("dates", dates);
        model.addAttribute("rates", rates);
        model.addAttribute("rawXml", xmlData);

        return "SOAP_result";
    }
}