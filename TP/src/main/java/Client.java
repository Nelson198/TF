import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Client
 */
public class Client {
    private static final BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));

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
            main.append("Bem-vindo ao nosso supermercado. O que pretende realizar?\n")
                .append("1 - Iniciar uma compra\n")
                .append("2 - Consultar o preço de um produto\n")
                .append("3 - Consultar a disponibilidade de um produto\n")
                .append("4 - Confirmar uma encomenda\n")
                .append("5 - Sair\n");

            clearTerminal();
            System.out.println(main.toString());

            int choice;
            do {
                choice = readInt();
            } while (choice < 1 || choice > 5);

            switch (choice) {
                case 1:
                    System.out.println(1);
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
