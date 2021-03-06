# Mestrado em Engenharia Informática - UMinho
## Tolerância a Faltas
## Trabalho Prático - 2019/2020

### Contribuidores

[João Imperadeiro](https://github.com/JRI98)  
[José Boticas](https://github.com/SacitobJose)  
[Nelson Teixeira](https://github.com/Nelson198)  

### Instalação

* Ficheiro *.jar* relativo ao *spread*:
```bash
mvn install:install-file -Dfile=/path/to/spread-4.4.0.jar \
                         -DgroupId=org.spread \
                         -DartifactId=spread \
                         -Dversion=4.4.0 \
                         -Dpackaging=jar \
                         -DgeneratePom=true
```

### Arranque do sistema

* *Spread*:
```bash
./spread-src-5.0.1/daemon/spread -c spread.conf
```

* Servidor:
```bash
mvn exec:java -Dexec.mainClass=Server.Supermarket -Dexec.args="1111"
```

* Cliente:
```bash
mvn exec:java -Dexec.mainClass=Client.Client -Dexec.args="9999 1111"
```