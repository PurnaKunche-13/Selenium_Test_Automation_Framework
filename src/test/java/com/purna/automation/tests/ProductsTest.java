package com.purna.automation.tests;

import com.purna.automation.base.BaseTest;
import com.purna.automation.utilities.TestDataUtils;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;
import org.testng.Assert;
import org.testng.annotations.Test;

@Test(groups = {"ui", "regression"})
public class ProductsTest extends BaseTest {
    @Test(description = "TC008: Products page has the expected number of products")
    public void productsPage() {
        var products = loginAsStandardUser();
        Assert.assertEquals(products.title(), "Products");
        Assert.assertEquals(products.productNames().size(), TestDataUtils.products().size());
    }
    @Test(description = "TC009: Verify all product names")
    public void productNames() {
        var expected = TestDataUtils.products().stream().map(TestDataUtils.Product::name).sorted().toList();
        Assert.assertEquals(loginAsStandardUser().productNames().stream().sorted().toList(), expected);
    }
    @Test(description = "TC010: Each product has its expected price")
    public void productPrices() {
        Map<String, BigDecimal> expected = TestDataUtils.products().stream()
                .collect(Collectors.toMap(TestDataUtils.Product::name, TestDataUtils.Product::price));
        Assert.assertEquals(loginAsStandardUser().catalog(), expected);
    }
    @Test(dataProvider = "sortModes", dataProviderClass = TestDataUtils.class,
          description = "TC011-TC014: Verify four sort directions")
    public void productSorting(String id, String mode, String field, String direction) {
        var products = loginAsStandardUser();
        boolean descending = direction.equals("descending");
        if (field.equals("name")) {
            var expected = new ArrayList<>(products.productNames());
            expected.sort(descending ? Comparator.reverseOrder() : Comparator.naturalOrder());
            products.sort(mode);
            Assert.assertEquals(products.waitForNames(expected), expected, id);
        } else {
            var expected = new ArrayList<>(products.productPrices());
            expected.sort(descending ? Comparator.reverseOrder() : Comparator.naturalOrder());
            products.sort(mode);
            Assert.assertEquals(products.waitForPrices(expected), expected, id);
        }
    }
    @Test(groups = "smoke", description = "TC015: Add product and verify cart badge")
    public void addProduct() {
        var products = loginAsStandardUser();
        Assert.assertEquals(products.header().cartCount(), 0);
        products.addToCart(TestDataUtils.products().get(0).name());
        Assert.assertEquals(products.header().waitForCartCount(1), 1);
    }
}
