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
                           alt="MeshStorage logo" width={55} height={50}/>
                </div>
                <div className="h4 mb-0">
                    <h2 className="text-xl font-semibold text-gray-800">{title}</h2>
                </div>
            </div>
        </header>
    );
}
