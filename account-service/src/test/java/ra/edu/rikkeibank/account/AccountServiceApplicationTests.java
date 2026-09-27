package ra.edu.rikkeibank.account;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ra.edu.rikkeibank.account.dto.AccountResponse;
import ra.edu.rikkeibank.account.service.AccountService;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AccountServiceApplicationTests {

    @Autowired
    private AccountService accountService;

    @Test
    void contextLoads() {
        assertNotNull(accountService);
    }

    @Test
    void testGetAccountByNumber() {
        AccountResponse account = accountService.getAccountByNumber("1000000001");
        assertNotNull(account);
        assertEquals("1000000001", account.getAccountNumber());
        assertEquals("CUST001", account.getCustomerCode());
    }
}
