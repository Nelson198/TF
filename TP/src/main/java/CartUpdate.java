public class CartUpdate {
    private String idCart;
    private String idProduct;
    private int qtd;

    public CartUpdate(String idCart, String idProduct, int qtd) {
        this.idCart = idCart;
        this.idProduct = idProduct;
        this.qtd = qtd;
    }

    public String getIdCart() {
        return idCart;
    }

    public String getIdProduct() {
        return idProduct;
    }

    public int getQtd() {
        return qtd;
    }
}
