type ApplicationCardProps = {
  name: string;
  icon: string;
  files: number;
  color: string;
};

export default function ApplicationCard({ name, icon, files, color }: ApplicationCardProps) {
  return (
    <div className="card mb-2">
      <div className="card-body d-flex align-items-center">
        <div className={`bg-${color} text-white rounded-circle d-flex align-items-center justify-content-center me-3`}
             style={{ width: 40, height: 40 }}>
          <span className="material-icons">{icon}</span>
        </div>
        <div className="flex-grow-1">
          <h6 className="mb-0">{name}</h6>
          <small className="text-muted">{files} arquivos</small>
        </div>
        <div className="ms-auto d-flex">
          <span className="material-icons me-2 text-muted" role="button">file_upload</span>
          <span className="material-icons text-muted" role="button">delete</span>
        </div>
      </div>
    </div>
  );
}
