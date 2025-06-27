package Bluvolt.bluvolt.repository;

import Bluvolt.bluvolt.model.Consumidor;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ConsumidorRepository extends CrudRepository<Consumidor, Long> {
    Consumidor findById(long id);

    @Query(value = "SELECT * FROM bluvoltuser.consumidor WHERE email = :email AND senha = :senha", nativeQuery = true)
    Consumidor login(@Param("email") String email, @Param("senha") String senha);

    @Query("SELECT COUNT(c) FROM Consumidor c WHERE c.email = :email AND c.id <> :id")
    Long countByEmailAndIdNot(@Param("email") String email, @Param("id") Long id);

    Optional<Consumidor> findByEmail(String email);

    boolean existsByEmail(String email);


}

