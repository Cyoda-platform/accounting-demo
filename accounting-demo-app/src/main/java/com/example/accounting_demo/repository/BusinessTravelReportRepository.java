package com.example.accounting_demo.repository;

import com.example.accounting_demo.model.BusinessTravelReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BusinessTravelReportRepository extends JpaRepository<BusinessTravelReport, UUID> {
}
