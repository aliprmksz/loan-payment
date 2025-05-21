package com.ing.loan.payment.controller;



import com.ing.loan.payment.config.JwtAuthenticationFilter;
import com.ing.loan.payment.model.Customer;
import com.ing.loan.payment.repository.CustomerRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    private final JwtAuthenticationFilter  jwtAuthenticationFilter;

    public AuthenticationController(CustomerRepository customerRepository, PasswordEncoder passwordEncoder,
                                    JwtAuthenticationFilter  jwtAuthenticationFilter) {
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @PostMapping("/create")
    public String create(@RequestBody Customer customer) {
        customer.setPassword(passwordEncoder.encode(customer.getPassword()));
        customerRepository.save(customer);
        return "User created successfully";
    }

    @PostMapping("/login")
    public String login(@RequestBody LoginRequest request) {
        Customer customer = customerRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (passwordEncoder.matches(request.getPassword(), customer.getPassword())) {
            return jwtAuthenticationFilter.generateToken(customer.getUsername(), customer.getRole().name());
        }
        throw new RuntimeException("Invalid credentials");
    }
}


