import Image from "next/image";

export default function Home() {
    return (
        <div
            className="grid grid-rows-[20px_1fr_20px] items-center justify-items-center min-h-screen p-8 pb-20 gap-16 sm:p-20 font-[family-name:var(--font-geist-sans)]">
            <main className="flex flex-col gap-[32px] row-start-2 items-center sm:items-start">
                <Image
                    className="dark:invert"
                    src="/logoMeshStorage.png"
                    alt="MeshStorage logo"
                    width={450} height={150}
                    priority
                />
                <div className="items-center justify-center">
                    <h1>Arquivos sempre presentes, espaço sempre inteligente.</h1>
                </div>
            </main>
            <footer className="row-start-3 flex gap-[24px] flex-wrap items-center justify-center">
            </footer>
        </div>
    );
}
