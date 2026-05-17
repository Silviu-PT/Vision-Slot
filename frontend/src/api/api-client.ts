import axios from "axios";
import {
  AvailableDaySlotsResponse,
  AppointmentCreateRequest,
  AppointmentResponse,
  SchedulingConfigurationRequest,
  SchedulingConfigurationResponse,
  AppointmentRescheduleRequest,
  AppointmentReportResponse,
} from "../types/models";

const apiClient = axios.create({
  baseURL: "/api",
  headers: {
    "Content-Type": "application/json",
  },
});

export const slotsApi = {
  getAvailableSlots: async (): Promise<AvailableDaySlotsResponse[]> => {
    const response = await apiClient.get<AvailableDaySlotsResponse[]>("/slots");
    return response.data;
  },
};

export const appointmentsApi = {
  createAppointment: async (
    request: AppointmentCreateRequest
  ): Promise<AppointmentResponse> => {
    const response = await apiClient.post<AppointmentResponse>("/appointments", request);
    return response.data;
  },
  getAppointmentById: async (id: string): Promise<AppointmentResponse> => {
    const response = await apiClient.get<AppointmentResponse>(`/appointments/${id}`);
    return response.data;
  },
  getAppointmentsByEmployee: async (employeeCode: string): Promise<AppointmentResponse[]> => {
    const response = await apiClient.get<AppointmentResponse[]>(`/appointments/by-employee/${employeeCode}`);
    return response.data;
  },
  rescheduleAppointment: async (id: string, request: AppointmentRescheduleRequest): Promise<any> => {
    const response = await apiClient.put(`/appointments/${id}`, request);
    return response.data;
  },
  cancelAppointment: async (id: string): Promise<any> => {
    const response = await apiClient.delete(`/appointments/${id}`);
    return response.data;
  },
};

export const reportsApi = {
  getAppointmentsReport: async (startDate?: string, endDate?: string): Promise<AppointmentReportResponse[]> => {
    const response = await apiClient.get<AppointmentReportResponse[]>("/reports/appointments", {
      params: { startDate, endDate }
    });
    return response.data;
  },
  exportAppointments: async (startDate?: string, endDate?: string): Promise<void> => {
    const response = await apiClient.get("/reports/appointments/export", {
      params: { startDate, endDate },
      responseType: 'blob'
    });
    const url = window.URL.createObjectURL(new Blob([response.data]));
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', 'visionslot-programari.xlsx');
    document.body.appendChild(link);
    link.click();
    link.remove();
  },
};

export const adminConfigApi = {
  getConfiguration: async (): Promise<SchedulingConfigurationResponse> => {
    const response = await apiClient.get<SchedulingConfigurationResponse>("/admin/configuration");
    return response.data;
  },
  updateConfiguration: async (
    request: SchedulingConfigurationRequest
  ): Promise<SchedulingConfigurationResponse> => {
    const response = await apiClient.put<SchedulingConfigurationResponse>("/admin/configuration", request);
    return response.data;
  },
};
