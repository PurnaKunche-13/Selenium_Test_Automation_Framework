package com.purna.automation.tests;

import com.purna.automation.base.BaseTest;
import com.purna.automation.utilities.TestDataUtils;
import java.util.List;
import org.testng.Assert;
import org.testng.annotations.Test;

@Test(groups = {"ui", "regression"})
public class CartTest extends BaseTest {
    @Test(description = "TC016: Cart contains selected products, prices and quantities")
    public void cartContents() {
        var items = TestDataUtils.products().subList(0, 2);
        var products = loginAsStandardUser();
        items.forEach(item -> products.addToCart(item.name()));
        var cart = products.header().openCart();
        Assert.assertEquals(cart.productNames(), items.stream().map(TestDataUtils.Product::name).toList());
        for (var item : items) {
            Assert.assertEquals(cart.priceOf(item.name()), item.price());
            Assert.assertEquals(cart.quantityOf(item.name()), 1);
        }
        Assert.assertEquals(cart.header().waitForCartCount(2), 2);
    }
    @Test(description = "TC017: Remove one of two items")
    public void removeOneProduct() {
        var items = TestDataUtils.products().subList(0, 2);
        var products = loginAsStandardUser();
        items.forEach(item -> products.addToCart(item.name()));
        var cart = products.header().openCart().remove(items.get(0).name());
        Assert.assertEquals(cart.productNames(), List.of(items.get(1).name()));
        Assert.assertEquals(cart.header().waitForCartCount(1), 1);
    }
    @Test(description = "TC018: Removing the last item clears the badge")
    public void emptyCart() {
        String name = TestDataUtils.products().get(0).name();
        var cart = loginAsStandardUser().addToCart(name).header().openCart().remove(name);
        Assert.assertTrue(cart.productNames().isEmpty());
        Assert.assertEquals(cart.header().waitForCartCount(0), 0);
    }
    @Test(description = "TC019: Continue shopping preserves selected items")
    public void continueShopping() {
        String name = TestDataUtils.products().get(0).name();
        var products = loginAsStandardUser().addToCart(name).header().openCart().continueShopping();
        Assert.assertEquals(products.title(), "Products");
        Assert.assertEquals(products.header().waitForCartCount(1), 1);
        Assert.assertEquals(products.header().openCart().productNames(), List.of(name));
    }
    @Test(dataProvider = "products", dataProviderClass = TestDataUtils.class,
          description = "TC034-TC039: Each catalog product can be added with its expected price")
    public void catalogProductInCart(TestDataUtils.Product item) {
        var cart = loginAsStandardUser().addToCart(item.name()).header().openCart();
        Assert.assertEquals(cart.productNames(), List.of(item.name()), item.id());
        Assert.assertEquals(cart.priceOf(item.name()), item.price(), item.id());
        Assert.assertEquals(cart.header().waitForCartCount(1), 1);
    }
}
