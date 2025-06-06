import type {Metadata} from "next";
import 'bootstrap/dist/css/bootstrap.min.css';
import {Header} from "@/components/dashboard/Header";

export const metadata: Metadata = {
    title: "MeshStorage",
    description: "Interface da solução MeshStorage",
    applicationName: "MeshStorage",
    authors: {
        name: "Eladio Lima Magalhães Júnior",
        url: "https://github.com/eladiojunior"
    }
};

export default function RootLayout({ children }: Readonly<{ children: React.ReactNode }>) {
    return (
        <html lang="pt-br">
            <body>
                <Header title={"MeshStorage"}/>
                <main className="container">{children}</main>
            </body>
        </html>
    );
}