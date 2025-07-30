package com.sinch.demo.smsMessageRouter.optOut.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sinch.demo.smsMessageRouter.optOut.model.OptOut;
import com.sinch.demo.smsMessageRouter.optOut.repository.OptOutRepository;
import com.sinch.demo.smsMessageRouter.utils.PhoneNumberUtility;

@Service
public class OptOutService {

    @Autowired
    private OptOutRepository optOutRepository;

    @Autowired
    private PhoneNumberUtility phoneNumberUtility;

    @Transactional
    public Optional<OptOut> optOut(String phoneNumber) {
        String normalizedPhoneNumber = phoneNumberUtility.validateAndNormalize(phoneNumber);
        OptOut optOut = new OptOut(normalizedPhoneNumber);
        OptOut savedOptOut = optOutRepository.save(optOut);
        return Optional.of(savedOptOut);
    }

    public Optional<OptOut> getOptOut(String phoneNumber) {
        String normalizedPhoneNumber = phoneNumberUtility.validateAndNormalize(phoneNumber);
        return optOutRepository.findByPhoneNumber(normalizedPhoneNumber);
    }

    @Transactional
    public Optional<Long> removeOptOut(String phoneNumber) {
        String normalizedPhoneNumber = phoneNumberUtility.validateAndNormalize(phoneNumber);
        Optional<OptOut> existingOptOut = optOutRepository.findByPhoneNumber(normalizedPhoneNumber);
        if (existingOptOut.isPresent()) {
            Long optOutId = existingOptOut.get().getId();
            optOutRepository.deleteByPhoneNumber(normalizedPhoneNumber);
            return Optional.of(optOutId);
        } else {
            return Optional.empty();
        }
    }
} 