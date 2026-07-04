/**
 * STRYKER COLLAGE VISUAL ENGINE: NETWORK PIPELINE & INTERACTIVES
 */

let socket = null;
const wsUrl = "ws://localhost:8080";

// Telemetry state metrics
let alphaPossessionTicks = 0;
let betaPossessionTicks = 0;
let stats = {
    alpha: { passes: 0, shots: 0, steals: 0 },
    beta: { passes: 0, shots: 0, steals: 0 }
};

let lastProcessedMsg = "";

// Player team lookup mappings
const alphaRoster = [
    "A. Becker", "T. Arnold", "I. Konate", "V. van Dijk", "A. Robertson",
    "A. Mac Allister", "W. Endo", "D. Szoboszlai", "M. Salah", "D. Nunez", "L. Diaz"
];

const betaRoster = [
    "Ederson", "K. Walker", "R. Dias", "M. Akanji", "J. Gvardiol",
    "Rodri", "M. Kovacic", "K. De Bruyne", "B. Silva", "E. Haaland", "J. Doku"
];

const statusTag = document.getElementById("connection-status");

/**
 * Initialize connection to Java WebSocket server.
 */
function connect() {
    console.log(`[NETWORK] Attempting connection to STRYKER Server: ${wsUrl}`);
    socket = new WebSocket(wsUrl);

    socket.onopen = () => {
        console.log("[NETWORK] Connected successfully to STRYKER backend.");
        statusTag.textContent = "WS ACTIVE // 60HZ";
        statusTag.classList.remove("disconnected");
        statusTag.classList.add("connected");
        
        appendEventLog("SYSTEM: Pipeline connection established. Stream active.");
    };

    socket.onmessage = (event) => {
        try {
            const state = JSON.parse(event.data);
            processMatchFrame(state);
        } catch (e) {
            console.error("[NETWORK] Error parsing state frame:", e);
        }
    };

    socket.onclose = () => {
        console.log("[NETWORK] Socket closed. Reconnecting in 3 seconds...");
        statusTag.textContent = "WS DISCONNECTED";
        statusTag.classList.remove("connected");
        statusTag.classList.add("disconnected");
        
        // Auto-reconnection loop
        setTimeout(connect, 3000);
    };

    socket.onerror = (err) => {
        console.error("[NETWORK] WebSocket connection error:", err);
        socket.close();
    };
}

/**
 * Parses and updates DOM layout based on simulated state frame.
 */
function processMatchFrame(state) {
    // 1. Render field vectors onto HTML5 canvas
    if (typeof drawMatchState === "function") {
        drawMatchState(state);
    }

    // 2. Scoreboard updates
    document.getElementById("score-alpha").textContent = state.score.alpha;
    document.getElementById("score-beta").textContent = state.score.beta;
    document.getElementById("tick-clock").textContent = state.tick;

    // 3. Track possession ticks to calculate possession percentage
    if (state.ball.possession === "alpha") {
        alphaPossessionTicks++;
    } else if (state.ball.possession === "beta") {
        betaPossessionTicks++;
    }
    updatePossessionPercentages();

    // 4. Handle unique game events (PASS, SHOT, STEAL, GOAL)
    if (state.event && state.event !== "NONE" && state.eventMsg !== lastProcessedMsg) {
        lastProcessedMsg = state.eventMsg;
        handleNewEvent(state.event, state.eventMsg);
    }
}

/**
 * Processes game events, updates stats arrays, updates alert scraps, and logs chronological feed.
 */
function handleNewEvent(event, message) {
    // Determine which team triggered the event by looking up players in the message
    let actionTeam = "none";
    
    // Check if Alpha players are mentioned in the message
    for (const name of alphaRoster) {
        if (message.includes(name) || message.includes("Team Alpha")) {
            actionTeam = "alpha";
            break;
        }
    }
    
    // Check if Beta players are mentioned
    for (const name of betaRoster) {
        if (message.includes(name) || message.includes("Team Beta")) {
            actionTeam = "beta";
            break;
        }
    }

    // Increment statistics based on event action
    if (actionTeam !== "none") {
        if (event === "PASS") {
            stats[actionTeam].passes++;
            document.getElementById(`stat-${actionTeam}-passes`).textContent = stats[actionTeam].passes;
        } else if (event === "SHOT") {
            stats[actionTeam].shots++;
            document.getElementById(`stat-${actionTeam}-shots`).textContent = stats[actionTeam].shots;
        } else if (event === "STEAL") {
            stats[actionTeam].steals++;
            document.getElementById(`stat-${actionTeam}-tackles`).textContent = stats[actionTeam].steals;
        }
    }

    // Update the visual alert paper scrap (Crimson card)
    const alertScrap = document.getElementById("alert-scrap");
    const alertMsg = document.getElementById("alert-message");
    const alertBadge = alertScrap.querySelector(".alert-badge");

    alertBadge.textContent = `EVENT // ${event}`;
    alertMsg.textContent = message;

    // Add alert flash effect
    alertScrap.classList.add("flash-alert");
    setTimeout(() => alertScrap.classList.remove("flash-alert"), 300);

    // Append message to Chronicle event timeline list
    appendEventLog(message);
}

/**
 * Calculates and updates the possession percentage elements on screen.
 */
function updatePossessionPercentages() {
    const total = alphaPossessionTicks + betaPossessionTicks;
    if (total === 0) return;

    const alphaPct = Math.round((alphaPossessionTicks / total) * 100);
    const betaPct = 100 - alphaPct;

    document.getElementById("stat-alpha-possession").textContent = `${alphaPct}%`;
    document.getElementById("stat-beta-possession").textContent = `${betaPct}%`;
}

/**
 * Appends a list element into the chronological event logging feed.
 */
function appendEventLog(message) {
    const feed = document.getElementById("event-feed-list");
    if (!feed) return;

    const item = document.createElement("div");
    item.className = "feed-item";
    
    // Add current timestamp indicator
    const now = new Date();
    const timeStr = `${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}:${String(now.getSeconds()).padStart(2, '0')}`;
    
    item.innerHTML = `<strong>[${timeStr}]</strong> ${message}`;
    
    // Insert at top of scroll list
    feed.insertBefore(item, feed.firstChild);

    // Prune logs if list exceeds 40 messages to prevent memory leaks
    if (feed.children.length > 40) {
        feed.removeChild(feed.lastChild);
    }
}

// Boot socket connection
connect();
