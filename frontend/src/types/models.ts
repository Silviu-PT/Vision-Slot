export enum ParticipantType {
  EMPLOYEE = "EMPLOYEE",
  AGENCY = "AGENCY",
}

export enum BookingSource {
  LINK = "LINK",
  HR_PORTAL = "HR_PORTAL",
  PRISMA = "PRISMA",
  TERMINAL = "TERMINAL",
}

export enum AppointmentStatus {
  ACTIVE = "ACTIVE",
  CANCELLED = "CANCELLED",
}

export interface TimeIntervalResponse {
  start: string; // ISO String LocalDateTime
  end: string;
}

export interface AvailableDaySlotsResponse {
  date: string; // YYYY-MM-DD
  dayOfWeek: string;
  slots: TimeIntervalResponse[];
}

export interface AppointmentCreateRequest {
  employeeCode: string;
  fullName: string;
  email: string;
  participantType: ParticipantType;
  bookingSource: BookingSource;
  appointmentStart: string; // LocalDateTime
}

export interface AppointmentRescheduleRequest {
  newAppointmentStart: string; // LocalDateTime
}

export interface AppointmentReportResponse {
  date: string;
  startTime: string;
  endTime: string;
  employeeCode: string;
  fullName: string;
  email: string;
  participantType: ParticipantType;
  bookingSource: BookingSource;
  status: AppointmentStatus;
}

export interface AppointmentResponse {
  id: string; // UUID
  employeeCode: string;
  fullName: string;
  email: string;
  participantType: ParticipantType;
  bookingSource: BookingSource;
  status: AppointmentStatus;
  appointmentStart: string; // LocalDateTime
  appointmentEnd: string; // LocalDateTime
  createdAt: string; // Instant
  updatedAt: string; // Instant
}

export interface SchedulingConfigurationRequest {
  consultationDays: string[]; // DayOfWeek
  consultationStartTime: string; // LocalTime
  consultationEndTime: string; // LocalTime
  lunchBreakStart?: string; // LocalTime
  lunchBreakEnd?: string; // LocalTime
  slotDurationMinutes: number;
  bookingWindowStart: string; // LocalDate
  bookingWindowEnd: string; // LocalDate
}

export interface SchedulingConfigurationResponse extends SchedulingConfigurationRequest {
  id: string;
}
