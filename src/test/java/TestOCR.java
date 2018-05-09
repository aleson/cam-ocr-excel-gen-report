import finance.doctype.AbstractDocumentType;
import finance.doctype.CafeMegapolisDT;
import finance.doctype.DocumentTypeLinker;
import finance.doctype.entity.DocumentEntity;
import finance.doctype.item.ProductItem;
import finance.ocr.UsefulTesseract;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class TestOCR {
    private final static Logger LOGGER = Logger.getLogger(TestOCR.class);
    private UsefulTesseract aspire;

    @Before
    public void start() {
        aspire = new UsefulTesseract();
    }

    @Test
    public void fullCycle() throws IOException {
        aspire.scanTextWithImage("D:\\2311.jpg");
        String res = aspire.getResult();
        DocumentEntity entity = fillDocType(res);
        entity.calculateReport("report");
    }

    private DocumentEntity fillDocType(String text) {
        StringTokenizer st = new StringTokenizer(text, "\n");
        AbstractDocumentType documentType = null;
        ArrayList<String> rows = new ArrayList<>();
        while (st.hasMoreTokens()) {
            rows.add(st.nextToken());
        }
        List<ProductItem> products = new ArrayList<>();
        for (int i = 0; i < rows.size(); i++) {
            if (i == 2) {
                documentType = DocumentTypeLinker.INSTANCE.getDocTypeByName(rows.get(i));
            }
            if (documentType != null) {
                if (documentType instanceof CafeMegapolisDT) {
                    if (i == 4) {//parse date
                        String date = rows.get(i).substring(0, 8);
                        documentType.setDate(date);
                    }
                    if (i > 5 && !rows.get(i).contains("ИТОГ")) {//parse product items
                        ProductItem item = null;
                        if (rows.get(i + 1).contains("Код")) {//product name
                            item = new ProductItem();
                            item.setName(rows.get(i).substring(2, rows.get(i).length()));
                        }
                        if (item != null) {
                            if ((rows.get(i + 2).contains("x")
                                    || rows.get(i + 2).contains("›‹")
                                    || rows.get(i + 2).contains("><"))) {//count product
                                item.setCount(parseNumeral(rows.get(i + 2), 5));
                                item.setCost(parseNumeral(rows.get(i + 3), 2));
                            } else {//product cost
                                item.setCost(parseNumeral(rows.get(i + 2), 2));
                            }
                            products.add(item);
                        }
                    }
                    if (rows.get(i).contains("ИТОГ")) {//parse main sum
                        documentType.setProductItemList(products);
                        documentType.setSum(parseNumeral(rows.get(i + 1), 10));
                        break;
                    }
                }
            }
        }
        return documentType;
    }

    private float parseNumeral(String text, int index) {
        String sum = text;
        sum = sum.substring(index, sum.length());
        String numbers = "";
        for (int j = 1; j < sum.length(); j++) {
            try {
                Integer.parseInt(String.valueOf(sum.toCharArray()[j]));
                numbers += sum.toCharArray()[j];
            } catch (Exception ex) {
            }
        }
        StringBuilder sb = new StringBuilder(numbers);
        sb.insert(numbers.length() - 2, ".");
        return Float.parseFloat(sb.toString());
    }



    @After
    public void end() {
        aspire = null;
    }
}
