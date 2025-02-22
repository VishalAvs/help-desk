package com.example.ticketing_system.service;

import com.example.ticketing_system.model.Ticket;
import com.example.ticketing_system.repo.TicketRepository;
import com.example.ticketing_system.security.S3Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TicketService {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private S3Config s3Config;

    @Autowired
    private S3Client s3Client;

    public List<Ticket> getAllTickets() {
        return ticketRepository.findAll();
    }

    public Optional<Ticket> getTicketById(String id) {
        return ticketRepository.findById(id);
    }

    // Method to handle ticket creation along with image upload
    public Ticket createTicket(Ticket ticket, MultipartFile file) throws IOException {
        if (file != null && !file.isEmpty()) {
            String fileKey = uploadTicketImageToS3(file); // Upload image to S3 and get file key
            ticket.setImageUrl(fileKey); // Set S3 image URL to the ticket
        }

        ticket.setCreatedAt(LocalDateTime.now());
        ticket.setUpdatedAt(LocalDateTime.now());
        ticket.setStatus("OPEN");
        return ticketRepository.save(ticket);
    }

    // Helper method to upload ticket image to S3
    private String uploadTicketImageToS3(MultipartFile file) throws IOException {
        String fileName = "ticket-issues/" + file.getOriginalFilename();
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(s3Config.getBucketName())
                .key(fileName)
                .build();

        s3Client.putObject(putObjectRequest, file.getResource().getFile().toPath());
        return fileName; // Returning the file key to be stored in MongoDB
    }

    // Method to handle ticket updates
    public Ticket updateTicket(String id, Ticket updatedTicket) {
        return ticketRepository.findById(id).map(ticket -> {
            ticket.setTitle(updatedTicket.getTitle());
            ticket.setDescription(updatedTicket.getDescription());
            ticket.setStatus(updatedTicket.getStatus());
            ticket.setUpdatedAt(LocalDateTime.now());
            return ticketRepository.save(ticket);
        }).orElse(null);
    }

    // Method to handle ticket deletion
    public void deleteTicket(String id) {
        ticketRepository.deleteById(id);
    }
}
