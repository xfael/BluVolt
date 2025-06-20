package Bluvolt.bluvolt.repository;

import Bluvolt.bluvolt.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    @Query("SELECT MONTH(p.dataPedido), SUM(p.valorTotal) " +
            "FROM Pedido p " +
            "WHERE p.empresa.id = :empresaId " +
            "AND p.dataPedido >= :inicio " +
            "GROUP BY MONTH(p.dataPedido)")
    List<Object[]> findVendasMensais(@Param("empresaId") Long empresaId,
                                     @Param("inicio") LocalDate inicio);
}
