package Server;

import Helpers.CartUpdate;
import Helpers.Product;
import Helpers.Serializers;
import Messages.DBUpdate;
import Middleware.HandlerRes;
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

    // Skeleton for the catalog
    private CatalogSkeleton catalog;

    // Skeletons for the carts
    private final Map<Integer, CartSkeleton> carts = new HashMap<>();

    // Self use serializer
    public static Serializer serializer = Serializers.clientSerializer;

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
        BiFunction<DBUpdate, Connection, byte[]> processDBUpdate = (dbUpdate, dbConnection) -> {
            switch (dbUpdate.getSecondaryType()) {
                // Cart
                case "newCart":
                    CartSkeleton cs = new CartSkeleton(dbConnection);
                    this.carts.put(cs.getIdCart(), cs);

                    this.connection.startTimer(serializer.encode(cs.getIdCart()), "deleteCart", 5*60, false); // 5min seems to be a good cart lifetime

                    return serializer.encode(cs.getIdCart());

                case "deleteCart":
                    int objectID = serializer.decode(dbUpdate.getUpdateInfo());
                    CartSkeleton cartSkeleton = this.carts.get(objectID);
                    if (cartSkeleton != null) {
                        cartSkeleton.delete();
                        this.carts.remove(objectID);
                    }

                    return serializer.encode(null);

                case "checkout":
                    int objID = serializer.decode(dbUpdate.getUpdateInfo());

                    Boolean res = null;
                    CartSkeleton cart = this.carts.get(objID);
                    if (cart != null) {
                        res = cart.checkout();
                        this.carts.remove(objID);
                    }

                    return serializer.encode(res);

                case "updateCart":
                    CartUpdate cartUpdate = serializer.decode(dbUpdate.getUpdateInfo());
                    CartSkeleton cartSkeleton1 = this.carts.get(cartUpdate.getIdCart());
                    if (cartSkeleton1 != null)
                        cartSkeleton1.updateProduct(cartUpdate.getIdProduct(), cartUpdate.getAmount());

                    return serializer.encode(null);

                // Catalog
                case "addProduct":
                    Product p = serializer.decode(dbUpdate.getUpdateInfo());
                    catalog.addProduct(p);

                    return serializer.encode(null);

                case "removeProduct":
                    int prodId = serializer.decode(dbUpdate.getUpdateInfo());
                    catalog.removeProduct(prodId);

                    return serializer.encode(null);

                case "updateAmount":
                    CartUpdate ua = serializer.decode(dbUpdate.getUpdateInfo());
                    catalog.updateAmount(ua.getIdProduct(), ua.getAmount());

                    return serializer.encode(null);

                case "updateProduct":
                    Product p4 = serializer.decode(dbUpdate.getUpdateInfo());
                    catalog.updateProduct(p4);

                    return serializer.encode(null);
            }
            return serializer.encode(null);
        };

        Consumer<Connection> afterDBStart = (dbConnection) -> this.catalog = new CatalogSkeleton(dbConnection);

        List<String> tablesToCreate = new ArrayList<>();
        tablesToCreate.add("CREATE TABLE cart (id INT NOT NULL IDENTITY)");
        tablesToCreate.add("CREATE TABLE product (id INT NOT NULL IDENTITY, name VARCHAR(100) NOT NULL, description VARCHAR(100) NOT NULL, price FLOAT NOT NULL, amount INT NOT NULL)");
        tablesToCreate.add("CREATE TABLE cartProduct (idCart INT NOT NULL, idProduct INT NOT NULL, amount INT NULL, FOREIGN KEY (idCart) REFERENCES cart (id), FOREIGN KEY (idProduct) REFERENCES product (id), PRIMARY KEY (idCart, idProduct))");

        Map<String, BiFunction<Address, byte[], HandlerRes>> handlers = new HashMap<>();

        // Cart
        handlers.put("newCart", (address, bytes) -> new HandlerRes(null, true, false));

        handlers.put("updateCart", (address, bytes) -> new HandlerRes(bytes, true, false));

        handlers.put("checkout", (address, bytes) -> new HandlerRes(bytes, true, false));

        handlers.put("getProducts", (address, bytes) -> {
            int idCart = serializer.decode(bytes);

            List<Product> res = null;
            CartSkeleton cart = carts.get(idCart);
            if (cart != null)
                res = new ArrayList<>(cart.getProducts());

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

        handlers.put("addProduct", (address, bytes) -> new HandlerRes(bytes, true, false));

        handlers.put("removeProduct", (address, bytes) -> new HandlerRes(bytes, true, false));

        handlers.put("updateAmount", (address, bytes) -> new HandlerRes(bytes, true, false));

        handlers.put("updateProduct", (address, bytes) -> new HandlerRes(bytes, true, false));

        this.connection.initialize(processDBUpdate, afterDBStart, tablesToCreate, handlers);
    }

    /**
     * clearTerminal
     */
    private static void clearTerminal() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    public static void main(String[] args) throws InterruptedException, SpreadException, ExecutionException, UnknownHostException {
        clearTerminal();
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
