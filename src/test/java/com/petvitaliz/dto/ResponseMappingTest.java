package com.petvitaliz.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petvitaliz.entity.Appointment;
import com.petvitaliz.entity.Employee;
import com.petvitaliz.entity.Pet;
import com.petvitaliz.entity.Product;
import com.petvitaliz.entity.User;
import com.petvitaliz.enums.AppointmentStatus;
import com.petvitaliz.enums.EmployeeSpecialty;
import com.petvitaliz.enums.PetSex;
import com.petvitaliz.enums.PetSpecies;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ResponseMappingTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 6, 15);

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void productPriceIsTextWithoutTrailingZeros() {
        assertThat(productWithPrice("99.90").price()).isEqualTo("99.9");
        assertThat(productWithPrice("149.99").price()).isEqualTo("149.99");
        assertThat(productWithPrice("100.00").price()).isEqualTo("100");
    }

    @Test
    void catalogProductHidesTheIdAndAdminProductShowsIt() {
        Product product = new Product();
        product.setId(4);
        product.setName("Plano Teste");
        product.setPrice(new BigDecimal("10"));

        assertThat(mapper.valueToTree(ProductResponse.of(product)).has("id_produto")).isFalse();
        assertThat(mapper.valueToTree(ProductResponse.withId(product)).get("id_produto").asInt()).isEqualTo(4);
    }

    @Test
    void ownerAppointmentKeepsTheLegacyJsonShape() {
        JsonNode json = mapper.valueToTree(AppointmentResponse.forOwner(appointment()));

        assertThat(json.get("id_consulta").asInt()).isEqualTo(9);
        assertThat(json.get("data_consulta").asText()).isEqualTo("2026-06-15T00:00:00.000Z");
        assertThat(json.get("hora_inicio").asText()).isEqualTo("09:00");
        assertThat(json.get("status").asText()).isEqualTo("em_espera");
        assertThat(json.get("pet").get("nome").asText()).isEqualTo("Rex");
        assertThat(json.get("pet").has("idade")).isFalse();
        assertThat(json.get("funcionario").get("especialidade").asText()).isEqualTo("veterinario");
        assertThat(json.get("funcionario").has("carga")).isFalse();
    }

    @Test
    void employeeDetailsIncludeTheOwnerContact() {
        JsonNode pet = mapper.valueToTree(AppointmentResponse.forEmployeeDetails(appointment())).get("pet");

        assertThat(pet.get("especie").asText()).isEqualTo("cachorro");
        assertThat(pet.get("sexo").asText()).isEqualTo("M");
        assertThat(pet.get("usuario").get("telefone").asText()).isEqualTo("11999998888");
    }

    @Test
    void clientCpfIsMasked() {
        assertThat(ClientResponse.maskCpf("12345678901")).isEqualTo("123.456.789-01");
        assertThat(ClientResponse.maskCpf(null)).isEqualTo("Não informado");
    }

    @Test
    void patientAgeTextFollowsTheBirthDate() {
        assertThat(PatientResponse.ageText(0, TODAY.minusMonths(3), TODAY)).isEqualTo("Menos de 1 ano");
        assertThat(PatientResponse.ageText(1, TODAY.minusMonths(7), TODAY)).isEqualTo("7 meses");
        assertThat(PatientResponse.ageText(2, TODAY.minusYears(2).minusMonths(4), TODAY)).isEqualTo("2 anos e 4 m");
        assertThat(PatientResponse.ageText(3, TODAY.minusYears(3), TODAY)).isEqualTo("3 anos");
    }

    @Test
    void patientShowsFriendlyLabels() {
        PatientResponse patient = PatientResponse.of(pet(), TODAY, null, List.of());

        assertThat(patient.species()).isEqualTo("Cão");
        assertThat(patient.sex()).isEqualTo("Macho");
        assertThat(patient.weight()).isEqualTo("12.5 kg");
        assertThat(patient.ownerName()).isEqualTo("Kaio Sousa");
    }

    private static ProductResponse productWithPrice(String price) {
        Product product = new Product();
        product.setName("Plano Teste");
        product.setDescription("Descricao");
        product.setPrice(new BigDecimal(price));
        return ProductResponse.of(product);
    }

    private static Pet pet() {
        User owner = new User();
        owner.setId(1);
        owner.setFirstName("Kaio");
        owner.setLastName("Sousa");
        owner.setEmail("kaio@test.com");
        owner.setPhone("11999998888");

        Pet pet = new Pet();
        pet.setId(5);
        pet.setOwner(owner);
        pet.setOwnerId(1);
        pet.setName("Rex");
        pet.setSpecies(PetSpecies.DOG);
        pet.setSex(PetSex.M);
        pet.setBirthDate(TODAY.minusYears(4));
        pet.setAge(4);
        pet.setWeight(12.5);
        return pet;
    }

    private static Appointment appointment() {
        Employee employee = new Employee();
        employee.setId(3);
        employee.setFirstName("Ana");
        employee.setSpecialty(EmployeeSpecialty.VETERINARIAN);

        Appointment appointment = new Appointment();
        appointment.setId(9);
        appointment.setPet(pet());
        appointment.setPetId(5);
        appointment.setEmployee(employee);
        appointment.setEmployeeId(3);
        appointment.setAppointmentDate(TODAY);
        appointment.setStartTime("09:00");
        appointment.setEndTime("10:00");
        appointment.setStatus(AppointmentStatus.WAITING);
        return appointment;
    }
}
