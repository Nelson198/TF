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
     * readInt
     * @return User's option
     * @throws IOException IOException
     */
    private static int readInt() throws IOException {
        while (true) {
            try {
                return Integer.parseInt(stdin.readLine());
            } catch (NumberFormatException exc) {
                System.out.println("Please enter a number.");
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
            String cart = "Welcome to your cart. Please choose an option:\n" +
                          "1 - Get products\n" +
                          "2 - Add product\n" +
                          "3 - Remove product\n" +
                          "4 - Checkout\n" +
                          "5 - Get back\n";
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
                        sb.append("--> Product nº ").append(p.getId()).append(":\n")
                          .append("\t--> Name: ").append(p.getName()).append("\n")
                          .append("\t--> Description: ").append(p.getDescription()).append("\n")
                          .append("\t--> Price: ").append(p.getPrice()).append(" € (per unit)\n")
                          .append("\t--> Amount: ").append(p.getAmount()).append(" unit(s)\n");
                    }
                    System.out.println(sb.toString());
                    waitConfirmation();
                    break;

                case 2:
                    System.out.println("Add product(s):");
                    System.out.print("\t--> Product's identifier: ");
                    String idProduct = stdin.readLine();
                    System.out.print("\t--> Product's amount: ");
                    int amount = readInt();
                    cs.updateProduct(idProduct, amount);
                    waitConfirmation();
                    break;

                case 3:
                    System.out.println("Remove product(s):");
                    System.out.print("\t--> Product's identifier: ");
                    idProduct = stdin.readLine();
                    System.out.print("\t--> Product's amount: ");
                    int quantity = readInt();
                    cs.updateProduct(idProduct, -quantity);
                    waitConfirmation();
                    break;

                case 4:
                    cs.checkout();
                    waitConfirmation();
                    break;

                case 5:
                    menu();
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
            String main = "Welcome to our supermarket. Please choose an option:\n" +
                          "\t1 - Create a cart\n" +
                          "\t2 - Check/Update a cart\n" +
                          "\t3 - See catalog\n" +
                          "\t4 - Check a product's price\n" +
                          "\t5 - Check a product's availability\n" +
                          "\t6 - Exit\n";
            System.out.println(main);

            int choice;
            do {
                choice = readInt();
            } while (choice < 1 || choice > 6);

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
                    System.out.println("Choose a cart to check/update: ");
                    for (String c : carts.keySet()) {
                        System.out.println("\t" + c);
                    }
                    String cartName = stdin.readLine();
                    menuCart(cartName);
                    break;

                case 3:
                    String info = catalog.getCatalog();
                    System.out.println(info);
                    waitConfirmation();
                    break;

                case 4:
                    System.out.print("Insert the product's identifier: ");
                    String productId = stdin.readLine();
                    float price = catalog.getPrice(productId);
                    System.out.println("This product will cost you " + price + "€ per unit.");
                    waitConfirmation();
                    break;

                case 5:
                    System.out.print("Insert the product's identifier: ");
                    productId = stdin.readLine();
                    int amount = catalog.getAmount(productId);
                    System.out.println("We have " + amount + " units available at the moment");
                    waitConfirmation();
                    break;

                case 6:
                    System.exit(1);
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
