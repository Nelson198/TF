import io.atomix.cluster.messaging.ManagedMessagingService;
import io.atomix.cluster.messaging.MessagingConfig;
import io.atomix.cluster.messaging.impl.NettyMessagingService;
import io.atomix.utils.net.Address;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;

/**
 * Client
 */
public class Client {
    private static final BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));

    private static final HashMap<String, CartStub> carts = new HashMap<>();
    private static CatalogStub catalog;

    private static ManagedMessagingService ms;
    private static final List<Address> servers = new ArrayList<>();
    private static Address serverAddress;

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
                .append("1 - Add product\n")
                .append("2 - Remove product\n")
                .append("3 - Checkout\n")
                .append("4 - Get back\n");

            clearTerminal();
            System.out.println(cart.toString());

            int choice;
            do {
                choice = readInt();
            } while (choice < 1 || choice > 4);

            switch (choice) {
                case 1:
                    // por concluir
                    // cs.addProduct(idProduct, qtd);
                    break;

                case 2:
                    // por concluir
                    // cs.removeProduct(idProduct);
                    break;

                case 3:
                    cs.checkout();
                    break;

                case 4:
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
            main.append("Welcome to our supermarket. Choose an option?\n")
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
                    System.out.println("Choose a name for your cart: ");
                    String name = stdin.readLine();
                    // rever e acabar
                    CartStub cart = new CartStub(ms, serverAddress);
                    carts.put(name, cart);
                    menuCart(name);
                    break;

                case 2:
                    System.out.println("Choose a cart to check/update:\n");
                    for (String c : carts.keySet()) {
                        System.out.println("\t" + c);
                    }
                    String cartName = stdin.readLine();
                    menuCart(cartName);
                    break;

                case 3:
                    System.out.println(3);
                    break;

                case 4:
                    System.out.println(4);
                    break;

                case 5:
                    clearTerminal();
                    System.exit(1);
                    break;
            }
        }
    }

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        Address myAddress = Address.from(Integer.parseInt(args[0]));

        for (int i = 1; i < args.length; i++) {
            servers.add(Address.from(Integer.parseInt(args[0])));
        }
        Random rand = new Random();
        int server = rand.nextInt(args.length - 1) + 1;
        serverAddress = servers.get(server);

        ms = new NettyMessagingService("client", myAddress, new MessagingConfig());

        catalog = new CatalogStub(ms, myAddress);

        menu();
    }
}
