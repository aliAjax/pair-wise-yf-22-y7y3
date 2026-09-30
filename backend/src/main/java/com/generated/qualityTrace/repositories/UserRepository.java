package com.generated.qualityTrace.repositories;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.generated.qualityTrace.models.SystemUser;

@Repository
public class UserRepository {

  private final TraceDataStore store;

  public UserRepository(TraceDataStore store) {
    this.store = store;
  }

  public Optional<SystemUser> findByUsername(String username) {
    return store.findUser(username);
  }
}
