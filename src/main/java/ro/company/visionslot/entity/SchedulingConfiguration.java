package ro.company.visionslot.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedHashSet;
import java.util.Set;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "scheduling_configurations")
public class SchedulingConfiguration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ElementCollection(fetch = FetchType.EAGER, targetClass = DayOfWeek.class)
    @CollectionTable(name = "scheduling_configuration_days", joinColumns = @JoinColumn(name = "configuration_id"))
    @Column(name = "consultation_day", nullable = false)
    @Enumerated(EnumType.STRING)
    private Set<DayOfWeek> consultationDays = new LinkedHashSet<>();

    @Column(nullable = false)
    private LocalTime consultationStartTime;

    @Column(nullable = false)
    private LocalTime consultationEndTime;

    private LocalTime lunchBreakStart;

    private LocalTime lunchBreakEnd;

    @Column(nullable = false)
    private Integer slotDurationMinutes;

    @Column(nullable = false)
    private LocalDate bookingWindowStart;

    @Column(nullable = false)
    private LocalDate bookingWindowEnd;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    @Version
    private Long version;

    public Long getId() {
        return id;
    }

    public Set<DayOfWeek> getConsultationDays() {
        return consultationDays;
    }

    public void setConsultationDays(Set<DayOfWeek> consultationDays) {
        this.consultationDays = consultationDays;
    }

    public LocalTime getConsultationStartTime() {
        return consultationStartTime;
    }

    public void setConsultationStartTime(LocalTime consultationStartTime) {
        this.consultationStartTime = consultationStartTime;
    }

    public LocalTime getConsultationEndTime() {
        return consultationEndTime;
    }

    public void setConsultationEndTime(LocalTime consultationEndTime) {
        this.consultationEndTime = consultationEndTime;
    }

    public LocalTime getLunchBreakStart() {
        return lunchBreakStart;
    }

    public void setLunchBreakStart(LocalTime lunchBreakStart) {
        this.lunchBreakStart = lunchBreakStart;
    }

    public LocalTime getLunchBreakEnd() {
        return lunchBreakEnd;
    }

    public void setLunchBreakEnd(LocalTime lunchBreakEnd) {
        this.lunchBreakEnd = lunchBreakEnd;
    }

    public Integer getSlotDurationMinutes() {
        return slotDurationMinutes;
    }

    public void setSlotDurationMinutes(Integer slotDurationMinutes) {
        this.slotDurationMinutes = slotDurationMinutes;
    }

    public LocalDate getBookingWindowStart() {
        return bookingWindowStart;
    }

    public void setBookingWindowStart(LocalDate bookingWindowStart) {
        this.bookingWindowStart = bookingWindowStart;
    }

    public LocalDate getBookingWindowEnd() {
        return bookingWindowEnd;
    }

    public void setBookingWindowEnd(LocalDate bookingWindowEnd) {
        this.bookingWindowEnd = bookingWindowEnd;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
