package Client;

import Helpers.Product;
import Middleware.ClientConnection;

import io.atomix.utils.net.Address;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class PopulateCatalog {
    public static void populate(CatalogStub catalog) throws IOException {
        File file = new File("Products.txt");
        if (!file.exists()) {
            System.err.println("[ERROR] File \"" + file.getName() + "\" doesn't exist.");
            System.exit(1);
        }

        BufferedReader br = new BufferedReader(new FileReader(file.getName()));
        String s = br.readLine();
        while (s != null) {
            String[] data = s.split("\t");
            Product newProduct = new Product(-1, data[0], data[1], Float.parseFloat(data[2]), Integer.parseInt(data[3]));
            catalog.addProduct(newProduct);
            s = br.readLine();
        }
        br.close();

        System.out.println("[INFO] Supermarket's catalog has been successfully updated.");
        System.exit(0);
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException, IOException {
        if (args.length < 2) {
            System.out.println("Please indicate your port and the ports of at least one server.");
            System.exit(1);
        }

        Address myAddress = Address.from(Integer.parseInt(args[0]));

        List<Address> servers = new ArrayList<>();
        for (int i = 1; i < args.length; i++) {
            servers.add(Address.from(Integer.parseInt(args[i])));
        }

        ClientConnection connection = new ClientConnection(myAddress, servers);
        CatalogStub catalog = new CatalogStub(connection);

        populate(catalog);
    }
}
