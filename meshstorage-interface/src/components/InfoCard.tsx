type InfoCardProps = {
  title: string;
  value: string | number;
  icon: string;
  status?: 'success' | 'warning' | 'danger' | 'primary';
};

export default function InfoCard({ title, value, icon, status = 'primary' }: InfoCardProps) {
  const statusClasses = {
    primary: 'border-primary',
    success: 'border-success',
    warning: 'border-warning',
    danger: 'border-danger'
  };
  const iconClasses = {
    primary: 'text-primary',
    success: 'text-success',
    warning: 'text-warning',
    danger: 'text-danger'
  };
  return (
      <div className={`card metric-card ${statusClasses[status]} border-start border-4`}>
        <div className="card-body">
          <div className="d-flex align-items-center justify-content-between">
            <div>
              <p className="card-text text-muted small">{title}</p>
              <h3 className="card-title h2 mb-0">{value}</h3>
            </div>
            <div className={`rounded-circle p-2 div-with-icon bg-light ${iconClasses[status]}`}>
              <span className="material-icons" style={{fontSize: '2.0rem'}}>{icon}</span>
            </div>
          </div>
        </div>
      </div>
  );
}