package Bluvolt.bluvolt.repository;

import Bluvolt.bluvolt.model.Consumidor;
import Bluvolt.bluvolt.model.Favorito;
import Bluvolt.bluvolt.model.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface FavoritoRepository extends JpaRepository<Favorito, Long> {

    boolean existsByConsumidorAndProduto(Consumidor consumidor, Produto produto);
    Optional<Favorito> findByConsumidorAndProduto(Consumidor consumidor, Produto produto);
    List<Favorito> findAllByConsumidor(Consumidor consumidor);
}
