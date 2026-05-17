import { useState } from "react";
import { appointmentsApi, slotsApi } from "@/api/api-client";
import { AppointmentResponse, TimeIntervalResponse, AvailableDaySlotsResponse } from "@/types/models";
import { Card, CardContent, CardHeader, CardTitle, CardDescription, CardFooter } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Search, Calendar, Trash2, ArrowRight, CheckCircle2, AlertCircle } from "lucide-react";
import { CalendarWidget } from "@/components/calendar-widget";
import { format, parseISO } from "date-fns";
import { ro } from "date-fns/locale";

export function MyAppointmentPage() {
  const [employeeCode, setEmployeeCode] = useState("");
  const [appointment, setAppointment] = useState<AppointmentResponse | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  
  const [isRescheduling, setIsRescheduling] = useState(false);
  const [availableDays, setAvailableDays] = useState<AvailableDaySlotsResponse[]>([]);
  const [selectedDate, setSelectedDate] = useState<string | undefined>();
  const [selectedSlot, setSelectedSlot] = useState<TimeIntervalResponse | undefined>();
  const [rescheduleLoading, setRescheduleLoading] = useState(false);

  const handleSearch = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!employeeCode) return;

    setLoading(true);
    setError(null);
    setAppointment(null);
    setSuccess(null);
    setIsRescheduling(false);

    try {
      const data = await appointmentsApi.getAppointmentsByEmployee(employeeCode);
      if (data && data.length > 0) {
        // Assume the first one is the active one for simplicity, or find the active one
        setAppointment(data[0]);
      } else {
        setError("Nu s-a găsit nicio programare activă pentru acest cod.");
      }
    } catch (err: any) {
      setError("Eroare la căutarea programării. Verificați codul și încercați din nou.");
    } finally {
      setLoading(false);
    }
  };

  const handleCancel = async () => {
    if (!appointment) return;
    if (!confirm("Sigur doriți să anulați această programare?")) return;

    setLoading(true);
    try {
      await appointmentsApi.cancelAppointment(appointment.id);
      setSuccess("Programarea a fost anulată cu succes.");
      setAppointment(null);
    } catch (err: any) {
      setError("Eroare la anularea programării.");
    } finally {
      setLoading(false);
    }
  };

  const startRescheduling = async () => {
    setIsRescheduling(true);
    try {
      const days = await slotsApi.getAvailableSlots();
      setAvailableDays(days);
    } catch (err) {
      setError("Eroare la încărcarea intervalelor disponibile.");
    }
  };

  const handleReschedule = async () => {
    if (!appointment || !selectedSlot) return;

    setRescheduleLoading(true);
    try {
      await appointmentsApi.rescheduleAppointment(appointment.id, {
        newAppointmentStart: selectedSlot.start
      });
      setSuccess("Programarea a fost reprogramată cu succes!");
      setIsRescheduling(false);
      // Refresh appointment data
      const data = await appointmentsApi.getAppointmentsByEmployee(employeeCode);
      setAppointment(data[0]);
    } catch (err) {
      setError("Eroare la reprogramare. Încercați din nou.");
    } finally {
      setRescheduleLoading(false);
    }
  };

  return (
    <div className="flex-1 space-y-8 max-w-5xl mx-auto w-full animate-in fade-in duration-500">
      <div className="flex flex-col space-y-2">
        <h2 className="text-3xl font-bold tracking-tight">Programarea mea</h2>
        <p className="text-muted-foreground">
          Consultați, reprogramați sau anulați programarea dumneavoastră activă.
        </p>
      </div>

      <Card className="w-full">
        <CardHeader>
          <CardTitle>Caută programare</CardTitle>
          <CardDescription>Introduceți codul de angajat pentru a vă consulta programarea.</CardDescription>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSearch} className="flex gap-4">
            <div className="flex-1">
              <Input 
                placeholder="Ex: EMP-12345" 
                value={employeeCode}
                onChange={(e) => setEmployeeCode(e.target.value)}
                required
              />
            </div>
            <Button type="submit" disabled={loading}>
              <Search className="mr-2 h-4 w-4" />
              {loading ? "Se caută..." : "Caută"}
            </Button>
          </form>
        </CardContent>
      </Card>

      {error && (
        <div className="p-4 bg-destructive/10 text-destructive border border-destructive/20 rounded-lg flex items-center gap-3">
          <AlertCircle className="h-5 w-5" />
          {error}
        </div>
      )}

      {success && (
        <div className="p-4 bg-green-500/10 text-green-500 border border-green-500/20 rounded-lg flex items-center gap-3">
          <CheckCircle2 className="h-5 w-5" />
          {success}
        </div>
      )}

      {appointment && !isRescheduling && (
        <Card className="w-full animate-in slide-in-from-bottom-4 duration-500">
          <CardHeader className="bg-primary/5">
            <div className="flex justify-between items-start">
              <div>
                <CardTitle>Detalii programare</CardTitle>
                <CardDescription>Informații despre programarea dumneavoastră curentă.</CardDescription>
              </div>
              <div className="bg-primary/10 text-primary px-3 py-1 rounded-full text-sm font-medium">
                {appointment.status === 'ACTIVE' ? 'ACTIVĂ' : appointment.status === 'CANCELLED' ? 'ANULATĂ' : appointment.status}
              </div>
            </div>
          </CardHeader>
          <CardContent className="pt-6 space-y-6">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div className="space-y-1">
                <p className="text-sm text-muted-foreground">Data și ora</p>
                <p className="text-lg font-semibold flex items-center gap-2">
                  <Calendar className="h-4 w-4 text-primary" />
                  {format(parseISO(appointment.appointmentStart), "EEEE, d MMMM 'la ora' HH:mm", { locale: ro })}
                </p>
              </div>
              <div className="space-y-1">
                <p className="text-sm text-muted-foreground">Numele pacientului</p>
                <p className="text-lg font-semibold">{appointment.fullName}</p>
              </div>
              <div className="space-y-1">
                <p className="text-sm text-muted-foreground">Email</p>
                <p className="text-md">{appointment.email}</p>
              </div>
              <div className="space-y-1">
                <p className="text-sm text-muted-foreground">ID programare</p>
                <p className="text-xs font-mono text-muted-foreground">{appointment.id}</p>
              </div>
            </div>
          </CardContent>
          <CardFooter className="flex gap-3 bg-muted/5 border-t border-border pt-6">
            <Button variant="outline" size="lg" className="flex-1" onClick={startRescheduling}>
              <ArrowRight className="mr-2 h-4 w-4" />
              Reprogravează
            </Button>
            <Button variant="destructive" size="lg" className="flex-1" onClick={handleCancel}>
              <Trash2 className="mr-2 h-4 w-4" />
              Anulează programarea
            </Button>
          </CardFooter>
        </Card>
      )}

      {isRescheduling && (
        <div className="space-y-6 animate-in slide-in-from-bottom-4 duration-500">
          <div className="flex items-center justify-between">
            <h3 className="text-xl font-bold">Selectați un nou interval</h3>
            <Button variant="ghost" onClick={() => setIsRescheduling(false)}>Anulează reprogramarea</Button>
          </div>
          
          <CalendarWidget 
            availableDays={availableDays}
            onSlotSelect={(date, slot) => {
              setSelectedDate(date);
              setSelectedSlot(slot);
            }}
            selectedDate={selectedDate}
            selectedSlot={selectedSlot}
          />

          {selectedSlot && (
            <div className="p-6 bg-primary/5 rounded-xl border border-primary/20 flex flex-col md:flex-row items-center justify-between gap-4">
              <div>
                <p className="text-sm text-muted-foreground font-medium">Noul interval selectat:</p>
                <p className="text-lg font-bold">
                  {format(parseISO(selectedSlot.start), "d MMMM 'la ora' HH:mm", { locale: ro })}
                </p>
              </div>
              <Button size="lg" className="w-full md:w-auto px-8" onClick={handleReschedule} disabled={rescheduleLoading}>
                {rescheduleLoading ? "Se procesează..." : "Confirmă noul interval"}
              </Button>
            </div>
          )}
        </div>
      )}
    </div>
  );
}
