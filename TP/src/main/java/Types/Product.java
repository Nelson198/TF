package Types;

/**
 * Types.Product
 */
public class Product {
    private final String id;
    private final String name;
    private final String description;
    private final float price;
    private final int quantity;

    /**
     * Parameterized constructor
     * @param id Types.Product's identifier
     * @param name Types.Product's name
     * @param description Types.Product's description
     * @param price Types.Product's price
     * @param quantity Types.Product's quantity
     */
    public Product(String id, String name, String description, float price, int quantity) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.quantity = quantity;
    }

    /**
     * Parameterized constructor
     * @param p Types.Product
     */
    public Product(Product p) {
        this.id = p.getId();
        this.name = p.getName();
        this.description = p.getDescription();
        this.price = p.getPrice();
        this.quantity = p.getQuantity();
    }

    /**
     * Get the product's identifier
     * @return Types.Product's identifier
     */
    public String getId() {
        return this.id;
    }

    /**
     * Get the product's name
     * @return Types.Product's name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Get the product's description
     * @return Types.Product's description
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Get the product's price
     * @return Types.Product's price
     */
    public float getPrice() {
        return this.price;
    }

    /**
     * Get the product's quantity
     * @return Types.Product's quantity
     */
    public int getQuantity() {
        return quantity;
    }
}