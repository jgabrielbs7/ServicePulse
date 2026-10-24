package com.helpdesk.repository;

import com.helpdesk.model.Chamado;
import com.helpdesk.model.StatusChamado;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ChamadoRepository extends JpaRepository<Chamado, Long> {

    List<Chamado> findAllByOrderByDataAberturaDesc();

    List<Chamado> findByStatusOrderByDataAberturaDesc(StatusChamado status);

    long countByStatus(StatusChamado status);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update Chamado c set c.motivoAbertura = :novo where c.motivoAbertura = :antigo")
    int renomearMotivo(@Param("antigo") String antigo, @Param("novo") String novo);

    @Query("select distinct c.motivoAbertura from Chamado c")
    List<String> motivosDistintos();

    long countByStatusAndDataFechamentoBetween(StatusChamado status, LocalDateTime inicio, LocalDateTime fim);

    @Query("""
           select avg(c.tempoAtendimentoMinutos) from Chamado c
           where c.status = :status and c.tempoAtendimentoMinutos is not null
           """)
    Double mediaTempoAtendimento(@Param("status") StatusChamado status);

    /** Volume de chamados por dia (somente dias com registros). */
    @Query(value = """
           select to_char(data_abertura, 'YYYY-MM-DD') as dia, count(*) as total
           from tb_chamado
           where data_abertura >= :inicio
           group by to_char(data_abertura, 'YYYY-MM-DD')
           order by dia
           """, nativeQuery = true)
    List<ContagemPorDia> volumePorDia(@Param("inicio") LocalDateTime inicio);

    @Query("""
           select c.motivoAbertura as nome, count(c) as total
           from Chamado c
           group by c.motivoAbertura
           order by count(c) desc
           """)
    List<Contagem> motivosMaisFrequentes(Pageable pageable);

    @Query("""
           select c.cliente.razaoSocial as nome, count(c) as total
           from Chamado c
           group by c.cliente.razaoSocial
           order by count(c) desc
           """)
    List<Contagem> topClientes(Pageable pageable);
}
