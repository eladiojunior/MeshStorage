import { cva, type VariantProps } from "class-variance-authority";
import { cn } from "@/lib/utils";

const statusCardVariants = cva(
  "bg-white rounded-lg shadow p-4",
  {
    variants: {
      status: {
        default: "",
        success: "",
        warning: "",
        danger: "",
      }
    },
    defaultVariants: {
      status: "default"
    }
  }
);

type StatusIconProps = {
  icon: string;
  className?: string;
};

const StatusIcon = ({ icon, className }: StatusIconProps) => (
  <span className={cn("material-icons", className)}>{icon}</span>
);

export interface StatusCardProps extends VariantProps<typeof statusCardVariants> {
  title: string;
  value: string | number;
  icon: string;
  iconColor?: string;
  suffix?: string;
  progress?: number;
  progressLabel?: string;
  metricLeft?: string;
  metricRight?: string;
  className?: string;
}

export function StatusCard({
  title,
  value,
  icon,
  iconColor = "text-primary",
  suffix,
  progress,
  progressLabel,
  metricLeft,
  metricRight,
  status,
  className,
}: StatusCardProps) {
  return (
    <div className={cn(statusCardVariants({ status }), className)}>
      <div className="flex items-center justify-between mb-2">
        <h3 className="text-sm font-medium text-gray-500">{title}</h3>
        <StatusIcon icon={icon} className={iconColor} />
      </div>
      <div className="flex items-baseline">
        <p className={cn(
          "text-2xl font-semibold",
          status === "success" && "text-success",
          status === "warning" && "text-warning",
          status === "danger" && "text-danger"
        )}>
          {value}
        </p>
        {suffix && <p className="ml-2 text-sm text-gray-500">{suffix}</p>}
      </div>
      
      {progress !== undefined && (
        <div className="mt-2">
          <div className="w-full bg-gray-200 rounded-full h-2">
            <div 
              className={cn(
                "rounded-full h-2",
                status === "warning" ? "bg-warning" : 
                status === "danger" ? "bg-danger" : 
                status === "success" ? "bg-success" : 
                "bg-primary"
              )} 
              style={{ width: `${progress}%` }}
            ></div>
          </div>
          {progressLabel && (
            <p className="text-xs text-gray-500 mt-1">{progressLabel}</p>
          )}
        </div>
      )}
      
      {(metricLeft || metricRight) && (
        <div className="flex justify-between mt-2 text-xs text-gray-500">
          {metricLeft && <p>{metricLeft}</p>}
          {metricRight && <p>{metricRight}</p>}
        </div>
      )}
    </div>
  );
}
