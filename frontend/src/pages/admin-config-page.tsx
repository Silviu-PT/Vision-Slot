import { useEffect, useState } from "react";
import { adminConfigApi } from "@/api/api-client";
import { SchedulingConfigurationRequest } from "@/types/models";
import { Save } from "lucide-react";
import { Card, CardContent, CardHeader, CardTitle, CardDescription, CardFooter } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Button } from "@/components/ui/button";
import { PageLoader, Spinner } from "@/components/ui/spinner";

const DAYS_OF_WEEK = [
  { value: "MONDAY", label: "Luni" },
  { value: "TUESDAY", label: "Marți" },
  { value: "WEDNESDAY", label: "Miercuri" },
  { value: "THURSDAY", label: "Joi" },
  { value: "FRIDAY", label: "Vineri" },
  { value: "SATURDAY", label: "Sâmbătă" },
  { value: "SUNDAY", label: "Duminică" },
];

export function AdminConfigPage() {
  const [config, setConfig] = useState<SchedulingConfigurationRequest>({
    consultationDays: [],
    consultationStartTime: "09:00",
    consultationEndTime: "17:00",
    lunchBreakStart: "12:00",
    lunchBreakEnd: "13:00",
    slotDurationMinutes: 30,
    bookingWindowStart: new Date().toISOString().split("T")[0],
    bookingWindowEnd: new Date(new Date().setMonth(new Date().getMonth() + 1)).toISOString().split("T")[0],
  });
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState<{ type: "success" | "error"; text: string } | null>(null);

  useEffect(() => {
    const fetchConfig = async () => {
      try {
        const data = await adminConfigApi.getConfiguration();
        // Fallback to default if there's no data
        if (data && Object.keys(data).length > 0) {
           setConfig(data);
        }
      } catch (error: any) {
        if (error.response?.status !== 404) {
          setMessage({ type: "error", text: "Eroare la încărcarea configurărilor actuale." });
        }
      } finally {
        setLoading(false);
      }
    };
    fetchConfig();
  }, []);

  const handleDayToggle = (day: string) => {
    setConfig((prev) => ({
      ...prev,
      consultationDays: prev.consultationDays.includes(day)
        ? prev.consultationDays.filter((d) => d !== day)
        : [...prev.consultationDays, day],
    }));
  };

  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault();
    setSaving(true);
    setMessage(null);
    try {
      await adminConfigApi.updateConfiguration(config);
      setMessage({ type: "success", text: "Configurările au fost salvate cu succes!" });
    } catch (error: any) {
      setMessage({
        type: "error",
        text: error.response?.data?.message || "Eroare la salvarea configurărilor.",
      });
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return <PageLoader message="A carregar configurările..." />;
  }

  return (
    <div className="flex-1 space-y-8 max-w-5xl mx-auto w-full animate-in fade-in duration-500">
      <div className="flex items-center justify-between space-y-2 mb-6">
        <div>
          <h2 className="text-3xl font-bold tracking-tight">Setări</h2>
          <p className="text-muted-foreground">Configurați regulile de programare ale sistemului</p>
        </div>
      </div>

      <div className="w-full">
        {message && (
          <div
            className={`p-4 mb-6 rounded-lg border ${
              message.type === "success"
                ? "bg-green-500/10 border-green-500/20 text-green-500"
                : "bg-destructive/10 border-destructive/20 text-destructive"
            }`}
          >
            {message.text}
          </div>
        )}

        <Card>
          <form onSubmit={handleSave}>
            <CardHeader className="border-b">
              <CardTitle>Regulile sistemului</CardTitle>
              <CardDescription>
                Definiți programul de lucru, pauzele și durata consultațiilor.
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-8 pt-6">
              
              {/* Dias da Semana */}
              <div className="space-y-4">
                <h3 className="text-lg font-semibold border-b border-border pb-2">Zile de consultație</h3>
                <div className="flex flex-wrap gap-2">
                  {DAYS_OF_WEEK.map((day) => {
                    const isSelected = config.consultationDays.includes(day.value);
                    return (
                      <button
                        key={day.value}
                        type="button"
                        onClick={() => handleDayToggle(day.value)}
                        className={`px-4 py-2 rounded-md text-sm font-medium transition-colors border ${
                          isSelected
                            ? "bg-primary text-primary-foreground border-primary"
                            : "bg-background/50 text-foreground border-border hover:bg-accent"
                        }`}
                      >
                        {day.label}
                      </button>
                    );
                  })}
                </div>
              </div>

              {/* Horários */}
              <div className="space-y-4">
                <h3 className="text-lg font-semibold border-b border-border pb-2">Program de lucru</h3>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  <div className="space-y-2">
                    <Label>Ora de început</Label>
                    <Input
                      type="time"
                      value={config.consultationStartTime}
                      onChange={(e) => setConfig({ ...config, consultationStartTime: e.target.value })}
                      required
                    />
                  </div>
                  <div className="space-y-2">
                    <Label>Ora de sfârșit</Label>
                    <Input
                      type="time"
                      value={config.consultationEndTime}
                      onChange={(e) => setConfig({ ...config, consultationEndTime: e.target.value })}
                      required
                    />
                  </div>
                  <div className="space-y-2">
                    <Label>Început pauză de masă</Label>
                    <Input
                      type="time"
                      value={config.lunchBreakStart || ""}
                      onChange={(e) => setConfig({ ...config, lunchBreakStart: e.target.value })}
                    />
                  </div>
                  <div className="space-y-2">
                    <Label>Sfârșit pauză de masă</Label>
                    <Input
                      type="time"
                      value={config.lunchBreakEnd || ""}
                      onChange={(e) => setConfig({ ...config, lunchBreakEnd: e.target.value })}
                    />
                  </div>
                </div>
              </div>

              {/* Duração e Janela */}
              <div className="space-y-4">
                <h3 className="text-lg font-semibold border-b border-border pb-2">Configurări intervale</h3>
                <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                  <div className="space-y-2">
                    <Label>Durata intervalului (minute)</Label>
                    <Input
                      type="number"
                      min={1}
                      value={config.slotDurationMinutes}
                      onChange={(e) => setConfig({ ...config, slotDurationMinutes: Number(e.target.value) })}
                      required
                    />
                  </div>
                  <div className="space-y-2">
                    <Label>Fereastră (Data de început)</Label>
                    <Input
                      type="date"
                      value={config.bookingWindowStart}
                      onChange={(e) => setConfig({ ...config, bookingWindowStart: e.target.value })}
                      required
                    />
                  </div>
                  <div className="space-y-2">
                    <Label>Fereastră (Data de sfârșit)</Label>
                    <Input
                      type="date"
                      value={config.bookingWindowEnd}
                      onChange={(e) => setConfig({ ...config, bookingWindowEnd: e.target.value })}
                      required
                    />
                  </div>
                </div>
              </div>

            </CardContent>
            <CardFooter className="bg-muted/10 border-t border-border py-4 flex justify-end">
              <Button type="submit" disabled={saving || config.consultationDays.length === 0} size="lg" className="w-full sm:w-auto">
                {saving ? (
                  <span className="flex items-center gap-2">
                    <Spinner size="sm" className="border-background border-t-transparent" />
                    Se salvează...
                  </span>
                ) : (
                  <span className="flex items-center gap-2">
                    <Save className="w-4 h-4" /> Salvează configurările
                  </span>
                )}
              </Button>
            </CardFooter>
          </form>
        </Card>
      </div>
    </div>
  );
}
