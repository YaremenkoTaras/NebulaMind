import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "NebulaMind - Arbitrage Scanner",
  description: "Triangular arbitrage opportunity scanner for crypto trading",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en">
      <body className="antialiased">
        {children}
      </body>
    </html>
  );
}
