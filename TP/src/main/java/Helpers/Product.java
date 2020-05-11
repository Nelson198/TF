package Helpers;

/**
 * Product
 */
public class Product {
    private final String id;
    private final String name;
    private final String description;
    private final float price;
    private final int quantity;

    /**
     * Parameterized constructor
     * @param id Product's identifier
     * @param name Product's name
     * @param description Product's description
     * @param price Product's price
     * @param quantity Product's quantity
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
     * @param p Product
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
     * @return Product's identifier
     */
    public String getId() {
        return this.id;
    }

    /**
     * Get the product's name
     * @return Product's name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Get the product's description
     * @return Product's description
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Get the product's price
     * @return Product's price
     */
    public float getPrice() {
        return this.price;
    }

    /**
     * Get the product's quantity
     * @return Product's quantity
     */
    public int getQuantity() {
        return this.quantity;
    }
}