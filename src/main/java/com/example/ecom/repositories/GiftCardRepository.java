package com.example.ecom.repositories;

import com.example.ecom.models.GiftCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GiftCardRepository extends JpaRepository<GiftCard, Integer> {
    @Override
    Optional<GiftCard> findById(Integer integer);
}
