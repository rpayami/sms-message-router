package com.sinch.demo.smsMessageRouter.optOut.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.sinch.demo.smsMessageRouter.optOut.model.OptOut;

@Repository
public interface OptOutRepository extends CrudRepository<OptOut, Long> {
    Optional<OptOut> findByPhoneNumber(String phoneNumber);
    long deleteByPhoneNumber(String phoneNumber);
} 