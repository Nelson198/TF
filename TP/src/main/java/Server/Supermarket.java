package Server;

import Helpers.CartUpdate;
import Helpers.Product;
import Helpers.Serializers;
import Messages.DBUpdate;
import Middleware.ServerConnection;

import io.atomix.utils.net.Address;
import io.atomix.utils.serializer.Serializer;

import spread.SpreadException;

import java.net.UnknownHostException;
import java.sql.Connection;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * Supermarket Implementation
 */
public class Supermarket {
    // Connection to the cluster
    private final ServerConnection connection;

    // Serializer for the messages sent between servers
    private final Serializer serializer = Serializers.serverSerializer;

    // Skeleton for the catalog
    private CatalogSkeleton catalog;

    // Skeletons for the carts
    private final Map<Integer, CartSkeleton> carts = new HashMap<>();

    /**
     * Parameterized constructor
     * @param port Port
     */
    public Supermarket(String port) {
        this.connection = new ServerConnection(port);
    }

    /**
     * Initialize
     * @throws SpreadException SpreadException
     * @throws UnknownHostException UnknownHostException
     */
    public void initialize() throws SpreadException, UnknownHostException {
        BiFunction<DBUpdate, Connection, Object> processDBUpdate = (dbUpdate, dbConnection) -> {
            switch (dbUpdate.getSecondaryType()) {
                case "newCart":
                    CartSkeleton cs = new CartSkeleton(dbConnection);
                    this.carts.put(cs.getIdCart(), cs);

                    this.connection.startTimer(cs.getIdCart(), "deleteCart", 180); // TODO - 180s for the moment

                    return cs.getIdCart();
                case "deleteCart":
                    int objectID = (int) dbUpdate.getUpdateInfo();
                    this.carts.get(objectID).delete();
                    this.carts.remove(objectID);

                    return null;
                case "checkout":
                    int objID = (int) dbUpdate.getUpdateInfo();
                    boolean res = this.carts.get(objID).checkout();
                    this.carts.remove(objID);

                    return res;
                case "updateCart":
                    CartUpdate cartUpdate = (CartUpdate) dbUpdate.getUpdateInfo();
                    CartSkeleton cart = this.carts.get(cartUpdate.getIdCart());
                    cart.updateProduct(cartUpdate.getIdProduct(), cartUpdate.getAmount());

                    return null;
            }
            return null;
        };

        Consumer<Connection> afterDBStart = (dbConnection) -> this.catalog = new CatalogSkeleton(dbConnection);

        List<String> tablesToCreate = new ArrayList<>();
        tablesToCreate.add("CREATE TABLE cart (id INT NOT NULL IDENTITY)");
        tablesToCreate.add("CREATE TABLE product (id INT NOT NULL IDENTITY, name VARCHAR(100) NOT NULL, description VARCHAR(100) NOT NULL, price FLOAT NOT NULL, amount INT NOT NULL)");
        tablesToCreate.add("CREATE TABLE cartProduct (idCart INT NOT NULL, idProduct INT NOT NULL, amount INT NULL, FOREIGN KEY (idCart) REFERENCES cart (id), FOREIGN KEY (idProduct) REFERENCES product (id), PRIMARY KEY (idCart, idProduct))");

        Map<String, BiFunction<Address, byte[], HandlerRes>> handlers = new HashMap<>();

        // Cart
        handlers.put("newCart", (address, bytes) -> new HandlerRes(null, true, false));

        handlers.put("updateCart", (address, bytes) -> {
            CartUpdate cu = serializer.decode(bytes);

            return new HandlerRes(cu, true, false);
        });

        handlers.put("checkout", (address, bytes) -> {
            String cartID = serializer.decode(bytes);

            return new HandlerRes(cartID, true, false);
        });

        handlers.put("getProducts", (address, bytes) -> {
            int idCart = serializer.decode(bytes);
            List<Product> res = new ArrayList<>(carts.get(idCart).getProducts());

            return new HandlerRes(serializer.encode(res), false, true);
        });

        // Catalog
        handlers.put("getCatalog", (address, bytes) -> {
            List<Product> res = catalog.getCatalog();

            return new HandlerRes(serializer.encode(res), false, true);
        });

        handlers.put("getProduct", (address, bytes) -> {
            int idProduct = serializer.decode(bytes);
            Product res = catalog.getProduct(idProduct);

            return new HandlerRes(serializer.encode(res), false, true);
        });

        handlers.put("getPrice", (address, bytes) -> {
            int idProduct = serializer.decode(bytes);
            float res = catalog.getPrice(idProduct);

            return new HandlerRes(serializer.encode(res), false, true);
        });

        handlers.put("getAmount", (address, bytes) -> {
            int idProduct = serializer.decode(bytes);
            int res = catalog.getAmount(idProduct);

            return new HandlerRes(serializer.encode(res), false, true);
        });

        handlers.put("addProduct", ((address, bytes) -> new HandlerRes(bytes, false, true) ));

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
