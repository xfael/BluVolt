package Bluvolt.bluvolt.repository;

import Bluvolt.bluvolt.model.Consumidor;
import Bluvolt.bluvolt.model.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface ConsumidorRepository extends CrudRepository<Consumidor, String> {
    Consumidor findById(long id);
}
