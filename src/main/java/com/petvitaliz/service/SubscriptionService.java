package com.petvitaliz.service;

import com.petvitaliz.dto.ProductResponse;
import com.petvitaliz.entity.Product;
import com.petvitaliz.entity.Subscription;
import com.petvitaliz.exception.ApiException;
import com.petvitaliz.repository.ProductRepository;
import com.petvitaliz.repository.SubscriptionRepository;
import com.petvitaliz.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final ProductRepository productRepository;
    private final EmailService emailService;

    @Transactional(readOnly = true)
    public Optional<ProductResponse> findCurrentPlan(Integer userId) {
        return subscriptionRepository.findFirstByUserId(userId)
                .map(subscription -> ProductResponse.of(subscription.getProduct()));
    }

    @Transactional
    public void subscribe(AuthenticatedUser user, Integer productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> ApiException.notFound("Id não encontrado"));

        if (subscriptionRepository.findFirstByUserId(user.id()).isPresent()) {
            throw ApiException.badRequest("Você já possui um plano, cancele o plano atual para assinar outro");
        }

        Subscription subscription = new Subscription();
        subscription.setUserId(user.id());
        subscription.setProduct(product);
        subscriptionRepository.save(subscription);

        try {
            emailService.sendSubscriptionConfirmed(
                    user.fullName(), user.email(), product.getName(), product.getPrice());
        } catch (RuntimeException e) {
            log.error("Plan subscribed, but the confirmation e-mail failed", e);
        }
    }

    @Transactional
    public void cancel(Integer userId) {
        Subscription subscription = subscriptionRepository.findFirstByUserId(userId)
                .orElseThrow(() -> ApiException.notFound("Você não possui nenhum plano para ser cancelado"));
        subscriptionRepository.delete(subscription);
    }

    @Transactional
    public int cancelAll() {
        return subscriptionRepository.deleteAllSubscriptions();
    }
}
