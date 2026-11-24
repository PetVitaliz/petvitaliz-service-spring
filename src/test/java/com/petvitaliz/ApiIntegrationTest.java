package com.petvitaliz;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petvitaliz.controller.AuthController;
import com.petvitaliz.entity.Admin;
import com.petvitaliz.entity.Appointment;
import com.petvitaliz.entity.Employee;
import com.petvitaliz.entity.Product;
import com.petvitaliz.enums.AppointmentStatus;
import com.petvitaliz.enums.EmployeeSpecialty;
import com.petvitaliz.repository.AdminRepository;
import com.petvitaliz.repository.AppointmentRepository;
import com.petvitaliz.repository.EmployeeRepository;
import com.petvitaliz.repository.PetRepository;
import com.petvitaliz.repository.ProductRepository;
import com.petvitaliz.support.MutableClock;
import com.petvitaliz.web.IdempotencyFilter;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:petvitaliz;MODE=MySQL;DATABASE_TO_LOWER=TRUE;"
                + "CASE_INSENSITIVE_IDENTIFIERS=TRUE;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.mail.username=clinic@petvitaliz.test",
        "spring.mail.password=unused",
        "app.jwt.secret=integration-test-secret-with-32-chars-min",
        "app.cookies.secure=false",
        "app.cookies.same-site=Lax",
        "app.cloudinary.cloud-name=",
        "app.cloudinary.api-key=",
        "app.cloudinary.api-secret=",
        "app.rate-limit.general-limit=100000",
        "app.rate-limit.auth-limit=100000"
})
@AutoConfigureMockMvc
class ApiIntegrationTest {

