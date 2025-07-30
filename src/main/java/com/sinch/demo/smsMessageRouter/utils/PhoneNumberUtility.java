package com.sinch.demo.smsMessageRouter.utils;

import org.springframework.stereotype.Component;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

@Component
public class PhoneNumberUtility {

    private final PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();

    public boolean isValidPhoneNumber(String phoneNumber) {
        try {
            PhoneNumber parsedNumber = phoneNumberUtil.parse(phoneNumber, null);
            return phoneNumberUtil.isValidNumber(parsedNumber);
        } catch (NumberParseException e) {
            return false;
        }
    }

    public void validatePhoneNumber(String phoneNumber) {
        if (!isValidPhoneNumber(phoneNumber)) {
            throw new InvalidPhoneNumberException("Invalid phone number format: " + phoneNumber);
        }
    }

    public String normalizePhoneNumber(String phoneNumber) {
        try {
            PhoneNumber parsedNumber = phoneNumberUtil.parse(phoneNumber, null);
            if (phoneNumberUtil.isValidNumber(parsedNumber)) {
                return phoneNumberUtil.format(parsedNumber, PhoneNumberUtil.PhoneNumberFormat.E164);
            }
            return phoneNumber; // Return original if invalid
        } catch (NumberParseException e) {
            return phoneNumber; // Return original if parsing fails
        }
    }

    public String validateAndNormalize(String phoneNumber) {
        validatePhoneNumber(phoneNumber);
        return normalizePhoneNumber(phoneNumber);
    }

    public String getCountryCode(String phoneNumber) {
        try {
            PhoneNumber parsedNumber = phoneNumberUtil.parse(phoneNumber, null);
            if (phoneNumberUtil.isValidNumber(parsedNumber)) {
                return phoneNumberUtil.getRegionCodeForNumber(parsedNumber);
            }
            return null;
        } catch (NumberParseException e) {
            return null;
        }
    }

} 