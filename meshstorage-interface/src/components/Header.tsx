import Image from "next/image";

interface HeaderProps {
    title: string;
}

export function Header({title}: HeaderProps) {
    return (
        <header className="bg-white shadow">
            <div className="container d-flex align-items-center px-2 py-2">
                <div>
                    <Image className="dark:invert me-2" src="/logo-smart.png"
                           alt="MeshStorage logo" width={45} height={40}/>
                </div>
                <div className="mb-0">
                    <h2 className="text-xl font-semibold text-gray-800">{title}</h2>
                </div>
                <div className="ms-auto">
                    <span className="material-icons" style={{fontSize: '2.0rem'}}>account_circle</span>
                </div>
            </div>
        </header>
    );
}