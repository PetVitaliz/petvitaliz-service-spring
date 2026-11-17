package com.petvitaliz.service;

import com.petvitaliz.dto.EmployeeForm;
import com.petvitaliz.dto.EmployeeResponse;
import com.petvitaliz.entity.Employee;
import com.petvitaliz.enums.DbEnum;
import com.petvitaliz.enums.EmployeeSpecialty;
import com.petvitaliz.exception.ApiException;
import com.petvitaliz.repository.EmployeeRepository;
import com.petvitaliz.util.Texts;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminEmployeeService {

    private static final int MIN_NAME_LENGTH = 2;
    private static final int MIN_PASSWORD_LENGTH = 6;

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final CloudinaryService cloudinaryService;

    public void create(EmployeeForm form, MultipartFile image) {
        Employee employee = new Employee();
        String email = Texts.normalizeEmail(form.email());
        applyIdentity(employee, form, email,
                "O e-mail é obrigatório e deve ser corporativo (@petvitalizfuncionario.com)");

        if (form.password() == null || form.password().length() < MIN_PASSWORD_LENGTH) {
            throw invalid("Senha é obrigatória e deve ter pelo menos 6 caracteres");
        }
        if (employeeRepository.existsByEmail(email)) {
            throw invalid("E-mail já cadastrado para outro funcionário");
        }
        cloudinaryService.validate(image);

        employee.setPassword(passwordEncoder.encode(form.password()));
        employee.setWorkload(Employee.MAX_WORKLOAD);
        employee.setPhotoUrl(cloudinaryService.uploadOrKeep(image, CloudinaryService.EMPLOYEE_FOLDER, null));
        employeeRepository.save(employee);
    }

    @Transactional(readOnly = true)
    public List<EmployeeResponse> list() {
        return employeeRepository.findAllByOrderByIdDesc().stream().map(EmployeeResponse::of).toList();
    }

    @Transactional(readOnly = true)
    public EmployeeResponse get(Integer id) {
        return EmployeeResponse.of(find(id, "Id não encontrado"));
    }

    public EmployeeResponse update(Integer id, EmployeeForm form, MultipartFile image) {
        Employee employee = find(id, "Funcionário não encontrado");
        String email = Texts.normalizeEmail(form.email());
        applyIdentity(employee, form, email, EmployeeRules.CORPORATE_EMAIL_REQUIRED);

        if (employeeRepository.existsByEmailAndIdNot(email, id)) {
            throw invalid(EmployeeRules.EMAIL_IN_USE);
        }
        cloudinaryService.validate(image);

        String newPassword = Texts.trimToEmpty(form.password());
        if (newPassword.length() >= MIN_PASSWORD_LENGTH) {
            employee.setPassword(passwordEncoder.encode(newPassword));
        }
        employee.setPhotoUrl(cloudinaryService.uploadOrKeep(
                image, CloudinaryService.EMPLOYEE_FOLDER, employee.getPhotoUrl()));
        return EmployeeResponse.of(employeeRepository.save(employee));
    }

    @Transactional
    public List<EmployeeResponse> delete(Integer id) {
        Employee employee = find(id, "Id não encontrado");
        PersistenceGuard.deleteOrConflict(() -> {
            employeeRepository.delete(employee);
            employeeRepository.flush();
        }, "Este funcionário possui consultas e não pode ser excluído");
        return list();
    }

    private void applyIdentity(Employee employee, EmployeeForm form, String email, String emailMessage) {
        String firstName = Texts.trimToEmpty(form.firstName());
        String lastName = Texts.trimToEmpty(form.lastName());

        if (firstName.length() < MIN_NAME_LENGTH) {
            throw invalid("Nome é obrigatório e deve ter pelo menos 2 caracteres");
        }
        if (lastName.length() < MIN_NAME_LENGTH) {
            throw invalid("Sobrenome é obrigatório e deve ter pelo menos 2 caracteres");
        }
        EmployeeRules.requireCorporateEmail(email, invalid(emailMessage));
        EmployeeSpecialty specialty = DbEnum.find(EmployeeSpecialty.class, form.specialty()).orElseThrow(() ->
                invalid("Especialidade inválida. Escolha 'veterinario', 'tosador', 'recepcionista' ou 'financeiro' "));

        employee.setFirstName(firstName);
        employee.setLastName(lastName);
        employee.setEmail(email);
        employee.setSpecialty(specialty);
        employee.setActive(Texts.isTrue(form.active()));
    }

    private Employee find(Integer id, String notFoundMessage) {
        return employeeRepository.findById(id).orElseThrow(() -> ApiException.notFound(notFoundMessage));
    }

    private static ApiException invalid(String message) {
        return ApiException.plain(HttpStatus.BAD_REQUEST, message);
    }
}
