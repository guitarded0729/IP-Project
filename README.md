# Smart Campus Lost & Found Management System

A production-grade, college-level full-stack web application designed for campus lost and found management. Built purely in core **Java 8 style** (runnable on JDK 11+) without any third-party frameworks or libraries, coupled with a responsive, modern **Vanilla HTML/CSS/JavaScript** frontend.

---

## 📌 Project Overview

Misplacing personal belongings (ID cards, calculators, laptops, keys, wallets, water bottles) is a daily issue on university campuses. The **Smart Campus Lost & Found Management System** streamlines reporting, tracking, matching, and verified claiming across campus:

- **Unified Reporting**: Report LOST or FOUND items using a single intuitive form with real-time field validation.
- **Browse & Filter**: Browse campus records with live keyword search and multi-criteria filters (Type, Category, Status).
- **Smart Match Algorithm**: Automatically calculates match confidence scores (0-100%) between LOST and FOUND items using category similarity, campus location matching, and keyword tokenization.
- **Ownership Verification**: Allows campus members to submit formal claims with ownership proof against found items.
- **Admin Portal**: Secure administrator dashboard (`admin` / `password123`) to review, approve, or reject claims, change item statuses, and delete records.
- **Live Statistics Dashboard**: Real-time counter metrics for Total Lost, Total Found, Pending Claims, and Returned Items.

---

## 🛠️ Technology Stack & Constraints Adherence

| Layer | Technology | Details |
| :--- | :--- | :--- |
| **Backend** | Core Java (Java 8 style, JDK 11+ compatible) | Built-in `com.sun.net.httpserver.HttpServer`. No Spring, Servlets, Maven, Gradle, or external JARs. |
| **Frontend** | HTML5, Vanilla CSS3, Vanilla JavaScript | 3 files: `index.html`, `style.css`, `script.js`. Offline-ready, system fonts only, zero CDNs. |
| **Storage** | In-Memory Collections + Java File I/O | `ArrayList`, `HashMap`, and persistent storage in `data/items.txt` and `data/claims.txt` with `BufferedReader`/`BufferedWriter`. |
| **API Format**| RESTful JSON | Manual serialization via `JsonUtil`, request parsing via `HttpUtil`. |

---

## 🚀 How to Run the Project

### Prerequisites
- **Java Development Kit (JDK 11 or higher)** installed and available on your system `PATH`.
- Check your installation by opening a terminal and typing:
  ```bash
  javac -version
  java -version
  ```

### Option 1: On Windows (Quick Launch)
Double-click `run.bat` or run it from Command Prompt / PowerShell:
```cmd
run.bat
```
This will compile all backend Java files into an `out/` directory and launch the server on port `8080`.

### Option 2: On Linux / macOS
Make `run.sh` executable and run it:
```bash
chmod +x run.sh
./run.sh
```

### Option 3: Manual Compilation & Execution
From the project root directory (`SmartCampusLostFound/`):
```bash
# 1. Create output folder
mkdir out

# 2. Compile Java sources
javac -encoding UTF-8 -d out backend/src/exception/*.java backend/src/model/*.java backend/src/service/*.java backend/src/util/*.java backend/src/handler/*.java backend/src/Main.java

# 3. Start the server
java -cp out Main
```

### Accessing the Web Application
Open your web browser and navigate to:
```
http://localhost:8080
```

### Default Admin Credentials
- **Username**: `admin`
- **Password**: `password123`
*(Defined as constants in `Admin.java`)*

---

## 📁 Project Directory Structure

