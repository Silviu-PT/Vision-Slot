import { useState } from "react";
import { format, parseISO } from "date-fns";
import { ro } from "date-fns/locale";
import { User, Mail, Briefcase, CheckCircle2 } from "lucide-react";
import {
  AppointmentCreateRequest,
  ParticipantType,
  BookingSource,
  TimeIntervalResponse,
} from "../types/models";
import { Card, CardContent, CardHeader, CardTitle, CardDescription, CardFooter } from "./ui/card";
import { Input } from "./ui/input";
import { Label } from "./ui/label";
import { Button } from "./ui/button";
import { Select } from "./ui/select";
import { Spinner } from "./ui/spinner";

interface AppointmentFormProps {
  selectedDate: string;
  selectedSlot: TimeIntervalResponse;
  onSubmit: (data: AppointmentCreateRequest) => Promise<void>;
  isLoading: boolean;
}

export function AppointmentForm({ selectedDate, selectedSlot, onSubmit, isLoading }: AppointmentFormProps) {
  const [formData, setFormData] = useState({
    fullName: "",
    email: "",
    employeeCode: "",
    participantType: ParticipantType.EMPLOYEE,
    bookingSource: BookingSource.LINK,
  });

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    await onSubmit({
      ...formData,
      appointmentStart: selectedSlot.start,
    });
  };

  const slotStart = parseISO(selectedSlot.start);
  const slotEnd = parseISO(selectedSlot.end);

  return (
    <Card className="w-full">
      <CardHeader className="border-b pb-4">
        <CardTitle className="flex items-center gap-2 text-lg">
          <CheckCircle2 className="h-5 w-5 text-muted-foreground" />
          Confirmare programare
        </CardTitle>
        <CardDescription>
          Completați datele dumneavoastră pentru a confirma programarea pentru ziua de{" "}
          <span className="font-bold text-foreground">
            {format(parseISO(selectedDate), "d MMMM", { locale: ro })}
          </span>{" "}
          de la{" "}
          <span className="font-bold text-foreground">
            {format(slotStart, "HH:mm")}
          </span>{" "}
          la{" "}
          <span className="font-bold text-foreground">
            {format(slotEnd, "HH:mm")}
          </span>
          .
        </CardDescription>
      </CardHeader>
      
      <form onSubmit={handleSubmit}>
        <CardContent className="space-y-4 pt-6">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div className="space-y-2">
              <Label htmlFor="fullName" className="flex items-center gap-2">
                <User className="h-4 w-4" /> Nume complet
              </Label>
              <Input
                id="fullName"
                placeholder="Ion Popescu"
                required
                value={formData.fullName}
                onChange={(e) => setFormData({ ...formData, fullName: e.target.value })}
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="email" className="flex items-center gap-2">
                <Mail className="h-4 w-4" /> E-mail
              </Label>
              <Input
                id="email"
                type="email"
                placeholder="ion.popescu@exemplu.com"
                required
                value={formData.email}
                onChange={(e) => setFormData({ ...formData, email: e.target.value })}
              />
            </div>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div className="space-y-2">
              <Label htmlFor="employeeCode" className="flex items-center gap-2">
                <Briefcase className="h-4 w-4" /> Cod / Marcă
              </Label>
              <Input
                id="employeeCode"
                placeholder="Ex: EMP-12345"
                required
                value={formData.employeeCode}
                onChange={(e) => setFormData({ ...formData, employeeCode: e.target.value })}
              />
            </div>
            
            <div className="space-y-2">
              <Label htmlFor="participantType">Tip participant</Label>
              <Select
                id="participantType"
                value={formData.participantType}
                onChange={(e) =>
                  setFormData({
                    ...formData,
                    participantType: e.target.value as ParticipantType,
                  })
                }
              >
                <option value={ParticipantType.EMPLOYEE}>Angajat</option>
                <option value={ParticipantType.AGENCY}>Agenție</option>
              </Select>
            </div>
          </div>
        </CardContent>
        <CardFooter className="bg-muted/10 border-t border-border pt-6">
          <Button type="submit" size="lg" className="w-full" disabled={isLoading}>
            {isLoading ? (
              <span className="flex items-center gap-2">
                <Spinner size="sm" className="border-primary-foreground border-t-transparent" />
                Se confirmă...
              </span>
            ) : (
              "Confirmă programarea"
            )}
          </Button>
        </CardFooter>
      </form>
    </Card>
  );
}
