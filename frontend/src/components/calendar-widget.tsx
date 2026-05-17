import { useState } from "react";
import { format, parseISO } from "date-fns";
import { ro } from "date-fns/locale";
import { CalendarIcon, Clock } from "lucide-react";
import { AvailableDaySlotsResponse, TimeIntervalResponse } from "../types/models";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "./ui/card";
import { Button } from "./ui/button";

interface CalendarWidgetProps {
  availableDays: AvailableDaySlotsResponse[];
  onSlotSelect: (date: string, slot: TimeIntervalResponse) => void;
  selectedDate?: string;
  selectedSlot?: TimeIntervalResponse;
}

export function CalendarWidget({
  availableDays,
  onSlotSelect,
  selectedDate,
  selectedSlot,
}: CalendarWidgetProps) {
  const [activeDate, setActiveDate] = useState<string | null>(
    selectedDate || (availableDays.length > 0 ? availableDays[0].date : null)
  );

  const activeDayData = availableDays.find((d) => d.date === activeDate);

  if (!availableDays || availableDays.length === 0) {
    return (
      <Card className="w-full">
        <CardContent className="flex flex-col items-center justify-center py-10">
          <CalendarIcon className="h-10 w-10 text-muted-foreground mb-4" />
          <p className="text-muted-foreground">Nu există zile disponibile pentru programare.</p>
        </CardContent>
      </Card>
    );
  }

  return (
    <Card className="w-full overflow-hidden">
      <CardHeader className="pb-4">
        <CardTitle className="flex items-center gap-2">
          <CalendarIcon className="h-5 w-5 text-muted-foreground" />
          Alegeți o zi
        </CardTitle>
        <CardDescription>
          Selectați data și ora pentru programare
        </CardDescription>
      </CardHeader>
      <CardContent className="p-0">
        <div className="flex overflow-x-auto p-4 gap-3 border-b border-border scrollbar-hide snap-x snap-mandatory">
          {availableDays.map((day) => {
            const dateObj = parseISO(day.date);
            const isSelected = activeDate === day.date;
            const hasSlots = day.slots && day.slots.length > 0;

            return (
              <button
                key={day.date}
                disabled={!hasSlots}
                onClick={() => setActiveDate(day.date)}
                className={`snap-start flex flex-col items-center justify-center min-w-[70px] h-20 rounded-xl transition-all border active:scale-95 ${
                  isSelected
                    ? "bg-primary text-primary-foreground border-primary ring-2 ring-primary ring-offset-2 ring-offset-background"
                    : hasSlots
                    ? "bg-background text-foreground hover:bg-muted border-border"
                    : "bg-muted/50 text-muted-foreground opacity-50 cursor-not-allowed border-transparent"
                }`}
              >
                <span className="text-xs font-medium uppercase mb-1">
                  {format(dateObj, "EEE", { locale: ro })}
                </span>
                <span className="text-xl font-bold">
                  {format(dateObj, "dd", { locale: ro })}
                </span>
                <span className="text-xs">
                  {format(dateObj, "MMM", { locale: ro })}
                </span>
              </button>
            );
          })}
        </div>

        <div className="p-6">
          <h4 className="flex items-center gap-2 font-medium mb-4 text-sm text-muted-foreground">
            <Clock className="h-4 w-4" />
            Ore disponibile
          </h4>
          
          {!activeDayData || !activeDayData.slots || activeDayData.slots.length === 0 ? (
            <div className="text-center py-8 text-muted-foreground bg-muted/20 rounded-lg">
              Nu există intervale disponibile pentru această zi.
            </div>
          ) : (
            <div className="grid grid-cols-3 sm:grid-cols-4 md:grid-cols-6 lg:grid-cols-8 gap-3">
              {activeDayData.slots.map((slot, index) => {
                const startTime = parseISO(slot.start);
                const isSelected =
                  selectedSlot?.start === slot.start && selectedDate === activeDate;

                return (
                  <Button
                    key={index}
                    variant={isSelected ? "default" : "outline"}
                    className={`h-10 transition-all active:scale-95 ${
                      isSelected ? "ring-2 ring-primary ring-offset-2 ring-offset-background" : "hover:border-primary/50"
                    }`}
                    onClick={() => onSlotSelect(activeDate!, slot)}
                  >
                    {format(startTime, "HH:mm")}
                  </Button>
                );
              })}
            </div>
          )}
        </div>
      </CardContent>
    </Card>
  );
}
