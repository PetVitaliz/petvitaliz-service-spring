package com.petvitaliz.service;

import com.petvitaliz.dto.AdminClientResponse;
import com.petvitaliz.dto.AppointmentResponse;
import com.petvitaliz.repository.AppointmentRepository;
import com.petvitaliz.repository.PetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminOverviewService {

    private final AppointmentRepository appointmentRepository;
    private final PetRepository petRepository;
    private final SubscriptionService subscriptionService;

    @Transactional(readOnly = true)
    public List<AppointmentResponse> listAppointments() {
        return appointmentRepository.findAllWithPetOwnerAndEmployee().stream()
                .map(AppointmentResponse::forAdmin)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AdminClientResponse> listClients() {
        Map<Integer, String> plans = subscriptionService.planNamesByPet();
        return petRepository.findAllWithOwner().stream()
                .map(pet -> AdminClientResponse.of(pet, plans.get(pet.getId())))
                .toList();
    }
}
