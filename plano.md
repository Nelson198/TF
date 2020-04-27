# Plano - Trabalho prático

## Classes

* Encomenda;
* Produto;
* Carrinho de Compras;
* Supermercado (**implementação**):
   * métodos do **servidor**:
      * iniciar compra / criar carrinho (*start*);
      * consultar preço e disponibilidade;
      * acrescentar produto ao carrinho de compras (*do something*);
      * confirmar encomenda, indicando se foi concretizada com sucesso (*commit*).

## Interface

* Supermercado.

## Base de dados

* Cada servidor tem uma base de dados;
* Implementar uma tabela para o **catálogo**, que contém os produtos, com *id*, nome, descrição, preço e quantidade;
* A base de dados **guarda** os carrinhos de compras;
* A base de dados mantém dois ficheiros:
   * *re-do log* : ficheiro com modo de escrita *append* onde existe a possibilidade de realizar *checkpoint*;
   * ficheiro que vai sendo modificado.

### Observação

1. Quando um servidor reinicia, recebe apenas o que tem de atualizar, ou seja, o que falta na sua base de dados.

### Perguntas

1. Como fazer o *backup* do carrinho de compras ? Todos os servidores guardam os carrinhos dos outros ? Em anel ?
   * Tentar utilizar um dos algoritmos que vimos nas aulas. Não pensar demasiado nisto. Não tentar "inventar".
2. Histórico de compras ?
   * Podemos ou não fazer.
3. *TMAX* ? Desde o início da encomenda ? Desde que a mesma foi confirmada ?
   * Desde que se inicia a compra até desistir ou confirmar a encomenda.
4. Quando um servidor falha, o de *backup* envia os seus carrinhos de compras para os outros ? Desta forma seria possível o cliente, ao voltar a estabelecer ligação, ligar-se a qualquer servidor. (depende da primeira)
   * Não faz sentido pensar nisto.
