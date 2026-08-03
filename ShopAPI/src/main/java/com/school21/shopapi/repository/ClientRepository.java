package com.school21.shopapi.repository;

import com.school21.shopapi.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ClientRepository extends JpaRepository<Client, UUID> {
    List<Client> findByClientNameAndClientSurname(String clientName, String clientSurname);
}
