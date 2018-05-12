package finance.doctype;

import finance.doctype.item.ProductItem;
import org.apache.log4j.Logger;

import java.text.ParseException;

public class MarketGrozdDT extends AbstractDocumentType{
    private final static Logger LOGGER = Logger.getLogger(MarketGrozdDT.class);
    private final static String NAME = "Магазин ГРОЗДЬ";

    @Override
    public void calculateDocument(String reportName) {
        try {
            for (ProductItem product : productItems) {
                fullData.add(prepareStructure(product,NAME));
            }
            writeToExcel(reportName);
        } catch (ParseException e) {
            LOGGER.error("Exception preparing report structure.", e);
        }
    }

    @Override
    public String toString() {
        return "GROZD MARKET CHECK DOCUMENT TYPE";
    }
}