```
SmartCampusLostFound/
├── backend/
│   └── src/
│       ├── Main.java                          # Server startup & route registration
│       ├── model/
│       │   ├── Item.java                      # Abstract base item (Encapsulation, Composition)
│       │   ├── LostItem.java                  # Extends Item (Inheritance, Polymorphism)
│       │   ├── FoundItem.java                 # Extends Item (Inheritance, Polymorphism)
│       │   ├── User.java                      # Abstract user base (Encapsulation, Abstraction)
│       │   ├── Student.java                   # Extends User (Inheritance, Polymorphism)
│       │   ├── Staff.java                     # Extends User (Inheritance, Polymorphism)
│       │   ├── Admin.java                     # Extends User with static final credentials
│       │   ├── Claim.java                     # Claim model referencing Item
│       │   ├── ItemStatus.java                # Enum: OPEN, CLAIM_PENDING, RETURNED
│       │   └── Category.java                  # Enum: Campus item categories
│       ├── service/
│       │   ├── ItemOperations.java            # Interface contract (Abstraction, Overloading)
│       │   ├── ItemService.java               # Implements ItemOperations, Smart Match logic
│       │   ├── ClaimService.java              # Manages claims and status transitions
│       │   └── AuthService.java               # Token-based admin session manager (HashMap)
│       ├── handler/
│       │   ├── ItemHandler.java               # Handles /api/items endpoints
│       │   ├── ClaimHandler.java              # Handles /api/claims endpoints
│       │   ├── AuthHandler.java               # Handles /api/login and /api/logout
│       │   ├── StatsHandler.java              # Handles /api/stats live metrics
│       │   └── StaticFileHandler.java         # Serves index.html, style.css, script.js
│       ├── exception/
│       │   ├── InvalidInputException.java     # HTTP 400 Bad Request
│       │   ├── ItemNotFoundException.java     # HTTP 404 Not Found
│       │   └── UnauthorizedException.java     # HTTP 401 Unauthorized
│       └── util/
│           ├── FileStorage.java               # Persistent File I/O (BufferedReader/BufferedWriter)
│           ├── JsonUtil.java                  # Manual JSON escaping and array formatting
│           └── HttpUtil.java                  # Form parsing, CORS, and response dispatcher
├── frontend/
│   ├── index.html                             # Semantic single-page HTML layout
│   ├── style.css                              # Modern Vanilla CSS (Flexbox/Grid, Color Badges)
│   └── script.js                              # Vanilla JS (State, Fetch API, DOM rendering)
├── data/
│   ├── items.txt                              # Pipe-delimited persistent items store
│   └── claims.txt                             # Pipe-delimited persistent claims store
├── run.bat                                    # Windows build & launch script
├── run.sh                                     # Linux/macOS build & launch script
├── README.md                                  # Complete documentation
└── PRESENTATION_NOTES.md                      # Viva questions, demo script & OOP concept map
```

---

## 📡 REST API Reference

All requests and responses use `application/json; charset=UTF-8` for responses, and form bodies are sent as `application/x-www-form-urlencoded`.

| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| **GET** | `/api/stats` | Public | Returns aggregated live metrics (totalLost, totalFound, pendingClaims, returnedItems). |
| **GET** | `/api/items?type=&category=&status=&q=` | Public | Retrieves all items with optional query parameter filters. |
| **GET** | `/api/items/{id}` | Public | Retrieves full details of a specific item by ID. |
| **GET** | `/api/items/{id}/matches` | Public | Returns scored smart match recommendations of opposite type. |
| **POST** | `/api/items` | Public | Reports a new LOST or FOUND item. |
| **PUT** | `/api/items/{id}` | Admin | Updates item status or details (`X-Admin-Token` required). |
| **DELETE**| `/api/items/{id}` | Admin | Deletes an item from the system (`X-Admin-Token` required). |
| **POST** | `/api/claims` | Public | Submits an ownership claim against a FOUND item. |
| **GET** | `/api/claims` | Admin | Retrieves all submitted claims (`X-Admin-Token` required). |
| **PUT** | `/api/claims/{id}` | Admin | Approves or rejects a claim (`action=APPROVE` or `REJECT`). |
| **POST** | `/api/login` | Public | Authenticates admin credentials (`username`, `password`) and issues token. |
| **POST** | `/api/logout` | Admin | Invalidates active admin session token. |
| **GET** | `/*` | Public | Static file handler serving frontend assets (`index.html`, `style.css`, `script.js`). |

---

## 🧠 Smart Match Algorithm

When viewing any item, the system searches all active items of the opposite type (`LOST` ↔ `FOUND`):
1. **Category Match (+40 pts)**: Awarded if both items share the exact same `Category` enum.
2. **Location Match (+30 pts)**: Awarded if both items were reported at the identical campus location.
3. **Keyword Tokenization (+10 pts each, max +30 pts)**: Words from title and description (ignoring common articles and stop words) are matched against candidate text.
4. **Scoring & Ranking**: Candidate items with score $\ge 30\%$ are sorted in descending order and displayed with a match percentage and factor breakdown.
