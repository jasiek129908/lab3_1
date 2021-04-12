package pl.com.bottega.ecommerce.sales.domain.invoicing;

import static java.util.Objects.nonNull;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.com.bottega.ddd.support.domain.BaseAggregateRoot;
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
    ProductBuilder productBuilder;
    //private Product sampleProduct;

    @BeforeEach
    void setUp() throws Exception {
        keeper = new BookKeeper(factory);
        productBuilder = new ProductBuilder();
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
        Product product = productBuilder.withPrice(new Money(10, Money.DEFAULT_CURRENCY))
                .withName("miesko")
                .withAggregateId(Id.generate())
                .withProductType(ProductType.FOOD)
                .build();
        request.add(new RequestItem(product.generateSnapshot(), 1, new Money(10, Money.DEFAULT_CURRENCY)));
        Invoice invoice = new Invoice(Id.generate(), dummy);

        when(factory.create(dummy)).thenReturn(invoice);
        when(taxPolicy.calculateTax(any(ProductType.class), any(Money.class))).thenReturn(new Tax(new Money(any(Integer.class)), "tax"));

        Invoice resInvoice = keeper.issuance(request, taxPolicy);
        assertTrue(nonNull(resInvoice));
        assertEquals(invoice, resInvoice);
        assertEquals(1, invoice.getItems().size());

    }

    @Test
    void testCase2() {
        Id sampleId = Id.generate();
        ClientData dummy = new ClientData(sampleId, SAMPLE_CLIENT_NAME);
        InvoiceRequest request = new InvoiceRequest(dummy);
        Product product = productBuilder.withPrice(new Money(10, Money.DEFAULT_CURRENCY))
                .withName("miesko")
                .withAggregateId(Id.generate())
                .withProductType(ProductType.FOOD)
                .build();
        Product product2 = productBuilder.withPrice(new Money(10, Money.DEFAULT_CURRENCY))
                .withName("warzywka")
                .withAggregateId(Id.generate())
                .withProductType(ProductType.FOOD)
                .build();
        request.add(new RequestItem(product.generateSnapshot(), 1, new Money(10, Money.DEFAULT_CURRENCY)));
        request.add(new RequestItem(product2.generateSnapshot(), 2, new Money(11, Money.DEFAULT_CURRENCY)));
        Invoice invoice = new Invoice(Id.generate(), dummy);

        when(factory.create(dummy)).thenReturn(invoice);
        when(taxPolicy.calculateTax(any(ProductType.class), any(Money.class))).thenReturn(new Tax(new Money(any(Integer.class)), "tax"));

        Invoice resInvoice = keeper.issuance(request, taxPolicy);
        verify(taxPolicy, times(2)).calculateTax(any(ProductType.class), any(Money.class));

    }

    private class ProductBuilder {
        private Id aggregateId;
        private Money price;
        private String name;
        private ProductType productType;

        public ProductBuilder withAggregateId(Id aggregateId) {
            this.aggregateId = aggregateId;
            return this;
        }

        public ProductBuilder withPrice(Money price) {
            this.price = price;
            return this;
        }

        public ProductBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public ProductBuilder withProductType(ProductType productType) {
            this.productType = productType;
            return this;
        }

        public Product build() {
            return new Product(aggregateId, price, name, productType);
        }
    }

}
