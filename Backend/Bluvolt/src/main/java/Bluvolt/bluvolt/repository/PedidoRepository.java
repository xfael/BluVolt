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

    @Query("SELECT COUNT(p) FROM Pedido p " +
            "WHERE p.empresa.id = :empresaId " +
            "AND p.status = 'PROCESSANDO'")
    int countPedidosPendentes(@Param("empresaId") Long empresaId);

    @Query("SELECT SUM(p.valorTotal) FROM Pedido p " +
            "WHERE p.empresa.id = :empresaId " +
            "AND p.status <> 'PROCESSANDO'")
    Double totalVendasConfirmadas(@Param("empresaId") Long empresaId);

    @Query("SELECT COUNT(DISTINCT p.nomeCliente) FROM Pedido p " +
            "WHERE p.empresa.id = :empresaId " +
            "AND p.status <> 'PROCESSANDO'")
    int countClientes(@Param("empresaId") Long empresaId);

    List<Pedido> findAllByEmpresaIdOrderByDataPedidoDesc(Long empresaId);

    @Query("SELECT COALESCE(SUM(p.valorTotal), 0) FROM Pedido p WHERE p.empresa.id = :empresaId AND p.dataPedido >= :dataLimite")
    Double totalVendasPorEmpresaNoUltimoMes(@Param("empresaId") Long empresaId, @Param("dataLimite") LocalDate dataLimite);

    @Query("SELECT COUNT(p) FROM Pedido p WHERE p.empresa.id = :empresaId AND p.status = 'PROCESSANDO'")
    Long countPedidosPendentesPorEmpresa(@Param("empresaId") Long empresaId);

    @Query("SELECT COUNT(DISTINCT p.nomeCliente) FROM Pedido p WHERE p.empresa.id = :empresaId")
    Long countClientesDiferentesPorEmpresa(@Param("empresaId") Long empresaId);

    @Query("SELECT p FROM Pedido p WHERE p.empresa.id = :empresaId")
    List<Pedido> findPedidosByEmpresaId(@Param("empresaId") Long empresaId);
}
