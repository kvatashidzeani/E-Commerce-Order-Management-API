# Frontend (React + TypeScript + Vite)

SPA for the E-Commerce Order Management API.

## Stack

Vite, React 19, TypeScript (strict), Tailwind CSS, React Router, TanStack Query, Axios + JWT interceptor, React Hook Form + Zod, Sonner.

## Run locally

1. Start backend on `http://localhost:8081`
2. In `frontend/`:

```bash
cp .env.example .env
npm install
npm run dev
```

Open http://localhost:5173

> Port **3000** is often used by Grafana on this machine, so the shop UI runs on **5173**.

## Scripts

| Script | Purpose |
|---|---|
| `npm run dev` | Dev server (:3000) |
| `npm run build` | Production build |
| `npm run lint` | ESLint |
| `npm test` | Vitest |
| `npm run generate:api` | Generate OpenAPI types from backend |

## Auth note

JWT is stored in `localStorage` for demo simplicity. Production should prefer httpOnly cookies to reduce XSS risk.

## Demo accounts (backend)

- Admin: `admin@ecommerce.local` / `Admin123!`
- Customer: register via UI
