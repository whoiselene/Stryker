/**
 * STRYKER COLLAGE VISUAL ENGINE: CANVAS RENDERING
 */

const canvas = document.getElementById("pitchCanvas");
const ctx = canvas.getContext("2d");

const SCALE = 10; // Maps 100x60 meter grid to 1000x600 pixels

// Ball heat trail history
let ballTrail = [];
const MAX_TRAIL_LENGTH = 100;

/**
 * Main draw loop called on incoming state frames.
 */
function drawMatchState(state) {
    if (!ctx) return;

    // 1. Clear field with base moss green
    ctx.fillStyle = "#3B7A57";
    ctx.fillRect(0, 0, canvas.width, canvas.height);

    // 2. Draw Screenprinted Pitch Markings
    drawPitchMarkings();

    // 3. Update and Draw Ball Heatmap Trail
    updateBallTrail(state.ball);
    drawBallTrail();

    // 4. Draw Player Nodes
    drawPlayers(state.players);

    // 5. Draw Ball
    drawBall(state.ball);
}

/**
 * Draws pitch lines using overlapping semi-transparent strokes
 * to replicate screenprint alignment imperfections.
 */
function drawPitchMarkings() {
    ctx.save();
    
    // Use the off-white paper color with transparency
    ctx.strokeStyle = "rgba(244, 241, 234, 0.55)";
    ctx.lineWidth = 3.5;
    ctx.lineCap = "round";

    // Subtly offset drawing function for imperfect texture
    const drawHandLine = (x1, y1, x2, y2) => {
        // Draw primary line
        ctx.beginPath();
        ctx.moveTo(x1, y1);
        ctx.lineTo(x2, y2);
        ctx.stroke();

        // Draw secondary slight misalignment line
        ctx.save();
        ctx.strokeStyle = "rgba(244, 241, 234, 0.25)";
        ctx.lineWidth = 1.5;
        ctx.beginPath();
        ctx.moveTo(x1 + (Math.random() - 0.5) * 1.2, y1 + (Math.random() - 0.5) * 1.2);
        ctx.lineTo(x2 + (Math.random() - 0.5) * 1.2, y2 + (Math.random() - 0.5) * 1.2);
        ctx.stroke();
        ctx.restore();
    };

    const drawHandCircle = (cx, cy, r) => {
        ctx.beginPath();
        ctx.arc(cx, cy, r, 0, Math.PI * 2);
        ctx.stroke();

        ctx.save();
        ctx.strokeStyle = "rgba(244, 241, 234, 0.2)";
        ctx.lineWidth = 1.5;
        ctx.beginPath();
        ctx.arc(cx + (Math.random() - 0.5) * 1.5, cy + (Math.random() - 0.5) * 1.5, r + (Math.random() - 0.5) * 0.8, 0, Math.PI * 2);
        ctx.stroke();
        ctx.restore();
    };

    // Boundary Outer Line
    drawHandLine(15, 15, canvas.width - 15, 15);
    drawHandLine(15, canvas.height - 15, canvas.width - 15, canvas.height - 15);
    drawHandLine(15, 15, 15, canvas.height - 15);
    drawHandLine(canvas.width - 15, 15, canvas.width - 15, canvas.height - 15);

    // Center Line
    drawHandLine(canvas.width / 2, 15, canvas.width / 2, canvas.height - 15);

    // Center Circle (Radius: 91.5 pixels)
    drawHandCircle(canvas.width / 2, canvas.height / 2, 91.5);

    // Penalty Boxes (16.5m -> 165px)
    // Left Box
    drawHandLine(15, 138.5, 180, 138.5);
    drawHandLine(180, 138.5, 180, 461.5);
    drawHandLine(15, 461.5, 180, 461.5);
    // Right Box
    drawHandLine(canvas.width - 15, 138.5, canvas.width - 180, 138.5);
    drawHandLine(canvas.width - 180, 138.5, canvas.width - 180, 461.5);
    drawHandLine(canvas.width - 15, 461.5, canvas.width - 180, 461.5);

    // Goal Boxes (5.5m -> 55px)
    // Left
    drawHandLine(15, 201.5, 70, 201.5);
    drawHandLine(70, 201.5, 70, 398.5);
    drawHandLine(15, 398.5, 70, 398.5);
    // Right
    drawHandLine(canvas.width - 15, 201.5, canvas.width - 70, 201.5);
    drawHandLine(canvas.width - 70, 201.5, canvas.width - 70, 398.5);
    drawHandLine(canvas.width - 15, 398.5, canvas.width - 70, 398.5);

    // Goals (Width: 16m -> 160px from y=220 to y=380)
    ctx.strokeStyle = "rgba(230, 57, 70, 0.7)"; // Red Goal Posts
    ctx.lineWidth = 5;
    // Left Goal
    drawHandLine(15, 220, 2, 220);
    drawHandLine(2, 220, 2, 380);
    drawHandLine(15, 380, 2, 380);
    // Right Goal
    drawHandLine(canvas.width - 15, 220, canvas.width - 2, 220);
    drawHandLine(canvas.width - 2, 220, canvas.width - 2, 380);
    drawHandLine(canvas.width - 15, 380, canvas.width - 2, 380);

    ctx.restore();
}

