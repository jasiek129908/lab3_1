package pl.com.bottega.ecommerce.sales.domain.invoicing;

import static java.util.Objects.nonNull;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.com.bottega.ecommerce.canonicalmodel.publishedlanguage.ClientData;
import pl.com.bottega.ecommerce.canonicalmodel.publishedlanguage.Id;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.Product;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.ProductData;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.ProductType;
import pl.com.bottega.ecommerce.sharedkernel.Money;

import java.util.Date;

@ExtendWith(MockitoExtension.class)
class BookKeeperTest {

    private static final String SAMPLE_CLIENT_NAME = "Kowalski";
    @Mock
    private InvoiceFactory factory;
    @Mock
    private TaxPolicy taxPolicy;
    private BookKeeper keeper;
    private Product sampleProduct;

    @BeforeEach
    void setUp() throws Exception {
        keeper = new BookKeeper(factory);
        sampleProduct = new Product(new Id("1"), new Money(10, Money.DEFAULT_CURRENCY), "miesko", ProductType.FOOD);
    }

    @Test
    void test() {
        Id sampleId = Id.generate();
        ClientData dummy = new ClientData(sampleId, SAMPLE_CLIENT_NAME);
        InvoiceRequest request = new InvoiceRequest(dummy);
        Invoice invoice = new Invoice(Id.generate(), dummy);
        when(factory.create(dummy)).thenReturn(invoice);

        keeper.issuance(request, taxPolicy);

        verifyNoInteractions(taxPolicy);
    }

    @Test
    void testCase1() {
        Id sampleId = Id.generate();
        ClientData dummy = new ClientData(sampleId, SAMPLE_CLIENT_NAME);
        InvoiceRequest request = new InvoiceRequest(dummy);
        request.add(new RequestItem(sampleProduct.generateSnapshot(), 1, new Money(10, Money.DEFAULT_CURRENCY)));
        Invoice invoice = new Invoice(Id.generate(), dummy);

        when(factory.create(dummy)).thenReturn(invoice);
        when(taxPolicy.calculateTax(any(ProductType.class),any(Money.class))).thenReturn(new Tax(new Money(any(Integer.class)),"tax"));

        Invoice resInvoice = keeper.issuance(request, taxPolicy);
        assertTrue(nonNull(resInvoice));
        assertEquals(invoice,resInvoice);
        assertEquals(invoice.getItems().size(),1);

    }
}
