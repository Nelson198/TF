/**
 * Supermarket Interface
 */
public interface Supermarket {
    // Iniciar compra / Criar carrinho
    void start();
    // Consultar o preço de um produto
    float getPrice(int id);
    // Verificar a disponibilidade de um produto
    boolean isAvailable(int id);
    // Acrescentar um ou mais produtos ao carrinho de compras
    void add(int id, int amount);
    // Confirmar encomenda, indicando se foi concretizada com sucesso
    boolean checkout(int id);
}