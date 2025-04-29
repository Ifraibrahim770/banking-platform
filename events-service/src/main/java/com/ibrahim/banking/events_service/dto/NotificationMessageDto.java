package com.ibrahim.banking.events_service.dto;

import com.ibrahim.banking.events_service.model.TransactionStatus;
import com.ibrahim.banking.events_service.model.TransactionType;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationMessageDto implements Serializable {
    private Long userId;
    private String transactionReference;
    private TransactionType transactionType;
    private TransactionStatus transactionStatus;
    private BigDecimal amount;
    private String currency;
    private Instant timestamp;
    private String message;
    


} 