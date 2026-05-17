import { useState, useEffect } from "react";
import { reportsApi } from "@/api/api-client";
import { AppointmentReportResponse } from "@/types/models";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Button } from "@/components/ui/button";
import { FileDown, Filter } from "lucide-react";
import { format, parseISO } from "date-fns";
import { Spinner, TableSkeleton } from "@/components/ui/spinner";

export function AdminReportsPage() {
  const [startDate, setStartDate] = useState<string>(format(new Date(), "yyyy-MM-01"));
  const [endDate, setEndDate] = useState<string>(format(new Date(), "yyyy-MM-dd"));
  const [reports, setReports] = useState<AppointmentReportResponse[]>([]);
  const [loading, setLoading] = useState(false);
  const [exporting, setExporting] = useState(false);

  const fetchReports = async () => {
    setLoading(true);
    try {
      const data = await reportsApi.getAppointmentsReport(startDate, endDate);
      setReports(data);
    } catch (error) {
      console.error("Erro ao carregar relatórios:", error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchReports();
  }, []);

  const handleExport = async () => {
    setExporting(true);
    try {
      await reportsApi.exportAppointments(startDate, endDate);
    } catch (error) {
      console.error("Erro ao exportar:", error);
    } finally {
      setExporting(false);
    }
  };

  return (
    <div className="flex-1 space-y-8 max-w-5xl mx-auto w-full animate-in fade-in duration-500">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-3xl font-bold tracking-tight">Rapoarte Programări</h2>
          <p className="text-muted-foreground">Analizați și exportați datele programărilor din sistem.</p>
        </div>
        <Button onClick={handleExport} disabled={exporting || reports.length === 0} variant="outline" className="flex items-center gap-2">
          {exporting ? (
             <Spinner size="sm" />
          ) : (
            <FileDown className="h-4 w-4" />
          )}
          {exporting ? "Se exportă..." : "Exportă în Excel"}
        </Button>
      </div>

      <Card>
        <CardHeader className="pb-3">
          <CardTitle className="text-lg flex items-center gap-2">
            <Filter className="h-4 w-4" />
            Filtre
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4 items-end">
            <div className="space-y-2">
              <Label>Data de început</Label>
              <Input type="date" value={startDate} onChange={(e) => setStartDate(e.target.value)} />
            </div>
            <div className="space-y-2">
              <Label>Data de sfârșit</Label>
              <Input type="date" value={endDate} onChange={(e) => setEndDate(e.target.value)} />
            </div>
            <Button onClick={fetchReports} disabled={loading} className="w-full">
              {loading ? "Se încarcă..." : "Aplică filtrele"}
            </Button>
          </div>
        </CardContent>
      </Card>

      <div className="border rounded-xl overflow-hidden bg-card">
        <div className="overflow-x-auto">
          <table className="w-full text-sm text-left">
            <thead className="bg-muted/50 border-b font-medium text-muted-foreground">
              <tr>
                <th className="px-4 py-3">Data/Ora</th>
                <th className="px-4 py-3">Pacient</th>
                <th className="px-4 py-3">Cod</th>
                <th className="px-4 py-3">Status</th>
                <th className="px-4 py-3">Sursă</th>
              </tr>
            </thead>
            <tbody className="divide-y">
              {loading ? (
                <TableSkeleton rows={5} />
              ) : reports.length === 0 ? (
                <tr>
                  <td colSpan={5} className="px-4 py-8 text-center text-muted-foreground italic">
                    Nu s-a găsit nicio programare pentru perioada selectată.
                  </td>
                </tr>
              ) : (
                reports.map((report, idx) => (
                  <tr key={idx} className="hover:bg-muted/30 transition-colors">
                    <td className="px-4 py-3 whitespace-nowrap">
                      <div className="flex flex-col">
                        <span className="font-medium">{format(parseISO(report.date), "dd/MM/yyyy")}</span>
                        <span className="text-xs text-muted-foreground">{report.startTime} - {report.endTime}</span>
                      </div>
                    </td>
                    <td className="px-4 py-3">
                      <div className="flex flex-col">
                        <span className="font-medium">{report.fullName}</span>
                        <span className="text-xs text-muted-foreground">{report.email}</span>
                      </div>
                    </td>
                    <td className="px-4 py-3 font-mono text-xs">{report.employeeCode}</td>
                    <td className="px-4 py-3">
                      <span className={`px-2 py-0.5 rounded-full text-[10px] font-bold uppercase ${
                        report.status === 'ACTIVE' ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'
                       }`}>
                        {report.status === 'ACTIVE' ? 'ACTIVĂ' : report.status === 'CANCELLED' ? 'ANULATĂ' : report.status}
                      </span>
                    </td>
                    <td className="px-4 py-3 text-xs text-muted-foreground">{report.bookingSource}</td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
