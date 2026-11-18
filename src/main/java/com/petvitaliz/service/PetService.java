package com.petvitaliz.service;

import com.petvitaliz.dto.PetForm;
import com.petvitaliz.dto.PetResponse;
import com.petvitaliz.entity.Pet;
import com.petvitaliz.enums.PetSpecies;
import com.petvitaliz.exception.ApiException;
import com.petvitaliz.repository.PetRepository;
import com.petvitaliz.repository.SubscriptionRepository;
import com.petvitaliz.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PetService {

    private final PetRepository petRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;
    private final Clock clock;

    @Transactional(readOnly = true)
    public List<PetResponse> list(Integer ownerId) {
        return petRepository.findByOwnerId(ownerId).stream().map(PetResponse::of).toList();
    }

    public void create(Integer ownerId, PetForm form, MultipartFile image) {
        Pet pet = new Pet();
        pet.setOwner(userRepository.getReferenceById(ownerId));
        applyCommonFields(pet, form);
        pet.setWeight(PetFormParser.requiredWeight(form.weight()));
        pet.setAge(PetFormParser.ageOrZero(form.age()));

        cloudinaryService.validate(image);
        pet.setPhotoUrl(cloudinaryService.uploadOrKeep(image, CloudinaryService.DEFAULT_FOLDER, null));
        petRepository.save(pet);
    }

    public PetResponse update(Integer ownerId, Integer petId, PetForm form, MultipartFile image) {
        Pet pet = findOwned(ownerId, petId);
        applyCommonFields(pet, form);
        pet.setWeight(PetFormParser.optionalWeight(form.weight()));
        if (!PetFormParser.isBlank(form.age())) {
            pet.setAge(PetFormParser.ageOrZero(form.age()));
        }

        cloudinaryService.validate(image);
        pet.setPhotoUrl(cloudinaryService.uploadOrKeep(image, CloudinaryService.DEFAULT_FOLDER, pet.getPhotoUrl()));
        return PetResponse.of(petRepository.save(pet));
    }

    @Transactional
    public void delete(Integer ownerId, Integer petId) {
        remove(findOwned(ownerId, petId));
    }

    @Transactional
    public void deleteAsAdmin(Integer petId) {
        remove(petRepository.findById(petId).orElseThrow(() -> ApiException.notFound("Pet não encontrado")));
    }

    private void remove(Pet pet) {
        PersistenceGuard.deleteOrConflict(() -> {
            subscriptionRepository.deleteByPetId(pet.getId());
            petRepository.delete(pet);
            petRepository.flush();
        }, "Este pet possui consultas e não pode ser excluído");
    }

    private void applyCommonFields(Pet pet, PetForm form) {
        pet.setName(PetFormParser.name(form.name()));
        PetSpecies species = PetFormParser.species(form.species());
        pet.setSpecies(species);
        pet.setOtherSpecies(PetFormParser.otherSpecies(species, form.otherSpecies()));
        pet.setSex(PetFormParser.sex(form.sex()));
        pet.setBirthDate(PetFormParser.birthDate(form.birthDate(), LocalDate.now(clock)));
        pet.setNotes(PetFormParser.notes(form.notes()));
    }

    private Pet findOwned(Integer ownerId, Integer petId) {
        PersistenceGuard.requirePositiveId(petId, ApiException.badRequest("Id inválido"));
        return petRepository.findByIdAndOwnerId(petId, ownerId)
                .orElseThrow(() -> ApiException.notFound("Pet não encontrado"));
    }
}
