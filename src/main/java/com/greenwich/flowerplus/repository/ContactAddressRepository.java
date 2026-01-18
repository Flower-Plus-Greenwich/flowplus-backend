package com.greenwich.flowerplus.repository;


import com.greenwich.flowerplus.entity.ContactAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContactAddressRepository extends JpaRepository<ContactAddress, Long> {
}
