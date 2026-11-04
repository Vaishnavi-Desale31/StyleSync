package com.project.stylesync_ai.service;

import com.project.stylesync_ai.entity.Address;
import com.project.stylesync_ai.repository.AddressRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/** Shared lookup of the delivery address chosen at checkout. */
final class DeliveryAddresses {

    private DeliveryAddresses() {
    }

    static Address require(
            AddressRepository addressRepository,
            Long addressId,
            String email) {

        if (addressId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Delivery address is required");
        }

        return addressRepository
                .findByIdAndUser_Email(addressId, email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Delivery address not found"));
    }
}
