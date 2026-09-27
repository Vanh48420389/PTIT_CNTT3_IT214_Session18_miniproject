package ra.edu.rikkeibank.transaction;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import ra.edu.rikkeibank.transaction.client.AccountClient;
import ra.edu.rikkeibank.transaction.dto.ApiResponse;
import ra.edu.rikkeibank.transaction.dto.TransactionResponse;
import ra.edu.rikkeibank.transaction.dto.TransferRequest;
import ra.edu.rikkeibank.transaction.dto.AccountResponseDto;
import ra.edu.rikkeibank.transaction.dto.BalanceOperationResponseDto;
import ra.edu.rikkeibank.transaction.service.TransactionService;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@SpringBootTest
class TransactionServiceApplicationTests {

    @Autowired
    private TransactionService transactionService;

    @MockBean
    private AccountClient accountClient;

    @MockBean
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Test
    void contextLoads() {
        assertNotNull(transactionService);
    }

    @Test
    void testTransferSagaSuccess() {
        // Mock source account
        AccountResponseDto src = AccountResponseDto.builder()
                .accountNumber("1000000001")
                .balance(new BigDecimal("5000000"))
                .status("ACTIVE")
                .build();
        when(accountClient.getAccountByNumber("1000000001")).thenReturn(ApiResponse.success(src, "OK"));

        // Mock destination account
        AccountResponseDto dest = AccountResponseDto.builder()
                .accountNumber("1000000002")
                .balance(new BigDecimal("2000000"))
                .status("ACTIVE")
                .build();
        when(accountClient.getAccountByNumber("1000000002")).thenReturn(ApiResponse.success(dest, "OK"));

        // Mock debit
        BalanceOperationResponseDto debitRes = BalanceOperationResponseDto.builder()
                .accountNumber("1000000001")
                .currentBalance(new BigDecimal("4000000"))
                .build();
        when(accountClient.debit(eq("1000000001"), any())).thenReturn(ApiResponse.success(debitRes, "OK"));

        // Mock credit
        BalanceOperationResponseDto creditRes = BalanceOperationResponseDto.builder()
                .accountNumber("1000000002")
                .currentBalance(new BigDecimal("3000000"))
                .build();
        when(accountClient.credit(eq("1000000002"), any())).thenReturn(ApiResponse.success(creditRes, "OK"));

        TransferRequest req = TransferRequest.builder()
                .sourceAccountNumber("1000000001")
                .destinationAccountNumber("1000000002")
                .amount(new BigDecimal("1000000"))
                .message("Test transfer")
                .build();

        TransactionResponse res = transactionService.transfer(req, "tester", "BRANCH_HN");
        assertNotNull(res);
        assertEquals("SUCCESS", res.getStatus().name());
    }

    @Test
    void testTransferSagaCompensatingRollback() {
        // Mock accounts
        AccountResponseDto src = AccountResponseDto.builder()
                .accountNumber("1000000001")
                .balance(new BigDecimal("5000000"))
                .status("ACTIVE")
                .build();
        when(accountClient.getAccountByNumber("1000000001")).thenReturn(ApiResponse.success(src, "OK"));

        AccountResponseDto dest = AccountResponseDto.builder()
                .accountNumber("1000000002")
                .balance(new BigDecimal("2000000"))
                .status("ACTIVE")
                .build();
        when(accountClient.getAccountByNumber("1000000002")).thenReturn(ApiResponse.success(dest, "OK"));

        // Mock debit succeeds
        BalanceOperationResponseDto debitRes = BalanceOperationResponseDto.builder()
                .accountNumber("1000000001")
                .currentBalance(new BigDecimal("4000000"))
                .build();
        when(accountClient.debit(eq("1000000001"), any())).thenReturn(ApiResponse.success(debitRes, "OK"));

        // Mock refund/compensate
        when(accountClient.compensateCredit(eq("1000000001"), any()))
                .thenReturn(ApiResponse.success(debitRes, "Compensated"));

        // Request with simulateFailure = DEST_FAIL
        TransferRequest req = TransferRequest.builder()
                .sourceAccountNumber("1000000001")
                .destinationAccountNumber("1000000002")
                .amount(new BigDecimal("1000000"))
                .message("Test fail & rollback")
                .simulateFailure("DEST_FAIL")
                .build();

        TransactionResponse res = transactionService.transfer(req, "tester", "BRANCH_HN");
        assertNotNull(res);
        assertEquals("COMPENSATED", res.getStatus().name());
        assertTrue(res.getFailureReason().contains("SAGA ROLLBACK"));
    }
}
