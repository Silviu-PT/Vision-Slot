import { useState } from "react";
import { BrowserRouter, Routes, Route, Link, useLocation } from "react-router-dom";
import { BookingPage } from "./pages/booking-page";
import { AdminConfigPage } from "./pages/admin-config-page";
import { MyAppointmentPage } from "./pages/my-appointment-page";
import { AdminReportsPage } from "./pages/admin-reports-page";
import { Menu, X, Calendar, ClipboardList, Settings, User } from "lucide-react";
import { Button } from "./components/ui/button";

function Navigation() {
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
  const location = useLocation();

  const toggleMenu = () => setIsMobileMenuOpen(!isMobileMenuOpen);
  const closeMenu = () => setIsMobileMenuOpen(false);

  return (
    <header className="border-b bg-white sticky top-0 z-50">
      <div className="flex h-16 items-center px-4 md:px-6 justify-between">
        <div className="flex items-center gap-2 font-semibold">
          <div className="w-6 h-6 rounded-full bg-gradient-to-tr from-green-400 to-blue-500" />
          <span>Vision Slot</span>
        </div>
        
        {/* Desktop Nav */}
        <nav className="hidden md:flex items-center space-x-6">
          <Link 
            to="/" 
            className={`text-sm font-medium flex items-center gap-2 transition-colors hover:text-primary ${location.pathname === '/' ? 'text-primary' : 'text-muted-foreground'}`}
          >
            <Calendar className="h-4 w-4" />
            Programare consultație
          </Link>
          <Link 
            to="/my-appointment" 
            className={`text-sm font-medium flex items-center gap-2 transition-colors hover:text-primary ${location.pathname === '/my-appointment' ? 'text-primary' : 'text-muted-foreground'}`}
          >
            <User className="h-4 w-4" />
            Programarea mea
          </Link>
          <Link 
            to="/admin/reports" 
            className={`text-sm font-medium flex items-center gap-2 transition-colors hover:text-primary ${location.pathname === '/admin/reports' ? 'text-primary' : 'text-muted-foreground'}`}
          >
            <ClipboardList className="h-4 w-4" />
            Rapoarte
          </Link>
          <Link 
            to="/admin/config" 
            className={`text-sm font-medium flex items-center gap-2 transition-colors hover:text-primary ${location.pathname === '/admin/config' ? 'text-primary' : 'text-muted-foreground'}`}
          >
            <Settings className="h-4 w-4" />
            Administrare
          </Link>
        </nav>
        
        {/* Mobile Menu Toggle */}
        <div className="md:hidden">
          <Button variant="ghost" size="icon" onClick={toggleMenu} aria-label="Menu">
            {isMobileMenuOpen ? <X className="h-5 w-5" /> : <Menu className="h-5 w-5" />}
          </Button>
        </div>
      </div>

      {/* Mobile Nav Dropdown */}
      {isMobileMenuOpen && (
        <div className="md:hidden border-t bg-white absolute w-full animate-in slide-in-from-top-2 shadow-md">
          <nav className="flex flex-col p-4 space-y-2">
            <Link 
              to="/" 
              onClick={closeMenu}
              className={`text-sm font-medium flex items-center gap-2 transition-colors p-3 rounded-md ${location.pathname === '/' ? 'bg-primary/5 text-primary' : 'text-muted-foreground hover:bg-muted'}`}
            >
              <Calendar className="h-4 w-4" />
              Programare consultație
            </Link>
            <Link 
              to="/my-appointment" 
              onClick={closeMenu}
              className={`text-sm font-medium flex items-center gap-2 transition-colors p-3 rounded-md ${location.pathname === '/my-appointment' ? 'bg-primary/5 text-primary' : 'text-muted-foreground hover:bg-muted'}`}
            >
              <User className="h-4 w-4" />
              Programarea mea
            </Link>
            <Link 
              to="/admin/reports" 
              onClick={closeMenu}
              className={`text-sm font-medium flex items-center gap-2 transition-colors p-3 rounded-md ${location.pathname === '/admin/reports' ? 'bg-primary/5 text-primary' : 'text-muted-foreground hover:bg-muted'}`}
            >
              <ClipboardList className="h-4 w-4" />
              Rapoarte
            </Link>
            <Link 
              to="/admin/config" 
              onClick={closeMenu}
              className={`text-sm font-medium flex items-center gap-2 transition-colors p-3 rounded-md ${location.pathname === '/admin/config' ? 'bg-primary/5 text-primary' : 'text-muted-foreground hover:bg-muted'}`}
            >
              <Settings className="h-4 w-4" />
              Administrare
            </Link>
          </nav>
        </div>
      )}
    </header>
  );
}

export function App() {
  return (
    <BrowserRouter>
      <div className="min-h-screen bg-background text-foreground flex flex-col font-sans">
        <Navigation />

        {/* Main Content */}
        <main className="flex-1 space-y-4 p-4 sm:p-6 md:p-8 pt-6">
          <Routes>
            <Route path="/" element={<BookingPage />} />
            <Route path="/my-appointment" element={<MyAppointmentPage />} />
            <Route path="/admin/reports" element={<AdminReportsPage />} />
            <Route path="/admin/config" element={<AdminConfigPage />} />
          </Routes>
        </main>
      </div>
    </BrowserRouter>
  );
}

export default App;
