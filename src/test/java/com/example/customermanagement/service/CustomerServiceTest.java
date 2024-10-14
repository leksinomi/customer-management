package com.example.customermanagement.service;

import com.example.customermanagement.dto.CustomerDTO;
import com.example.customermanagement.exception.CustomerNotFoundException;
import com.example.customermanagement.exception.InvalidSortParameterException;
import com.example.customermanagement.mapper.CustomerMapper;
import com.example.customermanagement.model.Customer;
import com.example.customermanagement.model.Gender;
import com.example.customermanagement.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerMapper customerMapper;

    private CustomerService customerService;

    @BeforeEach
    void setUp() {
        customerService = new CustomerService(customerRepository, customerMapper);
    }

    @Nested
    @DisplayName("Tests for createCustomer method")
    class CreateCustomerTests {

        @Test
        @DisplayName("Given valid CustomerDTO_When createCustomer_Then customer is created successfully")
        void givenValidCustomerDTO_WhenCreateCustomer_ThenCustomerIsCreatedSuccessfully() {
            CustomerDTO customerDTO = defaultCustomerDTO().build();
            Customer customerEntity = defaultCustomer().build();
            Customer savedCustomer = defaultCustomer()
                    .id(1L)
                    .build();
            CustomerDTO savedCustomerDTO = defaultCustomerDTO()
                    .id(1L)
                    .build();

            when(customerMapper.toEntity(customerDTO)).thenReturn(customerEntity);
            when(customerRepository.save(customerEntity)).thenReturn(savedCustomer);
            when(customerMapper.toDTO(savedCustomer)).thenReturn(savedCustomerDTO);

            CustomerDTO result = customerService.createCustomer(customerDTO);

            assertNotNull(result);
            assertEquals(1L, result.getId());
            assertEquals("John", result.getName());
            verify(customerMapper).toEntity(customerDTO);
            verify(customerRepository).save(customerEntity);
            verify(customerMapper).toDTO(savedCustomer);
        }
    }

    @Nested
    @DisplayName("Tests for getAllCustomers method")
    class GetAllCustomersTests {

        @Test
        @DisplayName("Given valid pagination and sorting parameters_When getAllCustomers_Then return list of customers")
        void givenValidPaginationAndSortingParameters_WhenGetAllCustomers_ThenReturnListOfCustomers() {
            int page = 0;
            int size = 10;
            String sortBy = "name";
            String sortDir = "asc";

            Sort sort = Sort.by("name").ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            Customer customer1 = defaultCustomer()
                    .id(1L)
                    .name("Alice")
                    .gender(Gender.F)
                    .build();
            Customer customer2 = defaultCustomer()
                    .id(2L)
                    .name("Bob")
                    .build();

            List<Customer> customers = Arrays.asList(customer1, customer2);
            Page<Customer> customerPage = new PageImpl<>(customers, pageable, customers.size());

            CustomerDTO customerDTO1 = defaultCustomerDTO()
                    .id(1L)
                    .name("Alice")
                    .gender("F")
                    .build();
            CustomerDTO customerDTO2 = defaultCustomerDTO()
                    .id(2L)
                    .name("Bob")
                    .build();


            when(customerRepository.findAll(pageable)).thenReturn(customerPage);
            when(customerMapper.toDTO(customer1)).thenReturn(customerDTO1);
            when(customerMapper.toDTO(customer2)).thenReturn(customerDTO2);

            Page<CustomerDTO> result = customerService.getAllCustomers(page, size, sortBy, sortDir);

            assertNotNull(result);
            assertEquals(2, result.getTotalElements());
            assertEquals(customerDTO1, result.getContent().get(0));
            assertEquals(customerDTO2, result.getContent().get(1));
            verify(customerRepository).findAll(pageable);
            verify(customerMapper).toDTO(customer1);
            verify(customerMapper).toDTO(customer2);
        }

        @Test
        @DisplayName("Given invalid sortBy parameter_When getAllCustomers_Then throw InvalidSortParameterException")
        void givenInvalidSortByParameter_WhenGetAllCustomers_ThenThrowInvalidSortParameterException() {
            int page = 0;
            int size = 10;
            String sortBy = "invalidField";
            String sortDir = "asc";

            InvalidSortParameterException exception = assertThrows(InvalidSortParameterException.class, () -> {
                customerService.getAllCustomers(page, size, sortBy, sortDir);
            });

            assertTrue(exception.getMessage().contains("Invalid sort parameter"));
            verifyNoInteractions(customerRepository, customerMapper);
        }

        @Test
        @DisplayName("Given invalid sortDir parameter_When getAllCustomers_Then throw InvalidSortParameterException")
        void givenInvalidSortDirParameter_WhenGetAllCustomers_ThenThrowInvalidSortParameterException() {
            int page = 0;
            int size = 10;
            String sortBy = "name";
            String sortDir = "invalidDir";

            InvalidSortParameterException exception = assertThrows(InvalidSortParameterException.class, () -> {
                customerService.getAllCustomers(page, size, sortBy, sortDir);
            });

            assertTrue(exception.getMessage().contains("Invalid sort direction"));
            verifyNoInteractions(customerRepository, customerMapper);
        }

        @Test
        @DisplayName("Given no customers in repository_When getAllCustomers_Then return empty page")
        void givenNoCustomersInRepository_WhenGetAllCustomers_ThenReturnEmptyPage() {
            int page = 0;
            int size = 10;
            String sortBy = "name";
            String sortDir = "asc";

            Sort sort = Sort.by("name").ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<Customer> customerPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

            when(customerRepository.findAll(pageable)).thenReturn(customerPage);

            Page<CustomerDTO> result = customerService.getAllCustomers(page, size, sortBy, sortDir);

            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(customerRepository).findAll(pageable);
            verifyNoInteractions(customerMapper);
        }
    }

    @Nested
    @DisplayName("Tests for getCustomerById method")
    class GetCustomerByIdTests {

        @Test
        @DisplayName("Given existing customer ID_When getCustomerById_Then return CustomerDTO")
        void givenExistingCustomerId_WhenGetCustomerById_ThenReturnCustomerDTO() {
            Long id = 1L;
            Customer customer = defaultCustomer()
                    .id(id)
                    .build();
            CustomerDTO customerDTO = defaultCustomerDTO()
                    .id(id)
                    .build();

            when(customerRepository.findById(id)).thenReturn(Optional.of(customer));
            when(customerMapper.toDTO(customer)).thenReturn(customerDTO);

            CustomerDTO result = customerService.getCustomerById(id);

            assertNotNull(result);
            assertEquals(customerDTO, result);
            verify(customerRepository).findById(id);
            verify(customerMapper).toDTO(customer);
        }

        @Test
        @DisplayName("Given non-existent customer ID_When getCustomerById_Then throw CustomerNotFoundException")
        void givenNonExistentCustomerId_WhenGetCustomerById_ThenThrowCustomerNotFoundException() {
            Long id = 1L;
            when(customerRepository.findById(id)).thenReturn(Optional.empty());

            CustomerNotFoundException exception = assertThrows(CustomerNotFoundException.class, () -> {
                customerService.getCustomerById(id);
            });

            assertTrue(exception.getMessage().contains("Customer not found with id " + id));
            verify(customerRepository).findById(id);
            verifyNoInteractions(customerMapper);
        }
    }

    @Nested
    @DisplayName("Tests for updateCustomer method")
    class UpdateCustomerTests {

        @Test
        @DisplayName("Given existing customer ID and valid CustomerDTO_When updateCustomer_Then customer is updated successfully")
        void givenExistingCustomerIdAndValidCustomerDTO_WhenUpdateCustomer_ThenCustomerIsUpdatedSuccessfully() {
            Long id = 1L;
            CustomerDTO updateDTO = defaultCustomerDTO()
                    .age(40)
                    .dateOfBirth("1983-07-25")
                    .build();
            Customer existingCustomer = defaultCustomer()
                    .age(39)
                    .dateOfBirth(LocalDate.parse("1984-06-15"))
                    .build();
            Customer updatedCustomer = defaultCustomer()
                    .id(id)
                    .age(40)
                    .dateOfBirth(LocalDate.parse("1983-07-25"))
                    .build();
            CustomerDTO updatedCustomerDTO = defaultCustomerDTO()
                    .id(id)
                    .age(40)
                    .dateOfBirth("1983-07-25")
                    .build();

            when(customerRepository.findById(id)).thenReturn(Optional.of(existingCustomer));
            doNothing().when(customerMapper).updateEntityFromDTO(updateDTO, existingCustomer);
            when(customerRepository.save(existingCustomer)).thenReturn(updatedCustomer);
            when(customerMapper.toDTO(updatedCustomer)).thenReturn(updatedCustomerDTO);

            CustomerDTO result = customerService.updateCustomer(id, updateDTO);

            assertNotNull(result);
            assertEquals(updatedCustomerDTO, result);
            verify(customerRepository).findById(id);
            verify(customerMapper).updateEntityFromDTO(updateDTO, existingCustomer);
            verify(customerRepository).save(existingCustomer);
            verify(customerMapper).toDTO(updatedCustomer);
        }

        @Test
        @DisplayName("Given non-existent customer ID_When updateCustomer_Then throw CustomerNotFoundException")
        void givenNonExistentCustomerId_WhenUpdateCustomer_ThenThrowCustomerNotFoundException() {
            Long id = 1L;
            CustomerDTO updateDTO = defaultCustomerDTO().build();

            when(customerRepository.findById(id)).thenReturn(Optional.empty());

            CustomerNotFoundException exception = assertThrows(CustomerNotFoundException.class, () -> {
                customerService.updateCustomer(id, updateDTO);
            });

            assertTrue(exception.getMessage().contains("Customer not found with id " + id));
            verify(customerRepository).findById(id);
            verifyNoMoreInteractions(customerMapper, customerRepository);
        }
    }

    @Nested
    @DisplayName("Tests for deleteCustomer method")
    class DeleteCustomerTests {

        @Test
        @DisplayName("Given existing customer ID_When deleteCustomer_Then customer is deleted successfully")
        void givenExistingCustomerId_WhenDeleteCustomer_ThenCustomerIsDeletedSuccessfully() {
            Long id = 1L;
            when(customerRepository.existsById(id)).thenReturn(true);
            doNothing().when(customerRepository).deleteById(id);

            assertDoesNotThrow(() -> customerService.deleteCustomer(id));

            verify(customerRepository).existsById(id);
            verify(customerRepository).deleteById(id);
        }

        @Test
        @DisplayName("Given non-existent customer ID_When deleteCustomer_Then throw CustomerNotFoundException")
        void givenNonExistentCustomerId_WhenDeleteCustomer_ThenThrowCustomerNotFoundException() {
            Long id = 1L;
            when(customerRepository.existsById(id)).thenReturn(false);

            CustomerNotFoundException exception = assertThrows(CustomerNotFoundException.class, () -> {
                customerService.deleteCustomer(id);
            });

            assertTrue(exception.getMessage().contains("Customer not found with id " + id));
            verify(customerRepository).existsById(id);
            verify(customerRepository, never()).deleteById(anyLong());
        }
    }

    private CustomerDTO.CustomerDTOBuilder defaultCustomerDTO() {
        return CustomerDTO.builder()
                .name("John")
                .age(30)
                .dateOfBirth("1993-01-01")
                .address("123 Street")
                .gender("M");
    }

    private Customer.CustomerBuilder defaultCustomer() {
        return Customer.builder()
                .name("John")
                .age(30)
                .dateOfBirth(LocalDate.parse("1993-01-01"))
                .address("123 Street")
                .gender(Gender.M);
    }
}
