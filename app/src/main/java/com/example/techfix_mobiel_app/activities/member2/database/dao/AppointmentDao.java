package com.example.techfix_mobiel_app.activities.member2.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.techfix_mobiel_app.activities.member2.database.entities.AppointmentEntity;

import java.util.List;

@Dao
public interface AppointmentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long createAppointment(AppointmentEntity appointment);

    // History includes Closed, Completed, and Rejected items
    @Query("SELECT * FROM appointments WHERE customerId = :customerId AND status IN ('Closed', 'Completed', 'Rejected') ORDER BY id DESC")
    List<AppointmentEntity> getCustomerHistory(int customerId);

    @Query("SELECT * FROM appointments WHERE customerId = :customerId ORDER BY id DESC")
    List<AppointmentEntity> getAppointmentsForUser(int customerId);

    @Query("SELECT * FROM appointments WHERE assignedTechId = :techId")
    List<AppointmentEntity> getTechnicianAssignedJobs(int techId);

    @Query("SELECT * FROM appointments WHERE assignedBranchId = :branchId")
    List<AppointmentEntity> getBranchAppointments(int branchId);

    @Query("UPDATE appointments SET status = :status WHERE id = :appointmentId")
    void updateStatus(int appointmentId, String status);

    @Query("UPDATE appointments SET paymentStatus = :paymentStatus WHERE id = :appointmentId")
    void updatePaymentStatus(int appointmentId, String paymentStatus);

    @Insert
    void insert(AppointmentEntity appointment);

    @Query("SELECT * FROM appointments WHERE customerId = :customerId AND status NOT IN ('Closed', 'Completed') ORDER BY id DESC")
    List<AppointmentEntity> getActiveAppointmentsForUser(int customerId);

    // Get all bookings for the admin list
    @Query("SELECT * FROM appointments ORDER BY id DESC")
    List<AppointmentEntity> getAllAppointments();

    // Get counts for the top dashboard cards
    @Query("SELECT COUNT(*) FROM appointments WHERE status = 'Pending'")
    int getPendingCount();

    @Query("SELECT COUNT(*) FROM appointments WHERE status = 'Approved'")
    int getApprovedCount();

    @Query("SELECT SUM(price) FROM appointments WHERE paymentStatus = 'Paid'")
    double getTotalRevenue();

    // 1. Admin sets price and sends to customer (Keeps it in Admin's Pending tab)
    @Query("UPDATE appointments SET price = :repairPrice, status = 'Awaiting Customer' WHERE id = :bookingId")
    void adminSetPrice(int bookingId, double repairPrice);

    // 2. Admin marks as completed
    @Query("UPDATE appointments SET status = 'Completed' WHERE id = :bookingId")
    void adminMarkCompleted(int bookingId);

    @Query("SELECT * FROM appointments WHERE id = :id")
    AppointmentEntity getAppointmentById(int id);

    @Update
    void update(AppointmentEntity appointment);

    @Query("SELECT * FROM appointments WHERE customerName = :customerName ORDER BY id DESC")
    List<AppointmentEntity> getAppointmentsByCustomerName(String customerName);

    // History includes Closed, Completed, and Rejected items
    @Query("SELECT * FROM appointments WHERE customerId = :customerId AND status IN ('Closed', 'Completed', 'Rejected') ORDER BY id DESC")
    List<AppointmentEntity> getHistoryAppointmentsForUser(int customerId);
}