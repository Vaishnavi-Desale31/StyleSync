package com.project.stylesync_ai.service;

import com.project.stylesync_ai.dto.AddressRequest;
import com.project.stylesync_ai.dto.ProfileResponse;
import com.project.stylesync_ai.entity.Address;
import com.project.stylesync_ai.entity.User;
import com.project.stylesync_ai.repository.AddressRepository;
import com.project.stylesync_ai.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class AccountService {

    static final int MAX_ADDRESSES = 10;

    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final PasswordEncoder passwordEncoder;

    public AccountService(UserRepository userRepository,
                          AddressRepository addressRepository,
                          PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.addressRepository = addressRepository;
        this.passwordEncoder = passwordEncoder;
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"));
    }

    @Transactional(readOnly = true)
    public ProfileResponse getProfile(String email) {
        User user = getUser(email);
        return new ProfileResponse(
                user.getName(), user.getEmail(), user.getRole());
    }

    @Transactional
    public ProfileResponse updateProfile(String email, String name) {
        if (name == null || name.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Name is required");
        }

        User user = getUser(email);
        user.setName(name.trim());
        userRepository.save(user);

        return new ProfileResponse(
                user.getName(), user.getEmail(), user.getRole());
    }

    @Transactional
    public void changePassword(String email,
                               String currentPassword,
                               String newPassword) {
        User user = getUser(email);

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Current password is incorrect");
        }

        if (newPassword == null || newPassword.length() < 8
                || newPassword.length() > 72) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Password must contain 8 to 72 characters");
        }

        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "New password must be different from the current one");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    // ---------- addresses ----------

    @Transactional(readOnly = true)
    public List<Address> getAddresses(String email) {
        return addressRepository.findByUser_EmailOrderByIdAsc(email);
    }

    @Transactional
    public Address addAddress(String email, AddressRequest request) {
        User user = getUser(email);

        if (addressRepository.countByUser_Email(email) >= MAX_ADDRESSES) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "You can save up to " + MAX_ADDRESSES + " addresses");
        }

        Address address = new Address();
        address.setUser(user);
        applyRequest(address, request);

        // The first address becomes the default automatically.
        boolean makeDefault =
                Boolean.TRUE.equals(request.defaultAddress())
                        || addressRepository.countByUser_Email(email) == 0;

        if (makeDefault) {
            clearDefault(email);
        }
        address.setDefaultAddress(makeDefault);

        return addressRepository.save(address);
    }

    @Transactional
    public Address updateAddress(String email, Long id,
                                 AddressRequest request) {
        Address address = findAddress(email, id);
        applyRequest(address, request);

        if (Boolean.TRUE.equals(request.defaultAddress())) {
            clearDefault(email);
            address.setDefaultAddress(true);
        }

        return addressRepository.save(address);
    }

    @Transactional
    public void deleteAddress(String email, Long id) {
        Address address = findAddress(email, id);
        boolean wasDefault = address.isDefaultAddress();

        addressRepository.delete(address);
        addressRepository.flush();

        if (wasDefault) {
            addressRepository.findByUser_EmailOrderByIdAsc(email)
                    .stream()
                    .findFirst()
                    .ifPresent(first -> {
                        first.setDefaultAddress(true);
                        addressRepository.save(first);
                    });
        }
    }

    private Address findAddress(String email, Long id) {
        return addressRepository.findByIdAndUser_Email(id, email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Address not found"));
    }

    private void clearDefault(String email) {
        for (Address existing
                : addressRepository.findByUser_EmailOrderByIdAsc(email)) {
            if (existing.isDefaultAddress()) {
                existing.setDefaultAddress(false);
                addressRepository.save(existing);
            }
        }
    }

    private void applyRequest(Address address, AddressRequest request) {
        address.setLabel(trimOrNull(request.label()));
        address.setFullName(request.fullName().trim());
        address.setPhone(request.phone().trim());
        address.setLine1(request.line1().trim());
        address.setLine2(trimOrNull(request.line2()));
        address.setCity(request.city().trim());
        address.setState(request.state().trim());
        address.setPostalCode(request.postalCode().trim());
        address.setCountry(
                request.country() == null || request.country().isBlank()
                        ? "India" : request.country().trim());
    }

    private String trimOrNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}