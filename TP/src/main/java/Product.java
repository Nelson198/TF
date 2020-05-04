/**
 * Product
 */
public class Product {
    private String id;
    private String name;
    private String description;
    private double price;

    /**
     * Parameterized constructor
     * @param id Product's identifier
     * @param name Product's name
     * @param description Product's description
     * @param price Product's price
     */
    public Product(String id, String name, String description, double price) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
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
    public double getPrice() {
        return this.price;
    }

    /**
     * Set the product's price
     * @param newPrice Product's price
     */
    public void setPrice(double newPrice) {
        this.price = newPrice;
    }
}