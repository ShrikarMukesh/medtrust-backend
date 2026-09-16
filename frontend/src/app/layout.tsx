import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "MedTrust Dashboard",
  description: "Healthcare SaaS administration dashboard — manage patients, appointments, clinical encounters, and compliance.",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en">
      <body>{children}</body>
    </html>
  );
}
