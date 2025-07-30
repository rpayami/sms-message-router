package com.sinch.demo.smsMessageRouter.message.repository;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import com.sinch.demo.smsMessageRouter.message.model.SMSMessage;

@Repository
public interface SMSMessageRepository extends ListCrudRepository<SMSMessage, Long> {
}
