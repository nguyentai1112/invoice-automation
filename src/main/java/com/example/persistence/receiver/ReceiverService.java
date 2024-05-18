package com.example.persistence.receiver;

import com.example.exception.RecordNotFoundException;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@Slf4j
public class ReceiverService {
  private final ReceiverRepository receiverRepository;
  @Transactional
  public ReceiverEntity insertReceiver(ReceiverEntity receiver) {
    receiver.setCreatedTime(ZonedDateTime.now());
    receiver.setModifiedTime(ZonedDateTime.now());
   return  receiverRepository.save(receiver);
  }

  public Optional<ReceiverEntity> getReceiver(String email) throws RecordNotFoundException {
    Optional<ReceiverEntity> found = receiverRepository.findById(email.toLowerCase());
    if (found.isEmpty()) {
      log.error("Receiver with email:{} not found", email);
      throw new RecordNotFoundException("Receiver with email " + email + " not found");
    }
    return found;
  }
  @Transactional
  public ReceiverEntity createIfNotExisting(ReceiverEntity receiver) {
    return receiverRepository.findById(receiver.email).orElse(insertReceiver(receiver));
  }

  public List<ReceiverEntity> getAllReceivers() {
    return receiverRepository.findAllByOrderByEmailAsc();
  }

}