    private static final ZoneId ZONE = ZoneId.of("America/Sao_Paulo");
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 6, 15, 14, 0);
    private static final LocalDate TODAY = NOW.toLocalDate();
    private static final String PASSWORD = "secret123";
    private static final String CORPORATE_DOMAIN = "@petvitalizfuncionario.com";
    private static final AtomicInteger SEQUENCE = new AtomicInteger(100);

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MutableClock clock;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private AdminRepository adminRepository;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private PetRepository petRepository;
    @Autowired
    private AppointmentRepository appointmentRepository;
    @MockitoBean
    private JavaMailSender mailSender;

    @TestConfiguration
    static class ClockConfiguration {

        @Bean
        @Primary
        MutableClock testClock() {
            return new MutableClock(NOW, ZONE);
        }
    }

    @BeforeEach
    void prepareMailSender() {
        when(mailSender.createMimeMessage()).thenAnswer(invocation -> new MimeMessage((Session) null));
    }

    @AfterEach
    void resetClock() {
        clock.reset();
    }

    @Test
    void publicRoutesAreOpenAndPrivateRoutesRequireTheRightLogin() throws Exception {
        mockMvc.perform(get("/")).andExpect(status().isOk()).andExpect(jsonPath("$.pagina").value("Home"));
        mockMvc.perform(get("/health")).andExpect(status().isOk()).andExpect(jsonPath("$.DB").value("ON"));
        mockMvc.perform(get("/adm/listar/produtos")).andExpect(status().isOk());

        mockMvc.perform(get("/user/home"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensagem").value("Acesso negado, faça login"));
        mockMvc.perform(get("/adm"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensagem").value("Acesso negado, faça login como administrador"));
        mockMvc.perform(get("/funcionario").cookie(loginNewUser()))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/user/home").cookie(new Cookie("token", "not.a.token")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.mensagem").value("Sessão expirada, faça login novamente"));
    }

    @Test
    void userSignsUpLogsInAndManagesPets() throws Exception {
        String email = signUp();
        mockMvc.perform(postJson("/user/cadastro", signupBody(email)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("Email já cadastrado"));
        mockMvc.perform(postJson("/user/login", Map.of("email", email, "senha", "wrong-password")))
                .andExpect(status().isUnauthorized());

        Cookie user = login("/user/login", email);
        mockMvc.perform(get("/user/buscar/perfil").cookie(user))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.CPF").exists());

        mockMvc.perform(multipart("/user/listar/pet/cadastar")
                        .param("nome", "Rex")
                        .param("especie", "cachorro")
                        .param("sexo", "M")
                        .param("data_nascimento", "2020-01-10")
                        .param("idade", "5")
                        .param("peso", "12,5")
                        .cookie(user))
                .andExpect(status().isCreated())
                .andExpect(content().string("\"Pet cadastrado com sucesso\""));

        Map<String, Object> hamster = new LinkedHashMap<>();
        hamster.put("nome", "Mia");
        hamster.put("especie", "outro");
        hamster.put("outra_especie", "hamster");
        hamster.put("sexo", "F");
        hamster.put("data_nascimento", "2026-03-01");
        hamster.put("peso", 0.2);
        mockMvc.perform(postJson("/user/listar/pet/cadastar", hamster).cookie(user))
                .andExpect(status().isCreated());

        MvcResult list = mockMvc.perform(get("/user/listar/pet").cookie(user))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pets", hasSize(2)))
                .andExpect(jsonPath("$.pets[0].especie").value("cachorro"))
                .andExpect(jsonPath("$.pets[0].data_nascimento").value("2020-01-10T00:00:00.000Z"))
                .andExpect(jsonPath("$.pets[1].outra_especie").value("hamster"))
                .andReturn();
        int petId = objectMapper.readTree(list.getResponse().getContentAsString()).at("/pets/0/id_pet").asInt();

        mockMvc.perform(multipart("/user/listar/pet/editar/" + petId)
                        .param("nome", "Rex Junior")
                        .param("especie", "cachorro")
                        .param("sexo", "macho")
                        .param("data_nascimento", "2020-01-10")
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        })
                        .cookie(user))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pet.nome").value("Rex Junior"))
                .andExpect(jsonPath("$.pet.idade").value(5));

        mockMvc.perform(delete("/user/listar/pet/delete/" + petId).cookie(loginNewUser()))
                .andExpect(status().isNotFound());
        mockMvc.perform(delete("/user/listar/pet/delete/" + petId).cookie(user))
                .andExpect(status().isOk());
        assertThat(petRepository.findById(petId)).isEmpty();
    }

    @Test
    void schedulingReservesAnEmployeeAndCancellingReleasesIt() throws Exception {
        Employee groomer = createEmployee(EmployeeSpecialty.GROOMER);
        Cookie user = loginNewUser();
        int petId = createPet(user);
        Map<String, Object> request = appointmentBody("tosador", petId, "2026-06-20", "9:00");

        mockMvc.perform(postJson("/agendamento", request).cookie(user))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.mensagem").value("Consulta agendada com sucesso"));
        assertThat(workloadOf(groomer)).isEqualTo(Employee.MAX_WORKLOAD - 1);

        mockMvc.perform(postJson("/agendamento", request).cookie(user))
                .andExpect(status().isConflict());
        mockMvc.perform(postJson("/agendamento", appointmentBody("tosador", petId, "2026-06-14", "09:00")).cookie(user))
                .andExpect(status().isBadRequest());
        mockMvc.perform(postJson("/agendamento", appointmentBody("financeiro", petId, "2026-06-20", "10:00"))
                        .cookie(user))
                .andExpect(status().isBadRequest());

        MvcResult list = mockMvc.perform(get("/consultas").cookie(user))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.consultas", hasSize(1)))
                .andExpect(jsonPath("$.consultas[0].hora_inicio").value("09:00"))
                .andExpect(jsonPath("$.consultas[0].hora_fim").value("10:00"))
                .andExpect(jsonPath("$.consultas[0].status").value("em_espera"))
                .andExpect(jsonPath("$.consultas[0].funcionario.especialidade").value("tosador"))
                .andReturn();
        int appointmentId = objectMapper.readTree(list.getResponse().getContentAsString())
                .at("/consultas/0/id_consulta").asInt();

        mockMvc.perform(delete("/user/listar/pet/delete/" + petId).cookie(user))
                .andExpect(status().isConflict());
        mockMvc.perform(delete("/user/agendamento/cancelar/" + appointmentId).cookie(loginNewUser()))
                .andExpect(status().isNotFound());
        mockMvc.perform(delete("/user/agendamento/cancelar/" + appointmentId).cookie(user))
                .andExpect(status().isOk());
        assertThat(workloadOf(groomer)).isEqualTo(Employee.MAX_WORKLOAD);
        mockMvc.perform(get("/consultas").cookie(user)).andExpect(status().isNotFound());
    }

    @Test
    void idempotencyKeyPreventsDuplicatedAppointments() throws Exception {
        createEmployee(EmployeeSpecialty.VETERINARIAN);
        Cookie user = loginNewUser();
        int petId = createPet(user);
        Map<String, Object> request = appointmentBody("veterinario", petId, "2026-06-21", "11:00");
        String key = "retry-" + SEQUENCE.incrementAndGet();

        mockMvc.perform(postJson("/agendamento", request).cookie(user).header(IdempotencyFilter.KEY_HEADER, key))
                .andExpect(status().isCreated())
                .andExpect(header().doesNotExist(IdempotencyFilter.REPLAYED_HEADER));
        mockMvc.perform(postJson("/agendamento", request).cookie(user).header(IdempotencyFilter.KEY_HEADER, key))
                .andExpect(status().isCreated())
                .andExpect(header().string(IdempotencyFilter.REPLAYED_HEADER, "true"))
                .andExpect(jsonPath("$.mensagem").value("Consulta agendada com sucesso"));
        mockMvc.perform(postJson("/agendamento", appointmentBody("veterinario", petId, "2026-06-22", "11:00"))
                        .cookie(user).header(IdempotencyFilter.KEY_HEADER, key))
                .andExpect(status().isUnprocessableEntity());

        mockMvc.perform(get("/consultas").cookie(user)).andExpect(jsonPath("$.consultas", hasSize(1)));
    }

    @Test
    void employeeFollowsTheAppointmentThroughTheDay() throws Exception {
        Employee veterinarian = createEmployee(EmployeeSpecialty.VETERINARIAN);
        Cookie owner = loginNewUser();
        int petId = createPet(owner);

        Appointment appointment = new Appointment();
        appointment.setPet(petRepository.getReferenceById(petId));
        appointment.setEmployee(veterinarian);
        appointment.setAppointmentDate(TODAY);
        appointment.setStartTime("15:00");
        appointment.setEndTime("16:00");
        appointment.setStatus(AppointmentStatus.WAITING);
        String statusPath = "/funcionario/consultas/atualizar/" + appointmentRepository.save(appointment).getId();
        String detailsPath = "/funcionario/consultas/" + appointment.getId();

        Cookie employee = login("/user/login/funcionario", veterinarian.getEmail());
        mockMvc.perform(get("/funcionario").cookie(employee))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.funcionario.especialidade").value("veterinario"))
                .andExpect(jsonPath("$.consultas", hasSize(1)))
                .andExpect(jsonPath("$.consultas[0].pet.usuario.nome").value("Kaio"));
        mockMvc.perform(get(detailsPath).cookie(employee))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.consulta.pet.usuario.telefone").exists());

        Employee colleague = createEmployee(EmployeeSpecialty.VETERINARIAN);
        mockMvc.perform(get(detailsPath).cookie(login("/user/login/funcionario", colleague.getEmail())))
                .andExpect(status().isNotFound());

        mockMvc.perform(putJson(statusPath, Map.of("novoStatus", "em_andamento")).cookie(employee))
                .andExpect(status().isBadRequest());

        clock.set(TODAY.atTime(15, 5));
        mockMvc.perform(get("/funcionario/consultas").cookie(employee))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.consultas[0].status").value("em_andamento"))
                .andExpect(jsonPath("$.consultas[0].pet.especie").value("cachorro"));
        mockMvc.perform(putJson(statusPath, Map.of("novoStatus", "finalizado")).cookie(employee))
                .andExpect(status().isBadRequest());

        clock.set(TODAY.atTime(16, 2));
        mockMvc.perform(putJson(statusPath, Map.of("novoStatus", "finalizado")).cookie(employee))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensagem").value("Status atualizado com sucesso"));
        mockMvc.perform(putJson(statusPath, Map.of("novoStatus", "em_andamento")).cookie(employee))
                .andExpect(status().isBadRequest());
        mockMvc.perform(delete("/user/agendamento/cancelar/" + appointment.getId()).cookie(owner))
                .andExpect(status().isConflict());

        mockMvc.perform(post("/funcionario/bater-ponto").cookie(employee)).andExpect(status().isOk());
        mockMvc.perform(get("/funcionario/perfil-dados").cookie(employee))
                .andExpect(jsonPath("$.email").value(veterinarian.getEmail()));
        mockMvc.perform(putJson("/funcionario/perfil-dados/atualizar",
                        Map.of("nome", "Ana", "sobrenome", "Lima", "email", "ana@gmail.com")).cookie(employee))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/funcionario/clientes").cookie(employee))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clientes[*].petsLista[*]", hasItem("Rex")));
        mockMvc.perform(get("/funcionario/pets").cookie(employee))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pets[*].especie", hasItem("Cão")));
        mockMvc.perform(get("/funcionario/logout").cookie(employee))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.SET_COOKIE, org.hamcrest.Matchers.startsWith("token_funcionario=;")));
    }

    @Test
    void adminManagesAccountsEmployeesAndProducts() throws Exception {
        Admin root = new Admin();
        root.setUsername("root" + SEQUENCE.incrementAndGet());
        root.setEmail(root.getUsername() + "@petvitaliz.test");
        root.setPassword(passwordEncoder.encode(PASSWORD));
        adminRepository.save(root);
        Cookie admin = login("/user/login/adm", root.getEmail());

        mockMvc.perform(get("/adm").cookie(admin)).andExpect(jsonPath("$.pagina").value("Home Administrador"));
        mockMvc.perform(get("/adm").cookie(loginNewUser())).andExpect(status().isUnauthorized());

        String username = "maria" + SEQUENCE.incrementAndGet();
        mockMvc.perform(multipart("/adm/listar/adm/cadastrar")
                        .param("username", username)
                        .param("email", username + "@petvitaliz.test")
                        .param("senha", PASSWORD)
                        .param("ativo", "true")
                        .cookie(admin))
                .andExpect(status().isCreated())
                .andExpect(content().string("\"Administrador cadastrado com sucesso\""));
        mockMvc.perform(multipart("/adm/listar/adm/cadastrar")
                        .param("username", username)
                        .param("email", "other" + username + "@petvitaliz.test")
                        .param("senha", PASSWORD)
                        .cookie(admin))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("\"Username ja cadastrado\""));
        mockMvc.perform(get("/adm/listar/adm").cookie(admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ADMs[*].ADM_NOME", hasItem(username)))
                .andExpect(jsonPath("$.ADMs[0].ADM_SENHA").doesNotExist());
        mockMvc.perform(delete("/adm/listar/adm/excluir/" + root.getId()).cookie(admin))
                .andExpect(status().isConflict());

        Map<String, Object> employee = new LinkedHashMap<>();
        employee.put("nome", "Beto");
        employee.put("sobrenome", "Souza");
        employee.put("email", "beto" + SEQUENCE.incrementAndGet() + "@gmail.com");
        employee.put("especialidade", "tosador");
        employee.put("ativo", true);
        employee.put("senha", PASSWORD);
        mockMvc.perform(postJson("/adm/listar/funcionario/cadastrar", employee).cookie(admin))
                .andExpect(status().isBadRequest());
        employee.put("email", "beto" + SEQUENCE.incrementAndGet() + CORPORATE_DOMAIN);
        mockMvc.perform(postJson("/adm/listar/funcionario/cadastrar", employee).cookie(admin))
                .andExpect(status().isCreated());
        mockMvc.perform(get("/adm/listar/funcionario").cookie(admin))
                .andExpect(jsonPath("$.funcionarios[0].nome").value("Beto"))
                .andExpect(jsonPath("$.funcionarios[0].carga").value(Employee.MAX_WORKLOAD))
                .andExpect(jsonPath("$.funcionarios[0].senha").doesNotExist());

        Map<String, Object> product = new LinkedHashMap<>();
        product.put("nome", "Plano Prata");
        product.put("descricao", "Plano basico");
        product.put("beneficios", "Banho mensal");
        product.put("preco", "29.5");
        mockMvc.perform(postJson("/adm/listar/produtos/cadastrar", product).cookie(admin))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("\"Preço deve ser um numero\""));
        mockMvc.perform(postJson("/adm/listar/produtos/cadastrar", product))
                .andExpect(status().isUnauthorized());

        Product saved = new Product();
        saved.setName("Plano Bronze");
        saved.setDescription("Plano de entrada");
        saved.setBenefits("Uma consulta");
        saved.setPrice(new BigDecimal("19.90"));
        String productPath = "/adm/listar/produtos/editar/" + productRepository.save(saved).getId();
        product.put("preco", 35);
        mockMvc.perform(putJson(productPath, product).cookie(admin))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.novo_Produto.nome").value("Plano Prata"))
                .andExpect(jsonPath("$.novo_Produto.preco").value("35"));
        mockMvc.perform(get("/adm/listar/produtos"))
                .andExpect(jsonPath("$.produtos[*].nome", hasItem("Plano Prata")));
        mockMvc.perform(delete("/adm/listar/produtos/excluir/" + saved.getId()).cookie(admin))
                .andExpect(status().isOk());

        mockMvc.perform(get("/adm/consultas").cookie(admin)).andExpect(status().isOk());
        mockMvc.perform(get("/adm/listar/clientes").cookie(admin)).andExpect(status().isOk());
        mockMvc.perform(multipart("/adm/upload").cookie(admin))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("Nenhum arquivo enviado"));
    }

    @Test
    void passwordResetCodeUnlocksANewPassword() throws Exception {
        String email = signUp();

        mockMvc.perform(postJson("/user/login/esqueci-a-senha", Map.of("email", email)))
                .andExpect(status().isOk());
        mockMvc.perform(postJson("/user/login/esqueci-a-senha", Map.of("email", email)))
                .andExpect(status().isTooManyRequests());

        ArgumentCaptor<MimeMessage> sent = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mailSender, atLeastOnce()).send(sent.capture());
        Matcher code = Pattern.compile("<h3>(\\d{6})</h3>").matcher(sent.getValue().getContent().toString());
        assertThat(code.find()).isTrue();

        mockMvc.perform(postJson("/user/login/esqueci-a-senha-confirmar", Map.of("email", email, "codigo", "000000x")))
                .andExpect(status().isNotFound());
        MvcResult confirmed = mockMvc.perform(postJson("/user/login/esqueci-a-senha-confirmar",
                        Map.of("email", email, "codigo", code.group(1))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token_reset").exists())
                .andReturn();
        Cookie resetCookie = cookieOf(confirmed);
        assertThat(resetCookie.getName()).isEqualTo("token_reset");

        Map<String, Object> passwords = Map.of("senha1", "new-password", "senha2", "new-password");
        mockMvc.perform(putJson("/user/login/alterar-senha", passwords).cookie(login("/user/login", email)))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(putJson("/user/login/alterar-senha", passwords).cookie(resetCookie))
                .andExpect(status().isOk());

        mockMvc.perform(postJson("/user/login", Map.of("email", email, "senha", PASSWORD)))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(postJson("/user/login", Map.of("email", email, "senha", "new-password")))
                .andExpect(status().isOk());
    }

    @Test
    void eachPetHasItsOwnPlan() throws Exception {
        Product gold = new Product();
        gold.setName("Plano Ouro");
        gold.setDescription("Plano completo");
        gold.setBenefits("Consultas ilimitadas");
        gold.setPrice(new BigDecimal("49.90"));
        productRepository.save(gold);
        Product bronze = new Product();
        bronze.setName("Plano Bronze");
        bronze.setDescription("Plano inicial");
        bronze.setBenefits("Banho e Tosa: 1x/mês");
        bronze.setPrice(new BigDecimal("19.90"));
        productRepository.save(bronze);
        Cookie user = loginNewUser();
        int rex = createPet(user);
        int luna = createPet(user);
        int someoneElsesPet = createPet(loginNewUser());

        mockMvc.perform(get("/user/planos").cookie(user))
                .andExpect(jsonPath("$.mensagem").value("Você ainda não possui nenhum plano ativo"));
        mockMvc.perform(postJson("/user/planos/pagamento", Map.of("id_produto", gold.getId())).cookie(user))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("Escolha o pet que vai receber o plano"));
        mockMvc.perform(postJson("/user/planos/pagamento",
                        Map.of("id_produto", gold.getId(), "id_pet", someoneElsesPet)).cookie(user))
                .andExpect(status().isNotFound());
        mockMvc.perform(postJson("/user/planos/pagamento", Map.of("id_produto", gold.getId(), "id_pet", rex)).cookie(user))
                .andExpect(status().isOk());
        mockMvc.perform(postJson("/user/planos/pagamento", Map.of("id_produto", bronze.getId(), "id_pet", rex)).cookie(user))
                .andExpect(status().isBadRequest());
        mockMvc.perform(postJson("/user/planos/pagamento", Map.of("id_produto", bronze.getId(), "id_pet", luna)).cookie(user))
                .andExpect(status().isOk());

        mockMvc.perform(get("/user/planos").cookie(user))
                .andExpect(jsonPath("$.tem_plano").value(true))
                .andExpect(jsonPath("$.planos", hasSize(2)))
                .andExpect(jsonPath("$.planos[?(@.id_pet == " + rex + ")].produto.nome", hasItem("Plano Ouro")))
                .andExpect(jsonPath("$.planos[?(@.id_pet == " + luna + ")].produto.nome", hasItem("Plano Bronze")));
        mockMvc.perform(get("/user/planos?id_pet=" + luna).cookie(user))
                .andExpect(jsonPath("$.include.preco").value("19.9"));
        mockMvc.perform(delete("/adm/listar/produtos/excluir/" + gold.getId()).cookie(loginNewAdmin()))
                .andExpect(status().isConflict());
        mockMvc.perform(delete("/user/planos/cancelar").cookie(user)).andExpect(status().isBadRequest());
        mockMvc.perform(delete("/user/planos/cancelar/" + rex).cookie(user)).andExpect(status().isOk());
        mockMvc.perform(delete("/user/planos/cancelar/" + rex).cookie(user)).andExpect(status().isNotFound());
        mockMvc.perform(delete("/user/listar/pet/delete/" + luna).cookie(user)).andExpect(status().isOk());
        mockMvc.perform(get("/user/planos").cookie(user))
                .andExpect(jsonPath("$.planos", hasSize(0)));
    }

    @Test
    void availabilityMarksATimeUnavailableOnlyWhenEveryProfessionalIsBusy() throws Exception {
        deactivateAllEmployees();
        createEmployee(EmployeeSpecialty.VETERINARIAN);
        createEmployee(EmployeeSpecialty.VETERINARIAN);
        createEmployee(EmployeeSpecialty.GROOMER);
        Cookie user = loginNewUser();
        int firstPet = createPet(user);
        Cookie otherUser = loginNewUser();
        int secondPet = createPet(otherUser);
        String date = "2026-07-10";
        String path = "/agendamento/horarios?servico=veterinario&data=" + date;

        mockMvc.perform(get(path).cookie(user))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profissionais").value(2))
                .andExpect(jsonPath("$.indisponiveis", hasSize(0)));

        mockMvc.perform(postJson("/agendamento", appointmentBody("veterinario", firstPet, date, "08:00")).cookie(user))
                .andExpect(status().isCreated());
        mockMvc.perform(get(path).cookie(user)).andExpect(jsonPath("$.indisponiveis", hasSize(0)));

        mockMvc.perform(postJson("/agendamento", appointmentBody("veterinario", secondPet, date, "08:00"))
                        .cookie(otherUser))
                .andExpect(status().isCreated());
        mockMvc.perform(get(path).cookie(user))
                .andExpect(jsonPath("$.indisponiveis", hasSize(3)))
                .andExpect(jsonPath("$.indisponiveis[0]").value("07:30"))
                .andExpect(jsonPath("$.indisponiveis[1]").value("08:00"))
                .andExpect(jsonPath("$.indisponiveis[2]").value("08:30"));
        mockMvc.perform(get("/adm/agendamento/horarios?servico=veterinario&data=" + date).cookie(loginNewAdmin()))
                .andExpect(jsonPath("$.indisponiveis[1]").value("08:00"));
        int thirdPet = createPet(loginNewUser());
        Cookie admin = loginNewAdmin();
        mockMvc.perform(postJson("/adm/agendamento", appointmentBody("veterinario", thirdPet, date, "08:30")).cookie(admin))
                .andExpect(status().isConflict());
        mockMvc.perform(postJson("/adm/agendamento", appointmentBody("veterinario", thirdPet, date, "09:00")).cookie(admin))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/agendamento/horarios?servico=tosador&data=" + date).cookie(user))
                .andExpect(jsonPath("$.servico").value("tosador"))
                .andExpect(jsonPath("$.profissionais").value(1))
                .andExpect(jsonPath("$.indisponiveis", hasSize(0)));
        mockMvc.perform(get("/agendamento/horarios?servico=financeiro&data=" + date).cookie(user))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get(path)).andExpect(status().isUnauthorized());
        deactivateAllEmployees();
    }

    private void subscribeNewPet(Product plan) throws Exception {
        Cookie user = loginNewUser();
        int petId = createPet(user);
        mockMvc.perform(postJson("/user/planos/pagamento", Map.of("id_produto", plan.getId(), "id_pet", petId)).cookie(user))
                .andExpect(status().isOk());
    }

    private void deactivateAllEmployees() {
        employeeRepository.findAll().forEach(employee -> {
            employee.setActive(false);
            employeeRepository.save(employee);
        });
    }

    @Test
    void employeeSeesThePetPlanAndSchedulesForTheClient() throws Exception {
        deactivateAllEmployees();
        Employee veterinarian = createEmployee(EmployeeSpecialty.VETERINARIAN);
        Product plan = new Product();
        plan.setName("Plano Ouro | OURO | 349.90");
        plan.setDescription("Plano completo");
        plan.setBenefits("Banho e Tosa: 2x/mês");
        plan.setPrice(new BigDecimal("399.90"));
        productRepository.save(plan);
        Cookie owner = loginNewUser();
        int petId = createPet(owner);
        int otherPet = createPet(owner);
        mockMvc.perform(postJson("/user/planos/pagamento", Map.of("id_produto", plan.getId(), "id_pet", petId)).cookie(owner))
                .andExpect(status().isOk());
        Cookie employee = login("/user/login/funcionario", veterinarian.getEmail());
        String petPath = "$.pets[?(@.id_pet == " + petId + ")]";

        mockMvc.perform(get("/funcionario/pets").cookie(employee))
                .andExpect(jsonPath(petPath + ".plano", hasItem("Plano Ouro | OURO | 349.90")))
                .andExpect(jsonPath(petPath + ".consultasDoMes[*]", hasSize(0)));
        mockMvc.perform(get("/funcionario/pets").cookie(employee))
                .andExpect(jsonPath("$.pets[?(@.id_pet == " + otherPet + ")].plano", hasItem(nullValue())));
        mockMvc.perform(get("/funcionario/clientes").cookie(employee))
                .andExpect(jsonPath("$.clientes[*].petsDetalhesLista[?(@.id_pet == " + petId + ")].plano",
                        hasItem("Plano Ouro | OURO | 349.90")));

        mockMvc.perform(get("/adm/listar/clientes").cookie(loginNewAdmin()))
                .andExpect(jsonPath("$.clientes[?(@.id_pet == " + petId + ")].plano",
                        hasItem("Plano Ouro | OURO | 349.90")));

        String date = TODAY.toString();
        Map<String, Object> body = new LinkedHashMap<>(appointmentBody("veterinario", petId, date, "18:00"));
        body.put("tipo_servico", "consulta");
        body.put("observacoes", "Serviço: Consulta.");
        mockMvc.perform(postJson("/funcionario/agendamento", body).cookie(employee))
                .andExpect(status().isCreated());
        mockMvc.perform(get("/funcionario/agendamento/horarios?servico=veterinario&data=" + date).cookie(employee))
                .andExpect(jsonPath("$.indisponiveis", hasItem("18:00")));
        mockMvc.perform(get("/funcionario/pets").cookie(employee))
                .andExpect(jsonPath(petPath + ".consultasDoMes[*].servico", hasItem("veterinario")))
                .andExpect(jsonPath(petPath + ".consultasDoMes[*].observacoes", hasItem("Serviço: Consulta.")));
        mockMvc.perform(postJson("/funcionario/agendamento", body).cookie(owner))
                .andExpect(status().isUnauthorized());
        deactivateAllEmployees();
    }

    @Test
    void adminCancelsAllSubscriptionsSoThePlanCanBeDeleted() throws Exception {
        Product plan = new Product();
        plan.setName("Plano Prata");
        plan.setDescription("Plano intermediario");
        plan.setBenefits("Banho e Tosa: 1x/mês");
        plan.setPrice(new BigDecimal("219.90"));
        productRepository.save(plan);
        Cookie admin = loginNewAdmin();
        mockMvc.perform(delete("/adm/listar/produtos/assinaturas/cancelar").cookie(admin)).andExpect(status().isOk());
        subscribeNewPet(plan);
        subscribeNewPet(plan);

        mockMvc.perform(delete("/adm/listar/produtos/assinaturas/cancelar").cookie(loginNewUser()))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(delete("/adm/listar/produtos/excluir/" + plan.getId()).cookie(admin))
                .andExpect(status().isConflict());
        mockMvc.perform(delete("/adm/listar/produtos/assinaturas/cancelar").cookie(admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.canceladas").value(2))
                .andExpect(jsonPath("$.mensagem").value("2 planos contratados foram cancelados"));
        mockMvc.perform(delete("/adm/listar/produtos/assinaturas/cancelar").cookie(admin))
                .andExpect(jsonPath("$.canceladas").value(0));
        mockMvc.perform(delete("/adm/listar/produtos/excluir/" + plan.getId()).cookie(admin))
                .andExpect(status().isOk());
    }

    @Test
    void userUpdatesTheProfileData() throws Exception {
        String email = signUp();
        Cookie user = login("/user/login", email);

        Map<String, Object> changes = new LinkedHashMap<>();
        changes.put("nome", "Kaio Eduardo");
        changes.put("sobrenome", "Alves");
        changes.put("email", email);
        changes.put("telefone", "(61) 99876-5432");
        changes.put("data_nascimento", "1999-03-04");
        mockMvc.perform(putJson("/user/perfil-dados/atualizar", changes).cookie(user))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensagem").value("Dados atualizados com sucesso"))
                .andExpect(jsonPath("$.perfil.telefone").value("61998765432"));

        mockMvc.perform(get("/user/buscar/perfil").cookie(user))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Kaio Eduardo"))
                .andExpect(jsonPath("$.sobrenome").value("Alves"))
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.telefone").value("61998765432"))
                .andExpect(jsonPath("$.data_nascimento").value("1999-03-04T00:00:00.000Z"));

        Map<String, Object> keepOptional = new LinkedHashMap<>();
        keepOptional.put("nome", "Kaio");
        keepOptional.put("sobrenome", "Alves");
        keepOptional.put("telefone", null);
        keepOptional.put("data_nascimento", null);
        mockMvc.perform(putJson("/user/perfil-dados/atualizar", keepOptional).cookie(user))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.perfil.telefone").value("61998765432"))
                .andExpect(jsonPath("$.perfil.data_nascimento").value("1999-03-04T00:00:00.000Z"));

        Map<String, Object> takenPhone = new LinkedHashMap<>(keepOptional);
        takenPhone.put("telefone", "61998765432");
        mockMvc.perform(putJson("/user/perfil-dados/atualizar", takenPhone).cookie(loginNewUser()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem").value("Número de telefone já cadastrado"));

        Map<String, Object> future = new LinkedHashMap<>(keepOptional);
        future.put("data_nascimento", TODAY.plusDays(1).toString());
        mockMvc.perform(putJson("/user/perfil-dados/atualizar", future).cookie(user))
                .andExpect(status().isBadRequest());

        Map<String, Object> shortName = new LinkedHashMap<>(keepOptional);
        shortName.put("nome", "K");
        mockMvc.perform(putJson("/user/perfil-dados/atualizar", shortName).cookie(user))
                .andExpect(status().isBadRequest());

        mockMvc.perform(putJson("/user/perfil-dados/atualizar", keepOptional))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void mobileClientAuthenticatesWithTheBearerToken() throws Exception {
        String email = signUp();
        Map<String, Object> credentials = Map.of("email", email, "senha", PASSWORD);

        mockMvc.perform(postJson("/user/login", credentials))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").doesNotExist());
        MvcResult login = mockMvc.perform(postJson("/user/login", credentials)
                        .header(AuthController.CLIENT_TYPE_HEADER, "mobile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn();
        String token = objectMapper.readTree(login.getResponse().getContentAsString()).get("token").asText();

        mockMvc.perform(get("/user/buscar/perfil").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email));
        mockMvc.perform(get("/user/buscar/perfil").header(HttpHeaders.AUTHORIZATION, "Bearer invalid"))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/adm").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/user/logout").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void webClientCreatesPetsWithTheBearerTokenWhenCookiesAreBlocked() throws Exception {
        String email = signUp();
        MvcResult login = mockMvc.perform(postJson("/user/login", Map.of("email", email, "senha", PASSWORD))
                        .header(AuthController.CLIENT_TYPE_HEADER, "web"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn();
        String bearer = "Bearer " + objectMapper.readTree(login.getResponse().getContentAsString()).get("token").asText();

        mockMvc.perform(multipart("/user/listar/pet/cadastar")
                        .param("nome", "Thor")
                        .param("especie", "gato")
                        .param("sexo", "M")
                        .param("data_nascimento", "2021-03-10")
                        .param("peso", "4")
                        .header(HttpHeaders.AUTHORIZATION, bearer))
                .andExpect(status().isCreated());
        mockMvc.perform(get("/user/listar/pet").header(HttpHeaders.AUTHORIZATION, bearer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pets[0].nome").value("Thor"));
        mockMvc.perform(get("/adm/listar/produtos").header(HttpHeaders.AUTHORIZATION, bearer))
                .andExpect(status().isOk());
    }

    @Test
    void adminSchedulesUpdatesAndCancelsAnyAppointment() throws Exception {
        Employee groomer = createEmployee(EmployeeSpecialty.GROOMER);
        groomer.setSpecialty(EmployeeSpecialty.RECEPTIONIST);
        employeeRepository.save(groomer);
        Employee veterinarian = createEmployee(EmployeeSpecialty.VETERINARIAN);
        int petId = createPet(loginNewUser());
        Cookie admin = loginNewAdmin();

        mockMvc.perform(postJson("/adm/agendamento", appointmentBody("veterinario", petId, TODAY.toString(), "23:00"))
                        .cookie(admin))
                .andExpect(status().isCreated());
        MvcResult list = mockMvc.perform(get("/adm/consultas").cookie(admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.consultas[*].pet.usuario.nome", hasItem("Kaio")))
                .andExpect(jsonPath("$.consultas[*].funcionario.sobrenome", hasItem("Lima")))
                .andReturn();
        int appointmentId = objectMapper.readTree(list.getResponse().getContentAsString())
                .at("/consultas").findValuesAsText("id_consulta").stream()
                .mapToInt(Integer::parseInt).max().orElseThrow();
        assertThat(veterinarian.getId()).isNotNull();

        mockMvc.perform(putJson("/adm/consultas/atualizar/" + appointmentId, Map.of("novoStatus", "em_andamento"))
                        .cookie(loginNewUser()))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(putJson("/adm/consultas/atualizar/" + appointmentId, Map.of("novoStatus", "em_andamento"))
                        .cookie(admin))
                .andExpect(status().isOk());
        assertThat(appointmentRepository.findById(appointmentId).orElseThrow().getStatus())
                .isEqualTo(AppointmentStatus.IN_PROGRESS);

        mockMvc.perform(delete("/adm/listar/clientes/excluir/" + petId).cookie(admin))
                .andExpect(status().isConflict());
        mockMvc.perform(delete("/adm/consultas/cancelar/" + appointmentId).cookie(admin))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/adm/listar/clientes/excluir/" + petId).cookie(admin))
                .andExpect(status().isOk());
        assertThat(petRepository.findById(petId)).isEmpty();
        mockMvc.perform(post("/adm/logout").cookie(admin)).andExpect(status().isOk());
    }

    @Test
    void adminSeesAppointmentsOfEveryDay() throws Exception {
        createEmployee(EmployeeSpecialty.VETERINARIAN);
        int petId = createPet(loginNewUser());
        Cookie admin = loginNewAdmin();
        String laterDate = TODAY.plusDays(3).toString();

        mockMvc.perform(postJson("/adm/agendamento", appointmentBody("veterinario", petId, laterDate, "10:00"))
                        .cookie(admin))
                .andExpect(status().isCreated());
        mockMvc.perform(get("/adm/consultas").cookie(admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.consultas[*].data_consulta", hasItem(laterDate + "T00:00:00.000Z")));
    }

    private Cookie loginNewAdmin() throws Exception {
        Admin admin = new Admin();
        admin.setUsername("admin" + SEQUENCE.incrementAndGet());
        admin.setEmail(admin.getUsername() + "@petvitaliz.test");
        admin.setPassword(passwordEncoder.encode(PASSWORD));
        adminRepository.save(admin);
        return login("/user/login/adm", admin.getEmail());
    }

    private Employee createEmployee(EmployeeSpecialty specialty) {
        Employee employee = new Employee();
        employee.setFirstName("Ana");
        employee.setLastName("Lima");
        employee.setEmail("ana" + SEQUENCE.incrementAndGet() + CORPORATE_DOMAIN);
        employee.setSpecialty(specialty);
        employee.setPassword(passwordEncoder.encode(PASSWORD));
        return employeeRepository.save(employee);
    }

    private int workloadOf(Employee employee) {
        return employeeRepository.findById(employee.getId()).orElseThrow().getWorkload();
    }

    private String signUp() throws Exception {
        String email = "kaio" + SEQUENCE.incrementAndGet() + "@petvitaliz.test";
        mockMvc.perform(postJson("/user/cadastro", signupBody(email))).andExpect(status().isCreated());
        return email;
    }

    private Map<String, Object> signupBody(String email) {
        int unique = SEQUENCE.incrementAndGet();
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("nome", "Kaio");
        body.put("sobrenome", "Sousa");
        body.put("email", email);
        body.put("senha", PASSWORD);
        body.put("CPF", String.format("%011d", unique));
        body.put("telefone", String.format("11%09d", unique));
        body.put("genero", "m");
        body.put("data_nascimento", "2000-05-20");
        return body;
    }

    private Cookie loginNewUser() throws Exception {
        return login("/user/login", signUp());
    }

    private Cookie login(String path, String email) throws Exception {
        return cookieOf(mockMvc.perform(postJson(path, Map.of("email", email, "senha", PASSWORD)))
                .andExpect(status().isOk())
                .andReturn());
    }

    private static Cookie cookieOf(MvcResult result) {
        String header = result.getResponse().getHeader(HttpHeaders.SET_COOKIE);
        String pair = header.substring(0, header.indexOf(';'));
        int separator = pair.indexOf('=');
        return new Cookie(pair.substring(0, separator), pair.substring(separator + 1));
    }

    private int createPet(Cookie user) throws Exception {
        Map<String, Object> pet = new LinkedHashMap<>();
        pet.put("nome", "Rex");
        pet.put("especie", "cachorro");
        pet.put("sexo", "M");
        pet.put("data_nascimento", "2020-01-10");
        pet.put("idade", 6);
        pet.put("peso", 12.5);
        mockMvc.perform(postJson("/user/listar/pet/cadastar", pet).cookie(user)).andExpect(status().isCreated());

        MvcResult list = mockMvc.perform(get("/user/listar/pet").cookie(user)).andReturn();
        int newest = 0;
        for (var item : objectMapper.readTree(list.getResponse().getContentAsString()).at("/pets")) {
            newest = Math.max(newest, item.get("id_pet").asInt());
        }
        return newest;
    }

    private static Map<String, Object> appointmentBody(String service, int petId, String date, String startTime) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("servico", service);
        body.put("id_pet", petId);
        body.put("data_consulta", date);
        body.put("hora_inicio", startTime);
        return body;
    }

    private MockHttpServletRequestBuilder postJson(String path, Object body) throws Exception {
        return post(path).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(body));
    }

    private MockHttpServletRequestBuilder putJson(String path, Object body) throws Exception {
        return put(path).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(body));
    }
}
