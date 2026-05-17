# Vision Slot — Frontend

React + TypeScript + Vite frontend for the Vision Slot scheduling system.

## Prerequisites

Make sure you have the following installed before getting started:

- [Node.js](https://nodejs.org/) **v18 or higher**
- **npm** (comes with Node.js)
- The **Vision Slot backend** running on `http://localhost:8080` (see backend README)

---

## Getting Started

### 1. Navigate to the frontend directory

```bash
cd Vision-Slot/frontend
```

### 2. Install dependencies

```bash
npm install
```

### 3. Start the development server

```bash
npm run dev
```

The app will be available at **http://localhost:5173**

> All requests to `/api/*` are automatically proxied to `http://localhost:8080` — no CORS issues, no extra configuration needed.

---

## Available Scripts

| Command | Description |
|---|---|
| `npm run dev` | Start the development server with hot reload |
| `npm run build` | Build the production bundle to `dist/` |
| `npm run preview` | Preview the production build locally |
| `npm run lint` | Run ESLint to check for code issues |

---

## Project Structure

```
src/
├── api/
│   └── api-client.ts        # Axios client + all API calls
├── components/
│   ├── ui/                  # Reusable UI components (Button, Card, Spinner...)
│   ├── appointment-form.tsx # Booking form component
│   └── calendar-widget.tsx  # Date & time slot picker
├── pages/
│   ├── booking-page.tsx         # Home — make a new appointment
│   ├── my-appointment-page.tsx  # View, reschedule or cancel appointment
│   ├── admin-reports-page.tsx   # Admin — view and export reports
│   └── admin-config-page.tsx    # Admin — configure scheduling rules
├── types/
│   └── models.ts            # TypeScript interfaces matching backend DTOs
└── App.tsx                  # Routes and navigation
```

---

## Pages & Routes

| Route | Page | Description |
|---|---|---|
| `/` | Booking | Select a date/time and make an appointment |
| `/my-appointment` | My Appointment | Look up, reschedule or cancel an appointment by employee code |
| `/admin/reports` | Reports | View and export appointments to Excel |
| `/admin/config` | Configuration | Set working hours, slot duration and booking window |

---

## Backend Requirement

The frontend **requires the backend to be running** before use. Make sure to:

1. Start the Spring Boot backend with the `dev` profile active
2. Confirm the backend is listening on port `8080`

The Vite proxy in `vite.config.ts` handles all API routing automatically during development.

---

## Tech Stack

- **React 19** + **TypeScript**
- **Vite 8** (dev server + bundler)
- **Tailwind CSS v4**
- **shadcn/ui** components
- **Axios** for HTTP requests
- **React Router v7** for navigation
- **date-fns** for date formatting
- **Lucide React** for icons
