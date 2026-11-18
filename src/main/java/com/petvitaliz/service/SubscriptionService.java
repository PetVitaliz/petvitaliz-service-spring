package com.petvitaliz.service;

import com.petvitaliz.dto.PetPlanResponse;
import com.petvitaliz.entity.Pet;
import com.petvitaliz.entity.Product;
import com.petvitaliz.entity.Subscription;
import com.petvitaliz.exception.ApiException;
import com.petvitaliz.repository.PetRepository;
import com.petvitaliz.repository.ProductRepository;
import com.petvitaliz.repository.SubscriptionRepository;
import com.petvitaliz.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final ProductRepository productRepository;
    private final PetRepository petRepository;
    private final EmailService emailService;

    @Transactional(readOnly = true)
    public Map<Integer, String> planNamesByPet() {
        return subscriptionRepository.findAllWithProduct().stream()
                .collect(Collectors.toMap(Subscription::getPetId,
                        subscription -> subscription.getProduct().getName(),
                        (first, second) -> first));
    }

    @Transactional(readOnly = true)
    public List<PetPlanResponse> listPlans(Integer userId) {
        return subscriptionRepository.findAllByUserIdWithProduct(userId).stream()
                .map(PetPlanResponse::of)
                .toList();
    }

    @Transactional
    public void subscribe(AuthenticatedUser user, Integer productId, Integer petId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> ApiException.notFound("Id não encontrado"));
        Pet pet = petRepository.findByIdAndOwnerId(petId, user.id())
                .orElseThrow(() -> ApiException.notFound("Pet não encontrado"));

        if (subscriptionRepository.findByPetId(pet.getId()).isPresent()) {
            throw ApiException.badRequest("Este pet já possui um plano, cancele o plano atual para assinar outro");
        }

        Subscription subscription = new Subscription();
        subscription.setUserId(user.id());
        subscription.setPetId(pet.getId());
        subscription.setProduct(product);
        subscriptionRepository.save(subscription);

        try {
            emailService.sendSubscriptionConfirmed(
                    user.fullName(), user.email(), planName(product) + " para " + pet.getName(), product.getPrice());
        } catch (RuntimeException e) {
            log.error("Plan subscribed, but the confirmation e-mail failed", e);
        }
    }

    @Transactional
    public void cancel(Integer userId, Integer petId) {
        Subscription subscription = petId == null ? onlyPlanOf(userId) : subscriptionRepository
                .findByPetIdAndUserId(petId, userId)
                .orElseThrow(() -> ApiException.notFound("Este pet não possui plano para ser cancelado"));
        subscriptionRepository.delete(subscription);
    }

    @Transactional
    public void cancelOfPet(Integer petId) {
        subscriptionRepository.deleteByPetId(petId);
    }

    @Transactional
    public int cancelAll() {
        return subscriptionRepository.deleteAllSubscriptions();
    }

    private static String planName(Product product) {
        return product.getName().split("\\|")[0].trim();
    }

    private Subscription onlyPlanOf(Integer userId) {
        List<Subscription> subscriptions = subscriptionRepository.findAllByUserIdWithProduct(userId);
        if (subscriptions.isEmpty()) {
            throw ApiException.notFound("Você não possui nenhum plano para ser cancelado");
        }
        if (subscriptions.size() > 1) {
            throw ApiException.badRequest("Informe o pet cujo plano será cancelado");
        }
        return subscriptions.get(0);
    }
}
