const gameId = window.location.pathname.split('/')[2];

function makeMove(column) {
    fetch(`/game/${gameId}/move`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: `column=${column}`
    })
    .then(response => response.json())
    .then(data => {
        if (!data.success) {
            alert(data.message);
            return;
        }

        updateBoard(data.playerMove, 'player-piece');
        if (data.aiMove) {
            updateBoard(data.aiMove, 'ai-piece');
        }

        if (data.gameStatus !== "IN_PROGRESS") {
        alert("Game Finished");
            setTimeout(() => {
                window.location.href = `/game/${gameId}/analysis`;
            }, 1500);
        }
    });
}

function updateBoard(move, pieceClass) {
    updateCell(move.row, move.column, pieceClass);
}

function updateCell(row, col, pieceClass) {
    const cell = document.querySelector(`.board-cell[data-row="${row}"][data-col="${col}"] div`);
    if (cell) {
        cell.className = pieceClass;
    }
}

function resetGame() {
    const form = document.createElement('form');
    form.method = 'POST';
    form.action = `/game/${gameId}/reset`;
    document.body.appendChild(form);
    form.submit();
}

function changeDifficulty() {
    const difficulty = document.getElementById('difficulty').value;
    const form = document.createElement('form');
    form.method = 'POST';
    form.action = `/game/${gameId}/difficulty`;

    const input = document.createElement('input');
    input.type = 'hidden';
    input.name = 'difficulty';
    input.value = difficulty;

    form.appendChild(input);
    document.body.appendChild(form);
    form.submit();
}
