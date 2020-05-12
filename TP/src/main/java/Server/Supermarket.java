package Server;

import Helpers.Product;
import Helpers.Serializers;
import Helpers.CartUpdate;
import Messages.DBUpdate;
import Helpers.ProductGet;

import Middleware.ServerConnection;
import io.atomix.utils.net.Address;
import io.atomix.utils.serializer.Serializer;

import spread.SpreadException;

import java.net.UnknownHostException;
import java.sql.Connection;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * Supermarket Implementation
 */
public class Supermarket {
    // Connection to the cluster
    ServerConnection connection;

    // Serializer for the messages sent between servers
    Serializer serializer = Serializers.serverSerializer;

    // Skeleton for the catalog
    CatalogSkeleton catalog;

    // Skeletons for the carts
    HashMap<String, CartSkeleton> carts = new HashMap<>();

    /**
     * Parameterized constructor
     * @param port Port
     */
    public Supermarket(String port) {
        this.connection = new ServerConnection(port);
    }

    public void initialize() throws SpreadException, UnknownHostException {
        BiFunction<DBUpdate, Connection, Object> processDBUpdate = (dbUpdate, dbConnection) -> {
            switch (dbUpdate.getSecondaryType()) {
                case "newCart":
                    CartSkeleton cs = new CartSkeleton(dbConnection);
                    this.carts.put(cs.getIdCart(), cs);

                    Thread timer = new TimerThread(cs.getIdCart(), connection);
                    timer.start();

                    return cs.getIdCart();
                case "deleteCart":
                    String objectID = (String) dbUpdate.getUpdateInfo();
                    this.carts.get(objectID).delete();
                    this.carts.remove(objectID);

                    return null;
                case "checkout":
                    String objID = (String) dbUpdate.getUpdateInfo();
                    boolean res = this.carts.get(objID).checkout();
                    this.carts.remove(objID);

                    return res;
                case "updateCart":
                    CartUpdate cartUpdate = (CartUpdate) dbUpdate.getUpdateInfo();
                    CartSkeleton cart = this.carts.get(cartUpdate.getIdCart());
                    if (cartUpdate.getAmount() < 0)
                        cart.removeProduct(cartUpdate.getIdProduct(), cartUpdate.getAmount()); // TODO - join these two cases in CartSkeleton (updateProduct)
                    else
                        cart.addProduct(cartUpdate.getIdProduct(), cartUpdate.getAmount()); // TODO - join these two cases in CartSkeleton (updateProduct)

                    return null;
            }

            return null;
        };

        Consumer<Connection> afterDBStart = (dbConnection) -> {
            this.catalog = new CatalogSkeleton(dbConnection);
        };

        ArrayList<String> tablesToCreate = new ArrayList<>();
        tablesToCreate.add("CREATE TABLE cart (id INT AUTO_INCREMENT PRIMARY KEY)");
        tablesToCreate.add("CREATE TABLE product (id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(100), description VARCHAR(100), price FLOAT, amount INT)");
        tablesToCreate.add("CREATE TABLE cartProduct (id INT AUTO_INCREMENT PRIMARY KEY, idProduct FOREIGN KEY REFERENCES product(id), amount INT)");

        HashMap<String, BiFunction<Address, byte[], HandlerRes>> handlers = new HashMap<>();

        // Cart
        handlers.put("newCart", (address, bytes) -> {
            return new HandlerRes(null, true, false);
        });

        handlers.put("updateCart", (address, bytes) -> {
            CartUpdate cu = serializer.decode(bytes);

            return new HandlerRes(cu, true, false);
        });

        handlers.put("checkout", (address, bytes) -> {
            String cartID = serializer.decode(bytes);

            return new HandlerRes(cartID, true, false);
        });

        handlers.put("getProducts", (address, bytes) -> {
            String idCart = serializer.decode(bytes);
            List<Product> res = new ArrayList<>(carts.get(idCart).getProducts());

            return new HandlerRes(serializer.encode(res), false, true);
        });

        // Catalog
        handlers.put("getCatalog", (address, bytes) -> {
            List<Product> res = catalog.getCatalog();

            return new HandlerRes(serializer.encode(res), false, true);
        });

        handlers.put("getProduct", (address, bytes) -> {
            ProductGet pg = serializer.decode(bytes);
            Product res = catalog.getProduct(pg.getId());

            return new HandlerRes(serializer.encode(res), false, true);
        });

        handlers.put("getPrice", (address, bytes) -> {
            String idProduct = serializer.decode(bytes);
            float res = catalog.getPrice(idProduct);

            return new HandlerRes(serializer.encode(res), false, true);
        });

        handlers.put("getAvailability", (address, bytes) -> {
            String idProduct = serializer.decode(bytes);
            int res = catalog.getAvailability(idProduct);

            return new HandlerRes(serializer.encode(res), false, true);
        });

        this.connection.initialize(processDBUpdate, afterDBStart, tablesToCreate, handlers);
    }

    public static void main(String[] args) throws InterruptedException, SpreadException, ExecutionException, UnknownHostException {
        if (args.length != 1) {
            System.out.println("Please indicate the server port.");
            System.exit(1);
        }

        Supermarket s = new Supermarket(args[0]);
        s.initialize();

        System.out.println("Server is running on port " + args[0] + " ...");

        new CompletableFuture<>().get();
    }
}
