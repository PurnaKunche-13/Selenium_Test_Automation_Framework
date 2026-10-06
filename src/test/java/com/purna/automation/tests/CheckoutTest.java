package com.purna.automation.tests;

import com.purna.automation.base.BaseTest;
import com.purna.automation.pages.CheckoutPage;
import com.purna.automation.utilities.TestDataUtils;
import java.util.List;
import org.testng.Assert;
import org.testng.annotations.Test;

@Test(groups = {"ui", "regression", "checkout"})
public class CheckoutTest extends BaseTest {
    private CheckoutPage startCheckout() {
        return loginAsStandardUser().addToCart(TestDataUtils.products().get(0).name()).header().openCart().checkout();
    }
    private CheckoutPage overview() {
        var customer = TestDataUtils.customer();
        return startCheckout().enterInformation(customer.firstName(), customer.lastName(), customer.postalCode())
                .submitInformation().waitForOverview();
    }
    @Test(description = "TC020: Valid customer details open checkout overview")
    public void customerInformation() {
        var checkout = overview();
        var item = TestDataUtils.products().get(0);
        Assert.assertEquals(checkout.productNames(), List.of(item.name()));
        Assert.assertEquals(checkout.subtotal(), item.price());
    }
    @Test(dataProvider = "checkoutErrors", dataProviderClass = TestDataUtils.class,
          description = "TC021-TC023: Required customer fields")
    public void requiredFields(TestDataUtils.CheckoutCase row) {
        var checkout = startCheckout().enterInformation(row.firstName(), row.lastName(), row.postalCode()).submitInformation();
        Assert.assertEquals(checkout.errorMessage(), row.error(), row.id());
    }
    @Test(description = "TC024: Total equals subtotal plus displayed tax")
    public void checkoutTotal() {
        var checkout = overview();
        Assert.assertTrue(checkout.tax().signum() >= 0);
        Assert.assertEquals(checkout.total(), checkout.subtotal().add(checkout.tax()));
    }
    @Test(groups = "smoke", description = "TC025: Complete checkout and verify success message")
    public void completeCheckout() {
        var checkout = overview().finish();
        Assert.assertEquals(checkout.confirmationMessage(), "Thank you for your order!");
        Assert.assertEquals(checkout.header().waitForCartCount(0), 0);
    }
    @Test(dataProvider = "customers", dataProviderClass = TestDataUtils.class,
          description = "TC040-TC041: Synthetic customers complete checkout")
    public void customerCheckout(TestDataUtils.CustomerCase customer) {
        var checkout = startCheckout().enterInformation(customer.firstName(), customer.lastName(), customer.postalCode())
                .submitInformation().waitForOverview();
        Assert.assertEquals(checkout.subtotal(), TestDataUtils.products().get(0).price(), customer.id());
        Assert.assertEquals(checkout.finish().confirmationMessage(), "Thank you for your order!", customer.id());
        Assert.assertEquals(checkout.header().waitForCartCount(0), 0);
    }
}