/**
 * Updates the array of ball coordinates for the heatmap.
 */
function updateBallTrail(ballState) {
    ballTrail.push({ x: ballState.x * SCALE, y: ballState.y * SCALE });
    if (ballTrail.length > MAX_TRAIL_LENGTH) {
        ballTrail.shift();
    }
}

/**
 * Draws the fading yellow marker highlights behind the ball trajectory.
 */
function drawBallTrail() {
    ctx.save();
    
    // Draw trail lines as highlighters
    for (let i = 1; i < ballTrail.length; i++) {
        let p1 = ballTrail[i - 1];
        let p2 = ballTrail[i];
        let age = i / ballTrail.length;
        
        ctx.strokeStyle = `rgba(255, 183, 3, ${age * 0.35})`; // Yellow highlighter
        ctx.lineWidth = 14 * age; // gets wider for newer positions
        ctx.lineCap = "round";
        ctx.lineJoin = "round";

        ctx.beginPath();
        ctx.moveTo(p1.x, p1.y);
        ctx.lineTo(p2.x, p2.y);
        ctx.stroke();
    }
    
    ctx.restore();
}

/**
 * Extracts initials from standard player name (e.g. "M. Salah" -> "MS").
 */
function getInitials(name) {
    if (!name) return "";
    let parts = name.replace(".", "").split(" ");
    if (parts.length >= 2) {
        return (parts[0][0] + parts[1][0]).toUpperCase();
    }
    return name.slice(0, 2).toUpperCase();
}

/**
 * Draws players as circular stamps with hard offset drop shadows.
 */
function drawPlayers(players) {
    players.forEach(p => {
        const px = p.x * SCALE;
        const py = p.y * SCALE;
        const r = p.radius * SCALE;

        ctx.save();

        // 1. Draw Flat Drop Shadow
        ctx.fillStyle = "rgba(10, 10, 10, 0.65)"; // Dark ink shadow
        ctx.beginPath();
        ctx.arc(px + 4, py + 4, r, 0, Math.PI * 2);
        ctx.fill();

        // 2. Draw Stamp Base Cutout
        ctx.beginPath();
        ctx.arc(px, py, r, 0, Math.PI * 2);
        
        if (p.team === "alpha") {
            ctx.fillStyle = "#FFFFFF"; // Clean white paper cutout
            ctx.strokeStyle = "#1E3B2B"; // Forest border
        } else {
            ctx.fillStyle = "#0A0A0A"; // Ink black blocks
            ctx.strokeStyle = "#FFFFFF"; // Contrasting white border
        }
        ctx.lineWidth = 2.5;
        ctx.fill();
        ctx.stroke();

        // 3. Draw Player Heading Pointer (small stamp tail)
        const angle = p.theta;
        const pointerX = px + Math.cos(angle) * r;
        const pointerY = py + Math.sin(angle) * r;

        ctx.fillStyle = p.team === "alpha" ? "#1E3B2B" : "#FFFFFF";
        ctx.beginPath();
        ctx.arc(pointerX, pointerY, 3, 0, Math.PI * 2);
        ctx.fill();

        // 4. Draw Initials inside Stamp
        ctx.fillStyle = p.team === "alpha" ? "#0A0A0A" : "#FFFFFF";
        ctx.font = `bold 10px 'Courier Prime', monospace`;
        ctx.textAlign = "center";
        ctx.textBaseline = "middle";
        const initials = getInitials(p.name);
        ctx.fillText(initials, px, py + 1);

        ctx.restore();
    });
}

/**
 * Draws the ball with a halftone dot-matrix overlay.
 */
function drawBall(ballState) {
    const bx = ballState.x * SCALE;
    const by = ballState.y * SCALE;
    const r = ballState.radius * SCALE;

    ctx.save();

    // 1. Draw Ball Drop Shadow
    ctx.fillStyle = "rgba(10, 10, 10, 0.4)";
    ctx.beginPath();
    ctx.arc(bx + 3, by + 3, r, 0, Math.PI * 2);
    ctx.fill();

    // 2. Setup Clipping for Ball Body
    ctx.beginPath();
    ctx.arc(bx, by, r, 0, Math.PI * 2);
    ctx.clip();

    // 3. Fill with Warm Yellow Marker Accent
    ctx.fillStyle = "#FFB703";
    ctx.fill();

    // 4. Draw Halftone Dot-Matrix Pattern
    ctx.fillStyle = "#0A0A0A";
    const dotSpacing = 4.5;
    const dotRadius = 1.25;

    // Draw grid of dots inside the ball's clipping boundaries
    for (let x = bx - r; x < bx + r; x += dotSpacing) {
        for (let y = by - r; y < by + r; y += dotSpacing) {
            ctx.beginPath();
            ctx.arc(x, y, dotRadius, 0, Math.PI * 2);
            ctx.fill();
        }
    }

    ctx.restore();

    // 5. Draw Ball Outline
    ctx.save();
    ctx.strokeStyle = "#0A0A0A";
    ctx.lineWidth = 2.5;
    ctx.beginPath();
    ctx.arc(bx, by, r, 0, Math.PI * 2);
    ctx.stroke();
    ctx.restore();
}
