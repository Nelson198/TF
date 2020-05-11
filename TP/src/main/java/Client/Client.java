package Client;

import Helpers.Product;
import Middleware.ClientConnection;

import io.atomix.utils.net.Address;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

/**
 * Client
 */
public class Client {
    private static final BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));

    private static final HashMap<String, CartStub> carts = new HashMap<>();
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
            StringBuilder cart = new StringBuilder();
            cart.append("What do you want to do?\n")
                .append("1 - Get products\n")
                .append("2 - Add product\n")
                .append("3 - Remove product\n")
                .append("4 - Checkout\n")
                .append("5 - Get back\n");

            clearTerminal();
            System.out.println(cart.toString());

            int choice;
            do {
                choice = readInt();
            } while (choice < 1 || choice > 5);

            switch (choice) {
                case 1:
                    ArrayList<Product> productList = cs.getProducts();

                    StringBuilder prods = new StringBuilder();
                    prods.append("This cart has:\n");
                    for (Product p : productList) {
                        prods.append("\t").append(p.getDescription()).append(" ").append(p.getQuantity());
                    }

                    clearTerminal();
                    System.out.println(prods.toString());

                    break;

                case 2:
                    System.out.print("Please insert the product's code: ");
                    String idProduct = stdin.readLine();
                    System.out.print("Specify a quantity to remove (press a to remove all): ");
                    int amount = readInt();
                    cs.addProduct(idProduct, amount);
                    break;

                case 3:
                    System.out.print("Please insert the product's code: ");
                    idProduct = stdin.readLine();
                    System.out.print("Specify a quantity to remove (press a to remove all): ");
                    String qtd = stdin.readLine();
                    if (qtd.equals("a"))
                        cs.removeProduct(idProduct, Integer.MAX_VALUE);
                    else
                        cs.removeProduct(idProduct, Integer.parseInt(qtd));
                    break;

                case 4:
                    cs.checkout();
                    break;

                case 5:
                    return;
            }
        }
    }

    /**
     * Main menu
     * @throws IOException IOException
     * @throws ExecutionException ExecutionException
     * @throws InterruptedException InterruptedException
     */
    private static void menu() throws IOException, ExecutionException, InterruptedException {
        while (true) {
            StringBuilder main = new StringBuilder();
            main.append("Welcome to our supermarket. Choose an option:\n")
                .append("\t1 - Create a cart\n")
                .append("\t2 - Check/Update a cart\n")
                .append("\t3 - Check a product's price\n")
                .append("\t4 - Check a product's availability\n")
                .append("\t5 - Disconnect\n");

            clearTerminal();
            System.out.println(main.toString());

            int choice;
            do {
                choice = readInt();
            } while (choice < 1 || choice > 5);

            switch (choice) {
                case 1:
                    clearTerminal();
                    System.out.println(3);
                    System.out.print("Choose a name for your cart: ");
                    String name = stdin.readLine();
                    CartStub cart = new CartStub(connection);
                    carts.put(name, cart);
                    menuCart(name); // TODO - allow to use more than one cart
                    break;

                case 2:
                    clearTerminal();
                    System.out.println("Choose a cart to check/update: ");
                    for (String c : carts.keySet()) {
                        System.out.println("\t" + c);
                    }
                    String cartName = stdin.readLine();
                    menuCart(cartName);
                    break;

                case 3:
                    clearTerminal();
                    System.out.print("Insert the product's code: ");
                    String productId = stdin.readLine();
                    float price = catalog.getPrice(productId);
                    System.out.println("This product will cost you €" + price);
                    break;

                case 4:
                    clearTerminal();
                    System.out.print("Insert the product's code: ");
                    productId = stdin.readLine();
                    int amount = catalog.getAvailability(productId);
                    System.out.println("We have " + amount + " available at the moment");
                    break;

                case 5:
                    clearTerminal();
                    System.exit(1);
                    break;
            }
        }
    }

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        if (args.length < 2) {
            System.out.println("The port of the client and the ports of at least one server were not specified");
            System.exit(1);
        }

        Address myAddress = Address.from(Integer.parseInt(args[0]));

        ArrayList<Address> servers = new ArrayList<>();
        for (int i = 1; i < args.length; i++) {
            servers.add(Address.from(Integer.parseInt(args[i])));
        }

        connection = new ClientConnection(myAddress, servers);

        catalog = new CatalogStub(connection);

        menu();
    }
}
