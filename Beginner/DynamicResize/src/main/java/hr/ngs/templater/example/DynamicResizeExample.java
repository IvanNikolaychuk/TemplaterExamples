package hr.ngs.templater.example;

import hr.ngs.templater.Configuration;
import hr.ngs.templater.TemplateDocument;

import java.awt.*;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class DynamicResizeExample {
    public static void main(final String[] args) throws Exception {
        InputStream templateStream = DynamicResizeExample.class.getResourceAsStream("/GroceryList.docx");
        File tmp = File.createTempFile("grocery", ".docx");

        String[][] array = {
                {"Apples", "Milk", "Bread"},
                {"Golden apple", "Dukat", "Black bread"},
                {"Granny smith", "Omega 3", "Alpine"},
                {"Red GMO", "Cow", "French bread"}
        };

        String[][] horizontal = {
                {"Day", "Breakfast", "Lunch", "Dinner"},
                {"Monday", "Cornflakes", "Cevapi with onions", null},
                {"Tuesday", "Serial", "Meatballs", "Apple"},
                {"Wednesday", "Cokolino", null, "Bananas"},
                {"Thursday", "Salad", null, "Fruit"},
                {"Friday", "Nutella", "Chocolate", null},
                {"Saturday", "Lasagnas", null, null},
                {"Sunday", "Cookies", "Cake", "Cake"}
        };

        String[][] vertical = {
                {"Meal", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"},
                {"Breakfast", "Cornflakes", "Serial", "Cokolino", "Salad", "Nutella", "Lasagnas", "Cookies"},
                {"Lunch", "Cevapi with onions", "Meatballs", null, null, "Chocolate", null, "Cake"},
                {"Dinner", null, "Apple", "Bananas", "Fruit", null, null, "Cake"}
        };

        try (FileOutputStream fos = new FileOutputStream(tmp);
             TemplateDocument tpl = Configuration.factory().open(templateStream, "docx", fos)) {
            //low level API call supports the dynamic resize feature
            tpl.templater().replace("myArr", array);
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("horizontal-nulls", horizontal);
            map.put("vertical-nulls", vertical);
            //high level API call supports the dynamic resize feature
            tpl.process(map);
        }
        Desktop.getDesktop().open(tmp);
    }
}
