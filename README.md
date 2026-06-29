# G11 Videojuegos - Plataforma de Torneos

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.1.2-brightgreen.svg)
![React](https://img.shields.io/badge/React-18-blue.svg)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue.svg)
![Vite](https://img.shields.io/badge/Vite-5-purple.svg)
![Stripe](https://img.shields.io/badge/Stripe-Integration-blueviolet.svg)

**G11 Videojuegos** es una plataforma diseñada para la gestión, organización y participación en torneos de esports. Ha sido diseñada con una arquitectura robusta, permite a los jugadores registrarse, competir en múltiples formatos, escalar en el ranking global mediante un sistema de ELO, y a los organizadores gestionar los brackets, resultados y premios de diferentes tipos, existiendo también premios monetarios respaldados por Stripe.

---

## Características Principales

### Gestión de Torneos y Formatos
- **Múltiples Formatos Soportados:** Generación automática de brackets para *Eliminación Simple*, *Doble Eliminación*, *Round Robin* y *Swiss*.
- **Ciclo de Vida del Torneo:** Transiciones controladas de estado (`DRAFT` → `REGISTRATION_OPEN` → `REGISTRATION_CLOSED` → `IN_PROGRESS` → `COMPLETED` / `CANCELLED`).
- **Sistema de Seeding:** Soporte para sieeding por ELO y distribución local.
- **Premios:** Soporte para premios mixtos (Dinero `CASH`, Puntos Virtuales `POINTS`, Objetos físicos `ITEM`, y otros tipos de premios `OTHER`).
- **Integración con Stripe:** Financiación automática de premios en efectivo. Un torneo con premios monetarios no puede publicarse hasta que los fondos son asegurados mediante *Stripe Checkout*.

### Gestión de Partidos y Resultados
- **Ciclo de Vida de Partidas:** Estados precisos (`SCHEDULED` → `IN_PROGRESS` → `RESULT_SUBMITTED` → `COMPLETED` o `DISPUTED`).
- **Reporte de Resultados:** Sistema de reporte de resultados con soporte para adjuntar evidencias.
- **Resolución de Disputas:** Herramientas para administradores para intervenir en partidos en conflicto (`DISPUTED`).
- **Avance Automático:** Los ganadores avanzan dinámicamente en el bracket según las reglas de cada motor de formato.

### Ranking y Estadísticas de Jugadores
- **Cálculo de ELO:** Algoritmo dinámico que ajusta el ELO de los jugadores tras la finalización de los partidos.
- **Panel de Estadísticas:** Historial completo de victorias, derrotas, porcentaje de victorias (Winrate) y puntos virtuales.
- **Notificaciones:** Sistema de notificaciones en tiempo real para eventos del torneo.

### Seguridad
- **Autenticación JWT:** Sesiones *stateless* y control de accesos basado en roles (`ADMIN`, `ORGANIZER`, `PLAYER`).
- **Contexto de Seguridad:** Inyección de contexto de usuario autenticado en cada petición REST.

---

## Arquitectura del Proyecto

El proyecto está dividido en dos partes principales, contenidas en este monorepo:

1. **/tournament (Backend):** 
   - Desarrollado en **Java 17** y **Spring Boot 3**.
   - Conexión a base de datos **PostgreSQL**.
   - ORM administrado mediante **Hibernate / Spring Data JPA**.
   - Configurado con pool de conexiones optimizado (HikariCP) y gestor de caché.

2. **/frontend (Frontend):** 
   - SPA desarrollada con **React.js** y **Vite**.
   - Estilado completo utilizando **TailwindCSS**.
   - Consumo de API manejado con **Axios** y caché de UI optimizada mediante **React Query**.

---

## Ejecutar el proyecto

### Prerrequisitos
- **Java 17** o superior.
- **Node.js 18** o superior.
- **PostgreSQL 15** corriendo de fondo.
- Una cuenta de **Stripe** para obtener llaves de desarrollo (opcional pero recomendado).

### 1. Configuración del Backend (Spring Boot)

1. Dirígete a la carpeta del backend:
   ```bash
   cd tournament
   ```
2. Configura tus variables de entorno locales o ajusta el archivo `src/main/resources/application-local.yaml`:
   - `DB_URL`: JDBC url de tu base de datos (ej. `jdbc:postgresql://localhost:5432/torneos`)
   - `DB_USERNAME` y `DB_PASSWORD`: Credenciales de tu BD.
   - `JWT_SECRET`: Clave secreta para firmar los tokens.
   - `STRIPE_API_KEY`: Tu Secret Key de Stripe.
3. Compila y ejecuta el servidor de Spring Boot usando Maven Wrapper:
   ```bash
   ./mvnw clean compile spring-boot:run -Dspring-boot.run.profiles=local
   ```
   > El backend estará escuchando peticiones en **http://localhost:8080**.

### 2. Configuración del Frontend (React + Vite)

1. En una nueva terminal, dirígete a la carpeta del frontend:
   ```bash
   cd frontend
   ```
2. Instala las dependencias de Node:
   ```bash
   npm install
   ```
3. Configura el archivo `.env.local` (si es necesario) apuntando a la API de tu backend local:
   ```env
   VITE_API_URL=http://localhost:8080
   ```
4. Ejecuta el servidor de desarrollo de Vite:
   ```bash
   npm run dev
   ```
   > El frontend estará disponible en **http://localhost:5173**.

---

## Resumen de la API

El backend expone una arquitectura REST dividida en controladores específicos:

- **Auth:** `/api/v1/auth/*` (Login, Registro, Refresh Tokens)
- **Torneos:** `/api/v1/tournaments/*` (CRUD de Torneos, Publicación, Cierre de Registro)
  - `/api/v1/tournaments/{id}/brackets`: Obtiene la estructura generada del bracket.
  - `/api/v1/tournaments/{id}/brackets/generate`: Dispara el motor algorítmico para armar las rondas.
- **Partidos:** `/api/v1/matches/*` (Reporte de resultados, actualización de estado, resolución de disputas).
- **Stripe:** `/api/v1/stripe/*` (Endpoint para fondear el checkout y recibir Webhooks de pago).
- **Estadísticas:** `/api/v1/stats/*` (Leaderboards, perfil de jugadores y ranking ELO).
- **Formatos:** `/api/v1/formats/*` (Catálogo activo de motores de torneo disponibles).
- **Notificaciones:** `/api/v1/notifications/*` (Listado y actualización de notificaciones del usuario).

*Para un listado detallado de DTO y validaciones, se recomienda importar la Colección de Postman ubicada en los archivos del proyecto.*

---

## Enlaces Importantes

- [**Tablero del proyecto en Jira**](https://grandeclaudia.atlassian.net/jira/software/projects/PNC/boards/35?atlOrigin=eyJpIjoiNWQ5NDI3NDhmNThkNDQ1M2I0MjRhMWYxYzZjNmNjMzEiLCJwIjoiaiJ9)
- [**Manual de usuario**](https://drive.google.com/file/d/1JlwazV3ThyeytbKt_RAK5cNV_X0VIXKS/view?usp=sharing)
- [**Diseño de la BD**](https://docs.google.com/document/d/1_uGAAtaexhuYHqKH7rj7ZQmuLQKu84kkZu9piolW4F8/edit?usp=drive_link)
- [**Reporte de aportes individuales**](https://ucaedusv-my.sharepoint.com/:w:/g/personal/00139622_uca_edu_sv/IQCfTzHBdk9zSJdFaCv-gHXgARJ5tFjUQZW8-b9qMNQW9wA?e=xyBcIe)

---

## Despliegue
La aplicación actualmente cuenta con despliegue en la nube:
- **Backend:** Railway (con PostgreSQL integrado)
- **Frontend:** Vercel
