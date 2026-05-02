package com.app.transaction;

import java.time.LocalDate;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.datafaker.Faker;

public class TransactionGenerator {

    private final Faker faker = new Faker();

    private final ObjectMapper mapper = new ObjectMapper();

    private Transaction generateTransaction() {
        Transaction txn = new Transaction(
            UUID.randomUUID().toString(),
            faker.number().randomDouble(2, 100, 10000),
            faker.options().option( "PENDING"),
            LocalDate.now().toString(),
            faker.finance().iban()
        );

        return txn;
    }

    public String generateMessage() throws JsonProcessingException {
        return mapper.writeValueAsString(generateTransaction());
    }

}
