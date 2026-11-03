package com.project.stylesync_ai.controller;

import com.project.stylesync_ai.dto.AddressRequest;
import com.project.stylesync_ai.dto.ChangePasswordRequest;
import com.project.stylesync_ai.dto.ProfileResponse;
import com.project.stylesync_ai.dto.UpdateProfileRequest;
import com.project.stylesync_ai.entity.Address;
import com.project.stylesync_ai.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/account")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/profile")
    public ProfileResponse getProfile(Authentication authentication) {
        return accountService.getProfile(authentication.getName());
    }

    @PutMapping("/profile")
    public ProfileResponse updateProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            Authentication authentication) {
        return accountService.updateProfile(
                authentication.getName(), request.name());
    }

    @PutMapping("/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication) {
        accountService.changePassword(
                authentication.getName(),
                request.currentPassword(),
                request.newPassword());
    }

    @GetMapping("/addresses")
    public List<Address> getAddresses(Authentication authentication) {
        return accountService.getAddresses(authentication.getName());
    }

    @PostMapping("/addresses")
    @ResponseStatus(HttpStatus.CREATED)
    public Address addAddress(
            @Valid @RequestBody AddressRequest request,
            Authentication authentication) {
        return accountService.addAddress(
                authentication.getName(), request);
    }

    @PutMapping("/addresses/{id}")
    public Address updateAddress(
            @PathVariable Long id,
            @Valid @RequestBody AddressRequest request,
            Authentication authentication) {
        return accountService.updateAddress(
                authentication.getName(), id, request);
    }

    @DeleteMapping("/addresses/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAddress(
            @PathVariable Long id,
            Authentication authentication) {
        accountService.deleteAddress(authentication.getName(), id);
    }
}