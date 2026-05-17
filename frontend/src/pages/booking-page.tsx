import { useEffect, useState } from "react";
import { slotsApi, appointmentsApi } from "@/api/api-client";
import { AvailableDaySlotsResponse, TimeIntervalResponse, AppointmentCreateRequest } from "@/types/models";
import { CalendarWidget } from "@/components/calendar-widget";
import { AppointmentForm } from "@/components/appointment-form";
import { SectionLoader } from "@/components/ui/spinner";



export function BookingPage() {
  const [availableDays, setAvailableDays] = useState<AvailableDaySlotsResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [selectedDate, setSelectedDate] = useState<string | null>(null);
  const [selectedSlot, setSelectedSlot] = useState<TimeIntervalResponse | null>(null);
  
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [success, setSuccess] = useState(false);

  useEffect(() => {
    const fetchSlots = async () => {
      try {
        const data = await slotsApi.getAvailableSlots();
        setAvailableDays(data);
      } catch (err: any) {
        setError(err.message || "Eroare la încărcarea intervalelor disponibile.");
      } finally {
        setLoading(false);
      }
    };

    fetchSlots();
  }, []);

  const handleSlotSelect = (date: string, slot: TimeIntervalResponse) => {
    setSelectedDate(date);
    setSelectedSlot(slot);
    setSuccess(false);
  };

  const handleBookingSubmit = async (request: AppointmentCreateRequest) => {
    setIsSubmitting(true);
    setError(null);
    try {
      await appointmentsApi.createAppointment(request);
      setSuccess(true);
      setSelectedDate(null);
      setSelectedSlot(null);
      // Recarregar os slots para remover o que foi ocupado
      const data = await slotsApi.getAvailableSlots();
      setAvailableDays(data);
    } catch (err: any) {
      setError(err.response?.data?.message || err.message || "Eroare la crearea programării.");
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="flex-1 space-y-8 max-w-5xl mx-auto w-full animate-in fade-in duration-500">
      <div className="flex flex-col space-y-2">
        <h2 className="text-3xl font-bold tracking-tight">Programare Consultație</h2>
        <p className="text-muted-foreground">
          Bine ați venit la Vision Slot. Alegeți data și ora pentru programare.
        </p>
      </div>

      <div className="w-full space-y-8 mt-6">
        {error && (
          <div className="p-4 bg-destructive/10 text-destructive border border-destructive/20 rounded-lg">
            {error}
          </div>
        )}

        {success && (
          <div className="p-6 bg-green-500/10 text-green-500 border border-green-500/20 rounded-lg text-center">
            <h3 className="text-xl font-bold mb-2">Programare confirmată!</h3>
            <p>Consultația dumneavoastră a fost programată cu succes. Verificați e-mailul pentru detalii suplimentare.</p>
          </div>
        )}

        {loading ? (
          <SectionLoader message="A carregar intervalele disponibile..." />
        ) : (
          <div className="grid gap-8">
            <CalendarWidget
              availableDays={availableDays}
              onSlotSelect={handleSlotSelect}
              selectedDate={selectedDate || undefined}
              selectedSlot={selectedSlot || undefined}
            />

            {selectedDate && selectedSlot && (
              <div className="animate-in fade-in slide-in-from-bottom-8 duration-500">
                <AppointmentForm
                  selectedDate={selectedDate}
                  selectedSlot={selectedSlot}
                  onSubmit={handleBookingSubmit}
                  isLoading={isSubmitting}
                />
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  );
}
