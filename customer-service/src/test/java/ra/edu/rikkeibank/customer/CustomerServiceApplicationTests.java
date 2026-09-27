package ra.edu.rikkeibank.customer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ra.edu.rikkeibank.customer.dto.CustomerResponse;
import ra.edu.rikkeibank.customer.service.CustomerService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CustomerServiceApplicationTests {

    @Autowired
    private CustomerService customerService;

    @Test
    void contextLoads() {
        assertNotNull(customerService);
    }

    @Test
    void testGetCustomersList() {
        List<CustomerResponse> list = customerService.getAllCustomers();
        assertNotNull(list);
        assertFalse(list.isEmpty());
    }
}
