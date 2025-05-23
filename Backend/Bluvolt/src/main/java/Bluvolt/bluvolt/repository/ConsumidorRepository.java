package Bluvolt.bluvolt.repository;

import Bluvolt.bluvolt.model.Consumidor;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ConsumidorRepository extends CrudRepository<Consumidor, String> {
    Consumidor findById(long id);

    @Query(value = "SELECT * FROM bluvoltuser.consumidor WHERE email = :email AND senha = :senha", nativeQuery = true)
    Consumidor login(@Param("email") String email, @Param("senha") String senha);
}
