/**
 * Cart Update
 */
public class CartUpdate {
    private final String idCart;
    private final String idProduct;
    private final int amount;

    /**
     * Parameterized constructor
     * @param idCart Cart's identifier
     * @param idProduct Product's identifier
     * @param amount Product's amount
     */
    public CartUpdate(String idCart, String idProduct, int amount) {
        this.idCart = idCart;
        this.idProduct = idProduct;
        this.amount = amount;
    }

    /**
     * Get the cart's identifier
     * @return Cart's identifier
     */
    public String getIdCart() {
        return this.idCart;
    }

    /**
     * Get the product's identifier
     * @return Product's identifier
     */
    public String getIdProduct() {
        return this.idProduct;
    }

    /**
     * Get the product's amount
     * @return Product's amount
     */
    public int getAmount() {
        return this.amount;
    }
}
