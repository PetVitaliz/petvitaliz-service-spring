package com.petvitaliz.service;

import com.petvitaliz.dto.AgendamentoRequest;
import com.petvitaliz.dto.ConsultaUsuarioResponse;
import com.petvitaliz.entity.Consulta;
import com.petvitaliz.entity.Funcionario;
import com.petvitaliz.entity.Pet;
import com.petvitaliz.enums.ConsultaStatus;
import com.petvitaliz.enums.FuncionarioEspecialidade;
import com.petvitaliz.exception.ApiException;
import com.petvitaliz.repository.ConsultaRepository;
import com.petvitaliz.repository.FuncionarioRepository;
import com.petvitaliz.repository.PetRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class AgendamentoService {

    private static final int CARGA_MAXIMA = 8;
    private static final String SEM_PROFISSIONAL =
            "Nenhum profissional disponivel no momento, desculpe a inconveniencia";
    private static final String HORARIO_OCUPADO = "Este horrario ja possui uma consulta ativa";

    private final ConsultaRepository consultaRepository;
    private final FuncionarioRepository funcionarioRepository;
    private final PetRepository petRepository;
    private final Clock clock;

    public AgendamentoService(ConsultaRepository consultaRepository,
                              FuncionarioRepository funcionarioRepository,
                              PetRepository petRepository,
                              Clock clock) {
        this.consultaRepository = consultaRepository;
        this.funcionarioRepository = funcionarioRepository;
        this.petRepository = petRepository;
        this.clock = clock;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void agendar(Integer idUsuario, AgendamentoRequest req) {
        FuncionarioEspecialidade especialidade = AgendamentoRegras.servico(req.servico());
        LocalDate data = AgendamentoRegras.lerData(req.dataConsulta());
        String horaInicio = AgendamentoRegras.normalizarHora(req.horaInicio());
        AgendamentoRegras.exigirFuturo(data, horaInicio, LocalDateTime.now(clock));
        String observacoes = AgendamentoRegras.observacoes(req.observacoes());

        Pet pet = petRepository.buscarDoUsuarioComTrava(req.idPet(), idUsuario)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Pet não encontrado"));

        boolean petOcupado = consultaRepository.existsByPetIdAndDataConsultaAndHoraInicioAndStatusNot(
                pet.getId(), data, horaInicio, ConsultaStatus.FINALIZADO);
        if (petOcupado) {
            throw new ApiException(HttpStatus.CONFLICT, "Seu pet já possui um agendamento neste horário.");
        }

        List<Funcionario> candidatos = funcionarioRepository
                .findByEspecialidadeAndAtivoTrueAndCargaGreaterThan(especialidade, 0);
        if (candidatos.isEmpty()) {
            throw new ApiException(HttpStatus.NOT_FOUND, SEM_PROFISSIONAL);
        }

        Set<Integer> ocupados = new HashSet<>(consultaRepository.idsFuncionariosOcupados(
                data, horaInicio, candidatos.stream().map(Funcionario::getId).toList()));
        List<Funcionario> livres = candidatos.stream()
                .filter(funcionario -> !ocupados.contains(funcionario.getId()))
                .toList();
        if (livres.isEmpty()) {
            throw new ApiException(HttpStatus.CONFLICT, HORARIO_OCUPADO);
        }

        Integer idEscolhido = livres.get(ThreadLocalRandom.current().nextInt(livres.size())).getId();
        Funcionario funcionario = funcionarioRepository.buscarComTrava(idEscolhido)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, SEM_PROFISSIONAL));

        if (consultaRepository.existsByFuncionarioIdAndDataConsultaAndHoraInicio(idEscolhido, data, horaInicio)) {
            throw new ApiException(HttpStatus.CONFLICT, HORARIO_OCUPADO);
        }
        if (funcionarioRepository.reservarCarga(idEscolhido) == 0) {
            throw new ApiException(HttpStatus.NOT_FOUND, SEM_PROFISSIONAL);
        }

        Consulta consulta = new Consulta();
        consulta.setPet(pet);
        consulta.setFuncionario(funcionario);
        consulta.setDataConsulta(data);
        consulta.setHoraInicio(horaInicio);
        consulta.setHoraFim(AgendamentoRegras.horaFim(horaInicio));
        consulta.setStatus(ConsultaStatus.EM_ESPERA);
        consulta.setObservacoes(observacoes);
        consultaRepository.save(consulta);
    }

    @Transactional(readOnly = true)
    public List<ConsultaUsuarioResponse> listar(Integer idUsuario) {
        List<ConsultaUsuarioResponse> consultas = consultaRepository.listarDoUsuario(idUsuario).stream()
                .map(ConsultaUsuarioResponse::de)
                .toList();
        if (consultas.isEmpty()) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Você não possui nenhuma consulta");
        }
        return consultas;
    }

    @Transactional
    public void cancelar(Integer idUsuario, Integer idConsulta) {
        Consulta consulta = consultaRepository.buscarDoUsuario(idConsulta, idUsuario)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Agendamento não encontrado."));

        if (consulta.getStatus() == ConsultaStatus.FINALIZADO) {
            throw new ApiException(HttpStatus.CONFLICT, "Consultas finalizadas não podem ser canceladas");
        }

        Integer idFuncionario = consulta.getFuncionario().getId();
        consultaRepository.delete(consulta);
        funcionarioRepository.devolverCarga(idFuncionario, CARGA_MAXIMA);
    }
}
