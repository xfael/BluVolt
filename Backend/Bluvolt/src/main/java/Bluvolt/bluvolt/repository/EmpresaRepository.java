package Bluvolt.bluvolt.repository;

import Bluvolt.bluvolt.model.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface EmpresaRepository extends CrudRepository<Empresa, Long> {
    Empresa findById(long id);

    @Query(value = "SELECT * FROM bluvoltuser.empresa WHERE email = :email AND senha = :senha", nativeQuery = true)
    Empresa login(@Param("email") String email, @Param("senha") String senha);
}
