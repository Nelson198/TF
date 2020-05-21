package Helpers;

/**
 * Product
 */
public class Product {
    private final int id;
    private String name;
    private String description;
    private float price;
    private final int amount;

    /**
     * Parameterized constructor
     * @param id Product's identifier
     * @param name Product's name
     * @param description Product's description
     * @param price Product's price
     * @param amount Product's amount
     */
    public Product(int id, String name, String description, float price, int amount) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.amount = amount;
    }

    /**
     * Get product's identifier
     * @return Product's identifier
     */
    public int getId() {
        return this.id;
    }

    /**
     * Get product's name
     * @return Product's name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Get product's description
     * @return Product's description
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Get product's price
     * @return Product's price
     */
    public float getPrice() {
        return this.price;
    }

    /**
     * Get product's amount
     * @return Product's amount
     */
    public int getAmount() {
        return this.amount;
    }

    /**
     * Set product's name
     * @param name Product's name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Set product's description
     * @param description Product's description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Set product's price
     * @param price Product's price
     */
    public void setPrice(float price) {
        this.price = price;
    }
}
