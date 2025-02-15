package com.example.ticketing_system.repo;

import com.example.ticketing_system.model.Ticket;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRepository extends MongoRepository<Ticket, String> {

    List<Ticket> findByStatus(String status);
    List<Ticket> findByCreatedBy(String createdBy);

}
