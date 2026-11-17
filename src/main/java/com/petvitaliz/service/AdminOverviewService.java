package com.petvitaliz.service;

import com.petvitaliz.dto.AdminClientResponse;
import com.petvitaliz.dto.AppointmentResponse;
import com.petvitaliz.repository.AppointmentRepository;
import com.petvitaliz.repository.PetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminOverviewService {

    private final AppointmentRepository appointmentRepository;
    private final PetRepository petRepository;
    private final Clock clock;

    @Transactional(readOnly = true)
    public List<AppointmentResponse> listTodayAppointments() {
        return appointmentRepository.findAllByDate(LocalDate.now(clock)).stream()
                .map(AppointmentResponse::forAdmin)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AdminClientResponse> listClients() {
        return petRepository.findAllWithOwner().stream().map(AdminClientResponse::of).toList();
    }
}
