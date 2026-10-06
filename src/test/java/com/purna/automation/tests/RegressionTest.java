package com.purna.automation.tests;

import com.purna.automation.base.BaseTest;
import com.purna.automation.utilities.TestDataUtils;
import java.math.BigDecimal;
import org.testng.Assert;
import org.testng.annotations.Test;

public class RegressionTest extends BaseTest {
    @Test(groups = {"ui", "regression", "checkout"},
          description = "TC026: Independent multi-product purchase journey")
    public void multiProductPurchase() {
        var items = TestDataUtils.products().subList(0, 2);
        var products = loginAsStandardUser();
        products.sort("lohi");
        items.forEach(item -> products.addToCart(item.name()));
        Assert.assertEquals(products.header().waitForCartCount(2), 2);
        var cart = products.header().openCart();
        var expectedNames = items.stream().map(TestDataUtils.Product::name).toList();
        Assert.assertEquals(cart.productNames(), expectedNames);
        var customer = TestDataUtils.customer();
        var checkout = cart.checkout().enterInformation(customer.firstName(), customer.lastName(), customer.postalCode())
                .submitInformation().waitForOverview();
        Assert.assertEquals(checkout.productNames(), expectedNames);
        BigDecimal expectedSubtotal = items.stream().map(TestDataUtils.Product::price).reduce(BigDecimal.ZERO, BigDecimal::add);
        Assert.assertEquals(checkout.subtotal(), expectedSubtotal);
        Assert.assertEquals(checkout.total(), checkout.subtotal().add(checkout.tax()));
        Assert.assertEquals(checkout.finish().confirmationMessage(), "Thank you for your order!");
        Assert.assertEquals(checkout.header().waitForCartCount(0), 0);
        Assert.assertTrue(checkout.header().logout().isDisplayed());
    }
}
