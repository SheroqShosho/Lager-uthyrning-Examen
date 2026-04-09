package se.gritacademy.lageruthyrningexamen.service;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PaymentService {

    // Simulera en betalning genom att validera belopp och generera en unik betalningsreferens
    public String mockPay(long amountInCents) {
        if (amountInCents <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        return "PAY-" + UUID.randomUUID();
    }
}
