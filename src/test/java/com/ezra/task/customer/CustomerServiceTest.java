package com.ezra.task.customer;

import com.ezra.task.customer.dto.CustomerDTOs;
import com.ezra.task.customer.entity.Customer;
import com.ezra.task.customer.repository.CustomerRepository;
import com.ezra.task.customer.service.CustomerService;
import com.ezra.task.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    @Test
    void should_create_customer() {
        var customer = new Customer();
        var request = mock(CustomerDTOs.CustomerRequest.class);

        when(request.toCustomer()).thenReturn(customer);
        when(customerRepository.save(customer)).thenReturn(customer);

        var result = customerService.createCustomer(request);

        assertNotNull(result);
        verify(customerRepository).save(customer);
    }

    @Test
    void should_get_customer_by_id() {
        var customer = new Customer();
        customer.setId(1);

        when(customerRepository.findById(1)).thenReturn(Optional.of(customer));

        var response = customerService.getCustomerById(1);

        assertNotNull(response);
    }

    @Test
    void should_throw_when_customer_not_found() {
        when(customerRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> customerService.getCustomerById(1));
    }
}
