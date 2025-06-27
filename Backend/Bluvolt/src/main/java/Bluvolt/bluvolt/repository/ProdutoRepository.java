package Bluvolt.bluvolt.repository;

import Bluvolt.bluvolt.model.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Long> {
    List<Produto> findAllByEmpresaId(Long empresaId);

    Long countByEmpresaId(Long empresaId);


}
