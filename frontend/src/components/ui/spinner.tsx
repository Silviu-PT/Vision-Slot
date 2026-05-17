import { cn } from "@/lib/utils";

// ── Inline Spinner (para botões e ações pequenas) ──────────────────────────
interface SpinnerProps {
  className?: string;
  size?: "sm" | "md" | "lg";
}

const sizeMap = {
  sm: "h-4 w-4 border-2",
  md: "h-6 w-6 border-2",
  lg: "h-10 w-10 border-[3px]",
};

export function Spinner({ className, size = "md" }: SpinnerProps) {
  return (
    <span
      className={cn(
        "inline-block rounded-full border-current border-t-transparent animate-spin",
        sizeMap[size],
        className
      )}
      role="status"
      aria-label="Se încarcă..."
    />
  );
}

// ── Page Loader (ecrã inteiro, para carregamentos iniciais) ────────────────
interface PageLoaderProps {
  message?: string;
}

export function PageLoader({ message = "Se încarcă..." }: PageLoaderProps) {
  return (
    <div className="fixed inset-0 z-50 flex flex-col items-center justify-center bg-background/80 backdrop-blur-sm">
      <div className="flex flex-col items-center gap-4">
        {/* Logo mark animado */}
        <div className="relative flex items-center justify-center">
          {/* Anel exterior pulsante */}
          <span className="absolute h-16 w-16 rounded-full bg-primary/10 animate-ping" />
          {/* Anel spinner */}
          <span className="relative h-12 w-12 rounded-full border-[3px] border-primary/20 border-t-primary animate-spin" />
          {/* Ponto central */}
          <span className="absolute h-3 w-3 rounded-full bg-primary" />
        </div>
        <p className="text-sm font-medium text-muted-foreground tracking-wide">
          {message}
        </p>
      </div>
    </div>
  );
}

// ── Section Loader (para áreas de conteúdo dentro de uma página) ───────────
interface SectionLoaderProps {
  message?: string;
  className?: string;
}

export function SectionLoader({ message, className }: SectionLoaderProps) {
  return (
    <div className={cn("flex flex-col items-center justify-center py-16 gap-4", className)}>
      <div className="relative flex items-center justify-center">
        <span className="absolute h-12 w-12 rounded-full bg-primary/10 animate-ping opacity-60" />
        <span className="relative h-9 w-9 rounded-full border-[3px] border-primary/20 border-t-primary animate-spin" />
      </div>
      {message && (
        <p className="text-sm text-muted-foreground">{message}</p>
      )}
    </div>
  );
}

// ── Skeleton Loader (para tabelas e listas) ────────────────────────────────
interface SkeletonProps {
  className?: string;
}

export function Skeleton({ className }: SkeletonProps) {
  return (
    <div
      className={cn(
        "animate-pulse rounded-md bg-muted/60",
        className
      )}
    />
  );
}

export function TableSkeleton({ rows = 5 }: { rows?: number }) {
  return (
    <>
      {Array.from({ length: rows }).map((_, i) => (
        <tr key={i} className="border-b">
          <td className="px-4 py-3">
            <div className="flex flex-col gap-1.5">
              <Skeleton className="h-4 w-24" />
              <Skeleton className="h-3 w-16" />
            </div>
          </td>
          <td className="px-4 py-3">
            <div className="flex flex-col gap-1.5">
              <Skeleton className="h-4 w-32" />
              <Skeleton className="h-3 w-28" />
            </div>
          </td>
          <td className="px-4 py-3">
            <Skeleton className="h-4 w-20" />
          </td>
          <td className="px-4 py-3">
            <Skeleton className="h-5 w-16 rounded-full" />
          </td>
          <td className="px-4 py-3">
            <Skeleton className="h-4 w-14" />
          </td>
        </tr>
      ))}
    </>
  );
}
