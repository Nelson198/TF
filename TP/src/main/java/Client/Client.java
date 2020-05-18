package Client;

import Helpers.Product;
import Middleware.ClientConnection;

import io.atomix.utils.net.Address;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Client
 */
public class Client {
    private static final BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));

    private static final Map<String, CartStub> carts = new HashMap<>();
    private static CatalogStub catalog;

    private static ClientConnection connection;

    /**
     * clearTerminal
     */
    private static void clearTerminal() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    /**
     * Wait confirmation by user
     * @throws IOException IOException
     */
    private static void waitConfirmation() throws IOException {
        System.out.print("\nPress ENTER to continue ...");
        System.out.flush();
        stdin.readLine();
    }

    /**
     * Read an integer value
     * @return User's option
     * @throws IOException IOException
     */
    private static int readInt() throws IOException {
        while (true) {
            try {
                System.out.print("> ");
                return Integer.parseInt(stdin.readLine());
            } catch (NumberFormatException exc) {
                System.out.println("Invalid number! Please enter a correct number.");
            }
        }
    }

    /**
     * Read a string
     * @return User's option
     */
    private static String readString() {
        while (true) {
            try {
                System.out.print("> ");
                String s = stdin.readLine();
                if (s.isEmpty()) {
                    continue;
                }
                return s;
            } catch (Exception e) {
                System.out.println("Invalid word(s)! Please enter a valid text.");
            }
        }
    }

    /**
     * Cart's menu
     * @param cartName cartName
     * @throws IOException IOException
     */
    private static void menuCart(String cartName) throws IOException {
        CartStub cs = carts.get(cartName);
        while(true) {
            clearTerminal();
            StringBuilder cart = new StringBuilder();
            cart.append("Welcome to your cart. Please choose an option:\n\n")
                .append("1 - Get products\n")
                .append("2 - Add product\n")
                .append("3 - Remove product\n")
                .append("4 - Checkout\n")
                .append("5 - Get back\n");

            System.out.println(cart);

            int choice;
            do {
                choice = readInt();
            } while (choice < 1 || choice > 5);

            clearTerminal();
            switch (choice) {
                case 1:
                    List<Product> products = cs.getProducts();

                    StringBuilder sb = new StringBuilder();
                    sb.append("This cart has the following products:\n\n");
                    for (Product p : products) {
                        sb.append("--> Product nº").append(p.getId()).append(":\n")
                          .append("\t--> Name: ").append(p.getName()).append("\n")
                          .append("\t--> Description: ").append(p.getDescription()).append("\n")
                          .append("\t--> Price: ").append(p.getPrice()).append(" € (per unit)\n")
                          .append("\t--> Amount: ").append(p.getAmount()).append(" unit(s)\n");
                    }
                    System.out.println(sb);
                    waitConfirmation();
                    break;

                case 2:
                    System.out.println("Add product(s):");
                    System.out.print("\t--> Product's identifier: ");
                    int idProduct = Integer.parseInt(stdin.readLine());
                    System.out.print("\t--> Product's amount: ");
                    int amount = readInt();
                    cs.updateProduct(idProduct, amount);
                    waitConfirmation();
                    break;

                case 3:
                    System.out.println("Remove product(s):");
                    System.out.print("\t--> Product's identifier: ");
                    int idProd = Integer.parseInt(stdin.readLine());
                    System.out.print("\t--> Product's amount: ");
                    int quantity = readInt();
                    cs.updateProduct(idProd, -quantity);
                    waitConfirmation();
                    break;

                case 4:
                    cs.checkout();
                    waitConfirmation();
                    break;

                case 5:
                    return;
            }
        }
    }

    /**
     * Auxiliar function to update a product
     * @param  p product
     * @throws IOException IOException
     */
    private static Product updateProduct(Product p) throws IOException {
        while(true) {
            StringBuilder change = new StringBuilder();
            change.append("Welcome to product's menu. Please choose an option:\n\n")
                  .append("1 - Change name\n")
                  .append("2 - Change description\n")
                  .append("3 - Change price\n")
                  .append("4 - Submit changes\n");

            clearTerminal();
            System.out.print(change);

            int update;
            do {
                update = readInt();
            } while (update < 1 || update > 4);

            switch(update) {
                case 1:
                    System.out.print("Insert a new name: ");
                    String productName = stdin.readLine();
                    p.setName(productName);
                    break;

                case 2:
                    System.out.print("Insert the new description: ");
                    String productDescription = stdin.readLine();
                    p.setDescription(productDescription);
                    break;

                case 3:
                    System.out.print("Insert the new price: ");
                    float productPrice = Float.parseFloat(stdin.readLine());
                    p.setPrice(productPrice);
                    break;

                case 4:
                    return p;
            }
        }
    }

    /**
     * Admin's menu
     * @throws IOException IOException
     */
    private static void menuAdmin() throws IOException {
        while (true) {
            StringBuilder admin = new StringBuilder();
            admin.append("Welcome to administrator's menu. Please choose an option:\n\n")
                 .append("1 - Add a new product to the catalog\n")
                 .append("2 - Update a product's amount in the catalog\n")
                 .append("3 - Other updates on a product in the catalog\n")
                 .append("4 - Remove a product from the catalog\n")
                 .append("5 - Go back\n");

            clearTerminal();
            System.out.println(admin);

            int choice;
            do {
                choice = readInt();
            } while (choice < 1 || choice > 5);

            clearTerminal();
            switch(choice) {
                case 1:
                    System.out.print("Insert the product's name: ");
                    String productName = stdin.readLine();

                    System.out.print("Insert a description to the product: ");
                    String productDescription = stdin.readLine();

                    System.out.print("Insert the product's price: ");
                    float productPrice = Float.parseFloat(stdin.readLine());

                    System.out.print("Insert the product's initial amount: ");
                    int productAmount = Integer.parseInt(stdin.readLine());

                    Product newProduct = new Product(-1, productName, productDescription, productPrice, productAmount);

                    catalog.addProduct(newProduct);
                    waitConfirmation();
                    break;

                case 2:
                    System.out.print("Insert the product's id: ");
                    int productId = Integer.parseInt(stdin.readLine());

                    System.out.print("Insert the product's name: ");
                    int amount = Integer.parseInt(stdin.readLine());

                    catalog.updateAmount(productId, amount);
                    waitConfirmation();
                    break;

                case 3:
                    System.out.print("Insert the product's id: ");
                    int prodId = Integer.parseInt(stdin.readLine());

                    Product toUpdate = catalog.getProduct(prodId);

                    catalog.updateProduct(updateProduct(toUpdate));
                    waitConfirmation();
                    break;

                case 4:
                    System.out.print("Insert the product's id: ");
                    int idProduct = Integer.parseInt(stdin.readLine());

                    catalog.removeProduct(idProduct);
                    waitConfirmation();
                    break;

                case 5:
                    return;
            }
        }
    }

    /**
     * Main menu
     * @throws IOException IOException
     */
    private static void menu() throws IOException {
        while (true) {
            clearTerminal();
            StringBuilder main = new StringBuilder();
            main.append("Welcome to our supermarket. Please choose an option:\n\n")
                .append("1 - Create a cart\n")
                .append("2 - Check/Update a cart\n")
                .append("3 - See catalog\n")
                .append("4 - Check a product's price\n")
                .append("5 - Check a product's availability\n")
                .append("6 - Login admin\n")
                .append("7 - Exit\n");

            System.out.println(main);

            int choice;
            do {
                choice = readInt();
            } while (choice < 1 || choice > 7);

            clearTerminal();
            switch (choice) {
                case 1:
                    System.out.print("Choose a name for your cart: ");
                    String name = stdin.readLine();
                    CartStub cart = new CartStub(connection);
                    carts.put(name, cart);
                    menuCart(name);
                    break;

                case 2:
                    if (carts.keySet().size() == 0) {
                        System.out.println("There is no cart created so far. Please create one.");
                        waitConfirmation();
                    } else {
                        System.out.println("Please choose a cart to check / update:\n");
                        for (String c : carts.keySet()) {
                            System.out.println("--> " + c);
                        }
                        System.out.println();
                        String cartName = readString();
                        menuCart(cartName);
                    }
                    break;

                case 3:
                    String info = catalog.getCatalog();
                    if (info.isEmpty()) {
                        System.out.println("There is no information in the catalog yet.");
                    } else {
                        System.out.println(info);
                    }
                    waitConfirmation();
                    break;

                case 4:
                    System.out.print("Insert the product's identifier: ");
                    int productId = readInt();
                    float price = catalog.getPrice(productId);
                    if (price == -1)
                        System.out.println("This product is not available.");
                    else
                        System.out.println("This product will cost you " + price + "€ per unit.");
                    waitConfirmation();
                    break;

                case 5:
                    System.out.print("Insert the product's identifier: ");
                    productId = readInt();
                    int amount = catalog.getAmount(productId);
                    if (amount == -1)
                        System.out.println("This product is not available.");
                    else
                        System.out.println("We have " + amount + " units available at the moment");
                    waitConfirmation();
                    break;

                case 6:
                    menuAdmin();
                    break;

                case 7:
                    System.exit(0);
                    break;
            }
        }
    }

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        if (args.length < 2) {
            System.out.println("Please indicate the port of the client and the ports of at least one server.");
            System.exit(1);
        }

        Address myAddress = Address.from(Integer.parseInt(args[0]));

        List<Address> servers = new ArrayList<>();
        for (int i = 1; i < args.length; i++) {
            servers.add(Address.from(Integer.parseInt(args[i])));
        }

        connection = new ClientConnection(myAddress, servers);
        catalog = new CatalogStub(connection);

        menu();
    }
}
