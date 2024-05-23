package com.example.ecom.services;

import com.example.ecom.controllers.GiftCardController;
import com.example.ecom.exceptions.GiftCardDoesntExistException;
import com.example.ecom.exceptions.GiftCardExpiredException;
import com.example.ecom.models.GiftCard;
import com.example.ecom.models.LedgerEntry;
import com.example.ecom.models.TransactionType;
import com.example.ecom.repositories.GiftCardRepository;
import com.example.ecom.repositories.LedgerEntryRepository;
import com.example.ecom.utils.GiftCardUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class GiftCardServiceImpl implements GiftCardService {
    private final GiftCardRepository giftCardRepository;
    private final LedgerEntryRepository ledgerEntryRepository;

    @Autowired
    public GiftCardServiceImpl(GiftCardRepository giftCardRepository, LedgerEntryRepository ledgerEntryRepository, GiftCardController giftCardController) {
        this.giftCardRepository = giftCardRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
    }

    @Override
    public GiftCard createGiftCard(double amount) {
        GiftCard giftCard = new GiftCard();
        giftCard.setAmount(amount);
        giftCard.setCreatedAt(new Date());
        giftCard.setExpiresAt(GiftCardUtils.getExpirationDate(giftCard.getCreatedAt()));
        giftCard.setGiftCardCode(GiftCardUtils.generateGiftCardCode());
        List<LedgerEntry> ledger = new ArrayList<>();
        LedgerEntry ledgerEntry = new LedgerEntry();
        ledgerEntry.setAmount(amount);
        ledgerEntry.setCreatedAt(giftCard.getCreatedAt());
        ledgerEntry.setTransactionType(TransactionType.CREDIT);
        ledgerEntryRepository.save(ledgerEntry);
        ledger.add(ledgerEntry);
        giftCard.setLedger(ledger);
        return giftCardRepository.save(giftCard);
    }

    @Override
    public GiftCard redeemGiftCard(int giftCardId, double amountToRedeem) throws GiftCardDoesntExistException, GiftCardExpiredException {
        Optional<GiftCard> giftCard = giftCardRepository.findById(giftCardId);
        if (giftCard.isEmpty()) {
            throw new GiftCardDoesntExistException("GIFT CARD DOES NOT EXIST");
        }
        GiftCard giftCardToRedeem = giftCard.get();
        if (giftCardToRedeem.getExpiresAt().before(new Date())) {
            throw new GiftCardExpiredException("GIFT CARD EXPIRED");
        }
        if (giftCardToRedeem.getAmount() < amountToRedeem) {
            throw new GiftCardExpiredException("GIFT CARD EXPIRED");
        }
        LedgerEntry ledgerEntry = new LedgerEntry();
        if(giftCardToRedeem.getAmount() >= amountToRedeem) {
            giftCardToRedeem.setAmount(giftCardToRedeem.getAmount() - amountToRedeem);
            ledgerEntry.setAmount(amountToRedeem);
        } else {
            ledgerEntry.setAmount(giftCardToRedeem.getAmount());
            giftCardToRedeem.setAmount(0);
        }
        ledgerEntry.setCreatedAt(new Date());
        ledgerEntry.setTransactionType(TransactionType.DEBIT);
        ledgerEntryRepository.save(ledgerEntry);

        List<LedgerEntry> ledgerEntries = giftCardToRedeem.getLedger();
        ledgerEntries.add(ledgerEntry);
        giftCardToRedeem.setLedger(ledgerEntries);

        return giftCardRepository.save(giftCardToRedeem);
    }
}
