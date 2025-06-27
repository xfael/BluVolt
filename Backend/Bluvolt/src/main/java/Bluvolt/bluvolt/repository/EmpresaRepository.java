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

    Optional<Empresa> findByEmail(String email);
    boolean existsByEmail(String email);

    // Método para verificar se email existe em outra empresa
    @Query("SELECT COUNT(e) > 0 FROM Empresa e WHERE e.email = :email AND e.id <> :id")
    boolean existsByEmailAndIdNot(@Param("email") String email, @Param("id") Long id);

    // Seus outros métodos existentes...
    Empresa findByEmailAndSenha(String email, String senha);
}
