package com.example.customermanagement.service;

import com.example.customermanagement.dto.CustomerDTO;
import com.example.customermanagement.exception.CustomerNotFoundException;
import com.example.customermanagement.mapper.CustomerMapper;
import com.example.customermanagement.model.Customer;
import com.example.customermanagement.repository.CustomerRepository;
import com.example.customermanagement.utils.SortUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CustomerService {

    private static final String CUSTOMER_NOT_FOUND_MESSAGE_PREFIX = "Customer not found with id ";
    private static final List<String> ALLOWED_SORT_FIELDS = List.of(
            "id",
            "name",
            "age",
            "dateOfBirth",
            "address",
            "gender"
    );

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    @Autowired
    public CustomerService(CustomerRepository customerRepository,
                           CustomerMapper customerMapper) {
        this.customerRepository = customerRepository;
        this.customerMapper = customerMapper;
    }

    @Transactional
    public CustomerDTO createCustomer(CustomerDTO customerDTO) {
        Customer customer = customerMapper.toEntity(customerDTO);
        Customer savedCustomer = customerRepository.save(customer);
        return customerMapper.toDTO(savedCustomer);
    }

    @Transactional(readOnly = true)
    public Page<CustomerDTO> getAllCustomers(int page, int size, String sortBy, String sortDir) {
        Pageable pageable = SortUtils.createPageable(page, size, sortBy, sortDir, ALLOWED_SORT_FIELDS);
        Page<Customer> customers = customerRepository.findAll(pageable);
        return customers.map(customerMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public CustomerDTO getCustomerById(Long id) {
        return customerRepository.findById(id)
                .map(customerMapper::toDTO)
                .orElseThrow(() -> new CustomerNotFoundException(CUSTOMER_NOT_FOUND_MESSAGE_PREFIX + id));
    }

    @Transactional
    public CustomerDTO updateCustomer(Long id, CustomerDTO customerDTO) {
        Customer existingCustomer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(CUSTOMER_NOT_FOUND_MESSAGE_PREFIX + id));
        customerMapper.updateEntityFromDTO(customerDTO, existingCustomer);
        Customer updatedCustomer = customerRepository.save(existingCustomer);
        return customerMapper.toDTO(updatedCustomer);
    }

    @Transactional
    public void deleteCustomer(Long id) {
        if (!customerRepository.existsById(id)) {
            throw new CustomerNotFoundException(CUSTOMER_NOT_FOUND_MESSAGE_PREFIX + id);
        }
        customerRepository.deleteById(id);
    }
}
