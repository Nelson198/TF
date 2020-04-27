## Trabalho prático

Classes:

- Encomenda
- Produto
- Carrinho de Compras
- (Interface) Supermercado - servidor: métodos - iniciar compra/criar carrinho (start), consultar preço e disponibilidade, acrescentar produto ao carrinho de compras (do something), confirmar encomenda indicando se foi concretizada com sucesso (commit).



Ter uma interface para o supermercado e depois a implementação concreta.

Cada servidor tem uma base de dados.

Para perguntar:

1. Como fazer o backup do carrinho de compras? Todos os servidores guardam os carrinhos dos outros? Em anel?
   1. Tentar utilizar um dos algoritmos que vimos nas aulas. Não pensar demasiado nisto. Não tentar "inventar".
2. Histórico de compras?
   1. Podemos ou não fazer.
3. TMAX? Desde o início da encomenda? Desde que a mesma foi confirmada?
   1. Desde que se inicia a compra até desistir ou confirmar a encomenda.
4. Quando um servidor falha, o de backup envia os seus carrinhos de compras para os outros? Desta forma seria possível o cliente, ao voltar a estabelecer ligação, ligar-se a qq servidor. (Depende da primeira)
   1. Não faz sentido pensar nisto.



Base de dados: uma tabela para o **catálogo**, que contém os produtos, com id, nome, descrição, preço e quantidade;

A base de dados **guarda** os carrinhos de compras.

Quando um servidor reinicia, recebe apenas o que tem de atualizar, ou seja, o que falta na sua base de dados.

A base de dados mantém dois ficheiros - um que é só append (re-do log) e outro que vai sendo modificado. É possível fazer um checkpoint no primeiro ficheiro.



