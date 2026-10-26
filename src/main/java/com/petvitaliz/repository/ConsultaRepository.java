package com.petvitaliz.repository;

import com.petvitaliz.entity.Consulta;
import com.petvitaliz.enums.ConsultaStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ConsultaRepository extends JpaRepository<Consulta, Integer> {

    @Query("select c from Consulta c join fetch c.pet p join fetch c.funcionario "
            + "where p.usuario.id = :idUsuario order by c.dataConsulta desc, c.horaInicio asc")
    List<Consulta> listarDoUsuario(@Param("idUsuario") Integer idUsuario);

    @Query("select c from Consulta c join fetch c.funcionario "
            + "where c.id = :id and c.pet.usuario.id = :idUsuario")
    Optional<Consulta> buscarDoUsuario(@Param("id") Integer id, @Param("idUsuario") Integer idUsuario);

    boolean existsByFuncionarioIdAndDataConsultaAndHoraInicio(
            Integer idFuncionario, LocalDate dataConsulta, String horaInicio);

    boolean existsByPetIdAndDataConsultaAndHoraInicioAndStatusNot(
            Integer idPet, LocalDate dataConsulta, String horaInicio, ConsultaStatus status);

    @Query("select distinct c.funcionario.id from Consulta c "
            + "where c.dataConsulta = :data and c.horaInicio = :hora and c.funcionario.id in :ids")
    List<Integer> idsFuncionariosOcupados(@Param("data") LocalDate data,
                                          @Param("hora") String hora,
                                          @Param("ids") Collection<Integer> ids);
}
