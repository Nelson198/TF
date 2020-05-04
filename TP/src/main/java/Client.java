import io.atomix.utils.net.Address;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Client
 */
public class Client {
    private static final BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
    private final HashMap<String, CartStub> carts = new HashMap<String, CartStub>();

    ManagedMessagingService ms = new NettyMessagingService("supermarket", Address.from(Integer.parseInt(args[0])), new MessagingConfig());


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
     * @throws IOException
     */
    private static int readInt() throws IOException {
        while (true) {
            try {
                return Integer.parseInt(stdin.readLine());
            } catch (NumberFormatException exc) {
                System.out.println("Por favor, introduza um número");
            }
        }
    }

    /**
     * menu
     * @throws IOException
     */
    private static void menu() throws IOException {
        while (true) {
            StringBuilder main = new StringBuilder();
            main.append("Welcome to our supermarket. Choose an option?\n")
                .append("1 - Create a cart\n")
                .append("2 - Check a product's price\n")
                .append("3 - Check a product's availability\n")
                .append("4 - Checkout\n")
                .append("5 - Disconnect\n");

            clearTerminal();
            System.out.println(main.toString());

            int choice;
            do {
                choice = readInt();
            } while (choice < 1 || choice > 7);

            switch (choice) {
                case 1:
                    System.out.println("Choose a name for your cart:\n");
                    String name = stdin.readLine();
                    // rever e acabar
                    CartStub cart = new CartStub(conn);
                    carts.put(name, cart);
                    break;

                case 2:
                    System.out.println(2);
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

    public static void main(String[] args) throws IOException {
        menu();
    }
}
