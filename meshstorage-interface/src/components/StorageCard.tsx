type StorageCardProps = {
    name: string;
    ip: string;
    used: string;
    usage: number;
    status: 'active' | 'warning';
};

export default function StorageCard({name, ip, used, usage, status}: StorageCardProps) {
    const color = status === 'warning' ? 'warning' : 'primary';
    return (
        <div className="card mb-3">
            <div className="card-body">
                <div className="d-flex justify-content-between">
                    <div>
                        <h6 className="mb-0">{name}</h6>
                        <small className="text-muted">{ip}</small>
                    </div>
                    <span className={`badge bg-${color}`}>{status}</span>
                </div>
                <div className="mt-2">
                    <small>{used} usados</small>
                    <div className="progress mt-1" style={{height: '6px'}}>
                        <div
                            className={`progress-bar bg-${color}`}
                            role="progressbar"
                            style={{width: `${usage}%`}}
                        />
                    </div>
                </div>
            </div>
        </div>
    );
}
